"""Unit tests for the string-dup-ratchet extractor and probe."""

import os
import shutil
import sys
import tempfile
import unittest

_HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(_HERE, "..", "checks"))
sys.path.insert(0, os.path.join(_HERE, "..", "lib"))

import guardlib  # noqa: E402
import string_dup_ratchet  # noqa: E402


class ExtractorTests(unittest.TestCase):
    def extract(self, text):
        return string_dup_ratchet.extract_literals(text)

    def test_plain_literal(self):
        self.assertEqual(self.extract('String s = "hello world";'), ["hello world"])

    def test_escaped_quote_does_not_end_literal(self):
        self.assertEqual(self.extract(r'String s = "say \"hi\"";'), [r'say \"hi\"'])

    def test_line_comment_stripped(self):
        self.assertEqual(self.extract('// not "in code"\nint x;'), [])

    def test_block_comment_stripped(self):
        self.assertEqual(
            self.extract('/* license "Apache License" header */\nint x;'), [])

    def test_char_literal_quote_does_not_desync(self):
        self.assertEqual(
            self.extract("char q = '\"'; String s = \"real one\";"), ["real one"])

    def test_text_block_skipped(self):
        self.assertEqual(
            self.extract('String s = """\nmulti "line" body\n""";\nString t = "kept";'),
            ["kept"])

    def test_comment_marker_inside_literal_kept(self):
        self.assertEqual(
            self.extract('String url = "https://prebid.org";'),
            ["https://prebid.org"])


class DuplicatedTests(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.mkdtemp(prefix="stringdup-test-")
        self.addCleanup(shutil.rmtree, self.tmp)
        self.src = os.path.join(self.tmp, string_dup_ratchet.SCAN_ROOT)
        os.makedirs(self.src)

    def write(self, name, content):
        with open(os.path.join(self.src, name), "w", encoding="utf-8") as fh:
            fh.write(content)

    def test_threshold_is_three_distinct_files(self):
        for name in ("A.java", "B.java"):
            self.write(name, 'String s = "duplicated-literal";')
        self.assertEqual(string_dup_ratchet.duplicated(self.tmp), {})
        self.write("C.java", 'String s = "duplicated-literal";')
        self.assertEqual(string_dup_ratchet.duplicated(self.tmp),
                         {"duplicated-literal": 3})

    def test_repeats_within_one_file_count_once(self):
        self.write("A.java", 'String a = "same-literal"; String b = "same-literal";')
        self.write("B.java", 'String s = "same-literal";')
        self.write("C.java", 'String s = "same-literal";')
        self.assertEqual(string_dup_ratchet.duplicated(self.tmp),
                         {"same-literal": 3})

    def test_short_literals_ignored(self):
        for name in ("A.java", "B.java", "C.java"):
            self.write(name, 'String s = "short";')  # 5 chars < MIN_LENGTH
        self.assertEqual(string_dup_ratchet.duplicated(self.tmp), {})

    def test_missing_scan_root_fails(self):
        with self.assertRaises(guardlib.GuardDataError):
            string_dup_ratchet.duplicated(self.tmp + "-absent")


if __name__ == "__main__":
    unittest.main()
