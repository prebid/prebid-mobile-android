"""Unit tests for the fixme-ratchet probe."""

import os
import shutil
import sys
import tempfile
import unittest

_HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(_HERE, "..", "checks"))
sys.path.insert(0, os.path.join(_HERE, "..", "lib"))

import fixme_ratchet  # noqa: E402
import guardlib  # noqa: E402


class FixmeCountTests(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.mkdtemp(prefix="fixme-test-")
        self.addCleanup(shutil.rmtree, self.tmp)
        self.src = os.path.join(self.tmp, fixme_ratchet.SCAN_ROOT)
        os.makedirs(self.src)

    def write(self, name, content):
        with open(os.path.join(self.src, name), "w", encoding="utf-8") as fh:
            fh.write(content)

    def test_counts_markers_in_java_and_kotlin(self):
        self.write("A.java", "// TODO: later\nint x;\n/* FIXME broken */\n")
        self.write("B.kt", "// todo lowercase counts too\n")
        self.assertEqual(fixme_ratchet.count(self.tmp), 3)

    def test_marker_must_be_in_a_comment(self):
        self.write("A.java", 'String s = "TODO";\nint todoCount = 0;\n')
        self.assertEqual(fixme_ratchet.count(self.tmp), 0)

    def test_files_outside_scope_ignored(self):
        test_dir = os.path.join(
            self.tmp, "PrebidMobile", "PrebidMobile-core", "src", "test")
        os.makedirs(test_dir)
        with open(os.path.join(test_dir, "T.java"), "w") as fh:
            fh.write("// TODO in a test is out of scope\n")
        self.assertEqual(fixme_ratchet.count(self.tmp), 0)

    def test_missing_scan_root_fails_not_zero(self):
        empty = tempfile.mkdtemp(prefix="fixme-empty-")
        self.addCleanup(shutil.rmtree, empty)
        with self.assertRaises(guardlib.GuardDataError):
            fixme_ratchet.count(empty)


if __name__ == "__main__":
    unittest.main()
