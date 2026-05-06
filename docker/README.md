# Docker Quick Start

## Prerequisites
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (Windows/Mac) or Docker Engine + Compose (Linux)
- Ports **4200, 5432, 5672, 6379, 8080, 8081, 15672** must be free

## First-time setup

```bash
# 1. Copy the env template
cp .env.example .env

# 2. Edit .env — set at minimum:
#    JWT_SECRET        (>= 32 characters)
#    VNPAY_TMN_CODE    (from VNPay sandbox registration)
#    VNPAY_HASH_SECRET (from VNPay sandbox registration)
nano .env   # or any editor

# 3. Build and start all services (~3-5 min first build)
docker compose up -d --build

# 4. Watch backend boot (Flyway migrations + seed data run here)
docker compose logs -f backend
# Wait for: "Started FreelancerApplication in X seconds"
```

## Port map

| Service           | URL                                   |
|-------------------|---------------------------------------|
| Frontend          | http://localhost:4200                 |
| Backend API       | http://localhost:8080/api             |
| API Docs          | http://localhost:8080/swagger-ui.html |
| Notification Svc  | http://localhost:8081/actuator/health |
| RabbitMQ UI       | http://localhost:15672                |
| PostgreSQL        | localhost:5432                        |

## Seed accounts

All passwords: `Password123!`

| Email                       | Role       |
|-----------------------------|------------|
| admin@freelance.local       | ADMIN      |
| client@freelance.local      | CLIENT     |
| freelancer@freelance.local  | FREELANCER |

## Common operations

```bash
# View all service status
docker compose ps

# View logs
docker compose logs -f              # all services
docker compose logs -f backend      # backend only
docker compose logs -f notification # notification service

# Restart a single service after code change
docker compose up -d --build backend

# Stop everything (keeps data volumes)
docker compose down

# Reset everything — DELETES all database data
docker compose down -v
docker compose up -d --build
```

## Uploads

File uploads are persisted in `source/uploads/` on the host (mounted into BE container at `/app/uploads`). This directory survives `docker compose down` and `docker compose up --build` — only `docker compose down -v` clears docker volumes (not this mount).

## Development without Docker

If you prefer running services locally (requires JDK 21, Node 20, Maven, and infra via Docker):

```bash
# Start only infra
docker compose up -d postgres redis rabbitmq

# Backend (port 8080)
cd source/backend
./mvnw spring-boot:run

# Notification service (port 8081)
cd source/notification-service
./mvnw spring-boot:run

# Frontend (port 4200 — live reload)
cd source/frontend
npm install
ng serve
```
