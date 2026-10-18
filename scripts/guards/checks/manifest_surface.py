#!/usr/bin/env python3
"""Guard: manifest-surface.

An ad SDK's manifest merges into every publisher's app. A silently added
permission changes their Play-Store data-safety story; a component that
becomes exported changes their attack surface; a <queries> entry changes
what their app can see. Today's surface is small and clean — exactly the
moment to freeze it.

The merged surface across every shipped module's src/main manifest is a
committed lockfile (baselines/manifest-surface.json):

  permission:<name>            uses-permission / uses-permission-sdk-23
  feature:<name>               uses-feature (with required flag)
  queries:<tag>:<name>         children of <queries>
  exported:<tag>:<name>        components with android:exported="true"
  uses-library:<name>          with required flag

ANY change — addition or removal — fails until the baseline is
regenerated in the same PR, making the manifest diff an explicit,
reviewable artifact:

  ./scripts/guards/run-guards.sh --update-manifest-baseline
"""

import os
import sys
import xml.etree.ElementTree as ET

_HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(os.path.dirname(os.path.dirname(_HERE)))
sys.path.insert(0, os.path.join(ROOT, "scripts", "guards", "lib"))
import guardlib  # noqa: E402

BASELINE = os.path.join(ROOT, "scripts", "guards", "baselines", "manifest-surface.json")
UPDATE_CMD = "./scripts/guards/run-guards.sh --update-manifest-baseline"
MODULES = (
    "PrebidMobile-core",
    "PrebidMobile-gamEventHandlers",
    "PrebidMobile-admobAdapters",
    "PrebidMobile-maxAdapters",
    "PrebidMobile-nextGenEventHandlers",
)
ANDROID_NS = "http://schemas.android.com/apk/res/android"
COMPONENT_TAGS = ("activity", "service", "receiver", "provider")


def _attr(element, name, default=None):
    return element.get(f"{{{ANDROID_NS}}}{name}", default)


def _manifest_entries(path):
    entries = []
    tree = ET.parse(path)
    root = tree.getroot()
    for element in root.iter():
        tag = element.tag
        if tag in ("uses-permission", "uses-permission-sdk-23"):
            suffix = " (sdk-23)" if tag.endswith("sdk-23") else ""
            entries.append(f"permission:{_attr(element, 'name')}{suffix}")
        elif tag == "uses-feature":
            name = _attr(element, "name") or f"glEsVersion={_attr(element, 'glEsVersion')}"
            entries.append(
                f"feature:{name} required={_attr(element, 'required', 'true')}")
        elif tag == "uses-library":
            entries.append(
                f"uses-library:{_attr(element, 'name')} "
                f"required={_attr(element, 'required', 'true')}")
        elif tag == "queries":
            for child in element:
                child_tag = child.tag
                name = (_attr(child, "name")
                        or child.findtext(".") or child_tag)
                # intent filters inside <queries> summarize by action name
                if child_tag == "intent":
                    actions = [_attr(a, "name") for a in child.iter("action")]
                    name = ",".join(filter(None, actions)) or "intent"
                entries.append(f"queries:{child_tag}:{name}")
        elif tag in COMPONENT_TAGS:
            if _attr(element, "exported") == "true":
                entries.append(f"exported:{tag}:{_attr(element, 'name')}")
    return entries


def surface(root=ROOT):
    """Sorted, deduplicated merged manifest surface of shipped modules."""
    entries = []
    scanned_any = False
    for module in MODULES:
        path = os.path.join(root, "PrebidMobile", module, "src", "main",
                            "AndroidManifest.xml")
        if not os.path.exists(path):
            continue
        scanned_any = True
        try:
            entries.extend(_manifest_entries(path))
        except ET.ParseError as exc:
            raise guardlib.GuardDataError(
                f"cannot parse {os.path.relpath(path, root)} ({exc}) — "
                "a broken manifest is not an empty surface."
            )
    if not scanned_any:
        raise guardlib.GuardDataError(
            "no module manifests found — refusing to snapshot an empty surface."
        )
    return guardlib.c_sorted(entries)


def main(argv):
    current = surface()

    if len(argv) > 1 and argv[1] == "--update":
        guardlib.write_entries(BASELINE, current, guard="manifest-surface")
        print(f"Recorded {len(current)} manifest-surface entr(ies) in "
              "scripts/guards/baselines/manifest-surface.json")
        return 0

    recorded = guardlib.read_entries(BASELINE, UPDATE_CMD)
    diff = guardlib.lockfile_diff(recorded, current)
    if diff:
        print("FAIL: the merged manifest surface changed. Every publisher inherits this")
        print("surface — the change must be an explicit, reviewed baseline diff:")
        print("\n".join(f"  {line}" for line in diff))
        print("If intentional, regenerate and commit in the same PR:")
        print("      " + UPDATE_CMD)
        return 1

    print(f"OK: manifest surface unchanged ({len(current)} entr(ies)).")
    return 0


if __name__ == "__main__":
    sys.exit(guardlib.cli(main)(sys.argv))
