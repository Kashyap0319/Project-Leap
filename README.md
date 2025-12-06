# API Monitoring & Observability Platform

Tech stack
- Backend: Spring Boot (Kotlin), dual Mongo (logs, metadata)
- Tracker: OkHttp interceptor, rate limiter, batching, JWT signing
- Frontend: Next.js dashboard (filters, analytics widgets, incidents)

Project layout
- `backend/collector-service` — REST APIs, dual Mongo templates, alerts/incidents, security + tests
- `backend/kotlin-tracker` — interceptor, limiter, batcher, JWT + tests
- `backend/shared-contracts` — DTOs
- `frontend/dashboard` — Next.js app

Quick start
1) Run Mongo (or use the provided compose):
	- `docker-compose up -d`
	- Logs DB: `mongodb://localhost:27017/logs-db`
	- Meta DB: `mongodb://localhost:27018/meta-db`
2) Backend tests:
	- `./gradlew :backend:kotlin-tracker:test :backend:collector-service:test`
3) Run collector locally:
	- `./gradlew :backend:collector-service:bootRun`
4) Dashboard build/dev:
	- `cd frontend/dashboard && npm install && npm run build`
	- `npm run dev -- --hostname 0.0.0.0 --port 3000`

Environment
- Backend: `backend/collector-service/.env.example` (also root `.env.example`)
	- `LOGS_MONGO_URI`, `META_MONGO_URI`, `JWT_SECRET`
- Tracker: `backend/kotlin-tracker/.env.example`
	- `MONITORING_JWT_SECRET`, `MONITORING_COLLECTOR_URL`
- Frontend: `frontend/dashboard/.env.example`
	- `NEXT_PUBLIC_API_BASE`

APIs (collector)
- `POST /api/logs/batch` (JWT required): accepts list of `LogEvent`
- `GET /api/logs` filters: `service`, `endpoint`, `status`, `slow`, `broken`, `rateLimited`
- `GET /api/incidents` lists open incidents
- `POST /api/incidents/{id}/resolve` body `{ "version": <long> }` optimistic lock

Alert rules
- Latency > 500ms
- Status 5xx
- Rate-limit flagged by tracker

Dashboard
- Login stores JWT token client-side
- Logs table with filters
- Widgets: slow count, broken count, rate-limit violations, avg latency by endpoint, top 5 slowest, error-rate trend, incident list with resolve

Perf sanity (manual)
- Start services, then drive 50+ concurrent writes to `/api/logs/batch` (e.g., `hey -n 1000 -c 50 -m POST -T application/json -d '[{...logs...}]' http://localhost:8080/api/logs/batch`)
- Observe Mongo writes and alert/incident creation.

Notes
- Gradle coordinates changed: use `:backend:*` for modules.
- Vercel/local Next root is `frontend/dashboard`.