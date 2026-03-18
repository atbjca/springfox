# Springfox Build System Customization – APM Implementation Plan
**Memory Strategy:** Dynamic-MD
**Last Modification:** Manager Agent 2 – Added Phase 5 (Bugfix) with 4 fix tasks based on Task 4.2 validation findings + user feedback on swagger-ui.
**Project Overview:** Customization of the springfox project build system to support Nexus private repository publishing, custom group prefix (libiao.test.io.springfox), local Gradle wrapper configuration, Makefile automation, dependency upgrades, and vulnerability documentation. Version: 2.10.5-bjca-patch / 2.10.5-bjca-patch-SNAPSHOT. Reference patterns from spring-framework, spring-authorization-server, and spring-security projects.

## Phase 1: Core Build Configuration

### Task 1.1 – Configure Custom Group Property - Agent_BuildConfig
**Objective:** Replace hardcoded group with configurable property variable, set to `libiao.test.io.springfox`.
**Output:** Modified `gradle.properties` and `build.gradle` with variable-driven group.
**Guidance:** Reference spring-framework pattern: `projectGroup` in gradle.properties, `group = projectGroup` in build.gradle subprojects block. Must be easily changeable later.

- Add `projectGroup=libiao.test.io.springfox` to `gradle.properties`
- In `build.gradle` subprojects block, change `group = 'io.springfox'` to `group = projectGroup`
- Verify with `./gradlew properties` that group is correctly applied

### Task 1.2 – Configure Local Gradle Wrapper - Agent_BuildConfig
**Objective:** Point Gradle wrapper to local distribution file instead of internet URL.
**Output:** Modified `gradle-wrapper.properties` with local file path.
**Guidance:** Reference spring-framework pattern: `file:///Volumes/LIBIAO_EX/dev/gradle-<version>-bin.zip`. Current version is Gradle 5.2. Check local availability first.

1. Check `/Volumes/LIBIAO_EX/dev/` for available `gradle-*-bin.zip` files to determine usable version
2. Modify `gradle/wrapper/gradle-wrapper.properties`: change `distributionUrl` to `file\:///Volumes/LIBIAO_EX/dev/gradle-<version>-bin.zip`
3. Verify `./gradlew --version` runs successfully with local distribution

### Task 1.3 – Configure Nexus Repository for Dependency Resolution - Agent_BuildConfig
**Objective:** Add Nexus private repository as dependency resolution source across all build configurations.
**Output:** Modified `settings.gradle`, `build.gradle`, and `buildSrc/build.gradle` with Nexus repository configuration.
**Guidance:** Reference spring-framework pattern: nexusPublicUrl + conditional nexusSnapshotUrl, allowInsecureProtocol=true, credentials from ~/.gradle/gradle.properties. Preserve existing jcenter as fallback.

1. Add Nexus repository to `settings.gradle` pluginManagement repositories (mavenLocal + nexusPublicUrl + conditional nexusSnapshotUrl)
2. Add Nexus repository to `build.gradle` repositories and subprojects repositories blocks, keeping existing jcenter
3. Add Nexus repository to `buildSrc/build.gradle` repositories
4. Verify `./gradlew dependencies` resolves correctly

### Task 1.4 – Update Version Management - Agent_BuildConfig
**Objective:** Update project version to match branch naming convention and restore version reading mechanism.
**Output:** Modified `.version` file with `2.10.5-bjca-patch-SNAPSHOT`, and `build.gradle` with direct version reading from `.version` file (replacing disabled `springfox-multi-release` plugin).
**Guidance:** The original `springfox-multi-release` plugin was disabled in Task 1.3 (jcenter dependency). Replace with direct file reading: `version = file(".version").text.trim()` in root `build.gradle`.

- Modify `.version` file content to `2.10.5-bjca-patch-SNAPSHOT`
- Add version reading in root `build.gradle` (after apply statements): `version = file(".version").text.trim()`
- Verify `./gradlew properties | grep version` outputs `2.10.5-bjca-patch-SNAPSHOT`

## Phase 2: Publishing & Deployment

### Task 2.1 – Configure Maven Publish with Nexus Repository - Agent_BuildConfig
**Objective:** Replace existing Bintray/Artifactory publishing with maven-publish + Nexus private repository.
**Output:** Modified `gradle/publishing.gradle` and `build.gradle` with Nexus publishing configuration.
**Guidance:** Reference spring-framework allprojects MavenPublishPlugin pattern. Auto-select snapshot/release repo by version suffix. Exclude petstore modules from publishing. **Depends on: Task 1.1, 1.3, 1.4 Output**

1. Modify `gradle/publishing.gradle`: remove Bintray and Artifactory plugin configurations, retain `maven-publish` plugin, configure publications (from components.java + sources jar + javadoc jar) with POM metadata
2. Add global Nexus publishing repository in root `build.gradle`: `allprojects { plugins.withType(MavenPublishPlugin) { publishing { repositories { maven { url = version.endsWith('-SNAPSHOT') ? nexusSnapshotUrl : nexusReleaseUrl; allowInsecureProtocol = true; credentials { ... } } } } } }`
3. Update `buildSrc` `ProjectDefinitions.publishable()` to exclude petstore modules (current logic already excludes contract-tests and spring-config)
4. Ensure POM configuration retains necessary project metadata (groupId, artifactId, version, description)
5. Verify `./gradlew publishToMavenLocal` succeeds for publishable modules

### Task 2.2 – Create Makefile - Agent_BuildConfig
**Objective:** Create Makefile with standard build/deploy targets for developer convenience.
**Output:** New `Makefile` in project root.
**Guidance:** Reference spring-framework and spring-authorization-server Makefile patterns. **Depends on: Task 2.1 Output**

- Create `Makefile` with `.PHONY` declarations and `help` target displaying available commands
- Define core targets: `clean` (gradlew clean), `build-thin` (build -x test -x checkstyleMain -x checkstyleTest -x asciidoctor -x javadoc), `install` (clean publishToMavenLocal skipping tests/docs), `deploy` (clean publish skipping tests/docs)
- Add auxiliary targets: `build` (full build), `stop` (stop Gradle Daemon), `projects` (list projects)

## Phase 3: Dependency Upgrade

### Task 3.1 – Analyze Dependencies and Determine Upgrade Targets - Agent_DepUpgrade
**Objective:** Analyze current dependency versions and create an upgrade plan respecting Java 8 and Spring Boot 2.x compatibility.
**Output:** Dependency upgrade plan with target versions and compatibility notes.
**Guidance:** Focus on core dependencies: Spring, Spring Boot, Jackson, Swagger Core, Groovy, Spock, etc. in `gradle/dependencies.gradle`. **Depends on: Task 2.1 Output by Agent_BuildConfig (build system must be functional)**

1. Ad-Hoc Delegation – Dependency version compatibility research (ref: apm-7-delegate-research.md)
2. Read `gradle/dependencies.gradle`, list all current dependencies and versions, identify upgradeable items (focus: Spring, Spring Boot, Jackson, Swagger Core and other critical libraries)
3. Create upgrade plan: determine target versions ensuring Java 8 and Spring Boot 2.x compatibility, document upgrade decisions and rationale

### Task 3.2 – Execute Dependency Upgrades - Agent_DepUpgrade ✅ COMPLETED
**Objective:** Apply dependency version upgrades according to the plan from Task 3.1.
**Output:** Modified `gradle/dependencies.gradle` with upgraded versions, passing build.
**Status:** Completed. 13 core modules compile+test pass on Java 8. HATEOAS 1.5 API fixes (2 test files) and sourceSets.main.output build fix (6 build.gradle) applied. Excluded: swagger-ui (Node.js 403), spring-integration* (Nexus unreachable).
**Guidance:** Incremental upgrade approach — compile first, then test. **Depends on: Task 3.1 Output**

1. Modify `gradle/dependencies.gradle` version numbers according to Task 3.1 upgrade plan
2. Run `./gradlew build -x test` to verify compilation passes with upgraded dependencies
3. Run `./gradlew test` to verify tests pass (if failures occur, analyze and fix compatibility issues)

### Task 3.3 – Vulnerability Scan and Listing - Agent_DepUpgrade ✅ COMPLETED
**Objective:** Scan dependencies and code for known vulnerabilities, produce vulnerability report.
**Output:** Vulnerability inventory (CVE IDs, affected components, severity, fix status) for documentation.
**Status:** Completed. 28 CVEs identified: 17 mitigated by Task 3.2 upgrades, 11 still present (Spring 5.3.x/Boot 2.7.x EOL constraint). Key remaining: CVE-2022-1471 (SnakeYAML RCE), WS-2022-0468 (jackson-core DoS).
**Guidance:** Code vulnerabilities are NOT to be fixed, only listed. Focus on identifying which vulnerabilities were mitigated by upgrades vs. still present. **Depends on: Task 3.2 Output**

1. Use `./gradlew dependencyUpdates` or manual analysis to identify known CVEs in dependencies
2. Document which vulnerabilities were mitigated by dependency upgrades in Task 3.2 and which remain
3. Produce vulnerability inventory (CVE ID, affected component, severity, fix status) for Phase 4 documentation

### Task 3.4 – Additional Dependency Upgrades (SnakeYAML 2.x + Jackson 2.21.0) - Agent_DepUpgrade ✅ COMPLETED
**Objective:** Upgrade snakeyaml to latest 2.x version and jackson to 2.21.0 to resolve CVE-2022-1471 and WS-2022-0468.
**Output:** Modified `gradle/dependencies.gradle` with upgraded versions, passing build. Documented any API breakages and fixes.
**Status:** Completed. SnakeYAML 1.33→2.6, Jackson 2.13.5→2.21.0. 5 test files fixed (PropertyNamingStrategies migration, introspect() fix). Zero production code changes. All 12 testable modules pass on Java 8.
**Guidance:** HIGH RISK — SnakeYAML 2.x has breaking API changes (package rename `org.yaml.snakeyaml` → new API surface, `Constructor` class removal). Jackson 2.21.0 is far ahead of Spring Boot 2.7.18 managed BOM (2.13.x). Must verify compatibility with Spring Boot 2.7.18 and Java 8. **Depends on: Task 3.2, Task 3.3 Output**

1. Research SnakeYAML 2.x latest stable version and API migration guide
2. Research Jackson 2.21.0 compatibility with Spring Boot 2.7.18 and Java 8
3. Apply version changes to `gradle/dependencies.gradle`
4. Compile all modules — fix any API breakages
5. Run tests on all 13 core modules — fix any failures
6. Document all changes and compatibility findings in Memory Log

## Phase 4: Documentation & Validation

### Task 4.1 – Create Change Documentation - Agent_Documentation ✅ COMPLETED
**Objective:** Create comprehensive documentation of all changes made in this project.
**Output:** Three Markdown documents in `doc/` directory.
**Status:** Completed. doc/CHANGES.md (构建系统), doc/DEPENDENCY_UPGRADE.md (依赖升级), doc/VULNERABILITY_REPORT.md (漏洞报告) 已创建。
**Guidance:** Project has existing `docs/` directory for AsciiDoc; create separate `doc/` for this task's records. **Depends on: Task 3.2, Task 3.3, Task 3.4 Output by Agent_DepUpgrade**

1. Create `doc/` directory in project root (separate from existing `docs/` AsciiDoc directory)
2. Create `doc/CHANGES.md`: document build system changes (group customization, wrapper localization, Nexus repository, Maven publishing, Makefile, version management) with usage instructions and configuration guide
3. Create `doc/DEPENDENCY_UPGRADE.md`: document dependency upgrade details (component name, old version, new version, upgrade rationale), referencing Task 3.1/3.2 output
4. Create `doc/VULNERABILITY_REPORT.md`: document fixed vulnerabilities and remaining vulnerabilities, include placeholder "Code Vulnerabilities" section (to be filled later), referencing Task 3.3 output

### Task 4.2 – End-to-End Validation - Agent_BuildConfig ✅ COMPLETED (PASS WITH EXCEPTIONS)
**Objective:** Verify all changes work correctly through end-to-end testing of build, install, and deploy workflows.
**Output:** Validated build system with all Makefile targets functioning correctly.
**Status:** PASS WITH EXCEPTIONS. Build (15/19 modules) PASS, Install (11 modules) PASS, Deploy FAIL (Nexus URL selection bug), Documentation PASS. Three issues identified for follow-up.
**Guidance:** Ensure no existing functionality is broken. **Depends on: Task 4.1 Output by Agent_Documentation**

1. Run `make clean` and `make build-thin` to verify slim build passes
2. Run `make install` to verify local Maven installation succeeds, check artifact groupId is `libiao.test.io.springfox` in local repository
3. Run `make deploy` to verify Nexus publishing (requires Nexus reachability), confirm SNAPSHOT version published to nexusSnapshotUrl
4. Verify documentation completeness: check all three documents in `doc/` directory are complete and well-formatted

## Phase 5: Bugfix (Post-Validation)

### Task 5.1 – Fix Nexus Publish URL Selection - Agent_BuildConfig
**Objective:** Fix the Nexus publish URL selection so SNAPSHOT versions are published to the snapshots repository, not the releases repository.
**Output:** Modified `build.gradle` with lazy evaluation for publish URL.
**Guidance:** The `version` variable at `build.gradle:57` is evaluated at configuration time before `.version` file is read. Change to lazy evaluation using `project.provider { ... }` or move version reading earlier. **Depends on: Task 4.2 findings**

1. Read `build.gradle` to understand the version loading and publishing block ordering
2. Fix URL selection to use lazy evaluation: e.g., `url = project.provider { version.toString().endsWith('-SNAPSHOT') ? nexusSnapshotUrl : nexusReleaseUrl }`
3. Verify with `./gradlew publish --dry-run` or inspect resolved URL

### Task 5.2 – Fix springfox-data-rest GroupId Override - Agent_BuildConfig
**Objective:** Remove the hardcoded `group 'io.springfox'` in springfox-data-rest so it uses the project-wide `projectGroup`.
**Output:** Modified `springfox-data-rest/build.gradle`.
**Guidance:** Line 1 of `springfox-data-rest/build.gradle` has `group 'io.springfox'` which bypasses the root subprojects `group = projectGroup`. Remove or change to `group = projectGroup`. **Depends on: Task 4.2 findings**

1. Remove `group 'io.springfox'` from `springfox-data-rest/build.gradle` line 1
2. Verify `./gradlew :springfox-data-rest:properties | grep group` outputs `libiao.test.io.springfox`

### Task 5.3 – Fix springfox-spring-integration ClassGraph API Break - Agent_BuildConfig
**Objective:** Fix the compilation failure in springfox-spring-integration caused by ClassGraph 4.8.184 removing `io.github.classgraph.utils.ReflectionUtils`.
**Output:** Modified `SpringIntegrationParametersProvider.java` with replacement reflection code.
**Guidance:** ClassGraph removed `io.github.classgraph.utils.ReflectionUtils` in a later version. Replace with standard Java reflection (`java.lang.reflect.Field.get()`). The 3 usages are `ReflectionUtils.getFieldVal(object, fieldName, false)` — replace with a local helper using `Field.setAccessible(true)` + `Field.get()`. **Depends on: Task 4.2 findings**

1. Read `SpringIntegrationParametersProvider.java` to understand all `ReflectionUtils` usages
2. Replace with standard Java reflection or a local utility method
3. Compile springfox-spring-integration module
4. Run tests if possible (may need Nexus for spring-restdocs-mockmvc)

### Task 5.4 – Fix springfox-swagger-ui Node.js Build - Agent_BuildConfig
**Objective:** Fix the springfox-swagger-ui build so it can compile and publish to local Maven.
**Output:** Modified `springfox-swagger-ui/build.gradle` with working Node.js configuration.
**Guidance:** Node.js 8.12.0 download from nodejs.org returns 403 (old version no longer hosted). Options: (a) upgrade Node.js version to one still available on nodejs.org, (b) use unofficial-builds.nodejs.org mirror, (c) use local pre-cached Node.js. Prefer option (a) — find a Node.js version compatible with the existing package.json/npm scripts. **Depends on: Task 4.2 findings**

1. Read `springfox-swagger-ui/build.gradle` and `springfox-swagger-ui/src/web/package.json` to understand Node.js requirements
2. Determine a compatible Node.js version still available for download
3. Update `node.version` and `node.npmVersion` in build.gradle
4. Optionally update `node.distBaseUrl` if using a mirror
5. Verify `./gradlew :springfox-swagger-ui:jar` succeeds
6. Verify `./gradlew :springfox-swagger-ui:publishToMavenLocal` succeeds
