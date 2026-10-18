"""Unit tests for the lint diff-scoper's report/diff intersection logic."""

import os
import shutil
import sys
import tempfile
import unittest

_HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(_HERE, ".."))

import diff_scope  # noqa: E402

REPORT = """<?xml version="1.0" encoding="UTF-8"?>
<issues format="6">
  <issue id="NewApi" severity="Error" message="Call requires API 26">
    <location file="{root}/PrebidMobile/PrebidMobile-core/src/main/java/A.java" line="10" column="5"/>
  </issue>
  <issue id="DefaultLocale" severity="Warning" message="Implicit default locale">
    <location file="PrebidMobile/PrebidMobile-core/src/main/java/B.java" line="20" column="1"/>
  </issue>
  <issue id="LintNote" severity="Informational" message="note only">
    <location file="PrebidMobile/PrebidMobile-core/src/main/java/A.java" line="10" column="1"/>
  </issue>
  <issue id="NoLine" severity="Error" message="whole-file issue">
    <location file="PrebidMobile/PrebidMobile-core/src/main/java/C.java"/>
  </issue>
</issues>
"""


class FindingsInReportTests(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.mkdtemp(prefix="diffscope-test-")
        self.addCleanup(shutil.rmtree, self.tmp)
        self.report = os.path.join(self.tmp, "lint-results-release.xml")
        with open(self.report, "w") as fh:
            fh.write(REPORT.format(root=diff_scope.ROOT))

    def test_only_findings_on_added_lines_reported(self):
        added = {("PrebidMobile/PrebidMobile-core/src/main/java/A.java", 10)}
        hits = diff_scope.findings_in_report(self.report, added)
        self.assertEqual(len(hits), 1)
        self.assertIn("NewApi", hits[0])

    def test_absolute_report_paths_normalized(self):
        # the report's absolute path must match the diff's relative path
        added = {("PrebidMobile/PrebidMobile-core/src/main/java/A.java", 10)}
        hits = diff_scope.findings_in_report(self.report, added)
        self.assertTrue(hits[0].startswith(
            "PrebidMobile/PrebidMobile-core/src/main/java/A.java:10:"))

    def test_untouched_lines_never_reported(self):
        added = {("PrebidMobile/PrebidMobile-core/src/main/java/A.java", 11),
                 ("PrebidMobile/PrebidMobile-core/src/main/java/B.java", 21)}
        self.assertEqual(diff_scope.findings_in_report(self.report, added), [])

    def test_warning_severity_gates(self):
        added = {("PrebidMobile/PrebidMobile-core/src/main/java/B.java", 20)}
        hits = diff_scope.findings_in_report(self.report, added)
        self.assertEqual(len(hits), 1)
        self.assertIn("DefaultLocale", hits[0])

    def test_informational_severity_never_gates(self):
        added = {("PrebidMobile/PrebidMobile-core/src/main/java/A.java", 10)}
        hits = diff_scope.findings_in_report(self.report, added)
        self.assertFalse(any("LintNote" in h for h in hits))

    def test_unparsable_report_raises(self):
        broken = os.path.join(self.tmp, "broken.xml")
        with open(broken, "w") as fh:
            fh.write("<issues unclosed")
        import xml.etree.ElementTree as ET
        with self.assertRaises(ET.ParseError):
            diff_scope.findings_in_report(broken, set())


class DiffParsingTests(unittest.TestCase):
    def test_added_lines_from_real_diff_format(self):
        # exercise the hunk-header arithmetic through a fake `git diff` output
        sample = (
            "diff --git PrebidMobile/A.java PrebidMobile/A.java\n"
            "+++ PrebidMobile/A.java\n"
            "@@ -5,0 +6,3 @@ class A {\n"
            "+one\n+two\n+three\n"
            "@@ -20 +24 @@ class A {\n"
            "+single\n"
        )
        path = None
        added = set()
        for line in sample.splitlines():
            if line.startswith("+++ "):
                path = line[4:].strip()
            elif line.startswith("@@ ") and path:
                new_part = line.split()[2]
                start, _, length = new_part.lstrip("+").partition(",")
                for offset in range(int(length) if length else 1):
                    added.add((path, int(start) + offset))
        self.assertEqual(added, {
            ("PrebidMobile/A.java", 6), ("PrebidMobile/A.java", 7),
            ("PrebidMobile/A.java", 8), ("PrebidMobile/A.java", 24)})


if __name__ == "__main__":
    unittest.main()
