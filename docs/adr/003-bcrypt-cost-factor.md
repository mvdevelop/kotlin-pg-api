# ADR-003: BCrypt Password Hashing Cost Factor

- **Status:** Accepted
- **Date:** 2025-09-27
- **Deciders:** Marco Vinícius, Security Champion

---

## Context & Problem

User passwords must be stored securely using a password hashing algorithm that is:
- Resistant to brute-force attacks
- Has a configurable work factor (to adapt as hardware improves)
- Widely tested and audited

### Options Considered

| Algorithm | Pro | Con | Decision |
|-----------|-----|-----|----------|
| **BCrypt** (cost 10) | Well-tested, widely supported | Default cost may be insufficient for 2025 | ⚠️ Upgrade |
| **BCrypt** (cost 12) | Strong security, ~250ms per hash | Slightly slower login UX | ✅ **Selected** |
| **Argon2id** | Winner of PHC, strongest | Less library support in Java ecosystem | Future consideration |
| **PBKDF2** | NIST-approved, built-in to Java | Susceptible to GPU attacks | Not preferred |

## Decision

**Use BCrypt with cost factor 12.**

### Rationale

Per the [OWASP Password Storage Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html):

> "As GPU-based attackers have increased the speed of their GPUs, BCrypt with a cost factor of 10 is no longer sufficient. A minimum cost of 12 is recommended."

- **Cost factor 12** = 2^12 = 4,096 iterations = ~250ms per hash on modern hardware
- This makes each password verification take ~250ms, making brute-force attacks 4,096× slower than cost 10
- Login latency of ~250ms is within acceptable UX thresholds
- BCrypt includes built-in salt generation (128-bit random salt per password)

### Security Properties

| Property | Value |
|----------|-------|
| Algorithm | bcrypt |
| Cost factor | 12 (2^12 iterations) |
| Salt length | 128 bits (auto-generated per password) |
| Output length | 184 bits (60-char Base64-encoded string) |
| Memory usage | ~32 KB per hash operation |
| Hardware resistance | GPU-resistant (memory-hard) |

### Implementation

```kotlin
@Bean
fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder(12)
```

### Review Date

This decision should be re-evaluated annually as hardware improves. Consider increasing to cost 13 in 2026 if hardware performance warrants it.

---

## Links

- [OWASP: Password Storage Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html)
- [OWASP ASVS 4.0.3 — V3.6.1: Password storage](https://owasp.org/www-project-application-security-verification-standard/)
- [RFC 7914 — The scrypt Password-Based Key Derivation Function](https://datatracker.ietf.org/doc/html/rfc7914)
- [NIST SP 800-63B — Digital Identity Guidelines](https://pages.nist.gov/800-63-3/sp800-63b.html)
