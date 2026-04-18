# URL Shortener Service

A production-ready **URL Shortener** REST API built with **Spring Boot 3**, featuring authentication, rate limiting, caching, and a full CI/CD pipeline.

## Tech Stack

- **Java 17** + **Spring Boot 3.3**
- **PostgreSQL** – persistent storage for URL mappings and users
- **Redis** – caching (URL & user) and rate limiting via Lua scripts
- **Firebase** – third-party authentication support
- **JWT** – stateless authentication with access tokens
- **Spring Security** – route protection and role-based access
- **Resilience4j** – fault tolerance and circuit breaking
- **Lombok** – boilerplate reduction
- **Docker** – containerised deployment with OpenTelemetry Java agent for observability

## Key Features

- Shorten long URLs with auto-generated **Base62** keys or custom aliases
- **JWT & Firebase** based authentication
- **Rate limiting** per user using Redis + Lua scripting
- **Caching** for fast URL resolution and user lookups
- Global exception handling with meaningful error responses
- **Health check** endpoint for monitoring
- Profile-based configuration (`dev` / `prod`)

## CI/CD Pipeline

- **Build & Test** – triggered on pull requests to `master` and `development` branches
- **Push & Deploy** – automated Docker image build and deployment
- Uses **GitHub Actions** with Maven, secrets management, and artifact uploads

## Running Locally

```bash
# Build the project
mvn clean package -DskipTests

# Run with Docker
docker build -t url-shortener .
docker run -p 8080:8080 url-shortener
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/shorten` | Shorten a URL |
| `GET` | `/{shortKey}` | Redirect to original URL |
| `POST` | `/auth/login` | Authenticate and get JWT |
| `GET` | `/health` | Health check |
