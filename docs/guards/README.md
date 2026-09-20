# Structural guards

Fast, deterministic architecture checks for prebid-mobile-android, ported from
[prebid-mobile-ios](https://github.com/prebid/prebid-mobile-ios) (same engine, Android-specific
probes). They run on every PR (`.github/workflows/guards.yml`, ubuntu, ~1 minute) and locally:

```bash
./scripts/guards/run-guards.sh
```

To run them automatically before every push (opt-in, one-time per clone):
`git config core.hooksPath .githooks` — the hook (`.githooks/pre-push`) runs the fast tier and
blocks the push on failure (`--no-verify` bypasses; CI still catches it).

Requirements: bash + git + python3. The `ast-rule-ratchet` guard additionally needs
[ast-grep](https://ast-grep.github.io/) (`brew install ast-grep`); `public-api-baseline`
additionally needs a JDK and the Android SDK (it runs a Gradle task). When a tool is missing
locally, that guard is skipped with a note — CI is authoritative, and in CI
(`GUARDS_REQUIRE_BUILD=1`) a could-not-run is a FAIL, never a skip.

## Two tiers

Everything Android-side that needs a classpath needs Gradle — the one structural difference from
the iOS original, which runs all its guards tool-free. So the guards split:

| Tier | Guards | Runs in | Cost |
|---|---|---|---|
| fast | everything below except `public-api-baseline` | `guards.yml` + the pre-push hook | seconds |
| build | `public-api-baseline` (Metalava), diff-scoped Lint | `lint.yml` | minutes |

`api-doc-coverage` and `api-test-presence` read the **committed** `api/*.txt` files, so they stay
in the fast tier even though their source of truth is build-generated.

## The ratchet

Guards enforce rules going **forward** without demanding the past be fixed first:

- Pre-existing violations are *grandfathered* in `scripts/guards/allowlists/<guard>.json`.
- Allowlists **only shrink**. A stale entry (the violation is gone) fails the run and must be
  deleted in the same PR — so the debt is monotonically decreasing.
- New violations fail immediately.
- Where sites are too numerous to enumerate, the same contract applies to a **count baseline**
  (`scripts/guards/baselines/`): growth fails, shrinkage must update the committed number in the
  same PR.

## Data format

Every committed allowlist and baseline is JSON with a `guard` field naming its owner and a
`description` stating its contract. The schema is validated on **every** read, so a malformed or
under-specified file fails the run instead of quietly reading as "no findings".

An allowlist entry carries its justification with it:

```json
{
  "guard": "logging-hygiene",
  "description": "Files with raw console output predating the LogUtil-facade rule…",
  "entries": [
    {
      "entry": "PrebidMobile/PrebidMobile-core/src/main/java/org/prebid/mobile/http/HTTPGet.java",
      "reason": "printStackTrace() predating the LogUtil-facade rule."
    }
  ]
}
```

Both fields are required and non-empty, and any other field is rejected — a typo (`resaon`) fails
loudly instead of silently dropping the justification. A stale-entry failure prints each entry's
`reason`, so the report says what the grant was for.

Baselines come in three shapes, all with the same `guard`/`description` header:

| Shape | Payload | Used by |
|---|---|---|
| count | `"count": 15` | `fixme-ratchet` |
| keyed count | `"counts": {"<key>": 3}` | `api-doc-coverage`, `string-dup-ratchet`, `ast-rule-ratchet` |
| entry list | `"entries": ["…"]` | `skiplist-ratchet`, `manifest-surface` |

`api/*.txt` (Metalava signature files) are their own richer lockfile — see `public-api-baseline`.

## Guards

### fixme-ratchet (blocking)

FIXME/TODO markers in `PrebidMobile-core/src/main` are deferred behavior decisions — several of
the current 15 (the ORTB 2.5 "auto detect?" questions in the openrtb models) shipped as behavior.
The count may only shrink. Growth fails (fix the marked issue, or file a GitHub issue and delete
the marker); shrinkage fails until `--update-fixme-baseline` re-records it in the same PR.

### logging-hygiene (blocking)

Publishers see the SDK's console output inside their apps — raw `android.util.Log` calls,
`System.out/err` printing, and `printStackTrace()` bypass the `LogUtil` facade's log level and
custom `PrebidLogger` routing, so publishers can't silence or redirect them. Use
`LogUtil.verbose/debug/info/warning/error`. `Log.getStackTraceString()` and the `Log.*` level
constants are pure formatting and stay allowed; `LogUtil.java` itself is structurally exempt.
15 violating files are grandfathered (shrink-only).

### ortb-test-presence (blocking)

Every OpenRTB model — discovered by **path** (`rendering/models/openrtb/` under core `src/main`),
so a new model is checked the moment it is added — must be referenced by at least one core unit
test. The wire format is the SDK's contract with Prebid Server; untested decode paths are where
protocol regressions hide. Empty discovery fails rather than passing on a moved directory. The
check is deliberately loose (name referenced anywhere in test sources); coverage *quality* is the
spec-grounding gate's job. One gap (`BaseBid`) is grandfathered.

**When it fails:** add a wire-format test exercising the type, citing the OpenRTB spec section.

### skiplist-ratchet (blocking)

The test-integrity policy says the skipped-test set may only shrink — never add a skip to make CI
pass. The Android skip surface is JUnit `@Ignore` plus any Gradle unit-test exclude filter (none
today). The baseline **enumerates every skipped identifier** (`Class#method`), so a *swap* —
un-ignoring one test while ignoring another — fails by name, which a bare count could never see.
A removed skip fails until `--update-skiplist-baseline` re-records it — a visible ratchet win
naming the exact test.

### deprecation-hygiene (blocking)

Deprecating API without telling the publisher what to use instead is a recurring review ask.
Java's `@Deprecated` annotation has no message parameter, so the machine-checkable form is the
Javadoc `@deprecated` tag with non-empty text — which is also what Javadoc/Dokka render as the
deprecation notice, so replacement prose without the tag still fails. Kotlin keeps the
compiler-side contract: non-empty `message` or `ReplaceWith`. Six declarations are grandfathered
(overloads share one token; an entry goes stale only when every overload is fixed).

### string-dup-ratchet (blocking)

A string literal duplicated across files is a constant waiting to drift. Literals ≥ 6 characters
in ≥ 3 distinct files of core `src/main` are baselined per literal (35 today — `'Context is
null'` appears in 10 files) and the per-literal file count may only shrink. Extraction strips
comments (license headers never count), char literals, and Java text blocks. The 3-file threshold
was tuned on this tree: at 2 files the findings are dominated by naturally repeated short JSON
keys. Update: `--update-string-dup-baseline`.

### manifest-surface (blocking) — no iOS counterpart

An ad SDK's manifest merges into every publisher's app: a silently added permission changes their
Play-Store data-safety story, an exported component changes their attack surface, a `<queries>`
entry changes what their app can see. The merged surface across every shipped module's manifest —
permissions, features, queries entries, `exported="true"` components, `uses-library` — is a
committed lockfile (3 entries today). **Any** change, addition or removal, fails until
`--update-manifest-baseline` re-records it in the same PR, making the manifest diff an explicit,
reviewable artifact. A broken manifest fails; it is never an empty surface.

### public-api-baseline (blocking, build tier)

The public API of every published module is a committed lockfile: one
[Metalava](https://android.googlesource.com/platform/tools/metalava/) signature file per module in
`api/`, reviewable as one `+`/`-` line per moved declaration. Metalava is AndroidX's own API
tracker — PSI-based, Java + Kotlin, reading the real compile classpath — so it sees
implicitly-public members, nested types, and enum constants that a hand-written source parser (the
iOS approach) cannot. This is a genuine upgrade over the iOS mechanism, not just a port.

The guard regenerates into a temp directory and diffs against `api/`. Any drift fails until:

```bash
./scripts/guards/run-guards.sh --update-api-baseline
```

Commit the `api/` diff in the same PR and call it out — the PR title becomes a release-note line.
If you didn't mean to change the API, narrow the visibility instead. Wiring lives in
`metalava.gradle` (version pinned; API-lint issues that fire on pre-existing debt are hidden —
the task snapshots, it does not lint). Generation is deterministic: two runs from the same tree
produce byte-identical files.

### api-doc-coverage (blocking)

Public API carries doc comments — Dokka publishes them as the SDK's reference docs. The per-file
count of **undocumented** public declarations may only shrink; new public API arrives documented,
and a cross-file swap fails in the file that grew. "Public" comes from the committed `api/*.txt`,
so this guard can never disagree with `public-api-baseline` about the surface. The starting
backlog is honest and large: 2,945 undocumented declarations across 405 files — grandfathered, not
demanded up front. Update: `--update-api-doc-baseline`.

Known, accepted misses (line-based heuristic): implicitly-public Java interface members, and
declaration heads that span lines before the keyword.

### api-test-presence (blocking)

Every public type in `api/*.txt` must be referenced by at least one module's unit tests —
`ortb-test-presence` for the whole surface. An untested public type is either missing a test or
shouldn't be public. 112 pre-existing gaps are grandfathered as the untested-public-API backlog
(mostly adapter managers and rendering internals that are public but unexercised).

### ast-rule-ratchet (blocking — per-rule finding count may not grow)

Four ast-grep pattern rules in `scripts/guards/rules/`, count-baselined per rule so one rule's win
can never mask another's regression. The iOS rules translate to Android's own defect classes:

- **empty-catch** (Java; 8 grandfathered) — a swallowed exception is a broken ad with no log
  line. Log through `LogUtil`, recover, or rethrow.
- **kotlin-not-null-assert** (Kotlin; **zero, locked**) — `!!` turns unexpected null into a
  publisher-visible crash; the literal analog of the iOS force-unwrap rule.
- **static-context-leak** (Java; **zero, locked**) — a static `Context`/`Activity`/`View`/
  `WebView` field outlives its component and leaks the *publisher's* activity — the Android form
  of the iOS weak-delegate defect class. An AST rule, not grep: only field declarations match,
  never locals or method signatures. `PrebidContextHolder`'s `WeakReference` is the sanctioned
  pattern.
- **main-thread-callbacks** (Java; 283 grandfathered) — `on*()` calls on `*[lL]istener`-named
  receivers not visibly inside `TasksManager.getInstance().executeOnMainThread`, a Handler
  `post`, or `runOnUiThread`. Same publisher-facing contract as iOS. Calibrated but still
  heuristic — a growth failure can be a false positive (e.g. a call already on the main thread by
  construction); the justified baseline update is the escape hatch.

A missing ast-grep reports SKIPPED (CI is authoritative — the version is pinned identically in
the check and `guards.yml`, because version drift changes finding counts); a failed or unparsable
scan FAILS — never zero findings. Inspect with:

```bash
ast-grep scan -c scripts/guards/sgconfig.yml PrebidMobile
```

## Deliberately not ported

Recorded here so the omissions read as decisions, not oversights:

- **swift-migration-direction** — the iOS "no new ObjC in the core" rule rests on an agreed
  migration. This core is 426 Java files with zero Kotlin, and no maintainer-agreed Java→Kotlin
  policy exists; imposing a direction through tooling would be a policy change smuggled in as a
  check.
- **api-naming** (the `PBM` prefix rule) — purely an artifact of iOS's ObjC bridging renames.
  Android has no prefix convention with a crisp mechanical rule.
- **adapter-isolation** — Swift gives the rule real seams (`@_spi`, private headers, an internal
  module). Java has none: the four adapter modules already import ~22 internal classes
  (`rendering.*`, `configuration.*`) by design, and an allowlist would freeze that reality
  without a path to zero. The public-doc boundary remains visible in the Dokka suppression list
  in `PrebidMobile/build.gradle`; reviewers judge adapter imports against it by hand.
- **js-bridge-surface** — considered (a lockfile over `@JavascriptInterface` methods,
  `addJavascriptInterface` sites, and WebView hardening flags; 43/2/2 today) and deferred as a
  follow-up candidate rather than shipped in the first cut.

Other follow-up candidates, measured but not implemented: **resource-prefix** (no
`resourcePrefix` is set and library resources can collide with publisher resources — should guard
newly added resources only), **consumer-proguard** (`proguard-rules.pro` is 0 bytes while
`consumerProguardFiles` references it, and the core uses reflection — needs investigation before
a guard), **sdk-level-ratchet** (`minSdk`/`targetSdk`/`compileSdk` + dependency versions as a
lockfile).

## Adding a guard

1. Structural Java/Kotlin pattern → ast-grep YAML in `scripts/guards/rules/` + its id in
   `RULES` in `ast_rule_ratchet.py`, baselined via `--update-ast-rule-baseline`.
2. Content/parse rule (imports, counts, XML, consistency) → Python check in
   `scripts/guards/checks/` built on `scripts/guards/lib/guardlib.py` (stdlib only — no pip),
   with unit tests in `scripts/guards/tests/` (run in CI).

`guardlib.py` is copied **verbatim** from prebid-mobile-ios — deliberately platform-agnostic; do
not fork it. It provides the three ratchet primitives (lockfile, allowlist, count), the validated
JSON readers/writers, and `cli()`, which turns a malformed data file into an actionable FAIL. A
check that needs an external tool exits `guardlib.EXIT_SKIPPED` when the tool is missing.

Every new guard must: run green on `master` (grandfather via allowlist if needed), prove one true
positive and one true negative (introduce a deliberate violation → actionable FAIL; revert →
clean pass), be registered in `run-guards.sh`, and get a section in this file. See
`agents/guard/SKILL.md` for the full authoring playbook.
