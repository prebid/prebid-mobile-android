"""Unit tests for the api-baseline comparison logic (the Gradle/Metalava
invocation itself is exercised by running the guard for real)."""

import os
import shutil
import sys
import tempfile
import unittest

_HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(_HERE, "..", "checks"))
sys.path.insert(0, os.path.join(_HERE, "..", "lib"))

import api_baseline  # noqa: E402

SIGNATURE = """// Signature format: 4.0
package org.prebid.mobile {
  public class AdSize {
    ctor public AdSize(int, int);
    method public int getHeight();
  }
}
"""


class CompareTests(unittest.TestCase):
    def setUp(self):
        self.baseline = tempfile.mkdtemp(prefix="api-baseline-")
        self.current = tempfile.mkdtemp(prefix="api-current-")
        self.addCleanup(shutil.rmtree, self.baseline)
        self.addCleanup(shutil.rmtree, self.current)

    def write(self, where, module, content=SIGNATURE):
        with open(os.path.join(where, f"{module}.txt"), "w") as fh:
            fh.write(content)

    def test_identical_signatures_no_drift(self):
        self.write(self.baseline, "M")
        self.write(self.current, "M")
        self.assertEqual(api_baseline.compare(self.baseline, self.current, ("M",)), {})

    def test_changed_member_is_drift(self):
        self.write(self.baseline, "M")
        self.write(self.current, "M",
                   SIGNATURE.replace("getHeight", "getHeightDp"))
        drift = api_baseline.compare(self.baseline, self.current, ("M",))
        self.assertIn("M", drift)
        joined = "\n".join(drift["M"])
        self.assertIn("-    method public int getHeight();", joined)
        self.assertIn("+    method public int getHeightDp();", joined)

    def test_missing_committed_baseline_is_drift_not_silence(self):
        self.write(self.current, "M")
        drift = api_baseline.compare(self.baseline, self.current, ("M",))
        self.assertIn("missing", drift["M"][0])

    def test_generation_producing_no_file_is_drift_not_silence(self):
        self.write(self.baseline, "M")
        drift = api_baseline.compare(self.baseline, self.current, ("M",))
        self.assertIn("no M.txt", drift["M"][0])

    def test_untracked_modules_ignored(self):
        self.write(self.baseline, "M")
        self.write(self.current, "M")
        self.write(self.current, "Extra")  # not in the module list
        self.assertEqual(api_baseline.compare(self.baseline, self.current, ("M",)), {})


class EnvironmentContractTests(unittest.TestCase):
    def test_ci_treats_generation_failure_as_fail(self):
        os.environ["GUARDS_REQUIRE_BUILD"] = "1"
        try:
            import contextlib
            import io
            with contextlib.redirect_stdout(io.StringIO()):
                code = api_baseline._environment_failure("boom")
            self.assertEqual(code, 1)
        finally:
            del os.environ["GUARDS_REQUIRE_BUILD"]

    def test_local_generation_failure_is_advisory_skip(self):
        os.environ.pop("GUARDS_REQUIRE_BUILD", None)
        import contextlib
        import io
        with contextlib.redirect_stdout(io.StringIO()):
            code = api_baseline._environment_failure("boom")
        self.assertEqual(code, api_baseline.guardlib.EXIT_SKIPPED)


if __name__ == "__main__":
    unittest.main()
