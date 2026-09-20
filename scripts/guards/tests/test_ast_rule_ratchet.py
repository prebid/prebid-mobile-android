"""Unit tests for the ast-rule-ratchet stream parsing (the ast-grep
invocation itself is exercised by running the guard for real)."""

import os
import sys
import unittest

_HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(_HERE, "..", "checks"))
sys.path.insert(0, os.path.join(_HERE, "..", "lib"))

import ast_rule_ratchet  # noqa: E402


class ParseStreamTests(unittest.TestCase):
    RULES = ("empty-catch", "static-context-leak")

    def stream(self, *rule_ids):
        return "\n".join('{"ruleId":"%s","file":"F.java"}' % r for r in rule_ids)

    def test_counts_per_rule(self):
        got = ast_rule_ratchet.parse_stream(
            self.stream("empty-catch", "static-context-leak", "empty-catch"),
            self.RULES)
        self.assertEqual(got, {"empty-catch": 2, "static-context-leak": 1})

    def test_known_rule_with_no_findings_is_zero_not_absent(self):
        got = ast_rule_ratchet.parse_stream(self.stream("empty-catch"), self.RULES)
        self.assertEqual(got["static-context-leak"], 0)

    def test_empty_scan_is_all_zero(self):
        self.assertEqual(ast_rule_ratchet.parse_stream("", self.RULES),
                         {"empty-catch": 0, "static-context-leak": 0})

    def test_unknown_rule_id_is_counted(self):
        got = ast_rule_ratchet.parse_stream(self.stream("brand-new-rule"), self.RULES)
        self.assertEqual(got["brand-new-rule"], 1)

    def test_blank_lines_ignored(self):
        got = ast_rule_ratchet.parse_stream(
            self.stream("empty-catch") + "\n\n", self.RULES)
        self.assertEqual(got["empty-catch"], 1)

    def test_unparsable_line_raises_rather_than_undercounting(self):
        # a changed output format must never read as "fewer findings"
        with self.assertRaises(ast_rule_ratchet.ScanError):
            ast_rule_ratchet.parse_stream("not json at all", self.RULES)

    def test_every_registered_rule_file_exists(self):
        rules_dir = os.path.join(_HERE, "..", "rules")
        on_disk = {name[:-4] for name in os.listdir(rules_dir)
                   if name.endswith(".yml")}
        self.assertEqual(on_disk, set(ast_rule_ratchet.RULES))


if __name__ == "__main__":
    unittest.main()
