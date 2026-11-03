#!/usr/bin/env python3
"""Guard: ortb-test-presence.

Every OpenRTB model class must be referenced by at least one unit test.
The wire format is the SDK's contract with Prebid Server; untested
encode/decode paths are where protocol regressions hide.

Models are discovered by PATH — every .java/.kt file under a
rendering/models/openrtb/ directory in PrebidMobile-core/src/main — so a
new model is checked automatically the moment it is added. If discovery
finds no model at all, the guard FAILS rather than passing on an empty
scope. The check is deliberately loose (class name referenced anywhere in
core's src/test); coverage QUALITY is review territory, this only pins
presence.

Pre-existing gaps are grandfathered in
allowlists/ortb-test-presence.json (shrink-only; a stale entry fails and
must be deleted in the same PR the test lands in).
"""

import os
import sys

_HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(os.path.dirname(os.path.dirname(_HERE)))
sys.path.insert(0, os.path.join(ROOT, "scripts", "guards", "lib"))
import guardlib  # noqa: E402

ALLOWLIST = os.path.join(ROOT, "scripts", "guards", "allowlists", "ortb-test-presence.json")
MODEL_DIR_MARKER = os.path.join("rendering", "models", "openrtb")
SRC_ROOT = os.path.join("PrebidMobile", "PrebidMobile-core", "src", "main")
TEST_ROOT = os.path.join("PrebidMobile", "PrebidMobile-core", "src", "test")


def discover_models(root=ROOT):
    """Sorted model class names under the openrtb models path."""
    names = []
    for path in guardlib.walk_files(os.path.join(root, SRC_ROOT), (".java", ".kt")):
        if MODEL_DIR_MARKER in path:
            names.append(os.path.splitext(os.path.basename(path))[0])
    return guardlib.c_sorted(names)


def load_test_corpus(root=ROOT):
    """Concatenated text of every test source file."""
    chunks = []
    for path in guardlib.walk_files(os.path.join(root, TEST_ROOT), (".java", ".kt")):
        with open(path, encoding="utf-8", errors="replace") as fh:
            chunks.append(fh.read())
    return "\n".join(chunks)


def untested_models(root=ROOT):
    models = discover_models(root)
    if not models:
        raise guardlib.GuardDataError(
            f"no OpenRTB models found under {SRC_ROOT} — the models moved? "
            "Update MODEL_DIR_MARKER in this check; an empty scope is never a pass."
        )
    corpus = load_test_corpus(root)
    return [name for name in models if name not in corpus], len(models)


def main(_argv):
    missing, total = untested_models()
    allow = guardlib.read_allowlist(ALLOWLIST)
    new, stale = guardlib.ratchet(missing, allow)

    fail = False
    if new:
        print("FAIL: OpenRTB model(s) with no unit-test reference — add a wire-format")
        print("test exercising the type (cite the OpenRTB spec section it encodes):")
        for name in new:
            print(f"  {name}")
        fail = True
    if stale:
        print("FAIL: stale allowlist entries (the model is now tested, or gone) — delete")
        print("them from scripts/guards/allowlists/ortb-test-presence.json in this PR:")
        print("\n".join(guardlib.describe_entries(ALLOWLIST, stale)))
        fail = True

    if not fail:
        print(f"OK: {total - len(allow)}/{total} OpenRTB models referenced by tests "
              f"(allowlist: {len(allow)} grandfathered gap(s)).")
    return 1 if fail else 0


if __name__ == "__main__":
    sys.exit(guardlib.cli(main)(sys.argv))
