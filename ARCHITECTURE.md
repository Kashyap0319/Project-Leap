# Project Leap - Architecture Documentation

## System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         Client Browser                           │
│                    (Next.js Frontend)                            │
│                   http://localhost:3000                          │
└────────────────────────┬────────────────────────────────────────┘
                         │ HTTP/REST + JWT
                         │
┌────────────────────────▼────────────────────────────────────────┐
│                   Spring Boot Backend                            │
│                   http://localhost:8080                          │
│                                                                   │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │              Security Layer                              │   │
│  │  • JWT Authentication Filter                            │   │
│  │  • CORS Configuration                                    │   │
│  │  • BCrypt Password Encoding                             │   │
│  └─────────────────────────────────────────────────────────┘   │
│                         │                                        │
│  ┌─────────────────────▼───────────────────────────────────┐   │
│  │           Rate Limiting Layer (Bucket4j)                │   │
│  │  • Token Bucket Algorithm                               │   │
│  │  • Per-User, Per-Endpoint Buckets                      │   │
│  │  • Configurable Capacity & Refill Rate                 │   │
│  └─────────────────────────────────────────────────────────┘   │
│                         │                                        │
│  ┌─────────────────────▼───────────────────────────────────┐   │
│  │              Controller Layer                            │   │
│  │  • AuthController - /api/auth/*                         │   │
│  │  • LogController - /api/logs/*                          │   │
│  │  • AlertController - /api/alerts/*                      │   │
│  └─────────────────────────────────────────────────────────┘   │
│                         │                                        │
│  ┌─────────────────────▼───────────────────────────────────┐   │
│  │               Service Layer                              │   │
│  │  • UserService                                           │   │
│  │  • LogService (with Semaphore for concurrency)          │   │
│  │  • AlertService (error rate monitoring)                 │   │
│  └─────────────────────────────────────────────────────────┘   │
│                         │                                        │
│  ┌─────────────────────▼───────────────────────────────────┐   │
│  │           Repository Layer (Spring Data)                │   │
│  │  Logs DB Repos          │    Metadata DB Repos          │   │
│  │  • LogRepository        │    • UserRepository           │   │
│  │                          │    • AlertRepository          │   │
│  │                          │    • SystemMetadataRepository │   │
│  │                          │    • RateLimitStatRepository  │   │
│  └──────────┬───────────────┴──────────────┬────────────────┘   │
└─────────────┼──────────────────────────────┼────────────────────┘
              │                              │
┌─────────────▼──────────────┐  ┌───────────▼────────────────────┐
│     MongoDB - Logs DB      │  │   MongoDB - Metadata DB        │
│  mongodb://localhost:27017 │  │  mongodb://localhost:27018     │
│                             │  │                                │
│  Collections:               │  │  Collections:                  │
│  • logs (with @Version)    │  │  • users                       │
│                             │  │  • alerts                      │
│                             │  │  • metadata                    │
│                             │  │  • rate_limit_stats            │
│                             │  │                                │
│  Characteristics:           │  │  Characteristics:              │
│  • High write throughput   │  │  • Lower volume                │
│  • Time-series data        │  │  • Higher consistency          │
│  • Optimistic locking      │  │  • User/config data            │
└────────────────────────────┘  └────────────────────────────────┘
```

## Data Flow

### 1. User Authentication Flow
```
Browser → POST /api/auth/login
    ↓
AuthController validates credentials
    ↓
UserService checks UserRepository (Metadata DB)
    ↓
JwtTokenProvider generates JWT token
    ↓
Token returned to browser
    ↓
Browser stores token in localStorage
    ↓
All subsequent requests include: Authorization: Bearer <token>
```

### 2. Log Creation Flow
```
Browser → POST /api/logs + JWT
    ↓
JwtAuthenticationFilter validates token
    ↓
RateLimiter checks bucket (per user+endpoint)
    ↓
LogController receives request
    ↓
LogService.createLog() with Semaphore (max 50 concurrent)
    ↓
LogRepository.save() to Logs DB (with @Version)
    ↓
Async: AlertService checks error rate
    ↓
If threshold exceeded → AlertRepository.save() to Metadata DB
    ↓
Log entry returned to browser
```

### 3. Alert Resolution Flow
```
Browser → POST /api/alerts/{id}/resolve + JWT
    ↓
JwtAuthenticationFilter validates token
    ↓
RateLimiter checks bucket
    ↓
AlertController receives request
    ↓
AlertService.resolveAlert() with optimistic locking
    ↓
AlertRepository updates alert (Metadata DB)
    ↓
Updated alert returned to browser
```

## Concurrency Handling

### Semaphore-based Write Control
```kotlin
private val writeSemaphore = Semaphore(50)

suspend fun createLog(logEntry: LogEntry): LogEntry {
    writeSemaphore.acquire()  // Block if 50 concurrent writes
    try {
        return logRepository.save(logEntry)
    } finally {
        writeSemaphore.release()
    }
}
```

### Kotlin Coroutines for Bulk Operations
```kotlin
suspend fun createLogsBulk(entries: List<LogEntry>) = coroutineScope {
    entries.map { entry ->
        async { createLog(entry) }  // Parallel execution
    }.awaitAll()
}
```

### Optimistic Locking
```kotlin
@Version
val version: Long? = null  // Auto-incremented on each update
```
- MongoDB compares version on update
- Throws OptimisticLockingFailureException if mismatch
- Application retries or returns error

## Rate Limiting Details

### Token Bucket Configuration
```
Bucket Capacity: 100 tokens
Refill Rate: 10 tokens/second
Refill Interval: 1 second

Example:
  Time 0s: Bucket has 100 tokens
  User makes 100 requests → 0 tokens left
  Time 1s: Bucket refills to 10 tokens
  User makes 11th request → REJECTED (429)
  Time 2s: Bucket has 20 tokens
```

### Implementation
```kotlin
val bucket = Bucket.builder()
    .addLimit(Bandwidth.classic(capacity, 
        Refill.intervally(refillTokens, duration)))
    .build()

if (!bucket.tryConsume(1)) {
    throw ResponseStatusException(TOO_MANY_REQUESTS)
}
```

## Alert System Logic

### Error Rate Monitoring
```
1. On log creation with level ERROR/FATAL
   ↓
2. Count errors in last N minutes (configurable)
   ↓
3. If count > threshold (configurable)
   ↓
4. Check if alert already exists for source
   ↓
5. If not, create new alert with severity based on multiplier:
   - 3x threshold → CRITICAL
   - 2x threshold → HIGH
   - 1x threshold → MEDIUM
```

### Alert Lifecycle
```
OPEN → ACKNOWLEDGED → RESOLVED
  ↓
DISMISSED (false positive)
```

## Security Features

### JWT Token Structure
```json
{
  "header": {
    "alg": "HS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "username",
    "iat": 1234567890,
    "exp": 1234654290
  },
  "signature": "HMAC-SHA256(header + payload + secret)"
}
```

### Password Security
- BCrypt hashing with strength 10
- Salt automatically generated per password
- One-way hash (cannot be reversed)

### CORS Policy
- Allowed Origin: http://localhost:3000
- Allowed Methods: GET, POST, PUT, DELETE, OPTIONS
- Credentials: true (for cookies/auth headers)

## Database Schema

### Logs Database

#### logs Collection
```json
{
  "_id": "ObjectId",
  "timestamp": "ISODate",
  "level": "ERROR|WARN|INFO|DEBUG|TRACE|FATAL",
  "message": "string",
  "source": "string",
  "metadata": {
    "key": "value"
  },
  "tags": ["string"],
  "userId": "string",
  "version": 1,
  "_class": "com.projectleap.model.LogEntry"
}
```

### Metadata Database

#### users Collection
```json
{
  "_id": "ObjectId",
  "username": "string (unique)",
  "password": "bcrypt-hash",
  "email": "string (unique)",
  "roles": ["USER", "ADMIN"],
  "enabled": true,
  "createdAt": "ISODate",
  "version": 1
}
```

#### alerts Collection
```json
{
  "_id": "ObjectId",
  "type": "ERROR_RATE|RATE_LIMIT|SYSTEM|CUSTOM",
  "severity": "LOW|MEDIUM|HIGH|CRITICAL",
  "title": "string",
  "description": "string",
  "source": "string",
  "threshold": 10.0,
  "currentValue": 15.0,
  "status": "OPEN|ACKNOWLEDGED|RESOLVED|DISMISSED",
  "createdAt": "ISODate",
  "resolvedAt": "ISODate?",
  "resolvedBy": "string?",
  "resolution": "string?",
  "version": 1
}
```

## Performance Characteristics

### Expected Throughput
- **Concurrent Writes**: 50 simultaneous log entries
- **Read Queries**: Unlimited (MongoDB read scaling)
- **Rate Limit**: 100 requests/user/endpoint initially, then 10/second

### Scalability Considerations
1. **Horizontal Scaling**: Deploy multiple backend instances behind load balancer
2. **MongoDB Sharding**: Shard logs collection by timestamp or source
3. **Caching**: Add Redis for rate limit buckets (current: in-memory)
4. **CDN**: Serve frontend from CDN
5. **Read Replicas**: Add MongoDB read replicas for queries

## Monitoring & Observability

### Key Metrics to Monitor
1. **Rate Limit Stats**: Track blocked requests per user
2. **Error Rates**: Monitor alert generation frequency
3. **Write Latency**: Track log write performance
4. **MongoDB Connections**: Monitor connection pool usage
5. **JVM Metrics**: Heap usage, GC pauses

### Logging Strategy
- Application logs → Logs DB (self-monitoring)
- Error logs trigger alerts
- Audit trail in Metadata DB

## Deployment Architecture

### Development
```
Frontend: npm run dev (port 3000)
Backend: ./gradlew bootRun (port 8080)
MongoDB: Docker (ports 27017, 27018)
```

### Production (Docker)
```
docker-compose up -d
  ├── frontend (nginx + Node.js)
  ├── backend (Java 17 JRE)
  ├── mongodb-logs (persistent volume)
  └── mongodb-metadata (persistent volume)
```

### Cloud Deployment (Example: AWS)
```
Frontend → S3 + CloudFront
Backend → ECS/EKS (Auto-scaling)
MongoDB → DocumentDB or Atlas
Load Balancer → ALB
```

## Configuration Management

### Environment Variables
```bash
# Backend
SPRING_DATA_MONGODB_URI=mongodb://...
MONGODB_METADATA_URI=mongodb://...
JWT_SECRET=...
RATELIMIT_CAPACITY=100
ALERT_THRESHOLD_ERROR_RATE=10

# Frontend
NEXT_PUBLIC_API_URL=http://backend:8080
```

### Profile-based Config
- `application.properties` - Default
- `application-dev.properties` - Development
- `application-prod.properties` - Production
