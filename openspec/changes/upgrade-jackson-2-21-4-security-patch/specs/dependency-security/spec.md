## ADDED Requirements

### Requirement: Jackson version SHALL resolve known fixable CVEs

The project SHALL declare a Jackson version in `gradle/dependencies.gradle` that is not affected by any CVE with a published fix on the Jackson 2.21.x LTS line. As of June 2026, the minimum compliant version is 2.21.4.

#### Scenario: Jackson version meets security baseline

- **WHEN** the `jackson` version variable in `gradle/dependencies.gradle` is inspected
- **THEN** the value MUST be `2.21.4` or later within the 2.21.x line

#### Scenario: Known Jackson CVEs are patched

- **WHEN** comparing declared Jackson version against CVE-2025-52999, CVE-2026-54512, CVE-2026-54513, CVE-2026-54514, CVE-2026-54516, CVE-2026-54517, and CVE-2026-54518
- **THEN** the declared version MUST be at or above the vendor-published fix version (2.21.4)

### Requirement: Vulnerability report SHALL reflect current dependency state

The file `doc/VULNERABILITY_REPORT.md` SHALL accurately document which CVEs are mitigated by the current dependency versions and which remain, including CVEs published after the initial 2026-03-18 scan.

#### Scenario: Jackson mitigated CVEs are listed

- **WHEN** a reader consults `doc/VULNERABILITY_REPORT.md`
- **THEN** the document MUST list CVE-2025-52999 and CVE-2026-545xx series as mitigated by the Jackson 2.21.4 upgrade

#### Scenario: New Spring Framework residual CVEs are documented

- **WHEN** a reader consults the "CVEs Still Present" section
- **THEN** the document MUST include CVE-2026-41849, CVE-2026-41847, and CVE-2026-41853 with severity, fix availability, and springfox applicability assessment

#### Scenario: Executive summary counts are accurate

- **WHEN** a reader consults the executive summary table
- **THEN** the resolved and remaining CVE counts MUST reflect the updated inventory after this change

### Requirement: Dependency upgrade documentation SHALL record patch-level changes

The file `doc/DEPENDENCY_UPGRADE.md` SHALL document the Jackson 2.21.0 → 2.21.4 upgrade with version change, CVE resolution list, and compatibility notes.

#### Scenario: Jackson patch upgrade is documented

- **WHEN** a reader consults `doc/DEPENDENCY_UPGRADE.md`
- **THEN** the document MUST include an entry for Jackson 2.21.0 → 2.21.4 with rationale and list of CVEs resolved

### Requirement: Core modules SHALL pass tests after dependency patch upgrade

After upgrading Jackson, all 12 testable core modules (as defined in Task 3.4 validation) MUST compile and pass tests when built with Java 8.

#### Scenario: Core module tests pass on Java 8

- **WHEN** `./gradlew test` is executed on the 12 core modules with `JAVA_HOME` set to Java 8
- **THEN** all tests MUST pass with zero failures attributable to the Jackson version change
