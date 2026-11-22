# JARVIS Implementation Tracker

## Project boundaries
- This repository is the separate JARVIS assistant project.
- Do not modify the user's separate Even assistant project.
- Preserve Android permission boundaries, user-visible controls, and confirmation for sensitive external actions.
- Blueprint inclusion, code presence, automated validation, and physical-device verification are separate statuses.

## Current working branch
- Branch: `blueprint/advanced-feature-integration`
- Draft PR: https://github.com/sd2176845-glitch/jarvis-ai-assistant/pull/21
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
4. Implement one bounded module per batch, update this tracker and the blueprint, then run CI/build checks.
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
- **Scope boundary:** this is the review/edit/export/delete foundation. A timestamped event timeline, knowledge graph, versioned idea history and encrypted archive are not yet implemented.
- **Validation:** source commit 7eef4d7a66f4cdc2c436997fa5d81cbe69a5b441; current-head GitHub Actions build and artifact validation pending. Poco M4 Pro 4G device verification pending.
