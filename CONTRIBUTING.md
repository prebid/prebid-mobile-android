# Contributing to Prebid Mobile Android

Thank you for contributing! This document covers how to build, test, and submit changes.

## Repository layout

| Path | What it is |
|---|---|
| `PrebidMobile/PrebidMobile-core/` | Core SDK (Java) |
| `PrebidMobile/PrebidMobile-gamEventHandlers/` | Google Ad Manager event handlers |
| `PrebidMobile/PrebidMobile-admobAdapters/` | AdMob mediation adapters |
| `PrebidMobile/PrebidMobile-maxAdapters/` | AppLovin MAX mediation adapters |
| `PrebidMobile/PrebidMobile-nextGenEventHandlers/` | Next-gen GAM event handlers (Kotlin) |
| `Example/` | Demo apps and the internal test app |
| `api/` | Committed public-API signature files (Metalava) |
| `scripts/` | Build, test, and quality scripts used locally and in CI |

## Building and testing

Requires JDK 17 and the Android SDK.

```bash
# Unit tests for all library modules (what CI runs on every PR)
scripts/testPrebidMobile.sh

# One module
./gradlew PrebidMobile-core:testReleaseUnitTest

# Build the frameworks
scripts/buildPrebidMobile.sh -nojar
```

## Quality checks

`./scripts/guards/run-guards.sh` runs fast, deterministic structural checks (public-API
baselining, manifest-surface freezing, logging hygiene, test-presence rules, and more). They run
on every PR in CI and take seconds locally. See [`docs/guards/README.md`](docs/guards/README.md)
for what each guard enforces and what to do when one fails.

Optionally, run them automatically before every push (one-time, per clone):

```bash
git config core.hooksPath .githooks
```

The hook runs the same checks CI runs — failing locally just fails faster; bypass a genuine
emergency with `git push --no-verify` (CI still catches it).

Android Lint runs diff-scoped and blocking on every PR: it fails on findings in the lines your
branch adds, and never asks you to fix code you didn't write
([`docs/lint/README.md`](docs/lint/README.md)). Run it locally with `./scripts/lint/run-lint.sh`.

## Pull requests

- **PR titles become release notes.** Write a clear, user-facing title
  (e.g. `Fix: interstitial controls misplaced after orientation changes`).
- Include tests for behavior changes.
- Changes to the public API must regenerate the baseline (`api/*.txt`) in the same PR
  (`./scripts/guards/run-guards.sh --update-api-baseline`) — this makes API changes an explicit,
  reviewable decision. New public API arrives documented and tested (guarded).
- Changes to the merged manifest surface (permissions, exported components, queries) must update
  the manifest baseline in the same PR — every publisher inherits that surface.
- Changes to OpenRTB request building should cite the relevant spec section in the PR
  description.
- For significant features, open an "intent to implement" issue first.

## AI-assisted development (optional)

The repository ships configuration for AI coding agents (`AGENTS.md`, `CLAUDE.md`, `agents/`).
Using an AI assistant is entirely optional — nothing in the contribution process requires one. CI
enforces quality through the deterministic guards above, which apply equally to everyone.
