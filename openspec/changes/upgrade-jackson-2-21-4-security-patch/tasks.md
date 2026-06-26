## 1. Dependency Upgrade

- [x] 1.1 Update `jackson = '2.21.4'` in `gradle/dependencies.gradle`

## 2. Verification

- [x] 2.1 Run core module tests on Java 8 (12 modules per Task 3.4 scope) — verified 2026-06-25 using `/Volumes/LIBIAO_HY/dev/gradle-5.2-bin.zip`
- [x] 2.2 Fix any test failures caused by Jackson API changes (if needed) — N/A: patch release within 2.21.x; no source changes required

## 3. Documentation

- [x] 3.1 Update `doc/VULNERABILITY_REPORT.md`: add Jackson 2.21.4 mitigated CVEs, add three new Spring residual CVEs, adjust executive summary counts
- [x] 3.2 Update `doc/DEPENDENCY_UPGRADE.md`: add Jackson 2.21.0 → 2.21.4 patch section with CVE list and compatibility notes

## 4. Validation

- [x] 4.1 Add `DependencyVersionChecker` (buildSrc) + `verifyDependencySecurity` Gradle task
- [x] 4.2 Add `JacksonRuntimeVersionSpec` runtime smoke tests in springfox-schema
- [x] 4.3 Add `make verify` target — all checks pass on Java 8
