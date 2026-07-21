## Context

The Wrapper currently references `file:///Volumes/LIBIAO_HY/dev/gradle-5.2-bin.zip`, which is available only on one workstation. The build remains tied to Gradle 5.2 because 23 Gradle scripts still use legacy configurations such as `compile`, `testCompile`, and `testRuntime` that were removed in later Gradle releases.

## Goals / Non-Goals

**Goals:**

- Make Wrapper bootstrap portable across developer machines and CI agents.
- Preserve the existing Gradle 5.2 compatibility baseline.
- Continue using Gradle Wrapper's standard distribution cache.

**Non-Goals:**

- Upgrade Gradle or modernize build scripts and plugins.
- Change dependency resolution, application code, or publication behavior.
- Bundle a Gradle distribution in the repository.

## Decisions

1. Use `https://services.gradle.org/distributions/gradle-5.2-bin.zip`. This is the official distribution corresponding to the project's current Wrapper version and removes the workstation-specific path.
   - Alternative: keep the local file URL. Rejected because it is not portable.
   - Alternative: switch directly to Gradle 8.11.1. Rejected because it requires a broader build migration and is not a safe URL-only change.
2. Change only `distributionUrl`. Existing Wrapper scripts, the Wrapper JAR, and Gradle dependency caches remain unchanged.
3. Validate bootstrap with `./gradlew --version`. Run project configuration under Java 8 because Gradle 5.2's embedded Groovy is incompatible with the current Java 17 shell environment.

## Risks / Trade-offs

- [First run requires external network access when the official URL is not cached] → CI and developer environments must allow access to `services.gradle.org` or pre-populate the standard Wrapper cache.
- [Gradle 5.2 is old] → Keep this change narrowly scoped and handle a Gradle upgrade through a separate OpenSpec change.
- [The local-file cache uses a different URL hash and is not reused] → The current workstation already has a valid cache entry for the official HTTPS URL.

## Migration Plan

1. Replace the local `distributionUrl` with the official Gradle 5.2 HTTPS URL.
2. Verify that the Wrapper reports Gradle 5.2 without downloading when the official URL cache exists.
3. Verify project configuration under Java 8.

Rollback consists of restoring the previous `distributionUrl`, although doing so reintroduces the portability problem.

## Open Questions

None.
