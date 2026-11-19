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