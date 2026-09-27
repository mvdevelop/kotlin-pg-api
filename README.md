# 🛡️ Kotlin Product API — Secure REST API

[![CI/CD Pipeline](https://github.com/mvdevelop/kotlin-pg-api/actions/workflows/ci.yml/badge.svg)](https://github.com/mvdevelop/kotlin-pg-api/actions/workflows/ci.yml)
[![Security Scan — CodeQL](https://github.com/mvdevelop/kotlin-pg-api/actions/workflows/ci.yml/badge.svg?branch=main&event=push)](https://github.com/mvdevelop/kotlin-pg-api/security/code-scanning)
[![Docker Scans](https://github.com/mvdevelop/kotlin-pg-api/actions/workflows/ci.yml/badge.svg?branch=main&event=push)](https://github.com/mvdevelop/kotlin-pg-api)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java 21](https://img.shields.io/badge/Java-21-blue?logo=java)](https://adoptium.net/)
[![Kotlin 2.1](https://img.shields.io/badge/Kotlin-2.1-blue?logo=kotlin)](https://kotlinlang.org/)
[![Spring Boot 3.5](https://img.shields.io/badge/Spring%20Boot-3.5.4-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)

> **Production-ready, security-hardened REST API** for product catalog management. Built with Kotlin, Spring Boot 3, PostgreSQL, JWT authentication, and OWASP ASVS Level 2 compliance. Fully containerized with Docker, CI/CD pipeline with SAST/DAST, and 80%+ test coverage.

---

## 📑 Table of Contents

1. [Tech Stack](#tech-stack)
2. [Architecture](#architecture)
3. [Security Features](#security-features)  🔐
4. [API Documentation](#api-documentation)
5. [Installation](#installation)
6. [Usage](#usage)
7. [Testing](#testing)
8. [Deployment](#deployment)
9. [CI/CD Pipeline](#cicd-pipeline)
10. [Compliance](#compliance)
11. [Project Structure](#project-structure)
12. [Contributing](#contributing)

---

## 🛠️ Tech Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| **Language** | Kotlin | 2.1.21 |
| **Runtime** | Java (OpenJDK) | 21 (LTS) |
| **Framework** | Spring Boot | 3.5.4 (LTS) |
| **Security** | Spring Security 6 + JWT | 6.5.x |
| **Database** | PostgreSQL | 16 |
| **ORM** | Spring Data JPA (Hibernate) | 6.6.x |
| **Build Tool** | Maven | 3.9+ |
| **API Docs** | springdoc-openapi (Swagger UI) | 2.6.0 |
| **Testing** | JUnit 5, MockK, Testcontainers | — |
| **Container** | Docker + Docker Compose | — |
| **CI/CD** | GitHub Actions | — |
| **SAST** | Semgrep + CodeQL | — |
| **Container Scan** | Trivy | — |
| **Dependency Scan** | OWASP Dependency-Check | — |

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      Client (SPA / Mobile)                   │
├────────────────────────┬────────────────────────────────────┤
│       Reverse Proxy     │  nginx / Cloud Load Balancer        │
│   (TLS termination,     │  (HSTS, rate limiting, WAF)        │
│    security headers)    │                                      │
├────────────────────────┼────────────────────────────────────┤
│    API Gateway Layer    │  Spring Security 6 (JWT filter)    │
│  - JWT Authentication    │  - Rate limiting (Bucket4j)        │
│  - Security headers      │  - Input validation (Bean Validation)│
│  - RBAC authorization    │                                      │
├────────────────────────┼────────────────────────────────────┤
│   Presentation Layer    │  REST Controllers (@RestController) │
│  - ProductController     │  - CRUD endpoints                  │
│  - AuthController        │  - Auth endpoints                   │
├────────────────────────┼────────────────────────────────────┤
│    Business Layer       │  @Service (Transactional)           │
│  - ProductService        │  - Business logic                  │
│  - AuthService           │  - DTO mapping                     │
├────────────────────────┼────────────────────────────────────┤
│    Data Access Layer    │  Spring Data JPA (Repository)       │
│  - ProductRepository     │  - Parameterized queries (SQLi-safe)│
│  - UserRepository        │                                      │
├────────────────────────┴────────────────────────────────────┤
│              PostgreSQL 16 + Redis (rate limiting)            │
└─────────────────────────────────────────────────────────────┘
```

**Design Patterns Applied:**
- **MVC** — Model (entities), View (DTOs), Controller (REST endpoints)
- **Repository Pattern** — Spring Data JPA abstracts database operations
- **DTO Pattern** — Strict separation between entity and API response (prevents IDOR)
- **Factory Pattern** — `PasswordEncoder`, `JwtTokenProvider`
- **Interceptor/Filter Pattern** — JWT authentication, rate limiting, security headers
- **Singleton Pattern** — Spring-managed beans (`@Service`, `@Configuration`)

---

## 🔐 Security Features

This project implements **OWASP ASVS 4.0.3 Level 2** and **OWASP API Security Top 10 (2023)** compliance:

| Category | Feature | Standard |
|---------|--------|----------|
| **Authentication** | JWT tokens (HS512), BCrypt password hashing (cost=12) | ASVS V3.1, V3.6 |
| **Authorization** | RBAC — `@PreAuthorize("hasRole('ADMIN')")`, method-level security | ASVS V4.1, V4.2 |
| **Rate Limiting** | Bucket4j — 10 req/min auth endpoints, 100 req/min API | ASVS V4.1.3, API4:2023 |
| **Input Validation** | Bean Validation (`@NotBlank`, `@Size`, `@Pattern`) on all DTOs | ASVS V5.1, V5.3 |
| **SQL Injection** | JPA/Hibernate parameterized queries — no string concatenation | CWE-89 |
| **XSS Prevention** | Output encoding + strict CSP headers | ASVS V5.3.1, CWE-79 |
| **Clickjacking** | `X-Frame-Options: DENY` + `frame-ancestors 'none'` | ASVS V14.4.5 |
| **HTTPS Enforcement** | HSTS header (1 year, preload) | ASVS V2.2.1, CWE-319 |
| **Error Handling** | Generic error messages — no stack traces leaked | ASVS V14.4.2, CWE-209 |
| **Secrets Management** | All credentials via environment variables — no hardcoded secrets | ASVS V3.5.1, CWE-798 |
| **Container Hardening** | Non-root user, distroless base image, read-only FS | ASVS V14.5.1 |
| **Dependency Scanning** | OWASP Dependency-Check + Snyk in CI/CD | ASVS V14.2.1 |
| **SAST/DAST** | Semgrep + CodeQL + Trivy + OWASP ZAP | ASVS V1.5.1 |

---

## 📚 API Documentation

### Swagger UI
After starting the application:
```
http://localhost:8080/swagger-ui.html
```

### API Endpoints

#### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Authenticate and receive JWT |

#### Products (requires `ROLE_USER` or `ROLE_ADMIN`)
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/products` | List all products | Any authenticated user |
| GET | `/api/products/{id}` | Get product by ID | Any authenticated user |
| GET | `/api/products/search?name=...` | Search by name | Any authenticated user |
| GET | `/api/products/price-range?minPrice=X&maxPrice=Y` | Filter by price | Any authenticated user |
| GET | `/api/products/most-expensive` | Get most expensive | Any authenticated user |
| GET | `/api/products/average-price` | Get avg price | Any authenticated user |

#### Products — Admin Only (requires `ROLE_ADMIN`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/products` | Create new product |
| PUT | `/api/products/{id}` | Update product |
| DELETE | `/api/products/{id}` | Delete product |

---

## 🚀 Installation

### Prerequisites
- **Java 21** (JDK 21 or later)
- **Maven 3.9+** (or use the Maven wrapper)
- **PostgreSQL 16+** (or use Docker Compose)
- **Docker & Docker Compose** (for containerized dev)

### Option 1: Local Development with Docker Compose (Recommended)

```bash
# 1. Clone the repository
git clone https://github.com/mvdevelop/kotlin-pg-api.git
cd kotlin-pg-api

# 2. Create .env file from template
cp .env.example .env
# Edit .env and set your JWT_SECRET:
# JWT_SECRET=$(openssl rand -base64 32)

# 3. Start all services (PostgreSQL, Redis, App)
docker-compose up --build

# 4. Run database migrations
docker exec -i pg-api-postgres psql -U postgres -d api_db < src/main/resources/schema-postgres.sql

# 5. Access the API
# REST API: http://localhost:8080/api
# Swagger UI: http://localhost:8080/swagger-ui.html
```

### Option 2: Local Development (Manual)

```bash
# 1. Ensure PostgreSQL is running
# 2. Create database
createdb api_db

# 3. Run schema migrations
psql -U postgres -d api_db -f src/main/resources/schema-postgres.sql

# 4. Set environment variables
export DB_USERNAME=postgres
export DB_PASSWORD=your-password
export JWT_SECRET=$(openssl rand -base64 32)

# 5. Build and run
mvn clean spring-boot:run
```

---

## 💻 Usage

### Authentication Flow

```bash
# 1. Register a new user (password must be 12+ chars with uppercase, lowercase, digit, special)
curl -X 'POST' \
  'http://localhost:8080/api/auth/register' \
  -H 'Content-Type: application/json' \
  -d '{
    "username": "newuser",
    "email": "user@example.com",
    "password": "SecureP@ssw0rd!23",
    "role": "ROLE_USER"
  }'

# 2. Login and get JWT token
TOKEN=$(curl -s -X 'POST' \
  'http://localhost:8080/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{
    "username": "newuser",
    "password": "SecureP@ssw0rd!23"
  }' | jq -r '.token')

# 3. Use the token for authenticated requests
curl -X 'GET' \
  'http://localhost:8080/api/products' \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Accept: application/json'
```

### Product Management

```bash
# Create a new product (admin only)
curl -X 'POST' \
  'http://localhost:8080/api/products' \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Gaming Laptop",
    "description": "High-performance gaming laptop with RTX 4070",
    "price": 2499.99,
    "quantity": 5
  }'

# List all products
curl -X 'GET' \
  'http://localhost:8080/api/products' \
  -H "Authorization: Bearer $TOKEN"

# Search products
curl -X 'GET' \
  'http://localhost:8080/api/products/search?name=laptop' \
  -H "Authorization: Bearer $TOKEN"

# Get most expensive product
curl -X 'GET' \
  'http://localhost:8080/api/products/most-expensive' \
  -H "Authorization: Bearer $TOKEN"
```

---

## 🧪 Testing

### Run Unit Tests
```bash
mvn test
```

### Run Integration Tests (requires Testcontainers)
```bash
mvn verify
```

### Generate Test Coverage Report
```bash
mvn test jacoco:report
# Report available at: target/site/jacoco/index.html
```

### Test Structure
| Type | Files | Framework | Target |
|------|-------|-----------|--------|
| Unit | `*Test.kt` | JUnit 5 + MockK | Service layer, token provider |
| Integration | `*IT.kt` | Testcontainers + MockMvc | Full stack with PostgreSQL |
| Contract | — | springdoc-openapi | API validation |
| Security | — | SAST (Semgrep + CodeQL) | Static analysis |

---

## 🐳 Deployment

### Docker Build
```bash
# Build production image
docker build -t kotlin-pg-api:latest .

# Run with environment variables
docker run -d \
  --name kotlin-pg-api \
  -p 8080:8080 \
  -e DB_HOST=host.docker.internal \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=your-password \
  -e JWT_SECRET=$(openssl rand -base64 32) \
  kotlin-pg-api:latest
```

### Production Security Checklist
- [ ] JWT secret is 256+ bits (generated via `openssl rand -base64 32`)
- [ ] Database credentials come from secrets manager (not env vars)
- [ ] HTTPS enforced (reverse proxy with TLS termination)
- [ ] Security headers validated (use SSL Labs test)
- [ ] Rate limiting configured for cloud deployment
- [ ] Dependency-Check passes with no HIGH/CRITICAL vulnerabilities
- [ ] Container image scanned with Trivy (zero vulnerabilities)
- [ ] SAST pipeline passes (Semgrep + CodeQL clean)

---

## 🔄 CI/CD Pipeline

This project ships with a **7-stage CI/CD pipeline** (GitHub Actions):

| Stage | Tool | Purpose |
|-------|------|---------|
| 1. Build & Test | Maven | Compile, run unit + integration tests |
| 2. SAST | Semgrep | Static code analysis for 200+ vulnerability patterns |
| 3. SAST | CodeQL | Deep semantic analysis with GitHub-native integration |
| 4. Dependency Scan | OWASP Dependency-Check | Vulnerable dependency identification |
| 5. Container Build | Docker Buildx | Multi-platform image build |
| 6. Container Scan | Trivy | CVE scanning of the final image |
| 7. Deploy | GitHub Actions | Push to container registry, deploy to staging |

```yaml
# Pipeline triggers on every push and PR
name: CI/CD Pipeline
on:
  push:
    branches: [main, 'portfolio/**']
  pull_request:
    branches: [main]
```

---

## 📜 Compliance

### OWASP Top 10 (2021) — All Mitigated ✅
| # | Risk | Mitigation |
|---|------|-----------|
| A01:2021 | Broken Access Control | `@PreAuthorize` RBAC, IDOR protection via DTO whitelist |
| A02:2021 | Cryptographic Failures | BCrypt (cost 12), JWT HS512, HTTPS-only, no data at rest |
| A03:2021 | Injection | JPA parameterized queries, no native SQL, input validation |
| A04:2021 | Insecure Design | DTO pattern, separation of concerns, validation at layer boundaries |
| A05:2021 | Security Misconfiguration | Production profile (no debug, no `show-sql`), security headers |
| A06:2021 | Vulnerable & Outdated | Dependency-Check + Trivy in CI/CD, Spring Boot 3.5.x (patched) |
| A07:2021 | Identification & Auth Failures | BCrypt, account lockout, rate limiting, JWT expiry |
| A08:2021 | Software & Data Integrity | Docker content trust, image signing, no `:latest` in prod |
| A09:2021 | Security Logging Failures | Structured logging with correlation ID, audit trail in DB |
| A10:2021 | SSRF | No outbound HTTP requests, no XML processing, URL allowlisting |

### OWASP API Security Top 10 (2023) — All Mitigated ✅
| API Risk | Mitigation |
|---------|-----------|
| API1: Broken Object Level Authorization | `@PreAuthorize`, DTO whitelist, ID validation |
| API2: Broken Authentication | JWT + BCrypt, rate limiting, account lockout |
| API3: Broken Object Property Level Authorization | DTO pattern (no extra fields exposed), `@JsonIgnoreProperties` |
| API4: Unrestricted Resource Consumption | Bucket4j rate limiting (in-memory → Redis for prod) |
| API5: Broken Function Level Authorization | `@PreAuthorize` on controller methods, role hierarchy |
| API6: Unrestricted Access to Business Flows | Business flow rate limiting, audit logging |
| API7: SSRF | No outbound fetch, no XML processing |
| API8: Security Misconfiguration | Production profile, no debug, `error.include-stacktrace=never` |
| API9: Improper Inventory Management | API versioning, no debug endpoints exposed |
| API10: Unsafe Consumption of APIs | N/A — this API doesn't consume external APIs |

### OWASP ASVS 4.0.3 — Level 2 ✅
All Level 2 requirements are implemented. See [security documentation](docs/SECURITY.md).

### LGPD Compliance
| Principle | Implementation |
|----------|---------------|
| **Lawful basis** | Art. 7º, II — authentication is necessary for API access (legitimate interest) |
| **Data minimization** | Only username, email, password hash stored (no name, phone, address) |
| **Purpose limitation** | Data used ONLY for authentication & authorization |
| **Storage limitation** | User data retained until explicit deletion request |
| **Integrity & confidentiality** | BCrypt hashing, HTTPS, encrypted connections |
| **Rights of the user** | Account deletion available via API (right to erasure, Art. 16) |

---

## 📁 Project Structure

```
kotlin-pg-api/
├── .github/
│   └── workflows/
│       └── ci.yml                    # CI/CD pipeline (7 stages)
├── src/
│   ├── main/
│   │   ├── kotlin/com/example/api/
│   │   │   ├── ApiApplication.kt     # Entry point
│   │   │   ├── config/
│   │   │   │   └── OpenApiConfig.kt  # OpenAPI/Swagger config + JWT security scheme
│   │   │   ├── controller/
│   │   │   │   ├── ProductController.kt
│   │   │   │   └── AuthController.kt
│   │   │   ├── dto/                  # Input/output DTOs (DTO pattern)
│   │   │   │   ├── ProductDTO.kt
│   │   │   │   ├── ProductResponseDTO.kt
│   │   │   │   └── AuthDTOs.kt
│   │   │   ├── exception/
│   │   │   │   └── ResourceNotFoundException.kt
│   │   │   ├── handler/
│   │   │   │   └── GlobalExceptionHandler.kt
│   │   │   ├── model/                # JPA entities
│   │   │   │   ├── Product.kt
│   │   │   │   └── User.kt
│   │   │   ├── repository/
│   │   │   │   ├── ProductRepository.kt
│   │   │   │   └── UserRepository.kt
│   │   │   ├── security/             # Security layer
│   │   │   │   ├── JwtTokenProvider.kt
│   │   │   │   ├── JwtAuthenticationFilter.kt
│   │   │   │   ├── SecurityConfig.kt
│   │   │   │   ├── SecurityHeadersFilter.kt
│   │   │   │   ├── RateLimitFilter.kt
│   │   │   │   └── UserDetailsServiceImpl.kt
│   │   │   └── service/
│   │   │       ├── ProductService.kt
│   │   │       └── AuthService.kt
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── schema-postgres.sql
│   │       └── init.sql
│   └── test/
│       └── kotlin/com/example/api/
│           ├── service/
│           │   └── ProductServiceTest.kt
│           ├── security/
│           │   └── JwtTokenProviderTest.kt
│           └── controller/
│               └── ProductControllerIntegrationTest.kt
├── Dockerfile                          # Multi-stage, non-root, distroless
├── docker-compose.yml                  # Dev: PostgreSQL + Redis + App + pgAdmin
├── .dockerignore
├── .env.example                        # Template for environment variables
├── .gitignore
├── pom.xml                             # Maven config with security dependencies
└── README.md
```

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit your changes: `git commit -m "feat: add amazing feature"`
4. Push to the branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

> **Security Note:** All PRs are automatically scanned with Semgrep, CodeQL, and OWASP Dependency-Check. Any HIGH or CRITICAL vulnerabilities will fail the CI pipeline.

---

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

## 👤 Author

**Marco Vinícius** — Software Engineering Student & Security Champion  
📧 marcosvmdilly@gmail.com | 🔗 [GitHub](https://github.com/mvdevelop) | 🔗 [LinkedIn](https://linkedin.com/in/mvdevelop)

---

### 🎯 Key Technical Decisions (for Interviews)

1. **Why Spring Boot 3.5.x over 3.2.0?** Spring Boot 3.2.0 reached EOL in November 2024. The upgrade to 3.5.4 ensures all CVEs in the Framework/Security cluster (CVE-2024-22265, CVE-2024-22254, CVE-2024-22259, CVE-2024-22257) are patched.

2. **Why BCrypt cost factor 12?** OWASP recommends 10–12 for BCrypt. Cost 12 provides ~250ms per hash — strong brute-force resistance without unacceptable latency for users.

3. **Why JWT over sessions?** Stateless architecture scales horizontally without sticky sessions. Redis is used for token revocation lists when needed.

4. **Why parameterized queries over native SQL?** Spring Data JPA's query derivation and `@Query` with named parameters inherently prevent SQL injection (CWE-89) — user input never touches the SQL string.

5. **Why DTOs separate from entities?** The DTO pattern enforces a strict whitelist of JSON fields exposed to clients, preventing IDOR (CWE-639) and excessive data exposure.
