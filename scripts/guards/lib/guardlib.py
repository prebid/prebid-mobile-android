"""Shared engine for the structural guards.

Python 3 stdlib only — no pip, no requirements file. The guards' contract
("seconds on any machine, ubuntu CI is authoritative") dies the moment they
need dependency installation.

Three ratchet primitives, used by the checks in scripts/guards/checks/:

  lockfile   — a committed snapshot must match regenerated reality exactly
               (public-api baseline).
  allowlist  — grandfathered violations enumerated as records; new
               violations fail, stale entries fail (shrink-only).
  count      — grandfathered violations recorded as a number (or a
               {key: number} map); growth fails, shrinkage fails until the
               committed number is updated.

All committed guard data is JSON, one object per file, with a `guard` field
naming its owner and a `description` explaining the file's contract. The
schema is validated on every read: a grandfathered entry must state what it
is and why it is allowed, so the justification is machine-checked instead of
living in a comment a parser cannot see.

  allowlist    {"guard", "description", "entries": [{"entry", "reason"}]}
  count        {"guard", "description", "count": <int>}
  keyed count  {"guard", "description", "counts": {<key>: <int>}}
  entry list   {"guard", "description", "entries": [<str>]}

A malformed or schema-violating file raises GuardDataError, which `cli()`
turns into an actionable FAIL — never into a silent zero, which would read
as a ratchet win.

This file is deliberately platform-agnostic: nothing in it knows about
Swift, Xcode, or iOS. Porting the guards to another repo (e.g.
prebid-mobile-android) means copying this file and the runner, then writing
platform-specific probes that feed these primitives.
"""

import json
import os

# Exit code a guard returns when its environment cannot run the check
# (a missing external tool). The runner reports it as an advisory SKIP —
# distinct from a pass, so a skipped check never masquerades as a clean one.
EXIT_SKIPPED = 2

# scripts/guards/lib/ → repo root, for repo-relative paths in messages.
_LIB_DIR = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(os.path.dirname(os.path.dirname(_LIB_DIR)))


class GuardDataError(Exception):
    """A committed guard data file is missing, malformed, or violates the
    schema. The message names the file and the fix."""


def _rel(path):
    """Repo-relative path for error messages."""
    try:
        return os.path.relpath(path, ROOT)
    except ValueError:  # different drive (Windows) — absolute is fine
        return path


# ── JSON data files ──────────────────────────────────────────────────────────

_ENTRY_FIELDS = ("entry", "reason")


def load_json(path, update_cmd=None):
    """The parsed object of a committed guard data file.

    Raises GuardDataError for a missing file, invalid JSON, or a non-object
    top level — each with the command that regenerates it, when there is one.
    """
    fix = f" Regenerate it with:\n      {update_cmd}" if update_cmd else ""
    if not os.path.exists(path):
        raise GuardDataError(f"{_rel(path)} is missing.{fix}")
    try:
        with open(path, encoding="utf-8") as fh:
            data = json.load(fh)
    except ValueError as exc:
        raise GuardDataError(f"{_rel(path)} is not valid JSON ({exc}).{fix}")
    if not isinstance(data, dict):
        raise GuardDataError(
            f"{_rel(path)} must contain a JSON object, found "
            f"{type(data).__name__}.{fix}"
        )
    return data


def dump_json(path, payload):
    """Write a guard data file: 2-space indent, trailing newline, keys in
    the order given — a stable shape so regeneration produces a minimal diff."""
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as fh:
        json.dump(payload, fh, indent=2, ensure_ascii=False)
        fh.write("\n")


def _metadata(path, guard, description):
    """Existing guard/description of a data file, so regenerating a baseline
    preserves the prose a human wrote there."""
    if os.path.exists(path):
        try:
            data = load_json(path)
        except GuardDataError:
            data = {}
        guard = data.get("guard", guard)
        description = data.get("description", description)
    return {"guard": guard or "", "description": description or ""}


def _field(data, path, key, kind, update_cmd=None):
    fix = f" Regenerate it with:\n      {update_cmd}" if update_cmd else ""
    if key not in data:
        raise GuardDataError(f"{_rel(path)} has no '{key}' field.{fix}")
    value = data[key]
    if not isinstance(value, kind) or isinstance(value, bool):
        raise GuardDataError(
            f"{_rel(path)}: '{key}' must be {kind.__name__}, found "
            f"{type(value).__name__}.{fix}"
        )
    return value


# ── primitive 1: lockfile ────────────────────────────────────────────────────

def lockfile_diff(baseline_lines, current_lines, limit=40):
    """Changed lines between two snapshots, in unified-diff notation with
    the +++/--- headers dropped. Empty list == no drift."""
    import difflib
    out = []
    for line in difflib.unified_diff(baseline_lines, current_lines, lineterm=""):
        if len(line) >= 1 and line[0] in "+-" and not line[1:2] in ("+", "-"):
            out.append(line)
            if len(out) >= limit:
                break
    return out


# ── primitive 2: allowlist ratchet ───────────────────────────────────────────

def read_allowlist_records(path):
    """Validated allowlist records, in file order.

    Every record needs `entry` (the violation token the probe emits) and
    `reason` (why it is grandfathered). Both are required and non-empty, so
    an entry can never arrive without its justification.
    """
    data = load_json(path)
    entries = _field(data, path, "entries", list)
    records, seen = [], set()
    for index, record in enumerate(entries):
        where = f"{_rel(path)} entries[{index}]"
        if not isinstance(record, dict):
            raise GuardDataError(
                f"{where} must be an object with "
                f"{{{', '.join(_ENTRY_FIELDS)}}}, found {type(record).__name__}."
            )
        missing = [k for k in _ENTRY_FIELDS if k not in record]
        if missing:
            raise GuardDataError(
                f"{where} is missing required field(s): {', '.join(missing)}. "
                "Every grandfathered entry states what it is and why it is allowed."
            )
        unknown = sorted(set(record) - set(_ENTRY_FIELDS))
        if unknown:
            raise GuardDataError(
                f"{where} has unknown field(s): {', '.join(unknown)}. "
                f"Allowed: {', '.join(_ENTRY_FIELDS)}."
            )
        entry, reason = record["entry"], record["reason"]
        if not isinstance(entry, str) or not entry.strip():
            raise GuardDataError(f"{where}: 'entry' must be a non-empty string.")
        if not isinstance(reason, str) or not reason.strip():
            raise GuardDataError(
                f"{where} ('{entry}'): 'reason' must be a non-empty string — "
                "state why this violation is grandfathered."
            )
        if entry in seen:
            raise GuardDataError(f"{where}: duplicate entry '{entry}'.")
        seen.add(entry)
        records.append(record)
    return records


def read_allowlist(path):
    """Just the entry tokens of a validated allowlist, sorted."""
    return sorted(record["entry"] for record in read_allowlist_records(path))


def describe_entries(path, entries):
    """'<entry> — <reason>' lines for reporting, reasons pulled from the
    allowlist so a stale-entry failure says what the entry was for."""
    try:
        reasons = {r["entry"]: r["reason"] for r in read_allowlist_records(path)}
    except GuardDataError:
        reasons = {}
    return [
        f"{entry} — {reasons[entry]}" if entry in reasons else entry
        for entry in entries
    ]


def ratchet(current_violations, allowlist_entries):
    """(new, stale): violations not grandfathered, and grandfathered entries
    whose violation is gone. Both must be empty for a pass."""
    current = set(current_violations)
    allowed = set(allowlist_entries)
    new = sorted(current - allowed)
    stale = sorted(allowed - current)
    return new, stale


# ── primitive 3: count ratchet ───────────────────────────────────────────────

def read_count(path, update_cmd=None):
    """The single integer of a count baseline."""
    data = load_json(path, update_cmd)
    return _field(data, path, "count", int, update_cmd)


def write_count(path, count, guard=None, description=None):
    payload = _metadata(path, guard, description)
    payload["count"] = count
    dump_json(path, payload)


def check_count(current, baseline_path, label, update_cmd, grow_hint):
    """Standard count-ratchet verdict.

    Returns (exit_code, [message lines]). Growth and shrinkage both fail,
    with different instructions. A missing or malformed baseline raises
    GuardDataError rather than reading as zero.
    """
    recorded = read_count(baseline_path, update_cmd)
    if current > recorded:
        return 1, [f"FAIL: {label} count grew ({recorded} → {current})."] + grow_hint
    if current < recorded:
        return 1, [
            f"FAIL: {label} count shrank ({recorded} → {current}) — a ratchet win, but the",
            "baseline must be updated in the same PR:",
            "      " + update_cmd,
        ]
    return 0, [f"OK: {label} count unchanged ({current})."]


# ── primitive 3b: keyed count ratchet ────────────────────────────────────────

def read_keyed_counts(path, update_cmd=None):
    """The {key: count} map of a keyed-count baseline."""
    data = load_json(path, update_cmd)
    counts = _field(data, path, "counts", dict, update_cmd)
    for key, value in counts.items():
        if not isinstance(value, int) or isinstance(value, bool):
            raise GuardDataError(
                f"{_rel(path)}: count for '{key}' must be an integer, found "
                f"{type(value).__name__}."
            )
    return counts


def write_keyed_counts(path, counts, guard=None, description=None):
    """Write a keyed-count baseline, dropping zero counts and sorting keys
    so regeneration is diff-stable."""
    payload = _metadata(path, guard, description)
    payload["counts"] = {k: n for k, n in sorted(counts.items()) if n > 0}
    dump_json(path, payload)


def check_keyed_counts(current, baseline_path, label, update_cmd, grow_hint):
    """Per-key count-ratchet verdict: any key growing (or appearing) fails
    with the grow hint; any key shrinking (or disappearing) fails until the
    baseline is updated — a visible per-key ratchet win.

    Returns (exit_code, [message lines]); keys reported sorted.
    """
    recorded = read_keyed_counts(baseline_path, update_cmd)
    current = {k: n for k, n in current.items() if n > 0}
    grown = sorted(k for k in current if current[k] > recorded.get(k, 0))
    shrunk = sorted(k for k in recorded if recorded[k] > current.get(k, 0))
    if grown:
        lines = [f"FAIL: {label} count grew:"]
        lines += [f"  {k}: {recorded.get(k, 0)} → {current[k]}" for k in grown]
        return 1, lines + grow_hint
    if shrunk:
        lines = [f"FAIL: {label} count shrank — a ratchet win, but the baseline must be",
                 "updated in the same PR:",
                 "      " + update_cmd]
        lines += [f"  {k}: {recorded[k]} → {current.get(k, 0)}" for k in shrunk]
        return 1, lines
    total = sum(current.values())
    return 0, [f"OK: {label} count unchanged ({total} across {len(current)} entries)."]


# ── primitive 2b: enumerated baseline (a ratchet that names its members) ─────

def read_entries(path, update_cmd=None):
    """The string list of an entry-list baseline, sorted."""
    data = load_json(path, update_cmd)
    entries = _field(data, path, "entries", list, update_cmd)
    for index, entry in enumerate(entries):
        if not isinstance(entry, str) or not entry.strip():
            raise GuardDataError(
                f"{_rel(path)} entries[{index}] must be a non-empty string."
            )
    return c_sorted(entries)


def write_entries(path, entries, guard=None, description=None):
    payload = _metadata(path, guard, description)
    payload["entries"] = c_sorted(entries)
    dump_json(path, payload)


# ── shared utilities used by the platform probes ─────────────────────────────

def c_sorted(lines):
    """Byte-order sort + dedupe, matching `LC_ALL=C sort -u` for the ASCII
    content the guards emit."""
    return sorted(set(lines))


def walk_files(root, suffixes, exclude_dir_parts=()):
    """All files under root with one of the suffixes, path-sorted, skipping
    any path containing one of exclude_dir_parts."""
    hits = []
    for dirpath, _dirnames, filenames in os.walk(root):
        if any(part in dirpath for part in exclude_dir_parts):
            continue
        for name in filenames:
            if name.endswith(tuple(suffixes)):
                hits.append(os.path.join(dirpath, name))
    return sorted(hits)


def cli(main_fn):
    """Wrap a check's main() so a GuardDataError becomes an actionable FAIL
    instead of a traceback — and never a silent pass."""
    def run(argv):
        try:
            return main_fn(argv)
        except GuardDataError as exc:
            print(f"FAIL: {exc}")
            return 1
    return run
