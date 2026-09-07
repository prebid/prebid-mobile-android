---
name: sdk-review
description: Review a branch/PR against master — or audit the full tree — with this repo's review checklist. Guards first, then the human-judgment items guards can't see (privacy logging, API stability, WebView/rendering, accessibility, test integrity). Verdict-first structured review reported in the conversation, including guard candidates.
---

# PR review / audit for prebid-mobile-android

`/sdk-review [target]` — `target` is a branch, a commit range (default `master...HEAD`), or
`full` (audit the whole tree).

Findings are reported in the conversation, in the §5 format. This skill writes no files: it never
commits a review report, and never edits source, tests, or guard data — fixes are a follow-up the
maintainer asks for explicitly.

Out of scope for this checklist: the diff-scoped Lint (it gates the PR for itself — see
`docs/lint/README.md`) and build/test command sequencing.

## 1. Orient

```bash
git fetch origin master
git log --oneline origin/master..HEAD      # two-dot: commits on the branch
git diff origin/master...HEAD              # three-dot: merge-base diff
```

Use the **three-dot** form for `diff` — it compares against the merge-base, so a stale branch
doesn't show phantom reversions of other people's work.

Classify the PR and adapt depth: feature/fix (standard checklist; wire-format changes trigger
`/verify-spec`), tooling/CI (guard/workflow semantics and the release path), docs-only
(correctness of commands and paths only).

## 2. Mechanical checks first — never hand-review what a guard checks

```bash
./scripts/guards/run-guards.sh
```

| Concern | Already guarded by |
|---|---|
| Public API changes | `public-api-baseline` (+ `api-doc-coverage`, `api-test-presence`) |
| Raw `Log`/`printStackTrace` output | `logging-hygiene` |
| Manifest permissions / exported components | `manifest-surface` |
| Off-main listener callbacks, empty catches, `!!`, static Context fields | `ast-rule-ratchet` |
| ORTB models without tests | `ortb-test-presence` |
| New FIXME/TODO markers | `fixme-ratchet` |
| New `@Ignore` / test skips | `skiplist-ratchet` |
| Deprecations without a replacement note | `deprecation-hygiene` |

Guards green → those dimensions are done; don't re-litigate them by hand. Baseline or allowlist
diffs in the PR are the review surface: each must be intentional and called out. If review finds
a violation class no guard covers, that's a `/guard` candidate — say so in the review output.

## 3. Human-judgment checklist (all PRs)

Things no guard can see:

- **Privacy in logs**: never log consent strings, the advertising ID, or user IDs/EIDs — not
  even at debug level. Check every new `LogUtil.*` call's arguments.
- **Publisher API stability**: `fetchDemand` behavior untouched unless the PR says otherwise;
  the minSdk 19 floor preserved (API-level guards via `Build.VERSION` checks are fine, raising
  the floor is not).
- **WebView / rendering** (for creative or MRAID changes):
  - New `@JavascriptInterface` methods or `addJavascriptInterface` calls widen the SDK's largest
    untrusted-input surface — challenge each one (no guard owns this; Lint's
    `JavascriptInterface`/`AddJavascriptInterface` checks catch mistakes, not design).
  - WebView settings changes (`setAllowFileAccess`, mixed content, universal file access) are
    security decisions, not toggles.
  - Configuration changes (rotation, split-screen) — interstitial dialogs and video controls
    have a history here (#989, #998).
- **Accessibility** (SDK-drawn controls: close, skip, mute, CTA): ≥ 48dp touch targets,
  `contentDescription` set, contrast ≥ 4.5:1 for text and 3:1 for controls.
- **Threading**: background work stays off the main thread; only the callback hop goes through
  `TasksManager.executeOnMainThread`. New shared mutable state needs a documented
  synchronization story.
- **Test integrity**: no weakened assertions, no trivial asserts replacing real ones, prefer the
  existing mocks in `test-utils` over new hand-rolled stubs.

Layer spot-checks, when the diff touches them:

- **public-api**: is `public` deliberate (package-private wouldn't do)? Javadoc present and
  meaningful? PR title reflects the API change (titles become release notes)?
- **ortb**: spec permalink cited in code + PR; wire-format test asserts the change;
  Prebid-extension vs OpenRTB-core distinguished (`/verify-spec` for anything nontrivial).
- **networking**: MockWebServer rules, not ad-hoc stubs; timeout/retry behavior tested.
- **gradle/build**: version bumps pinned, not `+` ranges (the `play-services-ads` `+` is a known
  pre-existing trap); new dependencies justified — every publisher inherits them.
- **ci-scripts**: still runnable on the pinned runner images; guard and workflow pins
  (ast-grep, Metalava) stay in sync.

## 4. Known flaky areas

Robolectric tests touching timers/loopers (`RefreshTimerTaskTest` has one @Ignore already) —
only flag a failure if it reproduces in isolation.

## 5. Output format

In order, skipping empty sections, blockers first:

1. **Summary** — 2-3 sentences + verdict: `approve` / `request changes` / `comment`.
2. **Blockers** — each with file:line and why it blocks.
3. **Suggestions** — non-blocking improvements.
4. **Guard candidates** — violation classes found by hand that a guard could catch; hand each to
   `/guard` so review vigilance is never needed for it again.
5. **Nits** — style only, freely ignorable.
