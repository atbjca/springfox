## Why

The Gradle Wrapper distribution URL is hard-coded to an absolute path on one developer machine, so the wrapper cannot bootstrap on CI or other workstations. The project must use a portable distribution source without changing its Gradle 5.2 compatibility baseline.

## What Changes

- Replace the machine-specific `file:///` Gradle distribution URL with the official Gradle 5.2 HTTPS distribution URL.
- Preserve Gradle 5.2 because the current build scripts still depend on configurations removed in Gradle 7 and later.
- Verify that the wrapper resolves Gradle 5.2 through the normal Wrapper cache mechanism.

## Capabilities

### New Capabilities

- `gradle-wrapper-portability`: Defines portable and version-compatible Gradle Wrapper bootstrap behavior.

### Modified Capabilities

None.

## Impact

- Affects `gradle/wrapper/gradle-wrapper.properties` only.
- Does not change application APIs, published artifacts, or dependency versions.
- Machines without an existing Wrapper distribution cache require network access to `services.gradle.org` on first use.
