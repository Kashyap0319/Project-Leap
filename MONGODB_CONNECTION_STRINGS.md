# MongoDB Connection Strings

## Updated Connection Strings for Render Environment Variables

### LOGS_MONGO_URI
```
mongodb+srv://Shreyanshkp023:B8Cgnroqd57uUpz7@projectleap.dwucits.mongodb.net/logs?retryWrites=true&w=majority
```

### META_MONGO_URI
```
mongodb+srv://Shreyanshkp023:B8Cgnroqd57uUpz7@projectleap.dwucits.mongodb.net/meta?retryWrites=true&w=majority
```

## Instructions for Render Dashboard

1. Go to Render Dashboard → Backend Service (collector-service)
2. Navigate to **Environment** tab
3. Update the following environment variables:

   **LOGS_MONGO_URI:**
   - Set to: `mongodb+srv://Shreyanshkp023:B8Cgnroqd57uUpz7@projectleap.dwucits.mongodb.net/logs?retryWrites=true&w=majority`

   **META_MONGO_URI:**
   - Set to: `mongodb+srv://Shreyanshkp023:B8Cgnroqd57uUpz7@projectleap.dwucits.mongodb.net/meta?retryWrites=true&w=majority`

4. Save the changes
5. Render will automatically redeploy the service

## Database Names
- Logs database: `logs`
- Metadata database: `meta`

## Credentials
- Username: `Shreyanshkp023`
- Password: `B8Cgnroqd57uUpz7`
- Cluster: `projectleap.dwucits.mongodb.net`

