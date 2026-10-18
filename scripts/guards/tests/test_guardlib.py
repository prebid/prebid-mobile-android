"""Unit tests for the shared guard engine (lib/guardlib.py).

The engine is copied verbatim from prebid-mobile-ios, where it was designed
to be platform-agnostic. These tests pin the contract the Android checks
build on: schema validation fails loudly (never reads as "no findings"),
allowlists shrink-only, counts ratchet in both directions, and lockfile
diffs surface drift.
"""

import os
import shutil
import sys
import tempfile
import unittest

_HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(_HERE, "..", "lib"))

import guardlib  # noqa: E402


class TempDirTestCase(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.mkdtemp(prefix="guardlib-test-")
        self.addCleanup(shutil.rmtree, self.tmp)

    def path(self, name="data.json"):
        return os.path.join(self.tmp, name)

    def write(self, content, name="data.json"):
        target = self.path(name)
        with open(target, "w", encoding="utf-8") as fh:
            fh.write(content)
        return target


class LoadJsonTests(TempDirTestCase):
    def test_missing_file_raises_with_update_hint(self):
        with self.assertRaises(guardlib.GuardDataError) as ctx:
            guardlib.load_json(self.path(), update_cmd="./regen.sh")
        self.assertIn("missing", str(ctx.exception))
        self.assertIn("./regen.sh", str(ctx.exception))

    def test_invalid_json_raises(self):
        target = self.write("{not json")
        with self.assertRaises(guardlib.GuardDataError):
            guardlib.load_json(target)

    def test_non_object_top_level_raises(self):
        target = self.write("[1, 2, 3]")
        with self.assertRaises(guardlib.GuardDataError):
            guardlib.load_json(target)

    def test_valid_object_loads(self):
        target = self.write('{"guard": "x", "count": 3}')
        self.assertEqual(guardlib.load_json(target)["count"], 3)


class AllowlistSchemaTests(TempDirTestCase):
    def allowlist(self, entries_json):
        return self.write(
            '{"guard": "g", "description": "d", "entries": %s}' % entries_json
        )

    def test_valid_allowlist_reads_sorted_tokens(self):
        target = self.allowlist(
            '[{"entry": "b", "reason": "r1"}, {"entry": "a", "reason": "r2"}]'
        )
        self.assertEqual(guardlib.read_allowlist(target), ["a", "b"])

    def test_entry_without_reason_fails(self):
        target = self.allowlist('[{"entry": "a"}]')
        with self.assertRaises(guardlib.GuardDataError) as ctx:
            guardlib.read_allowlist(target)
        self.assertIn("reason", str(ctx.exception))

    def test_empty_reason_fails(self):
        target = self.allowlist('[{"entry": "a", "reason": "  "}]')
        with self.assertRaises(guardlib.GuardDataError):
            guardlib.read_allowlist(target)

    def test_unknown_field_fails_loudly(self):
        # a typo ("resaon") must not silently drop the justification
        target = self.allowlist('[{"entry": "a", "reason": "r", "resaon": "x"}]')
        with self.assertRaises(guardlib.GuardDataError) as ctx:
            guardlib.read_allowlist(target)
        self.assertIn("resaon", str(ctx.exception))

    def test_duplicate_entry_fails(self):
        target = self.allowlist(
            '[{"entry": "a", "reason": "r"}, {"entry": "a", "reason": "r"}]'
        )
        with self.assertRaises(guardlib.GuardDataError):
            guardlib.read_allowlist(target)

    def test_describe_entries_pulls_reasons(self):
        target = self.allowlist('[{"entry": "a", "reason": "why a"}]')
        lines = guardlib.describe_entries(target, ["a", "unknown"])
        self.assertEqual(lines, ["a — why a", "unknown"])


class RatchetTests(unittest.TestCase):
    def test_clean_pass(self):
        new, stale = guardlib.ratchet(["a", "b"], ["a", "b"])
        self.assertEqual((new, stale), ([], []))

    def test_new_violation_detected(self):
        new, stale = guardlib.ratchet(["a", "b", "c"], ["a", "b"])
        self.assertEqual(new, ["c"])
        self.assertEqual(stale, [])

    def test_stale_entry_detected(self):
        new, stale = guardlib.ratchet(["a"], ["a", "gone"])
        self.assertEqual(new, [])
        self.assertEqual(stale, ["gone"])


class CountRatchetTests(TempDirTestCase):
    def baseline(self, count):
        target = self.path("count.json")
        guardlib.write_count(target, count, guard="g", description="d")
        return target

    def test_unchanged_passes(self):
        code, _ = guardlib.check_count(3, self.baseline(3), "widget", "cmd", [])
        self.assertEqual(code, 0)

    def test_growth_fails_with_hint(self):
        code, messages = guardlib.check_count(
            4, self.baseline(3), "widget", "cmd", ["fix it"])
        self.assertEqual(code, 1)
        self.assertIn("fix it", messages)

    def test_shrink_fails_until_updated(self):
        code, messages = guardlib.check_count(2, self.baseline(3), "widget", "cmd", [])
        self.assertEqual(code, 1)
        self.assertTrue(any("cmd" in m for m in messages))

    def test_missing_baseline_raises_not_zero(self):
        with self.assertRaises(guardlib.GuardDataError):
            guardlib.check_count(0, self.path("absent.json"), "widget", "cmd", [])

    def test_regeneration_preserves_description(self):
        target = self.baseline(3)
        guardlib.write_count(target, 5)
        data = guardlib.load_json(target)
        self.assertEqual(data["description"], "d")
        self.assertEqual(data["count"], 5)


class KeyedCountRatchetTests(TempDirTestCase):
    def baseline(self, counts):
        target = self.path("counts.json")
        guardlib.write_keyed_counts(target, counts, guard="g", description="d")
        return target

    def test_unchanged_passes(self):
        code, _ = guardlib.check_keyed_counts(
            {"a": 1, "b": 2}, self.baseline({"a": 1, "b": 2}), "f", "cmd", [])
        self.assertEqual(code, 0)

    def test_one_key_growing_fails_even_if_another_shrinks(self):
        code, messages = guardlib.check_keyed_counts(
            {"a": 2, "b": 1}, self.baseline({"a": 1, "b": 2}), "f", "cmd", [])
        self.assertEqual(code, 1)
        self.assertTrue(any("a" in m and "2" in m for m in messages))

    def test_new_key_counts_as_growth(self):
        code, _ = guardlib.check_keyed_counts(
            {"a": 1, "new": 1}, self.baseline({"a": 1}), "f", "cmd", [])
        self.assertEqual(code, 1)

    def test_disappeared_key_counts_as_shrink(self):
        code, messages = guardlib.check_keyed_counts(
            {"a": 1}, self.baseline({"a": 1, "b": 1}), "f", "cmd", [])
        self.assertEqual(code, 1)
        self.assertTrue(any("cmd" in m for m in messages))

    def test_zero_counts_dropped_on_write(self):
        target = self.baseline({"a": 1, "zero": 0})
        self.assertEqual(guardlib.read_keyed_counts(target), {"a": 1})

    def test_non_int_count_rejected(self):
        target = self.write('{"guard": "g", "counts": {"a": "1"}}', "bad.json")
        with self.assertRaises(guardlib.GuardDataError):
            guardlib.read_keyed_counts(target)


class EntryListTests(TempDirTestCase):
    def test_round_trip_sorted_deduped(self):
        target = self.path("entries.json")
        guardlib.write_entries(target, ["b", "a", "b"], guard="g", description="d")
        self.assertEqual(guardlib.read_entries(target), ["a", "b"])

    def test_blank_entry_rejected(self):
        target = self.write('{"guard": "g", "entries": ["a", "  "]}', "bad.json")
        with self.assertRaises(guardlib.GuardDataError):
            guardlib.read_entries(target)


class LockfileDiffTests(unittest.TestCase):
    def test_no_drift_is_empty(self):
        self.assertEqual(guardlib.lockfile_diff(["a", "b"], ["a", "b"]), [])

    def test_drift_reports_signed_lines(self):
        diff = guardlib.lockfile_diff(["a", "b"], ["a", "c"])
        self.assertIn("-b", diff)
        self.assertIn("+c", diff)

    def test_headers_dropped(self):
        diff = guardlib.lockfile_diff(["a"], ["b"])
        self.assertFalse(any(line.startswith(("+++", "---")) for line in diff))

    def test_limit_caps_output(self):
        diff = guardlib.lockfile_diff([], [str(i) for i in range(100)], limit=5)
        self.assertEqual(len(diff), 5)


class UtilityTests(TempDirTestCase):
    def test_c_sorted_dedupes(self):
        self.assertEqual(guardlib.c_sorted(["b", "a", "b"]), ["a", "b"])

    def test_walk_files_filters_suffix_and_excludes(self):
        os.makedirs(os.path.join(self.tmp, "src", "build"))
        for rel in ("src/A.java", "src/B.kt", "src/C.txt", "src/build/D.java"):
            with open(os.path.join(self.tmp, rel), "w") as fh:
                fh.write("x")
        hits = guardlib.walk_files(
            self.tmp, (".java", ".kt"),
            exclude_dir_parts=(os.sep + "build",))
        names = [os.path.basename(h) for h in hits]
        self.assertEqual(names, ["A.java", "B.kt"])

    def test_cli_turns_data_error_into_fail(self):
        import contextlib
        import io

        def boom(_argv):
            raise guardlib.GuardDataError("bad data file")

        out = io.StringIO()
        with contextlib.redirect_stdout(out):
            code = guardlib.cli(boom)(["prog"])
        self.assertEqual(code, 1)
        self.assertIn("bad data file", out.getvalue())


if __name__ == "__main__":
    unittest.main()
