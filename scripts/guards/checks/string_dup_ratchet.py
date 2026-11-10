#!/usr/bin/env python3
"""Guard: string-dup-ratchet.

"The constant is duplicated across the SDK — consolidate it" is a
recurring review ask. A string literal (≥ 6 characters) appearing in 3 or
more distinct files of PrebidMobile-core/src/main is a duplicated
constant; the per-literal file counts are baselined and may only shrink.

  growth  → FAIL: reuse or hoist the existing constant instead of copying
            the literal
  shrink  → FAIL until the baseline is updated in the same PR (ratchet win)
  update  → ./scripts/guards/run-guards.sh --update-string-dup-baseline

Extraction is comment-aware (line and block comments stripped, so license
headers never count), char-literal-aware ('"' cannot desync the parser),
and Java text-block aware (\"\"\"…\"\"\" contents are skipped — a
multi-line block is not a constant candidate). The ≥3-files threshold was
tuned on this tree: at ≥2 the findings are dominated by naturally repeated
short JSON keys.
"""

import os
import sys

_HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(os.path.dirname(os.path.dirname(_HERE)))
sys.path.insert(0, os.path.join(ROOT, "scripts", "guards", "lib"))
import guardlib  # noqa: E402

BASELINE = os.path.join(ROOT, "scripts", "guards", "baselines", "string-dup-counts.json")
UPDATE_CMD = "./scripts/guards/run-guards.sh --update-string-dup-baseline"
SCAN_ROOT = os.path.join("PrebidMobile", "PrebidMobile-core", "src", "main")
MIN_LENGTH = 6
MIN_FILES = 3


def extract_literals(text):
    """Ordinary string literals in one source text, comments stripped."""
    literals = []
    i, n = 0, len(text)
    while i < n:
        ch = text[i]
        if text.startswith("//", i):
            j = text.find("\n", i)
            i = n if j == -1 else j + 1
        elif text.startswith("/*", i):
            j = text.find("*/", i + 2)
            i = n if j == -1 else j + 2
        elif ch == "'":
            j = i + 1
            while j < n and text[j] != "'":
                j += 2 if text[j] == "\\" else 1
            i = j + 1
        elif text.startswith('"""', i):  # Java 17 text block
            j = text.find('"""', i + 3)
            i = n if j == -1 else j + 3
        elif ch == '"':
            j = i + 1
            chunk = []
            while j < n and text[j] != '"':
                if text[j] == "\\" and j + 1 < n:
                    chunk.append(text[j:j + 2])
                    j += 2
                else:
                    chunk.append(text[j])
                    j += 1
            literals.append("".join(chunk))
            i = j + 1
        else:
            i += 1
    return literals


def duplicated(root=ROOT):
    """{literal: distinct-file count} for literals ≥ MIN_LENGTH chars seen
    in ≥ MIN_FILES distinct files."""
    src_root = os.path.join(root, SCAN_ROOT)
    if not os.path.isdir(src_root):
        raise guardlib.GuardDataError(
            f"scan root {SCAN_ROOT} does not exist — refusing to scan an empty scope."
        )
    seen = {}
    for path in guardlib.walk_files(src_root, (".java", ".kt")):
        with open(path, encoding="utf-8", errors="replace") as fh:
            text = fh.read()
        for literal in set(extract_literals(text)):
            if len(literal) >= MIN_LENGTH:
                seen.setdefault(literal, set()).add(path)
    return {lit: len(paths) for lit, paths in seen.items()
            if len(paths) >= MIN_FILES}


def main(argv):
    current = duplicated()

    if len(argv) > 1 and argv[1] == "--update":
        guardlib.write_keyed_counts(BASELINE, current, guard="string-dup-ratchet")
        print(f"Recorded {len(current)} duplicated literal(s) in "
              "scripts/guards/baselines/string-dup-counts.json")
        return 0

    code, messages = guardlib.check_keyed_counts(
        current, BASELINE, "duplicated string literal", UPDATE_CMD,
        grow_hint=[
            "Reuse or hoist the existing constant instead of copying the literal.",
            "Find the copies with:",
            "      grep -rn --fixed-strings '<literal>' "
            "PrebidMobile/PrebidMobile-core/src/main",
        ],
    )
    print("\n".join(messages))
    return code


if __name__ == "__main__":
    sys.exit(guardlib.cli(main)(sys.argv))
