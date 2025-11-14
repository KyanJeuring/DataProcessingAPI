# Data Processing API Docker Environment

This project provides a **zero‑install development setup** for both frontend (Vue + Vite) and backend (Spring Boot).

---

## 📦 Requirements

### All developers only need:
- **Docker Desktop** (Windows / macOS)
- OR **Docker Engine** (Linux)
- No Java, no Node, no Maven required.

---

## 🚀 How to start the project

Clone the repository and simply run:

```bash
docker compose up -d
```

That’s it.

### Frontend (Vue + Vite + HMR)
Runs at:

```
https://localhost
https://127.0.0.1
```

Changes in `frontend/src` automatically reload the browser instantly via HMR.

### Backend (Spring Boot + DevTools)
Runs at:

```
http://localhost:8080
```

Changes in `backend/src` automatically restart the Spring Boot app.

---

## 🧠 What's inside the containers?

### Frontend:
- Vue.js
- Vite dev server
- HMR enabled
- Your code is bind‑mounted from your machine

### Backend:
- Maven 3.9
- Java 21 (Temurin JDK)
- Spring Boot DevTools (auto‑restart)

None of this needs to be installed locally.

---

## 🖥️ Using different operating systems

### Windows
Works out of the box with Docker Desktop.

### macOS
Works out of the box with Docker Desktop.  
(Optional) If file watching behaves slowly on older Macs, you may set:

```
CHOKIDAR_USEPOLLING=true
```

### Linux
Works with Docker Engine.

---

## 📁 Important folders

```
frontend/  → Vue + Vite code
backend/   → Spring Boot code
docker-compose.yml
```

---

## 🧪 Test API connection

Visit:

```
http://localhost:8080/api/hello
```

or from Vue call the backend:

```js
fetch(import.meta.env.VITE_BACKEND_URL + "/api/hello")
```

---