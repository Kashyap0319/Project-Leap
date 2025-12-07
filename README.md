# Project Leap – API Monitoring & Observability

## Architecture
- **Collector Service (Spring Boot + Kotlin)**
  - Receives batched `LogEvent` payloads over REST at `/api/logs/batch`.
  - Dual Mongo setup: `logs` database for raw traffic, `meta` database for users, rate-limit overrides, alerts, and incidents. Provide separate URIs via `LOGS_MONGO_URI` and `META_MONGO_URI` (can point to different Mongo instances/clusters to satisfy “two separate databases” requirement).
  - JWT-authenticated APIs: alerts, incidents (with optimistic locking), rate-limit overrides, service analytics.
  - Alert rules: latency > 500ms, status 5xx, or rate-limit hit. Each alert also bumps/create an incident record.
- **Tracker Library (Spring Boot starter, Kotlin)**
  - Server filter + OkHttp interceptor to capture inbound/outbound requests.
  - Per-service, token-bucket rate limiter; overrides pulled from collector `/api/rate-limit` on startup.
  - Buffered, retried batching to `/api/logs/batch` with JWT signed by service name/secret.
- **Next.js Dashboard (App Router)**
  - Auth (signup/login) stores JWT client-side; protected routes via middleware.
  - Screens: dashboard, logs explorer (filters + pagination), alerts, services analytics, incidents resolution.
  - Uses `NEXT_PUBLIC_API_BASE_URL` to target collector.

## Data Model / Schemas
- **logs (logs DB)**: `service`, `endpoint`, `method`, `status`, `latencyMs`, `rateLimited`, `timestamp`, `requestId`, `requestSizeBytes`, `responseSizeBytes`.
- **alerts (meta DB)**: `type`, `message`, `service`, `endpoint`, `triggeredAt`, `severity`, exposed as `detectedAt` to clients.
- **incidents (meta DB)**: optimistic locked via `@Version`; `status`, `occurrences`, `firstSeen`, `lastSeen`, `severity`, `resolvedAt`.
- **rate_limit_configs (meta DB)**: per-service `limitPerSecond`, `burst` overrides.
- **users (meta DB)**: `username`, `passwordHash`, `createdAt`.

## Dual Mongo Setup
- `MongoConfig` declares two `MongoTemplate` beans (`logsTemplate`, `metaTemplate`) and two `MongoTransactionManager`s (`logsTxManager`, `metaTxManager`).
- Logs pipeline writes to `logsTemplate`; metadata (users, alerts, incidents, rate-limit overrides) uses `metaTemplate` with explicit transaction demarcation on mutating flows.

## Rate Limiter
- Default: 100 req/s token bucket, burst = 100 (configurable via `monitoring.rateLimit` in services).
- Overrides: collector `/api/rate-limit` stores overrides per service; tracker fetches override on startup (JWT-auth) and applies to limiter. Exceeding limit only flags the log with `rateLimited=true`; request still proceeds.
- Rate-limit-hit alerts emitted by collector rules.

## Concurrency & Consistency
- Incidents use optimistic locking and meta transaction manager for resolves and touch/create operations.
- Rate-limit config upserts and user signup also wrapped in meta transactions.
- Log ingestion bulk-writes to logs DB, then alert/incident creation happens in meta DB.
- Concurrency smoke test is provided (see `collector-service/src/test/kotlin/.../ConcurrencySmokeTest.kt`).

## API Surface (collector)
- `POST /auth/signup|/login` → JWT.
- `POST /api/logs/batch` (auth) → accept list of `LogEvent`.
- `GET /api/logs` (auth) → filters: `service`, `endpoint`, `status` (code or 2xx/4xx/5xx/429), `slow`, `broken`, `rateLimited`, `errorsOnly`, `q`, `window` (1h|24h|7d), `startTs`, `endTs`, paging `page`, `size`.
- `GET /api/alerts` (auth) → filters `service`, `endpoint`, `type`, `limit`; returns `detectedAt`.
- `GET /api/incidents` (auth) → open incidents; `PATCH /api/incidents/{id}/resolve` with `version` for optimistic resolution (POST supported for compatibility).
- `GET|POST /api/rate-limit` (auth) → list/upsert overrides.
- `GET /api/services` (auth) → aggregated service analytics (requests, avg latency, error rate, latency trend, endpoints stats) over window (1h|24h|7d).

## Running
- Backend: `./gradlew :backend:collector-service:bootRun` (requires `LOGS_MONGO_URI`, `META_MONGO_URI`, `JWT_SECRET`).
- Frontend: `cd frontend/dashboard && npm install && npm run dev` (requires `NEXT_PUBLIC_API_BASE_URL`).
- Tracker: include `kotlin-tracker` starter in Spring Boot services; configure `monitoring.serviceName`, `monitoring.collectorUrl`, `monitoring.jwtSecret`.

## Decisions
- Kept JSON payload field names aligned with frontend (requestSizeBytes/responseSizeBytes, detectedAt).
- Used MongoTemplate for flexibility; aggregation for `/api/services` computed in Kotlin over recent window.
- Optimistic locking chosen for incident resolution to satisfy concurrent-resolve safety.

## Non-Functional
- Bulk inserts used for logs; alerts/incidents use indexed fields for lookup.
- Concurrency smoke test exercises 50 parallel log batches to validate ingestion path.
