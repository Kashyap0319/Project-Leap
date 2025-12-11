# Orders Service - Self-Logging Microservice

This is a self-logging microservice that automatically generates API logs and sends them to the collector service. No manual API hits needed!

## Features

- ✅ Automatic log generation every 3 seconds
- ✅ Tracks all API calls with latency, status codes, request/response sizes
- ✅ Simulates realistic API behavior (slow APIs, errors, etc.)
- ✅ Sends logs to collector service automatically
- ✅ No Postman or manual API hits required

## How It Works

1. **AutoPingScheduler**: Automatically hits random endpoints every 3 seconds
2. **TrackingInterceptor**: Captures all requests and calculates metrics
3. **TrackingSender**: Sends logs to collector service at `/api/logs`
4. **FakeController**: Provides various endpoints that simulate real API behavior

## Running the Service

### Prerequisites
- Java 21+
- Collector service running on port 8080

### Start the Service

```bash
# From project root
./gradlew :orders-service:bootRun

# Or on Windows
gradlew.bat :orders-service:bootRun
```

The service will start on port **9000**.

## Endpoints

The service provides these endpoints that are automatically pinged:

- `GET /orders/create` - Creates an order (random latency 20-800ms)
- `GET /orders/list` - Lists orders (fast, 10-300ms)
- `GET /orders/{id}` - Gets order by ID (15-250ms)
- `POST /orders` - Creates order via POST (50-600ms)
- `PUT /orders/{id}` - Updates order (30-400ms)
- `DELETE /orders/{id}` - Deletes order (20-200ms)
- `GET /orders/payment/process` - Processes payment (sometimes fails with 5xx)
- `GET /orders/inventory/check` - Checks inventory (40-350ms)

## Configuration

Edit `src/main/resources/application.yml`:

```yaml
server:
  port: 9000

monitoring:
  serviceName: orders-service
  collectorUrl: http://localhost:8080/api/logs  # Collector service URL
  rateLimit:
    limit: 100
```

## Dashboard Integration

Once running, the dashboard will automatically show:
- Live API logs in the logs table
- Service analytics on the services page
- Alerts for slow APIs (>500ms) and broken APIs (5xx)
- Rate limit hits (if any)

## Stopping the Service

Press `Ctrl+C` to stop the service.

