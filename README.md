# DataProcessingAPI

Composed development environment for a Vue frontend (Vite) and a Spring Boot backend, with Postgres + pgAdmin included for local development.

## What’s in this repository

- `backend/` — Spring Boot application (Java, Maven)
- `frontend/` — Vue + Vite frontend
- `docker-compose.yml` — development compose file (frontend, backend, postgres, pgadmin)
- `pgadmin/servers.json` — preconfigured servers for pgAdmin

## Prerequisites

- Docker (Desktop or Engine) installed and running
- Optional: `curl`, `mvn`, `node`/`npm` if you want to run services locally without Docker

## Environment variables (Create before composing)

Create a `.env` file in the project root (this file is read by `docker-compose.yml`). Copy the contents of `.env.example` into the `.env` file and change the credentials.

Create a `servers.json` file in the folder `pgadmin/`. Copy the contents of `servers.example.json` into the `servers.json` file and change the credentials.

> Note: set secure values for the passwords in production or shared environments.

## Ports (host → container)

- Backend (Spring Boot): `8081:8080` — access at `http://localhost:8081`
- Frontend (Vite): `80:5173` and `5173:5173` — access at `http://localhost` and `http://localhost:5173`
- Postgres: `5432:5432` — DB listens on container port `5432`, host port `5432`
- pgAdmin: `8080:80` — access at `http://localhost:8080`

## Quick start (Docker Compose)

1. Copy or create `.env` in the project root (see above).
2. Copy or create `servers.json` in the `pgadmin/` directory (see above).
3. Start everything:
```bash
docker compose up -d
```
4. Check status:

```bash
docker compose ps
docker compose logs -f backend
```
5. Open services in your browser:

- Frontend: `http://localhost` or `http://localhost:5173`
- Backend API: `http://localhost:8081`
- pgAdmin: `http://localhost:8080`
- Swagger: `http://localhost:8081/api/docs`

## Testing the API with Swagger

To test protected endpoints (orders, fleet, vehicles, etc.) in Swagger, you need a JWT token. There are two ways to get one:

### Option 1: Get Auth via Frontend (Recommended)

1. Open the frontend: `http://localhost` or `http://localhost:5173`
2. Go to **Sign Up** and create a new account:
   - Select an existing company from the dropdown or create a new one
   - Fill in your details
   - Verify your email using the verification code
3. **Log in** on the frontend
4. The JWT token is stored in your browser session
5. Now open Swagger UI: `http://localhost:8081/api/docs`
6. Click the **"Authorize"** button (top right)
7. Paste your token in the modal
8. Test all protected endpoints!

### Option 2: Get Auth via Swagger

1. Open Swagger UI: `http://localhost:8081/api/docs`
2. Use the **public endpoints** to create an account and get a token:
   - `POST /api/auth/register` — Create a new account (provide `companyId` for existing company or `companyName` to create new)
   - `POST /api/auth/verify/code/check` — Verify email with the code sent to your email
   - `POST /api/auth/login` — Login to get JWT token
3. Copy the JWT token from the login response
4. Click the **"Authorize"** button in Swagger (top right)
5. Paste the token in the modal
6. Now you can test all protected endpoints!

## Additional Documentation

This project includes comprehensive documentation for various aspects of the system:

- **[BACKUP_RECOVERY_PROTOCOL.md](BACKUP_RECOVERY_PROTOCOL.md)** — Complete backup and disaster recovery strategy including automated daily backups, recovery procedures for different scenarios, and retention policies. Essential reading for understanding how we protect data and handle emergencies.

- **[DATABASE_DOCUMENTATION.md](backend/sql/DATABASE_DOCUMENTATION.md)** — Full technical specification of the PostgreSQL database schema including all tables, stored procedures, views, triggers, and usage examples. Reference this when working with the database or implementing new features.