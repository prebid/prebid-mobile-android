---
name: gradle
description: Build the SDK modules and run unit tests — full suite, one module, or a single test class — with this repo's Gradle setup, JDK expectations, and the failures worth knowing about in advance.
---

# Building and testing prebid-mobile-android

Requires **JDK 17** (CI uses temurin 17; a newer LTS works for Gradle 8.13, but Java 25+ does
not) and the Android SDK (`local.properties` with `sdk.dir`, or `ANDROID_HOME`).

## Unit tests

```bash
# All library modules — what PR CI runs (scripts/testPrebidMobile.sh does exactly this)
scripts/testPrebidMobile.sh

# One module
./gradlew PrebidMobile-core:testReleaseUnitTest

# One class (repeatable without re-running the world)
./gradlew PrebidMobile-core:testReleaseUnitTest --tests "org.prebid.mobile.TargetingParamsTest"

# One method
./gradlew PrebidMobile-core:testReleaseUnitTest --tests "*TargetingParamsTest.testGetUserKeywords"
```

Reports land in `PrebidMobile/<module>/build/reports/tests/`.

- Tests are Robolectric-heavy: the first run downloads a Robolectric SDK jar (needs network),
  and `--add-opens=java.base/java.lang=ALL-UNNAMED` is already configured in the module build
  files — don't remove it.
- `test-utils` is shared infrastructure: after touching it, run every module's tests.
- Never mark a test `@Ignore` to get a run green — the `skiplist-ratchet` guard fails the build
  on any new skip, by name.

## Builds

```bash
# Release frameworks for all five artifacts, no fat JAR (CI's build job)
scripts/buildPrebidMobile.sh -nojar

# A single module's AAR
./gradlew PrebidMobile-core:assembleRelease
```

## API signature generation (Metalava)

```bash
# Regenerate the committed public-API baseline in api/
./scripts/guards/run-guards.sh --update-api-baseline

# The same task directly, into a scratch directory
./gradlew generateApiSignatures -PmetalavaOutputDir=/tmp/api
```

Generation is deterministic — a diff in `api/` on an untouched tree means something is wrong
(wrong JDK, modified metalava.gradle); investigate rather than commit it.

## Failures worth knowing about

- `Unsupported class file major version …` from Gradle → your default JVM is too new for
  Gradle 8.13's Groovy; point `JAVA_HOME` at a JDK 17–21.
- Metalava task fails with API-lint errors → only ever for NEW issue ids; the known
  pre-existing ones are already `--hide`-listed in `metalava.gradle` with the reason.
- Lint tasks are wired but non-blocking (`abortOnError false`); the blocking gate is
  `./scripts/lint/run-lint.sh`, which is diff-scoped — see `docs/lint/README.md`.
