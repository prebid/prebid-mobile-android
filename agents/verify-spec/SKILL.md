---
name: verify-spec
description: Audit ORTB models and request-building tests against the authoritative OpenRTB 2.x spec — classify each behavior CONFIRMED/CONTRADICTS/UNSPECIFIED/PREBID-EXT with commit-pinned permalinks. Strictly read-only — produces a report, never touches source or test files.
---

# /verify-spec [scope]

`scope`: an ORTB model name (`BidRequest`), a directory, or `all` for
`PrebidMobile-core/src/main/java/org/prebid/mobile/rendering/models/openrtb/` + request building
+ their tests.

Each translation layer (IAB spec → Prebid Server semantics → SDK models → tests) can introduce
errors. This skill confirms the tests expect the RIGHT behavior — before any production code is
judged against them.

## Authoritative sources (priority order)

1. **IAB OpenRTB 2.x spec** — https://github.com/InteractiveAdvertisingBureau/openrtb2.x — the
   authority for core object/field semantics. Always cite commit-pinned permalinks, never `main`.
2. **Prebid Server request semantics** (docs.prebid.org / prebid-server repo) — the authority
   for `ext.prebid` extensions where OpenRTB is silent.

Other Prebid SDKs (prebid-mobile-**ios**, prebid.js) are cross-checks only — agreement with them
is evidence, not proof.

## Protocol

1. **Enumerate** the model fields / test assertions in scope (models under
   `rendering/models/openrtb/`, request building in `rendering/networking/parameters/`, tests
   like the parameter-builder and bid-parsing suites).
2. **Classify** each against the sources, recording the permalink in the report:
   - `CONFIRMED` — matches the spec.
   - `CONTRADICTS` — test/model disagrees with the spec. **Do not edit the test.** Add a
     proposed issue to the report (suggested label `spec-discrepancy`) with the permalink and
     both behaviors; changing asserted behavior is a reviewed code change, not an audit side
     effect.
   - `UNSPECIFIED` — spec is silent and it's not a documented prebid extension; note it.
   - `PREBID-EXT` — intentional Prebid extension; cite the prebid-server doc instead.
3. **Report** — the audit's only output, written in the conversation, never to a file: per
   model/test file, a summary table with the tally, the audited date and pinned spec commit,
   per-field classifications with permalinks, and discrepancies at the top with their proposed
   issues. When run as part of an `/sdk-review` audit, fold the findings into that review's
   output.

Strictly read-only — this skill writes no files at all: it never modifies source or test files,
not even to add comments or verification headers, and never commits a report. Permalink citations
in code (per the spec-grounding gate in AGENTS.md) are the job of the change that touches the
code, guided by the report.
