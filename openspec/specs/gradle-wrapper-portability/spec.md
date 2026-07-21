# gradle-wrapper-portability Specification

## Purpose
Ensure the project can bootstrap its compatible Gradle version on developer machines and CI agents without relying on workstation-specific filesystem paths.
## Requirements
### Requirement: Portable Gradle distribution source
The Gradle Wrapper configuration SHALL reference the official Gradle distribution service over HTTPS and SHALL NOT depend on an absolute local filesystem path.

#### Scenario: Wrapper runs on a machine without the original local path
- **WHEN** a developer or CI agent runs the project Wrapper on a machine that does not contain `/Volumes/LIBIAO_HY/dev/gradle-5.2-bin.zip`
- **THEN** the Wrapper resolves its distribution from `https://services.gradle.org/distributions/gradle-5.2-bin.zip`

### Requirement: Preserve the compatible Gradle baseline
The Gradle Wrapper configuration SHALL continue to select Gradle 5.2 until the legacy build configurations and plugins are migrated in a separate change.

#### Scenario: Wrapper version is inspected
- **WHEN** a user runs `./gradlew --version`
- **THEN** the Wrapper reports Gradle 5.2

### Requirement: Reuse the standard Wrapper cache
The project SHALL rely on the standard `GRADLE_USER_HOME/wrapper/dists` cache for the official distribution URL.

#### Scenario: Official distribution is already cached
- **WHEN** the official Gradle 5.2 distribution has a valid Wrapper cache marker
- **THEN** the Wrapper starts without downloading the distribution again
