---
name: guard
description: Author a new structural guard from a prose architecture rule — pick the right mechanism (ast-grep / Python probe / XML or Gradle-output parser), grandfather existing violations, prove true-positive and true-negative, register and document it.
---

# /guard <rule description>

Turns an architecture rule into a permanent, deterministic check in `scripts/guards/`.
Read `docs/guards/README.md` first — it defines the ratchet contract every guard obeys.

## 1. Pick the mechanism

| Rule shape | Mechanism | Existing example |
|---|---|---|
| Structural Java/Kotlin pattern ("X must (not) appear inside Y") | ast-grep YAML in `scripts/guards/rules/` + rule id in `RULES` in `ast_rule_ratchet.py` | `static-context-leak.yml` |
| Content/parse rule (calls, markers, counts, presence) | Python check in `scripts/guards/checks/` on `lib/guardlib.py` (stdlib only, no pip) | `logging_hygiene.py`, `fixme_ratchet.py` |
| Committed-surface change control (XML, signature files) | lockfile check on `guardlib` | `manifest_surface.py`, `api_baseline.py` |
| Anything needing a classpath | Gradle task + Python comparator, `EXIT_SKIPPED` when the toolchain is absent, `GUARDS_REQUIRE_BUILD=1` in CI | `metalava.gradle` + `api_baseline.py` |

Python checks reuse `guardlib`'s three primitives (lockfile, allowlist ratchet, count ratchet)
and get unit tests in `scripts/guards/tests/` (run in CI). All committed data is JSON read
through `guardlib` (schema-validated, so a malformed file FAILs instead of reading as "no
findings"); wrap `main` in `guardlib.cli()` so that failure is actionable. `guardlib.py` is
copied verbatim from prebid-mobile-ios and is platform-agnostic by design — never fork it.

Prefer the simplest mechanism with no false positives. False negatives are acceptable (a guard
that catches 80% mechanically beats a review comment); false positives are not — they teach
people to ignore guards.

## 2. Build it

1. Write the rule or check. Checks: Python 3 stdlib only, self-locate `ROOT` like the existing
   checks, deterministic output (sorted), actionable FAIL message that says how to fix. Keep
   parsing separate from I/O (see `ast_rule_ratchet.parse_stream`) so it is testable without the
   tool installed. An empty scan scope raises — it is never a pass.
2. Run against the current tree. Existing violations → grandfather into
   `scripts/guards/allowlists/<guard>.json` (or a count baseline) and implement the ratchet: new
   violations fail, stale entries fail. Use `guardlib.read_allowlist()` + `guardlib.ratchet()`;
   report stale entries with `guardlib.describe_entries()`.
3. Each grandfathered entry is a record — `entry` (the exact token your probe emits) and
   `reason`. The schema is validated on read, so a missing or empty reason fails the run.

## 3. Prove it (mandatory, before committing)

- **True positive**: introduce a violation on a scratch change → guard fails with the actionable
  message. Revert.
- **True negative**: clean tree → guard passes. Full suite `./scripts/guards/run-guards.sh`
  still exits 0.
- **Stale entry** (allowlist guards): remove one grandfathered violation from source → the guard
  fails naming the stale entry and printing its reason. Revert.

## 4. Register and document

- Add a `run_guard` line in `scripts/guards/run-guards.sh` (blocking), and an `--update-*` flag
  if the guard owns a baseline.
- Add a section to `docs/guards/README.md`: what it enforces, why, how to fix a failure.
- One commit for the whole guard: rule/script + allowlist/baseline + tests + docs + runner entry.
