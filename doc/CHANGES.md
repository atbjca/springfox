# Springfox Build System Changes

**Branch:** `2.10.5-bjca-patch`
**Version:** `2.10.5-bjca-patch-SNAPSHOT`
**Date:** 2026-03-18

This document describes all build system modifications made to the springfox project to support private repository publishing, custom artifact group, local Gradle wrapper, Makefile automation, and version management.

---

## Table of Contents

1. [Quick Start](#quick-start)
2. [Custom Group Property](#custom-group-property)
3. [Local Gradle Wrapper](#local-gradle-wrapper)
4. [Nexus Repository for Dependency Resolution](#nexus-repository-for-dependency-resolution)
5. [Maven Publishing with Nexus](#maven-publishing-with-nexus)
6. [Version Management](#version-management)
7. [Build Configuration Fixes](#build-configuration-fixes)
8. [Makefile Automation](#makefile-automation)
9. [Legacy Plugin Cleanup](#legacy-plugin-cleanup)
10. [Configuration Reference](#configuration-reference)

---

## Quick Start

### Prerequisites

- **Java 8** (required; Java 11+ will fail due to JAXB removal)
- Gradle wrapper is included (no separate Gradle install needed)
- Nexus credentials configured in `~/.gradle/gradle.properties` (optional; build works without Nexus)

### Common Commands

```bash
# Compile all modules (skip tests, checkstyle, javadoc)
make build-thin

# Install to local Maven repository (~/.m2/repository)
make install

# Deploy to Nexus private repository
make deploy

# Full build with tests
make build

# List all subprojects
make projects

# View dependency tree
make deps

# Stop Gradle daemons
make stop

# Show available commands
make help
```

### Without Nexus (fallback to Maven Central)

If the Nexus server is not reachable, override the public URL:

```bash
./gradlew build -x test -x checkstyleMain -x checkstyleTest -x javadoc \
  -PnexusPublicUrl=https://repo1.maven.org/maven2/
```

---

## Custom Group Property

**Files modified:** `gradle.properties`, `build.gradle`

The artifact group ID was changed from the hardcoded `io.springfox` to a configurable property `libiao.test.io.springfox`.

### How It Works

- `gradle.properties` defines `projectGroup=libiao.test.io.springfox`
- `build.gradle` subprojects block uses `group = projectGroup`
- Gradle automatically exposes properties from `gradle.properties` as project-level properties

### Changing the Group

Edit the single property in `gradle.properties`:

```properties
projectGroup=your.custom.group
```

All subprojects inherit this value automatically.

### Published Artifact Coordinates

After this change, artifacts are published as:

```
libiao.test.io.springfox:springfox-core:2.10.5-bjca-patch-SNAPSHOT
libiao.test.io.springfox:springfox-spi:2.10.5-bjca-patch-SNAPSHOT
libiao.test.io.springfox:springfox-schema:2.10.5-bjca-patch-SNAPSHOT
... (all publishable modules)
```

---

## Local Gradle Wrapper

**Files modified:** `gradle/wrapper/gradle-wrapper.properties`

The Gradle wrapper was configured to use a local distribution file instead of downloading from the internet.

### Configuration

```properties
distributionUrl=file\:///Volumes/LIBIAO_EX/dev/gradle-5.2-bin.zip
```

### Notes

- **Gradle version:** 5.2 (unchanged from original project)
- The local path `/Volumes/LIBIAO_EX/dev/` is an external drive mount. If the drive is not mounted, Gradle commands will fail.
- Gradle 7.x/8.x are incompatible because the project uses the `compile` configuration which was removed in Gradle 7.
- The zip was re-created from the Gradle wrapper cache and is functionally identical to the official distribution.

### Reverting to Internet Download

To revert to internet-based distribution:

```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-5.2-bin.zip
```

---

## Nexus Repository for Dependency Resolution

**Files modified:** `settings.gradle`, `build.gradle`, `buildSrc/build.gradle`

A Nexus private repository was added as the primary dependency resolution source across all build configurations.

### Repository Resolution Order

**`settings.gradle` (pluginManagement):**
1. mavenLocal
2. Nexus public (conditional)
3. Nexus snapshots (conditional)
4. Gradle Plugin Portal
5. Maven Central

**`build.gradle` buildscript.repositories:**
1. mavenLocal
2. Gradle Plugin Portal
3. Maven Central
4. Nexus public (conditional)
5. jcenter (last fallback)

**`build.gradle` subprojects.repositories:**
1. mavenLocal
2. Nexus public (conditional, with credentials)
3. Nexus snapshots (conditional, for `-SNAPSHOT` versions)
4. Maven Central
5. jcenter (last fallback)

**`buildSrc/build.gradle`:**
1. mavenLocal
2. Nexus public (conditional)
3. Maven Central
4. jcenter (last fallback)

### Conditional Configuration

All Nexus repository blocks are wrapped in `hasProperty()` checks, so the build remains fully functional without Nexus credentials:

```groovy
if (project.hasProperty('nexusPublicUrl')) {
  maven {
    url nexusPublicUrl
    credentials {
      username nexusUsername
      password nexusPassword
    }
  }
}
```

### Known Issue

The Nexus `maven-public` proxy has incomplete cache for some Maven Central artifacts (POM present, JAR missing for `gpars:1.2.1` and `multiverse-core:0.7.0`). For this reason, `buildscript.repositories` places Maven Central before Nexus.

---

## Maven Publishing with Nexus

**Files modified:** `gradle/publishing.gradle`, `build.gradle`, `buildSrc/src/main/groovy/springfox/gradlebuild/utils/ProjectDefinitions.groovy`

The existing Bintray/Artifactory publishing configuration was replaced with `maven-publish` + Nexus private repository.

### What Was Removed

- `com.jfrog.bintray` plugin and `bintray {}` block
- `com.jfrog.artifactory` plugin and `artifactory {}` block
- All Bintray-specific ext variables (`bintrayUser`, `bintrayApiKey`, `passphrase`, etc.)
- `springfox-multi-release` plugin import

### What Was Added

A global Nexus publishing repository in `build.gradle` that auto-selects snapshot vs. release URL:

```groovy
allprojects {
  plugins.withType(MavenPublishPlugin) {
    publishing {
      repositories {
        if (project.hasProperty('nexusReleaseUrl')) {
          maven {
            url = version.toString().endsWith('-SNAPSHOT')
                ? nexusSnapshotUrl : nexusReleaseUrl
            credentials {
              username nexusUsername
              password nexusPassword
            }
          }
        }
      }
    }
  }
}
```

### Published Artifacts

Each publishable module produces:
- Main JAR
- Sources JAR (`-sources.jar`)
- Javadoc JAR (`-javadoc.jar`)
- POM with correct dependency scopes

### Excluded from Publishing

The following modules are excluded via `ProjectDefinitions.groovy`:
- `swagger-contract-tests`
- `swagger-contract-tests-webflux`
- `buildSrc`
- `springfox-spring-config`
- `springfox-petstore`
- `springfox-petstore-webflux`

---

## Version Management

**Files modified:** `.version`, `build.gradle`

### How It Works

The version is managed through a single `.version` file in the project root:

```
2.10.5-bjca-patch-SNAPSHOT
```

The root `build.gradle` reads this file directly:

```groovy
version = file(".version").text.trim()
```

Subprojects inherit the version via:

```groovy
version = project.rootProject.version
```

### Changing the Version

Edit the `.version` file. The `-SNAPSHOT` suffix controls:
- Which Nexus repository is used (snapshot vs. release)
- Whether the Nexus snapshot repository is added to dependency resolution

### Background

The original `springfox-multi-release` plugin (which managed versions via `FileVersionStrategy`) was disabled because it depends on `grgit`, which was only available from jcenter (now defunct). Direct file reading is the replacement.

---

## Build Configuration Fixes

### sourceSets.main.output to Project Dependency

**Files modified:** 6 module `build.gradle` files

The pattern `project(':springfox-schema').sourceSets.main.output` was replaced with `project(':springfox-schema')` in the following modules:

- `springfox-spring-web/build.gradle`
- `springfox-spring-webmvc/build.gradle`
- `springfox-spring-webflux/build.gradle`
- `springfox-spring-integration/build.gradle`
- `springfox-spring-integration-webmvc/build.gradle`
- `springfox-spring-integration-webflux/build.gradle`

**Reason:** The `sourceSets.main.output` pattern creates a direct file dependency that does not carry transitive dependencies (classmate, slf4j, spring). This caused 100+ compilation errors in downstream test tasks. Using a proper `project()` dependency resolves the transitive dependency graph correctly.

---

## Makefile Automation

**File created:** `Makefile`

### Available Targets

| Target | Description | Gradle Command |
|--------|-------------|----------------|
| `help` | Display available commands (default) | -- |
| `clean` | Clean build artifacts | `./gradlew clean` |
| `build` | Full build with tests | `./gradlew build` |
| `build-thin` | Build without tests, checkstyle, javadoc | `./gradlew build -x test -x checkstyleMain -x checkstyleTest -x javadoc` |
| `install` | Install to local Maven repository | `./gradlew clean publishToMavenLocal -x test -x checkstyleMain -x checkstyleTest -x javadoc` |
| `deploy` | Publish to Nexus | `./gradlew clean publish -x test -x checkstyleMain -x checkstyleTest -x javadoc` |
| `stop` | Stop Gradle daemons | `./gradlew --stop` |
| `projects` | List all subprojects | `./gradlew projects` |
| `deps` | View dependency tree | `./gradlew dependencies --configuration compile` |

---

## Legacy Plugin Cleanup

The following legacy plugins were removed from `buildscript.dependencies` because they were only available from jcenter (now defunct):

| Plugin | Version | Reason for Removal |
|--------|---------|-------------------|
| `coveralls-gradle-plugin` | -- | jcenter-exclusive |
| `http-builder` | -- | jcenter-exclusive |
| `gradle-bintray-plugin` | -- | jcenter-exclusive, Bintray service shut down |
| `build-info-extractor-gradle` | 4.9.3 | jcenter-exclusive |
| `asciidoctor-gradle-plugin` | -- | jcenter-exclusive |
| `grgit-core` | -- | jcenter-exclusive |
| `gradle-git-publish` | -- | jcenter-exclusive |

**Kept:**
- `gradle-jvmsrc-plugin:0.6.1` (used by subprojects)
- `gradle-versions-plugin:0.20.0` (needed for dependency analysis)

**Disabled apply statements:**
- `apply plugin: 'springfox-multi-release'` (depends on grgit)
- `apply from: "$rootDir/gradle/documentation.gradle"` (depends on asciidoctor + git-publish)
- `wrapper { gradleVersion = '4.10.1' }` (incorrect version; actual is 5.2)

---

## Configuration Reference

### `gradle.properties` (project root)

```properties
org.gradle.jvmargs=-Xms256m -Xmx3072m -XX:+HeapDumpOnOutOfMemoryError
projectGroup=libiao.test.io.springfox
```

### `~/.gradle/gradle.properties` (user home, not committed)

These properties are required for Nexus integration. The build works without them (falls back to Maven Central):

```properties
# Nexus Repository URLs
nexusPublicUrl=http://your-nexus-host:port/repository/maven-public/
nexusSnapshotUrl=http://your-nexus-host:port/repository/maven-snapshots/
nexusReleaseUrl=http://your-nexus-host:port/repository/maven-releases/

# Nexus Credentials
nexusUsername=your-username
nexusPassword=your-password
```

### `.version`

```
2.10.5-bjca-patch-SNAPSHOT
```

Edit this file to change the project version. Remove `-SNAPSHOT` for release builds.

### `gradle/wrapper/gradle-wrapper.properties`

```properties
distributionUrl=file\:///Volumes/LIBIAO_EX/dev/gradle-5.2-bin.zip
```

### Note on `allowInsecureProtocol`

The `allowInsecureProtocol = true` setting was intentionally omitted because Gradle 5.2 does not support it (it was introduced in Gradle 6.6). HTTP repositories are allowed by default in Gradle 5.2. If the project is ever upgraded to Gradle 6.6+, this setting must be added to all HTTP Nexus repository blocks.
