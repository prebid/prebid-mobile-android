#!/usr/bin/env python3
"""Guard: deprecation-hygiene.

Deprecating API without telling the publisher what to use instead is a
recurring review ask. Java's @Deprecated annotation carries no message, so
the machine-checkable contract is documentation-side:

  Java   — the deprecated declaration's Javadoc block must contain a
           `@deprecated` tag with non-empty text (which is also what the
           published Javadoc renders as the deprecation notice).
  Kotlin — @Deprecated must carry a non-empty message string (or a
           ReplaceWith), the same contract the compiler surfaces to callers.

A Javadoc block that merely mentions a replacement in prose (no
`@deprecated` tag) still fails: Dokka/Javadoc render the tag, not the
prose, into the deprecation notice.

Ratchet: pre-existing violations are grandfathered per declaration in
allowlists/deprecation-hygiene.json (`path#member`; shrink-only — fix the
doc and delete the entry in the same PR). Overloads share one token, so an
entry goes stale only when every overload is fixed.
"""

import os
import re
import sys

_HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(os.path.dirname(os.path.dirname(_HERE)))
sys.path.insert(0, os.path.join(ROOT, "scripts", "guards", "lib"))
import guardlib  # noqa: E402

ALLOWLIST = os.path.join(ROOT, "scripts", "guards", "allowlists", "deprecation-hygiene.json")
MODULES = (
    "PrebidMobile-core",
    "PrebidMobile-gamEventHandlers",
    "PrebidMobile-admobAdapters",
    "PrebidMobile-maxAdapters",
    "PrebidMobile-nextGenEventHandlers",
)
_ANNOTATION_RE = re.compile(r"^\s*@\w+")
_JAVA_DEPRECATED_RE = re.compile(r"^\s*@Deprecated\s*$")
_KT_DEPRECATED_RE = re.compile(r"^\s*@Deprecated\b(\(?)")
_TAG_RE = re.compile(r"@deprecated\s+\S")
_TYPE_NAME_RE = re.compile(r"\b(?:class|interface|enum|object)\s+(\w+)")
_CALLABLE_NAME_RE = re.compile(r"(\w+)\s*\(")
_FIELD_NAME_RE = re.compile(r"(\w+)\s*[=;:]")
_KT_MESSAGE_RE = re.compile(r'"[^"]+"|ReplaceWith\s*\(')


def _member_name(lines, index):
    """Name of the declaration the annotation at `index` belongs to."""
    for follow in lines[index + 1:index + 8]:
        if _ANNOTATION_RE.match(follow) or not follow.strip():
            continue
        for regex in (_TYPE_NAME_RE, _CALLABLE_NAME_RE, _FIELD_NAME_RE):
            match = regex.search(follow)
            if match:
                return match.group(1)
        break
    return "(unknown)"


def _java_has_deprecated_tag(lines, index):
    """True when the Javadoc block above the annotation at `index` carries a
    non-empty @deprecated tag."""
    cursor = index - 1
    while cursor >= 0:
        line = lines[cursor].strip()
        if _ANNOTATION_RE.match(lines[cursor]) or not line:
            cursor -= 1
            continue
        if not line.endswith("*/"):
            return False
        block = []
        while cursor >= 0:
            block.append(lines[cursor])
            if "/**" in lines[cursor] or "/*" in lines[cursor]:
                break
            cursor -= 1
        text = "\n".join(reversed(block))
        # Strip the comment furniture (`*/`, leading `*`) so an empty tag
        # right before the block closer can't pass as tag text.
        text = re.sub(r"\*/", " ", text)
        text = re.sub(r"^\s*\*+", " ", text, flags=re.MULTILINE)
        return bool(_TAG_RE.search(text))
    return False


def _kotlin_has_message(lines, index):
    """True when the Kotlin @Deprecated annotation at `index` carries a
    non-empty message string or a ReplaceWith."""
    joined, depth_seen = [], False
    for line in lines[index:index + 10]:
        joined.append(line)
        if "(" in line:
            depth_seen = True
        text = "\n".join(joined)
        if depth_seen and text.count("(") <= text.count(")"):
            break
    text = "\n".join(joined)
    if "(" not in text.split("@Deprecated", 1)[1][:2]:
        return False  # bare @Deprecated — no message at all
    return bool(_KT_MESSAGE_RE.search(text))


def violations(root=ROOT):
    """Sorted `relpath#member` tokens for non-compliant deprecations."""
    found = set()
    scanned_any = False
    for module in MODULES:
        src_root = os.path.join(root, "PrebidMobile", module, "src", "main")
        if not os.path.isdir(src_root):
            continue
        scanned_any = True
        for path in guardlib.walk_files(src_root, (".java", ".kt")):
            rel = os.path.relpath(path, root)
            with open(path, encoding="utf-8", errors="replace") as fh:
                lines = fh.read().splitlines()
            is_kotlin = path.endswith(".kt")
            for index, line in enumerate(lines):
                if is_kotlin:
                    if not _KT_DEPRECATED_RE.match(line):
                        continue
                    ok = _kotlin_has_message(lines, index)
                else:
                    if not _JAVA_DEPRECATED_RE.match(line):
                        continue
                    ok = _java_has_deprecated_tag(lines, index)
                if not ok:
                    found.add(f"{rel}#{_member_name(lines, index)}")
    if not scanned_any:
        raise guardlib.GuardDataError(
            "no library module source roots found — refusing to scan an empty scope."
        )
    return sorted(found)


def main(_argv):
    found = violations()
    allow = guardlib.read_allowlist(ALLOWLIST)
    new, stale = guardlib.ratchet(found, allow)

    fail = False
    if new:
        print("FAIL: @Deprecated without a replacement note. Java: add a Javadoc")
        print("`@deprecated <use X instead>` tag; Kotlin: add a non-empty message")
        print("(or ReplaceWith) to the annotation:")
        for token in new:
            print(f"  {token}")
        fail = True
    if stale:
        print("FAIL: stale allowlist entries (declaration fixed or gone) — delete them")
        print("from scripts/guards/allowlists/deprecation-hygiene.json in this PR:")
        print("\n".join(guardlib.describe_entries(ALLOWLIST, stale)))
        fail = True

    if not fail:
        print(f"OK: every @Deprecated names its replacement "
              f"(allowlist: {len(allow)} grandfathered declaration(s)).")
    return 1 if fail else 0


if __name__ == "__main__":
    sys.exit(guardlib.cli(main)(sys.argv))
