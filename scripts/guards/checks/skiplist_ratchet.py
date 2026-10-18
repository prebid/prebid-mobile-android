#!/usr/bin/env python3
"""Guard: skiplist-ratchet.

The test-integrity policy says the set of skipped tests may only SHRINK —
never add a skip to make CI pass. On Android the skip surface is JUnit
`@Ignore` (plus any Gradle-level unit-test exclude filter, none today).

The baseline ENUMERATES every skipped test identifier
(baselines/skiplist.json, `Class#method` per entry, bare `Class` for a
class-level @Ignore), so a *swap* — un-ignoring one test while ignoring
another — fails by name, which a bare count could never see.

  new skip     → FAIL naming the test (fix the test, don't skip it)
  removed skip → FAIL until the baseline is updated in the same PR
  update       → ./scripts/guards/run-guards.sh --update-skiplist-baseline

Commented-out `//@Ignore` markers don't count (they are dead text, and
the fixme/marker hygiene of comments is not this guard's job).
"""

import os
import re
import sys

_HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(os.path.dirname(os.path.dirname(_HERE)))
sys.path.insert(0, os.path.join(ROOT, "scripts", "guards", "lib"))
import guardlib  # noqa: E402

BASELINE = os.path.join(ROOT, "scripts", "guards", "baselines", "skiplist.json")
UPDATE_CMD = "./scripts/guards/run-guards.sh --update-skiplist-baseline"
MODULES = (
    "PrebidMobile-core",
    "PrebidMobile-gamEventHandlers",
    "PrebidMobile-admobAdapters",
    "PrebidMobile-maxAdapters",
    "PrebidMobile-nextGenEventHandlers",
    "test-utils",
)
_IGNORE_RE = re.compile(r"^\s*@Ignore\b")
_METHOD_RE = re.compile(
    r"^\s*(?:public\s+|protected\s+|private\s+|static\s+|final\s+|synchronized\s+)*"
    r"(?:void\s+(\w+)|fun\s+(\w+))\s*\(")
_CLASS_RE = re.compile(r"^\s*(?:\w+\s+)*(?:class|object)\s+(\w+)")
_ANNOTATION_RE = re.compile(r"^\s*@\w+")
# Gradle-level unit-test skips (e.g. `excludeTestsMatching`, test { exclude … }).
_GRADLE_EXCLUDE_RE = re.compile(r"excludeTestsMatching\s*[('\"]+([^'\")]+)")


def _sites_in_file(path):
    """Skip identifiers declared in one test source file."""
    fallback_class = os.path.splitext(os.path.basename(path))[0]
    with open(path, encoding="utf-8", errors="replace") as fh:
        lines = fh.readlines()

    sites = []
    for index, line in enumerate(lines):
        if not _IGNORE_RE.match(line):
            continue
        # Walk forward past other annotations to the annotated declaration.
        target = None
        for follow in lines[index + 1:index + 10]:
            if _ANNOTATION_RE.match(follow) or not follow.strip():
                continue
            method = _METHOD_RE.match(follow)
            if method:
                target = f"{fallback_class}#{method.group(1) or method.group(2)}"
            else:
                klass = _CLASS_RE.match(follow)
                target = klass.group(1) if klass else fallback_class
            break
        sites.append(target or fallback_class)
    return sites


def _gradle_excludes(root):
    """`gradle:<pattern>` entries for unit-test exclude filters."""
    entries = []
    gradle_files = [os.path.join(root, "PrebidMobile", "android.gradle")]
    for module in MODULES:
        gradle_files.append(
            os.path.join(root, "PrebidMobile", module, "build.gradle"))
    for path in gradle_files:
        if not os.path.exists(path):
            continue
        with open(path, encoding="utf-8", errors="replace") as fh:
            for line in fh:
                match = _GRADLE_EXCLUDE_RE.search(line)
                if match:
                    entries.append(f"gradle:{match.group(1)}")
    return entries


def skipped_tests(root=ROOT):
    """Sorted skip identifiers across every library module's tests."""
    entries = []
    scanned_any = False
    for module in MODULES:
        test_root = os.path.join(root, "PrebidMobile", module, "src", "test")
        if not os.path.isdir(test_root):
            continue
        scanned_any = True
        for path in guardlib.walk_files(test_root, (".java", ".kt")):
            entries.extend(_sites_in_file(path))
    if not scanned_any:
        raise guardlib.GuardDataError(
            "no module test roots found — refusing to scan an empty scope."
        )
    entries.extend(_gradle_excludes(root))
    return guardlib.c_sorted(entries)


def main(argv):
    current = skipped_tests()

    if len(argv) > 1 and argv[1] == "--update":
        guardlib.write_entries(BASELINE, current, guard="skiplist-ratchet")
        print(f"Recorded {len(current)} skipped test(s) in "
              "scripts/guards/baselines/skiplist.json")
        return 0

    recorded = guardlib.read_entries(BASELINE, UPDATE_CMD)
    added = sorted(set(current) - set(recorded))
    removed = sorted(set(recorded) - set(current))

    if added:
        print("FAIL: new skipped test(s) — never add a skip to make CI pass; fix the")
        print("test or report the failure as a blocker:")
        for entry in added:
            print(f"  {entry}")
    if removed:
        print("FAIL: test(s) un-skipped — a ratchet win, but the baseline must be")
        print("updated in the same PR:")
        print("      " + UPDATE_CMD)
        for entry in removed:
            print(f"  {entry}")
    if added or removed:
        return 1

    print(f"OK: skip-list unchanged ({len(current)} skipped test(s)).")
    return 0


if __name__ == "__main__":
    sys.exit(guardlib.cli(main)(sys.argv))
