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

### M01 — Owner Identity, Access & Privacy
Includes owner sign-in/profile/session, Google/phone OTP setup, sign-out and expiry handling, guest mode, privacy lock, owner-only settings, permission explanations, consent, review/export/delete controls, data minimization and secure handling of credentials. Reuses Android permissions and one shared identity/access layer; no duplicate PIN/authentication systems.

### M02 — Core Voice, Conversation & Human Intelligence
Includes speech recognition, TTS, wake phrase, listening/thinking/speaking/offline/standby states, stop/pause controls, follow-up commands, Hindi/Hinglish/English and tested language support, natural conversation, context and intent understanding, assistant modes, personality, cautious emotion/tone interpretation, empathy, social-context reasoning and user-selected-media body-language interpretation. Basic voice and conversation behavior is part of this advanced engine, not a separate competing assistant.

### M03 — Knowledge, Search, Research & Learning
Includes browser-search foundations, source-aware in-app search, public API lookups, news/sports, translation, current-source research, citations/freshness, knowledge vault integration, scheduled research, scientific discovery, reality checks, learning tips, AI Learning Academy, Personal Education & Skill Academy, global knowledge and multilingual research. Each provider is a connector under one research/data layer; browser shortcuts must not be described as native live integrations.

### M04 — Universal App Intelligence & Knowledge Brain
Includes installed-app catalogue where Android permits, app identification, public app documentation/research, feature/settings explanations, version/update research, source freshness and capability boundaries. App knowledge never grants access to private app data.

### M05 — Universal Application & Phone Control
Includes app launching, Android settings handoffs, supported UI actions, accessibility-based controls only when explicitly enabled, context-aware interface handling, supported cross-app workflows, device-health basics, Bluetooth/headphone controls, screen-time overview and approved smart-device handoffs. Unknown controls must stop and ask rather than guess.

### M06 — Communication Intelligence & Unified Communication Hub
Includes contact resolution, call requests, spoken confirmation, SMS/message drafts, WhatsApp assistance, email/messaging/calendar connectors, live translation/captions where supported, recipient disambiguation, call/SMS event announcements and communication history. Draft first; require fresh explicit approval before calling, sending, sharing or changing external records.

### M07 — Personal Secretary, Productivity & Life Management
Includes local notes, structured notes, to-dos, preferences, goals, reminders, calendar handoff/connected schedule, daily briefing, multi-step personal planning, Life Management Intelligence, Personal Productivity Studio, asset/inventory tracking, vehicle records and maintenance reminders, journey preparation and personal finance organization using manually entered data. One shared task/calendar/reminder store; do not create duplicate schedulers.

### M08 — Workflow Automation & Orchestration
Includes command-chain planning, workflow builder, manual/time/event triggers, supported cross-app runners, conditions, approval gates, state/recovery, bounded retries, safe stop, history/audit, templates, dry-run/test mode and resource-aware scheduling. Sensitive actions always require fresh approval.

### M09 — Personal Memory, Knowledge Vault & Timeline
Includes recent conversation context, owner-approved long-term preferences, structured knowledge, provenance, knowledge graph, Digital Time Machine, Personal Knowledge Vault, Personal Experience Timeline, idea evolution and Digital Legacy Archive. Every saved item must be reviewable, correctable, exportable and deletable; avoid a second memory store in another module.

### M10 — Vision, OCR, Spatial & Media Intelligence
Includes user-selected image/document OCR, camera sessions initiated visibly by the owner, selected video/audio analysis, scene/object understanding, spatial intelligence, image understanding and media workflows. Show exactly what will be processed; no silent camera/gallery/microphone monitoring.

### M11 — Creative Media & Creator Studio
Includes image/video/presentation creation, creative project organization, media editing workflows and the Creative Media Studio. Reuse the media/vision layer where appropriate; respect ownership, licensing, copyright and provider terms.

### M12 — AI Development, Software & Data Intelligence Studio
Includes coding help, project explanations, developer workshop, app/website/software design and build workflows, project-aware code assistance, test plans, architecture, data analysis, visualization and reporting. This is the home for Software Development Studio and Data Intelligence Center. Generated code and analysis are proposals until reviewed and tested; process only owner-authorized data.

### M13 — Security Intelligence & Secure Evidence Vault
Includes permission/build review, privacy controls, read-only security checks, owner-approved security-event interpretation, secure evidence/document storage, encryption in transit/at rest, least privilege, strong authentication, dual approval for sensitive exports, integrity hashes, chain-of-custody/access logs, encrypted backups, retention/deletion, breach response and controlled disclosure. This is not a stealth-surveillance, interception or unauthorized-access system.

### M14 — Diagnostics, Reliability & Controlled Recovery
Includes self-diagnostics, problem reports, sanitized logs, health checks, troubleshooting, repair handoff, safe allowlisted reversible maintenance, checkpoints, rollback, test gates, recovery journal and failure escalation. Keep diagnostics read-only by default. No arbitrary commands, silent source edits, APK replacement, permission weakening or deployment.

### M15 — Adaptive UI, Command Center & Accessibility
Includes JARVIS dashboard/orb, red/black theme reconciliation, status feedback, command center, creator/project status, adaptive design proposals, preview/approval, versioning/rollback, gestures, widgets, dashboard profiles, visual defect checks, dynamic information display, accessibility options and universal accessibility mode. Preserve essential controls and readable contrast.

### M16 — Connected Ecosystem, Smart Environment & Collaboration
Includes compatible smart-home devices, authorized Bluetooth accessories, device discovery/pairing, device status, owner-approved collaboration spaces, role-based sharing, cross-device sync, plugin/extension registry, local device communication and authorized connected systems. Pairing, permissions, revocation and audit are mandatory; being on the same network grants no access.

### M17 — Platform, Resource & Connectivity Intelligence
Includes battery/network/storage/resource manager, hardware compatibility, network health for authorized connections, local/global/hybrid intelligence routing, offline capability, private local wireless network design, authorized satellite-internet research and educational satellite/CubeSat roadmap. A local network is not internet access; no unauthorized network or satellite access.

### M18 — Simulation, Robotics & Engineering Intelligence
Includes Digital World Simulator, Digital Twin, Virtual Experiment Laboratory, 3D Invention Studio, component library, robotics architecture/interfaces, simulated robot coordination, distributed computing with explicit enrollment and workload limits, and engineering compatibility research. Begin with virtual models/read-only telemetry; physical movement or actuation requires hardware-specific safety review, manual override, emergency stop and explicit authorization.

### M19 — Personal & Practical Intelligence Suite
Includes disaster preparedness using official sources, environmental information with timestamp/source, legal-document explanation with source passages and professional-review boundaries, intelligent journey planning, smart asset inventory, vehicle intelligence, collaborative project intelligence and owner-curated milestones. Reuse M07/M09/M03 rather than creating duplicate schedulers, memories or research engines.

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
