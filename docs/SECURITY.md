# Security Documentation

> **Document Version:** 1.0.0 | **Date:** September 2025 | **Standard:** OWASP ASVS 4.0.3 Level 2

---

## 1. Security Architecture Overview

This document details the security controls implemented in the Kotlin Product API. It aligns with **OWASP Application Security Verification Standard (ASVS) 4.0.3 Level 2** and **OWASP API Security Top 10 (2023)**.

### 1.1 Trust Boundaries

```
┌─────────────────────────────────────────────────┐
│  Internet (Untrusted)                             │
├──────────────────┬─────────────┬────────────┤
│  Reverse Proxy    │  API Layer   │  Database  │
│  (TLS Termination)│  (App)       │  (PG+Redis)│
├──────────────────┴─────────────┴────────────┤
│  ─ Trusted Internal Network ───────────────────│
└─────────────────────────────────────────────────┘
```

| Boundary | Protection |
|----------|-----------|
| Internet → App | HTTPS (TLS 1.2+), WAF, security headers |
| App → Database | Encrypted connections (TLS), credential rotation |
| Internal API calls | Service-to-service JWT authentication |

### 1.2 Security Layers

| Layer | Controls | Standard |
|-------|---------|----------|
| **Perimeter** | TLS termination, WAF, DDoS protection | ASVS V14.5 |
| **Network** | Security groups, VPC, private subnets | ASVS V14.6 |
| **Edge** | Security headers (CSP, HSTS, X-Frame-Options) | ASVS V1.10 |
| **API** | JWT authentication, RBAC, rate limiting | ASVS V3, V4 |
| **Data** | Parameterized queries, DTO whitelisting, BCrypt | ASVS V5, V12 |
| **Host** | Non-root containers, distroless images | ASVS V14.5 |
| **Pipeline** | SAST, DAST, dependency scanning | ASVS V1.5 |

---

## 2. Authentication

### 2.1 Password Storage

**Standard:** OWASP ASVS V3.6.1, NIST SP 800-63B

| Property | Value |
|---------|-------|
| **Algorithm** | BCrypt |
| **Work factor (cost)** | 12 (~250ms per hash) |
| **Salt** | Auto-generated per password (128-bit) |
| **Library** | Spring Security Crypto (`BCryptPasswordEncoder`) |

**Code:** `SecurityConfig.kt` → `passwordEncoder()` bean

```kotlin
@Bean
fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder(12)
```

**Why BCrypt cost 12?**
- OWASP Password Storage Cheat Sheet recommends 10–12
- Cost 12 provides ~4,096 rounds — strong brute-force resistance
- Latency (~250ms) is acceptable for login flows
- Can be tuned upward as hardware improves

### 2.2 JWT Token Security

**Standard:** ASVS V3.1, RFC 7519

| Property | Value |
|---------|-------|
| **Algorithm** | HS512 (HMAC-SHA-512) |
| **Secret storage** | Environment variable `JWT_SECRET` (Base64-encoded) |
| **Secret minimum** | 256 bits (32 bytes) |
| **Expiration** | 24 hours (configurable via `JWT_EXPIRATION`) |
| **Claims** | `sub` (username), `exp`, `iat`, `roles` |
| **Library** | JJWT 0.12.6 (no known CVEs) |

**Token flow:**

```
1. User POSTs /api/auth/login (username + password)
2. AuthService validates password against BCrypt hash
3. JwtTokenProvider generates JWT (signed with HS512 + env secret)
4. Token returned to client
5. Client sends Authorization: Bearer {token} header
6. JwtAuthenticationFilter validates token on every request
7. SecurityContext populated with authenticated principal
```

**Security considerations:**
- Tokens are stateless — scales without session storage
- Token revocation: use Redis blocklist for immediate invalidation (TODO)
- Token contains NO PII — only username and roles

### 2.3 Account Security

| Control | Implementation | Standard |
|--------|---------------|----------|
| **Failed attempt tracking** | `failed_login_attempts` column in DB | ASVS V3.4 |
| **Account lockout** | After 5 failed attempts, locked for 15 min | ASVS V3.4.1 |
| **Rate limiting** | 10 login attempts/min per IP (Bucket4j) | ASVS V4.1.3 |
| **Password complexity** | 12+ chars, upper+lower+digit+special | ASVS V3.4.2 |

---

## 3. Authorization

### 3.1 Role-Based Access Control (RBAC)

**Standard:** OWASP ASVS V4.2

| Role | Permissions | Endpoints |
|------|------------|-----------|
| `ROLE_USER` | Read products | GET /api/products/** |
| `ROLE_ADMIN` | Full CRUD on products | GET/POST/PUT/DELETE /api/products/** |
| *(none)* | Auth only | POST /api/auth/** |

**Method-level security** via `@PreAuthorize`:

```kotlin
@PreAuthorize("hasRole('ADMIN')")
fun createProduct(@Valid @RequestBody dto: ProductDTO): ResponseEntity<ProductResponseDTO>
```

### 3.2 Access Control Matrix

| Resource | Method | USER | ADMIN | Unauthenticated |
|----------|--------|------|-------|-----------------|
| `/api/auth/**` | POST | ✅ | ✅ | ✅ |
| `/api/products` | GET | ✅ | ✅ | ❌ (401) |
| `/api/products` | POST | ❌ (403) | ✅ | ❌ (401) |
| `/api/products/{id}` | PUT | ❌ (403) | ✅ | ❌ (401) |
| `/api/products/{id}` | DEL | ❌ (403) | ✅ | ❌ (401) |
| `/actuator/health` | GET | ✅ | ✅ | ✅ |
| `/actuator/**` | * | ❌ (403) | ✅ | ❌ (401) |

---

## 4. Input Validation & Output Encoding

### 4.1 Validation Strategy

**Standard:** OWASP ASVS V5.1, V5.3

| Layer | Technique | Purpose |
|-------|----------|---------|
| **DTO layer** | Bean Validation (`@NotBlank`, `@Size`, `@Pattern`) | Reject malformed input at API boundary |
| **Database layer** | Column types, CHECK constraints | Defense-in-depth for data integrity |
| **Query layer** | JPA parameterized queries | Prevent SQL injection (CWE-89) |

**Example — ProductDTO validation:**

```kotlin
data class ProductDTO(
    @field:NotBlank(message = "Name is required")
    @field:Size(min = 3, max = 100, message = "Name must be 3-100 characters")
    val name: String,

    @field:NotBlank(message = "Price is required")
    @field:DecimalMin(value = "0.01", message = "Price must be greater than zero")
    val price: BigDecimal,

    @field:DecimalMin(value = "0", message = "Quantity cannot be negative")
    val quantity: Int
)
```

### 4.2 XSS Prevention

| Control | Implementation |
|--------|---------------|
| **Content Security Policy** | `default-src 'self'` — blocks inline scripts |
| **XSS Protection header** | `X-XSS-Protection: 0` (disabled legacy filter — rely on CSP) |
| **JSON output encoding** | Jackson's default escaping (no `@JsonRawValue`) |
| **API responses** | `Content-Type: application/json` — not `text/html` |

### 4.3 Injection Prevention

All database access uses **Spring Data JPA** with parameterized JPQL:

```kotlin
// SAFE — parameters are bound, not interpolated
@Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice")
fun findByPriceRange(
    @Param("minPrice") minPrice: BigDecimal,
    @Param("maxPrice") maxPrice: BigDecimal
): List<Product>
```

**No native SQL strings are used anywhere** — all queries are JPQL with named parameters.

---

## 5. Transport Security

### 5.1 HTTPS Enforcement

| Control | Value |
|--------|-------|
| **Protocol** | TLS 1.2+ (TLS 1.3 preferred) |
| **HSTS** | `max-age=31536000; includeSubDomains; preload` |
| **HSTS preload** | Registered at [hstspreload.org](https://hstspreload.org) |
| **Certificate** | Let's Encrypt (production) — auto-renewal |

### 5.2 Security Headers

| Header | Value | Purpose | CWE |
|--------|-------|--------|-----|
| `Content-Security-Policy` | `default-src 'self'` | Prevents XSS | CWE-79 |
| `X-Frame-Options` | `DENY` | Prevents clickjacking | CWE-1021 |
| `X-Content-Type-Options` | `nosniff` | Prevents MIME sniffing | CWE-430 |
| `Strict-Transport-Security` | `max-age=31536000; includeSubDomains; preload` | Enforces HTTPS | CWE-319 |
| `Referrer-Policy` | `strict-origin-when-cross-origin` | Controls referrer leakage | CWE-200 |
| `Permissions-Policy` | `geolocation=(), microphone=(), camera=()` | Restricts browser APIs | CWE-73 |
| `Cache-Control` | `no-store, no-cache, must-revalidate` | Prevents caching of sensitive data | CWE-525 |

**Implementation:** `SecurityHeadersFilter.kt`

---

## 6. Rate Limiting

**Standard:** OWASP ASVS V4.1.3, OWASP API Security API4:2023

| Endpoint | Limit | Scope | Implementation |
|----------|-------|-------|----------------|
| `/api/auth/login` | 10 req/min | Per IP | Bucket4j (in-memory) |
| `/api/auth/register` | 3 req/hour | Per IP | Bucket4j (in-memory) |
| `/api/**` (other) | 100 req/min | Per IP | Bucket4j (in-memory) |

**Production note:** For distributed deployments, replace in-memory buckets with Redis-backed `ProxyManager`.

---

## 7. Error Handling

### 7.1 Security Principles

| Principle | Implementation |
|----------|---------------|
| **No information leakage** | Stack traces never sent to client |
| **Generic error messages** | "Authentication failed" — no username enumeration |
| **Standard error format** | RFC 7807-style JSON error response |
| **Server-side logging** | Full details logged with correlation ID |

### 7.2 Error Response Format

```json
{
  "timestamp": "2025-09-27T14:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed — one or more fields have invalid values",
  "errors": {
    "name": "Name must be between 3 and 100 characters",
    "price": "Price must be greater than zero"
  },
  "path": "/api/products"
}
```

---

## 8. Secrets Management

### 8.1 Current State (Development)

| Secret | Location | Rotation |
|--------|----------|----------|
| Database password | `.env` (not committed) | Manual |
| JWT secret | `.env` (not committed) | Manual |
| Redis password | `.env` (not committed) | Manual |

### 8.2 Production Recommendations

| Secret | Management | Standard |
|--------|-----------|----------|
| All secrets | HashiCorp Vault / AWS Secrets Manager | ASVS V3.5.1 |
| JWT secret | Auto-rotated every 90 days | NIST SP 800-57 |
| DB password | Auto-rotated every 30 days | ASVS V12.3.1 |
| API keys | Vault with lease/renewal | ASVS V6.2 |

---

## 9. Container Security

### 9.1 Docker Hardening

**Dockerfile** implements:

| Control | Implementation |
|--------|---------------|
| **Multi-stage build** | `maven:3.9` (build) → `eclipse-temini:java21` (runtime) |
| **Non-root user** | `USER 1001` |
| **Distroless base** | No shell, no package manager |
| **No build artifacts in final image** | Only the JAR is copied |
| **Minimal attack surface** | ~20MB image vs ~400MB for full JDK |

### 9.2 Runtime Security

| Control | Implementation |
|--------|---------------|
| **Read-only filesystem** | Container starts with read-only root FS |
| **No `latest` tag** | Pinned version in Dockerfile |
| **User namespace** | Runs as uid 1001 |
| **Dropped capabilities** | `--cap-drop=ALL` |
| **Seccomp profile** | Default Docker seccomp |

---

## 10. Vulnerability Management

### 10.1 SAST (Static Analysis)

| Tool | Frequency | Scope |
|------|----------|-------|
| **Semgrep** | Every PR | Java/Kotlin rules + secret scanning |
| **CodeQL** | Every PR | Deep semantic analysis |
| **OWASP Dependency-Check** | Every PR | CVE scanning of dependencies |

### 10.2 DAST (Dynamic Analysis)

| Tool | Frequency | Scope |
|------|----------|-------|
| **Trivy** | Every build | Container image CVE scan |
| **OWASP ZAP** | Staging deploys | Dynamic vulnerability scanning |

### 10.3 Dependency Scanning

Dependencies are scanned against:
- **NVD** (National Vulnerability Database)
- **GHSA** (GitHub Security Advisories)
- **OSV** (Open Source Vulnerabilities)

Fail criteria: **CVSS >= 7.0** (HIGH or CRITICAL).

---

## 11. Monitoring & Logging

### 11.1 Structured Logging

**Format:** JSON with correlation ID

```json
{
  "timestamp": "2025-09-27T14:30:00.123Z",
  "level": "INFO",
  "service": "kotlin-pg-api",
  "correlationId": "a1b2c3d4-e5f6-7890",
  "eventType": "LOGIN_SUCCESS",
  "userId": "*"
}
```

**Never logged:**
- Passwords (plaintext or hashed)
- Full email addresses (masked: `u***@example.com`)
- JWT tokens
- Personal identification data beyond username ID

### 11.2 Audit Trail

All security-relevant events are recorded in the `audit_log` table:

| Event | Fields Stored | Retention |
|-------|--------------|-----------|
| LOGIN_SUCCESS | username, IP, user-agent | 90 days |
| LOGIN_FAILURE | IP, user-agent | 90 days |
| REGISTER | username (masked) | Indefinite |
| PRODUCT_CREATE/UPDATE/DELETE | product ID | 365 days |
| USER_DELETE | user ID | Indefinite |

---

## 12. LGPD Compliance Matrix

| Article | Requirement | Implementation |
|---------|------------|----------------|
| **Art. 7º, II** | Legal basis: contract execution | Authentication required for API access |
| **Art. 9** | Data processing transparency | Privacy policy in API docs |
| **Art. 11** | Security of processing | BCrypt, HTTPS, access controls |
| **Art. 16** | Right to erasure | Account deletion endpoint |
| **Art. 18** | Data portability | Export user data endpoint |
| **Art. 18-A** | Right to correction | Update profile endpoint |

### Sensitive Data Handling

| Data | Storage | Justification | Retention |
|------|---------|--------------|-----------|
| Username | Plaintext (VARCHAR) | Required for authentication | Until account deletion |
| Email | Plaintext (VARCHAR) | Required for communication | Until account deletion |
| Password hash | BCrypt (VARCHAR 255) | Authentication | Until account deletion |
| Product data | Plaintext | Business data (not PII) | Indefinite |

---

## 13. Threat Model (STRIDE)

| Threat | Description | Mitigation | Rating |
|--------|------------|-----------|--------|
| **Spoofing** | Attacker impersonates user | JWT authentication, BCrypt | 🔴 Addressed |
| **Tampering** | Data modification in transit | HTTPS/TLS, integrity checks | 🔴 Addressed |
| **Repudiation** | User denies performing action | Audit trail in DB | 🟡 In progress |
| **Information Disclosure** | Sensitive data exposure | DTO whitelist, no stack traces | 🔴 Addressed |
| **Denial of Service** | Resource exhaustion | Rate limiting (Bucket4j) | 🟡 Partial |
| **Elevation of Privilege** | Unauthorized admin access | RBAC, @PreAuthorize | 🔴 Addressed |

---

## 14. Security Checklist

Before each release, verify:

- [ ] No secrets in source code (`git ls-files | xargs grep -r "password\|secret\|key"`)
- [ ] All dependencies patched (OWASP Dependency-Check passes)
- [ ] Docker image scanned with Trivy (zero CRITICAL)
- [ ] SAST passes (Semgrep + CodeQL clean)
- [ ] Security headers present (test with `curl -I`)
- [ ] CORS properly configured (only allowed origins)
- [ ] Rate limiting active (test with rapid requests)
- [ ] Error responses do not leak internals
- [ ] JWT secret is 256+ bits and auto-rotated

---

## 15. References

- [OWASP ASVS 4.0.3](https://owasp.org/www-project-application-security-verification-standard/)
- [OWASP API Security Top 10 (2023)](https://owasp.org/API-Security/editions/2023/en/0x11-t10/)
- [OWASP Top 10 (2021)](https://owasp.org/Top10/)
- [Spring Security Documentation](https://docs.spring.io/spring-security/reference/)
- [OWASP Cheat Sheet Series](https://cheatsheetseries.owasp.org/)
- [NIST SP 800-63B (Digital Identity Guidelines)](https://pages.nist.gov/800-63-3/sp800-63b.html)
