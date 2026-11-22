# JARVIS Implementation Tracker

## Project boundaries
- This repository is the separate JARVIS assistant project.
- Do not modify the user's separate Even assistant project.
- Preserve Android permission boundaries, user-visible controls, and confirmation for sensitive external actions.
- Blueprint inclusion, code presence, automated validation, and physical-device verification are separate statuses.

## Current working branch
- Branch: `feature/m09-memory-expansion-3pack` (stacked on `feature/m14-diagnostics-3pack`; JARVIS only)
- Current draft PR: https://github.com/sd2176845-glitch/jarvis-ai-assistant/pull/24
- Parent batch PR: https://github.com/sd2176845-glitch/jarvis-ai-assistant/pull/23
- `main` has not been changed or merged by this work.

## Latest implementation batches
1. **M02 — Voice Session Feedback & Foreground Stop Control** — code-present: clear listening, hearing, processing, thinking, speaking, offline/local-command-ready, and standby states; owner-visible control stops foreground speech recognition and TTS output. It does not stop the separate background wake-word service.
2. **Voice Studio** — code-present: installed Android TTS voice selection (where available), speech-rate presets, pitch presets, and saved preferences. Device voice availability varies.
2. **Focus & Productivity Session** — code-present: owner-started 15/25/45-minute focus countdown, task label, visible timer, and Stop control. It does not block apps or notifications and only runs while the JARVIS process remains alive.
4. **M17 — Device Resource Intelligence** — code-present: read-only battery, app-data-volume storage, RAM availability, device model, and Android version overview. No new permissions; no automatic cleanup or upload.

## Validation status
- Latest feature commit: `1881e6f349f1dde673ce0d551da210e69e4c2f02` (M02 Voice Session Feedback & Foreground Stop Control).
- Previous commit `aab57e6bcd0df5844f0aae2ba98e60c8f0c5c239` passed GitHub Actions run #453 (`45038456991`). The M02 voice-session commit passed GitHub Actions run #460 (`45038787391`): backend JavaScript checks, Gradle debug APK build, and APK artifact upload all completed successfully.
- Verified CI steps: backend JavaScript syntax checks, Java/Gradle setup, debug APK build, and APK artifact upload.
- Previous successful commit artifact: `jarvis-debug-apk`, artifact ID `14759949342`, SHA-256 `a5fbcf01cbdcec9c4796d65765492867b9466add403eba2464206ddc1061c15c`. The M02 artifact for commit `1881e6f` is artifact ID `14760812400`, SHA-256 `b2c4bbb5eec1e44b5cf073269dbe2afb3f21c33440c914d6789f8adec26a7132`.
- This workflow run did not show separate Android unit-test or lint steps; do not claim those checks passed.
- Poco M4 Pro 4G physical-device testing remains pending. Do not mark Voice Studio, Focus Session, or Device Resource Intelligence as device-verified.
- Do not claim an APK is ready for release; this is a successful CI debug artifact only.

## Resume procedure
1. Read this tracker and `JARVIS_MASTER_BLUEPRINT_CONSOLIDATED.md`.
2. Inspect the current branch head and open PR before editing.
3. Check whether a requested capability already exists; do not duplicate existing features.
4. Implement three distinct, independently tracked sub-features per batch; keep their code paths and acceptance checks separate, then run a combined build/regression check. If a feature fails, record its result independently.
5. Keep all changes on the feature branch until the user explicitly authorizes merging.

## Next technical steps
- CI build/artifact for commit `1881e6f` is verified successful (run `45038787391`, artifact `14760812400`). Install this exact artifact; do not use the older artifact as if it contained M02.
- Install and test the current-head debug APK on the target Poco M4 Pro 4G.
- Test Voice Studio: available voice listing, voice selection, saved voice after restart, speech-rate and pitch behavior in foreground and background.
- Test Focus Session: 15/25/45-minute choices, task label, countdown, Stop, and process-lifecycle limitation.
- Test Device Resource Intelligence: battery values against Android Settings, app-data-volume storage label, RAM values, refresh and error handling.
- Record physical-device results here before marking any of these modules verified.
- After the voice-session CI and device checks, choose the next unimplemented module from the consolidated blueprint; implement it as a separate batch.

## Third feature implementation note
- Device Resource Dashboard uses Android `BatteryManager`, `StatFs`, and `ActivityManager.MemoryInfo` APIs.
- Storage figure is for the app data volume; it may not equal all user-visible shared storage on every device.
- Network status is intentionally omitted because the current manifest does not declare `ACCESS_NETWORK_STATE`; no new permission was added.
- No cleanup, process killing, setting changes, or report upload is performed.
- **Validation:** current-head CI build and artifact upload passed; target-device checks remain pending.


## M02 — Voice Session Feedback & Foreground Stop Control
- **Code-present:** `MainActivity` now reports speech-recognition states (listening, hearing, processing, no-speech/input-ended), AI thinking/connection, TTS speaking/completion/error, and AI-backend-offline/local-command-ready state.
- Added a visible **Stop Foreground Voice** control. It cancels the foreground `SpeechRecognizer` session and stops JARVIS TTS output.
- The control deliberately does not stop the independent background `WakeWordService`; that service retains its separate existing Command Center stop action.
- No new Android permission or external data collection was added.
- **Validation:** CI for commit `1881e6f349f1dde673ce0d551da210e69e4c2f02` passed (run `45038787391`; artifact `14760812400`). Target-device testing is pending.


## M07 — Structured Task Manager
- **Code-present:** upgraded the existing Tasks & reminders hub; no second notes store or duplicate scheduler was created.
- Added a separate structured task list in the existing private SharedPreferences store (`structured_tasks_v1`), with add, completion toggle/reopen, and delete actions.
- Existing legacy notes (`jarvis_notes` / `notes`) are displayed separately and are not migrated, overwritten, or deleted by this feature.
- Alarm and calendar actions remain Android Clock/Calendar handoffs with user confirmation; M07 does not add background scheduling or external sync.
- **Validation:** post-fix GitHub Actions runs #475–#478 passed backend syntax checks, Gradle debug APK build and artifact upload. Latest PR run #478 (run ID `45039428363`, artifact `14760933049`) includes M07. Poco M4 Pro 4G device verification remains pending; M02 and M07 are to be tested on the eventual final APK.


### M07 CI correction note
- The first M07 workflow run (#470, run ID `45039317458`) failed at Java compilation because the import line contained literal escaped newline characters. The line was corrected in commit `e5474c7476633dc9de490e4bd9e03b74550820f3`.
- Post-fix CI was subsequently confirmed by runs #475–#478. The latest successful PR artifact is `14760933049` from run #478. Target-device testing remains pending.


## M08 — Workflow Automation & Orchestration (safe manual foundation)
- **Code-present:** added a Workflow Studio entry to the existing Command Center.
- Owners can create a named checklist with up to 12 steps, open it, tick/untick steps, save progress, reset it, and delete it.
- Data is stored locally in one dedicated `manual_workflows_v1` preference key. It does not duplicate the existing task list or notes.
- **Safety boundary:** this first increment is a manual checklist only. It has no background/time/event triggers, no autonomous execution, no cross-app actions, no calls/messages, and no device-setting changes. The user performs each step and records progress.
- **Validation:** commit `a65f9ba644d2462c63dc7794e137a7046689ee21` passed GitHub Actions #479 (push) and #480 (pull request). Run #480 built the debug APK and uploaded artifact `14761207706` (SHA-256 `ffc681e64f7f077b32e286321a1069347aa51bd2379c0775762687fbc69a93b1`). CI build verified; Poco M4 Pro 4G device verification pending.
- Keep all changes on `blueprint/advanced-feature-integration`; do not merge to `main`. Final APK download/install remains deferred until the planned feature set is complete.


## M09 — Personal Memory, Knowledge Vault & Timeline (owner-controlled foundation)
- **Code-present:** added a Personal Memory & Knowledge Vault entry to the existing Command Center.
- The vault reviews the existing local notes (notes / jarvis_notes) and recent AI conversation context (jarvis_ai_memory); it does not create a second memory database or silently collect new data.
- Owners can edit/correct the existing notes, export the displayed notes and recent conversation through the Android share chooser, delete notes with confirmation, or clear recent conversation context separately. Tasks and workflow checklists are not changed.
- **Scope boundary:** this is the review/edit/export/delete foundation. At the time of this original foundation entry, the timeline, knowledge graph, versioned idea history and encrypted archive were not yet implemented; subsequent batch records below track their implementation.
- **Validation:** source commit 7eef4d7a66f4cdc2c436997fa5d81cbe69a5b441; current-head GitHub Actions build and artifact validation pending. Poco M4 Pro 4G device verification pending.


## M09 — Owner-Created Personal Timeline (2026-11-22)
- **Code-present:** extended the existing Personal Memory Vault with a manually curated timeline. The owner can add a milestone/event title and optional details; JARVIS records the creation timestamp, lists entries, opens entry details, and deletes an individually selected entry after confirmation.
- Timeline data uses the existing jarvis_preferences SharedPreferences store (memory_timeline_v1); it does not create a second memory database or modify notes, tasks, or recent conversation context.
- Entries are never captured automatically. Export now includes the owner-created timeline alongside existing notes and recent conversation context.
- **Implementation commit:** 5296fabe1d3670201dffeb72801bfc219f4f86c8.
- **CI:** GitHub Actions run #495 (45040755446) passed backend syntax validation, Gradle debug APK build, and artifact upload. Artifact 14761891383; SHA-256 93c16f0cbfe5dfca2da02e855a13c43672d9b5904225cda413d102e2feb2e3f2.
- **Not verified:** Android unit tests/lint were not separate steps in this workflow; Poco M4 Pro 4G physical-device testing remains pending. Do not mark M09 or the earlier features device-verified.
- **Next gate:** test timeline add/list/detail/delete/export on the Poco, including empty/corrupt data handling. Then choose the next separate unimplemented sub-feature from the consolidated blueprint. Keep all work on this feature branch; do not merge without owner approval.


## Batch B01 — M14 Diagnostics Studio (three independent sub-features; 2026-11-22)

**Branch:** `feature/m14-diagnostics-3pack`, created from PR #21 head `b209bb0a81f84aaf70d9294771bba2e669f4f22f`. PR #21 remains open and unchanged; this batch has not been merged.

### Feature 1 — App Health Check
- **Code-present:** upgraded the existing Command Center's **System safety check** entry to open Diagnostics Studio. It checks speech-recognition service availability, TTS initialization, INTERNET permission declaration, app package/version readability, and reports optional microphone/contacts/call/notification permission states.
- This extends the existing safety-check entry; it does not add a duplicate diagnostics menu or repeat M17 battery/storage/RAM reporting.
- **Acceptance checks:** run with microphone granted/denied; verify optional permission states; verify unavailable speech/TTS handling; confirm no permission prompt or settings change is triggered.

### Feature 2 — Sanitized Diagnostic Report Export
- **Code-present:** the owner can explicitly open Android's share chooser for the current app-health report.
- The report excludes contacts, messages, call data, conversation content and raw logs. No network upload or automatic sharing occurs.
- **Acceptance checks:** inspect report contents; verify share chooser appears only after owner action; cancel chooser and confirm nothing is sent.

### Feature 3 — Local Diagnostic History
- **Code-present:** owner may save a diagnostic summary locally, view the last 10 timestamp/pass-count summaries, or clear the history after confirmation. Only timestamp and check counts are retained; report text and personal content are not stored.
- Uses the existing private `jarvis_preferences` store under `m14_diagnostic_history_v1`; no new database or permission.
- **Acceptance checks:** save/view multiple summaries; confirm maximum 10 entries; clear with confirmation; verify malformed history is preserved and reported rather than silently overwritten.

### Batch validation and safety status
- **Source commits:** `cc8e73d3a6b8b8c65d9ee7639f2893e8e94dedbc` (DiagnosticsStudio), `21c7b04de1e6b48907ffb95925feff36c7a7b1e1` (existing safety-check integration), and `7c86b2f73fffab58a782bb8c11473c213f6db9dd` (history navigation correction).
- **CI/build:** passed on the current batch head `22efe6293bd5953c5af34d9dab4f806185fb3f8c`. GitHub Actions run #528 (`45043208526`) completed backend syntax validation, Gradle debug APK build, and APK artifact upload successfully. Artifact ID: `14762108881` (`jarvis-debug-apk`). A second push run #527 (`45043204131`) also completed successfully with artifact `14762720811`.
- **Physical device:** pending on Poco M4 Pro 4G. Do not mark any of the three sub-features device-verified.
- **Test scope caveat:** workflow did not run separate Android unit-test or lint steps; those checks are not claimed.
- **Permissions:** no new manifest permission; no hidden monitoring, log collection, private-content access, settings changes, or background work.
- **Regression:** re-test existing System safety check entry, voice session, M17 resource dashboard, Command Center navigation and app startup.
- **Merge policy:** keep the batch on its feature branch; do not merge into PR #21 or `main` without owner approval.


## Batch B02 — M09 Memory Expansion (three independent sub-features; 2026-11-22)

**Branch:** `feature/m09-memory-expansion-3pack`, based on the latest M14 batch branch head `66365fb4ef54db8cf6f0ec6c2c72e1f003ca8f09`. This is a stacked feature branch; no merge to parent PR or `main`.

### Feature 1 — Owner-Curated Knowledge Graph
- **Code-present:** added Knowledge Graph entry inside the existing Personal Memory Vault. Owner can create named nodes, add typed relationships between two existing nodes, review graph items and delete selected items. Deleting a node also removes its linked relationships.
- Uses one new key in the existing private JARVIS preferences store (`memory_graph_v1`); no separate database, automatic inference, contact scraping or background capture.
- **Acceptance checks:** add two nodes; connect them; review nodes/edge; delete a node and verify linked edges disappear; verify malformed JSON is not overwritten.

### Feature 2 — Idea Evolution / Versioned Notes
- **Code-present:** owner can explicitly snapshot the current saved notes, review up to 20 versions, preview a selected version and restore it to the existing notes keys after confirmation.
- Uses the existing private preferences store (`idea_versions_v1`). No automatic snapshots or parallel current-notes store.
- **Acceptance checks:** save multiple versions; verify timestamps/content; confirm cap of 20; restore a selected version; cancel restore; verify corrupt history is preserved and reported.

### Feature 3 — Encrypted Digital Legacy Archive
- **Code-present:** owner can create/replace an encrypted local snapshot of saved notes, owner-created timeline, knowledge graph and idea-version history. Uses AES-GCM with an Android Keystore device-bound key and an app-private file. Includes authenticated decrypt/verification preview and confirmed archive deletion.
- Recent conversation text, contacts, messages and call data are excluded. No new Android permission, cloud upload, background collection or automatic backup.
- **Portability limitation:** the key is device-bound. This is an on-device encrypted archive, not a portable password-protected export; loss of app data/Keystore key can make the archive unrecoverable.
- **Acceptance checks:** create archive; verify authentication/decryption; replace existing archive and verify fallback behavior; corrupt/tamper test must fail closed; delete archive and confirm source notes/timeline/graph/versions remain intact.

### Batch validation and safety status
- **Implementation files:** `MemoryExpansionStudio.java` (new) and existing `MainActivity.java` (one entry added inside the existing vault).
- **Permissions:** no manifest permission added. Uses Android Keystore and app-private storage only.
- **CI/build:** passed on the batch source/documentation head `0de68e1501efa54523776625964b387690d1c9ca` (GitHub Actions run #537, ID `45043884161`) and the latest tracker-linked head `f27de69d8faade2c565adf0870569e301fe3dec7` (run #538, ID `45043923314`). Backend JavaScript syntax checks, Gradle debug APK build and APK artifact upload completed successfully. Latest artifact ID: `14762319147` (`jarvis-debug-apk`).
- **Unit tests/lint:** not yet run as separate tasks; no claim of passing.
- **Physical device:** pending on Poco M4 Pro 4G. Verify API 23+ Keystore AES-GCM support, create/verify/replace/delete archive, graph CRUD and idea-version restore.
- **Merge:** keep stacked on parent PR #23 until reviewed; do not merge to parent or `main` without owner approval.
