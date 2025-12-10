# 🚀 Deployment Guide

## Vercel Deployment (Frontend)

### Step 1: Connect GitHub Repository

1. Go to [https://vercel.com](https://vercel.com)
2. Sign in with GitHub
3. Click **"Add New Project"**
4. Import repository: `Kashyap0319/Project-Leap`

### Step 2: Configure Project

**Project Settings:**
- **Framework Preset:** Next.js
- **Root Directory:** `frontend/dashboard`
- **Build Command:** `npm run build` (auto-detected)
- **Output Directory:** `.next` (auto-detected)
- **Install Command:** `npm install` (auto-detected)

### Step 3: Environment Variables

Add these environment variables in Vercel Dashboard:

```
NEXT_PUBLIC_API_BASE_URL=https://your-backend-url.com
```

**Important:** Replace `your-backend-url.com` with your actual backend deployment URL.

### Step 4: Deploy

Click **"Deploy"** and wait for the build to complete.

Your frontend will be live at: `https://your-project.vercel.app`

---

## Backend Deployment Options

### Option 1: Railway.app (Recommended)

1. Go to [https://railway.app](https://railway.app)
2. Sign in with GitHub
3. Click **"New Project"** → **"Deploy from GitHub repo"**
4. Select `Kashyap0319/Project-Leap`
5. Set **Root Directory:** `backend/collector-service`

**Environment Variables:**
```
LOGS_MONGO_URI=mongodb+srv://user:pass@cluster.mongodb.net/logsdb
META_MONGO_URI=mongodb+srv://user:pass@cluster.mongodb.net/metadb
JWT_SECRET=your-secret-key-here
```

**Build Command:**
```bash
./gradlew :backend:collector-service:bootJar
```

**Start Command:**
```bash
java -jar build/libs/collector-service-*.jar
```

### Option 2: Render.com

1. Go to [https://render.com](https://render.com)
2. Sign in with GitHub
3. Click **"New +"** → **"Web Service"**
4. Connect repository: `Kashyap0319/Project-Leap`

**Settings:**
- **Root Directory:** `backend/collector-service`
- **Build Command:** `./gradlew :backend:collector-service:bootJar`
- **Start Command:** `java -jar build/libs/collector-service-*.jar`

**Environment Variables:**
```
LOGS_MONGO_URI=mongodb+srv://...
META_MONGO_URI=mongodb+srv://...
JWT_SECRET=your-secret-key
```

### Option 3: Heroku

1. Install Heroku CLI: `npm install -g heroku`
2. Login: `heroku login`
3. Create app: `heroku create your-app-name`
4. Set buildpack: `heroku buildpacks:set heroku/gradle`
5. Set environment variables:
```bash
heroku config:set LOGS_MONGO_URI=...
heroku config:set META_MONGO_URI=...
heroku config:set JWT_SECRET=...
```
6. Deploy: `git push heroku main`

### Option 4: Docker Deployment

**Build Docker Image:**
```bash
docker build -t project-leap-backend .
```

**Run Container:**
```bash
docker run -p 8080:8080 \
  -e LOGS_MONGO_URI=mongodb://... \
  -e META_MONGO_URI=mongodb://... \
  -e JWT_SECRET=... \
  project-leap-backend
```

---

## MongoDB Atlas Setup

### Step 1: Create Cluster

1. Go to [https://www.mongodb.com/cloud/atlas](https://www.mongodb.com/cloud/atlas)
2. Create a free cluster
3. Create database user
4. Whitelist IP addresses (0.0.0.0/0 for all)

### Step 2: Create Databases

Create two databases:
- `logsdb` - For API logs
- `metadb` - For metadata (users, alerts, incidents)

### Step 3: Get Connection Strings

1. Click **"Connect"** on your cluster
2. Choose **"Connect your application"**
3. Copy connection string
4. Replace `<password>` with your database password

**Example:**
```
mongodb+srv://username:password@cluster.mongodb.net/logsdb?retryWrites=true&w=majority
mongodb+srv://username:password@cluster.mongodb.net/metadb?retryWrites=true&w=majority
```

---

## Complete Deployment Checklist

### Frontend (Vercel)
- [ ] Repository connected
- [ ] Root directory set to `frontend/dashboard`
- [ ] Environment variable `NEXT_PUBLIC_API_BASE_URL` set
- [ ] Build successful
- [ ] Site accessible

### Backend (Railway/Render/Heroku)
- [ ] Repository connected
- [ ] Root directory set to `backend/collector-service`
- [ ] Environment variables set:
  - [ ] `LOGS_MONGO_URI`
  - [ ] `META_MONGO_URI`
  - [ ] `JWT_SECRET`
- [ ] Build successful
- [ ] Service running
- [ ] Health check passing

### MongoDB Atlas
- [ ] Cluster created
- [ ] Databases created (`logsdb`, `metadb`)
- [ ] User created with read/write permissions
- [ ] IP whitelisted
- [ ] Connection strings obtained

### Testing
- [ ] Frontend loads correctly
- [ ] Signup works
- [ ] Login works
- [ ] Dashboard displays data
- [ ] API endpoints accessible
- [ ] Logs can be created
- [ ] Alerts appear

---

## Post-Deployment

### Update Frontend Environment Variable

After backend is deployed, update Vercel environment variable:

```
NEXT_PUBLIC_API_BASE_URL=https://your-backend.railway.app
```

Redeploy frontend to pick up the new URL.

### Test Deployment

1. **Test Signup:**
```bash
curl -X POST https://your-backend.railway.app/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"password123"}'
```

2. **Test Login:**
```bash
curl -X POST https://your-backend.railway.app/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'
```

3. **Test Frontend:**
- Visit your Vercel URL
- Signup/Login
- Check dashboard

---

## Troubleshooting

### Frontend can't connect to backend
- Check `NEXT_PUBLIC_API_BASE_URL` is correct
- Verify backend is running
- Check CORS configuration in backend

### Backend won't start
- Check MongoDB connection strings
- Verify environment variables
- Check build logs for errors

### Database connection errors
- Verify MongoDB Atlas IP whitelist
- Check username/password
- Verify database names match

---

## Quick Deploy Commands

### Vercel (CLI)
```bash
cd frontend/dashboard
npm i -g vercel
vercel
```

### Railway (CLI)
```bash
npm i -g @railway/cli
railway login
railway init
railway up
```

---

**🎉 Your project is now live!**

