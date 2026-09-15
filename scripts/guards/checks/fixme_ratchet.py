#!/usr/bin/env python3
"""Guard: fixme-ratchet.

FIXME/TODO markers in shipped core sources are deferred behavior decisions —
several of the current ones (e.g. the ORTB 2.5 auto-detect questions in the
openrtb models) have shipped as behavior. The count is a committed ratchet:
it may only shrink.

  growth  → FAIL: remove the marker (fix it, or file an issue and delete it)
  shrink  → FAIL until the baseline is updated in the same PR (ratchet win)
  update  → ./scripts/guards/run-guards.sh --update-fixme-baseline

Scope: PrebidMobile/PrebidMobile-core/src/main — the code publishers ship.
Tests and demo apps are out of scope; a TODO in a test is review territory,
not a publisher-facing deferred decision.
"""

import os
import re
import sys

_HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(os.path.dirname(os.path.dirname(_HERE)))
sys.path.insert(0, os.path.join(ROOT, "scripts", "guards", "lib"))
import guardlib  # noqa: E402

BASELINE = os.path.join(ROOT, "scripts", "guards", "baselines", "fixme-count.json")
UPDATE_CMD = "./scripts/guards/run-guards.sh --update-fixme-baseline"
SCAN_ROOT = os.path.join("PrebidMobile", "PrebidMobile-core", "src", "main")
_MARKER_RE = re.compile(r"(//|/\*).*(FIXME|TODO)", re.IGNORECASE)


def count(root=ROOT):
    src_root = os.path.join(root, SCAN_ROOT)
    if not os.path.isdir(src_root):
        raise guardlib.GuardDataError(
            f"scan root {SCAN_ROOT} does not exist — refusing to count an empty scope."
        )
    total = 0
    for path in guardlib.walk_files(src_root, (".java", ".kt")):
        with open(path, encoding="utf-8", errors="replace") as fh:
            total += sum(1 for line in fh if _MARKER_RE.search(line))
    return total


def main(argv):
    current = count()

    if len(argv) > 1 and argv[1] == "--update":
        guardlib.write_count(BASELINE, current, guard="fixme-ratchet")
        print(f"Recorded {current} FIXME/TODO marker(s) in "
              "scripts/guards/baselines/fixme-count.json")
        return 0

    code, messages = guardlib.check_count(
        current, BASELINE, "FIXME/TODO", UPDATE_CMD,
        grow_hint=[
            "The ratchet only shrinks. Fix the marked issue, or file a GitHub issue and",
            "delete the marker. Inspect with:",
            "      grep -rniE '(//|/\\*).*(FIXME|TODO)' "
            "PrebidMobile/PrebidMobile-core/src/main --include='*.java' --include='*.kt'",
        ],
    )
    print("\n".join(messages))
    return code


if __name__ == "__main__":
    sys.exit(guardlib.cli(main)(sys.argv))
