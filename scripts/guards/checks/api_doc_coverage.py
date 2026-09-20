#!/usr/bin/env python3
"""Guard: api-doc-coverage.

Public API carries doc comments — Dokka publishes them as the SDK's
reference documentation, and "please describe this method" is a recurring
review ask. This guard makes the rule a per-file ratchet: the count of
UNDOCUMENTED public declarations per source file may only shrink. New
public API arrives documented; a cross-file swap fails in the file that
grew.

Scope: the source files that define the public types in the committed API
baseline (api/*.txt) — the same definition of "public" the
public-api-baseline guard checks. Within those files:

  Java   — a declaration line starting with `public` counts; "documented"
           means a /** … */ block immediately above (annotations between
           are fine).
  Kotlin — declarations are public by default; class/fun/val/var/object/
           interface heads without private/internal/protected/override
           count the same way.

Known, accepted misses (line-based heuristic, not a parser): implicitly
public interface members in Java, and declarations whose head spans lines
before the keyword. The committed counts are the contract; the regenerated
diff is what reviewers eyeball.

  growth  → FAIL: document the new declaration
  shrink  → FAIL until the baseline is updated in the same PR (ratchet win)
  update  → ./scripts/guards/run-guards.sh --update-api-doc-baseline
"""

import os
import re
import sys

_HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(os.path.dirname(os.path.dirname(_HERE)))
sys.path.insert(0, os.path.join(ROOT, "scripts", "guards", "lib"))
import apisig  # noqa: E402
import guardlib  # noqa: E402

BASELINE = os.path.join(ROOT, "scripts", "guards", "baselines", "api-doc-counts.json")
UPDATE_CMD = "./scripts/guards/run-guards.sh --update-api-doc-baseline"
MODULES = (
    "PrebidMobile-core",
    "PrebidMobile-gamEventHandlers",
    "PrebidMobile-admobAdapters",
    "PrebidMobile-maxAdapters",
    "PrebidMobile-nextGenEventHandlers",
)
_JAVA_PUBLIC_RE = re.compile(r"^\s*public\s")
_KT_DECL_RE = re.compile(
    r"^\s*(?:(?:open|final|abstract|sealed|data|inner|suspend|operator|infix"
    r"|inline|enum|annotation|companion|lateinit|const)\s+)*"
    r"(?:class|interface|object|fun|val|var)\b")
_KT_NONPUBLIC_RE = re.compile(r"^\s*(?:private|internal|protected|override)\b")
_ANNOTATION_RE = re.compile(r"^\s*@\w+")
_COMMENT_LINE_RE = re.compile(r"^\s*(\*|//|/\*)")


def _is_documented(lines, index):
    """True when a doc block closes immediately above lines[index]
    (annotation and blank lines in between are tolerated)."""
    cursor = index - 1
    while cursor >= 0:
        line = lines[cursor].strip()
        if not line or _ANNOTATION_RE.match(lines[cursor]):
            cursor -= 1
            continue
        return line.endswith("*/")
    return False


def undocumented_in_file(path):
    """Count of undocumented public declarations in one source file."""
    with open(path, encoding="utf-8", errors="replace") as fh:
        lines = fh.read().splitlines()
    is_kotlin = path.endswith(".kt")
    count = 0
    for index, line in enumerate(lines):
        if _COMMENT_LINE_RE.match(line):
            continue
        if is_kotlin:
            if not _KT_DECL_RE.match(line) or _KT_NONPUBLIC_RE.match(line):
                continue
        else:
            if not _JAVA_PUBLIC_RE.match(line):
                continue
        if not _is_documented(lines, index):
            count += 1
    return count


def public_source_files(root=ROOT, api_dir=None):
    """Sorted relative paths of the source files defining public types."""
    api_dir = api_dir or os.path.join(root, "api")
    files = set()
    for module in MODULES:
        signature_path = os.path.join(api_dir, f"{module}.txt")
        try:
            types = apisig.public_types(signature_path)
        except apisig.SignatureError as exc:
            raise guardlib.GuardDataError(
                f"{exc} — regenerate with ./scripts/guards/run-guards.sh "
                "--update-api-baseline")
        known_packages = apisig.packages(signature_path)
        java_root = os.path.join(root, "PrebidMobile", module, "src", "main", "java")
        for qualified, _kind in types:
            package = apisig.package_of(qualified, known_packages)
            for candidate in apisig.outer_source_candidates(qualified, package):
                path = os.path.join(java_root, candidate)
                if os.path.exists(path):
                    files.add(os.path.relpath(path, root))
                    break
    if not files:
        raise guardlib.GuardDataError(
            "no source files resolved from api/*.txt — an empty scope is never a pass."
        )
    return sorted(files)


def undocumented_counts(root=ROOT, api_dir=None):
    """{relative file path: undocumented public declaration count}."""
    counts = {}
    for rel in public_source_files(root, api_dir):
        count = undocumented_in_file(os.path.join(root, rel))
        if count:
            counts[rel] = count
    return counts


def main(argv):
    current = undocumented_counts()

    if len(argv) > 1 and argv[1] == "--update":
        guardlib.write_keyed_counts(BASELINE, current, guard="api-doc-coverage")
        total = sum(current.values())
        print(f"Recorded {total} undocumented public declaration(s) across "
              f"{len(current)} file(s) in scripts/guards/baselines/api-doc-counts.json")
        return 0

    code, messages = guardlib.check_keyed_counts(
        current, BASELINE, "undocumented public declaration", UPDATE_CMD,
        grow_hint=[
            "New public API arrives documented: add a /** … */ (or KDoc) block",
            "naming what the declaration does and when publishers use it.",
        ],
    )
    print("\n".join(messages))
    return code


if __name__ == "__main__":
    sys.exit(guardlib.cli(main)(sys.argv))
