## Why

The `2.10.5-bjca-patch` branch upgraded Jackson to 2.21.0 (Task 3.4), but six new CVEs were disclosed in June 2026 that affect 2.21.0 through 2.21.3, including a high-severity DoS (CVE-2025-52999). The vulnerability report (`doc/VULNERABILITY_REPORT.md`) is also stale: it does not record three newly published Spring Framework 5.3.39 CVEs. This change closes the Jackson security gap with a minimal patch upgrade and refreshes dependency security documentation.

## What Changes

- Bump `jackson` version in `gradle/dependencies.gradle` from `2.21.0` to `2.21.4`
- Update `doc/VULNERABILITY_REPORT.md`:
  - Mark Jackson CVEs resolved by 2.21.4 (CVE-2025-52999, CVE-2026-54512/54513/54514/54516/54517/54518)
  - Add three new Spring Framework 5.3.39 residual CVEs (CVE-2026-41849, CVE-2026-41847, CVE-2026-41853) with risk assessment
  - Adjust executive summary counts
- Update `doc/DEPENDENCY_UPGRADE.md` with Jackson 2.21.0 → 2.21.4 patch notes and CVE resolution details
- Run core module tests on Java 8 to verify compatibility (same validation pattern as Task 3.4)

## Capabilities

### New Capabilities

- `dependency-security`: Defines requirements for keeping declared dependency versions free of known fixable CVEs and maintaining accurate vulnerability documentation for downstream consumers.

### Modified Capabilities

<!-- No existing openspec/specs/ capabilities to modify -->

## Impact

- **Affected files**: `gradle/dependencies.gradle`, `doc/VULNERABILITY_REPORT.md`, `doc/DEPENDENCY_UPGRADE.md`
- **Runtime impact**: None expected — patch release within Jackson 2.21 LTS line; no production source changes anticipated
- **Downstream consumers**: Should align Jackson to 2.21.4+ in their dependency management to avoid classpath version conflicts
- **Out of scope**: Spring Framework 5.3.39 residual CVEs (require Java 17 / commercial support); code-level SAST scan
