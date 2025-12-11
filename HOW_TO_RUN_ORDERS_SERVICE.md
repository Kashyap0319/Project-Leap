# 🚀 Orders Service Kaise Chalayein

## ⚠️ IMPORTANT: Website par koi input nahi chahiye!

**Orders-service ko separately RUN karna hoga** - tabhi dashboard par data dikhega.

---

## 📋 Step-by-Step Guide

### Step 1: Collector Service Start Karo (Agar nahi chal raha)

**Terminal 1** mein:
```bash
cd backend/collector-service
./gradlew bootRun
```

Ya project root se:
```bash
./gradlew :backend:collector-service:bootRun
```

✅ Collector service port **8080** par chalega

---

### Step 2: Orders Service Start Karo (NEW!)

**Terminal 2** mein (Naya terminal kholo):
```bash
# Project root se
./gradlew :backend:orders-service:bootRun
```

Ya Windows par:
```bash
gradlew.bat :backend:orders-service:bootRun
```

✅ Orders service port **9000** par chalega

---

### Step 3: Dashboard Kholo

Browser mein dashboard kholo - **automatic data dikhne lagega!**

---

## ✅ Kya Hoga:

1. ✅ Orders service har **3 seconds** mein automatically random endpoints hit karega
2. ✅ Har request automatically track hoga
3. ✅ Logs collector service ko bheje jayenge
4. ✅ Dashboard **automatically update** hoga:
   - Live traffic table mein logs dikhenge
   - Services page par "orders-service" dikhega
   - Alerts generate honge
   - KPIs update honge

---

## 🎯 No Input Needed!

- ❌ Page par kuch input nahi karna
- ❌ Postman se API hit nahi karna
- ❌ Manual kuch nahi karna
- ✅ Bas orders-service start karo - sab automatic!

---

## 🔍 Check Karne Ke Liye:

### Orders Service Running Hai?
Browser mein check karo:
```
http://localhost:9000/orders/list
```

Agar response aaye = Service running ✅

### Logs Generate Ho Rahe Hain?
Dashboard par "Live traffic" section check karo - har 3 seconds mein naye logs dikhne chahiye.

---

## ⚠️ Common Issues:

### Issue 1: "Port 9000 already in use"
**Solution**: Kisi aur service ko port 9000 par band karo, ya `application.yml` mein port change karo.

### Issue 2: "Cannot connect to collector"
**Solution**: Pehle collector service start karo (port 8080)

### Issue 3: "No data on dashboard"
**Solution**: 
1. Orders service running hai? Check karo
2. 10-15 seconds wait karo (logs generate hone mein time lagta hai)
3. Dashboard refresh karo

---

## 📊 Expected Output:

Dashboard par yeh dikhna chahiye:

1. **Dashboard Page**:
   - Live traffic table mein "orders-service" ke logs
   - Slow APIs count > 0
   - Broken APIs count (agar 5xx errors aaye)

2. **Services Page**:
   - "orders-service" button dikhega
   - Click karo → KPIs, latency trends, endpoints dikhenge

3. **Logs Page**:
   - Har 3 seconds mein naye logs add hote rahenge

---

## 🎉 Summary:

**Bas yeh karo:**
1. Terminal 1: Collector service start (port 8080)
2. Terminal 2: Orders service start (port 9000) ← **YE IMPORTANT HAI!**
3. Dashboard kholo - automatic data dikhega!

**Koi input nahi chahiye page par - sab automatic hai!** 🚀

