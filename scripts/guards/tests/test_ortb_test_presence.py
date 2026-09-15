"""Unit tests for the ortb-test-presence probe."""

import os
import shutil
import sys
import tempfile
import unittest

_HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(_HERE, "..", "checks"))
sys.path.insert(0, os.path.join(_HERE, "..", "lib"))

import guardlib  # noqa: E402
import ortb_test_presence  # noqa: E402


class OrtbPresenceTests(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.mkdtemp(prefix="ortb-test-")
        self.addCleanup(shutil.rmtree, self.tmp)
        self.model_dir = os.path.join(
            self.tmp, ortb_test_presence.SRC_ROOT, "java", "org", "prebid",
            "mobile", "rendering", "models", "openrtb", "bidRequests")
        self.test_dir = os.path.join(self.tmp, ortb_test_presence.TEST_ROOT, "java")
        os.makedirs(self.model_dir)
        os.makedirs(self.test_dir)

    def add_model(self, name):
        with open(os.path.join(self.model_dir, name + ".java"), "w") as fh:
            fh.write(f"public class {name} {{}}\n")

    def add_test(self, content, name="SomeTest.java"):
        with open(os.path.join(self.test_dir, name), "w") as fh:
            fh.write(content)

    def test_referenced_model_passes(self):
        self.add_model("Imp")
        self.add_test("class SomeTest { Imp imp = new Imp(); }\n")
        missing, total = ortb_test_presence.untested_models(self.tmp)
        self.assertEqual((missing, total), ([], 1))

    def test_unreferenced_model_reported(self):
        self.add_model("Imp")
        self.add_model("Banner")
        self.add_test("class SomeTest { Imp imp; }\n")
        missing, total = ortb_test_presence.untested_models(self.tmp)
        self.assertEqual((missing, total), (["Banner"], 2))

    def test_duplicate_basenames_collapse_to_one_name(self):
        self.add_model("Geo")
        other_dir = os.path.join(os.path.dirname(self.model_dir), "geo")
        os.makedirs(other_dir)
        with open(os.path.join(other_dir, "Geo.java"), "w") as fh:
            fh.write("public class Geo {}\n")
        self.assertEqual(ortb_test_presence.discover_models(self.tmp), ["Geo"])

    def test_source_outside_openrtb_path_not_a_model(self):
        outside = os.path.join(self.tmp, ortb_test_presence.SRC_ROOT, "java", "other")
        os.makedirs(outside)
        with open(os.path.join(outside, "NotAModel.java"), "w") as fh:
            fh.write("public class NotAModel {}\n")
        self.assertEqual(ortb_test_presence.discover_models(self.tmp), [])

    def test_empty_discovery_fails_not_passes(self):
        with self.assertRaises(guardlib.GuardDataError):
            ortb_test_presence.untested_models(self.tmp)


if __name__ == "__main__":
    unittest.main()
