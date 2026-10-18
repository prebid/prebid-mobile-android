# AGENTS.md

Canonical instructions for AI coding agents working in this repository. Codex, Cursor, and Copilot
read this file directly; Claude Code reaches it via the `@AGENTS.md` import in `CLAUDE.md`.
Human contributors: see [CONTRIBUTING.md](CONTRIBUTING.md); nothing in the contribution process
requires an AI tool.

The philosophy here: rules are machine-enforced wherever possible. Most "musts" below point at a
structural guard (`./scripts/guards/run-guards.sh`, enforced in CI) — follow the rule because the
build fails otherwise, and when you find a recurring violation class that isn't guarded yet,
propose a new guard rather than relying on review vigilance.

## Overview

Prebid Mobile Android SDK — an open-source header bidding SDK that integrates with Prebid Server
to increase ad yield. Java core (Kotlin only in the next-gen event handlers), minSdk 19,
distributed via Maven Central as five artifacts.

## Agent runbooks (`agents/`)

Detailed procedures live in `agents/` as plain markdown, readable by any agent. Read the relevant
file before acting rather than improvising the commands.

| Task | Read |
|------|------|
| Build, run unit tests, run a single test class | `agents/gradle/SKILL.md` |
| Review a PR on this repo | `agents/sdk-review/SKILL.md` |
| Author a new structural guard | `agents/guard/SKILL.md` |
| Audit ORTB models against the OpenRTB spec | `agents/verify-spec/SKILL.md` |

## Repo map

| Path | Role |
|---|---|
| `PrebidMobile/PrebidMobile-core/` | Core SDK (Java). API layer in `org.prebid.mobile` + `org.prebid.mobile.api.*`; rendering engine under `org.prebid.mobile.rendering.*` |
| `…/rendering/models/openrtb/` | OpenRTB request models — **spec-grounding gate applies** |
| `PrebidMobile/PrebidMobile-gamEventHandlers/` | Google Ad Manager event handlers |
| `PrebidMobile/PrebidMobile-admobAdapters/` | AdMob mediation adapters |
| `PrebidMobile/PrebidMobile-maxAdapters/` | AppLovin MAX mediation adapters |
| `PrebidMobile/PrebidMobile-nextGenEventHandlers/` | Next-gen GAM handlers (Kotlin) |
| `PrebidMobile/omsdk-android/` | Bundled Open Measurement SDK artifact |
| `PrebidMobile/test-utils/` | Shared test infrastructure |
| `Example/` | Demo apps (`PrebidDemoJava`, `PrebidDemoKotlin`, `PrebidInternalTestApp`, `PrebidNextGenDemo`) |
| `api/` | Committed Metalava API signature files — the public-API lockfile |
| `scripts/guards/` | Structural guards (engine, checks, allowlists, baselines) |
| `scripts/lint/` | Diff-scoped Android Lint gate |
| `docs/guards/`, `docs/lint/` | What each check enforces and why |

## Hard rules (each is guarded — the build fails, don't argue with it)

1. **No new public API without a baseline update.** Any visibility widening or new
   public declaration requires regenerating `api/*.txt` in the same PR
   (`./scripts/guards/run-guards.sh --update-api-baseline`) and calling it out — the PR title
   becomes a release-note line (guard: `public-api-baseline`). New public API also arrives
   **documented** (guard: `api-doc-coverage`) and **tested** (guard: `api-test-presence`).
2. **All console output goes through `LogUtil`.** No raw `android.util.Log` calls,
   `System.out/err`, or `printStackTrace()` — publishers can't silence those
   (guard: `logging-hygiene`).
3. **The manifest surface is frozen.** Permissions, features, queries, exported components:
   any change is an explicit baseline diff (guard: `manifest-surface`).
4. **Listener callbacks to publishers are delivered on the main thread** — wrap in
   `TasksManager.getInstance().executeOnMainThread(...)` (guard: `ast-rule-ratchet` /
   `main-thread-callbacks`; same ratchet holds `empty-catch`, `kotlin-not-null-assert`, and
   `static-context-leak` counts).
5. **No new FIXME/TODO in core src/main** — fix it or file an issue (guard: `fixme-ratchet`).
6. **Deprecations name their replacement** — Javadoc `@deprecated` tag / Kotlin message
   (guard: `deprecation-hygiene`).

## Test-integrity policy (zero tolerance)

- Never delete, weaken, or skip a failing test to make a run pass. Fix the code, or report the
  failure as a blocker with the output.
- The skipped-test set (`@Ignore`, Gradle excludes) may only shrink — never add entries to make
  CI pass (guard: `skiplist-ratchet`).
- `test-utils` is shared infrastructure — after touching it, run every module's unit tests, not
  just the one you changed.
- Never report success you didn't verify. If the environment can't run a check, say SKIPPED and
  why — a skipped check is honest; a fabricated pass is not.

## Spec-grounding gate (OpenRTB)

Changes to `rendering/models/openrtb/` models or bid-request building must cite the authoritative
source **before** the code changes:

- [IAB OpenRTB 2.x spec](https://github.com/InteractiveAdvertisingBureau/openrtb2.x) — cite the
  section, link a commit-pinned permalink in the PR description and in a code comment.
- Prebid Server's request semantics where OpenRTB is silent (`imp.ext.prebid`, etc.).

Other SDKs (prebid-mobile-**ios**, prebid.js) are cross-checks, **not** the authority. Uncited
protocol changes should be rejected in review.

## Environment: what can run where

- **Any OS**: the fast-tier guards (`python3`; [ast-grep](https://ast-grep.github.io) on PATH for
  the ast rules — a missing tool makes that guard exit 2 = SKIPPED, which is *not* a pass).
- **JDK 17 + Android SDK**: builds, unit tests, the Metalava API baseline, and the diff-scoped
  Lint runner. All run on ubuntu in CI; no macOS or device needed for any gate.

## Commands

### Guards and lint

```bash
# Structural guards (seconds — run before every commit)
./scripts/guards/run-guards.sh

# …or have git run them automatically before every push (opt-in, once per clone):
git config core.hooksPath .githooks

# Style/correctness lint — fails on violations in the lines this branch adds
./scripts/lint/run-lint.sh
```

When a guard fails legitimately, its failure message names the fix: ratchet wins and intentional
surface changes re-record a baseline (`./scripts/guards/run-guards.sh --update-<name>-baseline`;
the script header lists them all), and grandfathered exceptions live in
`scripts/guards/allowlists/*.json` — shrink-only, one entry per exception with its `reason`.
Commit baseline/allowlist diffs in the same PR and call them out. The full catalog is in
`docs/guards/README.md`.

### Build and test

```bash
# Unit tests, all library modules (what PR CI runs)
scripts/testPrebidMobile.sh

# One module
./gradlew PrebidMobile-core:testReleaseUnitTest

# One test class
./gradlew PrebidMobile-core:testReleaseUnitTest --tests "org.prebid.mobile.TargetingParamsTest"

# Build the release frameworks (no fat JAR)
scripts/buildPrebidMobile.sh -nojar

# Regenerate the API signature files
./scripts/guards/run-guards.sh --update-api-baseline
```

See `agents/gradle/SKILL.md` for the details (Robolectric config, JDK selection, common failures).

## Decision trees

**Adding a public API** → is it necessary, or can it stay package-private? If public: implement,
document (doc-coverage guard), test (test-presence guard), regenerate `api/`, mention the API
change in the PR title/description.

**Touching ORTB / request building** → find the spec section first (permalink), write/adjust the
test asserting the wire format, then change the model. Cite the permalink in a comment.

**Fixing a bug** → write the failing test first, fix, run guards + the module's unit tests; if
the fix touches `test-utils` or rendering, run all modules' tests.

**Adapter feature** → prefer the public API. Adapters currently import core internals
(`rendering.*`) — that is legacy reality, not license: don't deepen the reach, and if the core
lacks a hook, adding a public one (with baseline update) beats a new internal import.

## Commits and PRs

Commit subjects use release-note-quality language and reference issues (`Fix #998: …`). The PR
title becomes a release-note line; the PR template's checklist mirrors the guards. No
AI-attribution trailers on commits.

## Known traps

- Android Lint is **blocking but diff-scoped** — it fails on violations in lines a branch adds
  and never reports the lines it didn't touch. Don't "fix" the legacy backlog as a drive-by
  (`docs/lint/README.md`); `lintOptions { abortOnError false }` stays — the gate is the runner.
- The adapters legitimately import `org.prebid.mobile.rendering.*` today — there is no
  compile-time isolation seam in Java. See "Deliberately not ported" in `docs/guards/README.md`.
- `PrebidMobile/proguard-rules.pro` is **0 bytes** while `consumerProguardFiles` references it,
  and the core uses reflection — treat R8 behavior changes as untested territory.
- The Dokka `perPackageOption` suppression regex in `PrebidMobile/build.gradle` is the
  machine-readable "not public docs" boundary (`rendering`, `configuration`, `http`,
  `tasksmanager`, `addendum`, …). Keep it in sync when adding packages.
- Robolectric unit tests need `--add-opens=java.base/java.lang=ALL-UNNAMED` (already configured)
  and download an SDK jar on first run — first test run needs network.
- Metalava generation is deterministic; if `--update-api-baseline` produces a diff on an
  untouched tree, something is wrong — investigate, don't commit it.
