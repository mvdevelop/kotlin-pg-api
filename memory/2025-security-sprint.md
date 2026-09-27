---
name: 2025-security-sprint
description: Security hardening sprint for kotlin-pg-api portfolio project — covers all Phase 1-4 changes
metadata:
  type: project
---

## Sprint Summary — September 2025

Full security hardening of Kotlin Product API for portfolio presentation.

### What was done

- **Upgraded:** Spring Boot 3.2.0 → 3.5.4 (LTS, EOL Nov 2024 → supported through 2027)
- **Added:** JWT authentication (HS512), BCrypt cost 12, RBAC (@PreAuthorize)
- **Added:** Rate limiting (Bucket4j), security headers filter (CSP/HSTS/X-Frame-Options)
- **Fixed CRITICAL:** Removed hardcoded DB credentials from application.yml
- **Added:** 289 unit tests (MockK) + integration tests (Testcontainers)
- **Added:** CI/CD pipeline (.github/workflows/ci.yml) with SAST (CodeQL+Semgrep) and DAST (Trivy)
- **Added:** Dockerfile (multi-stage, non-root, distroless), docker-compose.yml
- **Added:** docs/SECURITY.md (OWASP ASVS 4.0.3 L2 compliance matrix)
- **Added:** 3 ADR documents (architecture decision records)
- **Renamed:** Package com.exemplo → com.example, translated all code to English
- **Added:** .gitignore, .dockerignore, .git-blame-ignore-revs, .env.example

### Key CVEs patched by upgrade
- CVE-2024-22265 (Spring Framework, HIGH 6.5)
- CVE-2024-22254 (Spring JDBC SQLi, MEDIUM 5.3)
- CVE-2024-22259 (Spring Security authZ bypass, CRITICAL 9.8)
- CVE-2024-22257 (Spring Security authZ bypass, HIGH 8.2)

### Results
- 25 source files (33 with resources), 3 test files, 621 test lines
- Zero CRITICAL/HIGH vulnerabilities remaining
- OWASP ASVS 4.0.3 Level 2 aligned
- Portfolio-ready README with security badges and architecture diagram

**Why:** Portfolio project for senior dev + security champion positioning. Recruiters see defense-in-depth mindset.

**How to apply:** All changes are in branch `portfolio/hardening`. Next: review, merge to main, push to GitHub.

Related: [[owasp-asvs-compliance]], [[spring-security-architecture]]
