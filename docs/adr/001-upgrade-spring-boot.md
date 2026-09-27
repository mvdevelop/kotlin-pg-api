# ADR-001: Upgrade Spring Boot to 3.5.x LTS

- **Status:** Accepted
- **Date:** 2025-09-27
- **Deciders:** Marco Vinícius (Lead), Security Champion
- **Technical Story:** The application was running Spring Boot 3.2.0, which reached EOL on November 21, 2024.

---

## Context & Problem

Spring Boot 3.2.0 shipped with Spring Framework 6.1.1, which is vulnerable to multiple CVEs:

| CVE | Severity | Affected Versions | Fixed In |
|-----|----------|------------------|----------|
| CVE-2024-22265 | HIGH (6.5) | Framework 6.1.0–6.1.1 | 6.1.2 |
| CVE-2024-22254 | MEDIUM (5.3) | Framework 6.0.0–6.0.9, 6.1.0–6.1.1 | 6.0.10, 6.1.2 |
| CVE-2024-22259 | CRITICAL (9.8) | Security 6.1.4–6.2.0 | 6.2.1+ |

With Boot 3.2.0, the Spring Framework version (6.1.1) is vulnerable to all three.

### Options Considered

1. **Upgrade to Spring Boot 3.2.12** (latest 3.2.x patch) — Patches all CVEs but remains EOL (no security support after November 2024).
2. **Upgrade to Spring Boot 3.5.x** (current LTS) — Patches all CVEs, receives support until May 2027.
3. **Keep Spring Boot 3.2.0** — Unacceptable security risk.

## Decision

**Upgrade to Spring Boot 3.5.4.** This is the latest stable LTS release as of September 2025.

### Rationale
- **Security:** Patches all CVEs in the 3.2.x line
- **Support:** Active security patches until May 2027
- **Compatibility:** Minimal breaking changes from 3.2.x → 3.5.x
- **Features:** Includes Spring Security 6.4+ (improved JWT support)

### Consequences
- Requires Maven version update (already handled)
- Kotlin version bumped from 1.9.22 → 2.1.21 (required by Boot 3.5.x)
- All existing API contracts preserved (no breaking API changes)
- `ddl-auto: update` → `validate` (security improvement)

---

## Links

- [Spring Boot 3.5.x Release Notes](https://github.com/spring-projects/spring-boot/releases)
- [Spring CVE Advisories](https://spring.io/security)
- [NIST NVD — CVE-2024-22265](https://nvd.nist.gov/vuln/detail/CVE-2024-22265)
