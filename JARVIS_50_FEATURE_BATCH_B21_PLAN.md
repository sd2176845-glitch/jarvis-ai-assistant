# JARVIS — 50-Feature Integrated Batch (B21)

**Status:** Scope and acceptance plan only. None of these 50 items is claimed implemented by this document.
**Branch:** `feature/jarvis-50-feature-megabatch`
**Parent:** `feature/m17-battery-health-snapshot` (B20).
**Project boundary:** JARVIS only. Even is not part of this work.

## Rules for this batch

- One coordinated release/PR containing 50 individually testable capabilities, grouped by their canonical M01–M19 owners.
- Reuse existing code and stores. Upgrade existing modules rather than creating duplicate dashboards, assistants, memories, task databases, schedulers, voice routers, or permission systems.
- Every capability must have a visible owner action, explicit permission/data scope, failure state, and automated test where practical.
- Sensitive communication, external sharing, account changes, repository writes, and physical-device actions require a fresh owner confirmation.
- No stealth monitoring, hidden recording, backdoors, unauthorized access, or arbitrary self-repair.
- “Code-present”, “CI passed”, and “Poco device verified” are separate statuses. Nothing is complete merely because it appears in this plan.

## 50 candidate capabilities

### M01 — Owner Identity, Access & Privacy (1–5)
1. Google owner sign-in using a configured identity provider.
2. Phone OTP sign-in using a configured provider; never store OTPs or passwords locally.
3. Owner profile and session-expiry handling.
4. Re-authentication gate for sensitive settings and exports.
5. Guest mode that suppresses owner-specific notes, memory, and briefings.

### M02 — Core Voice & Conversation (6–10)
6. Owner-configurable wake/listening preference with a clear visible stop/pause state.
7. Hindi/English/Hinglish/Bengali language-selection and fallback workflow, subject to installed speech/TTS support.
8. Follow-up command context with explicit expiry and cancel/reset controls.
9. Offline-ready local command catalogue with honest online-only status.
10. Conversation response controls: repeat, stop speech, and return to standby.

### M03 — Knowledge, Search, Research & Learning (11–17)
11. Native weather lookup with source, timestamp, units, and clear location consent.
12. Currency conversion with rate date and source.
13. Country information lookup with source link.
14. Dictionary lookup with source and pronunciation availability.
15. India PIN-code lookup with input validation and returned district/state/post-office details.
16. India public-holiday lookup by year with source label.
17. Source-aware in-app web research with clickable references and freshness labels.

### M04 — Universal App Intelligence (18–20)
18. Android-permitted installed-app catalogue.
19. Public app documentation and feature explainer with source/freshness.
20. App update/release-note tracker that distinguishes public documentation from on-device verification.

### M05 — Application & Phone Control (21–23)
21. Supported app-launch catalogue with unavailable-app handling.
22. Safe media-player controls through Android-supported media sessions where available.
23. Unknown-screen/control detection that stops and asks instead of guessing.

### M06 — Communication Intelligence (24–26)
24. Voice-to-message drafting with editable preview.
25. Recipient disambiguation against owner-permitted contacts.
26. Confirm-before-send workflow for supported SMS/messaging handoffs.

### M07 — Personal Secretary & Productivity (27–30)
27. Structured task categories and priorities, reusing the existing task store.
28. Due-date fields and task completion history, reusing the existing task store.
29. Calendar event draft and owner-confirmed handoff.
30. Daily agenda view from explicitly connected or owner-entered schedule data.

### M08 — Workflow Automation (31–34)
31. Multi-step workflow preview before execution.
32. Per-step approval gates for sensitive actions.
33. Dry-run mode that reports intended actions without executing them.
34. Workflow history with outcome, cancellation, and failure reason.

### M09 — Memory & Knowledge Vault (35–37)
35. Owner review/edit/delete controls for saved preferences.
36. Export of owner-selected memory records with preview.
37. Provenance labels distinguishing owner-entered facts from imported/researched material.

### M10 — Vision & Media Intelligence (38–40)
38. User-selected image OCR with preview and cancellation.
39. User-selected document text extraction with local preview.
40. Explicit screen-context explainer session with a visible capture indicator and no background monitoring.

### M11 — Creative Media Studio (41)
41. Creative project organizer for owner-created drafts, with clear draft/licensing labels.

### M12 — Software & Data Studio (42–43)
42. Repository/project selection and read-only project overview before any edit.
43. Owner-approved build/test report viewer that never labels an unrun test as passed.

### M13 — Security & Secure Evidence (44)
44. Local secure-vault design with encryption, access control, export preview, audit trail, and retention/deletion controls; no covert collection.

### M14 — Diagnostics & Recovery (45–46)
45. Owner-invoked sanitized diagnostic report with secret/private-data redaction and preview.
46. Read-only backend/network/speech health summary with evidence-based wording.

### M15 — Adaptive UI & Accessibility (47)
47. Consistent theme and accessibility profile coverage across existing dialogs and studios, preserving readable contrast.

### M16 — Connected Ecosystem (48)
48. Authorized smart-device routine preview with explicit confirmation and a visible manual stop/off path; reuse the existing paired-device inventory.

### M17 — Platform & Connectivity (49)
49. Local/global/hybrid intelligence status selector that reports real connectivity and does not imply satellite or internet access when unavailable.

### M18/M19 — Simulation & Practical Intelligence (50)
50. Read-only virtual experiment notebook for engineering concepts, with assumptions, units, source notes, and a clear “simulation only” label.

## Delivery gates

- Audit the B20 branch and existing studios before implementation; remove any candidate already implemented in code.
- If a candidate overlaps an existing module, extend that module and retain one canonical owner.
- Implement the 50 accepted capabilities in coherent internal workstreams on this single branch; do not split the owner's requested release into multiple small PRs.
- Run backend syntax checks, Android unit tests, debug APK build, and artifact upload.
- Review diffs and test output before opening one draft PR.
- Poco M4 Pro 4G device verification remains mandatory for device-dependent behavior.
- Do not merge into B20 or main without owner approval.


## B21 implementation ledger

The following are implemented as extensions of existing canonical modules, not separate duplicate modules:

- **Task manager (11 improvements):** categories, priority, due dates, completion history, full task-detail editing, overdue detection, task overview, local title search, daily due-date agenda, category filtering, and priority filtering.
- **Manual workflow studio (2 improvements):** read-only dry-run preview and last-progress-updated timestamp.
- **Voice controls (3 improvements):** en-IN/hi-IN/bn-IN speech input selection, repeat last spoken response, and stop speech output.
- **Communication flow (2 improvements):** editable message preview before messaging-app handoff; contact disambiguation before explicit call confirmation.

**Current B21 code-present count: 18 capability improvements.** This is not 50/50 completion. Remaining items must pass overlap audit and dependency/provider readiness before implementation. All external communication stays owner-confirmed; no background or covert actions are introduced.


### Additional B21 ledger entries (19–23)

19. M09 Knowledge Graph provenance labels (owner-entered vs legacy source unknown).
20. M18 Virtual Experiment Notebook (owner-saved, local, simulation-only, source-verification note).
21. M11 Creative Project Organizer (local saved drafts; not rendered media).
22. M10 Image Text Reader (user-selected local OCR, Latin and Devanagari scripts).
23. OCR recognizer cleanup on success/failure.
