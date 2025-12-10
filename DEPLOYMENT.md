# Render Deployment Guide

This guide will help you deploy Project-Leap to Render.

## Prerequisites

1. A Render account (sign up at https://render.com)
2. MongoDB Atlas account (or use Render's MongoDB service)
3. Git repository (GitHub, GitLab, or Bitbucket)

## Step 1: Set Up MongoDB Atlas

**⚠️ IMPORTANT: You MUST use MongoDB Atlas (cloud) for Render deployment. Localhost MongoDB will NOT work!**

### Quick Setup:

1. **Create MongoDB Atlas account**: https://www.mongodb.com/cloud/atlas
2. **Create a free cluster** (M0 Sandbox - 512MB free)
3. **Create database user**:
   - Go to Database Access → Add New Database User
   - Choose Password authentication
   - Save username and password securely
4. **Configure network access**:
   - Go to Network Access → Add IP Address
   - Click "Allow Access from Anywhere" (for development)
   - For production, whitelist Render's IP ranges
5. **Get connection string**:
   - Click "Connect" on your cluster
   - Choose "Connect your application"
   - Copy the SRV connection string (looks like: `mongodb+srv://user:pass@cluster.mongodb.net/?retryWrites=true&w=majority`)

### Create Two Connection Strings:

You need **TWO** connection strings pointing to different databases in the same cluster:

**LOGS_MONGO_URI:**
```
mongodb+srv://username:password@projectleap.dwucits.mongodb.net/logsdb?retryWrites=true&w=majority
```

**META_MONGO_URI:**
```
mongodb+srv://username:password@projectleap.dwucits.mongodb.net/metadb?retryWrites=true&w=majority
```

**Key points:**
- Replace `username` and `password` with your MongoDB Atlas database user credentials
- Cluster address: `projectleap.dwucits.mongodb.net`
- Add `/logsdb` or `/metadb` **before** the `?` in the connection string
- Both databases use the same cluster (different database names)
- URL encode special characters in password if needed (`@` → `%40`, `#` → `%23`, etc.)
- **Never commit connection strings with passwords to Git** - use environment variables only

## Step 2: Deploy to Render

### Option A: Using render.yaml (Recommended)

1. Push your code to GitHub/GitLab/Bitbucket
2. Go to Render Dashboard → New → Blueprint
3. Connect your repository
4. Render will automatically detect `render.yaml` and create both services

### Option B: Manual Setup

#### Backend Service (collector-service)

1. Go to Render Dashboard → New → Web Service
2. Connect your repository
3. Configure:
   - **Name**: `collector-service`
   - **Environment**: `Docker`
   - **Dockerfile Path**: `backend/collector-service/Dockerfile`
   - **Docker Context**: `.` (root directory)
   - **Build Command**: (leave empty, Dockerfile handles it)
   - **Start Command**: (leave empty, Dockerfile handles it)

4. Add Environment Variables:
   - `SPRING_PROFILES_ACTIVE` = `production`
   - `SERVER_PORT` = `8080`
   - `LOGS_MONGO_URI` = `mongodb+srv://username:password@projectleap.dwucits.mongodb.net/logsdb?retryWrites=true&w=majority`
     - ⚠️ Replace `username` and `password` with your MongoDB Atlas credentials
     - Cluster: `projectleap.dwucits.mongodb.net`
   - `META_MONGO_URI` = `mongodb+srv://username:password@projectleap.dwucits.mongodb.net/metadb?retryWrites=true&w=majority`
     - ⚠️ Same cluster, different database name (`metadb` instead of `logsdb`)
     - Use same `username` and `password` as above
   - `JWT_SECRET` = (generate a secure random string, minimum 64 characters)
   - `JAVA_OPTS` = `-Xmx512m -Xms256m`
   - `LOG_LEVEL` = `INFO`

#### Frontend Service (dashboard)

1. Go to Render Dashboard → New → Web Service
2. Connect your repository
3. Configure:
   - **Name**: `dashboard`
   - **Environment**: `Docker`
   - **Dockerfile Path**: `frontend/dashboard/Dockerfile`
   - **Docker Context**: `.` (root directory)

4. Add Environment Variables:
   - `NODE_ENV` = `production`
   - `NEXT_PUBLIC_API_BASE_URL` = `https://collector-service.onrender.com` (use your backend service URL)
   - `PORT` = `3000`

## Step 3: Generate JWT Secret

Generate a secure JWT secret (64+ characters):

**Linux/Mac:**
```bash
openssl rand -hex 32
```

**PowerShell (Windows):**
```powershell
-join ((48..57) + (65..90) + (97..122) | Get-Random -Count 64 | ForEach-Object {[char]$_})
```

Or use an online generator: https://www.grc.com/passwords.htm

## Step 4: Update Frontend API URL

After the backend service is deployed, update the `NEXT_PUBLIC_API_BASE_URL` in the frontend service to point to your backend URL.

## Step 5: Verify Deployment

1. Check backend health: `https://your-backend-url.onrender.com/actuator/health` (if actuator is enabled)
2. Test frontend: `https://your-frontend-url.onrender.com`
3. Sign up a new user through the frontend
4. Test API endpoints

## Environment Variables Summary

### Backend (collector-service)
- `LOGS_MONGO_URI` - MongoDB connection string for logs database
- `META_MONGO_URI` - MongoDB connection string for metadata database
- `JWT_SECRET` - Secret key for JWT token signing (64+ characters)
- `SERVER_PORT` - Port for the Spring Boot server (default: 8080)
- `SPRING_PROFILES_ACTIVE` - Spring profile (set to `production`)
- `LOG_LEVEL` - Logging level (INFO for production)

### Frontend (dashboard)
- `NEXT_PUBLIC_API_BASE_URL` - Backend API URL (e.g., `https://collector-service.onrender.com`)
- `NODE_ENV` - Node environment (set to `production`)
- `PORT` - Port for Next.js server (default: 3000)

## Troubleshooting

### Backend Issues

1. **Build fails**: Check that Gradle wrapper is executable and all dependencies are available
2. **MongoDB connection fails**: Verify connection strings and network access (whitelist Render IPs in MongoDB Atlas)
3. **JWT errors**: Ensure JWT_SECRET is set and is at least 64 characters

### Frontend Issues

1. **API calls fail**: Verify `NEXT_PUBLIC_API_BASE_URL` points to the correct backend URL
2. **Build fails**: Check Node.js version compatibility (requires Node 20+)
3. **CORS errors**: Ensure backend CORS configuration allows your frontend domain

### MongoDB Atlas Network Access

If using MongoDB Atlas, add Render's IP ranges or allow access from anywhere (0.0.0.0/0) for development.

## Cost Considerations

- Render's free tier includes:
  - 750 hours/month of build time
  - Services spin down after 15 minutes of inactivity (free tier)
  - Consider upgrading to paid tier for always-on services

## Next Steps

After deployment:
1. Set up custom domains (optional)
2. Configure SSL certificates (automatic with Render)
3. Set up monitoring and alerts
4. Configure database backups

