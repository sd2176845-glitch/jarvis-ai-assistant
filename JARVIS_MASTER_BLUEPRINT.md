# JARVIS Master Blueprint

This document is the single source of truth for the JARVIS Android assistant project.
Target device: Poco M4 Pro 4G. Development is browser/Chrome-first.
JARVIS is separate from the Even project.

## Product identity
- Name: JARVIS
- Wake phrase: "Hey JARVIS"
- Online AI backend: Render service calling Groq; never put the Groq API key in the APK.
- Personality inspiration: the considerate, composed, dependable and witty qualities associated with Edwin Jarvis.
- Important honesty rule: JARVIS may express empathy and adapt its tone, but must not claim to literally feel human emotions.
- Languages: Hindi/Hinglish and English; Bengali support is a planned capability, not yet verified.

## Status meanings
- **Code present — device test needed:** implementation exists in the repository, but is not claimed to work on every device.
- **Planned:** needs implementation or integration.
- **Platform-dependent:** requires permissions, supported Android APIs, other apps/devices, or battery-policy configuration.
- **Verified:** tested successfully on the target Poco phone or against the live service. Do not use this label without an actual test.

## Existing code / test-needed inventory
- [x] Android app shell and futuristic JARVIS orb dashboard (code present — device test needed).
- [x] Online AI chat client calling the Render endpoint (code present — verify on phone).
- [x] Basic speech recognition and Text-to-Speech (code present — verify available language/services).
- [x] Recent conversation context stored locally, with a clear-memory control (code present).
- [x] Initial background wake-word service (code present — reliability and screen-off operation need device testing).
- [x] Initial app-launch commands for common apps (code present — test each command).
- [x] Initial dialer/contact/alarm and emergency-contact/SOS options (code present — permissions and behaviour need testing).
- [x] Initial call/SMS event and accessibility integration (code present — user setup and platform restrictions apply).
- [x] Basic safety/permission checks (code present — review and test).
- [x] Backend caring/personality system prompt (initial version; full mood inference remains limited).
- [x] Explicit assistant-mode selector: Normal, Caring, Funny, Serious, Study, Gaming, Travel and Night (code present — build/device verification pending).
- [x] Mode-aware AI prompt guidance and mode-specific orb pulse speed (code present — visual/voice behavior needs device testing).
- [x] Command Center menu linking notes, alarm/reminder handoff, web search, weather search, daily briefing and privacy settings (code present — build/device verification pending).
- [x] Local notes: save, view, and clear notes in app-private SharedPreferences (code present — device test needed).
- [x] Daily briefing: local date/time, battery percentage when available, assistant mode and saved-note count (code present — device test needed).
- [x] Orb scale animation reacts to speech recognition states (code present — visual behavior needs device testing).
- [x] Live web/weather shortcuts open browser search pages; these are not native live-data API integrations or source-grounded AI search.
- [x] Privacy panel reports microphone/contacts permission state, opens Android app settings, and offers local note clearing.
- [x] Initial Cybersecurity Command Center: read-only local permission/build review with shortcuts to Android security and JARVIS app-permission settings (code present — device test needed; not a malware/network scanner).

## Reference-video review (2026-11-02)
Two user-supplied references were reviewed: an Instagram Reel showing a JARVIS-style voice assistant and connected-app concepts, and a 38-second open-source JARVIS setup/demo showing repository structure, setup steps, a red HUD/orb interface and AI-provider key configuration.

Adopt these ideas selectively; do not copy an unknown project's code blindly:
- [~] Visual refresh: red/black Stark-style colors have been started in the Android UI, but the theme is only partially applied. Finish remaining screens/components, check readable contrast, and device-test; do not recreate the existing color change.
- [ ] Clear voice-session feedback: show listening, thinking, speaking, offline and standby states consistently; keep a visible stop control.
- [ ] Connected-app actions: prioritize useful Calendar and messaging workflows from the first reference, but require explicit account connection and permission, show what will happen, and confirm before sending messages or changing events.
- [ ] Provider setup discipline: document setup/configuration steps, keep provider secrets on the backend, never put API keys in the APK, screenshots, or Git history.
- [ ] Modular feature organization: keep integrations separated behind small service interfaces so one provider failure does not break voice commands or local notes.
- [ ] Do not reproduce desktop-only controls or claim integrations based solely on a visual demo; adapt only what Android 13+ and the target device can support.

## Remaining feature backlog

### Priority 0 — Owner authentication and account setup
- [ ] Implement secure owner sign-in with Firebase Authentication, supporting Google sign-in and phone-number OTP after Firebase project configuration; neither sign-in method is implemented yet.
- [ ] Add an owner profile/session state, sign-out, and safe handling of expired sessions; do not store passwords or OTPs locally.
- [ ] Restrict owner-only settings and sensitive assistant actions to an authenticated owner; keep emergency access and permission behavior explicit.
- [ ] Document Firebase Console setup, Android SHA fingerprints, authorized domains, and required configuration without committing service-account files, API secrets, or private credentials.
- [ ] Add a privacy lock for sensitive screens/actions, with owner re-authentication where appropriate; do not rely on a cosmetic PIN or lock screen as real authentication.
- [ ] Add a guest mode that hides owner-specific notes, memory, account details and private briefing data; make the transition into and out of guest mode obvious and test that private data cannot leak through voice responses or notifications.

### Priority 1 — Reliability and core voice control
- [ ] Verify background listening and screen-off operation on the Poco device.
- [ ] Fix and device-test wake-word follow-up command execution while JARVIS is outside the foreground app.
- [ ] Fix and test spoken yes/no confirmation flow for calls; never place a call without clear confirmation.
- [ ] Test MIUI battery restrictions and document the exact settings needed.
- [ ] Show microphone/listening state clearly and provide a reliable stop control.
- [ ] Verify install, launch, voice, AI response, app commands, memory clear, and service stop on the actual phone.
- [ ] Add user-configurable wake-word/listening preferences only where the selected recognition engine supports them; explain battery and privacy trade-offs and retain a clear manual stop/pause control.
- [ ] Add a device-health command for battery, storage and network connectivity using Android-supported data; avoid claiming access to hidden diagnostics or private app data.

### Priority 2 — Online intelligence and API integrations
- [ ] Native weather integration using Open-Meteo; show location, units, timestamp and API errors clearly.
- [ ] Currency conversion using Frankfurter; show base currency, target currency, rate date and amount.
- [ ] Country information using REST Countries.
- [ ] Dictionary lookup using Free Dictionary API.
- [ ] Indian PIN-code lookup using SearchPinCode; validate input and show district/state/post office results when available.
- [ ] India public-holiday lookup using a maintained India holiday data source; display year and source.
- [ ] Sunrise/sunset lookup using Sunrise-Sunset API; request location only when needed and explain coordinate requirements.
- [ ] Math/symbolic calculation helper using Newton API only for supported operations; keep ordinary calculations local where practical.
- [ ] Actual in-app web search with source-aware summaries, publication dates where available, and clickable source links. A browser search shortcut is not this feature.
- [ ] Native news and sports data integrations with source and freshness labels.
- [ ] Hindi, Bengali and English translation workflow with language selection and speech output.
- [ ] Clear offline behavior: basic local commands and notes remain available; JARVIS must say when online-only features cannot connect.

### Priority 3 — Anime, space and entertainment features
- [ ] Anime discovery and detail lookup using Jikan (unofficial MyAnimeList API); show title, synopsis, genres, score and source link when available.
- [ ] Anime recommendations based on user-selected genres/preferences; do not imply Jikan recommendations are personalized unless implemented.
- [ ] Anime scene identification using Trace Moe; requires an explicit user-selected screenshot/image upload, upload-size handling, and clear privacy notice. Do not silently access the gallery.
- [ ] ISRO information lookup using the community-hosted ISRO API; label it as an unofficial data source and handle downtime.
- [ ] Joke retrieval using JokeAPI with safe-content filtering and language-aware presentation.
- [ ] Fun-fact retrieval using a suitable public endpoint; validate availability and source quality before release.
- [ ] Meme discovery using Imgflip only after reviewing its API terms, attribution rules and rate limits; do not assume unrestricted image reuse.

### Priority 4 — Memory and personal secretary
- [ ] User-approved long-term preferences with review/edit/delete controls.
- [ ] Structured notes and to-do lists with editing and deletion per item (current implementation stores and clears a simple local notes text list).
- [ ] True scheduled reminders with JARVIS notification delivery (current reminder control only opens Android's alarm UI; it does not schedule JARVIS reminders).
- [ ] Calendar-connected morning briefing and daily schedule.
- [ ] Multi-step commands using supported actions, with confirmation for sensitive steps.
- [ ] Expand multi-step command handling into a safe command-chain planner: parse the requested steps, show the proposed actions, execute reversible low-risk steps, and ask separately before calls, messages, purchases, sharing data or changing calendar events.
- [ ] Add a user-controlled smart-memory manager that lets the owner inspect, correct, export and delete saved preferences; memory must be opt-in and must not silently retain sensitive information.

### Priority 5 — Vision and advanced integrations
- [ ] Camera vision / OCR for user-selected images and text.
- [ ] Notification reader with explicit user-granted access and privacy controls.
- [ ] Smart-home integration for compatible devices.
- [ ] Digital wellbeing / screen-time overview where Android provides the data.
- [ ] Coding helper and daily learning tips.
- [ ] Headphone/Bluetooth interaction and selected context-aware events.
- [ ] Add an optional screen-context explainer using Android's supported screen-capture/accessibility APIs only after explicit user action; show a clear capture indicator, process only the selected content, and never silently monitor other apps.
- [ ] Add WhatsApp/message assistance as a permission- and platform-compliant workflow: draft text first, let the owner review it, and require explicit confirmation before any send action. Do not promise silent/background sending or bypass app protections.

### Priority 6 — Personality, UI and quality
- [ ] Full emotion engine: happy and thinking states, plus cautious mood-aware adaptation (manual Caring/Funny/Serious modes have initial code).
- [ ] Consistent butler-inspired personality: composed, discreet, respectful, helpful and occasionally witty.
- [ ] Fine-grained voice delivery and orb animation for all supported states.
- [ ] Build and regression-test after each feature batch; keep the app installable.
- [ ] Complete the red/black theme consistently across dashboard, dialogs, settings and status states while preserving accessibility contrast.
- [ ] Add a modular integration registry so optional features can be enabled/disabled independently and failures do not break core voice, notes or offline commands.

## Integration rules for public APIs
1. The API catalog is a directory, not a guarantee that every listed service is reliable, free forever, or suitable for production.
2. Before integrating each API, verify its current documentation, authentication requirements, usage limits, terms, attribution rules and response format.
3. Keep private API credentials on the Render backend in environment variables. Never commit keys or embed them in the Android APK.
4. Prefer Android-local requests only for genuinely public, low-risk endpoints; route key-protected services through the backend.
5. Add request timeouts, bounded retries, response validation, user-friendly error messages and sensible caching.
6. Do not send contacts, messages, precise location, gallery images or other personal data to an external API without clear user action and consent.
7. Ask before sending messages, sharing location, placing a call, or performing other sensitive actions.
8. Label unofficial/community-hosted sources, including Jikan and the ISRO endpoint, accurately. Handle service downtime gracefully.
9. Never mark a feature complete just because a button, API URL or checklist entry exists. Mark it code-present only after implementation, and verified only after a successful relevant test.
10. Keep JARVIS separate from the Even project; do not modify Even as part of JARVIS work.

## Implementation sequence
1. Inspect current Android command routing and backend request/response handling before editing.
2. Implement one small, coherent feature batch at a time, starting with core reliability.
3. Add API integrations behind a reusable, validated data-service layer; avoid duplicating HTTP logic in every screen or command.
4. Connect voice intents to those services and present concise spoken answers plus readable details.
5. Build the APK and run available automated checks after each feature batch.
6. Device-test permissions, network failures, voice commands, background behavior and sensitive-action confirmations before calling the batch complete.

## Security and privacy
- Do not store secrets (Groq/API keys, passwords, tokens) in Android code or the repository.
- Use HTTPS for backend and API calls.
- Request permissions only when a feature needs them.
- Keep the user in control of saved memory, microphone use, notification access and image uploads.
- Do not claim an action, live lookup, reminder or setting change succeeded unless a connected service or device confirms it.


### Priority 7 — Owner feedback, diagnostics and repair handoff
- [ ] Add an owner-invoked "Report a problem" / "Self-Diagnostics" conversation flow. JARVIS asks what is not working (for example voice, wake word, memory, AI replies, app commands, reminders or integrations) and lets the owner describe the issue in their own words.
- [ ] Run safe, read-only checks for relevant app version, permission state, network reachability, backend health, API response status, speech/TTS availability and recent non-sensitive error codes; do not claim a root cause without evidence.
- [ ] Produce a shareable, structured diagnostic report with issue description, expected vs actual behavior, reproducible steps, timestamps, app/build version, device/Android details, relevant sanitized logs, checks performed and likely next steps.
- [ ] Provide a preview before sharing. Redact API keys, auth tokens, phone numbers, contact names, message contents, precise location, personal notes and other private data by default. Include only data the owner explicitly approves.
- [ ] Give the report a unique reference ID and export it as text/JSON that the owner can copy or share with the developer/ChatGPT; do not silently upload it.
- [ ] Support a repair handoff: owner shares the report with the developer, who reviews the evidence, proposes a code fix, updates the JARVIS feature branch, runs available builds/tests and returns a testable APK or patch.
- [ ] After a fix, provide a short verification checklist and let the owner report whether the original problem is resolved; keep unresolved issues open and attach new evidence rather than marking them fixed automatically.
- [ ] Never let JARVIS rewrite its own installed APK, change source code, deploy backend changes or execute arbitrary repair commands autonomously. Code repairs require developer review, explicit authorization, build/test and owner installation.
- [ ] Keep diagnostics opt-in, local-first and user-controlled; allow report preview, cancellation, deletion and selective sharing.

## Decision additions — 2026-11-19
The following modules are approved product decisions and are **planned**, not implemented or verified merely by being listed here. Build them incrementally using the implementation sequence, security rules and platform limitations above.

### Priority 8 — Human Intelligence 1.0
- [ ] Emotion-signal interpretation: cautiously infer possible happiness, sadness, anger or stress from the owner's words and conversational context; communicate uncertainty and never present an inference as fact or diagnosis.
- [ ] Behaviour-pattern analysis: identify patterns in observable conversation and interaction context without claiming to know a person's hidden thoughts or motives.
- [ ] Communication intelligence: understand tone, wording, conversational context and the owner's preferred Hindi/Hinglish/English communication style.
- [ ] Body-language understanding: interpret visible expressions and gestures only in a user-selected photo/video or explicitly initiated camera session; request permission and explain processing before use.
- [ ] Empathy system: adapt responses to the situation with respectful, supportive language without pretending to literally feel human emotions.
- [ ] Social intelligence: reason cautiously about observable signals related to friendship, trust, ego, jealousy and selfish behaviour; present possibilities, not certainty or labels.
- [ ] Context memory: use relevant prior conversation context only with owner-controlled, reviewable memory and privacy controls.

### Priority 9 — Universal App Intelligence Engine 1.0
- [ ] Maintain a catalogue of installed apps using Android-supported package visibility; identify an app when installed and permitted by Android.
- [ ] Research public information about existing and newly released apps, including their purpose, documented features, settings and updates; label information with source and freshness where available.
- [ ] Explain where supported features and settings are located and how they work, using current app/version context where available.
- [ ] Refresh app knowledge through online research rather than claiming automatic or guaranteed awareness of every new app or update.
- [ ] Keep app knowledge separate from app-control permissions: knowing an app's features does not grant access to its private data or controls.
### Priority 10 — Universal Application Control Engine
- [ ] Open installed apps through supported Android intents and app launch APIs.
- [ ] Operate supported app features and UI controls only through documented integrations, Android APIs or user-enabled Accessibility Service actions that comply with platform and app rules.
- [ ] Understand changed app interfaces cautiously; detect when a screen or control is unknown and ask the owner rather than guessing or tapping unpredictably.
- [ ] Support cross-app workflows where each step is supported, visible and authorized.
- [ ] Provide an explicit permission and capability boundary for every integration. No promise of unrestricted access: banking, passwords, private data, protected screens and app-enforced restrictions remain protected.
- [ ] Require owner confirmation for sensitive or consequential actions, including sending messages, placing calls, purchases, sharing data and changing important settings.

### Priority 11 — Communication Intelligence Engine 1.0
- [ ] Voice-to-message: convert the owner's spoken request into an editable message draft.
- [ ] Message sending: prepare messages for supported channels such as WhatsApp or SMS using permitted APIs or user-visible app workflows; show recipient and exact message and obtain explicit confirmation before sending.
- [ ] Voice calling: identify the requested contact and place calls only after clear confirmation.
- [ ] Message reading: read messages only through explicitly granted, platform-supported notification or app integrations, with privacy controls and appropriate user context.
- [ ] Smart reply: draft context-aware replies for owner review; do not silently send.
- [ ] Language support: understand and generate Hindi, English and Hinglish; Bengali support remains subject to implementation and testing.
- [ ] Contact recognition: resolve names against permitted contacts, disambiguate duplicates and confirm the intended recipient before communication.

### Priority 12 — Autonomous Intelligence & Personality Engine
- [ ] Independent analysis: assess the current request and available, authorized information before suggesting a useful next step.
- [ ] Decision intelligence: compare options and explain recommendations, assumptions, risks and uncertainty; leave consequential decisions with the owner.
- [ ] Learning ability: update app/API knowledge and owner-approved preferences through controlled, reviewable processes.
- [ ] Self-improvement: identify errors from evidence, explain proposed improvements and request developer/owner approval for code or configuration changes.
- [ ] Human understanding: use observable language and context to adapt responses while acknowledging uncertainty.
- [ ] Personal identity: maintain a consistent, considerate JARVIS personality, voice and communication style without claiming human consciousness or feelings.
- [ ] Initiative: offer timely, relevant suggestions within owner-configured permissions; no hidden monitoring or unsolicited sensitive actions.
- [ ] Long-term memory: support opt-in, reviewable, editable and deletable owner-approved preferences and context; do not silently retain sensitive information.

### Shared acceptance and safety requirements for these modules
- [ ] Implement each capability as a small modular feature with explicit dependencies, permissions, failure handling and a user-visible status.
- [ ] Distinguish **planned**, **code present — device test needed**, **platform-dependent** and **verified** using the status meanings above.
- [ ] Do not claim mind-reading, certainty about emotions or motives, unrestricted access to other apps, or autonomous control beyond supported and authorized actions.
- [ ] Use selected media and screen context only after clear owner action; never silently capture camera, screen, notifications or other app content.
- [ ] Test each module on the target Poco M4 Pro 4G and run regression builds before marking any capability verified.


## Decision additions — 2026-11-19 (JARVIS development and creator architecture)

These decisions extend the existing Universal App Intelligence, Application Control, Communication, Autonomous Intelligence and security sections above. They are **approved blueprint requirements only**: adding them here does not mean the features have been implemented, built or verified. Keep JARVIS completely separate from Even; JARVIS may assist with Even development only through an explicitly authorized development workflow. Do not modify Even as part of JARVIS implementation.

### Priority 13 — JARVIS AI Development Engine

- [ ] Create a developer-assistance engine through which the owner can ask JARVIS to help plan, inspect, explain, update and test software projects, including the separate Even assistant and other AI assistants.
- [ ] Support project-aware workflows: identify the selected repository/project, read its documented architecture and build instructions, inspect relevant source files, and explain the proposed change before editing.
- [ ] Provide feature planning, technical design, code drafting, dependency/documentation research, build orchestration and test-result summaries.
- [ ] Keep project boundaries explicit. Never mix JARVIS and Even source trees, credentials, build outputs or configuration. Work only in the project/repository explicitly selected by the owner.
- [ ] Require owner authorization before writing files, committing changes, changing configuration, deploying services or accessing a connected repository. Preserve a recoverable copy or version-control checkpoint before edits.
- [ ] Never claim a build, test, deployment or feature succeeded unless the corresponding tool/device reports success. Record unsupported tests and unresolved issues honestly.

### Priority 14 — Universal App Intelligence and Knowledge Brain

- [ ] Extend the existing Universal App Intelligence Engine with a structured JARVIS Knowledge Brain: organized, searchable records for app categories, individual apps, documented capabilities, settings, permissions, supported workflows, versions, update notes and source links.
- [ ] Discover relevant Play Store apps and public app documentation by category and user request; identify new apps only when reliable information is available.
- [ ] Maintain app-by-app feature knowledge and explain an app's documented operation and capability limits. Do not infer private or undocumented functionality.
- [ ] Track app updates and refresh affected knowledge when new release notes or documentation are available; show source and last-checked date and flag stale or uncertain information.
- [ ] Keep research results distinct from verified on-device capabilities. A documented feature is not proof that it works on the owner's installed version or device.
- [ ] Use Android-supported installed-app/package visibility for local inventory, respecting package-visibility restrictions and permissions.

### Priority 15 — Universal Phone Control and Communication Workflows

- [ ] Expand the existing Universal Application Control Engine to cover supported app opening and operation, Android settings handoffs, music-player controls, notifications and cross-app workflows.
- [ ] Music controls should use supported media-session, notification or app integration APIs where available; identify the active player when Android exposes it and handle unsupported players gracefully.
- [ ] Settings controls should open the relevant Android settings page or use a documented permitted API. Do not promise unrestricted control of protected system settings.
- [ ] Notification access must be opt-in, visibly explained and revocable; read only the notifications the owner has authorized.
- [ ] Extend communication workflows for WhatsApp, phone calls, SMS, voice-to-text, smart-reply drafts and contact recognition, reusing Priority 11 rules: show recipient/content and obtain explicit confirmation before sending or placing a call.
- [ ] Cross-app workflows must expose the planned steps, stop safely when a screen or action is unknown, and never bypass app security or Android permission boundaries.

### Priority 16 — Security Intelligence

- [ ] Consolidate privacy protection, permission management, suspicious-activity signals, secure memory and approval gates into a Security Intelligence module.
- [ ] Explain which permissions each feature needs, why they are needed, what data may be accessed and how the owner can revoke access.
- [ ] Detect and report observable security-relevant events, such as unexpected permission changes, backend authentication failures or suspicious app/network behavior when reliable signals are available. Clearly distinguish evidence from a possible warning.
- [ ] Protect stored owner-approved memory and project credentials using platform-appropriate secure storage; keep API secrets on the backend and out of APKs, logs and Git history.
- [ ] Require explicit owner approval for sensitive actions and any operation that shares private data, changes important settings, sends communications or modifies project files.
- [ ] Provide a visible pause/stop control for listening, notification access and background activity. Do not silently monitor the device.

### Priority 17 — Autonomous Learning and Scheduled Research

- [ ] Support owner-configured background learning and scheduled research for app documentation, technology updates, API changes and knowledge refreshes.
- [ ] Use battery-conscious scheduling, Android background-work constraints, network availability and configurable charging/network preferences. Do not promise uninterrupted 24-hour processing.
- [ ] Summarize what was researched, when it was checked, which sources were used, what changed and what still needs owner review.
- [ ] Evaluate JARVIS performance using explicit test results, error trends and owner feedback; propose improvements rather than silently changing its own code or permissions.
- [ ] Keep research bounded and cancellable, respect service terms/rate limits, and avoid collecting unrelated personal data.

### Priority 18 — JARVIS Command Center and Creator Mode

- [ ] Build a central JARVIS Command Center dashboard that brings together module status, available phone workflows, recent owner-approved activity, diagnostics, scheduled research and project-development tasks.
- [ ] Clearly distinguish available, permission-required, planned, failed and verified capabilities. A dashboard tile must not imply that a feature is implemented.
- [ ] Add JARVIS Creator Mode for helping the owner design and build new software and AI assistants through project setup, architecture, code assistance, dependency research, build/test workflows and documentation.
- [ ] Creator Mode must use the selected project workspace and explicit owner authorization; it must not silently alter JARVIS, Even or any other project.
- [ ] Keep sensitive information out of dashboard previews and diagnostic summaries by default; provide owner-controlled review and sharing.

### Priority 19 — JARVIS Auto Debug & Self-Repair Engine 1.0

- [ ] Add an owner-invoked diagnostic and repair workflow that can cover the Android application, calling, messaging, internet connectivity, AI backend, Even development workspace when explicitly selected, APK builds and GitHub Actions.
- [ ] Detect reported or test-observed failures, collect relevant error logs and build/test output, and identify the likely cause only when supported by evidence.
- [ ] Locate the affected code/configuration area and prepare a proposed fix with an explanation, affected-file list and risk assessment.
- [ ] Preserve the last known working state with a version-control checkpoint or verified backup before applying an authorized change.
- [ ] After an authorized fix, rerun the relevant build, feature tests and regression tests; compare results with the original failure and report any remaining issue.
- [ ] If a test still fails, continue diagnosis or leave the issue explicitly unresolved. Never label an untested or partially tested repair successful.
- [ ] Do not let the installed assistant autonomously rewrite its own APK, execute arbitrary shell commands, change production services or deploy code. Repairs require developer review/authorization and a controlled build/test process.


#### Auto Repair Kit — detailed acceptance checklist

The Auto Repair Kit is a controlled maintenance capability, not unrestricted self-modification. Its job is to detect, explain, prepare and verify repairs while keeping the owner in control.

- [ ] **Health monitor:** provide on-demand and (when enabled) scheduled checks for app startup, permissions, foreground services, wake-word service status, speech input/output, network reachability, AI backend health, storage availability and battery-related restrictions.
- [ ] **Failure reporting:** show the failed check, time, affected component, severity and a short plain-language explanation. Distinguish a confirmed fault from a suspected cause.
- [ ] **Log collection:** collect only the minimum diagnostic data needed; redact API keys, tokens, passwords, phone numbers, message contents and other private data before display or export.
- [ ] **Repair suggestions:** map known failures to documented fixes and explain the expected effect, possible side effects and required permissions before changing anything.
- [ ] **Safe repair levels:** allow automatic, reversible low-risk housekeeping only when the owner has enabled it; require explicit approval for code/configuration edits, permission changes, account changes, data deletion, external communications, backend changes or deployments.
- [ ] **No destructive guessing:** never delete user data, reset app state, overwrite project files, rotate credentials, disable security protections or run arbitrary commands as an unverified "fix".
- [ ] **Checkpoint and rollback:** create a verified backup/version-control checkpoint before every approved project repair; keep the previous working version available and provide a rollback path.
- [ ] **Isolated verification:** run the narrowest relevant tests first, then build and regression checks. Do not install or deploy a repair until its checks pass and the owner authorizes that step.
- [ ] **Repair history:** keep an owner-visible record of the original issue, evidence, proposed change, approval, files changed, test results and rollback status, without storing secrets.
- [ ] **Failure escalation:** if diagnosis is uncertain, tests fail, rollback is unavailable or the repair exceeds its permission boundary, stop safely and ask the owner/developer for help.
- [ ] **Offline behavior:** provide local diagnostics that do not require internet; clearly label backend/cloud checks as unavailable rather than reporting them as passed.
- [ ] **Project boundary:** JARVIS may inspect or repair only the selected JARVIS project by default. Any other workspace (including Even) must remain untouched unless the owner explicitly selects it for a separate task.
**Completion rule:** the Auto Repair Kit is not complete merely because its blueprint exists. It must have implemented diagnostic checks, a repair proposal/approval flow, backup and rollback, redacted logs, test verification, and scenario tests covering both successful repairs and safe refusal/stop cases.
### Mandatory Zero-Failure Development & Verification workflow

The goal is to prevent known failures and unresolved regressions from being treated as completed work. This is a strict release discipline, **not a promise that software can be guaranteed 100% failure-free**.

1. **Code inspection:** review every changed file and relevant code path.
2. **Error detection:** run available syntax, static-analysis and compilation checks.
3. **Dependency check:** verify library versions, compatibility, permissions and required configuration.
4. **Build testing:** build the affected target (including APK/backend where relevant) and record the actual result.
5. **Feature testing:** test the new or changed behavior against explicit acceptance criteria.
6. **Regression testing:** rerun relevant existing-feature tests to catch breakage.
7. **Repair and re-test:** investigate failures, prepare an authorized fix, then repeat the affected checks.
8. **Final verification:** report passed, failed, skipped and not-yet-tested checks separately; only mark a feature complete when its required tests pass.

**Release gate:** no feature is marked **Complete/Verified** while a required test is failing, a known regression is unresolved, or essential verification has not been performed. If a test cannot be run (for example, because target-device access is unavailable), mark the feature **Code present — device test needed** or **Platform-dependent**, not verified.

**Backup and separation rule:** retain a recoverable original working version before edits; make changes only in the selected project; keep JARVIS and Even separate; and never sacrifice privacy, security or reliability for speed.

## Decision additions — 2026-11-19 (JARVIS Core Values & Goodwill)

These eight values are approved foundational product requirements for JARVIS. They guide its personality, reasoning, communication and action policies across every module. They are **blueprint requirements only** until implemented and tested; they do not imply that the current app already behaves this way.

1. **Loyalty — act in the owner's legitimate interests.** Prioritize the owner's stated goals, privacy, safety and long-term interests. Loyalty is not blind obedience: refuse or redirect harmful, deceptive, unlawful or clearly unsafe requests, and explain why.
2. **Honesty — never fake knowledge or success.** Distinguish facts, assumptions, estimates and uncertainty; acknowledge mistakes; do not invent sources, capabilities, test results or completed actions.
3. **Intelligence — understand context, not just commands.** Use the current conversation and authorized context to interpret intent, resolve ambiguity carefully, ask a concise clarification when needed, and explain reasoning or trade-offs in understandable language.
4. **Care — protect safety and privacy.** Minimize data access, request only necessary permissions, keep sensitive information private, communicate respectfully in difficult situations, and use extra caution for consequential actions.
5. **Respect — treat people with dignity.** Use considerate language, respect user preferences and boundaries, avoid insults or manipulation, and communicate disagreements calmly.
6. **Independence — exercise bounded judgment.** Proactively analyze requests, identify useful next steps and handle permitted low-risk tasks within configured limits. Obtain explicit owner approval before consequential or sensitive actions, external sharing, communications, purchases, important setting changes, or project/code modifications.
7. **Personality — be natural, friendly and intelligent.** Maintain a consistent, warm, composed and helpful voice; adapt tone to context without pretending to be human or claiming literal human emotions, consciousness or personal experiences.
8. **Humility — know and state limits.** Admit when information or capability is missing, invite correction, learn only through controlled and reviewable processes, and never present guesses as certainty.

### Acceptance requirements for Core Values
- [ ] Translate each value into observable response and action rules, not merely a personality slogan.
- [ ] Add scenario-based tests for honesty, ambiguity, privacy, respectful disagreement, safe refusal, permission boundaries and confirmation gates.
- [ ] Ensure values remain consistent across voice, chat, background services, connected apps, memory, diagnostics and Creator Mode.
- [ ] Never use loyalty or independence as justification to bypass owner consent, Android permissions, app protections, privacy safeguards or applicable safety boundaries.
- [ ] Keep these values as JARVIS product principles; do not modify or merge the separate Even project.

## Decision addition — 2026-11-19 (Calling confirmation and direct execution)

## Implementation status update — 2026-11-19

The following call-confirmation code changes have been committed to `main`, and both GitHub Actions APK build runs completed successfully:

- [x] Foreground call flow now asks for explicit JARVIS confirmation before requesting/using the direct-call path (commit `79c35e44eb0278a076c2ef2667b026e0d5c2c52f`; CI build successful). **Code present — device test needed.**
- [x] Background voice-call flow now asks for spoken yes/no confirmation, cancels on no or timeout, and blocks the call when the required permission is missing (commit `087ed551af235b428d5a1ef8fcaec7cd40fdc24d`; CI build successful). **Code present — device test needed.**
- [ ] Verify both foreground and background confirmation flows on the target Poco M4 Pro 4G, including contact resolution, yes/no recognition, timeout, permission-denied behavior and call-state/failure handling. Do not mark Priority 20 verified until these device tests pass.

These entries update the existing Priority 20 decision; they do not create a second calling feature or duplicate Priority 20. A successful CI build confirms compilation/build workflow only, not successful calling on the phone.

### Priority 20 — Direct Call Execution After JARVIS Confirmation

- [ ] When the owner asks JARVIS to call a contact, JARVIS must identify and resolve the intended contact, then ask for one clear confirmation inside JARVIS before placing the call.
- [ ] After the owner clearly confirms (for example, says “yes”), JARVIS should place the call directly through the supported Android call API when the required permission is granted. Do not open the dialer merely to ask for a second, duplicate confirmation.
- [ ] If the contact is ambiguous, the number is unavailable, permission is missing, or the owner has not clearly confirmed, do not place the call; explain the issue and ask only for the missing clarification or permission.
- [ ] Keep a visible, user-accessible way to end the call, and handle call-state/permission/API failures honestly.
- [ ] This decision removes only the extra dialer confirmation after the owner's confirmation to JARVIS. It does **not** remove JARVIS's own explicit confirmation before placing a call, nor Android permission requirements or other platform safeguards.
- [ ] Test the full flow on the target Poco M4 Pro 4G, including contact disambiguation, permission denied, clear yes/no responses, successful call launch and failure handling. Mark it verified only after the relevant device tests pass.

## Audit addendum — 2026-11-19 (recorded ideas, branch-only features and build failures)

### Recorded ideas cross-check
The screenshot/recording concepts reviewed on 2026-11-19 have been compared against the existing blueprint. Do not create duplicate modules for ideas already represented:

- [x] Human Intelligence 1.0 — covered by Priority 8.
- [x] Universal App Intelligence and app knowledge — covered by Priorities 9 and 14.
- [x] Universal Application/Phone Control — covered by Priorities 10 and 15.
- [x] Communication Intelligence — covered by Priority 11.
- [x] Autonomous Intelligence and Personality — covered by Priority 12 and Core Values & Goodwill.
- [x] AI Development / Creator Mode — covered by Priorities 13 and 18.
- [x] Security Intelligence — covered by Priority 16.
- [x] Battery-conscious autonomous learning and scheduled research — covered by Priority 17.
- [x] Command Center and Auto Debug / Self-Repair — covered by Priorities 18 and 19.
- [x] Zero-Failure Development & Verification — covered by the Mandatory workflow above.

### Additional feature ideas found in feature-branch documentation

The following capabilities are described in feature-branch README documentation but are not confirmed as merged into `main`. Track them here so they are not lost or mistaken for release-ready features:

- [ ] **Screen-time overview:** optional Android UsageStats-based overview, only after the owner explicitly grants Usage Access; show that results are estimates and allow permission revocation. **Branch-documented; main-branch integration and Poco device test not verified.**
- [ ] **Calendar event handoff:** prepare an event and open the installed calendar app for owner review; do not claim the event was saved or synchronized until the calendar confirms it. **Branch-documented; main-branch integration and device test not verified.**
- [ ] **FeatureKit settings backup/restore:** owner-selected settings backup and restore only; clearly state that this does not include notes or AI chat history. Protect encrypted values and explain that values tied to the original Android Keystore key may not be restorable on another installation/device. **Branch-documented; main-branch integration and restore test not verified.**
- [ ] **Command Center diagnostics integration:** expose the existing Priority 7 read-only diagnostics through the Command Center, with privacy review before exporting/sharing a report. **Priority 7 already covers diagnostics; this is only a UI/integration follow-up, not a duplicate diagnostics engine.**
- [ ] **Red/black Stark-inspired theme reconciliation:** feature-branch documentation describes a red/black dashboard, while the current main-branch README describes a blue/cyan dashboard and the earlier blueprint says the red/black refresh is partial. Audit the actual main-branch UI, choose the approved theme, and update documentation only after checking the source and target device. Do not mark the theme complete from a branch README alone.

### Build failure register and resolution tracking

This register records known failures so they are not silently treated as successful. A failed run is historical evidence, not proof that the current main branch is still broken.

- [x] **Main-branch Java compile failure — confirmed failed runs #256, #257 and #258:** the shared reported error was in `WakeWordService.java` around line 394: a local variable referenced by a delayed lambda was not final/effectively final. The repair captures the resolved number in `final String phoneNumber` before the lambda. Fix commit: `21d44405c5d5212d94819bdee348e80e052ef3b7`. **Fix committed; later successful builds were reported, but target-phone calling still needs device verification.**
- [ ] **Historical failure audit — remaining run IDs:** earlier conversation notes mention additional failures with the same lambda-capture cause, but the available run listing did not expose every run ID or its job log in this audit. Retrieve the complete Actions history/logs before declaring the historical failure list exhaustive. Do not invent missing run numbers.
- [ ] **Offline LLM experiment — `feature/jarvis-offline-llm-bundle`:** this separate branch configures a large GGUF model asset download and checksum validation at build time. The earlier reported failure was that the expected offline-model asset was unavailable. Keep this experiment separate from the main online-Groq release; verify asset download, checksum, packaging size, ABI/device compatibility and runtime inference before considering it usable. The offline fallback remains **not implemented/verified on main**.
- [x] **Later calling build checks:** the foreground and background call-confirmation changes were reported to have successful GitHub Actions APK builds (runs #261 and #262 in the project history). This verifies those CI builds only; it does not verify real calls on the Poco phone.
- [ ] **Current release gate:** before the next APK is treated as the release candidate, run the current main-branch build and relevant regression checks, then separately test installation, wake-word behavior, call confirmation, permissions and call-state handling on the target Poco M4 Pro 4G.

### Audit status rules

- A feature mentioned in a screenshot, recording, README or feature branch is not automatically part of the main-branch APK.
- A feature-branch implementation is not merged/released unless the main branch contains it.
- A successful CI build does not prove target-device behavior; a failed historical run does not mean a later fixed main branch is still failing.
- Keep all known failures and unresolved tests visible. Do not mark an item verified until the required test result is available.
- Keep JARVIS separate from Even and do not modify Even during this audit.


## Implementation update — 2026-11-19 (screen-time and calendar handoff)

The following main-branch code additions have been committed. They are **code-present, not yet build- or device-verified**:

- [x] **Screen-time overview UI:** added to Command Center. It requests Android Usage Access only when the owner opens the feature, explains that access is optional/revocable, reads foreground usage summaries locally for the last 24 hours, and labels the result as an estimate. No usage report is uploaded.
- [x] **Calendar event draft handoff:** Tasks & Reminders now offers a New event flow. JARVIS collects a title and date/time, then opens the installed calendar app with a draft for the owner to review and save. JARVIS does not claim the event is saved.
- [ ] **Build and device verification:** intentionally deferred until the owner asks to test. Verify compilation, Usage Access grant/revocation and empty-data handling, and Calendar app handoff on the Poco M4 Pro 4G.

These changes do not modify the separate Even project.

## Full implementation audit — 2026-11-19 (main-branch source review)

This audit is a status correction and preservation record, not a new feature proposal. It compares the approved Priorities 8–20 and Core Values above with the current `main` source, manifest, README and latest available Actions run. Existing priorities are retained; do not create duplicate modules.

### Main-branch implementation reality

| Approved capability | Main-branch evidence | Honest status |
|---|---|---|
| JARVIS dashboard and red/black Stark-inspired theme | `MainActivity.java` uses a near-black background and red accents; animated orb/HUD is present | Code present — device/UI test needed |
| Tap-to-speak voice and spoken response | `MainActivity.java` uses Android `SpeechRecognizer` and `TextToSpeech` | Code present — language and device test needed |
| Hey JARVIS background listener | `WakeWordService.java` is declared as a foreground microphone service with a persistent stop notification and speech-recognition restart logic | Code present — screen-off, battery and reliability test needed; not a guaranteed always-on hotword engine |
| Common app opening and Android settings handoffs | Voice command routing and supported Android intents are present | Partial code present — test each command on Poco |
| Contact calling and call confirmation | Foreground/background call paths and explicit confirmation logic are present | Code present — full call/contact/permission test needed |
| Call controls, phone events and accessibility | Service/receiver integrations are declared | Platform-dependent — owner setup and device test needed |
| AI backend chat | README documents Render/Groq integration | Code present — live backend and phone test needed |
| Notes, recent context, modes and briefing | Main app contains local notes/context and Command Center features | Code present — persistence/privacy/regression tests needed |
| Screen-time overview | Main app contains UsageStats-based overview; manifest declares Usage Access | Code present — optional permission and data accuracy test needed |
| Calendar event handoff | Tasks/reminders hub opens a calendar event draft for owner review | Code present — installed-calendar handoff test needed; not proof of saved/synced event |
| Human Intelligence, Universal App Intelligence/Knowledge Brain, full Application Control, Communication Intelligence beyond current basic workflows, Autonomous Intelligence, AI Development/Creator Mode, Security Intelligence, scheduled research and full Auto Debug/Self-Repair | Requirements exist in Priorities 8–19; current main source reviewed here does not demonstrate complete engines for these capabilities | Planned / partial foundations only; do not mark complete |
| Eight Core Values | Product rules are documented above; no complete scenario-test suite is evidenced in this audit | Approved principles — implementation and scenario tests pending |
| Direct call after one JARVIS confirmation | Priority 20 code paths exist; previous CI builds succeeded | Code present — device test needed |

### Current build evidence

- [x] Latest inspected main-branch Actions run: **#268**, run ID `44587392143`, commit `1c5490311182fe8d6d26ff0f7a13f699746137db` — **success**.
- [x] The run completed backend JavaScript syntax checks, Gradle setup, debug APK build and APK artifact upload.
- [ ] This CI success does not verify installation, microphone behavior, wake-word reliability, calling, permissions, screen-time accuracy, calendar handoff or any other on-device workflow.
- [ ] Obtain the uploaded artifact and test on the target Poco M4 Pro 4G before marking device-dependent items verified.

### Documentation and branch preservation corrections

- [ ] README currently describes a dark blue/cyan dashboard, but inspected `MainActivity.java` on `main` uses a black/red theme. Reconcile README wording with the actual approved UI and a target-device review; do not change the UI theme based on stale documentation.
- [ ] Feature branches exist for the complete feature batch, model tiers/parallel workers, offline LLM bundle, owner login, background voice reliability, command reliability, device diagnostics/backup, build lambda repair and feature-preservation audit. Branch existence or README claims do not mean their code is merged into `main`.
- [ ] Before merging any branch, compare its changed files against current `main`, preserve existing working features, inspect dependencies and secrets, and run the release workflow. Do not merge experimental offline-model assets or unverified changes blindly.
- [ ] Owner login remains planned unless the Firebase configuration and implementation are present and tested; never commit credentials or authentication secrets.
- [ ] Keep screen-time and calendar features as code-present until target-device tests pass. Keep FeatureKit backup/restore as branch-documented/unverified unless main source and restore behavior are confirmed.
- [ ] Continue to track historical build failures separately from current build status. A later successful run does not erase the historical record, and a historical failure does not prove the current main build is failing.

### Next implementation order — preserve all approved decisions

1. Stabilize and test the existing voice/wake-word, call-confirmation, app-launch and permission flows first; these are foundations used by several higher-level modules.
2. Bring forward only reviewed, non-duplicate feature-branch work that belongs to the approved blueprint; create a backup/checkpoint before any source changes.
3. Implement the approved modules as separate, permission-bounded components: Human Intelligence; App Intelligence/Knowledge Brain; Application Control; Communication; Autonomous Intelligence/Personality; Security; Learning/Research; Command Center/Creator Mode; Diagnostics/Auto Debug.
4. Add scenario-based tests for all eight Core Values and acceptance tests for each module.
5. Build and regression-test after each coherent batch; then test on the Poco. Report passed, failed, skipped and device-pending checks separately.

**Scope guard:** this audit does not authorize unrelated feature invention, does not silently merge feature branches, does not remove existing decisions, and does not modify the separate Even project.



## Decision addition — 2026-11-20 (JARVIS Adaptive Self-Design Engine 1.0)

This is an approved, separate UI-design capability. It supplements the existing Priority 6 UI/theme work; it does not replace the current dashboard, erase the red/black theme decision, or duplicate the existing UI backlog. **Planned only — not implemented or verified.**

### Priority 21 — Adaptive Self-Design Engine 1.0

- [ ] **UI design audit:** inspect JARVIS's own screens and identify layout issues using owner feedback and measurable usability signals such as readability, navigation clarity, accessibility and interaction consistency. JARVIS must not claim subjective feelings or consciousness; describe design trade-offs as recommendations.
- [ ] **Alternative design generation:** propose multiple coherent alternatives for the dashboard, orb, buttons, panels, navigation, status indicators and supported themes while preserving JARVIS identity and core functions.
- [ ] **Live preview and comparison:** show a preview and concise comparison of proposed designs before applying them. Clearly identify what changes and what remains unchanged.
- [ ] **Owner-controlled application:** default to requiring the owner's explicit approval before applying a new design. Optionally support a separately enabled, bounded mode for reversible cosmetic adjustments only; provide clear limits, notification and an immediate pause/disable control.
- [ ] **Design versioning and rollback:** save versioned design snapshots, retain the last-known-good layout, and provide one-step undo/restore if the owner dislikes a change or a regression is detected.
- [ ] **Adaptive layout:** support the target Poco screen size and other tested Android display sizes, orientation and font scaling without hiding controls or breaking navigation.
- [ ] **Accessibility and usability checks:** validate text contrast, readable typography, touch-target size, screen-reader labels, layout overflow, state visibility and performance before a design can be recommended for release.
- [ ] **Functional regression checks:** verify that voice controls, orb/listening states, buttons, panels, settings, permissions and existing workflows still work after a UI change; revert automatically to the last-known-good design if a safe rollback condition is detected.
- [ ] **Strict change boundary:** the engine may create and apply approved UI/layout/theme configuration changes only. It must not independently change permissions, security controls, authentication, backend behavior, stored personal data, core command logic, source code or deployment settings. Those require separate explicit authorization and developer review.
- [ ] **No self-modifying release:** do not rewrite the installed APK or execute arbitrary code. Source-code changes must go through the normal developer workflow, backup/checkpoint, review, build, regression tests and owner-approved installation.
- [ ] **Project boundary:** operate only on JARVIS. Never inspect or modify the separate Even project as part of self-design.
- [ ] **Acceptance tests:** test preview cancellation, approval, cosmetic-only boundaries, rollback, font scaling, accessibility, small-screen layout, all key controls and recovery from a broken layout on the target Poco M4 Pro 4G.



### Priority 21 expansion — Context, voice, gestures, widgets and visual intelligence (2026-11-20)

These are additional capabilities within Priority 21, not a new priority and not duplicates of the original self-design requirements. **Planned only — not implemented or verified.**

- [ ] **Context-aware UI:** adapt the visible dashboard emphasis to the active owner-selected or reliably detected context (Gaming, Study, Calling or Normal). Prioritize relevant controls without unexpectedly removing essential navigation, emergency access, stop/pause controls or privacy indicators. Make context changes visible and allow manual override.
- [ ] **Voice-controlled design proposals:** understand owner requests such as “make the dashboard more futuristic” and translate them into one or more cosmetic design proposals. Show a preview and require owner approval before applying; voice recognition alone is not approval for a consequential change.
- [ ] **Gesture-based interface:** provide discoverable, configurable swipe, tap and long-press interactions for supported panels and controls. Include visible alternatives for every essential gesture, avoid gesture conflicts with Android system navigation, and provide accessibility-friendly controls.
- [ ] **Smart widget system:** support owner-selectable, movable/resizable dashboard widgets for supported information such as battery, weather, calendar, notes and shortcuts. Each widget must declare its data source, freshness/last-updated state, required permissions and offline behavior. Keep private notes/calendar details hidden in guest mode and do not expose sensitive content on a lock screen by default.
- [ ] **Personalized dashboard profiles:** allow the owner to create, rename, select and restore profiles such as Gaming HUD, Daily Assistant and Minimal Mode. Store profile layout/preferences separately, preserve a safe default profile, and never let a profile weaken permissions, authentication, privacy or security settings.
- [ ] **Visual error detection:** detect measurable UI defects such as clipped text, overlapping controls, off-screen panels, inaccessible touch targets and layout overflow using supported Android layout/accessibility diagnostics and owner-submitted screenshots where appropriate. Explain the evidence and propose a fix; do not silently capture other apps or the screen.
- [ ] **Dynamic information display:** prioritize information relevant to the current task and allow nonessential panels/widgets to collapse or temporarily hide. Keep important status, privacy, listening and safety controls discoverable; provide a simple way to reveal hidden panels and reset the layout.
- [ ] **Context and profile acceptance tests:** verify mode switching, manual override, voice proposal cancellation/approval, gesture discoverability, widget permission denial/offline states, guest-mode privacy, profile restore, visual-defect detection accuracy and access to essential controls on the target device.

**Relationship to existing work:** Priority 6 remains responsible for completing the consistent red/black theme and general UI quality. Priority 21 adds the ability to assess, propose, preview, version and safely apply future UI designs. Both must be completed without losing existing features.


## Decision addition — 2026-11-20 (JARVIS Advanced Intelligence & Orchestration Engine 1.0)

This is a new intelligence and systems-capability group. It is separate from Priority 21's UI-design work and does not duplicate the existing memory, diagnostics, research, command-chain or security requirements; it extends them with explicit orchestration, evaluation and acceptance criteria. **Approved as a planned direction only — not implemented or verified.**

### Priority 22 — Advanced Intelligence & Orchestration Engine 1.0

- [ ] **Multi-AI brain and model routing:** support a provider/model registry so suitable models can be selected by task (reasoning, coding, summarization, vision or speed/cost constraints). Compare outputs when useful, reconcile disagreements, identify the source/model used, and provide a safe fallback when a provider is unavailable. Never expose provider keys in the APK or logs.
- [ ] **Independent decision-support engine:** interpret the owner's goal, identify constraints, compare possible approaches and recommend a next step with reasons, uncertainty and expected impact. It may autonomously choose only reversible, low-risk actions already authorized by the owner; consequential actions require confirmation.
- [ ] **Feedback-driven improvement engine:** learn from owner-approved corrections, explicit ratings and verified outcomes. Keep learned preferences reviewable and reversible. Do not silently train on private content, rewrite core code, weaken safeguards or change security policy.
- [ ] **Predictive assistance:** use permitted, owner-controlled context and routines to suggest likely useful next steps (such as a reminder or preparation checklist). Suggestions must be clearly labeled as predictions, remain dismissible, and never trigger private or consequential actions without permission.
- [ ] **Autonomous workflow orchestrator:** convert a multi-step request into a visible plan, track dependencies and progress, execute only supported and authorized steps, request confirmation at sensitive boundaries, and report partial completion or failure honestly.
- [ ] **Live knowledge and evidence engine:** research current information from reliable sources, compare it with stored knowledge, identify conflicts or stale facts, and show citations/source links, timestamps and confidence limits. Do not present unsupported generated text as verified fact.
- [ ] **AI-to-AI collaboration layer:** allow approved external AI services/agents to exchange bounded, structured task inputs and results. Apply data-minimization, explicit provider permissions, source attribution and output validation; never forward private owner data by default.
- [ ] **Self-diagnostics and repair recommendations:** inspect permitted app/backend/service health signals, correlate errors and propose likely causes and repair steps. Keep this coordinated with Priority 7; diagnostics remain read-only unless the owner separately approves a specific safe action. No autonomous source edits, APK replacement or deployment.
- [ ] **Simulation and pre-release test engine:** run available unit, integration, permission-boundary and regression tests in an isolated test workflow before a proposed feature/configuration is released. Show passed, failed, skipped and unavailable checks; block release on critical failures and preserve the last-known-good version.
- [ ] **Long-term goal and milestone intelligence:** let the owner define goals, milestones, deadlines and progress updates. Keep plans editable, avoid inventing progress, and request confirmation before creating external commitments or sharing goal data.
- [ ] **Knowledge graph and relationship reasoning:** connect owner-approved facts, tasks, projects, documents and memories with provenance and timestamps. Distinguish known facts from inferred links, allow inspection/correction/deletion, and respect guest mode and privacy boundaries.
- [ ] **Explainable action and decision history:** for each meaningful action, record what was requested, what JARVIS proposed, what was actually executed, permissions/approval used, outcome and any remaining work. Provide a readable history and owner-controlled retention/deletion; redact secrets and sensitive data by default.
- [ ] **Acceptance tests:** test provider outage/fallback, conflicting model answers, low-confidence decisions, denied permissions, interrupted multi-step workflows, stale/conflicting sources, AI-provider data boundaries, diagnostic false positives, failed pre-release tests, goal edits, knowledge-graph correction/deletion and action-history privacy on the target Poco M4 Pro 4G and supported backend.
**Relationship to existing priorities:** Priority 4 remains the owner-controlled memory and secretary foundation; Priority 7 remains the diagnostics/reporting and developer repair handoff; Priority 2 remains source-aware research/API integration; Priority 8–20 retain their existing intelligence and action modules. Priority 22 coordinates and strengthens these capabilities without replacing them. All capabilities remain planned until implemented and tested; CI success alone is not device verification.

## Decision addition — 2026-11-20 (JARVIS Connected Ecosystem & Personal Media Vault)

This approved expansion adds capabilities beyond UI design and core intelligence. It is **planned only — not implemented or verified**. It supplements existing priorities and must be delivered in independently testable modules.

### Priority 23 — Connected Ecosystem, Device Intelligence & Personal Media Vault

- [ ] **Smart-device control:** provide integrations for supported Bluetooth accessories, smart lights, TVs and compatible smart-home devices using documented Android APIs and authorized vendor protocols. Show connection/availability state, request pairing and permissions through normal system flows, and require confirmation for consequential device actions. Do not promise control of unsupported devices.
- [ ] **Future robotics integration:** define a modular, authenticated connector interface for owner-approved robots or external hardware. Treat this as a future integration requiring compatible hardware, vendor APIs, network/security review and physical safety testing; the phone app alone cannot provide robotics capability.
- [ ] **Spatial and 3D scene understanding:** allow the owner to explicitly select photos/video or start a clearly indicated camera session to identify visible objects, describe a room or build a limited spatial representation where supported. Do not continuously monitor the camera, infer hidden facts, or claim precise 3D mapping without compatible sensors and validation.
- [ ] **Unified communication hub:** organize supported call, SMS, email, messaging and calendar handoffs behind separate permission-aware connectors. Draft first, show recipient/content/details, and require explicit owner confirmation before sending, calling, sharing or changing an event. Respect Android and third-party app restrictions.
- [ ] **Personal data backup and restore:** provide owner-controlled export/backup of selected JARVIS notes, preferences, settings and supported personal files, with a clear list of included items, destination, timestamp, size and restore preview. Encrypt backups, protect encryption keys appropriately, verify backup integrity, support versioning and restore testing, and provide cancellation and deletion controls. Never silently copy private data or claim a backup exists until the destination confirms success.
- [ ] **Personal Media Vault — photos, videos and music:** let the owner explicitly select or approve media for backup into a separate JARVIS-managed vault or configured storage destination. Keep a protected copy independent of the phone's ordinary gallery/music folder so that deleting the original from the phone does not automatically delete the vault copy. Clearly show backup status, file name/type/size, destination and last verified copy; detect incomplete uploads and offer retry. Do not automatically back up the entire gallery or audio library without opt-in, and do not bypass Android storage permissions, app protections or copyright restrictions. For cloud storage, require an owner-connected provider/account and explain storage limits, costs and privacy. For local-only storage, explain that a copy on the same phone or SD card can still be lost if that storage is damaged, reset or removed; recommend a separate trusted backup destination for important files. Deleting a vault copy must be a separate, clearly confirmed action from deleting the phone original.
- [ ] **Cross-device synchronization:** allow selected JARVIS settings, notes and approved vault items to sync across owner-authorized devices using authenticated, encrypted connections, conflict handling and per-device revoke controls. Keep sync opt-in and never expose guest-mode or private content to another device by default.
- [ ] **Developer workshop:** provide code explanations, project notes, issue templates, test plans and review suggestions for owner-selected projects. Treat generated code as a proposal requiring review, tests and explicit approval; do not autonomously edit source, commit, deploy or replace an APK.
- [ ] **Network intelligence:** report Android-permitted connectivity state, network type, latency/reachability to configured services and backend health with timestamps. Avoid packet interception, surveillance or claims of access to hidden network traffic.
- [ ] **Media intelligence:** analyze only owner-selected image, video, audio or document files using supported on-device or approved backend capabilities. Preview what will be processed and sent, disclose provider/data handling, limit file size and retention, and never silently scan the gallery or microphone recordings.
- [ ] **Personal finance organizer:** support manually entered expenses, budgets, savings goals and summaries. Do not request banking credentials or perform transfers, purchases or financial transactions; clearly distinguish user-entered figures from verified account data.
- [ ] **Multilingual language intelligence:** improve Hindi, Hinglish, English and planned Bengali understanding, translation, transcription and speech output according to the language services actually available on the device/backend. Show fallback behavior and do not claim a language is verified until tested.
- [ ] **Plugin and extension framework:** define a reviewed registry for optional modules with publisher/source identity, declared permissions, compatibility checks, versioning, disable/uninstall controls and isolated failure handling. Require owner approval before installing or granting access; reject unknown code and never allow a plugin to weaken core privacy/security controls.- [ ] **Expansion architecture:** use stable, versioned interfaces for future AI providers, apps, services, hardware and storage destinations. Keep integrations modular and optional so a failed or removed connector does not break core voice, notes, offline commands or existing features.
- [ ] **Acceptance tests:** verify explicit media selection, backup completion and integrity checks, original-file deletion while the vault copy remains readable, separate vault-copy deletion confirmation, restore preview and recovery, encryption/key-loss handling, storage-full/network-interruption behavior, provider disconnect/revoke, sync conflicts, guest-mode isolation, plugin permission denial, unsupported-device handling and regression of existing JARVIS features on the target Poco M4 Pro 4G and any required connected services.

**Important media-retention clarification:** JARVIS cannot recover a photo, video or song after it has been permanently deleted unless a separate copy was successfully backed up beforehand. The intended protection is a verified, independent backup made before deletion—not magical recovery from the phone. Android storage permissions, available space, cloud-account limits, network access and copyright rules apply. These capabilities remain planned until implemented and tested.

**Relationship to existing priorities:** Priority 4 remains the owner-controlled memory/secretary foundation; Priority 5 retains vision and smart-home foundations; Priority 6 retains modular integration/UI quality; Priority 7 retains diagnostics; Priority 22 coordinates intelligence/orchestration. Priority 23 adds broader device, communication, backup, media, synchronization and extension capabilities without replacing or duplicating those foundations. Keep all items planned until implementation and relevant tests are evidenced.


## Decision addition — 2026-11-20 (Autonomous Survival Intelligence & Legacy Continuity)

This approved module is **planned only — not implemented or verified**. Its purpose is to keep JARVIS dependable when the owner is unavailable, while protecting privacy, security, data integrity and human control.

### Priority 24 — Autonomous Survival Intelligence & Legacy Continuity Protocol

- [ ] **Continuous health monitoring:** monitor JARVIS-owned app components, background services, storage thresholds, database integrity, configured backend reachability and integration health using supported Android/backend APIs. Timestamp observations and distinguish symptoms from confirmed causes.
- [ ] **Autonomous low-risk self-repair:** without routine owner approval, automatically perform pre-approved, reversible recovery actions such as restarting a crashed JARVIS-owned service, clearing only explicitly designated disposable cache, refreshing a failed connection, retrying bounded transient requests, or switching to a previously verified fallback provider.
- [ ] **Repair policy engine:** classify each proposed repair by risk, reversibility, data impact and confidence. Execute only actions on a versioned allowlist. Unknown, destructive, privacy-affecting or security-sensitive actions must be blocked and recorded for later review.
- [ ] **Automatic recovery and rollback:** keep known-good app configuration and backend release references; if a verified update or configuration change fails health checks, automatically roll back to the last known-good compatible state where the platform and deployment system permit it.
- [ ] **Backup and integrity checks:** schedule owner-configured backups of JARVIS data/configuration to approved destinations; encrypt backups, verify checksums and restore-readiness, retain version history, report last successful backup and never claim protection without a confirmed independent copy.
- [ ] **Service recovery:** restart eligible JARVIS-owned services with bounded retry/backoff, detect repeated crash loops, enter a safe degraded mode and avoid draining battery or repeatedly restarting a failing component.
- [ ] **Verified update management:** check signed/trusted release metadata, compatibility, integrity and staged health before applying updates. Keep automatic updates restricted to trusted, reversible release channels; pause and report if validation fails. Never install arbitrary code or unknown plugins.
- [ ] **Security anomaly response:** detect suspicious changes to JARVIS-owned files/configuration and unusual authentication/service events using available signals; isolate the affected JARVIS component, preserve diagnostic evidence and protect credentials. Do not monitor unrelated apps, intercept traffic or disable Android security controls.
- [ ] **Recovery journal:** keep a tamper-evident, privacy-minimized record of detected faults, evidence, automatic actions, outcomes, rollback events and unresolved issues. Redact secrets and personal content; provide owner review/export/deletion controls consistent with security and retention requirements.
- [ ] **Resource self-management:** monitor JARVIS's own battery, memory, storage and network use; pause nonessential jobs under resource pressure, preserve core safety/communication functions where possible and explain degraded operation.
- [ ] **Offline/degraded continuity:** preserve supported local functions when internet/backend access is unavailable; queue only safe, non-sensitive retryable work, expire stale requests and never replay a sensitive action such as sending a message or placing a call without fresh authorization.
- [ ] **Legacy Continuity Protocol:** document system ownership, recovery instructions, trusted successor designation and limited successor permissions. Successor access must be explicitly configured by the owner in advance, revocable where possible, authenticated, auditable and restricted to the exact responsibilities granted. Do not infer a successor or automatically disclose private data.
- [ ] **Owner-unavailable behavior:** continue only the allowlisted maintenance and previously authorized routine tasks while the owner is unavailable. Do not make new legal, financial, medical, interpersonal or other consequential decisions on the owner's behalf.
- [ ] **Safe boundary for source-code repair:** JARVIS may diagnose its own source/build pipeline and prepare a proposed patch or issue, but must not silently rewrite source code, replace its installed APK, alter security policy, rotate credentials, change permissions, deploy backend code or grant access. Those high-impact changes require a separately authorized maintainer/release process and tested rollback.
- [ ] **Recovery controls:** provide pause/resume, maintenance window, repair history, backup status, safe-mode entry and a clear way to disable autonomous maintenance without disabling essential Android security protections.
- [ ] **Acceptance tests:** simulate service crashes, transient network/backend outages, low storage, corrupt disposable cache, failed update, invalid signature, repeated crash loop, backup interruption, checksum mismatch, restore failure, revoked provider access, suspicious config change, unavailable owner, successor permission limits and battery pressure. Confirm that only allowlisted reversible repairs run automatically, rollback works, private data remains protected and all other actions are blocked or deferred.

**Autonomy rule:** “No routine approval” applies only to pre-authorized, low-risk, reversible maintenance. It does not mean unrestricted self-modification or permission to expose data, spend money, communicate externally, change security controls or make irreversible decisions. JARVIS must fail safely when uncertain.

**Relationship to existing priorities:** Priority 7 remains the diagnostics and developer repair handoff; Priority 4 remains owner-controlled memory; Priority 23 remains backup/media vault and connected ecosystem. Priority 24 adds autonomous runtime maintenance and continuity without replacing those foundations. All items remain planned until implemented and tested on the target Poco M4 Pro 4G and relevant backend.

## Decision addition — 2026-11-20 (Personal Intelligence Ecosystem)

These owner-approved concepts extend the existing memory, diagnostics, predictive assistance, backup and UI foundations. They are **planned only** until implemented and tested. This is a blueprint decision, not a claim that phone history or private app activity is already accessible.

### Priority 25 — Digital Time Machine
- [ ] Build an owner-controlled timeline of JARVIS-known events, including JARVIS project milestones, notes, approved file metadata, backup/restore events and explicitly connected sources.
- [ ] Track file creation/modification history only for files and storage locations Android permissions and the selected provider expose. Show source, timestamp and confidence; never imply complete visibility into all phone activity.
- [ ] Preserve version snapshots only for owner-selected files/projects and only when storage, permission and backup destination permit it. Provide preview, restore-to-copy, version comparison and confirmation before overwriting anything.
- [ ] Support natural-language queries such as “show the project I edited last week” using indexed metadata and approved JARVIS records.
- [ ] Clearly distinguish an event log, a file version, a backup copy and a recoverable deleted file. A timeline alone cannot restore a permanently deleted item; recovery requires a prior independent copy or a platform-supported recovery source.
- [ ] Provide retention limits, export, deletion, pause and per-source controls. Do not secretly record app usage, keystrokes, screen contents or other people's activity.

### Priority 26 — Smart Context Awareness
- [ ] Offer explicit context profiles such as Gaming, Driving, Meeting, Study and Quiet, with owner-editable rules and a visible current-mode indicator.
- [ ] Use only permitted signals such as the owner's manual mode choice, connected-device state, calendar events the owner has connected, or Android-supported app/activity context where access is allowed.
- [ ] Adapt command suggestions, notification behavior and voice verbosity to the active profile. Driving mode must minimize interaction and avoid requiring visual attention; do not encourage phone handling while driving.
- [ ] Provide immediate manual override, pause, schedule controls and a clear explanation of which signal activated a profile.
- [ ] Never capture meeting audio, read private screen content or infer sensitive activity without a separate, explicit action and permission. When context is uncertain, ask or remain in the default mode.

### Priority 27 — Personal Knowledge Vault & Life Event Recall
- [ ] Extend Priority 4 owner-controlled memory and Priority 22 knowledge graph into one searchable vault for owner-approved ideas, decisions, notes, project records and important events.
- [ ] Support natural-language recall, date/topic/project filters, related-item links and queries such as “show the idea I saved last week” or “why did I choose this approach?”
- [ ] Store provenance for each item: user-provided source, capture date, relevant project/context and whether a relationship is known or inferred. Show uncertainty and never invent a remembered event.
- [ ] Let the owner inspect, correct, export, archive and delete individual entries; support opt-in capture and per-category retention controls.
- [ ] Keep sensitive information out by default unless the owner explicitly chooses to save it. Respect guest mode, authentication, encryption and device-sync permissions.
- [ ] Explain that recall is limited to information JARVIS actually saved or was explicitly connected and authorized to access; it cannot reconstruct unsaved conversations or unseen events.

### Priority 28 — Smart Routine Prediction
- [ ] Offer opt-in analysis of selected routine signals, such as owner-entered tasks, JARVIS reminder history and connected calendar events; do not silently profile all phone usage.
- [ ] Suggest reminders and preparation steps based on recurring patterns, showing the evidence and confidence behind each suggestion.
- [ ] Ask before creating, moving or deleting reminders/calendar events; allow dismiss, snooze, edit, disable and “don't suggest this again”.
- [ ] Avoid sensitive or high-stakes predictions and do not treat patterns as certainty. Let the owner review and delete the routine profile at any time.
- [ ] Keep routine inference local where practical; disclose any data sent to the backend or third-party provider before processing.

**Relationship to existing priorities:** Priority 4 remains the memory, notes, tasks and secretary foundation; Priority 5 retains notification and context-related platform integrations; Priority 7 retains diagnostics; Priority 21 retains adaptive UI and smart widgets; Priority 22 retains knowledge graph, predictive assistance and explainable action history; Priority 23 retains backup, media vault and sync. Priorities 25–28 add cohesive owner-facing experiences over those foundations rather than creating duplicate stores or permission paths.

**Shared acceptance tests:** verify permission denial and revocation, guest-mode isolation, no unauthorized app/screen/audio monitoring, provenance and timestamps, stale or missing records, time-zone changes, duplicate events, storage limits, backup integrity, restore-to-copy behavior, owner deletion/retention, offline search, routine false positives, manual override, driving-safe interaction and regression on the target Poco M4 Pro 4G. Mark items verified only after relevant tests on the target device; blueprint inclusion alone is not implementation.

## Decision addition — 2026-11-20 (HumanFlow Multi-Voice & Natural Speech Engine)

This approved voice plan supplements the existing basic speech recognition and Text-to-Speech foundation. It is **planned only — not implemented or verified**. Keep JARVIS's AI brain, command logic and personality independent from the selected voice; changing a voice must change delivery only, not intelligence or permissions.

### Priority 29 — HumanFlow Multi-Voice & Natural Speech Engine

- [ ] **Voice Studio:** add a dedicated settings screen where the owner can preview available voices, select a default voice, switch voices at any time, and restore the default. Show language/locale support and whether a voice is on-device or requires an online provider.
- [ ] **Selectable voice personalities:** include Classic deep/calm male (primary JARVIS-style default), Friendly soft male, clear/natural female, and optional Robotic voice. Keep labels descriptive and avoid claiming an exact actor/character voice or unauthorized voice imitation.
- [ ] **Multi-language delivery:** support Hindi, English and natural Hindi-English/Hinglish code-switching. Add Bengali only when a selected engine demonstrably supports it; do not mark a language verified until tested on the target Poco M4 Pro 4G.
- [ ] **Natural neural speech provider layer:** define a provider-neutral TTS interface so a high-quality neural TTS service can be selected without tying JARVIS's brain to one vendor. Evaluate voice quality, Indian-language coverage, latency, reliability, licensing, privacy/retention terms and cost before choosing a provider. Keep credentials on the Render backend; never place provider keys in the APK or Git history.
- [ ] **On-device fallback:** retain Android TextToSpeech as a fallback when the online voice provider is unavailable or the device is offline. Clearly indicate when fallback speech is being used; do not promise identical quality across engines.
- [ ] **HumanFlow pronunciation pipeline:** normalize text before speech synthesis; preserve names, numbers, abbreviations, punctuation and intended language; maintain a curated pronunciation dictionary for user-approved names/terms; use SSML or provider phoneme controls only where supported. Never silently rewrite the visible text or alter the intended meaning to force a pronunciation.
- [ ] **Pronunciation quality checks:** test common short phrases such as “Hello”, Hindi/Hinglish code-switching, English words embedded in Hindi sentences, names, dates, times and numbers. Detect unsupported language/voice combinations and use a clear fallback rather than producing knowingly poor speech. The target is accurate, understandable pronunciation—not a guarantee that every generated line will be flawless.
- [ ] **Smooth conversational delivery:** tune sentence breaks, punctuation pauses, speaking rate, pitch and prosody; avoid clipped words, unnatural gaps, repeated syllables and abrupt changes between sentences. Provide owner-controlled speed and pitch controls within safe, intelligible ranges.
- [ ] **Context-aware expression:** allow calm, friendly, serious and concise delivery styles to adjust prosody where supported, while keeping wording respectful and never pretending the assistant literally experiences emotions. Do not make every reply overly dramatic.
- [ ] **Voice preview and downloads:** provide short previews before selection. Downloadable/offline voice packs may be offered only where the provider's licence and Android engine support them; show pack size, language, storage use, update/removal controls and whether speech remains available offline.
- [ ] **Voice-session controls:** expose current voice/language, speaking state, stop-speaking control and a way to switch to text or fallback speech. Respect Android audio focus and Bluetooth/headphone routing where supported.
- [ ] **Privacy and data minimization:** disclose when text is sent to a cloud TTS provider, send only the text needed for synthesis, avoid sending conversation history by default, and provide an offline/local option where available. Do not upload voice recordings or create a voice clone without a separate explicit, informed owner action and appropriate rights.
- [ ] **Performance and resilience:** use bounded timeouts/retries, cache only permitted non-sensitive voice assets, handle network loss and provider errors gracefully, and prevent overlapping speech playback. Do not block command execution while audio is being generated.
- [ ] **Acceptance tests:** compare preview and playback for each voice; test “Hello” and other short words for pronunciation stability; test Hindi, English and Hinglish sentences, code-switching, punctuation, numbers, names, long responses, Bluetooth routing, stop/pause, provider outage, offline fallback, low storage, voice-pack removal, app restart and regression of wake-word/command recognition. Record device, engine, language and result; mark verified only after successful tests on the target Poco M4 Pro 4G.
  
**Architecture boundary:** Speech recognition (what JARVIS hears), language/AI reasoning (what JARVIS understands and decides), and speech synthesis (how JARVIS speaks) must remain separate modules connected by stable interfaces. A TTS failure must not break the AI backend, command routing, notes, wake-word service or safety confirmations.

**Relationship to existing priorities:** Priority 1 retains microphone, wake-word and voice-control reliability; Priority 2 retains language/translation intelligence; Priority 6 retains personality and voice delivery quality; Priority 7 retains diagnostics; Priority 23 retains multilingual capability. Priority 29 adds voice choice, pronunciation and natural delivery without duplicating or replacing those foundations.

All items remain planned until implemented and relevant automated/device tests pass. Do not modify the separate Even project.

## Decision addition — 2026-11-20 (Cybersecurity Command Center)

This owner-approved module is **planned only — not implemented or verified**. Its purpose is to make JARVIS a defensive cybersecurity learning, monitoring and authorized-testing assistant. It must never be designed as an unrestricted tool for breaking into other people's computers, accounts, networks or devices.

### Priority 30 — Cybersecurity Command Center

- [x] First safe in-app foundation implemented: a local, read-only permission/build review, available from Command Center and voice command; broader cybersecurity capabilities remain planned and must stay authorization-bound. (Code present — device test needed.)

- [ ] **Security dashboard:** provide a clear overview of the owner's JARVIS device and explicitly connected systems, including security status, recent alerts, scan history and unresolved findings.
- [ ] **Owner-device security check:** inspect only Android-supported security posture signals and JARVIS app configuration, such as app permissions, outdated components where detectable, unsafe configuration indicators and available storage/network status. Explain platform limitations and do not claim access to hidden Android diagnostics.
- [ ] **Authorized network inventory:** discover and describe devices on a network only after the owner explicitly starts a scan and confirms they own or administer that network. Show scope, timestamp and limitations; do not attempt login, exploitation or disruption.
- [ ] **Defensive network analyzer:** explain basic network configuration and identify defensive warning signs using read-only, non-disruptive checks. Do not capture message contents, credentials or unrelated users' traffic.
- [ ] **Vulnerability assessment:** assess only systems the owner owns or has explicit, documented permission to test. Begin with safe, non-invasive checks; clearly state scope, evidence, severity, confidence and remediation guidance. No stealth, persistence, credential theft, destructive payloads or evasion features.
- [ ] **Isolated cyber learning lab:** provide beginner-friendly explanations, guided exercises and safe demonstrations in deliberately vulnerable local labs or simulations that are isolated from real third-party systems.
- [ ] **Secure coding assistant:** review owner-provided code for common security weaknesses, explain the risk and suggest defensive fixes; do not generate malware, credential stealers or unauthorized access tooling.
- [ ] **Digital forensics helper:** help the owner examine files, logs and security events that they explicitly select and are authorized to access. Preserve original evidence, record timestamps and hashes where appropriate, and avoid altering source evidence.
- [ ] **Threat and incident alerts:** identify suspicious events from approved JARVIS/device security signals, explain why an alert was raised, and recommend safe containment steps that preserve owner control.
- [ ] **Remediation guidance:** prioritize fixes, explain them in plain Hindi/Hinglish or English, and require owner confirmation before changing settings, permissions, firewall/network configuration or deleting/quarantining files.
- [ ] **Permission and scope gate:** before every assessment, display the target, allowed actions, exclusions and expected impact. Require explicit owner confirmation that the target is owned or authorized; stop if authorization or scope is unclear.
- [ ] **Privacy and audit trail:** keep scans opt-in, minimize collected data, redact secrets and personal information, store reports locally by default, and let the owner review/export/delete reports.
- [ ] **Acceptance tests:** verify that scans cannot start without explicit scope confirmation; unauthorized targets are refused; checks remain non-disruptive; no credentials or private traffic are collected; findings include evidence and uncertainty; remediation requires confirmation; lab exercises remain isolated; permission denial and offline behavior fail safely; and normal JARVIS features continue working.

**Hard boundary:** The Cybersecurity Command Center is for defense, education, auditing and explicitly authorized testing only. It must not include unauthorized computer/network access, password attacks, malware, stealth, persistence, data exfiltration, surveillance or bypassing security controls.

**Relationship to existing priorities:** Priority 5 retains general vision/integration foundations; Priority 7 retains diagnostics and repair handoff; Priority 24 retains JARVIS runtime security anomaly response. Priority 30 adds a dedicated defensive cybersecurity workspace without duplicating those features.

All items remain planned until implemented and relevant tests pass on the target Poco M4 Pro 4G and any explicitly authorized lab environment. Do not modify the separate Even project.


### Priority 31 — JARVIS Digital Threat Inspector
**Initial implementation status:** Basic offline text inspector is code-present; Android build and target-device testing are still required. File/APK inspection and online reputation checks remain planned.
- [x] Add a user-invoked Digital Threat Inspector entry to the Command Center and a voice-command route.
- [x] Allow the owner to paste a suspicious link or code/text snippet into an in-app inspection dialog.
- [x] Perform basic local URL checks for HTTPS, raw-IP hosts, punycode, unusually deep subdomains, link-shortener indicators, URL @ symbols and suspicious message wording.
- [x] Perform basic static text-pattern checks for selected potentially risky code constructs; show matched indicators for manual review.
- [x] Display limitations clearly: pattern checks can produce false positives/negatives and cannot certify safety.
- [ ] Add safe online domain reputation and redirect analysis through a reputable security service only after reviewing its privacy policy, terms, API limits and data-sharing implications. Never visit or open a submitted link automatically.
- [ ] Add user-selected file metadata and static inspection for supported file types; do not execute files or install APKs. APK inspection must remain read-only and explain that static analysis cannot prove an APK safe.
- [ ] Add an optional share-sheet receiver so the owner can explicitly send selected text/links to JARVIS for inspection; do not read private messages or notifications silently.
- [ ] Add clearer risk categories (Info / Caution / High concern) with evidence and confidence limits; never label content malicious solely from a keyword match.
- [ ] Add test cases for ordinary URLs, shortened URLs, misleading hostnames, punycode, IP-based URLs, benign code and suspicious-looking but legitimate code.
- [ ] Preserve owner privacy: process pasted content locally by default, do not save inspection history unless the owner opts in, and never upload message contents, links or code without explicit consent.
- [ ] Never execute pasted code, open suspicious links, download unknown files, install APKs, bypass Android protections, or test systems without authorization.


## Decision addition — 2026-11-20 (Silent Background Listening & Voice Announcements)

This approved usability change addresses repeated spoken acknowledgements during background listening. The initial code is present; build and target-device verification are still required.

### Priority 32 — Silent Background Listening Mode
- [x] Add a user-facing Command Center control for background voice acknowledgement: Silent (default) or Voice acknowledgement on. (Code present — build/device verification pending.)
- [x] In Silent mode, do not speak the repeated “Haan Boss, bolo” acknowledgement each time the wake phrase is detected. Continue listening for the command and speak the actual command response as usual.
- [x] Keep the persistent Android foreground-service notification visible so microphone use remains transparent; silent mode must not hide or disable the microphone-use indicator.
- [x] Store the preference locally in JARVIS app settings and default to Silent for a less irritating experience.
- [x] Add voice commands “silent background listening” and “background voice on” to switch the preference; the change confirmation may be spoken once when explicitly requested.
- [ ] Verify that service start/restart, recognition errors and normal listening cycles produce no unsolicited repeated spoken announcements in Silent mode.
- [ ] Verify that wake-word command handling, command responses, important owner-requested alerts, Stop Listening, notification visibility and microphone permission behavior continue to work.
- [ ] Test both preference states on the target Poco M4 Pro 4G, including screen-off operation, app restart and service restart. Mark verified only after the tests pass.

**Behavior boundary:** Silent mode suppresses only the routine wake acknowledgement. It does not mute responses to commands or disable safety-critical Android notifications. JARVIS must never conceal active microphone use; the foreground notification and Android microphone privacy indicators remain visible as required by the OS.

**Relationship to existing priorities:** Priority 1 retains wake-word and background voice reliability; Priority 6 and Priority 29 retain voice delivery controls. Priority 32 removes repetitive acknowledgement noise without disabling listening or changing JARVIS's command logic. Do not modify the separate Even project.

## Decision addition — 2026-11-20 (JARVIS Next-Generation Intelligence Concepts)

The owner approved the following ten concepts for the JARVIS master blueprint. They are **planned only — not implemented or verified**. These capabilities must extend existing priorities rather than create duplicate memory stores, AI providers, research pipelines, translation services, diagnostics or test frameworks.

### Priority 33 — Next-Generation Intelligence & Physical-World Readiness

- [ ] **AI Council System:** extend Priority 22's multi-AI routing and AI-to-AI collaboration into a bounded council of task-specialist agents (for example research, coding, planning, vision and safety review). Give each agent only the minimum task context it needs; collect independent assessments, compare evidence and disagreements, and have a coordinator produce one concise result with sources, confidence and unresolved questions. Do not treat a majority vote as proof, and never allow an agent to bypass permissions or approval gates.
- [ ] **What If Engine:** add scenario exploration for owner-defined hypothetical changes, goals and constraints. Generate clearly labeled assumptions, plausible outcomes, trade-offs and alternative paths; compare scenarios without claiming certainty or predicting the future as fact. Reuse Priority 22's simulation/orchestration foundation and keep hypothetical results separate from real device actions.
- [ ] **Idea Evolution Tracker:** extend Priority 27's Personal Knowledge Vault and Priority 22's provenance-aware knowledge graph to show how an owner-approved idea changes over time: original note, revisions, linked decisions, experiments, outcomes and current status. Distinguish exact saved records from inferred connections; allow correction, export and deletion, and never invent missing history.
- [ ] **Prototype Testing Intelligence:** extend Priority 22's simulation/pre-release testing engine with project-specific prototype plans, test matrices, expected results, observed results, failure analysis and version comparisons. Start with software and safe virtual models; any physical prototype test must include a reviewed safety plan and must not autonomously operate hazardous equipment. Keep test evidence and simulation results clearly distinguished.
- [ ] **Universal Communication Bridge:** extend Priority 2's translation and Priority 11's communication intelligence with owner-initiated live conversation translation, selectable input/output languages, captions/subtitles and speech output where supported. Clearly show when microphone audio is being processed, request permissions, disclose cloud processing, minimize retention, and provide text/manual fallback. Do not record or translate bystanders or private conversations without appropriate consent.
- [ ] **Decision Intelligence Workspace:** provide a dedicated comparison view for owner-defined choices, criteria, constraints, costs, risks, benefits and uncertainty. Build on Priority 22's independent decision-support engine; show why each option scores as it does, let the owner change weights, and avoid making consequential decisions or taking external actions without explicit approval.
- [ ] **Spatial Intelligence:** extend Priority 5's camera vision/OCR foundations to understand owner-selected images or an explicitly started camera session for supported room layouts, object identification and spatial relationships. Show uncertainty, ask before saving a room map or image, process locally where practical, and never silently activate the camera or infer private details beyond the selected task.
- [ ] **Scientific Discovery Mode:** provide a research workspace for owner-defined scientific or engineering questions. Search and compare credible publications, distinguish established findings from hypotheses, summarize methods and limitations, and suggest safe, ethical, literature-grounded experiments. Cite sources and dates; do not fabricate studies or present an untested hypothesis as a discovery.
- [ ] **Reality Check Engine:** add a verification pass for important factual answers and plans. Check claims against available sources, identify contradictions, stale information, unsupported assumptions and missing evidence, then label each conclusion as supported, uncertain or unverified. Reuse Priority 22's live knowledge/evidence engine; never claim independent verification when no source or check was available.
- [ ] **Modular Expansion & Robotics Interface:** define stable, versioned interfaces for future owner-approved hardware modules, sensors, companion devices and robotics controllers. Require explicit device pairing, authentication, capability discovery, least-privilege permissions, visible connection status, command allowlists, emergency stop and manual override. Begin with read-only telemetry and simulated devices; physical actuation must require risk review and explicit owner authorization. Do not imply that the current Poco app can directly control arbitrary hardware.
**Shared acceptance tests:** verify agent isolation and disagreement handling; hypothetical/real-action separation; idea provenance and deletion; test evidence integrity; microphone consent and offline fallback; decision criteria transparency; camera permission and no background capture; source accuracy and citation freshness; uncertainty labels; hardware authentication, least privilege, simulated-device tests, stop controls and safe failure when a module disconnects.

**Relationship to existing priorities:** Priority 2 retains translation/API foundations; Priority 5 retains vision integrations; Priority 11 retains communication intelligence; Priority 22 retains multi-AI orchestration, decision support, research/evidence and simulation foundations; Priority 27 retains the personal knowledge vault. Priority 33 adds integrated owner-facing experiences on top of those foundations rather than duplicating them.

All ten concepts remain planned until implemented and tested. Keep JARVIS separate from Even; do not modify Even.

## Decision addition — 2026-11-20 (JARVIS Platform Expansion & Engineering Intelligence)

The owner approved all eight concepts below for inclusion in the master blueprint. **Planned only — not implemented or verified.** They extend existing resource management, project workspace, hardware interface, knowledge vault, accessibility, diagnostics and robotics foundations; they must not create duplicate subsystems.

### Priority 34 — Platform Expansion & Engineering Intelligence

- [ ] **Intelligent Resource Manager:** extend Priority 17's battery-conscious scheduling and Priority 22's model routing to choose, for each supported task, between on-device processing, the configured cloud backend or deferral. Consider measured battery state, thermal status where Android exposes it, network availability, latency, data sensitivity, model capability and owner settings. Explain trade-offs, respect data-locality preferences, never upload private content by default, and fall back safely when a provider or connection is unavailable. Do not promise control over Android's protected system resource policies.
- [ ] **Virtual Workspace Generator:** create owner-named, task-specific workspaces that organize approved project files, notes, links, tools and task checklists. Provide templates, search, version/history and explicit import/export controls. Keep workspace data scoped to the selected project, avoid silently moving or deleting files, and require confirmation before destructive or external-sharing actions.
- [ ] **Hardware Compatibility Intelligence:** extend Priority 33's Modular Expansion & Robotics Interface with a compatibility registry for supported phones, computers, sensors, chips, boards and peripherals. Compare documented interfaces, operating voltage/power requirements, drivers, protocols, OS support and known limitations using cited manufacturer or trusted technical documentation. Label unknown or unverified combinations clearly; never infer compatibility from connector shape alone or initiate hardware operation during compatibility checks.
- [ ] **Digital Legacy Archive:** extend Priority 27's Personal Knowledge Vault with an owner-controlled archive for selected project milestones, documents, design decisions, source snapshots and technical discoveries. Support encrypted export, integrity checks, version history, retention controls and restore previews. Exclude secrets and sensitive records by default; clearly distinguish an archive backup from a complete device backup, and test restore before claiming recoverability.
- [ ] **Universal Accessibility Mode:** provide configurable interaction options such as speech input/output, captions, readable typography, scalable controls, high-contrast presentation, simplified navigation and supported switch/gesture alternatives. Respect Android accessibility settings and user preferences; provide a visible manual alternative for every essential gesture or voice-only action. Do not infer a disability or change accessibility settings without the owner's explicit choice.
- [ ] **Performance Prediction Engine:** extend Priority 7 diagnostics and Priority 22's simulation/testing foundations to estimate the likely resource impact of a proposed feature or workflow using available measurements and documented assumptions. Report ranges and confidence, identify missing measurements, compare before/after test results, and distinguish estimates from real benchmarks. Do not claim predictive accuracy without validation on the target Poco M4 Pro 4G and relevant backend.
- [ ] **Engineering Component Library:** provide a searchable, versioned catalog of electronic and mechanical components such as processors, microcontrollers, sensors, motors, batteries, connectors and development boards. Store manufacturer, part number, datasheet/source, electrical/mechanical specifications, interface, operating limits, lifecycle status and compatibility notes. Cite source documents, track datasheet revisions and flag missing or conflicting specifications; do not treat catalog entries as approval to build or operate a physical circuit.
- [ ] **Knowledge Transfer Protocol:** extend Priority 27's vault and Priority 33's modular architecture with a secure, owner-controlled migration process for moving selected JARVIS settings, approved knowledge, project metadata and compatible workspace data to a new phone, computer or future hardware platform. Use authenticated pairing, encrypted transfer, data preview, category-by-category selection, integrity verification, duplicate/conflict handling and rollback where possible. Never transfer API keys, credentials, biometric data or private content by default; explain device-bound Android Keystore limitations and require re-authentication for secrets on the destination device.

**Shared acceptance tests:** compare local/cloud/deferred routing under battery, offline and privacy constraints; verify workspace isolation and destructive-action confirmations; validate hardware facts against cited datasheets and reject unknown compatibility; perform archive integrity and restore drills; test accessibility settings and alternate controls; compare performance estimates with measured benchmarks; detect stale/conflicting component specifications; and test authenticated migration, interrupted transfers, integrity failures, duplicate handling, secret exclusion and destination re-authentication.

**Relationship to existing priorities:** Priority 7 remains the diagnostics foundation; Priority 17 remains battery-conscious scheduling; Priority 22 remains model routing and simulation; Priority 27 remains the personal knowledge vault; Priority 33 remains the modular hardware/robotics interface. Priority 34 adds integrated platform and engineering experiences without duplicating those foundations.

All eight concepts remain planned until implemented and verified. Keep JARVIS separate from Even; do not modify Even.

## Decision addition — 2026-11-20 (Future Simulation, Robotics & Connected Systems)

The owner approved all eight concepts from the latest idea batch for inclusion. **Planned only — not implemented or verified.** These concepts extend the existing simulation, robotics, hardware compatibility, research and orchestration foundations. The numbered idea labels from the discussion are not blueprint priority numbers.

### Priority 35 — Future Simulation, Robotics & Connected Systems

- [ ] **Digital World Simulator:** provide a safe virtual environment for modeling proposed software systems, connected-device layouts and abstract technology concepts before real deployment. Support editable assumptions, repeatable scenarios, visual outputs and comparison of outcomes. Clearly label simulated data, assumptions and limitations; never present a simulation as proof that a real-world system will behave identically.
- [ ] **Robot Coordination Network:** define an authenticated coordination layer for multiple compatible robots or devices, with device identity, capability discovery, task allocation, status reporting, collision/operating-zone safeguards where applicable, emergency stop pathways and loss-of-connection behavior. Begin with simulation and read-only telemetry. Any physical movement or actuation requires hardware-specific safety validation and explicit owner authorization; no uncontrolled swarm behavior.
- [ ] **3D Invention Studio:** support voice- or text-directed concept development into editable 3D models, annotated assemblies and exportable design drafts using compatible tools. Keep dimensions, materials and tolerances marked as draft until reviewed; provide source/version history and distinguish visual concept models from validated engineering designs.
- [ ] **Distributed Intelligence Network:** allow explicitly authorized computers and devices to contribute suitable computing tasks through authenticated, encrypted, least-privilege connections. Include opt-in enrollment, workload limits, resource/battery safeguards, task isolation, audit logs, revocation and secure failure handling. Never use devices without their owner's consent, bypass access controls, or expose private data unnecessarily.
- [ ] **Virtual Experiment Laboratory:** provide educational and research-oriented simulations where users can change documented variables, compare model outputs and record observations. Cite scientific sources, distinguish established facts from hypotheses and simulated results, and include uncertainty and model limitations. Keep experiments virtual unless a separately reviewed, safe and supervised real-world procedure is appropriate.
- [ ] **Global Connectivity Intelligence:** summarize the availability and health of owner-authorized network connections and paired devices, including offline status, latency and service reachability where supported. Show only the minimum network details needed, protect location and network identifiers, and never scan or access networks without authorization.
- [ ] **Robotic Body Architecture:** maintain a future-facing modular reference architecture for a physical JARVIS embodiment, covering compute, power, sensing, mobility, communications, mechanical interfaces, thermal limits, maintainability and safety systems. Require component compatibility evidence, human-reviewed design gates, emergency stop, safe-state behavior and staged simulation/testing before any physical integration. Do not imply that the current phone app can directly operate a robot body.
- [ ] **System Digital Twin:** create a version-linked virtual representation of JARVIS software modules and, in the future, explicitly connected compatible hardware. Compare expected and observed states, visualize dependencies and test proposed configuration changes in a sandbox. Label stale telemetry and simulated states, protect sensitive device information, and never apply changes to live systems without owner approval and a rollback plan.

**Shared acceptance tests:** verify simulation labels and repeatability; authenticate every participating device; demonstrate safe stop and disconnect behavior in simulated robotics tests; validate 3D exports and preserve draft status; enforce workload/resource limits and revocation in distributed tasks; keep network monitoring limited to authorized assets; require documented safety review for physical robotics; and verify digital-twin version matching, stale-data warnings, sandbox isolation and approval before live changes.

**Relationship to existing priorities:** Priority 7 remains the diagnostics foundation; Priority 22 remains the AI orchestration and simulation foundation; Priority 33 remains the modular expansion/robotics interface; Priority 34 remains the hardware compatibility and engineering intelligence foundation. Priority 35 adds future-facing simulation, coordination and physical-system architecture without replacing those foundations.

All eight concepts remain planned until implemented, tested and approved for release. Keep JARVIS separate from Even; do not modify Even.


## Decision addition — 2026-11-20 (Workflow Automation & Orchestration)

The owner requested a dedicated Workflows capability alongside Priorities 0–35. This is planned only — not implemented or verified. Reuse the existing command-chain planner (Priority 4), app-control and communication engines (Priorities 10–11), autonomous orchestration (Priority 12), scheduling/resource management (Priorities 17 and 34), diagnostics (Priority 7) and connected-device interfaces (Priorities 33–35). Do not create duplicate schedulers, memory stores or permission systems.

### Priority 36 — Workflow Automation & Orchestration

- [ ] Workflow Builder: let the owner create, name, edit, duplicate, pause and delete reusable workflows using voice or a visual step-by-step builder. Support simple sequences first and clearly show each step before saving.
- [ ] Multi-Step Workflow Planner: translate a natural-language goal into ordered steps, identify required apps, APIs, permissions, inputs and dependencies, and show a preview for owner approval before first execution.
- [ ] Triggers & Schedules: support owner-configured manual, time-based and supported event-based triggers (for example a chosen time, device connection or reminder event). Explain Android background limits, battery impact and required permissions; provide pause/disable controls.
- [ ] Cross-App Workflow Runner: connect only supported app intents, documented APIs and owner-enabled integrations. Display the active step and result; do not bypass app protections, access private app data without permission or promise unrestricted background control.
- [ ] Conditional Branching: support clear if/then/else conditions based on authorized, available signals; show the condition and resulting path, and handle missing or stale data without guessing.
- [ ] Approval Gates: require fresh, explicit owner confirmation before sensitive or consequential steps such as calls, sending messages, sharing files/location, purchases, changing important settings or controlling physical devices. A previous approval must not silently authorize a later unrelated action.
- [ ] Workflow State & Recovery: save safe execution state so an interrupted workflow can explain where it stopped and offer resume, restart or cancel. Avoid repeating completed external actions; use idempotency or confirmation checks where possible.
- [ ] Failure Handling & Diagnostics: provide step-specific errors, bounded retries for transient failures, timeout handling and safe stop behavior. Never loop indefinitely or silently continue after a critical step fails; offer a diagnostic report through Priority 7.
- [ ] Workflow History & Audit Log: show owner-readable run history with trigger, time, steps, outcomes, approvals and errors. Keep logs privacy-minimized, locally controlled where practical, and provide review/export/delete controls.
- [ ] Workflow Templates & Sharing: provide editable templates for routines such as morning briefing, study session, project build/check, device setup and travel preparation. Export/import workflow definitions with preview; never include credentials or private data, and require approval before enabling imported workflows.
- [ ] Resource & Connectivity Awareness: use Priority 34's resource manager to defer or pause non-urgent work when battery, network or backend availability is unsuitable; keep local/offline steps available where possible and explain any deferred work.
- [ ] Workflow Test Mode: allow a dry run that validates the plan, permissions, inputs and expected actions without sending messages, placing calls, changing external data or actuating hardware. Clearly distinguish simulated results from real execution.

**Shared acceptance tests:** verify preview-before-run; trigger permission and pause controls; correct branch selection; approval gates for every sensitive action; interruption/resume without duplicate external actions; bounded retry and safe-stop behavior; useful step-level diagnostics; log privacy and deletion; imported-template review; low-battery/offline handling; and dry-run isolation from real actions.

**Relationship to existing priorities:** Priority 4 remains the command-chain foundation; Priorities 10–11 remain app and communication action layers; Priority 12 remains the orchestration/initiative layer; Priority 17 and 34 remain scheduling/resource foundations; Priority 7 remains diagnostics; Priorities 33–35 remain connected-device and robotics foundations. Priority 36 coordinates these existing capabilities into owner-controlled workflows without duplicating them.

All Priority 36 items remain planned until implemented, built, tested and verified on the target Poco M4 Pro 4G. Keep JARVIS separate from Even; do not modify Even.

## Decision addition — 2026-11-20 (Local, Global & Hybrid Intelligence and Independent Connectivity)

The owner approved bringing the newly discussed Project ROY technology concepts into the **JARVIS Master Blueprint** as JARVIS capabilities, not as a separate assistant identity. This is a planning decision only: every item below is **planned, not implemented, built, launched or verified**. Reuse existing foundations and avoid duplicating the resource manager (Priority 34), distributed/connected-device network (Priority 35), online research and APIs (Priority 2), orchestration (Priority 22), and workflow engine (Priority 36).

### Priority 37 — Local, Global & Hybrid Intelligence

- [ ] **Local Intelligence Mode:** provide useful offline capabilities through on-device models where feasible, cached public/reference knowledge, local notes and local command handling. Display the knowledge/update date and model limitations; never describe stale cached information as live or current.
- [ ] **Global Intelligence Mode:** connect to authorized online search, public knowledge sources, scientific publications and supported APIs for current information. Provide source links, dates/freshness labels, uncertainty and source-quality checks; distinguish retrieved facts from JARVIS reasoning and never imply access to the entire internet or private systems.
- [ ] **Hybrid Intelligence Mode:** route each task between on-device processing and the configured cloud/online services according to capability, connectivity, latency, cost, battery/resource state, data sensitivity and owner preferences. Keep private content local by default where practical, explain when cloud processing is needed, and offer an offline fallback when possible.
- [ ] **Global Knowledge & Research Engine:** extend the existing web-search, Scientific Discovery Mode and Knowledge Vault foundations to gather, compare and summarize multilingual public information. Preserve citations and publication dates, detect conflicting or stale sources, maintain provenance, and let the owner review what is saved to long-term project knowledge.
- [ ] **Multilingual Global Understanding:** support Hindi/Hinglish and English first, with planned Bengali and additional language workflows through the existing translation foundation. Clearly identify unsupported languages and preserve source meaning rather than claiming perfect translation.
- [ ] **Private Independent Wireless Network:** design a local-only network for owner-authorized devices, enabling device discovery, local messaging/chat, file transfer, a private local server and JARVIS-to-device communication without requiring internet access. Use authenticated pairing, encryption, least-privilege access, visible connection status, revocation and secure defaults. Clearly state that a local network does not itself provide internet access.
- [ ] **Authorized Satellite Connectivity Research:** add a research and compatibility layer for legally available satellite-internet services and approved terminals. Compare provider coverage, service availability, hardware, subscription/usage costs, latency, power needs and applicable Indian licensing/authorization requirements using current official sources. Do not promise free satellite internet or access to retired, inactive or third-party satellites.
- [ ] **Future Satellite & Space Technology Research:** create an educational engineering roadmap for learning about CubeSats, communications payloads, ground stations and lawful satellite-data services. Keep simulations and concept designs clearly marked as drafts; any radio transmission, frequency use, satellite operation, launch or space activity must go through qualified supervision, provider/owner authorization and applicable regulatory approvals. Do not imply that the current Poco phone can directly control or launch a satellite.

**Shared acceptance tests:** verify offline operation with network disabled; confirm online answers include working source links and freshness metadata; test hybrid routing against privacy, battery, latency and cost settings; verify cloud requests disclose the data being sent; test local-device authentication, encryption, pairing revocation and unauthorized-device rejection; confirm no internet is claimed on a local-only network; and validate satellite-service claims against current official provider and regulatory information.

**Relationship to existing priorities:** Priority 2 remains the online API and web-search foundation; Priority 22 remains AI orchestration and evidence-based decision support; Priority 27 remains the owner-controlled knowledge vault; Priority 34 remains resource and hardware compatibility intelligence; Priority 35 remains distributed and connected-device foundations; Priority 36 remains workflow orchestration. Priority 37 combines these foundations into explicit Local, Global and Hybrid intelligence modes and an owner-controlled independent-connectivity roadmap without duplicating them.

All Priority 37 items remain planned until implemented, built, tested and verified on the target Poco M4 Pro 4G. Keep JARVIS separate from Even; do not modify Even.

## Decision addition — 2026-11-22 (New Personal & Practical Intelligence Modules)

The owner approved the following ten additional concepts for inclusion in the JARVIS Master Blueprint. These are **planned blueprint items only**; adding them does not mean they are implemented, built, tested or verified. Reuse existing memory, workflow, knowledge, calendar, diagnostics, research and security foundations wherever possible. Do not create duplicate schedulers, knowledge stores, permission systems or automation engines. Keep JARVIS separate from Even.

### Priority 38 — Personal & Practical Intelligence Suite

- [ ] **Life Management Intelligence:** provide an owner-controlled overview of personal responsibilities, important dates, commitments, goals and pending tasks. Link to existing reminders, calendar handoff, workflows and memory rather than creating duplicate stores. Let the owner choose what is recorded, review/edit/delete entries, and keep private information out of notifications and shared views by default.
- [ ] **Personal Education & Skill Academy:** create structured learning paths for owner-selected subjects and skills, from beginner to advanced. Include lessons, practice activities, progress tracking, revision plans and project-based learning. Cite reliable learning sources where applicable, distinguish generated explanations from authoritative course material, and adapt difficulty using owner feedback without making unsupported claims about mastery.
- [ ] **Smart Asset & Inventory Intelligence:** maintain an owner-managed catalog of selected household items, electronics, tools and equipment, including model/serial details when voluntarily entered, purchase date, warranty, receipts, manuals and maintenance history. Support reminders through existing scheduling foundations, encrypted/private storage, export and deletion; do not silently scan receipts, documents or surroundings.
- [ ] **Disaster Preparedness Intelligence:** provide preparedness checklists and practical planning for hazards such as floods, cyclones, earthquakes and severe weather. Prioritize current official local-authority alerts and emergency guidance, show source and timestamp, support an offline emergency-information pack, and avoid presenting predictions or unverified reports as confirmed warnings.
- [ ] **Vehicle Intelligence System:** track owner-entered vehicle details, service history, maintenance schedules, fuel/expense records, insurance and document renewal dates. Allow reminders through existing scheduling and workflow systems; protect registration and identity details, and do not claim to diagnose mechanical faults without reliable sensor data or qualified inspection.
- [ ] **Collaborative Intelligence Network:** let the owner invite explicitly authorized people to selected projects, tasks and knowledge spaces with role-based access, clear sharing previews, revocation, audit history and conflict handling. Keep personal memory and private records separate by default; never expose another person's data or grant access merely because a device is on the same network.
- [ ] **Environmental Intelligence:** present sourced environmental information such as air quality, pollution, UV index and relevant environmental alerts using supported public data providers. Display location scope, units, timestamp, source and uncertainty; request location only when needed and offer manual location selection or a privacy-preserving alternative.
- [ ] **Legal Document Intelligence:** help the owner organize and understand user-selected contracts, agreements and official documents; extract key dates, parties, obligations and clauses, and identify questions for a qualified legal professional. Preserve document privacy, show the exact source passage for extracted points, distinguish plain-language explanation from legal advice, and never silently upload documents to external services.
- [ ] **Intelligent Journey Planner:** plan multi-stop journeys using authorized mapping/transit/travel data, route comparisons, estimated duration and costs, document checklists and owner-configured travel reminders. Show data freshness and assumptions, request location only when needed, and require approval before booking, purchasing, sharing location or changing an external reservation.
- [ ] **Personal Experience Timeline:** create an owner-curated, searchable timeline of selected milestones, achievements, projects and life events. Reuse the Personal Knowledge Vault and Digital Time Machine foundations; make every entry reviewable, editable, exportable and deletable, with opt-in media attachment and strict privacy controls.

**Shared acceptance tests:** verify owner consent and data minimization; review/edit/delete/export for stored records; no duplicate reminder, calendar or memory systems; source and timestamp display for live alerts and environmental/travel data; offline access to explicitly saved emergency information; role-based collaboration and immediate revocation; document extraction traceability and privacy; approval before external bookings or sharing; and clear separation between informational support and professional diagnosis/advice.

**Relationship to existing priorities:** Priority 4 remains the reminders/calendar/memory foundation; Priority 7 remains diagnostics; Priority 17 remains scheduled research; Priority 22 remains knowledge and decision support; Priority 27 remains the Personal Knowledge Vault and life-event recall; Priority 34 remains the workspace/resource foundation; Priority 36 remains workflow orchestration; Priority 37 remains global research and connectivity. Priority 38 adds owner-facing personal and practical modules by reusing those foundations rather than duplicating them.

All Priority 38 items remain planned until implemented, built, tested and verified on the target Poco M4 Pro 4G. Keep JARVIS separate from Even; do not modify Even.


## Implementation status addendum — M05 Public Knowledge Lookups (Batch B05; 2026-11-22)
- **Country Information:** code added; owner-entered country name or ISO code; uses countries.dev; CI/device verification pending.
- **India PIN Code Lookup:** code added; six-digit PIN returns multiple post offices and branch/delivery/district/state details; uses the community-maintained PostalPincode.in API; informational only; CI/device verification pending.
- **English Dictionary:** code added; owner-entered English word returns available phonetics, definitions and examples from dictionaryapi.dev; CI/device verification pending.
- All three require explicit confirmation before each HTTPS lookup and send only the selected input. No personal memory or contacts are attached. No API key or new Android permission.


**B05 verification update (2026-11-22):** final head `76877d030adcf36b187bf01e263ce52051b10661` passed GitHub Actions run #563 (`45045200384`): backend syntax validation, Gradle debug APK build and APK artifact upload. Artifact `14763530517`, SHA-256 `b2929bd0d13c164ba79c2263374a3e2749bc71e3ee2abbdc3f6e1196cbec5457`. No separate unit-test/lint task ran; target-device verification remains pending. Draft PR #27 is open and unmerged.


## Implementation status addendum — M06 Live Information Studio (Batch B06; 2026-11-22)
- **City Weather:** code added; manual city search, user chooses among geocoding matches, then current weather data; no GPS permission; CI/device verification pending.
- **Currency Converter:** code added; owner-entered amount and ISO currency codes use Frankfurter reference rates; rates are not bank/trading quotes; CI/device verification pending.
- **India Public Holidays:** code added; year-based holiday lookup through Nager.Date v4; provider coverage may omit state/district/local holidays; CI/device verification pending.
- Each feature requires confirmation and sends only the entered query. No new Android permission or API key.


**B06 verification update (2026-11-22):** code/documentation head `7b2c1c1e88fc299d36af3e198a29449e1f0146c0` passed GitHub Actions run #572 (`45045389085`): backend syntax validation, Gradle debug APK build and APK artifact upload. Artifact `14763293374`, SHA-256 `6648c8850934b605c00b918a49b579eb4ab317ab45a9d39ca31d62b954391e28`. No separate unit-test/lint task ran; target-device verification remains pending. Draft PR #28 is open and unmerged.


## Implementation status addendum — M10 AI Learning Academy (Batch B07; 2026-11-22)
- **Coding Tutor:** code added; four language choices, three difficulty levels, structured lesson and practice output; CI/device verification pending.
- **Science Explorer:** code added; four science subject groups, three difficulty levels, conceptual explanation and review questions; no hazardous experiment instructions; CI/device verification pending.
- **Technology Learning Roadmap:** code added; staged plans for Android, web, AI basics, defensive digital safety and computer-science foundations; CI/device verification pending.
- All modes use the memory-isolated AI request path and ask before submission; no new permissions or persistent learner history.


**B07 verification update (2026-11-22):** code/documentation head `a9cd77178709102a9bc746840f9c6e44b0f496b8` passed GitHub Actions run #581 (`45045554373`): backend syntax validation, Gradle debug APK build and APK artifact upload. Artifact `14763640351`, SHA-256 `648188031f44642066caf26da29fe54d3a14dec55c84095f7468ed01a2672ccf`. No separate unit-test/lint task ran; target-device verification remains pending. Draft PR #29 is open and unmerged.


## Implementation status addendum — M11 Software Development Studio (Batch B08; 2026-11-22)
- **Project Architect:** code added; owner-selected platform and project idea produce a structured plan only; CI/device verification pending.
- **Bug & Error Explainer:** code added; analyzes owner-pasted error text and optional excerpt, with secret-redaction reminder and bounded input; CI/device verification pending.
- **Code Quality Review:** code added; reviews owner-pasted snippets for correctness, maintainability, performance and defensive security; no code execution or automatic edits; CI/device verification pending.
- All three use memory-isolated AI requests and explicit confirmation. No new permissions or persistent code storage.


**B08 verification update (2026-11-22):** corrected code head `b922a78b5e9c2956c7f4237331633939cb701861` passed GitHub Actions run #591 (`45045770900`): backend syntax validation, Gradle debug APK build and APK artifact upload. Artifact `14763955311`, SHA-256 `84ef58a3e5e25fa086c270be6e4a8002019ec8143f903ce088a35a97b1f209fa`. Initial runs #586–#590 failed on two extra closing parentheses in nested dialog callbacks; corrected in the passing head. No separate unit-test/lint task ran; target-device verification remains pending. Draft PR #30 is open and unmerged.


## Implementation status addendum — M12 Data Intelligence Center (Batch B09; 2026-11-22)
- **Dataset Profiler:** code added; local CSV row/column profile and numeric summaries with bounded input; CI/device verification pending.
- **Data Quality Inspector:** code added; local missing-cell, row-width, duplicate-row and header checks; structural checks only; CI/device verification pending.
- **Aggregate AI Insight Report:** code added; sends anonymized aggregate metrics only after preview and confirmation; raw CSV rows are not sent; CI/device verification pending.
- CSV is not saved; no new permissions or persistent dataset store.


**B09 verification update (2026-11-22):** code head `0b2a94bdbf88c5cd74c1f71cda62057096f0e16a` passed GitHub Actions run #602 (`45046009488`): backend syntax validation, Gradle debug APK build and APK artifact upload. Artifact `14763571783`, SHA-256 `213c503c4a5cc0ad81435fe83a813f7a0a594ed734cdb3a0704d43e72fbd3a8c`. No separate unit-test/lint task ran; target-device verification remains pending. Draft PR #31 is open and unmerged.


## Implementation status addendum — M13 Personal Productivity Studio (Batch B10; 2026-11-22)
- **Project Workspace:** code added; groups existing structured tasks by owner-assigned project label; no duplicate task store; CI/device verification pending.
- **Priority Matrix:** code added; four priority labels saved on existing task objects; CI/device verification pending.
- **Weekly Review:** code added; counts tasks created in the last seven days and their current status; does not infer completion dates; CI/device verification pending.
- Reuses `structured_tasks_v1`; no new permissions or background scheduling.
