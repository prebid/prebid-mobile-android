#!/usr/bin/env python3
"""Guard: public-api-baseline.

The public API surface of every published module is a committed, reviewable
artifact: one Metalava signature file per module in api/ (the lockfile
pattern — one +/- line per moved declaration). Any surface change fails
until the baseline is regenerated in the same PR:

  ./scripts/guards/run-guards.sh --update-api-baseline

Metalava (metalava.gradle) is PSI-based and reads the real compile
classpath, so it sees implicitly-public members, nested types, enum
constants, and annotation-driven visibility — the things a hand-written
source parser (the iOS approach) cannot.

Environment contract: this guard needs Gradle + a JDK + the Android SDK.
When generation fails locally the guard reports SKIPPED (exit 2) with the
build output — CI, which always has the toolchain, is authoritative and
treats any generation failure as FAIL (GUARDS_REQUIRE_BUILD=1). A failed
generation is never treated as an empty surface.

Downstream: api_doc_coverage.py and api_test_presence.py read the
committed api/*.txt files as their definition of "public", so the three
guards can never disagree about what the surface is.
"""

import os
import shutil
import subprocess
import sys
import tempfile

_HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(os.path.dirname(os.path.dirname(_HERE)))
sys.path.insert(0, os.path.join(ROOT, "scripts", "guards", "lib"))
import guardlib  # noqa: E402

API_DIR = os.path.join(ROOT, "api")
UPDATE_CMD = "./scripts/guards/run-guards.sh --update-api-baseline"
MODULES = (
    "PrebidMobile-core",
    "PrebidMobile-gamEventHandlers",
    "PrebidMobile-admobAdapters",
    "PrebidMobile-maxAdapters",
    "PrebidMobile-nextGenEventHandlers",
)


def generate(output_dir, root=ROOT):
    """Run the Metalava Gradle task into output_dir.

    Returns (ok, output_tail)."""
    gradlew = os.path.join(root, "gradlew")
    result = subprocess.run(
        [gradlew, "-q", "generateApiSignatures",
         f"-PmetalavaOutputDir={output_dir}"],
        cwd=root, capture_output=True, text=True, check=False,
    )
    tail = "\n".join((result.stdout + "\n" + result.stderr).strip().splitlines()[-15:])
    return result.returncode == 0, tail


def compare(baseline_dir, current_dir, modules=MODULES):
    """{module: [diff lines]} for every module whose signature drifted.

    A missing file on either side is drift, never silence."""
    drift = {}
    for module in modules:
        name = f"{module}.txt"
        baseline_path = os.path.join(baseline_dir, name)
        current_path = os.path.join(current_dir, name)
        if not os.path.exists(current_path):
            drift[module] = [f"generation produced no {name}"]
            continue
        if not os.path.exists(baseline_path):
            drift[module] = [f"api/{name} is missing — commit the baseline"]
            continue
        with open(baseline_path, encoding="utf-8") as fh:
            baseline_lines = fh.read().splitlines()
        with open(current_path, encoding="utf-8") as fh:
            current_lines = fh.read().splitlines()
        lines = guardlib.lockfile_diff(baseline_lines, current_lines)
        if lines:
            drift[module] = lines
    return drift


def _environment_failure(tail):
    """SKIPPED locally, FAIL in CI (GUARDS_REQUIRE_BUILD=1)."""
    if os.environ.get("GUARDS_REQUIRE_BUILD") == "1":
        print("FAIL: Metalava signature generation failed — a failed generation is")
        print("never an unchanged surface. Build output:")
        print(tail)
        return 1
    print("SKIPPED: could not run the Metalava Gradle task locally (JDK/Android SDK")
    print("needed). CI is authoritative. Last build output lines:")
    print(tail)
    return guardlib.EXIT_SKIPPED


def main(argv):
    if len(argv) > 1 and argv[1] == "--update":
        ok, tail = generate(API_DIR)
        if not ok:
            print("FAIL: Metalava signature generation failed:")
            print(tail)
            return 1
        print("Regenerated api/*.txt — review and commit the diff in the same PR.")
        return 0

    temp_dir = tempfile.mkdtemp(prefix="metalava-api-")
    try:
        ok, tail = generate(temp_dir)
        if not ok:
            return _environment_failure(tail)
        drift = compare(API_DIR, temp_dir)
    finally:
        shutil.rmtree(temp_dir, ignore_errors=True)

    if drift:
        print("FAIL: the public API surface differs from the committed baseline in api/.")
        print("If the change is intentional, regenerate and commit in the same PR:")
        print("      " + UPDATE_CMD)
        for module in sorted(drift):
            print(f"  {module}:")
            for line in drift[module][:20]:
                print(f"    {line}")
        return 1

    print(f"OK: public API surface matches api/ ({len(MODULES)} module baselines).")
    return 0


if __name__ == "__main__":
    sys.exit(guardlib.cli(main)(sys.argv))
