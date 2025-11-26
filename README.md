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