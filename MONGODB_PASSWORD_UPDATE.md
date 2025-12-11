# MongoDB Password Update

## Date: 2024-12-19

## Update Required in Render Dashboard

The MongoDB password has been updated. Please update the following environment variable in your Render backend service:

### Environment Variable to Update:
- **Variable Name**: `SPRING_DATA_MONGODB_URI`
- **New Password**: `NMDXBXvqfeGvYYXk`
- **Username**: `Shreyanshkp023`
- **Cluster**: `projectleap.dwucits.mongodb.net`

### Updated Connection String Format:

**For LOGS_MONGO_URI:**
```
mongodb+srv://Shreyanshkp023:NMDXBXvqfeGvYYXk@projectleap.dwucits.mongodb.net/logsdb?retryWrites=true&w=majority
```

**For META_MONGO_URI:**
```
mongodb+srv://Shreyanshkp023:NMDXBXvqfeGvYYXk@projectleap.dwucits.mongodb.net/metadb?retryWrites=true&w=majority
```

**For SPRING_DATA_MONGODB_URI (if used):**
```
mongodb+srv://Shreyanshkp023:NMDXBXvqfeGvYYXk@projectleap.dwucits.mongodb.net/<DB_NAME>?retryWrites=true&w=majority
```

### Steps to Update in Render:

1. Go to Render Dashboard → Your Backend Service
2. Navigate to **Environment** tab
3. Find the environment variable `SPRING_DATA_MONGODB_URI` (or `LOGS_MONGO_URI` and `META_MONGO_URI`)
4. Update the password in the connection string from the old password to: `NMDXBXvqfeGvYYXk`
5. Save the changes
6. Render will automatically redeploy the service

### Note:
- Replace `<DB_NAME>` with the actual database name if using `SPRING_DATA_MONGODB_URI`
- Ensure the password is URL-encoded if it contains special characters
- The service will automatically restart after saving the environment variable

