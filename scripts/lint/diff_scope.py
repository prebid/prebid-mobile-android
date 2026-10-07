#!/usr/bin/env python3
"""Intersect Android Lint findings with the lines a branch ADDS.

Used by scripts/lint/run-lint.sh. Reads the lint-results XML reports the
module lint tasks produce, computes the added lines versus the merge-base
with master, and prints only the findings that sit on one of those lines.

Exit codes: 0 = no findings on added lines, 1 = findings, 2 = could not
run (no diff base). A parse failure of a lint report is exit 1 — an
unreadable report is never an empty one.

Python 3 stdlib only.
"""

import os
import subprocess
import sys
import xml.etree.ElementTree as ET

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
EXIT_SKIPPED = 2
# Severities that gate. "informational" lines never block.
BLOCKING_SEVERITIES = ("Fatal", "Error", "Warning")


def merge_base():
    """The merge-base with master, or None."""
    for ref in ("origin/master", "master"):
        probe = subprocess.run(["git", "rev-parse", "--verify", "-q", ref],
                               cwd=ROOT, capture_output=True, text=True)
        if probe.returncode != 0:
            continue
        result = subprocess.run(["git", "merge-base", "HEAD", ref],
                                cwd=ROOT, capture_output=True, text=True)
        if result.returncode == 0 and result.stdout.strip():
            return result.stdout.strip()
    return None


def added_lines(base):
    """{(relpath, lineno), …} added versus `base`, uncommitted work included.

    -U0 so each hunk covers only changed lines; --no-prefix so the +++ path
    needs no a/ b/ stripping regardless of the developer's diff config.
    """
    result = subprocess.run(
        ["git", "diff", "--unified=0", "--no-color", "--no-prefix",
         "--diff-filter=AM", base, "--", "*.java", "*.kt", "*.xml"],
        cwd=ROOT, capture_output=True, text=True)
    if result.returncode != 0:
        return None
    added = set()
    path = None
    for line in result.stdout.splitlines():
        if line.startswith("+++ "):
            path = line[4:].strip()
        elif line.startswith("@@ ") and path and path != "/dev/null":
            # @@ -old,len +new,len @@
            new_part = line.split()[2]
            start, _, length = new_part.lstrip("+").partition(",")
            start = int(start)
            count = int(length) if length else 1
            for offset in range(count):
                added.add((path, start + offset))
    return added


def findings_in_report(report_path, added):
    """Lint findings from one report that land on added lines."""
    tree = ET.parse(report_path)
    hits = []
    for issue in tree.getroot().iter("issue"):
        severity = issue.get("severity", "")
        if severity not in BLOCKING_SEVERITIES:
            continue
        location = issue.find("location")
        if location is None:
            continue
        file_attr = location.get("file", "")
        line_attr = location.get("line")
        if line_attr is None:
            continue
        rel = os.path.relpath(file_attr, ROOT) if os.path.isabs(file_attr) else file_attr
        key = (rel.replace(os.sep, "/"), int(line_attr))
        if key in added:
            hits.append(
                f"{key[0]}:{key[1]}: {severity.lower()}: "
                f"{issue.get('message', '')} [{issue.get('id', '')}]")
    return hits


def main(argv):
    reports = argv[1:]
    if not reports:
        print("usage: diff_scope.py <lint-results.xml> [...]")
        return 1

    base = merge_base()
    if base is None:
        print("SKIPPED: no master ref to diff against (shallow clone?). CI is authoritative.")
        return EXIT_SKIPPED
    added = added_lines(base)
    if added is None:
        print("SKIPPED: could not compute the added-lines diff. CI is authoritative.")
        return EXIT_SKIPPED

    all_hits = []
    for report in reports:
        try:
            all_hits.extend(findings_in_report(report, added))
        except (ET.ParseError, OSError) as exc:
            print(f"FAIL: cannot read lint report {report} ({exc}) — an unreadable")
            print("report is never an empty one.")
            return 1

    if not all_hits:
        print(f"no lint findings on the {len(added)} line(s) this branch adds")
        return 0
    for hit in sorted(set(all_hits)):
        print(hit)
    print(f"\n{len(set(all_hits))} lint finding(s) on lines this branch adds.")
    return 1


if __name__ == "__main__":
    sys.exit(main(sys.argv))
