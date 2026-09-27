# ADR-002: JWT vs. Session-Based Authentication

- **Status:** Accepted
- **Date:** 2025-09-27
- **Deciders:** Marco Vinícius, Security Champion

---

## Context & Problem

The API requires authentication for all endpoints except `/api/auth/**`. Two approaches were considered:

1. **Session-based auth:** Server stores session state, client receives a session cookie
2. **JWT-based auth:** Stateless tokens containing encoded user claims

### Comparison

| Factor | Session | JWT |
|--------|---------|-----|
| State | Server-side state required | Stateless |
| Scalability | Requires sticky sessions or shared session store | Horizontally scalable |
| Bandwidth | Cookie is small (~50 bytes) | JWT is larger (~1KB+ with claims) |
| Revocation | Immediate (delete server-side session) | Requires blocklist/revocation list |
| Complexity | Simple to implement | More complex token management |
| Caching | Easy (session ID → user) | Must re-validate token claims |

## Decision

**Use JWT-based authentication.**

### Rationale
- **Scalability:** The API is designed for cloud deployment with multiple replicas. JWT eliminates the need for sticky sessions or shared session state.
- **Simplicity:** No session store to manage (Redis, Memcached, etc.)
- **Decoupling:** Each microservice can independently validate JWTs without calling an auth service.
- **Standard:** JWT is an RFC (RFC 7519) with broad library support.

### Security Tradeoffs
- **Token revocation:** JWTs cannot be instantly revoked without a blocklist. Mitigated by short expiration (24h) + Redis blocklist for emergency revocation.
- **Token size:** JWTs are larger than session cookies. Acceptable for API traffic where payload size is not critical.
- **Replay attacks:** Mitigated by short expiration + HTTPS-only transport (HSTS).

### Implementation
- Algorithm: HS512 (HMAC-SHA-512)
- Secret: 256+ bit key from environment variable `JWT_SECRET`
- Expiration: 24 hours
- Claims: `sub` (username), `exp`, `iat`, `roles`
- Library: JJWT 0.12.6

---

## Links

- [RFC 7519 — JSON Web Token (JWT)](https://datatracker.ietf.org/doc/html/rfc7519)
- [OWASP JSON Web Token Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/JSON_Web_Token_for_Java_Cheat_Sheet.html)
- [OWASP ASVS 4.0.3 — V3.1: Authentication Requirements](https://owasp.org/www-project-application-security-verification-standard/)
