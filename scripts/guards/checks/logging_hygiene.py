#!/usr/bin/env python3
"""Guard: logging-hygiene.

Publishers see the SDK's console output inside THEIR apps — raw
android.util.Log calls, System.out/err printing, and printStackTrace()
bypass the LogUtil facade's log level and custom-logger routing
(PrebidLogger), so publishers can't silence or redirect them. All output
goes through LogUtil.verbose/debug/info/warning/error.

Violations, per line, outside comments:
  - android.util.Log output calls: Log.v/d/i/w/e/wtf/println(
    (Log.getStackTraceString() is a pure formatter, not output — allowed)
  - System.out.print* / System.err.print*
  - .printStackTrace()

Structurally exempt: LogUtil.java — the facade is the one sanctioned
android.util.Log call site.

Scope: src/main of every shipped library module. Ratchet: pre-existing
violating files are grandfathered in allowlists/logging-hygiene.json
(shrink-only; stale entries fail and must be removed in the same PR).
"""

import os
import re
import sys

_HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(os.path.dirname(os.path.dirname(_HERE)))
sys.path.insert(0, os.path.join(ROOT, "scripts", "guards", "lib"))
import guardlib  # noqa: E402

ALLOWLIST = os.path.join(ROOT, "scripts", "guards", "allowlists", "logging-hygiene.json")
MODULES = (
    "PrebidMobile-core",
    "PrebidMobile-gamEventHandlers",
    "PrebidMobile-admobAdapters",
    "PrebidMobile-maxAdapters",
    "PrebidMobile-nextGenEventHandlers",
)
EXEMPT_FILES = (
    os.path.join("PrebidMobile", "PrebidMobile-core", "src", "main", "java",
                 "org", "prebid", "mobile", "LogUtil.java"),
)
# Output-producing android.util.Log calls. Log.getStackTraceString and the
# level constants (Log.VERBOSE, …) are deliberately not matched.
_LOG_CALL_RE = re.compile(
    r"(^|[^A-Za-z0-9_.]|android\.util\.)Log\.(v|d|i|w|e|wtf|println)\(")
_SYSTEM_RE = re.compile(r"System\.(out|err)\.print")
_STACKTRACE_RE = re.compile(r"\.printStackTrace\(")
_COMMENT_RE = re.compile(r"^\s*(//|\*|/\*)")


def _match_lines(path):
    """(lineno, text) for raw console-output calls in one file."""
    hits = []
    with open(path, encoding="utf-8", errors="replace") as fh:
        for lineno, line in enumerate(fh, 1):
            if _COMMENT_RE.match(line):
                continue
            if (_LOG_CALL_RE.search(line) or _SYSTEM_RE.search(line)
                    or _STACKTRACE_RE.search(line)):
                hits.append((lineno, line.rstrip("\n")))
    return hits


def violations(root=ROOT):
    """{relative file path: [(lineno, line), …]} for violating files."""
    out = {}
    scanned_any = False
    for module in MODULES:
        src_root = os.path.join(root, "PrebidMobile", module, "src", "main")
        if not os.path.isdir(src_root):
            continue
        scanned_any = True
        for path in guardlib.walk_files(src_root, (".java", ".kt")):
            rel = os.path.relpath(path, root)
            if rel in EXEMPT_FILES:
                continue
            hits = _match_lines(path)
            if hits:
                out[rel] = hits
    if not scanned_any:
        raise guardlib.GuardDataError(
            "no library module source roots found — refusing to scan an empty scope."
        )
    return out


def main(_argv):
    found = violations()
    allow = guardlib.read_allowlist(ALLOWLIST)
    new, stale = guardlib.ratchet(found.keys(), allow)

    fail = False
    if new:
        print("FAIL: raw console output in production code — route through LogUtil")
        print("(LogUtil.verbose/debug/info/warning/error), which respects the log level")
        print("and custom PrebidLogger routing:")
        for f in new:
            for lineno, line in found[f][:5]:
                print(f"{f}:{lineno}:{line}")
        fail = True
    if stale:
        print("FAIL: stale allowlist entries (file clean or gone) — delete them from")
        print("scripts/guards/allowlists/logging-hygiene.json in this PR:")
        print("\n".join(guardlib.describe_entries(ALLOWLIST, stale)))
        fail = True

    if not fail:
        print(f"OK: no raw console output outside the LogUtil facade "
              f"(allowlist: {len(allow)} grandfathered file(s)).")
    return 1 if fail else 0


if __name__ == "__main__":
    sys.exit(guardlib.cli(main)(sys.argv))
