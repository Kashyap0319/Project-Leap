#!/bin/bash

echo "🚀 Starting Project Leap Setup..."

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed. Please install Docker first."
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

echo "✅ Docker and Docker Compose are installed"

# Start services
echo "📦 Starting services with Docker Compose..."
docker-compose up -d

echo ""
echo "✅ Services are starting up!"
echo ""
echo "📊 Service URLs:"
echo "  - Frontend: http://localhost:3000"
echo "  - Backend API: http://localhost:8080"
echo "  - MongoDB Logs: mongodb://localhost:27017/project-leap-logs"
echo "  - MongoDB Metadata: mongodb://localhost:27018/project-leap-metadata"
echo ""
echo "⏳ Please wait 30-60 seconds for all services to initialize"
echo ""
echo "📝 To view logs: docker-compose logs -f"
echo "🛑 To stop: docker-compose down"
echo ""
