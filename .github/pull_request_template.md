<!--
The PR title becomes a release-note line. Make it clear and user-facing,
e.g. "Fix: interstitial controls misplaced after orientation changes".
-->

## Description

<!-- What does this change do, and why? Link the related issue. -->

## Checklist

- [ ] Tests added or updated for behavior changes
- [ ] No new public API — or the API baseline (`api/*.txt`) is regenerated in this PR, and the new API is documented and tested
- [ ] No manifest-surface change (permissions, exported components, queries) — or the baseline is updated and the change is called out
- [ ] OpenRTB request/response changes cite the relevant spec section
- [ ] `./scripts/guards/run-guards.sh` passes locally (or rely on the CI guards job)
