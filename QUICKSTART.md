# Quick Start Guide

This guide will help you get Project Leap up and running quickly.

## Prerequisites

- Docker and Docker Compose
- OR Java 17+, Node.js 18+, and MongoDB 4.4+

## Option 1: Using Docker (Recommended)

1. **Clone the repository:**
```bash
git clone https://github.com/Kashyap0319/Project-Leap.git
cd Project-Leap
```

2. **Start all services:**
```bash
./start.sh
```
Or manually:
```bash
docker-compose up -d
```

3. **Wait for services to initialize** (30-60 seconds)

4. **Access the application:**
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8080

5. **Default credentials:**
   - Username: `admin`
   - Password: `admin123`

## Option 2: Manual Setup

### 1. Start MongoDB

```bash
# Using Docker
docker run -d -p 27017:27017 --name mongodb mongo:7.0

# Or install MongoDB locally
```

### 2. Start Backend

```bash
cd backend

# Update application.properties if needed
# Then run:
./gradlew bootRun
```

Backend will start on http://localhost:8080

### 3. Start Frontend

```bash
cd frontend

# Install dependencies
npm install

# Create .env.local from example
cp .env.example .env.local

# Start development server
npm run dev
```

Frontend will start on http://localhost:3000

## First Steps

1. **Login** with credentials: `admin` / `admin123`
2. **Create some logs** via the API or use the bulk endpoint
3. **Set filters** to view specific log levels or sources
4. **Monitor alerts** that appear when error thresholds are exceeded
5. **Resolve issues** by clicking on alerts

## Testing the API

### Register a new user:
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"pass123","email":"user@example.com"}'
```

### Login:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

### Create a log entry:
```bash
TOKEN="your-jwt-token"
curl -X POST http://localhost:8080/api/logs \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"level":"ERROR","message":"Test error","source":"test-app"}'
```

### Get all logs:
```bash
curl -X GET http://localhost:8080/api/logs \
  -H "Authorization: Bearer $TOKEN"
```

### Get alerts:
```bash
curl -X GET http://localhost:8080/api/alerts \
  -H "Authorization: Bearer $TOKEN"
```

## Stopping the Application

### Docker:
```bash
docker-compose down
```

### Manual:
- Press `Ctrl+C` in each terminal running the backend and frontend

## Troubleshooting

### Backend won't start
- Check if MongoDB is running: `docker ps` or `mongosh`
- Check port 8080 is not in use: `lsof -i :8080`
- Review logs: `docker-compose logs backend`

### Frontend won't start
- Check port 3000 is not in use: `lsof -i :3000`
- Clear Next.js cache: `rm -rf frontend/.next`
- Reinstall dependencies: `cd frontend && rm -rf node_modules && npm install`

### Can't login
- Ensure backend is running and accessible
- Check browser console for errors
- Verify CORS settings in backend SecurityConfig.kt

### Database connection issues
- Ensure MongoDB is running
- Check connection strings in application.properties
- For Docker: ensure containers are on the same network

## Next Steps

- Read the full [README.md](README.md) for architecture details
- Explore the API endpoints
- Customize rate limits and alert thresholds
- Add your own log sources

## Need Help?

- Check the [README.md](README.md) for detailed documentation
- Review the API documentation in README.md
- Check Docker logs: `docker-compose logs -f`
