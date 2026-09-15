"""Unit tests for the two guards that read the committed API baseline:
api-doc-coverage and api-test-presence, plus the shared apisig reader."""

import os
import shutil
import sys
import tempfile
import unittest

_HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(_HERE, "..", "checks"))
sys.path.insert(0, os.path.join(_HERE, "..", "lib"))

import api_doc_coverage  # noqa: E402
import api_test_presence  # noqa: E402
import apisig  # noqa: E402
import guardlib  # noqa: E402

SIGNATURE = """// Signature format: 4.0
package org.prebid.mobile {

  public class AdSize {
    ctor public AdSize(int, int);
  }

  public enum NativeImage.Type {
    enum_constant public static final org.prebid.mobile.NativeImage.Type ICON;
  }

  public abstract sealed class AdEvent {
  }

  public static final class AdEvent.Clicked extends org.prebid.mobile.AdEvent {
  }
}
"""


class ApisigTests(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.mkdtemp(prefix="apisig-test-")
        self.addCleanup(shutil.rmtree, self.tmp)
        self.path = os.path.join(self.tmp, "M.txt")
        with open(self.path, "w") as fh:
            fh.write(SIGNATURE)

    def test_public_types_include_nested_and_modified(self):
        names = [q for q, _ in apisig.public_types(self.path)]
        self.assertEqual(names, [
            "org.prebid.mobile.AdSize",
            "org.prebid.mobile.NativeImage.Type",
            "org.prebid.mobile.AdEvent",
            "org.prebid.mobile.AdEvent.Clicked",
        ])

    def test_member_lines_are_not_types(self):
        kinds = [k for _, k in apisig.public_types(self.path)]
        self.assertNotIn("ctor", kinds)
        self.assertEqual(len(kinds), 4)

    def test_missing_file_raises(self):
        with self.assertRaises(apisig.SignatureError):
            apisig.public_types(os.path.join(self.tmp, "absent.txt"))

    def test_outer_source_candidates_for_nested_type(self):
        candidates = apisig.outer_source_candidates(
            "org.prebid.mobile.NativeImage.Type", "org.prebid.mobile")
        self.assertEqual(candidates[0],
                         os.path.join("org", "prebid", "mobile", "NativeImage.java"))


class DocCoverageTests(unittest.TestCase):
    def count(self, content, name="A.java"):
        tmp = tempfile.mkdtemp(prefix="doccov-test-")
        self.addCleanup(shutil.rmtree, tmp)
        path = os.path.join(tmp, name)
        with open(path, "w") as fh:
            fh.write(content)
        return api_doc_coverage.undocumented_in_file(path)

    def test_documented_java_declaration_not_counted(self):
        self.assertEqual(self.count(
            "/**\n * The ad size.\n */\npublic class AdSize {\n"
            "    /** Width in dp. */\n    @NonNull\n    public int getWidth() {}\n}\n"
        ), 0)

    def test_undocumented_java_declarations_counted(self):
        self.assertEqual(self.count(
            "public class AdSize {\n    public int getWidth() {}\n}\n"), 2)

    def test_non_public_java_not_counted(self):
        self.assertEqual(self.count(
            "class Internal {\n    void helper() {}\n    private int x;\n}\n"), 0)

    def test_javadoc_example_line_not_counted(self):
        self.assertEqual(self.count(
            "/**\n * Example:\n * public void fake() {}\n */\npublic class A {}\n"), 0)

    def test_kotlin_public_by_default_counted(self):
        self.assertEqual(self.count(
            "class Wrapper {\n    fun handle() {}\n    private fun helper() {}\n"
            "    override fun toString() = \"\"\n    internal val x = 1\n}\n",
            name="W.kt"), 2)  # class + fun handle

    def test_kotlin_documented_not_counted(self):
        self.assertEqual(self.count(
            "/**\n * Wrapper.\n */\nclass Wrapper\n", name="W.kt"), 0)


class TestPresenceTests(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.mkdtemp(prefix="presence-test-")
        self.addCleanup(shutil.rmtree, self.tmp)
        self.api_dir = os.path.join(self.tmp, "api")
        os.makedirs(self.api_dir)
        for module in api_test_presence.MODULES:
            with open(os.path.join(self.api_dir, f"{module}.txt"), "w") as fh:
                fh.write("// Signature format: 4.0\n")
        with open(os.path.join(self.api_dir, "PrebidMobile-core.txt"), "w") as fh:
            fh.write(SIGNATURE)
        self.test_dir = os.path.join(
            self.tmp, "PrebidMobile", "PrebidMobile-core", "src", "test", "java")
        os.makedirs(self.test_dir)

    def add_test(self, content):
        with open(os.path.join(self.test_dir, "SomeTest.java"), "w") as fh:
            fh.write(content)

    def test_referenced_types_pass_unreferenced_reported(self):
        self.add_test("class SomeTest { AdSize s; Type t; }\n")
        missing, total = api_test_presence.untested_types(self.tmp, self.api_dir)
        self.assertEqual(total, 4)
        self.assertEqual(missing, [
            "org.prebid.mobile.AdEvent",
            "org.prebid.mobile.AdEvent.Clicked",
        ])

    def test_word_boundary_no_substring_credit(self):
        # "AdSizeHelper" must not count as a reference to AdSize
        self.add_test("class SomeTest { AdSizeHelper h; Type t; AdEvent e; "
                      "Clicked c; }\n")
        missing, _ = api_test_presence.untested_types(self.tmp, self.api_dir)
        self.assertEqual(missing, ["org.prebid.mobile.AdSize"])

    def test_empty_surface_fails(self):
        for module in api_test_presence.MODULES:
            with open(os.path.join(self.api_dir, f"{module}.txt"), "w") as fh:
                fh.write("// Signature format: 4.0\n")
        with self.assertRaises(guardlib.GuardDataError):
            api_test_presence.untested_types(self.tmp, self.api_dir)


if __name__ == "__main__":
    unittest.main()
