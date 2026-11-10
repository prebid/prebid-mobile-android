"""Unit tests for the deprecation-hygiene probe."""

import os
import shutil
import sys
import tempfile
import unittest

_HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(_HERE, "..", "checks"))
sys.path.insert(0, os.path.join(_HERE, "..", "lib"))

import deprecation_hygiene  # noqa: E402


class ProbeTestCase(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.mkdtemp(prefix="deprecation-test-")
        self.addCleanup(shutil.rmtree, self.tmp)
        self.src = os.path.join(
            self.tmp, "PrebidMobile", "PrebidMobile-core", "src", "main", "java")
        os.makedirs(self.src)

    def scan(self, content, name="A.java"):
        with open(os.path.join(self.src, name), "w", encoding="utf-8") as fh:
            fh.write(content)
        return deprecation_hygiene.violations(self.tmp)


class JavaTests(ProbeTestCase):
    def test_tagged_javadoc_passes(self):
        found = self.scan(
            "/**\n"
            " * Old size setter.\n"
            " * @deprecated use {@link BannerParameters} instead.\n"
            " */\n"
            "@Deprecated\n"
            "public void addSize(AdSize size) {}\n")
        self.assertEqual(found, [])

    def test_untagged_javadoc_fails(self):
        # prose alone doesn't render as a deprecation notice
        found = self.scan(
            "/**\n * Should be replaced by BannerParameters.\n */\n"
            "@Deprecated\n"
            "public void addSize(AdSize size) {}\n")
        self.assertEqual(len(found), 1)
        self.assertTrue(found[0].endswith("#addSize"))

    def test_no_javadoc_fails(self):
        found = self.scan("@Deprecated\npublic void gone() {}\n")
        self.assertEqual(len(found), 1)

    def test_empty_tag_fails(self):
        found = self.scan(
            "/**\n * @deprecated\n */\n@Deprecated\npublic void gone() {}\n")
        self.assertEqual(len(found), 1)

    def test_interleaved_annotations_tolerated(self):
        found = self.scan(
            "/**\n * @deprecated use v2.\n */\n"
            "@Override\n@Deprecated\n@JavascriptInterface\n"
            "public void shouldUseCustomClose(String s) {}\n")
        self.assertEqual(found, [])

    def test_enum_constant_member_name(self):
        found = self.scan(
            "public enum AdPosition {\n"
            "    @Deprecated\n"
            "    LOCKED(2),\n"
            "}\n")
        self.assertEqual(len(found), 1)
        self.assertTrue(found[0].endswith("#LOCKED"))

    def test_overloads_share_one_token(self):
        found = self.scan(
            "@Deprecated\npublic void addSizes(AdSize... s) {}\n"
            "@Deprecated\npublic void addSizes(Set<AdSize> s) {}\n")
        self.assertEqual(len(found), 1)


class KotlinTests(ProbeTestCase):
    def test_message_passes(self):
        found = self.scan(
            '@Deprecated("Use fetchDemand(listener) instead")\n'
            "fun fetchDemand() {}\n", name="W.kt")
        self.assertEqual(found, [])

    def test_replace_with_passes(self):
        found = self.scan(
            '@Deprecated("old", ReplaceWith("newApi()"))\n'
            "fun oldApi() {}\n", name="W.kt")
        self.assertEqual(found, [])

    def test_bare_annotation_fails(self):
        found = self.scan("@Deprecated\nfun oldApi() {}\n", name="W.kt")
        self.assertEqual(len(found), 1)
        self.assertTrue(found[0].endswith("#oldApi"))

    def test_empty_message_fails(self):
        found = self.scan('@Deprecated("")\nfun oldApi() {}\n', name="W.kt")
        self.assertEqual(len(found), 1)


if __name__ == "__main__":
    unittest.main()
