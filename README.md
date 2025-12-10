# 🚀 Project-Leap: API Monitoring & Observability Platform

A complete full-stack platform for tracking, monitoring, and analyzing API requests across microservices in real-time.

---

## 📋 Table of Contents

- [Features](#-features)
- [Architecture](#-architecture)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Setup & Installation](#-setup--installation)
- [API Documentation](#-api-documentation)
- [Deployment](#-deployment)
- [Testing](#-testing)

---

## ✨ Features

### 🔐 Authentication & Security
- ✅ JWT-based authentication
- ✅ Secure signup/login endpoints
- ✅ Protected API routes
- ✅ Session management with cookies

### 📊 API Tracking & Logging
- ✅ Real-time API call tracking
- ✅ Captures: endpoint, method, status, latency, request/response sizes
- ✅ Batch log ingestion (`POST /api/logs/batch`)
- ✅ Advanced filtering (service, endpoint, status, slow, broken, rate-limited)
- ✅ Time-window filtering (1h, 24h, 7d)

### 🚨 Alerting System
- ✅ Auto-creates alerts for:
  - Latency > 500ms (WARNING/CRITICAL)
  - Status codes >= 500 (CRITICAL)
  - Rate limit exceeded (MEDIUM)
- ✅ Alert severity levels (LOW, MEDIUM, HIGH, CRITICAL)
- ✅ Alert resolution tracking

### 📈 Dashboard & Analytics
- ✅ Real-time dashboard with auto-refresh
- ✅ Service-centric analytics
- ✅ KPIs: Slow APIs, Broken APIs, Rate Limit Hits
- ✅ Latency trend charts
- ✅ Top 5 slow endpoints
- ✅ Error rate graphs
- ✅ Success vs Error distribution
- ✅ Live traffic table with filters

### 🔧 Rate Limiting
- ✅ Token-bucket rate limiter
- ✅ Per-service rate limiting (100 req/sec default)
- ✅ Configurable via API (`/api/rate-limit`)
- ✅ Rate-limit-hit event logging
- ✅ Non-blocking (requests continue, events logged)

### 🗄️ Database Architecture
- ✅ **Dual MongoDB Configuration**
  - `logsdb`: Raw API logs, rate-limit events
  - `metadb`: Users, alerts, incidents, rate-limit configs
- ✅ Separate MongoTemplates and Transaction Managers
- ✅ Optimistic locking for incident resolution

### 🔄 Concurrency & Safety
- ✅ Optimistic locking with `@Version` for incidents
- ✅ Thread-safe rate limiter
- ✅ Transaction management for dual DB operations

---

## 🏗️ Architecture

```
┌─────────────────┐
│  Next.js UI     │
│  (Frontend)     │
└────────┬────────┘
         │ REST API
         │ (JWT Auth)
         ▼
┌─────────────────┐
│ Spring Boot     │
│ Collector       │
│ Service         │
└─────┬───────┬───┘
      │       │
      ▼       ▼
  ┌──────┐ ┌──────┐
  │logsdb│ │metadb│
  │Mongo │ │Mongo │
  └──────┘ └──────┘
```

### Data Flow

1. **API Tracking Client** → Tracks API calls → Sends to Collector
2. **Collector Service** → Receives logs → Stores in `logsdb`
3. **Alert Engine** → Evaluates rules → Creates alerts in `metadb`
4. **Dashboard** → Fetches data → Displays analytics

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| **Frontend** | Next.js 14, TypeScript, TailwindCSS, Recharts, React Hook Form, Zod |
| **Backend** | Spring Boot 3.3.2, Kotlin 1.9.23, Spring Security, JWT |
| **Database** | MongoDB (Dual: logsdb + metadb) |
| **Build Tool** | Gradle 8.x |
| **Authentication** | JWT (io.jsonwebtoken) |
| **API Client** | Axios |

---

## 📁 Project Structure

```
Project-Leap/
├── backend/
│   ├── collector-service/          # Main Spring Boot service
│   │   ├── src/main/kotlin/
│   │   │   ├── auth/               # Authentication
│   │   │   ├── alerts/             # Alert management
│   │   │   ├── incidents/          # Incident tracking
│   │   │   ├── logs/                # Log ingestion
│   │   │   ├── services/           # Service analytics
│   │   │   ├── ratelimit/          # Rate limiting
│   │   │   ├── tracking/           # API tracking client
│   │   │   ├── config/             # Configuration (MongoDB, CORS)
│   │   │   ├── security/           # JWT, Security config
│   │   │   └── model/              # Data models
│   │   └── src/main/resources/
│   │       └── application.yml     # Configuration
│   └── shared-contracts/           # Shared DTOs
├── frontend/
│   └── dashboard/                  # Next.js dashboard
│       ├── app/                    # App router pages
│       ├── components/            # UI components
│       ├── lib/                    # API clients, utilities
│       └── middleware.ts          # Route protection
└── README.md
```

---

## 🚀 Setup & Installation

### Prerequisites

- Java 21+
- Node.js 18+
- MongoDB (local or Atlas)
- Gradle 8.x

### 1. Clone Repository

```bash
git clone https://github.com/Kashyap0319/Project-Leap.git
cd Project-Leap
```

### 2. MongoDB Setup

**Local MongoDB:**
```bash
# Start MongoDB (if not running)
mongod
```

**MongoDB Atlas:**
- Create two databases: `logsdb` and `metadb`
- Get connection strings

### 3. Backend Setup

```bash
cd backend/collector-service

# Set environment variables (or use application.yml)
export LOGS_MONGO_URI=mongodb://localhost:27017/logsdb
export META_MONGO_URI=mongodb://localhost:27017/metadb
export JWT_SECRET=your-secret-key-here

# Run backend
cd ../..
./gradlew :backend:collector-service:bootRun
```

Backend will start on **http://localhost:8080**

### 4. Frontend Setup

```bash
cd frontend/dashboard

# Install dependencies
npm install

# Set environment variable (optional, defaults to localhost:8080)
export NEXT_PUBLIC_API_BASE_URL=http://localhost:8080

# Run frontend
npm run dev
```

Frontend will start on **http://localhost:3000**

### 5. Configuration

**Backend (`application.yml`):**
```yaml
server:
  port: 8080

mongo:
  logs:
    uri: mongodb://localhost:27017/logsdb
  meta:
    uri: mongodb://localhost:27017/metadb

jwt:
  secret: ${JWT_SECRET:your-secret-key}
  expiration: 86400000

rate-limit:
  default: 100

monitoring:
  rateLimit:
    default: 100
```

---

## 📡 API Documentation

### Authentication

#### Signup
```http
POST /auth/signup
Content-Type: application/json

{
  "username": "testuser",
  "email": "test@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

#### Login
```http
POST /auth/login
Content-Type: application/json

{
  "username": "testuser",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### Logs API

#### Create Log (Single)
```http
POST /api/logs
Authorization: Bearer <token>
Content-Type: application/json

{
  "service": "test-service",
  "endpoint": "/api/test",
  "method": "GET",
  "statusCode": 200,
  "latencyMs": 150,
  "requestSize": 100,
  "responseSize": 500,
  "rateLimited": false
}
```

#### Create Logs (Batch)
```http
POST /api/logs/batch
Authorization: Bearer <token>
Content-Type: application/json

[
  {
    "service": "test-service",
    "endpoint": "/api/test1",
    "method": "GET",
    "statusCode": 200,
    "latencyMs": 150,
    "requestSize": 100,
    "responseSize": 500
  },
  {
    "service": "test-service",
    "endpoint": "/api/test2",
    "method": "POST",
    "statusCode": 500,
    "latencyMs": 600,
    "requestSize": 200,
    "responseSize": 100
  }
]
```

#### Get Logs
```http
GET /api/logs?service=test-service&slow=true&size=10&page=0
Authorization: Bearer <token>
```

**Query Parameters:**
- `service` - Filter by service name
- `endpoint` - Filter by endpoint
- `status` - Filter by status code
- `slow` - Show only slow APIs (>500ms)
- `broken` - Show only broken APIs (5xx)
- `rateLimited` - Show rate-limited requests
- `errorsOnly` - Show errors (>=400)
- `q` - Search query (searches service/endpoint/method)
- `window` - Time window (1h, 24h, 7d)
- `page` - Page number (default: 0)
- `size` - Page size (default: 10)

### Alerts API

#### Get Alerts
```http
GET /api/alerts
Authorization: Bearer <token>
```

**Response:**
```json
[
  {
    "id": "...",
    "service": "test-service",
    "endpoint": "/api/slow",
    "type": "LATENCY",
    "severity": "WARNING",
    "message": "High latency detected: 600ms",
    "detectedAt": "2025-12-10T10:00:00Z",
    "resolved": false
  }
]
```

#### Resolve Alert
```http
POST /api/alerts/{id}/resolve
Authorization: Bearer <token>
```

### Incidents API

#### Get Incidents
```http
GET /api/incidents
Authorization: Bearer <token>
```

#### Resolve Incident (with Optimistic Locking)
```http
POST /api/incidents/{id}/resolve
Authorization: Bearer <token>
Content-Type: application/json

{
  "version": 1
}
```

**Response (on version mismatch):**
```json
{
  "status": 409,
  "error": "Optimistic locking failure",
  "message": "Incident version mismatch..."
}
```

### Services API

#### Get Services Summary
```http
GET /api/services
Authorization: Bearer <token>
```

**Response:**
```json
[
  {
    "name": "test-service",
    "requests": 100,
    "avgLatency": 250.5,
    "errorRate": 0.05,
    "latencyTrend": [
      {
        "timestamp": "2025-12-10T10:00:00Z",
        "latencyMs": 150
      }
    ],
    "endpoints": [
      {
        "path": "GET /api/test",
        "method": "GET",
        "avgLatency": 150.0,
        "p95Latency": 200.0,
        "errorRate": 0.0,
        "requestCount": 10
      }
    ]
  }
]
```

#### Get Dashboard Widgets
```http
GET /api/services/widgets?window=24h
Authorization: Bearer <token>
```

**Response:**
```json
{
  "slowApiCount": 5,
  "brokenApiCount": 2,
  "rateLimitViolations": 1,
  "top5SlowEndpoints": [
    {
      "endpoint": "GET /api/slow",
      "avgLatency": 600.0,
      "count": 3
    }
  ],
  "errorRateGraph": [
    {
      "timestamp": "2025-12-10T10",
      "errorRate": 5.0,
      "totalRequests": 100,
      "errors": 5
    }
  ]
}
```

### Rate Limit API

#### Get Rate Limit Configs
```http
GET /api/rate-limit
Authorization: Bearer <token>
```

#### Set Rate Limit Override
```http
POST /api/rate-limit
Authorization: Bearer <token>
Content-Type: application/json

{
  "service": "test-service",
  "limit": 120
}
```

#### Get Rate Limit for Service
```http
GET /api/rate-limit/{service}
Authorization: Bearer <token>
```

---

## 🌐 Deployment

### Vercel Deployment (Frontend)

1. **Install Vercel CLI:**
```bash
npm i -g vercel
```

2. **Deploy:**
```bash
cd frontend/dashboard
vercel
```

3. **Set Environment Variables in Vercel Dashboard:**
- `NEXT_PUBLIC_API_BASE_URL` - Your backend API URL

### Backend Deployment

**Option 1: Cloud (Railway, Render, Heroku)**
```bash
# Build JAR
./gradlew :backend:collector-service:bootJar

# Deploy JAR with environment variables:
# LOGS_MONGO_URI, META_MONGO_URI, JWT_SECRET
```

**Option 2: Docker**
```bash
docker build -t project-leap-backend .
docker run -p 8080:8080 \
  -e LOGS_MONGO_URI=mongodb://... \
  -e META_MONGO_URI=mongodb://... \
  -e JWT_SECRET=... \
  project-leap-backend
```

---

## 🧪 Testing

### Quick API Tests (PowerShell)

```powershell
# 1. Signup
$signup = Invoke-RestMethod -Uri "http://localhost:8080/auth/signup" `
  -Method POST -ContentType "application/json" `
  -Body '{"username":"testuser","email":"test@example.com","password":"password123"}'
$token = $signup.token

# 2. Create Test Logs
$headers = @{ "Authorization" = "Bearer $token"; "Content-Type" = "application/json" }
$log = @{
  service = "test-service"
  endpoint = "/api/test"
  method = "GET"
  statusCode = 200
  latencyMs = 150
  requestSize = 100
  responseSize = 500
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/logs" `
  -Method POST -Headers $headers -Body $log

# 3. Get Logs
Invoke-RestMethod -Uri "http://localhost:8080/api/logs?size=5" `
  -Method GET -Headers $headers

# 4. Get Alerts
Invoke-RestMethod -Uri "http://localhost:8080/api/alerts" `
  -Method GET -Headers $headers

# 5. Get Services
Invoke-RestMethod -Uri "http://localhost:8080/api/services" `
  -Method GET -Headers $headers
```

### Automated Tests

```bash
# Run backend tests
./gradlew :backend:collector-service:test

# Run frontend tests
cd frontend/dashboard
npm test
```

---

## 🎯 Key Features Implemented

### ✅ Assignment Requirements Met

1. **API Tracking Client** ✅
   - Reusable library for Spring Boot services
   - Tracks all required fields
   - Built-in rate limiter
   - Batch sending support

2. **Central Collector Service** ✅
   - Dual MongoDB connections
   - JWT authentication
   - Alert auto-creation
   - Incident management with optimistic locking
   - Rate limiting system

3. **Next.js Dashboard** ✅
   - Login/Signup pages
   - Protected routes
   - Logs explorer with filters
   - Dashboard widgets
   - Service analytics
   - Alert viewer
   - Incident resolution UI

4. **Rate Limiting** ✅
   - Token-bucket algorithm
   - Per-service limits (100 req/sec default)
   - Configurable via API
   - Non-blocking behavior

5. **Concurrency Safety** ✅
   - Optimistic locking for incidents
   - Version-based conflict detection
   - Transaction management

---

## 📊 Database Schemas

### Logs Database (`logsdb`)

**Collection: `logs`**
```kotlin
{
  id: String,
  service: String,
  endpoint: String,
  method: String,
  statusCode: Int,
  latencyMs: Long,
  requestSize: Long,
  responseSize: Long,
  rateLimited: Boolean,
  timestamp: Instant
}
```

### Metadata Database (`metadb`)

**Collection: `users`**
```kotlin
{
  id: String,
  username: String,
  email: String,
  password: String (hashed),
  role: Role (USER/ADMIN)
}
```

**Collection: `alerts`**
```kotlin
{
  id: String,
  service: String,
  endpoint: String,
  type: String (LATENCY/ERROR/RATE_LIMIT),
  severity: String (LOW/MEDIUM/HIGH/CRITICAL),
  message: String,
  detectedAt: Instant,
  resolved: Boolean,
  resolvedAt: Instant?
}
```

**Collection: `incidents`**
```kotlin
{
  id: String,
  alertId: String,
  service: String,
  endpoint: String,
  type: String,
  severity: String,
  message: String,
  firstSeen: Instant,
  lastSeen: Instant,
  occurrences: Int,
  resolved: Boolean,
  version: Long (for optimistic locking)
}
```

---

## 🔧 Configuration

### Environment Variables

**Backend:**
- `LOGS_MONGO_URI` - MongoDB URI for logs database
- `META_MONGO_URI` - MongoDB URI for metadata database
- `JWT_SECRET` - Secret key for JWT tokens

**Frontend:**
- `NEXT_PUBLIC_API_BASE_URL` - Backend API URL (default: http://localhost:8080)

---

## 🚀 Quick Start

```bash
# 1. Start MongoDB
mongod

# 2. Start Backend (Terminal 1)
cd backend
../gradlew :backend:collector-service:bootRun

# 3. Start Frontend (Terminal 2)
cd frontend/dashboard
npm install
npm run dev

# 4. Open Browser
# http://localhost:3000
```

---

## 📝 API Tracking Client Usage

```kotlin
// In your Spring Boot service
val trackingClient = ApiTrackingClient.builder()
    .collectorUrl("http://localhost:8080")
    .serviceName("my-service")
    .jwtSecret("your-secret")
    .rateLimit(100L) // 100 req/sec
    .build()

// Track API call
trackingClient.trackApiCall(
    endpoint = "/api/users",
    method = "GET",
    statusCode = 200,
    latencyMs = 150,
    requestSize = 100,
    responseSize = 500
)
```

---

## 🎨 Dashboard Features

### Main Dashboard
- **Metrics Cards**: Slow APIs, Broken APIs, Rate Limit Hits, Total APIs
- **Service KPIs**: Per-service analytics cards
- **Charts**: Success vs Error pie chart, Top 5 slow endpoints bar chart
- **Error Rate Trend**: Hourly error rate graph
- **Live Traffic Table**: Real-time logs with filters
- **Alerts Panel**: Recent alerts with severity badges
- **Incidents Panel**: Open incidents with resolve button

### Services Page
- **Service Selection**: Click to view service analytics
- **KPIs**: Requests, Avg Latency, Error Rate
- **Latency Trend Chart**: Time-series latency data
- **Endpoints Table**: Per-endpoint performance metrics

### Logs Page
- **Advanced Filters**: Service, Method, Status, Time window
- **Search**: Quick search across logs
- **Pagination**: Navigate through large datasets

---

## 🔐 Security

- JWT-based authentication
- Password hashing with BCrypt
- Protected API routes
- CORS configuration
- Input validation
- SQL injection protection (MongoDB)

---

## 📈 Performance

- Handles 50+ concurrent log writes
- Efficient MongoDB queries with indexes
- Batch log ingestion
- Auto-refresh with configurable intervals
- Optimized dashboard rendering

---

## 🐛 Troubleshooting

### Backend won't start
- Check MongoDB is running
- Verify environment variables
- Check port 8080 is available

### Frontend can't connect to backend
- Verify `NEXT_PUBLIC_API_BASE_URL` is correct
- Check CORS configuration
- Verify backend is running

### No data showing in dashboard
- Create test logs via API
- Check MongoDB connections
- Verify JWT token is valid

---

## 📄 License

This project is part of an academic assignment.

---

## 👨‍💻 Developer

**Shreyansh Kashyap**  
Full Stack Developer

---

## 🎉 Project Status

✅ **FULLY FUNCTIONAL** - All features implemented and tested

- ✅ Dual MongoDB configuration
- ✅ Rate limiting system
- ✅ API tracking client
- ✅ Alert auto-creation
- ✅ Dashboard with real-time updates
- ✅ Service analytics
- ✅ Incident management
- ✅ JWT authentication
- ✅ All assignment requirements met

---

**Ready for production deployment! 🚀**
