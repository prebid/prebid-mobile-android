#!/usr/bin/env bash
#
# Android Lint, scoped to the lines this branch adds.
#
# NOT a guard. The structural guards (./scripts/guards/run-guards.sh) own the
# architecture rules and their ratchets; this owns the per-file style/
# correctness layer they ignore. The rule set lives in lint.xml at the repo
# root (explicit enables only — see docs/lint/README.md for why, and for
# which checks are deliberately left to guards).
#
# Whole-tree linting on a 400+ file Java core reports a legacy backlog no
# one can act on. So the default mode reports only violations on lines the
# branch ADDS versus the merge-base with master: new code arrives clean,
# legacy lines stay silent until someone edits them on purpose.
#
# Findings on added lines FAIL. Blocking is the default so a local run gives
# the same verdict as CI.
#
# Usage:
#   ./scripts/lint/run-lint.sh              # added lines only — exit 1 on findings
#   ./scripts/lint/run-lint.sh --advisory   # same, report only (exit 0)
#
# Exit codes: 0 = clean (or advisory mode), 1 = findings on added lines,
#             2 = could not run (no Gradle/JDK, no master ref to diff).
#             Matches the guards' EXIT_SKIPPED convention — never a silent pass.
#             In CI, GUARDS_REQUIRE_BUILD=1 turns "could not run" into FAIL.

set -u

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT" || exit 1

EXIT_SKIPPED=2
BLOCKING=1
case "${1:-}" in
    --advisory) BLOCKING=0 ;;
    "")         ;;
    *)
        echo "Unknown option: $1"
        sed -n '/^# Usage:/,/^#$/p' "${BASH_SOURCE[0]}" | sed 's/^# \{0,1\}//'
        exit 2
        ;;
esac

MODULES=(
    PrebidMobile-core
    PrebidMobile-gamEventHandlers
    PrebidMobile-admobAdapters
    PrebidMobile-maxAdapters
    PrebidMobile-nextGenEventHandlers
)

LINT_TASKS=()
for module in "${MODULES[@]}"; do
    LINT_TASKS+=(":${module}:lintRelease")
done

echo "── lint: running ${#MODULES[@]} module lint tasks (first run takes minutes) ──"
if ! ./gradlew -q "${LINT_TASKS[@]}"; then
    # lintOptions.abortOnError is false repo-wide, so a non-zero exit here is
    # an environment/build failure, not a finding.
    if [ "${GUARDS_REQUIRE_BUILD:-}" = "1" ]; then
        echo "FAIL: the lint build failed — findings are unavailable, NOT zero."
        exit 1
    fi
    echo "SKIPPED: could not run the lint tasks locally (JDK/Android SDK needed)."
    echo "CI is authoritative."
    exit $EXIT_SKIPPED
fi

REPORTS=()
for module in "${MODULES[@]}"; do
    report="PrebidMobile/${module}/build/reports/lint-results-release.xml"
    if [ ! -f "$report" ]; then
        echo "FAIL: expected lint report missing: $report"
        echo "A missing report is never an empty one."
        exit 1
    fi
    REPORTS+=("$report")
done

python3 scripts/lint/diff_scope.py "${REPORTS[@]}"
STATUS=$?

if [ "$STATUS" -eq "$EXIT_SKIPPED" ]; then
    if [ "${GUARDS_REQUIRE_BUILD:-}" = "1" ]; then
        echo "FAIL: diff scoping unavailable in CI."
        exit 1
    fi
    exit $EXIT_SKIPPED
fi

if [ "$STATUS" -ne 0 ] && [ "$BLOCKING" -eq 0 ]; then
    echo "Report-only mode: not failing the run."
    exit 0
fi
exit $STATUS
