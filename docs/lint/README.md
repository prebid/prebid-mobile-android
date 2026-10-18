# Android Lint (diff-scoped)

Style and correctness linting for the library modules, scoped to the lines a branch **adds**. A
violation on one of those lines fails the run, locally and on the PR.

```bash
./scripts/lint/run-lint.sh              # what this branch added — exit 1 on findings
./scripts/lint/run-lint.sh --advisory   # same, report only
```

Blocking is the default so a local run gives the same verdict as CI. Requires a JDK and the
Android SDK (the runner invokes the module `lintRelease` tasks); without them it exits 2
(SKIPPED) rather than 0 — it never reports a skip as a pass, the same convention the guards use.
In CI (`GUARDS_REQUIRE_BUILD=1`, `.github/workflows/lint.yml`) a could-not-run is a FAIL.

## This is not a guard

The [structural guards](../guards/README.md) and Lint solve different problems:

| | Guards | Lint |
|---|---|---|
| Enforces | architecture: API surface, manifest surface, test presence, ratchets | per-file correctness and idiom |
| Scope | cross-file, XML + git + Gradle output | one file at a time |
| Debt model | ratcheted — allowlists only shrink, a stale entry fails | none; legacy is simply out of scope |
| Debt it can demand you fix | anything the rule matches | only lines you added |

## One owner per rule

Checks a guard already enforces are deliberately left ignored in [`lint.xml`](../../lint.xml) —
two systems counting the same thing with different numbers is worse than either alone:

| Lint check | Owned instead by |
|---|---|
| `StaticFieldLeak` | `ast-rule-ratchet` → `static-context-leak` |
| `StopShip` (marker policy) | `fixme-ratchet` |

`SetJavaScriptEnabled` / `JavascriptInterface` / `AddJavascriptInterface` stay in Lint: the
`js-bridge-surface` guard was deliberately not shipped (see "Deliberately not ported" in the
guards README), so Lint is their sole owner.

## Why diff-scoped

`lintOptions { abortOnError false }` has been set repo-wide for years, so the 400+-file Java core
carries a legacy backlog no one can act on. That backlog is deliberately not being fixed:
reformatting or drive-by-fixing untouched code would bury every real diff in the release it
landed in. The runner intersects the lint reports with the lines the branch added versus the
merge-base with `master`: touching a file with legacy findings reports only what you wrote.

`abortOnError false` stays as-is — the gate is the diff-scoped runner, not the module task.

## Why not lint-baseline.xml

Lint ships its own baseline mechanism, and it is deliberately not used: `lint-baseline.xml`
suppresses pre-existing findings but **never fails when an entry goes stale**, so the debt stops
being monotonically decreasing — it has no ratchet. Diff-scoping achieves the same "new code
arrives clean, legacy stays silent" outcome without a file that can rot.

## Changing the rule set

[`lint.xml`](../../lint.xml) ignores everything (`id="all"`) and enables checks **explicitly**, so
an AGP/Lint version bump can never silently introduce or drop a check — the same determinism
requirement that pins ast-grep for the guards. Adding a check means: run it on the current tree,
confirm it does not duplicate a guard, and add it with an explicit severity. Findings gate at
`Warning` severity and above; `Informational` never blocks.

## CI

[`.github/workflows/lint.yml`](../../.github/workflows/lint.yml) — ubuntu, JDK 17, blocking, runs
on every PR alongside the Metalava API-baseline check (the two build-tier checks share the one
expensive toolchain setup). Findings are echoed into the job summary so a contributor reads what
to fix without opening the log. The one thing the gate never does is demand you fix code you did
not write.
