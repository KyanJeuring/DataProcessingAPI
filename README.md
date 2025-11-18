# Data Processing API Docker Environment

This project provides a **zero‑install development setup** for both frontend (Vue + Vite) and backend (Spring Boot).

---

## Requirements

### All developers only need:
- **Docker Desktop** (Windows / macOS)
- OR **Docker Engine** (Linux)
- No Java, no Node, no Maven required.

---

## How to start the project

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

## What's inside the containers?

### Frontend:
- Vue.js
- Vite dev server
- HMR enabled
- Your code is bind‑mounted from your machine

### Backend:
- Maven 3.9
- Java 21 (Temurin JDK)
- Spring Boot DevTools (auto‑restart)

### Database (Postgres):
- Postgres server running in a container (default image: `postgres:15-alpine`)
- Database name: `datadb`

### Admin (pgAdmin):
- pgAdmin runs in a container and provides a GUI for the Postgres server
- UI is available on `http://localhost:8081`

None of this needs to be installed locally.

---

## Using different operating systems

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

## Important folders

```
frontend/  → Vue + Vite code
backend/   → Spring Boot code
docker-compose.yml
```

---

## Postgres & pgAdmin

This repository includes an optional Postgres server and pgAdmin in `docker-compose.yml` for local development. They are intended for development and testing only.

- Postgres:
	- Image: `postgres:15-alpine`
	- Default DB: `datadb`
	- Default user/password: `postgres` / `postgres` (see `.env` or `docker-compose.yml`)
	- Data is persisted in the `postgres_data` named volume.

- pgAdmin:
	- UI: `http://localhost:8081`
	- Credentials are read from `.env` (see `.env.example`)
	- When adding a server in pgAdmin (from inside the UI), use the following connection settings:
		- Host: `db` (Compose service name)
		- Port: `5432`
		- Maintenance DB: `postgres` (or `datadb`)
		- Username: `postgres`
		- Password: `postgres`

Notes:
- pgAdmin runs in its own container on the same Compose network; when configuring a server inside the pgAdmin UI use the Compose service hostname `db` and container port `5432` (not `localhost` or the host-mapped port).
- If you want to connect from your host machine (psql or a GUI tool), use `localhost:<host_port>` where `<host_port>` is the host port mapped in `docker-compose.yml` (commonly `5432` or `5433` if you changed it to avoid conflicts).

---