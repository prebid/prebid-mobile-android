"""Unit tests for the logging-hygiene probe."""

import os
import shutil
import sys
import tempfile
import unittest

_HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(_HERE, "..", "checks"))
sys.path.insert(0, os.path.join(_HERE, "..", "lib"))

import guardlib  # noqa: E402
import logging_hygiene  # noqa: E402


class MatchLineTests(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.mkdtemp(prefix="logging-test-")
        self.addCleanup(shutil.rmtree, self.tmp)

    def probe(self, content, name="A.java"):
        path = os.path.join(self.tmp, name)
        with open(path, "w", encoding="utf-8") as fh:
            fh.write(content)
        return logging_hygiene._match_lines(path)

    def test_raw_log_calls_flagged(self):
        hits = self.probe(
            'Log.d(TAG, "x");\nLog.wtf(TAG, "y");\nandroid.util.Log.e(TAG, "z");\n')
        self.assertEqual([h[0] for h in hits], [1, 2, 3])

    def test_get_stack_trace_string_allowed(self):
        # a pure formatter routed through LogUtil is the sanctioned pattern
        hits = self.probe('LogUtil.error(TAG, Log.getStackTraceString(e));\n')
        self.assertEqual(hits, [])

    def test_log_level_constants_allowed(self):
        hits = self.probe("int level = Log.VERBOSE;\n")
        self.assertEqual(hits, [])

    def test_system_out_and_print_stack_trace_flagged(self):
        hits = self.probe(
            'System.out.println("x");\nSystem.err.print("y");\ne.printStackTrace();\n')
        self.assertEqual(len(hits), 3)

    def test_comment_lines_exempt(self):
        hits = self.probe('// Log.d(TAG, "commented out");\n * Log.e(TAG, "doc");\n')
        self.assertEqual(hits, [])

    def test_identifier_containing_log_not_flagged(self):
        hits = self.probe('myLog.d("custom logger");\nLogUtil.debug(TAG, "x");\n')
        self.assertEqual(hits, [])


class ViolationScopeTests(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.mkdtemp(prefix="logging-scope-")
        self.addCleanup(shutil.rmtree, self.tmp)

    def write(self, rel, content):
        path = os.path.join(self.tmp, rel)
        os.makedirs(os.path.dirname(path), exist_ok=True)
        with open(path, "w", encoding="utf-8") as fh:
            fh.write(content)

    def test_exempt_facade_and_out_of_scope_files_skipped(self):
        self.write(logging_hygiene.EXEMPT_FILES[0], 'Log.println(p, TAG, "facade");\n')
        self.write("PrebidMobile/PrebidMobile-core/src/main/java/B.java",
                   'Log.d(TAG, "x");\n')
        self.write("PrebidMobile/PrebidMobile-core/src/test/java/T.java",
                   'Log.d(TAG, "tests are out of scope");\n')
        found = logging_hygiene.violations(self.tmp)
        self.assertEqual(
            list(found), ["PrebidMobile/PrebidMobile-core/src/main/java/B.java"])

    def test_kotlin_files_scanned(self):
        self.write(
            "PrebidMobile/PrebidMobile-nextGenEventHandlers/src/main/java/W.kt",
            'Log.e(TAG, "kotlin")\n')
        found = logging_hygiene.violations(self.tmp)
        self.assertEqual(len(found), 1)

    def test_no_module_roots_fails_not_empty_pass(self):
        with self.assertRaises(guardlib.GuardDataError):
            logging_hygiene.violations(self.tmp + "-absent")


if __name__ == "__main__":
    unittest.main()
