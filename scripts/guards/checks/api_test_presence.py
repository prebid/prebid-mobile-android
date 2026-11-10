#!/usr/bin/env python3
"""Guard: api-test-presence.

Every public type in the committed API baseline (api/*.txt) must be
referenced by at least one unit test. ortb-test-presence pins this for the
OpenRTB models; this guard extends the same contract to the whole public
surface — an untested public type is either missing a test or shouldn't be
public.

The type list comes from the SAME committed signature files the
public-api-baseline guard checks, so the two can never disagree about what
is public. The reference check is deliberately loose (simple type name
anywhere in any module's test sources); coverage quality is review
territory. An empty type list fails rather than passing.

Pre-existing gaps are grandfathered in
allowlists/api-test-presence.json (shrink-only; entries are qualified type
names — fix by adding a test and deleting the entry in the same PR, or by
narrowing the type out of the public API).
"""

import os
import re
import sys

_HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(os.path.dirname(os.path.dirname(_HERE)))
sys.path.insert(0, os.path.join(ROOT, "scripts", "guards", "lib"))
import apisig  # noqa: E402
import guardlib  # noqa: E402

ALLOWLIST = os.path.join(ROOT, "scripts", "guards", "allowlists", "api-test-presence.json")
API_DIR = os.path.join(ROOT, "api")
MODULES = (
    "PrebidMobile-core",
    "PrebidMobile-gamEventHandlers",
    "PrebidMobile-admobAdapters",
    "PrebidMobile-maxAdapters",
    "PrebidMobile-nextGenEventHandlers",
)
TEST_MODULES = MODULES + ("test-utils",)


def load_test_corpus(root=ROOT):
    """Concatenated text of every module's test sources."""
    chunks = []
    for module in TEST_MODULES:
        test_root = os.path.join(root, "PrebidMobile", module, "src", "test")
        if not os.path.isdir(test_root):
            continue
        for path in guardlib.walk_files(test_root, (".java", ".kt")):
            with open(path, encoding="utf-8", errors="replace") as fh:
                chunks.append(fh.read())
    return "\n".join(chunks)


def untested_types(root=ROOT, api_dir=None):
    """(sorted untested qualified names, total count of public types)."""
    api_dir = api_dir or os.path.join(root, "api")
    types = []
    for module in MODULES:
        path = os.path.join(api_dir, f"{module}.txt")
        try:
            types.extend(apisig.public_types(path))
        except apisig.SignatureError as exc:
            raise guardlib.GuardDataError(
                f"{exc} — regenerate with ./scripts/guards/run-guards.sh "
                "--update-api-baseline")
    if not types:
        raise guardlib.GuardDataError(
            "no public types found in api/*.txt — an empty surface is never a pass."
        )
    corpus = load_test_corpus(root)
    names = {}
    for qualified, _kind in types:
        names.setdefault(apisig.simple_name(qualified), []).append(qualified)
    missing = []
    for name, qualified_list in names.items():
        if not re.search(r"\b%s\b" % re.escape(name), corpus):
            missing.extend(qualified_list)
    return sorted(set(missing)), len(set(q for q, _ in types))


def main(_argv):
    missing, total = untested_types()
    allow = guardlib.read_allowlist(ALLOWLIST)
    new, stale = guardlib.ratchet(missing, allow)

    fail = False
    if new:
        print("FAIL: public type(s) with no test reference — add a test exercising the")
        print("type, or question whether it should be public at all:")
        for name in new:
            print(f"  {name}")
        fail = True
    if stale:
        print("FAIL: stale allowlist entries (type now tested, or no longer public) —")
        print("delete them from scripts/guards/allowlists/api-test-presence.json:")
        print("\n".join(guardlib.describe_entries(ALLOWLIST, stale)))
        fail = True

    if not fail:
        print(f"OK: {total - len(allow)}/{total} public types referenced by tests "
              f"(allowlist: {len(allow)} grandfathered gap(s)).")
    return 1 if fail else 0


if __name__ == "__main__":
    sys.exit(guardlib.cli(main)(sys.argv))
