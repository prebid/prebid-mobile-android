#!/usr/bin/env python3
"""Guard: ast-rule-ratchet.

The ast-grep pattern rules in scripts/guards/rules/ (empty-catch,
kotlin-not-null-assert, static-context-leak, main-thread-callbacks)
ratchet on COUNTS — per rule, so one rule's win can never mask another's
regression:

  growth  → FAIL: fix the new site (or update the baseline with justification)
  shrink  → FAIL until the baseline is updated in the same PR (ratchet win)
  update  → ./scripts/guards/run-guards.sh --update-ast-rule-baseline

Two failure modes this check refuses to confuse with a clean tree:

  - ast-grep is not installed → EXIT_SKIPPED (advisory; CI is authoritative).
    A missing tool is not zero findings.
  - the scan fails or emits an unparsable line → FAIL. A malformed rule file
    would otherwise read as "no findings", i.e. a fake ratchet win.
  - a rule file disappearing → its key starts at 0, which reads as a shrink
    and fails until explained.

The scan runs ONCE and every rule's count comes out of that single result.
The version is pinned (PINNED_VERSION, kept in sync with the install step
in .github/workflows/guards.yml) because version drift changes finding
counts; a local mismatch prints a note and defers to CI.
"""

import json
import os
import shutil
import subprocess
import sys

_HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(os.path.dirname(os.path.dirname(_HERE)))
sys.path.insert(0, os.path.join(ROOT, "scripts", "guards", "lib"))
import guardlib  # noqa: E402

BASELINE = os.path.join(ROOT, "scripts", "guards", "baselines", "ast-rule-counts.json")
SGCONFIG = os.path.join(ROOT, "scripts", "guards", "sgconfig.yml")
UPDATE_CMD = "./scripts/guards/run-guards.sh --update-ast-rule-baseline"
# NB: relative scan path — the rules' `files:` globs match relative paths.
SCAN_PATH = "PrebidMobile"
INSPECT_CMD = f"ast-grep scan -c scripts/guards/sgconfig.yml {SCAN_PATH}"
RULES = ("empty-catch", "kotlin-not-null-assert", "static-context-leak",
         "main-thread-callbacks")
PINNED_VERSION = "0.45.1"  # keep in sync with .github/workflows/guards.yml


class ScanError(Exception):
    """The scan could not produce trustworthy counts."""


def installed_version():
    """The local ast-grep version, or None when it is not installed."""
    if not shutil.which("ast-grep"):
        return None
    result = subprocess.run(["ast-grep", "--version"], cwd=ROOT,
                            capture_output=True, text=True, check=False)
    return result.stdout.strip().split()[-1] if result.returncode == 0 else None


def parse_stream(stdout, rules=RULES):
    """{rule id: finding count} from `--json=stream` output (one JSON object
    per line).

    Known rules start at 0, so a rule that stops matching entirely surfaces
    as a shrink rather than vanishing silently. An unparsable line raises
    rather than being skipped: silently ignoring output whose format changed
    would undercount, which reads as a ratchet win.
    """
    counts = {rule: 0 for rule in rules}
    for lineno, line in enumerate(stdout.splitlines(), 1):
        line = line.strip()
        if not line:
            continue
        try:
            finding = json.loads(line)
        except ValueError:
            raise ScanError(
                f"unparsable --json=stream output at line {lineno} "
                "(ast-grep output format changed?)"
            )
        rule = finding.get("ruleId")
        if rule is not None:
            counts[rule] = counts.get(rule, 0) + 1
    return counts


def scan_counts():
    """{rule id: finding count} from a single ast-grep scan."""
    result = subprocess.run(
        ["ast-grep", "scan", "-c", SGCONFIG, SCAN_PATH, "--json=stream"],
        cwd=ROOT, capture_output=True, text=True, check=False,
    )
    if result.returncode != 0:
        raise ScanError(
            (result.stderr.strip().splitlines() or
             [f"ast-grep exited {result.returncode}"])[-1]
        )
    return parse_stream(result.stdout)


def main(argv):
    version = installed_version()
    if version is None:
        print("SKIPPED: ast-grep not installed (brew install ast-grep). CI runs this check.")
        return guardlib.EXIT_SKIPPED
    if version != PINNED_VERSION:
        print(f"NOTE: local ast-grep {version} differs from CI's pinned {PINNED_VERSION} —")
        print("      counts may disagree with CI, which is authoritative.")

    try:
        current = scan_counts()
    except ScanError as exc:
        print(f"FAIL: ast-grep scan failed ({exc}) — counts are unavailable, NOT zero.")
        print("Do not update any baseline off this state. Debug with:")
        print("      " + INSPECT_CMD)
        return 1

    if len(argv) > 1 and argv[1] == "--update":
        guardlib.write_keyed_counts(BASELINE, current, guard="ast-rule-ratchet")
        print("Recorded ast-grep rule counts in scripts/guards/baselines/ast-rule-counts.json:")
        for rule, count in sorted(current.items()):
            print(f"  {rule} {count}")
        print("Commit the diff in the same PR and justify any increase for review.")
        return 0

    code, messages = guardlib.check_keyed_counts(
        current, BASELINE, "ast-grep finding", UPDATE_CMD,
        grow_hint=[
            "Fix the new site(s), or update the baseline with justification in this PR.",
            "Inspect with:",
            "      " + INSPECT_CMD,
        ],
    )
    print("\n".join(messages))
    return code


if __name__ == "__main__":
    sys.exit(guardlib.cli(main)(sys.argv))
