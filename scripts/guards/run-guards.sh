#!/usr/bin/env bash
#
# Structural guards for prebid-mobile-android.
#
# Fast, deterministic architecture checks that run locally and in CI.
# See docs/guards/README.md for what each guard enforces and how to fix
# a failure.
#
# Usage:
#   ./scripts/guards/run-guards.sh                          # run all guards
#   ./scripts/guards/run-guards.sh --update-fixme-baseline  # re-record the FIXME/TODO count
#
# This script is orchestration only: every check lives in checks/ as a Python
# module on lib/guardlib.py, and every committed allowlist/baseline is JSON.
#
# Exit code: non-zero if any blocking guard fails. A guard whose environment
# cannot run it (a missing external tool) exits EXIT_SKIPPED (2) and is
# reported as an advisory SKIP — never as a pass.

set -u

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
GUARDS_DIR="$ROOT/scripts/guards"
export GUARDS_DIR
cd "$ROOT"

EXIT_SKIPPED=2

# ── baseline regeneration flags ─────────────────────────────────────────────
case "${1:-}" in
    --update-fixme-baseline) exec python3 "$GUARDS_DIR/checks/fixme_ratchet.py" --update ;;
    "") ;;
    *)
        echo "Unknown option: $1"
        sed -n '9,11p' "${BASH_SOURCE[0]}" | sed 's/^# \{0,1\}//'
        exit 2
        ;;
esac

FAILED=()
PASSED=()
ADVISORY_NOTES=()

run_guard() { # <id> <check-script>
    local id="$1" script="$2" status
    echo ""
    echo "── guard: $id ──────────────────────────────────────────"
    python3 "$GUARDS_DIR/checks/$script"
    status=$?
    if [ "$status" -eq 0 ]; then
        PASSED+=("$id")
    elif [ "$status" -eq "$EXIT_SKIPPED" ]; then
        ADVISORY_NOTES+=("$id: SKIPPED (environment; CI is authoritative)")
    else
        FAILED+=("$id")
    fi
}

# Guards are registered here as they land; each is blocking.
run_guard fixme-ratchet    fixme_ratchet.py
run_guard logging-hygiene  logging_hygiene.py

# ── summary ─────────────────────────────────────────────────────────────────
echo ""
echo "═════════════════════════════════════════════════════════"
echo "Guards passed: ${#PASSED[@]:-0} (${PASSED[*]:-})"
for note in "${ADVISORY_NOTES[@]:-}"; do
    [ -n "$note" ] && echo "Advisory: $note"
done
if [ "${#FAILED[@]}" -gt 0 ]; then
    echo "Guards FAILED: ${FAILED[*]}"
    echo "See docs/guards/README.md for how to fix each guard."
    exit 1
fi
echo "All blocking guards passed."
