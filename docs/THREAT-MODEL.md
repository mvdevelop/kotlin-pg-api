# Threat Model — STRIDE Analysis

> **Methodology:** STRIDE (Spoofing, Tampering, Repudiation, Information Disclosure, Denial of Service, Elevation of Privilege)
> **Date:** September 2025
> **Asset:** Kotlin Product API — Product Catalog & Authentication Service

---

## 1. System Overview

```
┌──────────────┐      HTTPS      ┌──────────────────┐             ┌─────────────┐
│  Client (SPA) │  ────────────► │  Spring Security  │  ────────► │  PostgreSQL │
│  / Mobile App │  (JWT Bearer)  │  6 + JWT Filter    │   JPA    │    16        │
└──────────────┘  ◄───────────  │  + Rate Limiter    │  ◄───────  │  + Redis    │
                                │  + Security Headers │            │ (sessions)   │
                                └──────────────────┘             └─────────────┘
                                    │     │
                                    │     ├─ /actuator/health (public)
                                    │     └─ /actuator/** (admin only)
                                    │
                                    ├─ /api/auth/** (public, rate limited)
                                    └─ /api/products/** (authenticated, RBAC)
```

---

## 2. Data Flow Diagram (DFD)

### Trust Boundaries

| Boundary | Protection |
|----------|-----------|
| Internet → Load Balancer | TLS 1.2+, WAF, security headers |
| LB → App Container | TLS (internal), mTLS (future) |
| App → Database | TLS, credential rotation |
| App → Redis | Password auth, network ACLs |

### Data Stores

1. **PostgreSQL** — Stores: products, users (password_hash), user_roles, audit_log
2. **Redis** — Stores: rate limit buckets, session blocklist
3. **Logs** — Stores: structured JSON logs with correlation IDs (no PII)

---

## 3. STRIDE Threat Analysis

### 🔴 S — Spoofing

| # | Threat | Description | Mitigation | Status |
|---|--------|-------------|-----------|--------|
| S1 | Credential stuffing | Attacker uses leaked credentials to authenticate | BCrypt hashing, rate limiting (10/min), account lockout | ✅ Mitigated |
| S2 | JWT token forgery | Attacker creates forged JWT | HS512 with 256-bit secret from env var, token signature validation | ✅ Mitigated |
| S3 | User impersonation | Attacker accesses another user's data | RBAC (`@PreAuthorize`), DTO whitelist (no IDOR) | ✅ Mitigated |
| S4 | IP spoofing | Attacker forges X-Forwarded-For | Rate limiting keyed on verified IP (future: trusted proxy config) | ⚠️ Partial |

### 🟠 T — Tampering

| # | Threat | Description | Mitigation | Status |
|---|--------|-------------|-----------|--------|
| T1 | SQL injection | Attacker injects malicious SQL | JPA parameterized queries (no native SQL) | ✅ Mitigated |
| T2 | JSON mutation | Attacker modifies request body | Bean Validation (`@Valid`), DTO immutability | ✅ Mitigated |
| T3 | Token tampering | Attacker modifies JWT claims | HS512 signature verification on every request | ✅ Mitigated |
| T4 | Schema tampering | Attacker modifies DB schema | Flyway migrations with `clean-disabled: true`, `ddl-auto: none` | ✅ Mitigated |

### 🟡 R — Repudiation

| # | Threat | Description | Mitigation | Status |
|---|--------|-------------|-----------|--------|
| R1 | Action attribution | User denies performing an action | Audit trail in `audit_log` table (event type, IP, timestamp) | ⚠️ Partial — no full request body logging |
| R2 | Login repudiation | Failed logins not tracked | `audit_log` records LOGIN_FAILURE with IP + user agent | ✅ Mitigated |
| R3 | Non-repudiation | No proof of action | TODO: Add digital signatures for critical operations | ⚠️ Not started |

### 🔵 I — Information Disclosure

| # | Threat | Description | Mitigation | Status |
|---|--------|-------------|-----------|--------|
| I1 | Stack trace leakage | Internal errors expose stack traces | `include-stacktrace: never`, generic error messages | ✅ Mitigated |
| I2 | IDOR data exposure | Attacker accesses other products' data | DTO pattern — strict field whitelist | ✅ Mitigated |
| I3 | PII in logs | User email/password in logs | Structured logging with PII masking | ✅ Mitigated |
| I4 | Secret exposure | Credentials in source code | `.env` externalized, `.gitignore`, `.dockerignore` | ✅ Mitigated |
| I5 | Endpoint discovery | Attackers enumerate API endpoints | Swagger UI only in dev profile, no debug endpoints | ✅ Mitigated |
| I6 | Version disclosure | Server reveals version info | Custom error headers, no `Server` header | ⚠️ Partial |

### 🟡 D — Denial of Service

| # | Threat | Description | Mitigation | Status |
|---|--------|-------------|-----------|--------|
| D1 | Brute-force login | Attacker tries many password combinations | Rate limiting (10/min), account lockout (5 attempts) | ✅ Mitigated |
| D2 | API flood | Attacker floods API with requests | Rate limiting (100/min per IP) | ✅ Mitigated |
| D3 | Payload DoS | Large request bodies exhaust memory | Spring Boot default: 1MB max body, streaming parser | ⚠️ Partial |
| D4 | Slowloris | Slow HTTP headers exhaust connections | Tomcat default timeouts, connection pool limits | ✅ Mitigated |
| D5 | Database DoS | Heavy queries exhaust DB resources | Query timeout, connection pool limits | ✅ Mitigated |

### 🟣 E — Elevation of Privilege

| # | Threat | Description | Mitigation | Status |
|---|--------|-------------|-----------|--------|
| E1 | Admin escalation | Regular user gains admin access | `@PreAuthorize("hasRole('ADMIN')")` on POST/PUT/DELETE | ✅ Mitigated |
| E2 | Self-registration admin | Attacker registers with ROLE_ADMIN | Server-side role assignment: always `ROLE_USER` for self-registration | ✅ Mitigated |
| E3 | Privilege chaining | Attacker chains multiple low-priv bugs | Defense-in-depth: RBAC + input validation + rate limiting | ✅ Mitigated |
| E4 | Container escape | Attacker escapes container to host | Non-root user, distroless image, no shell, read-only FS | ✅ Mitigated |

---

## 4. Risk Matrix

| Threat | Likelihood | Impact | Risk Score | Priority |
|--------|-----------|--------|-----------|----------|
| S2 (JWT forgery) | Low | Critical | 🔴 High | P0 |
| S1 (Credential stuffing) | High | Medium | 🟡 Medium | P1 |
| T1 (SQL injection) | Low | Critical | 🔴 High | P0 |
| D1 (Brute-force) | High | Medium | 🟡 Medium | P1 |
| E1 (Admin escalation) | Medium | High | 🔴 High | P0 |
| I1 (Stack trace) | Medium | Low | 🟢 Low | P2 |
| I4 (Secret exposure) | Low | Critical | 🔴 High | P0 |
| R1 (No audit trail details) | Medium | Low | 🟢 Low | P2 |

---

## 5. Attack Surface Analysis

### Public Attack Surface (no authentication)

| Endpoint | HTTP Method | Risk | Protection |
|----------|------------|------|-----------|
| `/api/auth/register` | POST | Medium | Rate limited (3/hr), password validation, email uniqueness |
| `/api/auth/login` | POST | High | Rate limited (10/min), account lockout, uniform error messages |
| `/actuator/health` | GET | Low | No information disclosed beyond UP/DOWN |
| `/actuator/info` | GET | Low | Version info only, no secrets |

### Authenticated Attack Surface

| Endpoint | Auth | Risk | Protection |
|----------|------|------|-----------|
| `/api/products/**` (GET) | JWT Bearer | Low | DTO whitelist, rate limited |
| `/api/products` (POST) | JWT + ADMIN | Medium | @PreAuthorize, Bean Validation, parameterized queries |
| `/api/products/{id}` (PUT) | JWT + ADMIN | Medium | ID validation, DTO update, transaction rollback |
| `/api/products/{id}` (DELETE) | JWT + ADMIN | Low | Existence check, hard delete logged |
| `/actuator/**` | JWT + ADMIN | Medium | Restricted to localhost in prod (future: network ACL) |

---

## 6. Mitigation Verification

### Automated Checks

| Requirement | Test | CI/CD Stage |
|-------------|------|-------------|
| No hardcoded secrets | `git ls-files \| grep -i pass` + Gitleaks | Semgrep (security) |
| SAST scanning | CodeQL + Semgrep | SAST stage |
| Dependency vulnerabilities | OWASP Dependency-Check | Dependency Scan |
| Container vulnerabilities | Trivy | Docker Scan |
| Security headers | curl script in integration test | Integration Tests |

### Manual Verification Checklist

Before each release, verify:
- [ ] `curl -I https://api.example.com/api/health` returns HSTS header
- [ ] Unauthorized POST to `/api/products` returns 401
- [ ] Non-admin POST to `/api/products` returns 403
- [ ] Login with wrong password returns generic "Invalid credentials" (no username enum)
- [ ] 10 rapid logins from same IP trigger rate limit
- [ ] JWT tampered → 401 returned
- [ ] Expired JWT → 401 returned
- [ ] Stack trace not in 500 error response
- [ ] SQL injection in name field returns 400 (validation)
- [ ] Response does not contain `password_hash` or `passwordHash` field

---

## 7. Future Hardening (Next Sprint)

| # | Threat | Planned Mitigation | Timeline |
|----|--------|-------------------|----------|
| 1 | I6 (Server header) | Add `server: none` Tomcat config | v2.1 |
| 2 | D3 (Payload DoS) | Spring Cloud Gateway with payload size limit | v2.1 |
| 3 | R3 (Non-repudiation) | Digital signatures for critical operations | v2.2 |
| 4 | S4 (IP spoofing) | Trusted proxy config for X-Forwarded-For | v2.1 |
| 5 | Refresh token rotation | Implement refresh token + blocklist | v2.2 |

---

## 8. References

- [OWASP Threat Dragon — STRIDE methodology](https://owasp.org/www-project-threat-dragon/)
- [Microsoft STRIDE framework](https://learn.microsoft.com/en-us/azure/security/develop/threat-modeling-tool-stride)
- [OWASP ASVS 4.0.3](https://owasp.org/www-project-application-security-verification-standard/)
- [OWASP API Security Top 10 (2023)](https://owasp.org/API-Security/editions/2023/en/0x11-t10/)

---

*Document maintained by: Security Team | Last reviewed: 2025-09-27*
