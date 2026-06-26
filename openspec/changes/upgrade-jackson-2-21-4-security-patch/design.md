## Context

Springfox branch `2.10.5-bjca-patch` already upgraded Jackson from 2.10.1 → 2.21.0 in Task 3.4, resolving WS-2022-0468 and dozens of earlier CVEs. The project is constrained to Java 8 and Spring Boot 2.7.18. Jackson 2.21.x remains the highest compatible 2.x LTS line.

A post-scan review (June 2026) identified Jackson 2.21.0 as affected by CVE-2025-52999 (DoS, CVSS 8.7) and five additional CVEs fixed in Jackson 2.21.4 (released May 2026). Three new Spring Framework CVEs were also published but cannot be fixed within current constraints.

Existing documentation lives in `doc/VULNERABILITY_REPORT.md` and `doc/DEPENDENCY_UPGRADE.md`, last updated 2026-03-18.

## Goals / Non-Goals

**Goals:**

- Upgrade Jackson to 2.21.4 (latest 2.21.x patch) to resolve all known fixable Jackson CVEs
- Update vulnerability and dependency upgrade documentation to reflect current state
- Verify build and tests pass on Java 8 using the same module set validated in Task 3.4

**Non-Goals:**

- Upgrading Spring Framework beyond 5.3.39
- Migrating to Spring Boot 3.x or Java 17
- Performing code-level SAST (SpotBugs/Semgrep) — remains a future task
- Changing production source code unless test failures require it

## Decisions

### 1. Target Jackson 2.21.4 (not 2.21.1 only)

**Decision**: Bump directly to 2.21.4 rather than stopping at 2.21.1.

**Rationale**: 2.21.4 is the latest patch on the same LTS line already in use. It resolves CVE-2025-52999 (fixed in 2.21.1) plus five additional CVEs from the June 2026 advisory batch. Patch-to-patch upgrades within 2.21.x carry minimal API risk.

**Alternatives considered**:
- Stay on 2.21.0 — rejected; leaves known CVEs unpatched
- Jump to Jackson 3.x — rejected; requires Java 17

### 2. Single version variable change

**Decision**: Only modify `jackson = '2.21.4'` in `gradle/dependencies.gradle`. All Jackson artifacts (`jackson-core`, `jackson-databind`, `jackson-annotations`) reference this variable.

**Rationale**: Matches Task 3.4 pattern; zero production code changes were needed for 2.13.5 → 2.21.0.

### 3. Document Spring CVEs as residual, not actionable

**Decision**: Add CVE-2026-41849, CVE-2026-41847, CVE-2026-41853 to the "CVEs Still Present" section with low-risk assessment for springfox as a library.

**Rationale**: Fixes require Spring Framework 5.3.49+ (commercial only) or Spring 6.x. Same EOL constraint documented since Task 3.3.

### 4. Test scope: 12 core modules on Java 8

**Decision**: Run tests on the same 12 modules validated in Task 3.4 (exclude swagger-ui, spring-integration*, contract-tests due to pre-existing infrastructure issues).

**Rationale**: Consistent with prior upgrade validation; patch release unlikely to introduce regressions beyond what full test suite would catch.

## Risks / Trade-offs

- [Spring Boot 2.7.18 BOM expects Jackson 2.13.x] → Documented in DEPENDENCY_UPGRADE.md; downstream consumers should override to 2.21.4
- [StreamReadConstraints defaults in Jackson 2.15+] → Already documented; unchanged by 2.21.0 → 2.21.4 patch
- [Test failure on unexpected API change] → Unlikely for patch release; fix test code only if needed, following Task 3.4 patterns
- [Spring CVE scanner false positives persist] → Document as residual with applicability notes, not fixable in this change

## Migration Plan

1. Update `gradle/dependencies.gradle` version variable
2. Run `./gradlew test` on core modules with Java 8
3. Update both doc files
4. No rollback needed beyond reverting the version bump; no database or config migration

## Open Questions

None — scope and approach are fully determined from prior Task 3.4 experience.
