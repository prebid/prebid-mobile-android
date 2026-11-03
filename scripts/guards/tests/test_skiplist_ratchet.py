"""Unit tests for the skiplist-ratchet probe."""

import os
import shutil
import sys
import tempfile
import unittest

_HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(_HERE, "..", "checks"))
sys.path.insert(0, os.path.join(_HERE, "..", "lib"))

import guardlib  # noqa: E402
import skiplist_ratchet  # noqa: E402


class SiteParsingTests(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.mkdtemp(prefix="skiplist-test-")
        self.addCleanup(shutil.rmtree, self.tmp)

    def parse(self, content, name="SomeTest.java"):
        path = os.path.join(self.tmp, name)
        with open(path, "w", encoding="utf-8") as fh:
            fh.write(content)
        return skiplist_ratchet._sites_in_file(path)

    def test_method_level_ignore(self):
        sites = self.parse(
            "class SomeTest {\n"
            "    @Ignore\n"
            "    public void testBroken() {}\n"
            "}\n")
        self.assertEqual(sites, ["SomeTest#testBroken"])

    def test_ignore_with_reason_and_interleaved_annotation(self):
        sites = self.parse(
            '@Ignore("flaky on CI")\n'
            "@Test\n"
            "public void testFlaky() throws Exception {}\n")
        self.assertEqual(sites, ["SomeTest#testFlaky"])

    def test_class_level_ignore(self):
        sites = self.parse(
            "@Ignore\n"
            "public class SomeTest {\n"
            "    @Test public void testA() {}\n"
            "}\n")
        self.assertEqual(sites, ["SomeTest"])

    def test_commented_out_ignore_does_not_count(self):
        sites = self.parse(
            "//@Ignore\n"
            "@Test\n"
            "public void testAlive() {}\n")
        self.assertEqual(sites, [])

    def test_kotlin_fun_declaration(self):
        sites = self.parse(
            "class SomeTest {\n"
            "    @Ignore\n"
            "    fun testBrokenKotlin() {}\n"
            "}\n", name="SomeTest.kt")
        self.assertEqual(sites, ["SomeTest#testBrokenKotlin"])

    def test_ignored_import_style_reference_not_matched(self):
        sites = self.parse("import org.junit.Ignore;\n")
        self.assertEqual(sites, [])


class ScopeTests(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.mkdtemp(prefix="skiplist-scope-")
        self.addCleanup(shutil.rmtree, self.tmp)

    def write(self, rel, content):
        path = os.path.join(self.tmp, rel)
        os.makedirs(os.path.dirname(path), exist_ok=True)
        with open(path, "w", encoding="utf-8") as fh:
            fh.write(content)

    def test_collects_across_modules_and_gradle(self):
        self.write("PrebidMobile/PrebidMobile-core/src/test/java/ATest.java",
                   "@Ignore\npublic void testX() {}\n")
        self.write("PrebidMobile/PrebidMobile-core/build.gradle",
                   "filter { excludeTestsMatching 'org.prebid.*SlowTest' }\n")
        entries = skiplist_ratchet.skipped_tests(self.tmp)
        self.assertEqual(entries,
                         ["ATest#testX", "gradle:org.prebid.*SlowTest"])

    def test_main_sources_out_of_scope(self):
        self.write("PrebidMobile/PrebidMobile-core/src/test/java/Empty.java", "\n")
        self.write("PrebidMobile/PrebidMobile-core/src/main/java/P.java",
                   "@Ignore\npublic void notATest() {}\n")
        self.assertEqual(skiplist_ratchet.skipped_tests(self.tmp), [])

    def test_no_test_roots_fails(self):
        with self.assertRaises(guardlib.GuardDataError):
            skiplist_ratchet.skipped_tests(self.tmp)


if __name__ == "__main__":
    unittest.main()
