# JARVIS Unified Batch — Implementation Tracker

Target: Poco M4 Pro 4G (Android 13 device profile), Java/Android app.
This integration branch is the single landing area for the 12-capability batch.
JARVIS is separate from EVEN. Preserve the current home screen and existing Command Center layout.

## Delivery rule
Do not publish intermediate APKs. Build and publish one final APK only after all safe integration work, tests, lint, and APK verification. CI success does not replace testing on the actual Poco device.

## Integration audit — 2026-11-20
The current `main` branch is behind/diverged from several feature branches. Do not merge these drafts blindly:
- `feature/voice-engine-controls`: 2 commits ahead of main; selectable installed TTS voice, rate and pitch controls in MainActivity and WakeWordService. PR #15 is draft and has not been build/device verified.
- `feature/personal-media-vault-restore`: diverged (6 ahead / 18 behind); adds MediaVaultActivity and Command Center entry. PR #11 is draft; app-private storage is not yet encrypted/checksummed and must not be described as a complete independent backup.
- `fix/background-voice-reliability`: diverged (1 ahead / 43 behind); wake-word confirmation/restart reliability. PR #6 is draft.
- `repair/jarvis-feature-preservation-audit`: diverged (33 ahead / 41 behind); contains FeatureKit, offline command classifier/tests, workflow changes and multiple feature repairs. PR #7 is draft; its own description reports CI success on its branch only.
- `fix/jarvis-device-diagnostics-backup`: diverged (16 ahead / 41 behind); includes diagnostics, safer preference backup and voice reliability changes. PR #8 is draft.
- `feature/jarvis-owner-login`: diverged (12 ahead / 45 behind); owner-login groundwork and setup documentation; not proof that secure Owner/Guest Mode is complete.
- `feature/jarvis-18-feature-implementation`: diverged (7 ahead / 41 behind); FeatureKit groundwork.

## Requested capabilities and honest status
1. Natural voice engine: **coded and CI build-verified**; Command Center settings save language, voice preference, speed and pitch, and foreground/background TTS apply the shared profile. Female/male selection is best-effort based on installed voice names; actual engine availability and device behavior remain unverified.
2. Owner / Guest Mode: **local Owner PIN / Guest session integrated**; app starts in Guest, owner PIN uses PBKDF2, private notes, tasks, SOS/call controls, call/SMS announcements and Media Vault are owner-gated, and AI conversation memory is separated. This is an app-level session gate, not device-level encryption; complete on-device privacy testing remains.
3. Reliable background listening: **duplicate service starts and queued recognition restarts now guarded in code**, while the existing visible foreground notification and Stop action remain. Android recognizer / MIUI battery behavior still requires device testing; it is not guaranteed always-on hotword detection.
4. Human Intelligence Engine: **partial implementation**; deterministic missing-target clarification is integrated before command routing, and the existing backend transcript-correction path remains text-only. Full context reasoning, consent controls and expanded tests remain.
5. Universal App Knowledge & Control: **partial implementation**; known Android intents remain, and dynamic installed-app discovery/launch is added with explicit user confirmation. This does not control arbitrary apps or bypass their security.
6. Communication Engine: **partial implementation**; owner-gated call confirmation and call/SMS announcements remain, and a new SMS composer opens the default messaging app with a draft only—JARVIS never auto-sends it. Contact resolution, broader messaging workflows and device tests remain.
7. Autonomous Engine: **partial implementation**; local user-scheduled reminders use AlarmManager and a notification receiver. Recurring routines, cancellation UI and an audit log remain.
8. Creator Engine: **partial implementation**; Creator Studio asks the AI backend for caption, short-video script, story and bio drafts; saving is explicit and owner-gated. No auto-posting or auto-sharing.
9. Security Engine: **partial implementation**; read-only permission review and a static link/code inspector exist, owner/guest privacy gates were expanded, and backend secrets remain server-side. A full threat model, encrypted storage and audit tests remain.
10. Scheduled Learning: **limited initial implementation**; users can schedule a local topic-review reminder. Automated approved-source refresh/indexing, provenance, pause/disable and learning history are not yet implemented.
11. Auto Repair Kit: **partial implementation**; read-only diagnostics cover microphone permission, speech recognition, TTS, notifications and accessibility. An owner-approved voice-profile repair backs up current settings and supports rollback; broader repair/rollback remains unimplemented.
12. Personal Media Vault: **partial implementation ported**; owner-only selected media copies stay in private app storage, with import/export/restore and SHA-256 integrity checks before restore/export. Files are not encrypted and local storage is not an independent cloud backup; device and recovery tests remain.

## Work sequence
### Priority sprint A — requested items 1–4
- [x] **1. Natural voice engine — initial implementation:** shared voice profile and Command Center controls integrated; CI build succeeded. Remaining: actual installed voice list/availability and device testing.
- [x] **2. Owner / Guest Mode — initial implementation:** local salted PBKDF2 PIN, Guest-by-default app launch, explicit Owner unlock / Guest switch, owner-only notes guard, and separate owner/guest AI conversation stores. Remaining: full private-data surface audit, rate limiting, session timeout, and on-device tests.
- [x] **3. Reliable background listening — initial code hardening:** duplicate service starts and queued restart callbacks are guarded; visible notification/Stop action and microphone permission checks remain. Remaining: pause/resume UX, error/status visibility and Poco/MIUI device tests.
- [x] **4. Human Intelligence Engine — initial implementation:** deterministic clarification for incomplete open/call/message/reminder/search requests is wired into local command routing. Broader context and memory-consent tests remain.

### Priority sprint B — remaining items 5–12
- [x] **5. Universal App Control — initial implementation:** dynamic app discovery and explicit launch confirmation added; broader per-app actions remain platform-limited.
- [x] **7. Autonomous Engine — initial implementation:** local reminder scheduling and notification receiver added; recurring/cancel/audit functions remain.
- [x] **8. Creator Engine — initial implementation:** AI-backed draft generation and explicit local save added.
- [x] **11. Auto Repair Kit — initial repair implementation:** diagnostics, owner-approved voice-profile repair with backup, and rollback added; other repair categories remain manual.
- [x] **12. Personal Media Vault — initial implementation:** owner-gated selected-file backup/import/export/restore plus SHA-256 sidecar integrity checks added; encryption and independent backup verification remain.
- [x] **6. Communication Engine — initial integration:** owner-gated calls and announcements plus SMS draft handoff to the user's messaging app; sending remains a user action. Device and contact-flow tests remain.
- [ ] Implement automated approved-source Scheduled Learning with provenance, pause/disable and limits.
- [ ] Add safe repair/rollback workflow and complete privacy/security review.
- [ ] Run lint, unit tests, latest CI build, APK size/existence checks and target-device testing.
- [ ] Update the master blueprint with implemented / partial / platform-dependent / device-test-needed status.
- [ ] Run unit tests, lint, build, APK size/existence checks; upload one final APK artifact.
- [ ] Perform target-device tests on Poco M4 Pro 4G before calling device-dependent capabilities verified.

## Safety and privacy invariants
- Never embed provider API keys or secrets in the APK or Git history.
- Never bypass Android permissions, app security, authentication, or accessibility safeguards.
- Calls, messages, payments, deletion, sharing, and system-changing actions require clear owner confirmation.
- Guest mode must not expose owner notes, memory, account data, private notifications, or media.
- Media Vault must distinguish a local app-private copy from a verified independent backup. Never imply deleted originals can be recovered without a separate copy.
- Auto-repair must back up before changes, explain the change, request owner approval, and provide rollback where possible.
- Scheduled learning may fetch and index approved information but must not modify its own executable code or permissions.
