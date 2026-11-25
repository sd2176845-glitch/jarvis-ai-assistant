# JARVIS Master Blueprint — Consolidated Architecture

**Status:** Consolidated planning baseline; documentation only. No Android/backend feature is newly implemented or verified by this document.
**Target:** Poco M4 Pro 4G; Chrome/browser-first development.
**Project boundary:** JARVIS is separate from Even. This blueprint applies only to JARVIS.

## 1. Non-negotiable consolidation rule

**Advanced = Basic + Enhanced + New Capabilities.**

- Basic capabilities belong inside the advanced module that owns and extends them; do not maintain duplicate standalone basic modules.
- Preserve every useful existing capability, requirement, permission boundary, acceptance test and implementation caveat. Consolidation changes ownership and structure, not the feature set.
- Every distinct capability remains a separately named sub-feature with its own status, dependencies and tests.
- Add a new top-level module only for a genuinely distinct responsibility.
- One canonical owner per capability. Other modules integrate through that owner rather than implementing a second copy.
- Keep code-present, planned, platform-dependent and verified statuses separate. A blueprint entry is not implementation evidence.

## 2. Canonical advanced modules

**Formatting rule:** every capability is listed line-by-line under exactly one canonical module. Use checkboxes for individual sub-features and add status/dependency/test notes as implementation begins. Do not create a new M-number for a sub-feature that belongs to an existing module.

### M01 — Owner Identity, Access & Privacy
- [ ] Owner sign-in, profile, session expiry and sign-out.
- [ ] OTP setup and recovery through supported providers.
- [ ] Guest mode and owner-only settings.
- [ ] Privacy lock, consent and permission explanations.
- [ ] Review, correct, export and delete owner-controlled data.
- [ ] Credential protection, data minimization and shared access-control rules.

### M02 — Core Voice, Conversation & Human Intelligence
- [ ] Speech recognition, text-to-speech and supported language handling.
- [ ] Wake phrase, listening/thinking/speaking/offline/standby states, stop and pause controls.
- [ ] Natural conversation, context tracking, intent understanding and concise clarification.
- [ ] Personality and empathetic response style without claiming human feelings.
- [ ] Emotion-signal interpretation from permitted words/voice/context; report uncertainty and allow correction—never claim 100% accuracy or make a diagnosis.
- [ ] Body-language analysis only on owner-selected media or an explicitly started camera session; describe observable gestures/expressions, not hidden thoughts, intentions or truthfulness.
- [ ] Social-context reasoning that offers possible explanations rather than certainty or labels.
- [ ] Owner-controlled conversational context and relevant memory integration; reuse M09 rather than creating another memory store.

### M03 — Knowledge, Search, Research & Learning
- [ ] Web search, source-aware answers, citations, timestamps and freshness indicators.
- [ ] Public API lookups, news/sports, translation and multilingual research.
- [ ] Scientific discovery, source comparison, contradiction checks and reality verification.
- [ ] Scheduled/owner-started research with reviewable reports.
- [ ] AI Learning Academy and Personal Education & Skill Academy.
- [ ] Public knowledge and learning content integrated with the single M09 knowledge vault.

### M04 — Universal App Intelligence & Knowledge Brain
- [ ] Catalogue installed apps using Android-supported package visibility.
- [ ] Discover newly installed apps when Android permits visibility.
- [ ] Research public app documentation, purpose, features, settings, compatibility and updates.
- [ ] Explain app features/settings using available app and version context.
- [ ] Refresh app knowledge through owner-invoked or bounded scheduled public-source checks.
- [ ] Show sources, freshness and what could not be inspected; do not promise awareness of every app/update.
- [ ] Keep app knowledge separate from app-control permission and private app data.

### M05 — Universal Application & Phone Control
- [ ] Launch apps and open supported Android settings handoffs.
- [ ] Operate supported features through documented APIs, intents or explicitly enabled Accessibility Service actions.
- [ ] Discover supported capabilities and show each integration's permission boundary.
- [ ] Handle changed/unknown interfaces cautiously; stop and ask instead of guessing.
- [ ] Run supported cross-app actions through M08 workflows.
- [ ] Never promise unrestricted control or bypass Android/app protections, authentication or protected screens.
- [ ] Require fresh, action-specific approval for sensitive actions and important setting changes.

### M06 — Communication Intelligence & Unified Communication Hub
- [ ] Resolve contacts and clarify duplicate/ambiguous recipients.
- [ ] Convert voice requests into editable SMS/message/email drafts.
- [ ] Show recipient and exact content before sending; require fresh approval.
- [ ] Place calls only after clear confirmation.
- [ ] Read messages only through explicitly granted, supported integrations.
- [ ] Provide supported WhatsApp, SMS, email, calendar and messaging handoffs.
- [ ] Support Hindi, Hinglish, English and other languages only as implemented and tested.

### M07 — Personal Secretary, Productivity & Life Management
- [ ] One shared notes, task, goals and reminder experience.
- [ ] Calendar handoff/connected schedule and daily briefing.
- [ ] Personal planning, checklists, routine planning and life-management overview.
- [ ] Personal Productivity Studio, asset/inventory tracking and vehicle records.
- [ ] Journey preparation and manually entered personal finance organization.
- [ ] Reuse existing task/reminder/calendar stores; do not add duplicate schedulers.

### M08 — Workflow Automation & Orchestration
- [ ] Convert requests into visible multi-step plans with dependencies and permissions.
- [ ] Workflow builder, templates, manual/time/event triggers and supported cross-app runners.
- [ ] Conditions, progress, bounded retries, interruption recovery and safe stop.
- [ ] Dry-run/test mode, history and owner-readable audit records.
- [ ] Independently perform only pre-authorized, reversible, low-risk actions within configured limits.
- [ ] Pause for fresh approval before consequential, sensitive, irreversible or uncertain actions; never claim safe autonomous decisions in every situation.

### M09 — Personal Memory, Knowledge Vault & Timeline
- [ ] Recent conversation context and owner-approved long-term preferences.
- [ ] Structured knowledge, provenance and searchable knowledge graph.
- [ ] Digital Time Machine, Personal Experience Timeline and idea evolution.
- [ ] Personal Knowledge Vault and Digital Legacy Archive.
- [ ] Review, correct, export and delete saved items; protect sensitive content.
- [ ] Provide one canonical memory/knowledge store shared through authorized module interfaces.

### M10 — Vision, OCR, Spatial & Media Intelligence
- [ ] OCR for owner-selected images and documents.
- [ ] Camera analysis only during a clearly indicated, owner-started session.
- [ ] Owner-selected image, video and audio understanding through supported models.
- [ ] Scene/object and spatial understanding with uncertainty labels.
- [ ] Preview what will be processed or sent; no silent camera, gallery or microphone monitoring.

### M11 — Creative Media & Creator Studio
- [ ] Image, video and presentation creation workflows.
- [ ] Media editing, project organization and creator workspace.
- [ ] Reuse M10 media inputs where appropriate.
- [ ] Respect ownership, licensing, copyright and provider terms.

### M12 — AI Development, Software & Data Intelligence Studio
- [ ] Coding help, project explanations, architecture and developer workshop.
- [ ] App, website and software design/build assistance.
- [ ] Generate reviewable code proposals, diffs, documentation and test plans.
- [ ] Analyze authorized datasets and create visualizations/reports.
- [ ] Self-improvement may identify issues and prepare proposed code/configuration changes.
- [ ] Test proposals in an isolated workspace and preserve backup/rollback evidence.
- [ ] Do not silently apply source/configuration changes, commit, deploy or replace an APK; require explicit approval for the specific change.

### M13 — Security Intelligence & Secure Evidence Vault
- [ ] Permission/build review and read-only security checks.
- [ ] Owner-approved interpretation of security events.
- [ ] Encrypt evidence and records in transit and at rest.
- [ ] Least-privilege access, strong authentication and dual approval for sensitive exports.
- [ ] Integrity hashes, access/chain-of-custody logs and encrypted backups.
- [ ] Retention/deletion controls, breach response and controlled disclosure.
- [ ] No stealth surveillance, interception or unauthorized access.

### M14 — Diagnostics, Reliability & Controlled Recovery
- [ ] Health checks, self-diagnostics, problem reports and sanitized logs.
- [ ] Troubleshooting, error analysis and developer repair handoff.
- [ ] Pre-authorized, reversible low-risk maintenance with visible limits.
- [ ] Backup/checkpoints, rollback, test gates and recovery journal.
- [ ] Keep diagnostics read-only by default.
- [ ] Code/configuration repair proposals require review, isolated tests and explicit approval before application.
- [ ] No arbitrary commands, silent source edits, APK replacement, permission weakening or deployment.

### M15 — Adaptive UI, Command Center & Accessibility
- [ ] JARVIS dashboard/orb, theme, status feedback and command center.
- [ ] Creator/project status, adaptive design proposals and preview/approval.
- [ ] Versioning, rollback, gestures, widgets and dashboard profiles.
- [ ] Visual-defect checks and dynamic information display.
- [ ] Accessibility options and universal accessibility mode.
- [ ] Preserve essential controls, readable contrast and owner override.

### M16 — Connected Ecosystem, Smart Environment & Collaboration
- [ ] Compatible smart-home devices and authorized Bluetooth accessories.
- [ ] Device discovery, explicit pairing, status and permission controls.
- [ ] Owner-approved collaboration spaces and role-based sharing.
- [ ] Cross-device sync with encryption, conflict handling and revoke controls.
- [ ] Reviewed plugin/extension registry with declared permissions and disable/uninstall controls.
- [ ] Local device communication and authorized connected-system integrations.
- [ ] A network connection alone never grants access to another device.

### M17 — Platform, Resource & Connectivity Intelligence
- [ ] Battery, storage, device-resource and authorized network-health information.
- [ ] Hardware compatibility research with trusted specifications.
- [ ] Local, Global and Hybrid Intelligence routing with offline fallback.
- [ ] Private local wireless network design with authenticated pairing and revocation.
- [ ] Research legally available satellite-internet services, coverage, approved terminals, cost and Indian authorization requirements.
- [ ] Explain that satellite internet needs compatible hardware and a supported provider/service; the phone/JARVIS cannot independently create satellite internet.
- [ ] Educational CubeSat/space-technology research only; no claim of direct satellite access or control.

### M18 — Simulation, Robotics & Engineering Intelligence
- [ ] Digital World Simulator, Digital Twin and Virtual Experiment Laboratory.
- [ ] On-screen 3D/AR workspace; clearly distinguish it from a true free-space holographic display.
- [ ] Engineering component library, compatibility research and 3D design tools.
- [ ] Authenticated modular interfaces for compatible, owner-approved robotics hardware.
- [ ] Begin with simulation and read-only telemetry; show simulated versus real states clearly.
- [ ] Before physical actuation, require hardware-specific safety review, supervised staged testing, manual override and accessible emergency stop.
- [ ] Stop physical actions safely on faults, disconnection or uncertain state; no unsupervised unrestricted robot control.

### M19 — Personal & Practical Intelligence Suite
- [ ] Disaster preparedness using official sources and clearly labelled offline guidance.
- [ ] Environmental information with source, timestamp and model/measurement limitations.
- [ ] Legal-document explanation with source passages and professional-review boundaries.
- [ ] Intelligent journey planning with approval before bookings or purchases.
- [ ] Smart asset inventory, vehicle intelligence and maintenance records.
- [ ] Collaborative project intelligence and owner-curated milestones.
- [ ] Reuse M03 research, M07 productivity and M09 memory rather than creating duplicates.

## 3. Legacy priority crosswalk — no capability is discarded

The original priority numbers remain traceable for audit, but they are no longer independent duplicate module definitions.

| Original source priorities / feature sets | Canonical owner |
|---|---|
| P0 Owner authentication and account setup | M01 |
| P1 Reliability and core voice control | M02, M05, M14 |
| P2 Online intelligence and API integrations | M03, M17 |
| P3 Anime, space and entertainment | M03 |
| P4 Memory and personal secretary | M07, M09 |
| P5 Vision and advanced integrations | M05, M10, M16 |
| P6 Personality, UI and quality | M02, M15 |
| P7 Owner feedback, diagnostics and repair handoff | M14 |
| P8 Human Intelligence 1.0 | M02 |
| P9 + P14 Universal App Intelligence / Knowledge Brain | M04 |
| P10 + P15 Universal Application / Phone Control | M05 |
| P11 Communication Intelligence | M06 |
| P12 Autonomous Intelligence | M08, with reasoning in M03/M07 |
| P13 + P18 AI Development / Creator Mode | M11, M12, M15 |
| P16 Security Intelligence | M01, M13 |
| P17 Autonomous Learning and Scheduled Research | M03, M07, M17 |
| P19 Auto Debug & Self-Repair | M14 |
| P20 Direct Call Execution After Confirmation | M06 |
| P21 Adaptive Self-Design and UI expansions | M15 |
| P22 Advanced Intelligence & Orchestration | M03, M08, M14 |
| P23 Connected Ecosystem, Device Intelligence & Media Vault | M10, M16, M17 |
| P24 Survival Intelligence & Legacy Continuity | M01, M09, M14, M17 |
| P25 Digital Time Machine | M09 |
| P26 Smart Context Awareness | M02, M05, M15 |
| P27 Personal Knowledge Vault & Life Event Recall | M09 |
| P28 Smart Routine Prediction | M07, M08 |
| P29 HumanFlow Multi-Voice & Natural Speech | M02 |
| P30–P33 Personal intelligence, advanced intelligence, expansion and robotics interfaces | M09, M10, M16, M18 |
| P34 Platform Expansion & Engineering Intelligence | M17, M18 |
| P35 Future Simulation, Robotics & Connected Systems | M16, M18 |
| P36 Workflow Automation & Orchestration | M08 |
| P37 Local, Global & Hybrid Intelligence / Independent Connectivity | M03, M17 |
| P38 Personal & Practical Intelligence Suite | M07, M09, M19 |
| Six approved studios: Learning, Productivity, Creative Media, Data Intelligence, Smart Home, Software Development | M03/M07, M07, M11, M12, M16, M12 |

**Crosswalk rule:** a source priority may map to more than one canonical owner only when its sub-features have genuinely different responsibilities. Each individual sub-feature must still have exactly one canonical owner.

## 4. Existing code inventory — preserve and re-home

The following remain code-present/test-needed exactly as recorded in the source README/blueprint; this document does not upgrade their status:
- Android app shell/orb dashboard; online AI client to Render/Groq; speech recognition and TTS; initial foreground wake-word service.
- Initial app launch, dialer/contact/alarm/SOS options; call/SMS event and accessibility integration.
- Assistant modes and initial personality prompt; mode-aware orb behavior.
- Command Center shortcuts; local notes; recent conversation context and clear-memory control; daily briefing.
- Browser web/weather shortcuts (not native live-data integrations); privacy panel and permission/settings handoffs.
- Initial read-only cybersecurity permission/build review (not a malware/network scanner).
- Camera/Google Lens shortcuts (not an in-app vision pipeline); reminder/alarm handoff (not JARVIS-owned scheduled notifications).
- Screen-time/calendar/FeatureKit additions only to the extent verified in the relevant branch/source; retain their stated verification caveats.

## 5. Independent feature cards and rollout

All M01–M19 modules are separate feature cards/workstreams, and their sub-features remain individually tracked. The full detailed requirements, acceptance tests, provider notes and audit history in the original blueprint and approved addenda remain the source material during migration; each item must be moved into its canonical module before the legacy duplicate heading is retired.

**Build approach:** organize all feature cards together now; implement in small independently testable batches. Do not attempt a single giant code change. For each sub-feature record: owner module, current status, dependency, permission/data scope, implementation files, tests, failure/rollback behavior and target-device verification. Merge only after review and passing checks.

Suggested implementation order:
1. M02/M05 core voice reliability and app/phone control regression.
2. M01/M06 owner identity, privacy and confirmed communication workflows.
3. M07/M09 notes, tasks, memory and reminders without duplicate stores.
4. M03/M04 app knowledge, research and source-aware integrations.
5. M14 diagnostics and controlled recovery.
6. M08 workflows and M15 Command Center/UI.
7. M10–M13 creative, vision, development, data and security workspaces.
8. M16–M19 connected devices, connectivity, simulation, robotics and practical intelligence.

## 6. Shared acceptance and safety gates

- Never put Groq/API keys, passwords, tokens or service credentials in Android source, APK, screenshots or Git history.
- Use HTTPS, validated responses, timeouts, bounded retries and source/freshness labels for online data.
- Request only permissions needed for the selected feature. No silent monitoring, stealth access, bypassing platform controls or unauthorized collection.
- Preview data sharing and require explicit approval for calls, messages, purchases, bookings, exports, location sharing, important settings and physical-device actions.
- Keep user data local by default where practical; disclose cloud processing and allow cancellation.
- Protect evidence and personal records with encryption, least privilege, auditability, backup and retention controls.
- Generated code, repairs, designs and engineering concepts require review, tests and owner authorization before release or physical use.
- Distinguish simulation from reality and draft concepts from validated engineering.
- Test offline behavior, denied permissions, network/provider failure, interrupted workflows, privacy/guest mode, recovery and regression on the Poco M4 Pro 4G.
- Never mark a feature verified without actual relevant test evidence.
- Keep JARVIS separate from Even.

## 7. Source-of-truth migration note

This consolidated architecture is a new reviewable baseline. The existing `JARVIS_MASTER_BLUEPRINT.md` is preserved unchanged on the source branch during this pass so no detailed requirement or audit record is accidentally lost. After owner review, migrate every detailed checklist item and its acceptance criteria into the canonical modules, then update the original master pointer and retire duplicate headings in a separately reviewed change. Do not merge to `main` without owner approval.


## 7. Implementation progress (branch-specific)

| Module / capability | Code status | Verification status |
|---|---|---|
| M02 — Core Voice, Conversation & Human Intelligence: Voice Studio | Added to `MainActivity`: installed offline TTS voice selection where exposed by Android, saved speech-speed presets (calm/normal/energetic), saved pitch presets (lower/normal/higher), and persistence through app preferences. | **Code added; Android build and device testing still required.** Voice availability and labels depend on the installed TTS engine. No guarantee that both male and female voices are installed. |

This table records only changes actually made on this development branch. It does not imply that the other modules are implemented.


## Latest implementation batch — Focus & Productivity Session

- **Code-present:** Added an owner-invoked Focus & Productivity Session entry to the Android Command Center.
- Supports a task label and 15-, 25-, or 45-minute countdown blocks, with a visible remaining-time display and an explicit Stop session control.
- The timer does not block apps, calls, or notifications and does not collect app-usage data. It runs only while the JARVIS process remains alive; process termination ends the in-memory timer.
- **Validation status:** Android build, unit tests, and Poco M4 Pro 4G device testing are still pending. Do not treat this feature as verified or production-ready.
- **Project boundary:** JARVIS only; the separate Even project is untouched.


### M17 — Device Resource Intelligence Dashboard (code-present; verification pending)
- Added a Command Center dashboard for device model/Android version, battery percentage and power connection state, app-data-volume storage total/available/used estimate, and RAM total/available/low-memory state.
- Uses Android public read-only APIs (`BatteryManager`, `StatFs`, `ActivityManager.MemoryInfo`). No new manifest permissions are requested.
- No app killing, memory clearing, settings changes, network inspection, or report upload.
- Storage is specifically the app data volume and may differ from total shared/user-visible storage. Network status is not shown because `ACCESS_NETWORK_STATE` is not currently declared.
- **Status:** code-present on the feature branch; build, CI, and Poco M4 Pro 4G device verification are pending.


### M02 — Voice Session Feedback & Foreground Stop Control (branch implementation)
- **Code-present:** The existing voice interface now exposes distinct listening, hearing, speech-processing, thinking/AI-connection, speaking, standby, speech-input failure, TTS failure, and AI-backend-offline/local-command-ready status feedback.
- Added a visible owner-invoked foreground stop control that cancels the foreground speech-recognition session and stops TTS playback.
- This foreground stop control does not stop the separate background wake-word service; its existing independent stop control remains authoritative.
- No new Android permission, background monitoring behavior, or external data collection was added.
- **Validation:** CI for commit `1881e6f349f1dde673ce0d551da210e69e4c2f02` passed (run `45038787391`; debug APK artifact `14760812400`). Target Poco M4 Pro 4G testing remains pending. Do not mark device-verified until actual tests pass.


## M07 implementation update — Structured Task Manager
The existing Tasks & reminders hub is being upgraded in place with a local structured task list: create tasks, mark complete/reopen, and delete. Legacy free-form notes remain intact in their existing keys and are shown separately; no duplicate notes store or scheduler is introduced. This implementation is code-present on the feature branch only. CI and Poco device verification are pending.


## M08 implementation update — Manual Workflow Studio
The existing Command Center now includes a Workflow Studio foundation. Owners can create a named checklist (up to 12 steps), mark steps complete, save progress, reset a checklist and delete it. The workflow records use a dedicated local preference key and do not duplicate the M07 task list or notes. This first increment is deliberately manual-only: no background triggers, autonomous execution, cross-app actions, calls/messages or device-setting changes. Code is present on the feature branch. CI passed on runs #479 and #480; run #480 uploaded debug artifact `14761207706`. Target-device verification remains pending.


### M09 implementation progress — Personal Memory Vault foundation
- **Code-present on feature branch:** Command Center now opens an owner-controlled vault over the existing local notes and recent AI conversation stores.
- Review and correction use the existing notes store; export uses Android's explicit share chooser; notes deletion and recent-conversation clearing are separate, confirmed actions.
- No parallel memory database, background capture, or automatic retention of new information was added.
- **Code-present:** owner-created Personal Timeline is now implemented within the existing vault. It stores manually entered milestones/events with creation timestamps, supports list/detail/individual delete, and is included in the existing owner-triggered export. It uses the existing JARVIS preferences store; no second memory database or background capture is added.
- **Still planned:** explicit provenance/source attribution and a richer queryable graph view. Owner-created knowledge graph nodes/links, versioned notes and an encrypted on-device legacy archive are tracked in M09 Batch B02 below.
- **Validation:** timeline code commit 5296fabe1d3670201dffeb72801bfc219f4f86c8 passed GitHub Actions run #495 (45040755446), including backend syntax validation, Gradle debug APK build and artifact upload 14761891383. Poco M4 Pro 4G device testing remains pending; no separate unit-test/lint steps were run.


## 7. Implementation status addendum — M14 Batch B01 (2026-11-22)

The first M14 implementation batch contains three separately tracked sub-features, all owned by **M14 — Diagnostics, Reliability & Controlled Recovery**:

1. **App Health Check — code-present, implementation CI passed; device test pending.** Extends the existing Command Center System safety check entry. It reports app-level capability availability and optional permission states. It does not duplicate M17's battery/storage/RAM dashboard or the existing cybersecurity permission/build review.
2. **Sanitized Diagnostic Report Export — code-present, implementation CI passed; device test pending.** Exports only the owner-triggered app-health report through Android's share chooser. It excludes contacts, messages, calls, conversation content and raw logs; it is not automatically transmitted.
3. **Local Diagnostic History — code-present, implementation CI passed; device test pending.** Stores at most 10 owner-requested timestamp/pass-count summaries in the existing private JARVIS preferences store. It does not retain report text or personal content; the owner can review or clear it.

**Batch boundary:** no new Android permission, second diagnostics module, background collection, private-content access, automatic repair, or remote upload. **CI build verified:** current batch head `22efe6293bd5953c5af34d9dab4f806185fb3f8c` passed GitHub Actions run #528 (`45043208526`), including backend syntax validation, Gradle debug APK build and artifact upload (`14762108881`). Android unit tests/lint were not separate workflow steps. All three sub-features remain **physical-device test pending** on the Poco M4 Pro 4G.


## 8. Implementation status addendum — M09 Batch B02 (2026-11-22)

Three separate capabilities have been added inside the existing Personal Memory Vault:

1. **Owner-Curated Knowledge Graph — code-present; CI passed; device test pending.** Manual nodes and typed links; deleting a node also deletes its linked edges. No automatic profiling or background capture.
2. **Idea Evolution / Versioned Notes — code-present; CI passed; device test pending.** Owner-triggered snapshots of the existing notes text, capped at 20 versions, with preview and confirmation before restoring to the existing notes store.
3. **Encrypted Digital Legacy Archive — code-present; CI passed; device test pending.** AES-GCM encrypted local snapshot using an Android Keystore device-bound key. Includes saved notes, owner-created timeline, graph and idea versions; excludes recent conversation, contacts, messages and call data. Owner can create/replace, authenticate/decrypt-verify, and delete. It is not a portable password-protected backup; loss of the device Keystore key can make it unrecoverable.

**Scope:** no new Android permissions, no second memory database, no automatic capture, no background backup, and no remote upload. The three features passed backend syntax validation, Gradle debug APK build and artifact upload on run #537 (`45043884161`, artifact `14762177978`) and latest tracker-linked run #538 (`45043923314`, artifact `14762319147`). Android unit tests/lint were not separate workflow steps. All three remain physical-device test pending on the Poco M4 Pro 4G.


### M03 — Language & Learning Studio (Batch B03; implementation status tracked separately)
- Added three owner-invoked tools to the existing Command Center: Hindi/Bengali/English translation with optional user-triggered TTS playback; text summary/simple explanation; and a vocabulary learning card.
- Uses the existing HTTPS backend through an isolated request method that does not attach or save recent conversation memory. Only text explicitly entered into the studio is submitted after a disclosure.
- No clipboard/gallery auto-read, background capture, additional permission or persistent learning store.
- **Status:** code added on `feature/m03-language-studio-3pack`; CI/build and target-device verification pending. TTS language support depends on installed Android voices.


**B03 verification update (2026-11-22):** final code head `4caf2cacef9ca55cac77b4ef05b990bee9c24545` passed GitHub Actions run #547 (`45044601421`): backend syntax validation, Gradle debug APK build and artifact upload. Artifact `14762243808`, SHA-256 `6915af375edcef95663da8204b636c6ec971bf991a482461fb4c108be8d15eef`. No separate unit-test/lint task ran; target-device testing remains pending. Draft PR #25 is open and unmerged.


### M04 — Universal App Intelligence (Batch B04; implementation status tracked separately)
- Added a read-only Installed App Catalogue using launcher-intent package visibility; app details (version, target API and install/update timestamps); and a requested-permission declaration viewer.
- App knowledge is kept separate from app control. This module does not launch apps, inspect private app data, infer runtime permission grants, alter settings or upload data.
- Uses a scoped launcher-intent `<queries>` declaration, not `QUERY_ALL_PACKAGES` or a new runtime permission.
- **Status:** code added on `feature/m04-app-intelligence-3pack`; CI/build and target-device verification pending.


**B04 verification update (2026-11-22):** code head `7d06870c70630171b69fbd9f6bc3935e6fb88fdc` passed GitHub Actions run #553 (`45044836017`): backend syntax validation, Gradle debug APK build and APK artifact upload. Artifact `14762504922`, SHA-256 `77eb8572bc40e7e53c22515789486388cba95d9287e3c976ee4ff8387a2d7f65`. No separate unit-test/lint task ran; target-device testing remains pending. Draft PR #26 is open and unmerged.


### M05 — Public Knowledge Lookups (Batch B05; implementation status tracked separately)
- Added three independent owner-invoked, read-only integrations to the existing Command Center:
  1. Country lookup by name or ISO code via countries.dev.
  2. India six-digit PIN lookup via PostalPincode.in, showing all returned post offices and clearly labelling the result as informational.
  3. English dictionary lookup via dictionaryapi.dev.
- Each lookup asks for confirmation before sending only the value the owner entered. No contact, notes, conversation, location or background data is attached.
- HTTPS GET, bounded timeouts and response-size cap; no API keys or new permissions. Public data providers can be unavailable or change response formats; the app reports errors instead of claiming success.
- **Status:** code added on `feature/m05-public-knowledge-3pack`; CI/build and target-device verification pending. Dictionary scope is English only; PIN data provider is community-maintained and has no production SLA.


**B05 verification update (2026-11-22):** final head `76877d030adcf36b187bf01e263ce52051b10661` passed GitHub Actions run #563 (`45045200384`): backend syntax validation, Gradle debug APK build and APK artifact upload. Artifact `14763530517`, SHA-256 `b2929bd0d13c164ba79c2263374a3e2749bc71e3ee2abbdc3f6e1196cbec5457`. No separate unit-test/lint task ran; target-device verification remains pending. Draft PR #27 is open and unmerged.


### M06 — Live Information Studio (Batch B06; implementation status tracked separately)
- Added three owner-invoked tools to the existing Command Center: city weather via Open-Meteo (manual city entry, no GPS); currency conversion via Frankfurter v2 (reference rates, not bank/live market quotes); and India public-holiday lookup via Nager.Date v4.
- All require confirmation and send only the entered city, amount/currency codes or year. No precise location or personal memory is attached.
- Uses HTTPS GET, bounded timeouts and response-size cap; no API key or new Android permission.
- Weather and holiday datasets are provider-dependent and must not be represented as guaranteed forecasts or exhaustive official calendars.
- **Status:** code added on `feature/m06-live-info-3pack`; CI/build and target-device verification pending.


**B06 verification update (2026-11-22):** code/documentation head `7b2c1c1e88fc299d36af3e198a29449e1f0146c0` passed GitHub Actions run #572 (`45045389085`): backend syntax validation, Gradle debug APK build and APK artifact upload. Artifact `14763293374`, SHA-256 `6648c8850934b605c00b918a49b579eb4ab317ab45a9d39ca31d62b954391e28`. No separate unit-test/lint task ran; target-device verification remains pending. Draft PR #28 is open and unmerged.


### M10 — AI Learning Academy (Batch B07; implementation status tracked separately)
- Added three guided learning modes: Coding Tutor (Python/Java/Kotlin/JavaScript), Science Explorer (Physics/Chemistry/Biology/Space & Earth Science), and Technology Learning Roadmap (Android, web, AI basics, defensive digital safety, computer-science foundations).
- Each mode uses a selected difficulty level and owner-entered topic/goal, then generates structured lessons or staged roadmaps through the existing memory-isolated AI request path.
- Explicit confirmation before submission; only selected choices and topic are sent. No conversation-memory attachment, saved notes, new permissions or persistent learner profile.
- Safe-learning boundaries exclude harmful code, hazardous experiments and offensive cyber instructions.
- **Status:** code added on `feature/m10-learning-academy-3pack`; CI/build and target-device verification pending.


**B07 verification update (2026-11-22):** code/documentation head `a9cd77178709102a9bc746840f9c6e44b0f496b8` passed GitHub Actions run #581 (`45045554373`): backend syntax validation, Gradle debug APK build and APK artifact upload. Artifact `14763640351`, SHA-256 `648188031f44642066caf26da29fe54d3a14dec55c84095f7468ed01a2672ccf`. No separate unit-test/lint task ran; target-device verification remains pending. Draft PR #29 is open and unmerged.


### M11 — Software Development Studio (Batch B08; implementation status tracked separately)
- Added Project Architect, Bug & Error Explainer, and Code Quality Review as three separate tools.
- Project Architect produces a reviewable project plan; Bug Explainer analyses only pasted error/context; Code Review assesses only the owner-pasted excerpt. These do not execute code, create files, deploy software or claim whole-project security verification.
- Uses the existing memory-isolated AI request path, explicit pre-send confirmation and a 5,000-character input limit. No saved memory, notes, contacts, new permission or persistent code archive.
- Unsafe code categories (malware, credential theft, covert surveillance, backdoors, evasion, unauthorized access) are excluded.
- **Status:** code added on `feature/m11-software-dev-studio-3pack`; CI/build and target-device verification pending.


**B08 verification update (2026-11-22):** corrected code head `b922a78b5e9c2956c7f4237331633939cb701861` passed GitHub Actions run #591 (`45045770900`): backend syntax validation, Gradle debug APK build and APK artifact upload. Artifact `14763955311`, SHA-256 `84ef58a3e5e25fa086c270be6e4a8002019ec8143f903ce088a35a97b1f209fa`. Initial runs #586–#590 failed on two extra closing parentheses in nested dialog callbacks; corrected in the passing head. No separate unit-test/lint task ran; target-device verification remains pending. Draft PR #30 is open and unmerged.


### M12 — Data Intelligence Center (Batch B09; implementation status tracked separately)
- Added a local-first CSV Dataset Profiler, Data Quality Inspector and Aggregate AI Insight Report.
- Parsing and structural/numeric statistics run locally with limits of 50,000 characters, 1,000 rows and 50 columns. Original CSV is not saved.
- AI report preview sends only anonymized column labels and aggregate counts/statistics after explicit confirmation; raw CSV rows and cell values are excluded.
- Structural checks do not establish factual correctness or cover all semantic anomalies. User is warned not to paste sensitive/confidential records.
- **Status:** code added on `feature/m12-data-intelligence-3pack`; CI/build and target-device verification pending.


**B09 verification update (2026-11-22):** code head `0b2a94bdbf88c5cd74c1f71cda62057096f0e16a` passed GitHub Actions run #602 (`45046009488`): backend syntax validation, Gradle debug APK build and APK artifact upload. Artifact `14763571783`, SHA-256 `213c503c4a5cc0ad81435fe83a813f7a0a594ed734cdb3a0704d43e72fbd3a8c`. No separate unit-test/lint task ran; target-device verification remains pending. Draft PR #31 is open and unmerged.


### M13 — Personal Productivity Studio (Batch B10; implementation status tracked separately)
- Upgraded the existing structured task list with Project Workspace (project grouping), Priority Matrix (four owner-selected priority labels) and Weekly Review (last-seven-day task creation/current-status summary).
- Reuses the existing `structured_tasks_v1` JSON records by adding optional `project` and `priority` fields; no parallel task or project database.
- Weekly Review avoids claiming completion dates because the current task schema does not track completion timestamps.
- No network request, new permission, background scheduler or automatic task execution.
- **Status:** code added on `feature/m13-productivity-studio-3pack`; CI/build and target-device verification pending.


**B10 verification update (2026-11-22):** rebased code/documentation head `5c6b3929c99aafdfd589d83c0921cdf8a3684a4f` passed GitHub Actions run #615 (`45046258241`): backend syntax validation, Gradle debug APK build and APK artifact upload. Artifact `14764031146`, SHA-256 `ee320e43e1d627326173b5537b151eb7df29dd42b7c118dc2bb4bd3fe2c19582`. No separate unit-test/lint task ran; target-device verification remains pending. PR #32 was opened before rebasing; its head reference may need a fresh PR after the rebase.


**B10 verification update (2026-11-22):** code/documentation head `f82347780326ecdc4a6bcfd10715a9488ba11f77` passed GitHub Actions run #620 (`45046350249`): backend syntax validation, Gradle debug APK build and APK artifact upload. Artifact `14762974382`, SHA-256 `3186014ea76112d07da2dde3e9595c5c5e70af900a5348522d7c881e12902571`. No separate unit-test/lint task ran; target-device verification remains pending. Replacement draft PR #33 is open and unmerged.


### M15 — Creative Media Studio (Batch B11; implementation status tracked separately)
- Added Video Script Planner, Storyboard & Shot List, and Presentation Builder.
- These are text-first planning tools using the existing memory-isolated AI request path. They produce scripts, scene plans and slide outlines; they do not render video/images or create PPTX files.
- Explicit confirmation before sending the creative brief; no conversation memory, notes or contacts attached, no persistent creative archive or new permission.
- Original-content, factual-accuracy, likeness-consent and safe-production boundaries are included.
- **Status:** code rebased on latest M13 parent on `feature/m15-creative-media-3pack`; final rebased CI/build and target-device verification pending.


**B11 verification update (2026-11-22):** rebased head `0aa72a61b9d1172e7d1c55d44e0d6480cf465f14` passed GitHub Actions run #636 (`45046654060`): backend syntax validation, Gradle debug APK build and APK artifact upload. Artifact `14764056646`, SHA-256 `7a73861a89d41ec3480a92820684251fd9e1197eff59ddb45f831180d2fd6aab`. No separate unit-test/lint task ran; target-device verification remains pending. Replacement draft PR #35 is open and unmerged.


**B11 final verification update (2026-11-22):** rebased head `9afe85fc318780b6605f0808bd5042a53ecc2216` passed GitHub Actions run #639 (`45046733733`): backend syntax validation, Gradle debug APK build and APK artifact upload. Artifact `14763344964`, SHA-256 `155e413f753aaee977972cf8a5e5f7061f7c2dc38ea65a8c675ea8680d05f0ab`. No separate unit-test/lint task ran; target-device verification remains pending. Draft PR #35 is open and unmerged.


### M13 — Creative Media Studio (Batch B10; implementation status tracked separately)
- Added Image Concept Builder (original image prompt + alt text), Video Storyboard Studio (timed scenes, narration and captions), and Presentation Planner (slide outline and speaker notes).
- These are text-planning tools only; no media files are rendered or saved. Each requires a manually entered brief and explicit pre-send confirmation via the memory-isolated AI request path.
- Copyright/safety boundaries require original age-appropriate concepts, no living-artist imitation, copyrighted lyrics/scripts/characters, graphic violence or harmful body ideals. Presentation output must flag facts for verification and avoid invented sources.
- **Status:** code added on `feature/m13-creative-media-studio-3pack`; CI/build and target-device verification pending.


**B10 verification update (2026-11-22):** final head `14b77ad900299a9a95c52781463aa0763c11733a` passed GitHub Actions run #648 (`45047351132`): backend syntax validation, Gradle debug APK build and APK artifact upload. Artifact `14764081799`, SHA-256 `b22ac837311db214740419c8deb4c988a6c5f8fb54bc0baa360eac320b67e743`. No separate unit-test/lint task ran; target-device verification remains pending. Draft PR #36 is open and unmerged.


### M16 — Smart Home Studio (Batch B11; implementation status tracked separately)
- Added Device Setup Guide, Local Room Device Inventory (up to 30 manually entered labels), and Smart Routine Planner.
- Setup and routine guidance use explicit owner submission through the memory-isolated AI path. Inventory stays local in the existing private preferences store.
- This increment does not scan networks, discover devices, pair, control devices, send device commands, store credentials or run unattended automations. It is a safe planning/inventory foundation, not a full smart-device integration.
- **Status:** code added on `feature/m16-smart-home-studio-3pack`; CI/build and target-device verification pending.


**B11 verification update (2026-11-22):** corrected final code head `d289065be47bc1fef289dc9b8d112ec345345940` passed GitHub Actions run #659 (`45047777962`): backend syntax validation, Gradle debug APK build and APK artifact upload. Artifact `14763894323`, SHA-256 `f0118febd3e755b3c3f797bd3f3b99b2aa205e4357214c51e3717491645bebe3`. Initial runs #653–#658 failed on a routine-dialog builder-chain syntax error; corrected in the passing head. No separate unit-test/lint task ran; target-device verification remains pending. Draft PR #37 is open and unmerged.


**B11 final documented-head verification:** `eaf5634ca4f8f652e242ba44bec940c9db2e7afe` passed GitHub Actions run #662 (`45047913711`): backend syntax validation, Gradle debug APK build and APK artifact upload. Artifact `14764123874`, SHA-256 `15e3ed72d5d98969b4eb4fd4981dc1a88eb0cb277e8cdf245eb95f807d19dcec`. No separate unit-test/lint task ran; Poco device verification remains pending.


## M17 implementation status — Batch B12 (2026-11-22)
- **Network Health Snapshot — code-present:** read-only active-network capability summary using Android `ConnectivityManager`/`NetworkCapabilities`; reports transport category, internet capability/validation and metered state. Uses only `ACCESS_NETWORK_STATE`; no SSID/IP/MAC/password reading, scanning, traffic inspection or settings changes.
- **Offline Capability Matrix — code-present:** static design-level guide separating local foundations from online AI/provider features. It explicitly does not claim to test every feature at runtime.
- **Connectivity Strategy Planner — code-present:** owner-confirmed Local-only, Hybrid, or Satellite internet/CubeSat educational planning through the existing memory-isolated AI path. It does not configure networks, scan/connect, provide transmission/frequency/interference instructions, or enable unauthorized access.
- **Tests:** three focused JUnit tests added; CI is configured to run `testDebugUnitTest` before `assembleDebug`. CI result is pending until the new branch workflow completes.
- **Device verification:** pending on Poco M4 Pro 4G. These capabilities are not marked verified.
- **Anti-duplication:** this extends M17 beyond the existing M17 battery/storage/RAM dashboard; it does not add a second resource dashboard or modify the separate Even project.


## M18 implementation status — Batch B13 (2026-11-22)
- **Digital World Simulator — code-present:** owner-selected ecosystem, planetary motion, water-cycle/climate and sustainable-community scenarios; text-only conceptual models with variables, outcomes, assumptions and limitations.
- **Virtual Experiment Lab — code-present:** owner-selected Physics/Chemistry/Biology/Earth-space concepts; hypothesis and variable explanations using harmless thought experiments only, not real-world procedures.
- **3D Invention Studio — code-present:** text-only design briefs for benign everyday, accessibility, sustainability and educational objects; no CAD file or manufacturing-ready instructions.
- **Safety boundary:** no weapons, attacks, harmful devices, hazardous experiments, surveillance/bypass equipment, dangerous fabrication, physical robot control or infrastructure actuation.
- **Integration:** existing Command Center and memory-isolated AI request path; explicit owner confirmation; no new permission or persistent store.
- **Tests:** three focused JUnit tests added; inherited CI runs unit tests before debug APK build. CI result pending for this branch.
- **Device verification:** pending on Poco M4 Pro 4G; not marked verified.


## M19 implementation status — Batch B14 (2026-11-22)
- **Emergency Readiness Planner — code-present:** owner-selected flood, cyclone/severe storm, heatwave, earthquake or general-emergency preparedness checklist. General planning only; no live alerts/forecasts/orders, invented sources or risky rescue guidance.
- **Legal Document Explainer — code-present:** owner-selected text excerpt is explained in plain language with terms, dates, obligations, ambiguities and questions for a qualified professional. Explicit redaction and confirmation; informational only, not legal advice; no excerpt saved to memory.
- **Vehicle Maintenance Log — code-present:** local owner-managed service history (nickname, date, optional odometer, summary), with add/list/remove/clear and a 50-record cap. No VIN/registration/insurance identifiers, cloud sync or duplicate scheduler; due reminders must reuse M07 tasks.
- **Tests:** three focused JUnit tests added; inherited CI runs unit tests before APK build. CI result pending for this branch.
- **Device verification:** pending on Poco M4 Pro 4G.
- **Anti-duplication:** no second task/reminder store, memory store or smart-home inventory. JARVIS only; Even untouched.


## 8. Implementation status addendum — M01 Batch B15 (2026-11-22)

**Owner Privacy Studio — code-present; CI and Poco device verification pending.** This batch upgrades the existing Privacy controls entry rather than creating a duplicate menu module.

1. **Permission & Access Review:** read-only runtime permission and special-access status for microphone, contacts, phone state, calling, SMS receipt, notifications (Android 13+), overlay and JARVIS accessibility. Offers Android settings handoffs; it does not request or alter permissions.
2. **Local Data Inventory:** reports counts/character lengths only for existing notes, recent AI context, structured tasks, workflows, timeline, knowledge graph, idea versions and vehicle records, plus encrypted archive presence. It does not show underlying content or create a second data store. Malformed JSON is surfaced as unreadable, not empty.
3. **Sanitized Privacy Summary Export:** explicit owner confirmation followed by Android share chooser; exports permission states and aggregate counts only, excluding private content and identifiers.

**Files:** `OwnerPrivacyStudio.java`, `OwnerPrivacyRules.java`, `OwnerPrivacyRulesTest.java`, and the existing `MainActivity.showPrivacyOptions()` method upgraded in place. No new permissions, background collection, network requests, data stores or API keys.

**Tests:** three JUnit tests cover permission wording, JSON count/corruption behavior, and content-free export boundaries. Final code head `34941b26529b36519f27bae775d0f85a588443b1` passed GitHub Actions run #711 (`45051230977`): backend syntax validation, `gradle testDebugUnitTest`, `gradle assembleDebug`, and APK artifact upload `14765930477`. Initial CI runs #707–#710 caught an Android `org.json` local-test stub issue; the rule was rewritten in pure Java and the final test/build passed. Poco M4 Pro 4G physical-device verification remains pending. Keep the batch stacked on M19 PR #40; do not merge without owner approval. JARVIS only; Even untouched.


## M02 implementation status — Batch B16 (2026-11-22)

**Owner-Reviewed Voice Transcript Correction Gate — code-present; CI and device verification pending.** The existing `understandVoiceCommand()` path is upgraded in place. If the correction endpoint changes recognized words, JARVIS shows the original and suggested wording and waits for the owner to choose corrected text, original text, edited text, or cancel. Unchanged transcripts continue without interruption. Empty edits and cancellation do not execute commands. The transcript is not persisted by this feature.

**Files:** `VoiceTranscriptReviewRules.java`, `VoiceTranscriptReviewRulesTest.java`, and existing `MainActivity.java`. Three focused JUnit tests cover the comparison and fallback rules. No duplicate voice engine, command router, transcript database, new permission, or background capture is introduced. CI pending; Poco M4 Pro 4G testing pending. Draft PR #42 is stacked on M01 PR #41. JARVIS only; Even untouched.


### M15 — Display & Accessibility Studio (Batch B17; implementation status tracked separately)
- **Code-present:** owner-selectable Standard, Large, Extra-large and High-contrast app-local readability profiles, with reset. Applies to existing JARVIS status and response text and persists in the existing private preferences store.
- **Boundary:** does not alter Android system settings, screen density, permissions, or background accessibility services.
- **Validation:** three focused JUnit tests; CI and Poco M4 Pro 4G physical-device verification pending.


### M16 — Authorized Bluetooth Accessory Handoff (Batch B18; status tracked separately)
- **Code-present:** owner-entered Bluetooth accessory labels share the existing Smart Home inventory; Android Bluetooth settings are opened only after an explicit tap.
- **Safety boundary:** no background scanning, identifier collection, automatic pairing, remote control or credentials. Unpairing is performed by the owner in Android settings; removing a label does not unpair the physical accessory.
- **Validation:** four focused JUnit tests; CI and Poco M4 Pro 4G physical-device verification pending.


### M17 — Hardware Compatibility Snapshot (Batch B19; status tracked separately)
- **Code-present:** extends the existing read-only Device Resource Intelligence report with Android-reported CPU cores, supported app ABIs, low-RAM flag and display metrics.
- **Boundary:** informational hints only; no benchmarking, stress tests, settings changes or private-data inspection.
- **Validation:** three focused JUnit tests; CI and Poco M4 Pro 4G physical-device verification pending.


### M17 — Battery Health & Charging Snapshot (Batch B20; status tracked separately)
- **Code-present:** extends the existing read-only resource dashboard battery section with Android-reported charging state, health category and temperature.
- **Boundary:** one-shot broadcast snapshot only; no background monitoring, alerts, uploads, persistence or repair diagnosis.
- **Validation:** three focused JUnit tests; CI and Poco M4 Pro 4G physical-device verification pending.


## M19 — B26 Smart Asset Inventory continuation (2026-11-24)
- **20/20 inventory capabilities code-present:** the B26 inventory continuation now includes owner-selected receipt/manual references alongside the original 15 capabilities, read-only data-quality and purchase-age reports, purchase-date sorting and inclusive purchase-date range filtering. The vehicle odometer consistency audit is a separate M19 Vehicle Intelligence feature and is not counted in the inventory-only total.
- **Receipt/manual reference behavior:** one owner-selected PDF/JPEG/PNG/WebP content URI per existing asset record; stores only sanitized name, MIME and URI in the existing local inventory record. Uses Android's Storage Access Framework and a persistable READ grant only; no broad storage permission, file copy, scan, OCR, network upload or new storage key. Explicit open/replace/remove actions; removal and inventory deletion/clear release the saved read grant after successful local update. Existing export excludes attachment metadata.
- **Verification:** GitHub Actions run #1022 (`45352808375`) passed backend syntax, Android unit tests, debug APK build and artifact upload on code head `244ecd9a6da109de52744073ac0c40a21aa35868`. Poco M4 Pro 4G physical-device testing remains pending.
- **Remaining Priority 38 scope:** reminder integration B27 and encrypted backup/restore B28 are code-present and CI-verified; Poco device tests remain pending. Other Priority 38 modules remain separate work. B26–B28 do not mark the full Priority 38 suite complete.


## B27 — Priority 38 warranty reminder integration (2026-11-24)
- **Code-present:** owner can choose a valid current/future warranty item and set a reminder date/time no later than the warranty end date.
- **Integration:** reuses existing ReminderStore/ReminderReceiver, notification channel, reboot restoration and snooze controls; no duplicate scheduler or new permission.
- **Privacy/reliability:** reminder title contains only the item name; notifications-disabled state is handled with a settings handoff; failed scheduling rolls back the new reminder record.
- **Tests:** bounded reminder-title helper covered by focused JUnit test. CI/build and Poco M4 Pro 4G runtime verification pending.
- JARVIS only; Even untouched.


**B27 verification update (2026-11-24):** final documented head `4a41e42681b855e2482926ab5d2806727f7a9d89` passed GitHub Actions run #1032 (`45360490561`): Android unit tests, debug APK build and artifact upload succeeded. Poco M4 Pro 4G runtime verification of notification delivery, reboot restoration and snooze behavior remains pending.


## B28 — Encrypted Asset Inventory Backup & Restore (2026-11-24)
- **Canonical owner:** M19 Smart Asset Inventory; upgrades the existing inventory menu and uses the existing `owner_asset_inventory_v1` SharedPreferences key. No duplicate inventory store or backup subsystem was added.
- **Export:** owner selects the destination through Android Storage Access Framework. The versioned `.jva` archive contains only allowlisted item name, category, purchase-date label, condition, warranty end date, notes and creation timestamp. Receipt/manual content URIs, provider filenames, MIME metadata, persisted document permissions, app credentials and unrelated JARVIS data are excluded.
- **Encryption:** AES-256-GCM authenticated encryption with a fresh 16-byte salt, 12-byte IV, 128-bit authentication tag and PBKDF2-HMAC-SHA256 (150,000 iterations). Passphrase length is 12–128 characters; it is held transiently and is not saved. Lost passphrases cannot be recovered.
- **Restore:** selected archive is size-bounded, decrypted and authenticated before JSON parsing; version, required fields, existing inventory validators and the 100-record limit are checked. A preview offers merge (adds only items without an existing same-name/category match) or replace (separate destructive confirmation). Replace releases old persisted document read grants only after the preference update succeeds; original provider files are never deleted. Existing warranty reminders remain unchanged.
- **Failure behavior:** wrong passphrase, altered ciphertext, unsupported header/version, malformed records, oversized files or failed preference commits do not alter the existing inventory. No network transfer or new Android permission is used.
- **Tests:** focused JUnit tests cover round-trip encryption, ciphertext confidentiality, wrong-passphrase rejection, tamper detection and malformed/short-password rejection.
- **CI/build:** PASS — GitHub Actions run #1037 (`45362567473`) on exact final feature-code head `1c4c569220f627c76076bcf90a8e5439c1cf34ea`; backend syntax validation, Android unit tests, debug APK build and artifact upload succeeded. Artifact `jarvis-debug-apk`, ID `14904838906`, size 23,277,086 bytes; uploaded ZIP SHA-256 `ba55fb8809f04496d0606985111d52f13aaf7f73cf5ae51a25230f57f8ee597a`.
- **Poco M4 Pro 4G:** physical-device verification remains pending: create/open archive through document providers, wrong-password/tamper rejection, merge/replace behavior, and document-permission release.
- **Anti-duplication:** extends the existing asset inventory export/menu and preference key; does not create another scheduler, document manager, or data store. JARVIS only; Even untouched.


## B29 — Read-only Life Management Overview (2026-11-24)
- **Canonical owner:** Priority 38 Life Management Intelligence; extends the existing Tasks & Reminders hub with an overview action rather than adding a second planner or dashboard store.
- **Existing stores reused:** structured tasks (`structured_tasks_v1`), owner-created reminders (`jarvis_scheduled_reminders_v1`), manual checklists (`manual_workflows_v1`) and owner-curated personal timeline (`memory_timeline_v1`). No records are copied to a new store or changed by the overview.
- **Summary:** open/completed tasks, overdue tasks, tasks due today and in the next 7 days, malformed non-empty due dates needing review, upcoming reminder counts and next reminder, saved manual checklist count and personal timeline entry count. Shows only the first five due-task lines and indicates additional items.
- **Boundaries:** local read-only aggregation; no network, location, new permission, automatic calendar import, notification changes, task edits or reminder scheduling. Calendar events are explicitly described as not imported or monitored. Guest mode remains blocked from private overview data.
- **Tests:** focused JUnit coverage for due-date buckets, completed/open states, malformed dates, upcoming and expired reminders, empty stores, unavailable optional stores and inconsistent inputs.
- **CI/build:** PASS — GitHub Actions run #1039 (`45363566421`) on exact feature-code head `04c1f861290ed23ef780993f1b23218d80e7a2eb`; backend syntax validation, Android unit tests, Java compilation, debug APK build and artifact upload succeeded. Artifact `jarvis-debug-apk`, ID `14904734084`, size 23,275,421 bytes.
- **Poco M4 Pro 4G:** physical-device verification remains pending for local date/time boundaries, actual reminder entries, the overview refresh action and guest-mode behavior.
- **Anti-duplication:** extends the existing Tasks & Reminders hub and reuses canonical task/reminder/workflow/timeline stores. It does not create a new calendar, planner, task database, timeline or reminder scheduler. JARVIS only; Even untouched.


## B30 — Offline Emergency Preparedness Pack (2026-11-24)
- **Canonical owner:** Priority 38 Disaster Preparedness Intelligence; extends the existing Practical Intelligence / Emergency Readiness area with a separate offline reference option. It does not duplicate or replace the existing AI-generated preparedness brief.
- **Topics:** General emergency, flood, cyclone/severe storm, earthquake and heatwave.
- **Offline behavior:** static locally bundled guidance remains readable without network. It clearly states that the pack is not an official warning, live forecast or evacuation order. It directs the owner to follow local authorities and stay with a parent/guardian or trusted adult. India's nationwide emergency number 112 is included with the official MHA reference.
- **Official-source handoff:** explicit buttons open NDMA SACHET and IMD pages in an installed browser for current bulletins; those links require internet. No automatic alert claim, background polling, location collection, new permission, network request from the offline guide or automatic emergency call was added.
- **Source set:** MHA ERSS 112 information, NDMA SACHET alert portal and Do's & Don'ts, IMD weather/heat guidance. Source review date is 24 November 2026.
- **Tests:** focused JUnit coverage for all supported topics, emergency number and official references, hazard-specific safe reminders, unsupported topics and the static/offline versus live-alert disclaimer.
- **CI/build:** PASS — GitHub Actions run #1042 (`45364985871`) on exact code head `e643991636dc053fb4d8b87860f83c072a37d261`; backend syntax validation, Android unit tests (119 tests; all passed), Java compilation, debug APK build and artifact upload succeeded. The preceding intermediate run #1041 failed one disclaimer wording assertion; the wording was clarified and #1042 passed.
- **Artifact:** `jarvis-debug-apk`, ID `14905373972`; `https://github.com/sd2176845-glitch/jarvis-ai-assistant/actions/runs/45364985871/artifacts/14905373972`.
- **Poco M4 Pro 4G:** physical-device verification remains pending for offline display/scrolling, browser handoff to SACHET/IMD, unavailable-network handling and checking that no location permission/network polling was introduced.
- **Anti-duplication:** upgrades the existing Emergency Readiness Planner menu with a local static pack; no duplicate planner, alert monitor or forecast engine. JARVIS only; Even untouched.


## B31 — Vehicle Service Due-Date Tracker (2026-11-24)
- **Canonical owner:** M19 Vehicle Intelligence; extends the existing Local Vehicle Maintenance Log and `vehicle_service_log_v1` records. No second vehicle database or planner was added.
- **Record upgrade:** optional `next_service_due` date in strict real `YYYY-MM-DD` format. Existing service date, vehicle nickname, odometer and maintenance summary fields remain compatible; old records without the new field continue to load.
- **Overview:** read-only local report of due-date records, overdue, due today, next 30 days, later dates and malformed dates requiring correction. Vehicle nickname only is displayed; no licence plate, VIN, policy number or personal identity detail is requested.
- **Boundaries:** this is a family/guardian-managed administrative record, not a mechanical diagnosis, roadworthiness check or driving instruction. No automatic reminder was added in B31; reminder integration remains pending and must reuse the existing JARVIS ReminderStore if added later. No new permission, network request or external vehicle account integration.
- **Tests:** added validation tests for optional and malformed next-service dates, date-window count tests for overdue/today/next-30/later, invalid dates, empty data and mismatched inputs.
- **CI/build:** PASS — GitHub Actions run #1044 (`45365706990`) on exact code head `770e35592394a48905f4efee140e4c6ac5f0713a`; backend syntax validation, Android unit tests, Java compilation, debug APK build and artifact upload succeeded.
- **Artifact:** `jarvis-debug-apk`, ID `14906612856`; `https://github.com/sd2176845-glitch/jarvis-ai-assistant/actions/runs/45365706990/artifacts/14906612856`.
- **Poco M4 Pro 4G:** physical-device verification remains pending for legacy record compatibility, date entry, service due overview and local date boundaries.
- **Anti-duplication:** extends existing vehicle maintenance log and existing date-validation utilities; no duplicate inventory, reminder scheduler or vehicle data store. JARVIS only; Even untouched.


## B32 — Learning Notebook Progress & Revision Planning (2026-11-24)
- **Canonical owner:** Existing AI Learning Academy / `learning_notebook_v1`; expands the current Learning Notebook instead of creating another learning database.
- **Progress states:** each saved lesson can be marked Not started, In progress or Completed. A progress overview counts each state and flags unknown/legacy values for review.
- **Revision planning:** owner can set or clear an optional local `review_on` date using a date picker. The dashboard counts dates due, dates in the next 7 days and malformed values. Older lessons without the new field remain compatible.
- **Boundaries:** local notebook data only; status/revision changes are owner-invoked. Revision dates are planning notes and do not create notifications, a new scheduler, background activity, external calendar events, network calls or new permissions.
- **Tests:** focused JUnit coverage for progress status aggregation, unknown/empty statuses, revision date due/upcoming buckets, impossible dates and unreadable/mismatched data.
- **CI/build:** PASS — GitHub Actions run #1046 (`45366614260`) on exact code head `11dfe5709a80c0ad48ccd9918db6c010e2f322ef`; backend syntax validation, Android unit tests, Java compilation, debug APK build and artifact upload succeeded.
- **Artifact:** `jarvis-debug-apk`, ID `14906797649`; `https://github.com/sd2176845-glitch/jarvis-ai-assistant/actions/runs/45366614260/artifacts/14906797649`.
- **Poco M4 Pro 4G:** physical-device verification remains pending for existing notebook compatibility, saving/reloading progress status, setting/clearing revision dates and local date boundaries.
- **Anti-duplication:** upgrades `learning_notebook_v1` and existing AI Learning Academy menu; no duplicate learning store or scheduler. JARVIS only; Even untouched.


## B33 — Environmental Intelligence expansion (2026-11-24)
- **Progress:** 5/20 net-new capabilities code-present and CI-verified. The existing manual CPCB AQI observation log remains baseline and is not duplicated or counted as a new B33 increment.
- **Feature 1:** live modelled air-quality snapshot by owner-entered city, with selected geocoding match, U.S. AQI, European AQI, PM2.5, PM10, ozone, NO2, SO2, UV index, model time and timezone.
- **Source/interpretation:** Open-Meteo Air Quality API CAMS model data. Modelled regional grid estimates are not CPCB monitoring-station readings; U.S./European indices are not India's CPCB National AQI scale.
- **Privacy:** owner-invoked only; no GPS, location permission, background polling, automatic log writes or sharing. Existing weather city search is reused through a shared city-selection helper.
- **Verification:** run #1055 (`45381052956`) passed backend syntax, Android unit tests, debug APK build and artifact upload on code head `8d7a25e20429efe8aeff5b6c64b3e8a1d76561d5`. Poco M4 Pro 4G device test pending.


## B33 implementation status — Feature 2 (2026-11-24)


### B33 Feature 2 — 24-hour modelled air-quality forecast (2026-11-24)
- **Progress:** 2/20 net-new Environmental Intelligence capabilities are code-present and CI-verified.
- **Capability:** owner selects a city through the existing shared city picker and can view up to 24 hourly forecast points for U.S. AQI, European AQI, PM2.5 and PM10 from the Open-Meteo Air Quality API.
- **Interpretation:** each point is timestamped in the selected location's time zone. U.S./European indices are labelled separately and are not India's CPCB National AQI. Model-grid estimates are not CPCB station observations, official alerts or medical advice.
- **Privacy:** city-only, owner-invoked lookup with explicit disclosure; no GPS, location permission, home address, background polling, automatic log writes or sharing. Existing manual CPCB observation records remain unchanged.
- **Anti-duplication:** adds a forecast view to the existing Environmental Intelligence menu and reuses the existing city picker and network helper; no second dashboard, location service, observation store or scheduler.
- **Tests:** added unit coverage for missing/malformed values, sanitized place labels and the 24-entry output cap.
- **CI/build:** PASS — GitHub Actions run #1060 (`45384556838`) on exact feature-code head `6435cad8bd405dd6f5f884e2c0611aadce5464d7`; backend syntax checks, Android unit tests, debug APK build and artifact upload all succeeded.
- **APK artifact:** `jarvis-debug-apk`, ID `14914259944`, size 23,290,448 bytes, SHA-256 `6e8627614f079d08c8c62fd139c40648bdebc565abb417ce25b6acb5f3558595`.
- **Poco M4 Pro 4G:** physical-device verification remains pending.


## B33 implementation status — Feature 3 (2026-11-24)


### B33 Feature 3 — 24-hour modelled UV-index forecast (2026-11-24)
- **Progress:** 3/20 net-new Environmental Intelligence capabilities are code-present and CI-verified (run #1064 / `45386198143`); Poco M4 Pro 4G physical-device verification remains pending.
- **Capability:** owner chooses a city using the existing shared city picker and views up to 24 hourly UV-index estimates plus a separately labelled clear-sky reference from the Open-Meteo Air Quality API.
- **Interpretation:** uses WHO Global Solar UV Index exposure bands (Low, Moderate, High, Very high, Extreme). Protection guidance begins at UVI 3; very high/extreme levels are highlighted. This is general public-safety information, not a personal exposure calculation or medical advice.
- **Data limits:** CAMS model-grid estimates are not direct ground measurements. Clear-sky values assume cloud-free conditions and are not presented as expected actual exposure.
- **Privacy:** owner-invoked city-only lookup; reuses existing geocoder/network helper; no GPS, location permission, home address, background polling, saved location or automatic history.
- **Anti-duplication:** a distinct UV outlook, not another air-quality dashboard or AQI forecast; no new data store, location system, scheduler or permissions.
- **Tests:** added JUnit coverage for WHO UV band boundaries, missing/malformed values, sanitized labels and the 24-entry output cap.
- **Poco M4 Pro 4G:** physical-device verification remains pending.


### B33 Feature 4 — 24-hour modelled weather watch outlook (2026-11-24)
- Owner-invoked 24-hour city forecast for thunderstorm/hail, heavy precipitation, wind, apparent-temperature and visibility planning indicators.
- Reuses the existing Environmental Intelligence menu, city picker and network helper; no duplicate weather dashboard, location service, GPS permission, saved location, background monitoring or automatic alerts.
- Thresholds are planning heuristics, not official IMD warnings. GitHub Actions run #1070 (`45392316586`) passed backend syntax, Android unit tests, debug APK build and artifact upload; Poco physical-device verification pending.

### Priority order for B33 Features 5–20
1. Official NDMA SACHET disaster-alert reader with source attribution, timestamp handling, CAP detail parsing and the required ETag/304 caching.
2. High-impact environmental preparedness based on authoritative observations and warnings, including flood, heat and pollution context.
3. Forecast interpretation and historical context.
4. Lower-impact reference and convenience features.
Reliability and source authority are mandatory. Modelled data and heuristics must never be presented as official government alerts.

### B33 Feature 5 — Official NDMA SACHET CAP disaster-alert reader (2026-11-24)
- Adds an owner-invoked read-only reader for All-India SACHET RSS entries and their linked CAP alert records, with optional local state/district filtering based on CAP affected-area descriptions.
- Displays sender, sent/effective/expiry times, status, event, urgency, severity, certainty, headline, description, official instruction and affected area when supplied by the source.
- Accepts CAP links only from the exact HTTPS SACHET host and endpoint path. Stores CAP XML and ETag in the existing private preferences store; subsequent requests use `If-None-Match` and reuse the cached XML on 304, following the NDMA integration guide.
- No GPS, location permission, background polling, push alerts, automatic emergency action, external sharing or location history. The existing offline emergency-preparedness pack remains unchanged.
- GitHub Actions run #1079 (`45393701084`) passed backend syntax, Android unit tests, debug APK build and artifact upload on exact head `ed3a0e4286b76b017d6e48b4876b238d6f8a2d72`. Artifact `14919538306`; Poco M4 Pro 4G device verification remains pending.

## B26 implementation status — 50-feature Batch 1 (in progress)

- **Feature 1/50 — Warranty Reminder Integration (M07/M19):** the existing Smart Asset Inventory item-details view now offers an owner-invoked reminder action when a valid warranty is active.
- The action reuses the existing M07 reminder scheduler and notification receiver, including quiet-hours and snooze behavior; it does not create a second scheduler, database or notification service.
- Owner selects a local date/time; the picker cannot go beyond the warranty expiry date, and the selected time must be future-dated. The default suggestion is one day before expiry at 9 AM when possible.
- Reminder text is derived from the owner-entered item nickname and normalized through the existing reminder title rules. No new permission, external sharing, background inventory scan or cloud upload.
- **Status:** CI PASS on commit `788f6be832051bf9d979a6799709e4804e5018a6` (unit tests, debug APK build and artifact upload); Poco M4 Pro 4G device verification pending. This entry does not imply the other 49 batch features are implemented.

## B26 implementation status — Feature 2/50

- **Owner Reminder Manager:** the existing Tasks & reminders hub now includes a Manage reminders action. It lists valid upcoming reminders created inside JARVIS in scheduled-time order.
- Selecting a reminder shows its title and local scheduled date/time. Cancellation requires an explicit confirmation and cancels only the matching JARVIS alarm, notification and stored record.
- The view does not read other apps' notifications and does not mutate malformed/expired records while listing. It reuses the existing ReminderStore and adds no permissions or duplicate scheduler.
- **Status:** final-head CI PASS on commit `01a49811edd243f15110bdaa798c96a2d8174f3d`, workflow run #918; Android unit tests, debug APK build and artifact upload passed. Poco M4 Pro 4G device verification pending.

## B26 implementation status — Feature 3/50

- **Owner Reminder Rescheduling:** the existing reminder details view now offers an owner-invoked Reschedule action.
- The new date/time must be in the future. The same reminder ID and existing local ReminderStore/AlarmManager are reused; if scheduling fails, JARVIS attempts to restore the previous time and alarm.
- No duplicate scheduler or storage, no new permission, no access to other apps' notifications and no cloud upload.
- **Status:** code added on the B26 branch; final-head CI and Poco M4 Pro 4G physical-device verification pending.


## B26 implementation status — Feature 4/50

- **Duplicate Reminder Protection:** creation and rescheduling now check for an existing owner-created reminder with the same normalized title and exact trigger time.
- The lookup reuses the existing local ReminderStore, is read-only, and excludes the current reminder ID during rescheduling.
- Added pure-Java tests for duplicate matching and allowed non-duplicates.
- No new permission, duplicate scheduler, external notification access, cloud upload or background collection.
- **Status:** code added on B26 branch; final-head CI and Poco M4 M4 Pro 4G physical-device verification pending.

## B26 implementation status — Feature 5/50

- **Reminder Search & Filtering:** upgraded the existing Manage reminders screen with local, live title filtering across upcoming JARVIS-created reminders.
- Matching is case-insensitive, whitespace-normalized and read-only. The filter does not modify reminders or change scheduling; selecting a result reuses existing details, reschedule and cancellation actions.
- Added pure-Java tests for partial matches, case-insensitivity, whitespace normalization, empty search and non-matches.
- No duplicate reminder hub/store/scheduler, new permission, other-app notification access, cloud upload or background collection.
- **Status:** code added on B26 branch; final-head CI and Poco M4 Pro 4G physical-device verification pending.
