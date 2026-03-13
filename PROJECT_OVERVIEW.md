# PROJECT_OVERVIEW.md

## Purpose
This repository implements a URL shortening service.

Core capabilities:
- create short aliases for long URLs
- redirect alias -> original URL
- reduce lookup latency with layered caching
- reduce unnecessary misses with Bloom filter pre-checks

## Business / Technical Context
This project is primarily a technical study / utility service focused on:
- low-latency redirect lookup
- scalable key generation
- caching strategy design
- handling high read traffic and failed-lookups efficiently

## Tech Stack
- Java 21
- Spring Boot 4
- Spring MVC
- Spring Data JPA
- MySQL 8
- Redis 8
- Redisson
- Prometheus + Grafana
- Docker Compose
- Gradle

## How to Run
### Local
- build: `./gradlew build`
- run app: `./gradlew bootRun`

### Docker Compose
- start stack: `docker compose up -d --build`
- stop stack: `docker compose down`
- clean volumes: `docker compose down -v`

## Main Runtime Dependencies
- MySQL for persistent alias storage
- Redis for caching / bloom-related support
- monitoring stack via Prometheus and Grafana

## Primary Entry Points
- `POST /app/api/create` -> create short URL
- `GET /{alias}` -> redirect to original URL

## Repo Shape
- `src/main/java/.../controller` -> HTTP endpoints
- `src/main/java/.../service` -> application/service logic
- `src/main/java/.../repository` -> JPA persistence
- `src/main/java/.../cache` -> cache-related components
- `src/main/java/.../config` -> app + infrastructure config
- `src/main/java/.../entity` -> database entities
- `src/main/java/.../dto` -> request/response DTOs
- `src/main/java/.../exceptions` -> exception handling
- `src/main/java/.../utils` -> local utility classes

## What an agent should know first
- This repo is performance-oriented, but also experimental / study-driven in some architecture choices.
- Redirect path is latency-sensitive.
- Create path uses asynchronous / buffered write behavior via `WriteBehindBuffer` and is not immediately durable at response time.
- Bloom filter behavior affects miss-path logic and should be treated carefully.
- Changes to alias generation or redirect lookup can impact correctness and performance together.
- The request DTO includes an `alias` field, but the current primary create flow generates its own key and does not honor user-provided alias.
