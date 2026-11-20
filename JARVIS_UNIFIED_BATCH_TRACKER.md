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
1. Natural voice engine (male/female where reliably identified; installed voice list, language, rate, pitch; foreground/background parity): **partial branch implementation; integrate and test**.
2. Owner / Guest Mode (authenticated owner, private-data isolation, owner-only actions): **planned / branch groundwork; security review required**.
3. Reliable background listening (wake lifecycle, screen-off, duplicate restart prevention, visible stop/pause, MIUI limits): **partial branch implementation; device test required**.
4. Human Intelligence Engine (context, intent, clarification, controlled memory, honest capability reporting): **planned; define testable interfaces**.
5. Universal App Knowledge & Control (supported Android intents/integrations, explicit permissions and confirmations; no security bypass): **planned; platform-limited**.
6. Communication Engine (contact/call/message drafts and explicit confirmation; never silently send/place calls): **partial foundation; expand and test**.
7. Autonomous Engine (owner-approved routines, schedule, cancel, audit log and permissions): **planned; bounded execution only**.
8. Creator Engine (user-directed creation, preview, explicit save/share): **planned**.
9. Security Engine (permission review, secure local storage, secret hygiene, privacy controls): **partial foundation; threat/privacy audit required**.
10. Scheduled Learning (owner-configured refresh, source provenance, limits, pause/disable; no self-modifying code): **planned**.
11. Auto Repair Kit (diagnostics, backup, owner-approved repair, rollback): **partial diagnostics foundation; safe repair/rollback not complete**.
12. Personal Media Vault (SAF import/export/restore, integrity and independent backup verification): **partial branch implementation; encryption/integrity/device tests required**.

## Work sequence
### Priority sprint A — requested items 1–4
- [ ] **1. Natural voice engine:** reconcile the voice branch; provide installed-voice selection (male/female only when available), language, rate and pitch controls; keep foreground/background speech settings consistent; handle missing voices gracefully.
- [ ] **2. Owner / Guest Mode:** implement an authenticated owner session, explicit guest entry/exit, private-data isolation, and owner confirmation for sensitive actions. Do not expose owner data to guests.
- [ ] **3. Reliable background listening:** reconcile wake-service changes; prevent duplicate listeners/restarts, provide visible pause/stop controls and clear status, handle microphone permission and Android/MIUI battery limits honestly.
- [ ] **4. Human Intelligence Engine:** implement testable intent/context/clarification interfaces, controlled opt-in memory, and truthful capability/error reporting; never pretend to understand an ambiguous command.

### Priority sprint B — remaining items 5–12
- [ ] Reconcile branch diffs against current main; port changes file-by-file, resolving conflicts intentionally.
- [ ] Add modular interfaces for app actions, communications, autonomy and creator workflows.
- [ ] Add security controls, scheduled-learning policy, diagnostics/repair rollback, and vault integrity safeguards.
- [ ] Add security controls, scheduled-learning policy, diagnostics/repair rollback, and vault integrity safeguards.
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
