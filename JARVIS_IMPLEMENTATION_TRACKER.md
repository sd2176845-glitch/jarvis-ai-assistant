# JARVIS Implementation Tracker

## Project boundaries
- This repository is the separate JARVIS assistant project.
- Do not modify the user's separate Even assistant project.
- Preserve Android permission boundaries, user-visible controls, and confirmation for sensitive external actions.
- Blueprint inclusion, code presence, automated validation, and physical-device verification are separate statuses.

## Current mainline state (2026-11-27)

- Main HEAD: `f346fe2b7d66f9281d03515c0f58c5c1eed8d1df`.
- PR #76 merged: M01, M02, M06, M13, M14 and M16–M19 actual-code reconciliation.
- PR #75 / M03 Academic Research Explorer merged as `00ffdb42ea8c6765ce641e82b09afc151b76024a`.
- PR #71 / M19 journey and collaborative planning foundation merged as `d7ad95a70c64d32aee41975f6c7b326cf596c62e`.
- Superseded PRs #62, #63, #65 and #70 closed; their scoped changes are in current main.
- PR #61 remains open for separate M01 identity/authentication scope. PR #66 remains open because it also contains unrelated Vision and Memory changes; its direct-SMS slice is already merged through #76.
- PR head SHA `f3d91ad0723c90ae36c09264466e381f8ace920b` passed Actions run #1443 (ID `46049760374`): backend tests, Android unit tests, debug APK build and artifact upload. Artifact ID `15223702648`, SHA-256 `32e1d191adfd1bc4876d709e51beeaed9405bc3e8fba07b7309a4a6484441182`.
- Post-merge main SHA `f346fe2b7d66f9281d03515c0f58c5c1eed8d1df` passed Actions run #1445 (ID `46049961444`): backend tests, Android unit tests, debug APK build and artifact upload. Artifact ID `15223881797`, SHA-256 `9e6fca49b8323da9ea68c020ad417dd6225f8f72150029620185b921349608ac`.
- Poco M4 Pro 4G physical-device verification remains pending. CI success is not release certification. The separate Even project remains untouched.

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

## Next technical steps (current mainline)

- Install post-merge debug APK artifact `15223881797` on the target Poco M4 Pro 4G.
- Verify M01 consent on/off for foreground/background voice, contact lookup, call/SMS announcements, outgoing SMS and diagnostics. Confirm consent blocks access before Android permission prompts.
- Verify M06 recipient selection, email/WhatsApp draft handoffs, SMS final confirmation, cancellation and permission denial.
- Verify M13 AES-GCM integrity check, corrupt-file handling, missing-key handling and delete confirmation.
- Verify M14 checkpoint create/review/restore/rollback, including empty/corrupt checkpoints and exclusion of private/security data.
- Verify M16 integration registry and share summary; M17 readiness report; M18 scenario comparison; M19 collaborative-plan sharing.
- Record physical-device results before marking any module device-verified.

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


## Batch B03 — M03 Language & Learning Studio (three independent owner-invoked tools; 2026-11-22)

**Branch:** `feature/m03-language-studio-3pack`, stacked on the M09 memory-expansion branch / PR #24. Draft PR #25: https://github.com/sd2176845-glitch/jarvis-ai-assistant/pull/25. This batch is separate and unmerged.

### Feature 1 — Hindi / Bengali / English Translation
- **Code-present:** added an owner-invoked translation workflow with explicit target-language selection (Hindi, Bengali, English), text entry, result preview and optional Android text-to-speech playback.
- Speech playback depends on an installed Android TTS voice for the selected language; missing language data is reported rather than silently substituted.
- **Acceptance checks:** translate each supported language pair; check punctuation/names/numbers; test missing TTS language data; confirm Speak is user-triggered and Stop/exit behavior is safe.

### Feature 2 — Text Clarity (Summary / Simple Explanation)
- **Code-present:** owner may paste text and choose concise key-point summary or a simple-language explanation. Output is shown in the app and is not saved as a note or memory.
- **Acceptance checks:** test short/long text, empty input, 5,000-character limit, source text with uncertainty, server failure and cancellation.

### Feature 3 — Vocabulary Builder
- **Code-present:** owner enters one word or short phrase and receives a learning card with simple meaning, example sentence and pronunciation guidance when available. It does not claim unsupported etymology.
- **Acceptance checks:** common word, ambiguous word, phrase, empty input, server failure and cancellation.

### Privacy, dependencies and verification
- The studio sends only the prompt constructed from the text the owner explicitly enters. It uses a new `JarvisAiClient.askStandalone` request path that does not attach or save recent conversation memory.
- The dialog discloses backend processing before submission. No automatic clipboard/gallery access, background collection, local history, extra permission or new persistent data store.
- Uses the existing HTTPS JARVIS backend and Android TTS engine; no API key is added to the APK.
- **Implementation files:** `LanguageStudio.java` (new), `JarvisAiClient.java` (isolated request method), and `MainActivity.java` (one Command Center entry).
- **CI/build:** passed on final code head `4caf2cacef9ca55cac77b4ef05b990bee9c24545`; GitHub Actions run #547 (`45044601421`) completed backend JavaScript syntax validation, Gradle debug APK build and APK artifact upload. Artifact ID `14762243808`, SHA-256 `6915af375edcef95663da8204b636c6ec971bf991a482461fb4c108be8d15eef`. Android unit tests/lint and Poco M4 Pro 4G device verification are not claimed.
- **Device acceptance:** test language voice availability, network-offline messaging, cancellation, text limit, and ensure the studio does not modify saved notes or conversation memory.
- **Merge policy:** keep this stacked PR separate; do not merge to parent PR #24 or `main` without owner approval.


## Batch B04 — M04 Universal App Intelligence (three independent read-only capabilities; 2026-11-22)

**Branch:** `feature/m04-app-intelligence-3pack`, stacked on the M03 Language Studio branch / PR #25. Draft PR #26: https://github.com/sd2176845-glitch/jarvis-ai-assistant/pull/26. Separate, unmerged batch.

### Feature 1 — Installed App Catalogue
- **Code-present:** lists apps Android exposes through the launcher intent, deduplicates by package name and sorts by visible app label. The catalogue does not launch apps; existing app-launch behavior remains owned by M05.
- **Acceptance checks:** empty catalogue, duplicate launcher activities, long labels, package-visibility restrictions and app uninstall/update between list and selection.

### Feature 2 — App Details & Update Information
- **Code-present:** for an owner-selected visible app, displays public package metadata: package name, version name/code, target Android API, first-install time and last-update time.
- **Acceptance checks:** missing version name, package removed between selection and lookup, system/user apps and date formatting.

### Feature 3 — Permission Transparency
- **Code-present:** displays permission names declared in the selected app's package manifest. Explicitly labels them as requested declarations only; it does not infer runtime grants or special access.
- **Acceptance checks:** no requested permissions, long permission list, package lookup failure and verify wording never claims a permission is granted.

### Privacy, platform and verification
- Uses Android launcher-intent package visibility through a matching `<queries>` declaration; no `QUERY_ALL_PACKAGES` or new runtime permission.
- Reads public PackageManager metadata only. No app-private files, screen contents, usage history, contacts, network scan, app launch, settings change or upload.
- **Implementation files:** `AppIntelligenceStudio.java` (new), existing `MainActivity.java` (one Command Center entry), `AndroidManifest.xml` (launcher-intent query declaration).
- **CI/build:** passed on code head `7d06870c70630171b69fbd9f6bc3935e6fb88fdc`; GitHub Actions run #553 (`45044836017`) completed backend syntax validation, Gradle debug APK build and APK artifact upload. Artifact ID `14762504922`, SHA-256 `77eb8572bc40e7e53c22515789486388cba95d9287e3c976ee4ff8387a2d7f65`. Android unit tests/lint and Poco M4 Pro 4G device verification are not claimed.
- **Device acceptance:** confirm Android 13 visibility results, catalogue count/labels, app update/uninstall race handling and permission declaration labels.
- **Merge policy:** do not merge to parent PR #25 or `main` without owner approval.


## Batch B05 — M05 Public Knowledge Lookups (three independent read-only integrations; 2026-11-22)

**Branch:** `feature/m05-public-knowledge-3pack`, stacked on M04 App Intelligence / PR #26. Separate, unmerged batch.

### Feature 1 — Country Information
- **Code-present:** owner enters a country name or ISO alpha-2/alpha-3 code. JARVIS retrieves public country facts including capital, region, population, area, currencies and languages. Name searches may return multiple matches; up to eight are displayed rather than silently choosing one.
- **Provider:** countries.dev, HTTPS, no API key. Source URL: https://countries.dev/docs
- **Acceptance checks:** ISO2, ISO3, full name, ambiguous partial name, unknown country, missing optional fields, rate-limit and network failure.

### Feature 2 — India PIN Code Lookup
- **Code-present:** owner enters a six-digit PIN; JARVIS displays the returned post-office list, branch type, delivery status, district, state and division. A PIN may map to several post offices, so the UI shows multiple entries and does not pick one as the user's address.
- **Provider:** PostalPincode.in public API. Source URL: https://www.postalpincode.in/Api-Details
- **Reliability note:** community-maintained reference service; no production SLA. Results are informational, not address verification or delivery guarantees.
- **Acceptance checks:** valid PIN with multiple post offices, unknown/invalid PIN, HTTP errors, long result list, unavailable provider and non-delivery branch labels.

### Feature 3 — English Dictionary
- **Code-present:** owner enters one English word; JARVIS displays available phonetic spelling, parts of speech, definitions and examples.
- **Provider:** Free Dictionary API / dictionaryapi.dev. Source URL: https://dictionaryapi.dev/
- **Scope:** English-only in this increment; no claim of Hindi/Bengali dictionary coverage. Provider availability and response completeness may vary.
- **Acceptance checks:** common word, multiple meanings, hyphen/apostrophe word, not-found word, missing phonetics/examples, HTTP 429 and malformed response.

### Privacy, network and verification
- Each lookup is separately selected and requires a confirmation before sending the entered value. Only the entered country name/code, six-digit PIN or English word is sent; no contacts, notes, conversation history, precise location or background data is attached.
- HTTPS GET only; bounded connect/read timeouts; response size capped; HTTP and malformed-response errors handled. No API keys or new Android permissions.
- **Implementation files:** `PublicKnowledgeStudio.java` (new) and existing `MainActivity.java` (one Command Center entry).
- **CI/build:** passed on final code/documentation head `76877d030adcf36b187bf01e263ce52051b10661`; GitHub Actions run #563 (`45045200384`) completed backend JavaScript syntax validation, Gradle debug APK build and APK artifact upload. Artifact ID `14763530517`, SHA-256 `b2929bd0d13c164ba79c2263374a3e2749bc71e3ee2abbdc3f6e1196cbec5457`. Android unit tests/lint and Poco M4 Pro 4G device verification are not claimed.
- **Merge policy:** keep stacked on PR #26; do not merge to parent PR #26 or `main` without owner approval.


## Batch B06 — M06 Live Information Studio (three independent lookups; 2026-11-22)

**Branch:** `feature/m06-live-info-3pack`, stacked on M05 Public Knowledge / PR #27. Separate, unmerged batch.

### Feature 1 — City Weather
- **Code-present:** owner enters a city/town name; Open-Meteo geocoding returns up to five matches, the owner selects the intended place, then JARVIS displays current temperature, feels-like temperature, humidity, wind, precipitation, WMO condition, daylight state, local data time and time zone.
- **Privacy:** manual city entry only; no GPS, background location or location permission.
- **Provider:** Open-Meteo forecast + geocoding. API docs: https://open-meteo.com/en/docs
- **Acceptance checks:** ambiguous city selection, no result, weather code mapping, missing optional fields, API error, offline behavior and timestamp/timezone.
### Feature 2 — Currency Converter
- **Code-present:** owner enters amount and ISO 4217 source/target codes (example: `100 USD INR`). Uses Frankfurter v2 reference rates and displays the returned rate date and calculated amount.
- **Important limitation:** reference rates are not real-time trading quotes, bank/card rates or guaranteed conversion amounts; spreads and fees may differ.
- **Provider:** Frankfurter. API docs: https://frankfurter.dev/v1/
- **Acceptance checks:** INR/USD and reverse pair, unsupported code, same-currency request, invalid/large amount, no rate, provider error and rounding.

### Feature 3 — India Public Holidays
- **Code-present:** owner enters a year; JARVIS lists India holiday entries, dates, holiday type, national flag and any returned subdivision codes.
- **Important limitation:** dataset coverage may not include every state, district or local observance. It is not an official complete state holiday calendar; verify important dates with relevant government notifications.
- **Provider:** Nager.Date public API v4. API docs: https://github.com/nager/Nager.Date/blob/main/doc/examples/rest-api.md
- **Acceptance checks:** current year, future year, no results, national vs subdivision entries, long list and provider downtime.

### Privacy, platform and verification
- Each lookup has an explicit confirmation step. Only the manually entered city, amount/currency codes or year is sent. No GPS, contacts, notes, conversation history or background data is attached.
- HTTPS GET only; bounded connect/read timeouts, response-size cap and malformed/error response handling. No API keys or new Android permissions.
- **Implementation files:** `LiveInfoStudio.java` (new) and existing `MainActivity.java` (one Command Center entry).
- **CI/build:** passed on code/documentation head `7b2c1c1e88fc299d36af3e198a29449e1f0146c0`; GitHub Actions run #572 (`45045389085`) completed backend JavaScript syntax validation, Gradle debug APK build and APK artifact upload. Artifact ID `14763293374`, SHA-256 `6648c8850934b605c00b918a49b579eb4ab317ab45a9d39ca31d62b954391e28`. Android unit tests/lint and Poco M4 Pro 4G device verification are not claimed.
- **Merge policy:** keep stacked on PR #27; do not merge to parent PR #27 or `main` without owner approval.


## Batch B07 — M10 AI Learning Academy (three guided learning modes; 2026-11-22)

**Branch:** `feature/m10-learning-academy-3pack`, stacked on M06 Live Information / PR #28. Separate, unmerged batch.

### Feature 1 — Coding Tutor
- **Code-present:** learner selects Python, Java, Kotlin or JavaScript, chooses Beginner/Intermediate/Advanced level, and enters a topic. JARVIS generates a structured lesson with explanation, small benign example, line-by-line notes, common mistake and practice task with a separate answer key.
- **Safety scope:** excludes malware, credential theft, covert surveillance, backdoors, exploit instructions and harmful code; unsafe topics are redirected to harmless programming concepts.
- **Acceptance checks:** each language selection, each level, empty/long topic, accurate runnable sample, offline/backend error and safety redirection.

### Feature 2 — Science Explorer
- **Code-present:** learner selects Physics, Chemistry, Biology or Space & Earth Science, chooses a level and enters a concept. JARVIS returns a definition, explanation, everyday example, key terms, misconception and review questions.
- **Safety scope:** conceptual teaching only; no hazardous experiment instructions, dangerous chemical procedures or unsafe activity steps.
- **Acceptance checks:** all subject choices, each level, uncertainty handling, empty/long topic, and safe conceptual redirection.

### Feature 3 — Technology Learning Roadmap
- **Code-present:** learner selects Android development, website development, AI basics, digital safety/defensive cybersecurity or computer-science foundations, then receives staged prerequisites, concepts, safe projects, effort estimates and checkpoints.
- **Safety scope:** cybersecurity path stays defensive and lawful; no intrusion, attack, credential theft, malware, evasion or surveillance instructions.
- **Acceptance checks:** each track, each level, roadmap structure, realistic project scope and defensive-only cyber content.

### Privacy, integration and verification
- Uses existing `JarvisAiClient.askStandalone`, so it sends only the selected learning choices and topic. Recent conversation memory, saved notes and contacts are not attached or updated.
- Explicit confirmation before the AI request; topic capped at 300 characters. No new Android permission, persistent learning database, background collection or API key in APK.
- **Implementation files:** `LearningAcademyStudio.java` (new) and existing `MainActivity.java` (one Command Center entry).
- **CI/build:** passed on code/documentation head `a9cd77178709102a9bc746840f9c6e44b0f496b8`; GitHub Actions run #581 (`45045554373`) completed backend JavaScript syntax validation, Gradle debug APK build and APK artifact upload. Artifact ID `14763640351`, SHA-256 `648188031f44642066caf26da29fe54d3a14dec55c84095f7468ed01a2672ccf`. Android unit tests/lint and Poco M4 Pro 4G device verification are not claimed.
- **Merge policy:** keep stacked on PR #28; do not merge to parent PR #28 or `main` without owner approval.


## Batch B08 — M11 Software Development Studio (three independent owner-invoked tools; 2026-11-22)

**Branch:** `feature/m11-software-dev-studio-3pack`, stacked on M10 AI Learning Academy / PR #29. Separate, unmerged batch.

### Feature 1 — Project Architect
- **Code-present:** owner selects Android, website, backend API, desktop or cross-platform target and enters a project idea. JARVIS generates requirements, user stories, architecture, modular file tree, data model, privacy/security considerations, accessibility, offline/error handling and build/test sequence.
- **Scope:** planning output only; it does not claim to create files, run code or deploy an app.
- **Acceptance checks:** each platform, vague project brief, optional vs required feature separation, offline/server error and safe scope redirection.

### Feature 2 — Bug & Error Explainer
- **Code-present:** owner pastes an error message and optional small code excerpt. JARVIS explains the error, ranks plausible causes, suggests a minimal correction and lists verification steps.
- **Privacy:** input capped at 5,000 characters; owner is reminded to remove secrets and personal data before submission.
- **Acceptance checks:** Java compile error, runtime error, incomplete context, malformed excerpt, secret-removal warning, 5,000-character cap and no unsupported root-cause claims.

### Feature 3 — Code Quality Review
- **Code-present:** owner selects Java, Kotlin, Python, JavaScript/TypeScript or Other and submits a code excerpt. JARVIS reviews correctness, readability, performance, accessibility/usability where relevant and defensive security/privacy; findings are grouped by severity and tied to visible evidence.
- **Scope:** no code execution, automatic edits, file-system access or whole-project security certification.
- **Acceptance checks:** each language choice, clean snippet, obvious bug, long snippet, missing context and defensive-only security guidance.

### Privacy, integration and verification
- Uses existing `JarvisAiClient.askStandalone`; only owner-entered descriptions/errors/code are sent after explicit confirmation. No saved conversation, notes or contacts are attached or updated.
- No new Android permission, background collection, persistent code archive or API key in APK.
- Safety boundaries exclude malware, credential theft, covert surveillance, backdoors, evasion and unauthorized access.
- **Implementation files:** `SoftwareDevelopmentStudio.java` (new) and existing `MainActivity.java` (one Command Center entry).
- **CI/build:** passed after correction on code head `b922a78b5e9c2956c7f4237331633939cb701861`; GitHub Actions run #591 (`45045770900`) completed backend JavaScript syntax validation, Gradle debug APK build and APK artifact upload. Artifact ID `14763955311`, SHA-256 `84ef58a3e5e25fa086c270be6e4a8002019ec8143f903ce088a35a97b1f209fa`. Initial runs #586–#590 failed due two extra closing parentheses in nested dialog callbacks; corrected in this head. Android unit tests/lint and Poco M4 Pro 4G device verification are not claimed.
- **Merge policy:** keep stacked on PR #29; do not merge to parent PR #29 or `main` without owner approval.


## Batch B09 — M12 Data Intelligence Center (three local-first CSV tools; 2026-11-22)

**Branch:** `feature/m12-data-intelligence-3pack`, stacked on M11 Software Development Studio / PR #30. Separate, unmerged batch.

### Feature 1 — Dataset Profiler
- **Code-present:** owner pastes CSV text (first row treated as headers); JARVIS parses locally and reports data row count, column count, non-empty cells and numeric min/max/average where a column is fully numeric.
- **Limits:** 50,000 characters, 1,000 rows, 50 columns. Supports quoted commas and escaped double-quotes; malformed/unclosed quoted fields are rejected.
- **Acceptance checks:** quoted cells, empty cells, numeric/text/mixed columns, CRLF/LF, malformed quote, row/column/character caps and empty input.

### Feature 2 — Data Quality Inspector
- **Code-present:** local checks for missing cells, inconsistent row widths, duplicate data rows, empty headers and duplicate headers.
- **Scope:** structural checks only; does not prove factual accuracy, detect every semantic anomaly or validate business rules.
- **Acceptance checks:** each warning type, no-issue dataset, inconsistent row lengths, duplicate rows, duplicate/empty headers and blank lines.

### Feature 3 — Aggregate AI Insight Report
- **Code-present:** builds an aggregate profile and quality summary locally, then shows the exact summary preview and asks for confirmation before sending it to the existing AI backend for a cautious narrative report.
- **Privacy:** original CSV rows are not sent. AI payload uses anonymized labels such as Column 1/Column 2 and structural counts/numeric aggregates only. No raw row values, contacts, notes or recent conversation are attached.
- **Acceptance checks:** inspect preview/payload, verify raw cell values are absent, cancel flow, low-data dataset, unsupported values, offline/backend error and no memory changes.

### Privacy, integration and verification
- Local-only parsing; original CSV is not saved. User is warned not to paste passwords, contact details, health/financial identifiers or confidential records.
- Aggregate AI report uses existing `JarvisAiClient.askStandalone` after explicit confirmation. No new Android permission, persistent dataset store, API key or background collection.
- **Implementation files:** `DataIntelligenceStudio.java` (new) and existing `MainActivity.java` (one Command Center entry).
- **CI/build:** passed on final code head `0b2a94bdbf88c5cd74c1f71cda62057096f0e16a`; GitHub Actions run #602 (`45046009488`) completed backend JavaScript syntax validation, Gradle debug APK build and APK artifact upload. Artifact ID `14763571783`, SHA-256 `213c503c4a5cc0ad81435fe83a813f7a0a594ed734cdb3a0704d43e72fbd3a8c`. Android unit tests/lint and Poco M4 Pro 4G device verification are not claimed.
- **Merge policy:** keep stacked on PR #30; do not merge to parent PR #30 or `main` without owner approval.


## Batch B10 — M13 Personal Productivity Studio (three upgrades to existing task manager; 2026-11-22)

**Branch:** `feature/m13-productivity-studio-3pack`, stacked on M12 Data Intelligence / PR #31. Separate, unmerged batch.

### Feature 1 — Project Workspace
- **Code-present:** owner assigns existing structured tasks to a project name, views project groups and sees pending/completed counts. Clearing the project name returns the task to Unassigned.
- **No duplicate store:** project membership is a `project` field on the existing `structured_tasks_v1` task objects. Tasks are not copied or moved into a parallel database.
- **Acceptance checks:** assign/reassign/clear project, multiple tasks in one project, unassigned tasks, long/empty project name, malformed task data and persistence after app restart.

### Feature 2 — Priority Matrix
- **Code-present:** owner assigns one of four priority labels to an existing task: Urgent + Important, Important/not urgent, Urgent/lower impact, Low priority.
- **No duplicate task list:** priority is a `priority` field on the existing task object.
- **Acceptance checks:** update priority repeatedly, preserve completion/project fields, task deletion behavior and malformed JSON protection.

### Feature 3 — Weekly Review
- **Code-present:** summarizes tasks created in the last seven days, their current pending/completed state, unassigned count and project-level counts.
- **Honesty:** it does not claim when a task was completed because the existing schema does not store a completion timestamp.
- **Acceptance checks:** no tasks, old tasks, tasks created in last seven days, pending/completed counts, unassigned and project grouping.

### Privacy, integration and verification
- Upgrades existing `structured_tasks_v1` in `jarvis_preferences`; no second task/project database and no change to legacy notes, workflow checklists or reminders.
- Local-only task data; no network request, new permission, background scheduling or automatic task creation.
- **Implementation files:** `ProductivityStudio.java` (new) and existing `MainActivity.java` (one Command Center entry).
- **CI/build:** passed after rebasing on latest PR #31 head. Code/documentation head `5c6b3929c99aafdfd589d83c0921cdf8a3684a4f` passed GitHub Actions run #615 (`45046258241`): backend JavaScript syntax validation, Gradle debug APK build and APK artifact upload. Artifact ID `14764031146`, SHA-256 `ee320e43e1d627326173b5537b151eb7df29dd42b7c118dc2bb4bd3fe2c19582`. Android unit tests/lint and Poco M4 Pro 4G device verification are not claimed.
- **Merge policy:** keep stacked on PR #31; do not merge to parent PR #31 or `main` without owner approval.


## Batch B11 — M15 Creative Media Studio (three text-first creative planning tools; 2026-11-22)

**Branch:** `feature/m15-creative-media-3pack`, stacked on M13 Personal Productivity / PR #33. Replacement draft PR #35 after rebasing onto the latest parent head.

### Feature 1 — Video Script Planner
- **Code-present:** owner selects vertical short, educational explainer or storytelling format; chooses 15-second, 30-second, 60-second or 2-minute target duration; enters a creative brief. JARVIS generates an original hook, timed narration, scene descriptions, on-screen text and caption notes.
- **Scope:** text script only; no video rendering or media file creation.
- **Acceptance checks:** each format/duration, realistic timing, factual claims, empty/long brief and backend failure.

### Feature 2 — Storyboard & Shot List
- **Code-present:** owner selects educational, product-demonstration or original-story style and enters a concept. JARVIS produces numbered scenes, duration, visual direction, framing, narration, on-screen text, transitions and accessibility notes.
- **Safety/privacy:** avoid hazardous setups and risky stunts; no use of a real person's likeness or private location without permission.
- **Scope:** text storyboard only; no image/video rendering.
- **Acceptance checks:** all styles, scene continuity, safe actions, accessibility notes and backend failure.

### Feature 3 — Presentation Builder
- **Code-present:** owner selects school/learning, project proposal or general-audience format and enters topic, purpose and preferred slide count. JARVIS generates a slide outline, concise bullets, speaker notes, visual suggestions and sources-to-verify.
- **Reliability:** prompt explicitly prohibits invented citations, URLs and statistics.
- **Scope:** text outline only; no PPTX file creation.
- **Acceptance checks:** 5–10 slide brief, audience-appropriate language, factual claims, sources-to-verify and no fabricated citations.

### Privacy, integration and verification
- Uses existing `JarvisAiClient.askStandalone`; only the manually entered creative brief and selected format are sent after explicit confirmation. No saved conversation, notes or contacts are attached.
- No new Android permission, persistent creative archive, background capture, media-rendering claim or API key in APK.
- **Implementation files:** `CreativeMediaStudio.java` (new) and existing `MainActivity.java` (one Command Center entry).
- **CI/build:** passed on rebased code/documentation head `0aa72a61b9d1172e7d1c55d44e0d6480cf465f14`; GitHub Actions run #636 (`45046654060`) completed backend JavaScript syntax validation, Gradle debug APK build and APK artifact upload. Artifact ID `14764056646`, SHA-256 `7a73861a89d41ec3480a92820684251fd9e1197eff59ddb45f831180d2fd6aab`. Android unit tests/lint and Poco M4 Pro 4G device verification are not claimed.
- **Merge policy:** keep stacked on PR #33; do not merge to parent PR #33 or `main` without owner approval.


## Batch B10 — M13 Creative Media Studio (three independent owner-invoked planners; 2026-11-22)

**Branch:** `feature/m13-creative-media-studio-3pack`, stacked on M12 Data Intelligence Center / PR #31. Separate, unmerged batch.

### Feature 1 — Image Concept Builder
- **Code-present:** owner selects square, portrait 9:16 or landscape 16:9 format and enters a short brief. JARVIS produces an original image-generation prompt, composition/framing, lighting/color direction and accessibility alt text.
- **Scope:** text prompt only; this Android module does not generate or save an image file.
- **Safety/copyright:** no imitation of living artists or reproduction of copyrighted characters, logos or existing artwork; no sexualized content, graphic violence, deceptive identity documents or harmful body ideals.
- **Acceptance checks:** each aspect ratio, brief cap/empty input, original-concept request, safe-content redirection and accurate prompt-only disclosure.

### Feature 2 — Video Storyboard Studio
- **Code-present:** owner selects 15/30/60/90 seconds and enters a concept/audience brief. JARVIS returns a scene-by-scene plan with approximate timing, visual direction, camera movement, narration/dialogue, captions, transitions and shot checklist.
- **Scope:** storyboard text only; no video rendering or audio generation.
- **Safety/copyright:** age-appropriate, non-graphic, original wording; no song lyrics, copyrighted scripts or imitation of a specific living creator.
- **Acceptance checks:** all duration choices, coherent timing, empty/overlong brief, scene structure and safe original output.

### Feature 3 — Presentation Planner
- **Code-present:** owner selects 5/8/10/12 slides and enters topic, audience and purpose. JARVIS returns slide titles, key bullets, suggested visuals, speaker notes and a conclusion.
- **Scope:** outline text only; no PowerPoint/PDF file generation. The output is instructed to flag facts needing verification and not invent sources/statistics.
- **Acceptance checks:** all slide counts, audience adaptation, fact-check flags, empty/overlong brief and no invented citations.

### Privacy, integration and verification
- Uses existing memory-isolated `JarvisAiClient.askStandalone`; only the manually entered creative brief and selected format are sent after explicit confirmation.
- No saved conversation, notes, contacts, clipboard/gallery auto-read, persistent project archive, background collection, new Android permission or API key in APK.
- **Implementation files:** `CreativeMediaStudio.java` (new) and existing `MainActivity.java` (one Command Center entry).
- **CI/build:** passed on final head `14b77ad900299a9a95c52781463aa0763c11733a`; GitHub Actions run #648 (`45047351132`) completed backend JavaScript syntax validation, Gradle debug APK build and APK artifact upload. Artifact ID `14764081799`, SHA-256 `b22ac837311db214740419c8deb4c988a6c5f8fb54bc0baa360eac320b67e743`. Android unit tests/lint and Poco M4 Pro 4G device verification are not claimed.
- **Merge policy:** keep stacked on PR #31; do not merge to parent PR #31 or `main` without owner approval.


## Batch B11 — M16 Smart Home Studio (three owner-controlled capabilities; 2026-11-22)

**Branch:** `feature/m16-smart-home-studio-3pack`, stacked on Creative Media Studio / PR #36. Separate, unmerged batch.

### Feature 1 — Device Setup Guide
- **Code-present:** owner enters device type/brand/model and setup goal. JARVIS generates a cautious setup checklist covering official manufacturer app/manual, compatibility verification, firmware updates, default credential changes, MFA where supported and privacy settings.
- **Scope:** guidance only; JARVIS does not pair with, scan, access or control devices. Model-specific claims must be verified against official documentation.
- **Acceptance checks:** vague/clear device details, compatibility uncertainty, firmware/privacy checklist, secret-removal warning and backend failure.

### Feature 2 — Local Room Device Inventory
- **Code-present:** owner can manually add, view/remove, and clear up to 30 room/device labels in existing private `jarvis_preferences` SharedPreferences under `smart_home_inventory_v1`.
- **Privacy:** labels only; prompts prohibit storing passwords, Wi-Fi keys, IP/MAC addresses and access codes. No device discovery, network scan or cloud sync.
- **Acceptance checks:** add/list/remove/clear, 30-item cap, empty state, malformed JSON preservation, storage failure and app restart persistence.

### Feature 3 — Smart Routine Planner
- **Code-present:** owner selects morning, leaving-home, evening, energy-saving or custom routine and enters a short device/room brief. JARVIS returns an ordered manual checklist, prerequisites, offline fallback and owner-approval points.
- **Safety boundary:** no hidden automation, unattended access, lock/door control or safety-critical cooking/heating/electrical actions. The routine is not executed and sends no device commands.
- **Acceptance checks:** every routine type, manual fallback, offline-device case, explicit approval notes and unsafe-action redirection.

### Privacy, integration and verification
- Uses existing memory-isolated `JarvisAiClient.askStandalone` for setup/routine guidance after explicit confirmation. Only the manually entered brief is sent; no conversation, notes or contacts are attached.
- Inventory stays local in the existing private preferences store. No new permission, background collection, network discovery, credentials, device control or API key in APK.
- Existing **Connected systems** menu remains unchanged; it still opens Android Wi-Fi/Bluetooth/location settings and common apps. Smart Home Studio is a separate planner/inventory layer, not a replacement for those settings shortcuts.
- **Implementation files:** `SmartHomeStudio.java` (new) and existing `MainActivity.java` (one Command Center entry).
- **CI/build:** passed on final documented branch head `eaf5634ca4f8f652e242ba44bec940c9db2e7afe`; GitHub Actions run #662 (`45047913711`) completed backend JavaScript syntax validation, Gradle debug APK build and APK artifact upload. Artifact ID `14764123874`, SHA-256 `15e3ed72d5d98969b4eb4fd4981dc1a88eb0cb277e8cdf245eb95f807d19dcec`. Earlier runs #653–#658 failed on a routine-dialog builder-chain syntax error; the final correction is included in this passing head. Android unit tests/lint and Poco M4 Pro 4G device verification are not claimed.
- **Merge policy:** keep stacked on PR #36; do not merge to parent PR #36 or `main` without owner approval.


## Batch B12 — M17 Connectivity Intelligence (three independently tracked capabilities; 2026-11-22)

**Branch:** `feature/m17-connectivity-intelligence-3pack`, stacked on M16 Smart Home Studio / PR #37. Draft PR #38 is intended to target `feature/m16-smart-home-studio-3pack`; this batch is separate and unmerged.

### Feature 1 — Network Health Snapshot
- **Code-present:** reads Android's active-network capability state through `ConnectivityManager` and `NetworkCapabilities`; reports transport category, whether Android reports internet capability, whether internet validation is confirmed, and metered/unknown state.
- **Privacy:** adds only the normal `ACCESS_NETWORK_STATE` permission. Does not read SSID, IP/MAC addresses, passwords, nearby networks or network traffic; does not scan, connect, or change settings.
- **Acceptance checks:** no active network; Wi-Fi; cellular; VPN; active network without capability details; validated vs unvalidated internet; permission/API exception; confirm no network identifiers are shown.

### Feature 2 — Offline Capability Matrix
- **Code-present:** presents a static, clearly labelled design-level guide separating locally available foundations (local commands, saved notes/tasks/checklists/inventory and installed offline TTS) from internet-dependent AI and provider lookups.
- **Honesty boundary:** this is not a live test of each feature; actual availability depends on Android permissions, installed apps/TTS data, and provider status.
- **Acceptance checks:** confirm local and online groups are distinct; check wording does not claim every feature works offline; verify this screen works with network disabled.

### Feature 3 — Connectivity Strategy Planner
- **Code-present:** owner selects Local-only network concept, Hybrid local + internet design, or Satellite internet & CubeSat learning, enters a short brief, reviews a disclosure and explicitly confirms before an isolated `JarvisAiClient.askStandalone` request.
- **Safety boundary:** planning/education only. No network configuration, discovery, scanning, pairing, transmission instructions, frequencies, interference, unauthorized access, credentials or covert monitoring.
- **Privacy:** only the manually entered brief and selected topic are sent; no conversation memory, notes or contacts are attached.

### Implementation and verification
- **Files:** `ConnectivityIntelligenceStudio.java` (new), `ConnectivityIntelligenceRules.java` (pure formatting/prompt rules), `ConnectivityIntelligenceRulesTest.java` (unit tests), `MainActivity.java` (one Command Center entry), `AndroidManifest.xml` (read-only network-state permission), `app/build.gradle` (JUnit), and `.github/workflows/android.yml` (run unit tests before APK build).
- **Automated tests:** three focused JUnit tests cover no-network vs unvalidated-network reporting, offline/online capability separation, and the planner's safety/prompt-injection boundaries. CI is configured to run `gradle testDebugUnitTest` before `gradle assembleDebug`.
- **CI/build:** code head `084c74bab505117a92960969b952d3c1f759207c` passed GitHub Actions run #677 (`45049194572`): backend syntax validation, `gradle testDebugUnitTest`, `gradle assembleDebug`, and APK artifact upload. Artifact ID `14764514133`; SHA-256 `77c6330b6d3c2e7f5cd20f7fe78c0aa559dd390b2c90e2926cff98cb2f3dd151`. Later documentation-only commits are running the same workflow; record their final outcome separately.
- **Physical device:** pending on Poco M4 Pro 4G. Check Wi-Fi/mobile/VPN/no-network states, verify network labels without exposing identifiers, review offline matrix in airplane mode, and test all three planner selections with cancel/confirm and offline/server-failure handling.
- **Merge policy:** keep stacked on PR #37; do not merge into PR #37 or `main` without owner approval. JARVIS only; Even untouched.


## Batch B13 — M18 Simulation & Engineering Intelligence (three safe virtual tools; 2026-11-22)

**Branch:** `feature/m18-virtual-engineering-studio-3pack`, stacked on M17 Connectivity Intelligence / PR #38. Draft PR #39 is intended to target `feature/m17-connectivity-intelligence-3pack`; this batch is separate and unmerged.

### Feature 1 — Digital World Simulator
- **Code-present:** owner selects Ecosystem balance, Planetary motion concepts, Water cycle and climate, or Sustainable community resources, then enters a short learning brief. JARVIS returns a text-only conceptual model with variables, possible outcomes, assumptions and limitations.
- **Safety boundary:** non-violent educational simulation only. No attack/weapon scenarios or real-world control of infrastructure, vehicles or equipment.
- **Acceptance checks:** all four scenario choices; empty/overlong brief; uncertainty and assumptions; offline/server failure; conflicting or unsafe input redirection.

### Feature 2 — Virtual Experiment Lab
- **Code-present:** owner selects Physics, Chemistry, Biology, or Earth and space science concepts and enters a learning question. JARVIS returns a hypothesis, variables, conceptual model, expected observations, explanation and limitations.
- **Safety boundary:** thought experiments and abstract models only; no real-world experiment procedures, hazardous chemicals, heating/electrical steps, biological culturing or dangerous activities.
- **Acceptance checks:** all subjects; conceptual output structure; empty/overlong brief; uncertainty; unsafe-procedure redirection; offline/server failure.

### Feature 3 — 3D Invention Studio
- **Code-present:** owner selects benign categories (school/desk organizer, everyday accessibility aid, sustainable household product, educational display model, abstract sculpture) and enters an idea. JARVIS returns a text-only concept brief with purpose, form, non-critical dimensions, accessible features, safe everyday materials and sustainability notes.
- **Scope:** no CAD/3D file generation or manufacturing-ready instructions.
- **Safety boundary:** no weapons, harmful devices, surveillance tools, bypass equipment or dangerous fabrication/assembly steps.
- **Acceptance checks:** all categories; accessibility/sustainability coverage; empty/overlong brief; unsafe concept redirection; explicit send confirmation and offline/server failure.

### Privacy, implementation and verification
- **Files:** `SimulationEngineeringStudio.java` (new), `SimulationEngineeringRules.java` (pure prompt builders), `SimulationEngineeringRulesTest.java` (three focused JUnit tests), and `MainActivity.java` (one Command Center entry).
- Uses the existing memory-isolated `JarvisAiClient.askStandalone` only after owner confirmation. Only the entered topic and selected category are sent. No new permission, persistent store, background process, physical device control or API key in APK.
- **Automated tests:** three separate JUnit tests cover non-violent conceptual simulation, exclusion of hazardous real-world experiment procedures, and benign non-manufacturing-ready design boundaries. The inherited M17 CI workflow runs `gradle testDebugUnitTest` before `gradle assembleDebug`.
- **CI/build:** code head `1f8057a55e1d96ac9f362bc47703a16621d45438` passed GitHub Actions run #686 (`45049538625`): backend syntax validation, `gradle testDebugUnitTest`, `gradle assembleDebug`, and APK artifact upload. Artifact ID `14764423492`; SHA-256 `f3b9fb59f768acce6a75b54ea7da088cffae4b0d2be6812b87de3b6ec9b4c00e`. Later documentation-only commits are running the same workflow; record their final outcome separately.
- **Physical device:** pending on Poco M4 Pro 4G. Confirm Command Center navigation, category selection, prompt preview/confirmation/cancel, response display, and backend/offline handling.
- **Merge policy:** keep stacked on PR #38; do not merge into PR #38 or `main` without owner approval. JARVIS only; Even untouched.


## Batch B14 — M19 Personal & Practical Intelligence (three distinct capabilities; 2026-11-22)

**Branch:** `feature/m19-practical-intelligence-3pack`, stacked on M18 Simulation & Engineering Intelligence / PR #39. Draft PR #40 is intended to target `feature/m18-virtual-engineering-studio-3pack`; this batch is separate and unmerged.

### Feature 1 — Emergency Readiness Planner
- **Code-present:** owner selects Flood, Cyclone/severe storm, Heatwave, Earthquake or General emergency, then enters optional city/district context and general household needs. JARVIS returns a general preparedness checklist and communication/check-in plan.
- **Honesty/safety:** not a live alert, forecast, evacuation order or official emergency instruction. It points users to current local authorities and relevant NDMA/IMD guidance, avoids invented links/phone numbers, and does not encourage entering floodwater, damaged buildings or risky rescues. For immediate danger in India it points to 112.
- **Acceptance checks:** all hazard categories; optional area omitted; empty/overlong brief; current-alert hallucination guard; unsafe rescue redirection; cancel/confirm and offline/server failure.

### Feature 2 — Legal Document Explainer
- **Code-present:** owner pastes a selected excerpt (maximum 4,000 characters); JARVIS explains visible terms, dates, obligations, ambiguities and questions to ask a qualified legal professional.
- **Privacy:** explicit disclosure/confirmation; instructs owner to redact names, addresses, phone/ID numbers, signatures and account details. Uses memory-isolated `JarvisAiClient.askStandalone`; does not save the excerpt in notes or recent conversation memory.
- **Honesty boundary:** plain-language information only, not legal advice. Does not invent missing clauses, laws, citations, deadlines or jurisdiction-specific conclusions.
- **Acceptance checks:** clear/ambiguous excerpt; missing context; 4,000-character cap; redaction notice; verify no memory persistence; legal-advice boundary; cancel/offline handling.

### Feature 3 — Vehicle Maintenance Log
- **Code-present:** owner can add, view/remove and clear up to 50 local service records with a vehicle nickname, service date label, optional digits-only odometer reading and short maintenance summary.
- **Anti-duplication:** uses one dedicated local key `vehicle_service_log_v1`; it is a service-history log, not a second task list or reminder scheduler. Any future due-date reminders should reuse M07 tasks.
- **Privacy:** no registration number, VIN, insurance details, credentials or personal identifiers requested; records remain in existing private `jarvis_preferences` storage. No cloud sync, background collection or new permission.
- **Acceptance checks:** add/list/remove/clear; 50-record cap; required fields; odometer validation; app-restart persistence; malformed-data preservation; storage failure.

### Implementation and verification
- **Files:** `PracticalIntelligenceStudio.java` (new), `PracticalIntelligenceRules.java` (pure prompts/validation), `PracticalIntelligenceRulesTest.java` (three focused JUnit tests), and `MainActivity.java` (one Command Center entry).
- **Automated tests:** separate tests cover emergency-planning honesty/safety, legal explanation boundaries, and vehicle-record validation. Inherits CI's `gradle testDebugUnitTest` before `gradle assembleDebug`.
- **CI/build:** corrected code head `9bd9ea41ccd237911ea86e133787b4d6bbe84f27` passed GitHub Actions run #701 (`45050181584`): backend syntax validation, `gradle testDebugUnitTest`, `gradle assembleDebug`, and APK artifact upload. Artifact ID `14764908613`; SHA-256 `dcac5e8d711b92ac0a88c7154b93d496c0610b297c50681f4a2c7e88d48c68af`. Intermediate run #700 failed because the vehicle form helper lacked an Activity parameter; this was corrected in the passing head. Earlier documentation-only runs #697–#699 passed.
- **Physical device:** pending on Poco M4 Pro 4G. Verify local record CRUD/restart/corrupt-data behavior, AI confirmation/cancel, and offline/server errors.
- **Merge policy:** keep stacked on PR #39; do not merge into PR #39 or `main` without owner approval. JARVIS only; Even untouched.


## Batch B15 — M01 Owner Privacy Studio (2026-11-22)

**Branch:** `feature/m01-owner-privacy-studio-3pack`, stacked on M19 / PR #40. Draft PR is to target `feature/m19-practical-intelligence-3pack`; this batch is separate and unmerged.

### Feature 1 — Permission & Access Review
- **Code-present:** read-only status summary for microphone, contacts, phone state, direct calling, SMS receipt, Android notifications (where runtime permission applies), display-over-other-apps and JARVIS accessibility service.- **Controls:** links to Android App Info and Accessibility settings. The screen never requests, grants, revokes or changes permissions.
- **Boundary:** status display only; not a security certification or proof that a feature is safe/necessary.

### Feature 2 — Local Data Inventory
- **Code-present:** counts/lengths only for saved notes, recent AI context, structured tasks, manual workflows, owner-created timeline, knowledge graph, idea versions and vehicle service records; reports whether the encrypted legacy archive file exists.
- **Privacy:** does not display or export the underlying content. Malformed JSON is labelled unreadable rather than falsely reported as empty. Existing stores are reused; no new store or background collection.

### Feature 3 — Sanitized Privacy Summary Export
- **Code-present:** owner-triggered Android share chooser for permission states and aggregate counts only. The export explicitly excludes note/conversation contents, contacts, calls, SMS, credentials and device identifiers.
- **Confirmation:** owner reviews the disclosure and chooses Continue before Android's share chooser opens. No automatic upload or recipient selection.

### Implementation and verification
- **Files:** `OwnerPrivacyStudio.java`, `OwnerPrivacyRules.java`, `OwnerPrivacyRulesTest.java`, and the existing `MainActivity.showPrivacyOptions()` entry upgraded in place. No duplicate Command Center entry, no new Android permission, no new storage key, no network request.
- **Automated tests:** three focused JUnit tests cover permission-state wording, JSON inventory counts/corrupt-data handling, and sanitized export boundaries.
- **CI/build:** corrected final head `34941b26529b36519f27bae775d0f85a588443b1` passed GitHub Actions run #711 (`45051230977`): backend syntax validation, `gradle testDebugUnitTest`, `gradle assembleDebug`, and APK artifact upload. Artifact ID `14765930477`. Earlier runs #707–#710 exposed a local JVM test issue from using Android's `org.json` stub; the counter was rewritten as a pure-Java structural check and the final run passed.
- **Physical device:** pending on Poco M4 Pro 4G. Verify permission status accuracy, Android settings handoffs, all known inventory counts, corrupt JSON handling, share chooser cancel/selection, and confirm no private content appears in exported text.
- **Merge policy:** keep stacked on PR #40; do not merge into PR #40 or `main` without owner approval. JARVIS only; Even untouched.


## Batch B16 — M02 Owner-Reviewed Voice Transcript Correction (2026-11-22)

**Branch:** `feature/m02-voice-transcript-review-3pack`, stacked on M01 / PR #41. Draft PR targets `feature/m01-owner-privacy-studio-3pack`; separate and unmerged.

### Feature — Transcript Correction Review Gate
- **Code-present:** after the existing speech-understanding endpoint returns a corrected transcript, JARVIS compares it with the original recognized words.
- **No unnecessary interruption:** unchanged wording continues through the existing command router without a dialog.
- **Owner choice:** when wording changes, JARVIS displays original and suggested correction with **Use correction**, **Use original**, **Edit text**, and cancel. No command is routed until the owner chooses.
- **Edit safety:** empty edited text is rejected. Cancelling review runs no command. The transcript is not saved by this review feature.
- **Anti-duplication:** upgrades the existing `understandVoiceCommand()` flow; reuses the existing speech recognizer, backend correction endpoint, command router and foreground voice UI. Does not create another voice assistant, transcript store, or command router.
- **Privacy:** only the current recognized utterance is reviewed; no new persistence, contacts access, background collection, or additional network request is introduced.
- **Files:** `VoiceTranscriptReviewRules.java`, `VoiceTranscriptReviewRulesTest.java`, and existing `MainActivity.java`.
- **Automated tests:** three JUnit tests cover unchanged wording, changed-word review requirement, and safe fallback for empty correction.
- **CI/build:** pending for this branch; do not report as passed until completed.
- **Physical device:** pending on Poco M4 Pro 4G. Verify unchanged transcripts route normally; changed transcripts show all choices; Use original/correction/Edit work; cancel and empty edit run no command; existing local commands and confirmation flows remain intact.
- **Merge:** draft PR only; no merge into M01 or `main` without owner approval. JARVIS only; Even untouched.


## Batch B17 — M15 Display & Accessibility Studio (2026-11-22)

**Integration status (verified 2026-11-27):** implemented on `main` and connected to the existing Command Center. The historical feature branch/stacked draft PR is not the merge source of truth; do not cherry-pick or duplicate these files.

### Feature — Owner-controlled readability profiles
- **Code-present:** adds a single Display & Accessibility Studio entry to the existing Command Center. Owner can choose Standard, Large text, Extra-large text, High contrast, or Reset to standard.
- Applies app-local text sizing and readable foreground colors to the existing JARVIS status and response text; the selected profile persists in the existing private `jarvis_preferences` store.
- The profile is applied on app startup and after an owner selection. It does not change Android system font/display settings, alter screen density, hide controls, or add permissions.
- **Anti-duplication:** extends the existing M15 Command Center and existing status/response views; no duplicate dashboard, settings page, or accessibility service.
- **Automated tests:** three JUnit tests cover profile sizing, high-contrast selection, and invalid-profile fallback.
- **Physical device:** pending on Poco M4 Pro 4G. Check all profiles, restart persistence, contrast in sunlight/darkness, text clipping and screen-reader announcements.
- **Main-branch verification:** `DisplayAccessibilityStudio.java` and `DisplayAccessibilityRules.java` are present; `MainActivity` applies the saved profile at startup and exposes the existing Command Center entry. `DisplayAccessibilityRulesTest.java` is present.
- **CI/build:** GitHub Actions run #1306 passed backend checks, Android unit tests, debug APK build and artifact upload on main commit `be86853b98e85e8a9360fab27a655489155e0709`.
- **Physical device:** pending on Poco M4 Pro 4G; verify all profiles, restart persistence, contrast, text clipping and screen-reader announcements. JARVIS only; Even untouched.


## Batch B18 — M16 Authorized Bluetooth Accessory Handoff (2026-11-22)

**Integration status (verified 2026-11-27):** implemented on `main` and connected through the existing Smart Home Studio inventory flow. The historical feature branch/stacked draft PR is not the merge source of truth; do not cherry-pick or duplicate these files.

### Feature — Owner-controlled Bluetooth accessory setup
- **Code-present:** upgrades the existing Smart Home Studio inventory menu with an owner-entered Bluetooth accessory label flow and a direct handoff to Android Bluetooth settings.
- Bluetooth labels reuse the existing local smart-home inventory and its 30-item limit; no second inventory database or sync service is created.
- Pairing and unpairing remain in Android's visible system UI. JARVIS does not scan nearby devices, read Bluetooth identifiers, pair/connect, send commands, or access credentials.
- Removing a Bluetooth label deletes only the local JARVIS label; the UI explains that actual unpairing must be done through Android Bluetooth settings > Forget.
- **Automated tests:** four pure-Java JUnit tests cover trimmed labels, empty/oversized input and Bluetooth label classification.
- **Anti-duplication:** extends existing M16 Smart Home Studio and reuses its inventory store, settings handoff conventions and owner-controlled removal flow.
- **Main-branch verification:** `SmartHomeAccessoryRules.java` and the Bluetooth inventory/settings handoff are present in `SmartHomeStudio.java`; the existing local inventory and 30-item limit are reused. `SmartHomeAccessoryRulesTest.java` is present.
- **CI/build:** GitHub Actions run #1306 passed backend checks, Android unit tests, debug APK build and artifact upload on main commit `be86853b98e85e8a9360fab27a655489155e0709`.
- **Physical device:** pending on Poco M4 Pro 4G; verify Bluetooth settings handoff and confirm no scanning or automatic pairing occurs. JARVIS only; Even untouched.


## Batch B19 — M17 Hardware Compatibility Snapshot (2026-11-22)

**Branch:** \`feature/m17-hardware-compatibility-snapshot\`, stacked on M16 / PR #44. Draft PR targets \`feature/m16-authorized-accessory-hub-3pack\`; unmerged.

### Feature — Read-only hardware compatibility hints
- **Code-present:** extends the existing Device Resource Intelligence report with Android-reported CPU core count, supported app ABI list, low-RAM device flag and display pixel dimensions/density.
- The report clearly labels these as compatibility hints, not performance benchmarks or guarantees. It does not run stress tests, inspect private app data, or change device settings.
- **Anti-duplication:** upgrades the existing M17 resource dashboard; no second diagnostics page or benchmark engine.
- **Automated tests:** three pure-Java JUnit tests cover ABI ordering, blank entries and unavailable data.
- **CI/build:** passed on final head f58bcfe73483a35e096ee8d71ea24f44484fb599, workflow run #738: backend syntax, Android unit tests, debug APK build and artifact upload all succeeded (artifact 14765789300).
- **Physical device:** pending on Poco M4 Pro 4G; compare reported ABI/display/resource information with Android settings and check report layout.
- **Merge:** draft PR only; do not merge into PR #44 or \`main\` without owner approval. JARVIS only; Even untouched.


## Batch B20 — M17 Battery Health & Charging Snapshot (2026-11-22)

**Branch:** feature/m17-battery-health-snapshot, stacked on M17 Hardware Compatibility / PR #45. Draft PR targets feature/m17-hardware-compatibility-snapshot; unmerged.

### Feature — Read-only battery state details
- **Code-present:** extends the existing Device Resource Intelligence battery section with Android-reported charging state, battery-health category and temperature.
- Values are clearly identified as device-reported snapshots, not a battery diagnostic or repair assessment. Missing values are shown as unavailable.
- **Anti-duplication:** upgrades the existing battery panel; no second battery page, background monitor, periodic polling, alerts or scheduler.
- **Privacy/safety:** reads only the existing battery-changed broadcast snapshot; no additional permission, upload or data persistence.
- **Automated tests:** three JUnit tests cover charging-state mapping, health labels and temperature formatting.
- **CI/build:** passed on final head `09cfe226988987e7fd79075ea4e95ec3a1de74d4`, GitHub Actions run `45053093326`: backend syntax validation, Android unit tests, debug APK build and APK artifact upload all succeeded. The successful run is attached to this exact code head; earlier intermediate failures are superseded.
- **Physical device:** pending on Poco M4 Pro 4G; verify available extras and layout; values may vary by manufacturer.
- **Merge:** draft PR only; do not merge into PR #45 or main without owner approval. JARVIS only; Even untouched.


## Batch B21 — Integrated 50-Feature Release (2026-11-22)

- **Branch:** `feature/jarvis-50-feature-megabatch`, created from B20 branch `feature/m17-battery-health-snapshot`.
- **Scope:** one coordinated batch with 50 candidate capabilities across the canonical M01–M19 owners. Full scope and acceptance gates are in `JARVIS_50_FEATURE_BATCH_B21_PLAN.md`.
- **Historical status at initial scope-lock commit:** planning only. Superseded by the implementation and verification entries later in this tracker.
- **Anti-duplication:** candidates must be audited against the existing B20 studios and stores before coding; overlapping items will upgrade the existing owner module rather than create duplicate engines, screens, schedulers, or databases.
- **Delivery:** one draft PR for the integrated batch after implementation and automated validation. No merge into B20 or main without owner approval.
- **Device verification:** target Poco M4 Pro 4G remains required for device-dependent capabilities.
- **Project boundary:** JARVIS only; Even untouched.


### B21 implementation update — Existing task module upgrade

- **Code-present:** the existing structured task manager now accepts category (Personal/Work/Study/Home/Health/Other), priority (Low/Normal/High/Urgent), and an optional date; task rows display these fields. Existing `structured_tasks_v1` storage is extended in place, preserving older records with default display values.
- **Validation:** added `B21TaskRules` for title length, allowed category/priority values, and strict optional ISO date validation, with JUnit coverage.
- **Scope count:** 3 of the 50 planned B21 capabilities have an initial code implementation (task category, priority, due date). The other 47 remain unimplemented or require audit; no claim that the full batch is complete.
- **CI:** GitHub Actions runs were triggered for the MainActivity change and the new validation/test files; final conclusions are pending. Device testing on Poco M4 Pro 4G is pending.


### B21 implementation update — 18 integrated capability improvements

**Implemented on the B21 branch (code-present; latest branch CI/device status tracked separately):**

1. Task categories in the existing structured task store.
2. Task priority levels in the same store.
3. Optional task due dates.
4. Completion timestamps and a completed-task history view.
5. Owner-controlled task editing for title, category, priority and due date.
6. Overdue detection and visible overdue labels.
7. Task overview counts for open, completed and overdue tasks.
8. Local task-title search.
9. Daily agenda based on owner-entered due dates.
10. Category filter.
11. Priority filter.
12. Read-only dry-run preview for existing manual workflows.
13. Last-updated timestamp for manual workflow progress.
14. Configurable speech-recognition locale: en-IN, hi-IN, bn-IN.
15. Repeat last spoken response.
16. Stop speech output without cancelling other app features.
17. Editable message preview before handing off to the messaging app; JARVIS does not send.
18. Contact match disambiguation before the existing explicit call confirmation.

**Automated tests added:** task metadata/date validation, overdue detection, workflow preview output, allowlisted speech locales, and message draft validation. CI is triggered per commit; only completed successful runs count as verified. Poco device testing remains pending.

**Scope note:** these are 18 B21 capability improvements/extensions, not a claim that all 50 planned items are complete. Existing B20 capabilities are reused rather than duplicated. Provider-backed sign-in, OCR, and other integrations that require external credentials/dependencies remain gated until their safe configuration and test requirements are established.


### B21 implementation update — additional 5 capabilities

19. Knowledge Graph provenance labels: new nodes/links are marked Owner-entered; legacy records without provenance are explicitly labeled source-not-recorded.
20. Virtual Experiment Notebook: owner can save, review and clear local text-only studies; entries are labeled SIMULATION ONLY and carry a source-verification note.
21. Creative Project Organizer: owner can save, review and clear locally stored creative drafts; drafts are clearly not rendered media files.
22. Image Text Reader: owner-selected images are processed locally with ML Kit; no image/text upload or persistence. Supported scripts are Latin and Devanagari; Bengali is not currently supported.
23. OCR processing releases the recognizer after success/failure; file selection is user-initiated.

The B21 code-present improvement count is now **23**. Automated CI for the newest OCR/dependency changes is pending; earlier individual feature commits have successful Actions runs. Device verification remains pending.


### B21 implementation update — 8 further capabilities (24–31)

24. PDF first-page OCR using Android PdfRenderer and on-device ML Kit.
25. Consent-gated AI explanation for owner-selected image OCR text; only extracted text is sent after a second confirmation.
26. Consent-gated AI explanation for owner-selected PDF first-page OCR text; the PDF itself is not sent.
27. Addressed SMS draft syntax `message to contact-name: text`, with contact choice and editable preview before opening the messaging app.
28. Daily briefing now includes owner-entered task due-today, overdue and urgent counts.
29. Software Project Notebook for locally saved project blueprints, bug analyses and code reviews; drafts are not executed.
30. Learning Notebook for locally saved lessons and roadmaps.
31. Data Insight Report archive for locally saved AI narratives generated from aggregate-only CSV statistics; raw CSV rows are not sent.

**B21 code-present improvement count: 31.** Latest CI for the newest notebook/archive changes is pending. Earlier commits have successful CI; physical device verification is still pending.


### B21 implementation update — 4 further improvements (32–35)

32. Android device-credential re-authentication gate before private notes, tasks, manual workflows, personal memory and privacy settings; no PIN/password is stored by JARVIS.
33. Read-only backend health check against the configured JARVIS health endpoint; sends no user content or credentials and clearly distinguishes endpoint health from a full AI-response test.
34. Selective memory export: choose notes, timeline and/or recent AI context, preview the exact export, then open the Android share sheet.
35. App details now distinguish local installed-package last-update metadata from an unverified store update comparison, with an owner-invoked Play Store listing handoff.

**B21 code-present improvement count: 35.** CI for the latest auth gate, backend health, export and app-store handoff commits is pending. Earlier commits have successful Actions runs; Poco device verification remains pending.


### B21 implementation update — quota reached (36–50)

36. Source-linked Wikipedia topic lookup with provider timestamp and verification caveat.
37. Local pasted CI/build report viewer that never infers missing tests as passed.
38. Read-only public GitHub repository overview with input allowlisting and no token/private-repository access.
39. Guest Mode: private notes, tasks, workflows, memory and privacy exports are hidden until device-credential-authenticated exit.
40. Recent conversation context expires after two hours; existing Clear AI Memory action remains the manual reset.
41. Launch selected installed apps from the app catalogue with unavailable-app handling.
42. Accessibility call controls act only on an allowlisted set of known dialer UI packages; unsupported screens are ignored.
43. Standard Android media-session key controls with honest “request sent” status.
44. Learning Notebook lesson completion status.
45. Individual learning lesson deletion.
46. Creative project rename/review/individual deletion.
47. Software plan review status and individual deletion.
48. Individual data-report sharing and deletion.
49. PDF OCR script selection supports Latin and Devanagari.
50. Manual workflow step-completion timestamps/history plus workflow duplication with a fresh unchecked checklist.

**Code-present quota: 50/50 capability improvements added or extended on B21.** This is not a claim of release readiness. Latest commits still require one clean end-to-end CI run (backend syntax, unit tests, debug APK build and artifact upload) and physical Poco M4 Pro 4G verification. Earlier intermediate failures include missing classes before their follow-up commits and a Maven Central HTTP 429 during ML Kit dependency resolution; inspect the final branch-head run before calling the batch CI-passed.

**Release status:** one integrated B21 branch; draft PR not yet opened; no merge performed. No change to Even.


### B21 final verification and PR state

- **Branch-head CI:** PASS — run `45058627982`, head `d2b04cf85be5f4a0cad816ed8559ad11c51cfa58`.
- Backend JavaScript syntax: PASS.
- Android unit tests: PASS.
- Debug APK build: PASS.
- APK artifact upload: PASS — artifact `jarvis-debug-apk`, ID `14768262216`.
- Draft PR opened: [#47 — B21 integrated 50-feature capability batch](https://github.com/sd2176845-glitch/jarvis-ai-assistant/pull/47).
- PR base is `feature/m17-battery-health-snapshot`; it remains draft, open and unmerged.
- Poco M4 Pro 4G physical install/functional verification: PENDING.
- No merge performed. Do not merge without explicit owner approval.


### B21 verification reconciliation — latest PR head

- Latest PR head: `6196e48794d0807ce4527a6ca0e95a1a04c7af0e`.
- Matching branch-head GitHub Actions run: **45058814748 — SUCCESS**.
- Verified steps: backend JavaScript syntax, Android unit tests, debug APK build, and APK artifact upload.
- Artifact: `jarvis-debug-apk`, ID `14768496836`, approximately 23.2 MB, not expired at last check.
- Draft PR #47 head matches the successful run; PR remains open, draft, clean/mergeable, and unmerged.
- This supersedes the earlier run #45058627982 noted above, whose head SHA was an earlier documentation commit.
- Remaining release gate: physical install and functional checks on Poco M4 Pro 4G. This cannot be certified from CI and must be done on the actual device.


### B21 final scope clarification and remaining master-backlog work

- B21's **50/50 code-present capability-improvement quota** is complete on branch head `6196e48794d0807ce4527a6ca0e95a1a04c7af0e`; it is not a claim that every unchecked item in the full Master Blueprint is complete.
- The Master Blueprint still contains a broad backlog. Its unchecked boxes include capabilities that overlap existing modules, provider-dependent integrations, and device-only acceptance checks; the raw checkbox count is not a unique-feature count.
- **Not implemented because owner setup is required:** Firebase Google/phone OTP sign-in and provider-backed owner profile/session. See `OWNER_AUTH_SETUP.md`; the repository owner must configure Firebase and supply local-only Android configuration. No credentials or config files should be committed.
- **Not physically verified:** Poco M4 Pro 4G install, background wake-word/screen-off behavior, MIUI battery-policy behavior, microphone/TTS language availability, OCR behavior, accessibility/media integrations, and app-specific launch/handoff behavior.
- **Not claimed implemented in B21:** full encrypted Secure Evidence Vault, covert/background evidence collection (not supported), full native live-data integrations that still require provider integration, and unconfigured account connectors.
- Latest final CI: run `45058814748`, exact head `6196e48794d0807ce4527a6ca0e95a1a04c7af0e`; backend syntax, unit tests, debug APK build and artifact upload all passed.
- PR #47 remains draft/open/unmerged. The next valid gate is owner-led physical device verification and Firebase setup where desired; do not mark either complete from CI alone.


## B22 — Owner-controlled reminder scheduler (2026-11-23)

- **Code-present:** extended the existing Tasks & Reminders hub; no duplicate task manager or task store.
- Owners can create one-time reminders with a normalized title (maximum 100 characters) and a future local date/time.
- Reminder records are stored in app-private preferences and scheduled through Android AlarmManager using inexact, Doze-sensitive delivery.
- A non-exported local receiver posts a JARVIS reminder notification with a tap-to-open action.
- A boot/package-replacement receiver restores only future owner-created reminders; expired or invalid records are discarded.
- Notification channel creation occurs while the app is open so Android 13 notification-permission behavior can be presented at the appropriate time. Notification delivery still depends on the owner's Android notification settings.
- Four pure-Java unit tests cover title normalization, empty/overlong title rejection, future-time validation and future-only restoration.
- **CI:** run #871 passed on code head `bb8f97b37e6e4914d1ea2b4e5edc12b2ebd09281`, including backend syntax validation, Android unit tests, debug APK build and artifact upload. Artifact `jarvis-debug-apk`, ID `14787672311` (23.2 MB). Subsequent commits only reconcile documentation; no app code changed after this tested code head.
- An intermediate run #866 failed because it checked an earlier commit snapshot before `ReminderReceiver.java` had been added; later complete snapshots passed. The final-head result is authoritative.
- **Poco M4 Pro 4G physical-device testing:** pending — test notification permission, scheduled delivery, tap-to-open, Doze delay, reboot recovery and app-update recovery.
- **Privacy boundary:** no NotificationListenerService was added; no other-app notification content, SMS content, contacts or background message scraping is read or stored.- Draft PR #48 is open against the B21 feature branch and remains unmerged. Do not merge without explicit owner approval.
- JARVIS only; the separate Even project remains untouched.


## B23 — Private Smart Notification Intelligence (2026-11-23)

- **Code-present:** extends B22's owner-created reminder notifications only; no duplicate reminder scheduler or task store.
- Added owner-controlled quiet hours with local start/end times, including overnight windows.
- A reminder due during quiet hours is deferred using the configured local end time; future-only scheduling continues through the existing ReminderStore.
- Added a 10-minute Snooze action to each JARVIS reminder notification.
- Added pure-Java tests for valid/invalid quiet windows, overnight and same-day quiet-hour detection, next allowed time, and ten-minute snooze calculation.
- **Privacy boundary:** no NotificationListenerService or notification-listener permission; no reading, classification, storage, speech or transmission of notifications from other apps. No new Android permission, backend request or background scraping.
- **Validation:** PASS — GitHub Actions run #883 on code head `00eb6b3d5fe7ad1694edaeddbb24c860fe89b91c`: backend syntax validation, Android unit tests, debug APK build and artifact upload. Artifact `jarvis-debug-apk`, ID `14788276206` (23.2 MB). Subsequent commits only reconcile blueprint/tracker documentation; no app code changed after this tested code head.
- **Poco M4 Pro 4G device verification:** pending — test quiet-hour deferral across midnight, snooze action, notification permission, Doze behavior and reboot recovery.
- Draft PR #49 is open against B22; no merge performed. JARVIS only; Even remains untouched.


## B24 — Smart Asset & Inventory Intelligence MVP (2026-11-23)

- **Code-present:** added to the existing M19 Practical Intelligence Studio; no second hub, no duplicate vehicle log, task store, reminder scheduler or personal timeline.
- Owner can add an item with nickname, category, optional purchase-date label, condition and optional notes; inspect and remove an individual item; or clear only the inventory.
- Separate app-private preference key `owner_asset_inventory_v1`; bounded to 100 entries. Existing vehicle records remain under their original key and are not modified.
- Pure validation rules reject missing/overlong names, categories and condition labels, oversized dates and notes. Added two JUnit tests for valid optional fields and invalid/oversized fields.
- **Privacy boundary:** no serial numbers, vehicle identifiers, exact home location, passwords, receipts, document/camera scanning, background collection, cloud upload or new Android permission.
- **Scope limitation:** this is an MVP, not full Priority 38 completion. Warranty/manual attachments, reminder integration, encrypted export/restore and Poco device testing remain pending.
- **CI: PASS** — GitHub Actions run #890 on app-code commit `4218c25f9b36c38b5918a24f8b2ce1ec6f04e355`: backend syntax validation, Android unit tests, debug APK build and artifact upload all succeeded.
- Artifact: `jarvis-debug-apk`, ID `14787827994`, 23.2 MB; SHA-256 `b408cd6e1de7b1e0679e3cf9a24798cfbf3daeb0719d99e37191d581356a1cc0`.
- Workflow run: https://github.com/sd2176845-glitch/jarvis-ai-assistant/actions/runs/45107225599
- **Device verification:** pending on Poco M4 Pro 4G — add/view/remove/clear flows, persistence after restart, 100-item cap, malformed-data handling and confirmation that vehicle log/tasks/reminders are unchanged.
- Draft PR #50 is open against B23, remains draft and unmerged. JARVIS only; Even untouched.

## Continuation state reconciliation (2026-11-23)

- Current continuation branch: `feature/b24-owner-asset-inventory`, stacked on B23 code head `97daee97951ca9b7fc7e52b20990dc48533f1f01`.
- B22 PR #48 and B23 PR #49 remain open draft PRs; neither has been merged. Their respective CI runs #871 and #883 passed, while Poco device checks remain pending.
- Do not merge B22, B23 or B24 without the owner's explicit approval.

## B25 — Owner-entered Warranty Tracking (2026-11-23)

- **Code-present:** extends B24's existing Smart Asset Inventory; no new inventory screen, storage key or duplicate asset manager.
- Adds an optional warranty end date in strict `YYYY-MM-DD` format to each new inventory record. Older B24 records without this field remain readable and display `Not recorded`.
- Inventory item details show the saved warranty end date and a local-date-derived `Active`, `Expired`, `Not recorded` or `Date unavailable` status. The expiry date itself is inclusive.
- Validation checks real Gregorian calendar dates, including leap-year rules; invalid dates are rejected before saving.
- Added JUnit tests for valid leap days, invalid calendar dates, malformed formats, inclusive expiry-day behavior, expired status and missing/invalid data.
- **Privacy boundary:** date is owner-entered and stored in the existing app-private inventory preference; no receipts, serial numbers, account details, scanning, background collection, cloud upload or new permission.
- **Anti-duplication:** this upgrades B24's inventory model in place; existing vehicle records, task store, reminders and Personal Timeline are not duplicated or modified.
- **CI:** final-head workflow pending. Runs #895 and #896 were triggered on earlier commits in this branch; only a successful run against the final documented branch head may verify the batch.
- **Device verification:** pending on Poco M4 Pro 4G — add/view old and new records, verify status around expiry date, restart persistence, and confirm B24 inventory plus vehicle log/tasks/reminders remain intact.
- Draft PR to be opened against B24 after final-head CI passes. JARVIS only; Even untouched.


## B26 — Integrated 50-capability batch (in progress; 2026-11-23)

- **Branch:** `feature/b26-integrated-50-capability-batch`, created separately from the B25 continuation branch. No merge to main; no change to Even.
- **Anti-duplication audit:** B26 builds on the existing M19 Smart Asset Inventory and B25 warranty field; it does not add another inventory screen, storage key, reminder scheduler, task store or vehicle log.
- **Implemented in code so far (code-present; CI pending):**
  1. Case-insensitive local asset search across item name, category, condition and notes.
  2. Optional category filter matching.
  3. Optional condition filter matching.
  4. Calendar-day warranty countdown, including zero-day expiry and negative expired values.
  5. Warranty-expiring-within-N-days rule.
  6. Warranty-status aggregate counting.
  7. Defensive inventory summary validation.
  8. Export-line sanitization for line breaks and field separators.
- Added JUnit tests for search, filters, warranty countdown/window, warranty counts, summary validation and export sanitization.
- **Not yet integrated into the Android inventory screen:** these are currently pure rules with unit tests; UI wiring and end-to-end behavior remain pending.
- **Verification:** GitHub Actions run for the latest B26 branch head is pending. No CI pass or APK artifact is claimed yet. Poco M4 Pro 4G physical-device testing is pending.
- **Batch target:** 50 distinct capabilities, audited against the consolidated blueprint and existing code. Remaining capabilities must be implemented and tested before calling B26 complete; this progress note is not a 50/50 completion claim.
- **Safety boundary:** owner-entered local records only; no background monitoring, hidden collection, credential storage, private app data access or unapproved external actions.

- **B26 UI wiring:** added owner-invoked local inventory search (name/category/condition/notes) and a read-only warranty overview to the existing Smart Asset Inventory menu.
- **Compatibility review:** warranty countdown uses UTC GregorianCalendar rather than java.time so the helper remains compatible with the app's minSdk 23 declaration.
- **Draft PR:** #53, stacked on `feature/b25-owner-warranty-tracking`; open and unmerged. Latest code commit: `26fc303cbe3cf03ccc349759deb9829cf8f41428`.
- **CI state:** no workflow run/status was returned for the latest head at the time of this update; build/test status is therefore unverified, not passed.


## B26 Next 20 — M19 Smart Asset Inventory extension (in progress; 2026-11-23)

**Branch:** `feature/b26-next-20-inventory`, created from the current PR #53 head `9a5af18ddeb3f8507aea1500305072e15da86928`. This is a draft continuation branch; no merge to PR #53, its base, or `main` is authorized. Even remains untouched.

### Implemented in code (15/20; CI passed, Poco device verification pending)
1. **Owner edit of an existing inventory record** — reuses the existing app-private inventory array; validates all edited fields and preserves the original `created_at` value.
2. **Category filtering** — owner-entered category filter; local/read-only.
3. **Condition filtering** — owner-entered condition filter; local/read-only.
4. **Alphabetical sorting** — case-insensitive name order for display only.
5. **Category/name sorting** — category order with item name as tie-breaker; display only.
6. **Warranty-expiry sorting** — valid ISO warranty dates earliest first; missing/invalid dates last; display only.
7. **Warranty countdown in item details** — displays days remaining, expired days or unavailable state using the existing calendar-day rule.
8. **Owner-approved inventory export** — previews a sanitized text report and opens Android's share chooser only after an explicit Share action; notes, purchase-date labels and internal metadata are excluded.
9. **Category-wise inventory summary** — displays case-insensitive counts from existing local inventory records; blank legacy categories are grouped as Uncategorized and the stored records are not changed.
10. **Possible duplicate asset warning** — checks normalized item name and category before saving; owner can cancel or explicitly add a legitimate second copy.
11. **Condition-wise inventory summary** — displays case-insensitive counts from existing local condition labels; blank legacy values are grouped as Unspecified and records remain unchanged.
12. **Warranty expiry watchlist** — connects the existing expiry-window rule to the inventory UI, listing items expiring within 7, 30 or 90 days without changing stored records.
13. **Warranty-status inventory filter** — upgrades the existing category/condition filter with Active, Expired, Not recorded and Date unavailable options.
14. **Condition/name sorting** — adds a case-insensitive condition sort with item name as tie-breaker to the existing sort menu.
15. **Inventory date search** — extends the existing local search to match owner-entered purchase-date labels and warranty end dates.

### Files changed
- Existing `PracticalIntelligenceStudio.java` upgraded in place; no second inventory page or storage key.
- Added `AssetInventoryBatchRules.java` for deterministic pure rules.
- Added `AssetInventoryBatchRulesTest.java` with focused JUnit tests for filter combinations, case-insensitive sorting, tie-breakers, warranty-date ordering and edit validation.

### Safety and validation
- All operations are owner-invoked and local; no new Android permissions, background collection, external uploads or changes to tasks/reminders/vehicle logs.
- Filtering and sorting do not rewrite stored records. Editing updates the selected existing JSON object in place, preserving `created_at` and any additional metadata.
- **CI/build:** PASS — GitHub Actions run #960 (`45181577757`) on exact head `569ce99f796cf253d9bbf0ce1ffabb8a7949ac3b`; backend syntax, Android unit tests, `gradle assembleDebug`, and APK artifact upload all succeeded. Artifact `jarvis-debug-apk`, ID `14821886641` (23.2 MB).
- **Poco M4 Pro 4G:** physical-device verification pending.
- **CI/build:** PASS — GitHub Actions run #964 (`45184002269`) on exact head `962fbbe5ff653cd3c1933cb271f4d8acab191b22`; backend syntax, Android unit tests, debug APK build and artifact upload all succeeded.
- **APK artifact:** `jarvis-debug-apk`, ID `14821912726`, size 23,243,152 bytes, SHA-256 `75f53e6cd69559deac6619aaf9ed4bb943f0dea15d93b94407ee5779bec0cfcd`.
- **Failure/retest record:** run #962 first exposed export-whitespace normalization gaps; run #963 confirmed the sanitizer fix and exposed one test expectation casing mismatch. Both were corrected. Run #964 passed all workflow steps.
- **CI/build:** PASS — GitHub Actions run #966 (`45184692964`) on exact head `629a6386371b44348d721e58ea1cc5be23672448`; backend syntax, Android unit tests, debug APK build and artifact upload all succeeded.
- **APK artifact:** `jarvis-debug-apk`, ID `14822892952`, size 23,244,892 bytes, SHA-256 `358d8b9a8b82e9e32995f1530aeb3a0dd58291d317c065063961ad0cdfa8ab73`.
- **CI/build:** PASS — GitHub Actions run #968 (`45185344691`) on exact head `f36624c80e6ea31736ce74643a013fc876e13d60`; backend syntax, Android unit tests, debug APK build and artifact upload all succeeded.
- **APK artifact:** `jarvis-debug-apk`, ID `14821159816`, size 23,247,998 bytes, SHA-256 `cf38931248735cc40772300d22ef9050c602585a35a09fe248f1edfbc425544f`.
- **CI/build:** PASS — GitHub Actions run #970 (`45185950203`) on exact head `adef5d93eaae058d4183a00215d6939975ba6546`; backend syntax, Android unit tests, debug APK build and artifact upload all succeeded.
- **APK artifact:** `jarvis-debug-apk`, ID `14823547505`, size 23,248,407 bytes, SHA-256 `418cf6a75058d0ebd80b29efb57f3e5c10df3be3f42192df614342c911dd8ade`.
- **CI/build:** PASS — GitHub Actions run #972 (`45186652899`) on exact head `c491a5faf3f8126dc808f970b895679eaae316cf`; backend syntax, Android unit tests, debug APK build and artifact upload all succeeded.
- **APK artifact:** `jarvis-debug-apk`, ID `14824286479`, size 23,251,197 bytes, SHA-256 `a6d9d4c3db2a65d4e192fb610ade29194a54068c6cd0c251546c90c23d728508`.
- **CI/build:** PASS — GitHub Actions run #974 (`45187390922`) on exact head `4d85b4da1a36861b2d34ff9e9629996cb069fbce`; backend syntax, Android unit tests, debug APK build and artifact upload all succeeded.
- **APK artifact:** `jarvis-debug-apk`, ID `14824232261`, size 23,251,489 bytes, SHA-256 `da1b477dc57f63baf42846ef6ec163353faee1f39c00e9bd2da497d322c93570`.
- **CI/build:** PASS — GitHub Actions run #977 (`45188308932`) on exact head `162d2ccac581f69ea311ace7ada16299640e0131`; backend syntax, Android unit tests, debug APK build and artifact upload all succeeded.
- **APK artifact:** `jarvis-debug-apk`, ID `14824248150`, size 23,252,221 bytes, SHA-256 `e92fc7722d013d069b727057f085b42f6454fc945d80bc832efff14a616ed053`.
- **Failure/retest record:** run #976 exposed one incorrect blank-condition sort test expectation. The comparator's blank-label ordering was retained and the expectation corrected. Run #977 passed the full workflow.
- **CI/build:** PASS — GitHub Actions run #979 (`45188942045`) on exact head `5089769ab97527413fa3c0380abd5be08025c297`; backend syntax, Android unit tests, debug APK build and artifact upload all succeeded.
- **APK artifact:** `jarvis-debug-apk`, ID `14823994641`, size 23,252,508 bytes, SHA-256 `910900708272c5b5ee41851c909ede5007caffaeb3613f444e19be412d80d440`.
- **Progress complete (code-present only):** 20/20 distinct M19 Smart Asset Inventory capabilities are now implemented: original capabilities 1–15, Feature 16 (data-quality audit), Feature 17 (purchase-age summary), Feature 19 (purchase-date sort), Feature 20 (purchase-date range filter), and Feature 21 (owner-selected receipt/manual references). Feature 18 (vehicle odometer consistency check) remains separately tracked under M19 Vehicle Intelligence and is not counted toward the inventory-only 20. Poco M4 Pro 4G physical-device verification remains pending; code-present and CI-pass do not mean device-verified.


### Feature 8 — implementation and verification record
- **Canonical owner:** M19 Smart Asset Inventory; extends the existing owner-entered inventory and preference key.
- **Implementation:** owner opens Export inventory, reviews a scrollable preview, and must press Share before Android displays the system share chooser. The report contains only item name, category, condition and warranty end date; notes, purchase-date labels and internal metadata are excluded.
- **Sanitization:** literal CR/LF/tab characters, escaped line-break sequences and field separators are normalized before export. The export is plain text and no file, network upload or background transfer is created by JARVIS.
- **Tests:** adds unit coverage for actual and escaped line breaks, tabs and field separators using the existing export sanitization helper.
- **Privacy:** external sharing is user-initiated; the preview explicitly discloses that choosing a share target may send the listed information outside JARVIS. Cancel leaves data on-device.
- **CI/build:** PASS — run #964 (`45184002269`) on code head `962fbbe5ff653cd3c1933cb271f4d8acab191b22`; unit tests, debug APK build and artifact upload succeeded.
- **Poco M4 Pro 4G:** device verification pending.


### Feature 9 — implementation and verification record
- **Canonical owner:** M19 Smart Asset Inventory; uses the existing owner-entered asset array and existing Command Center entry.
- **Implementation:** owner can open a read-only category summary showing item counts sorted alphabetically. Category matching is case-insensitive and trims surrounding whitespace; blank legacy categories are grouped as Uncategorized.
- **Data behavior:** calculations run locally, do not persist derived counts, and do not modify inventory records or other modules.
- **Tests:** covers category aggregation, case/whitespace normalization, null/blank legacy categories, alphabetical ordering, and null/empty input.
- **Privacy:** no new permission, background activity, network request, or external sharing.
- **CI/build:** PASS — run #966 (`45184692964`) on exact code head `629a6386371b44348d721e58ea1cc5be23672448`; unit tests, debug APK build and artifact upload passed.
- **Poco M4 Pro 4G:** device verification pending.


### Feature 10 — implementation and verification record
- **Canonical owner:** M19 Smart Asset Inventory; integrated into the existing Add item flow.
- **Implementation:** before saving, JARVIS compares the normalized item name and category with existing local records. A possible match opens a warning; the owner can cancel or explicitly choose Add anyway. Intentional duplicate physical items remain supported.
- **Data behavior:** no existing record is changed, merged or deleted. The inventory remains bounded by the existing 100-item limit and uses its original preference key.
- **Tests:** covers trimmed, case-insensitive exact identity matching and non-matches for different names/categories or missing identity fields.
- **Privacy:** local-only comparison; no new permission, background activity, network request, or external transfer.
- **CI/build:** PASS — run #968 (`45185344691`) on exact code head `f36624c80e6ea31736ce74643a013fc876e13d60`; unit tests, debug APK build and artifact upload passed.
- **Poco M4 Pro 4G:** device verification pending.


### Feature 11 — implementation and verification record
- **Canonical owner:** M19 Smart Asset Inventory; added to the existing inventory menu and existing local asset array.
- **Implementation:** read-only counts by owner-entered condition label. Labels are trimmed, grouped case-insensitively and alphabetically ordered; blank legacy values are shown as Unspecified.
- **Data behavior:** aggregation is calculated locally and not persisted. It does not modify any inventory item.
- **Tests:** covers condition aggregation, case/whitespace normalization, null/blank legacy conditions, alphabetical ordering, and null/empty input.
- **Privacy:** no new permission, background activity, network request or external transfer.
- **CI/build:** PASS — run #970 (`45185950203`) on exact code head `adef5d93eaae058d4183a00215d6939975ba6546`; unit tests, debug APK build and artifact upload passed.
- **Poco M4 Pro 4G:** device verification pending.


### Feature 12 — implementation and verification record
- **Canonical owner:** M19 Smart Asset Inventory; integrates the existing warranty-expiry calculation and existing warranty fields.
- **Implementation:** owner selects a 7-, 30- or 90-day window. JARVIS lists existing items whose valid owner-entered warranty dates fall within that inclusive window, including items expiring today. Expired, missing and invalid dates are excluded.
- **Data behavior:** read-only local calculation; uses original inventory array and existing date rules. No second warranty engine, store, scheduler or reminder system is created.
- **Tests:** covers inclusive window boundaries, same-day expiry, expired/missing/invalid dates, invalid today value, negative windows and null input.
- **Privacy:** no new permission, background activity, network request or external transfer.
- **CI/build:** PASS — run #972 (`45186652899`) on exact code head `c491a5faf3f8126dc808f970b895679eaae316cf`; unit tests, debug APK build and artifact upload passed.
- **Poco M4 Pro 4G:** device verification pending.


### Feature 13 — implementation and verification record
- **Canonical owner:** M19 Smart Asset Inventory; extends the existing Filter inventory dialog rather than creating another filter screen.
- **Implementation:** preserves category and condition filters and adds a warranty-status selector: All, Active, Expired, Not recorded and Date unavailable. Matching items display their calculated current warranty status.
- **Data behavior:** uses the existing warranty status rule and existing local asset records; filtering is read-only and does not persist results.
- **Tests:** covers each status, All selection, case/whitespace normalization, mismatched statuses and unknown selections.
- **Privacy:** no new permission, background activity, network request or external transfer.
- **CI/build:** PASS — run #974 (`45187390922`) on exact code head `4d85b4da1a36861b2d34ff9e9629996cb069fbce`; unit tests, debug APK build and artifact upload passed.
- **Poco M4 Pro 4G:** device verification pending.


### Feature 14 — implementation and verification record
- **Canonical owner:** M19 Smart Asset Inventory; extends the existing Sort inventory action.
- **Implementation:** adds Condition then name sorting. Condition labels are compared case-insensitively; item name breaks ties. Existing name, category/name and warranty-expiry sorting options remain available.
- **Data behavior:** sorts only an in-memory display copy; stored array order and records are unchanged.
- **Tests:** covers condition ordering, case-insensitive comparisons, name tie-breakers, whitespace normalization and blank legacy conditions.
- **Privacy:** no new permission, background activity, network request or external transfer.
- **CI/build:** PASS — run #977 (`45188308932`) on exact tested head `162d2ccac581f69ea311ace7ada16299640e0131`; unit tests, debug APK build and artifact upload passed.
- **Poco M4 Pro 4G:** device verification pending.


### Feature 15 — implementation and verification record
- **Canonical owner:** M19 Smart Asset Inventory; extends the existing local Search inventory workflow and existing asset-query helper.
- **Implementation:** the same search now matches item name, category, condition, notes, owner-entered purchase-date label and warranty end date. The original helper signature remains supported for existing callers.
- **Data behavior:** local read-only matching only; no records are changed and no new store is added.
- **Tests:** covers purchase-date and warranty-date matches, negative date matches, existing field matching, whitespace/case normalization and empty queries.
- **Privacy:** date text remains local; no new permission, background activity, network request or external transfer.
- **CI/build:** PASS — run #979 (`45188942045`) on exact code head `5089769ab97527413fa3c0380abd5be08025c297`; unit tests, debug APK build and artifact upload passed.
- **Poco M4 Pro 4G:** device verification pending.


### Feature 16 — Inventory data quality audit (2026-11-24)
- **Canonical owner:** M19 Smart Asset Inventory; adds a read-only audit action to the existing inventory menu.
- **Implementation:** checks existing records for missing required item name, category or condition fields, and malformed non-empty warranty dates. Optional blank warranty dates are valid. The report identifies affected record positions and fields; it does not expose or transmit record notes.
- **Data behavior:** no record is edited, deleted, reordered or persisted by the audit. It reuses the existing inventory array and Gregorian warranty-date validator.
- **Tests:** covers missing required fields, malformed warranty dates, valid optional blank dates, empty inventory and inconsistent input arrays.
- **Privacy:** owner-invoked and on-device only; no new permission, background work, network request, external sharing or second inventory store.
- **Status:** code-present on `feature/b26-next-20-inventory`; CI/build and Poco M4 Pro 4G physical-device verification pending until a workflow completes on the final branch head.
- **Anti-duplication:** read-only integrity audit, not a duplicate filter, summary, search, sort or edit flow. JARVIS only; Even untouched.


### Feature 17 — Purchase-age summary (2026-11-24)
- **Canonical owner:** M19 Smart Asset Inventory; added as one read-only action in the existing inventory menu.
- **Implementation:** groups existing owner-entered purchase dates by completed calendar age: under 1 year, 1–2 years, 2–5 years and 5+ years. Missing/malformed dates and future dates are reported separately.
- **Data behavior:** uses the existing `purchase_date` field and a local current date; no new preference key, no changes to inventory records, no resale-value or remaining-life inference.
- **Tests:** covers exact year boundaries, just-before-anniversary dates, leap-day handling, malformed/missing/future dates, invalid current date, and null/empty arrays.
- **Privacy:** owner-invoked and on-device only; no new permission, background work, network request or external sharing.
- **CI/build:** PASS — run #1002 on exact code commit `1087af8891a63f67f48328c56aa89f1764a85c2d`; 100 Android unit tests passed, debug APK built and uploaded.
- **Poco M4 Pro 4G:** physical-device installation and runtime verification pending.
- **Anti-duplication:** distinct from warranty status/countdown and the Feature 16 data-quality audit; uses purchase dates only for a read-only age distribution. JARVIS only; Even untouched.


### Feature 18 — Vehicle odometer consistency check (2026-11-24)
- **Canonical owner:** M19 Vehicle Intelligence; extends the existing Local Vehicle Maintenance Log menu.
- **Implementation:** owner-invoked, read-only comparison of numeric odometer readings for the same vehicle nickname, ordered by valid ISO service date. Reports valid readings, comparable chronological pairs and potential decreases for owner review.
- **Date handling:** records on the same date are not ordered because the existing log has no time-of-day field. Missing/malformed dates, blank/malformed readings and blank vehicle nicknames are excluded from comparisons.
- **Data behavior:** uses the existing vehicle maintenance log and existing fields; no new storage, edits, deletes, automatic reminders or remote lookups.
- **Tests:** covers decrease detection per vehicle, chronological ordering, same-day ambiguity, malformed/missing values and null/mismatched arrays.
- **Safety/privacy:** report is a data-entry consistency aid only, not a mechanical diagnosis or safety clearance. No license plate, VIN, insurance or location data is requested; no new permission or network access.
- **CI/build:** PASS — run #1002 on exact code commit `1087af8891a63f67f48328c56aa89f1764a85c2d`; 100 Android unit tests passed, debug APK built and uploaded.
- **Poco M4 Pro 4G:** physical-device installation and runtime verification pending.
- **Review hardening:** duplicate valid readings on the same service date cause that entire date to be excluded from chronology comparisons; report line breaks render correctly.
- **Anti-duplication:** distinct from adding/viewing/removing service records and the Smart Asset Inventory purchase-age/warranty summaries. JARVIS only; Even untouched.


### Feature 17 CI correction — purchase-age boundary expectation (2026-11-24)
- GitHub Actions run #988 exposed one incorrect unit-test expectation, not a compilation failure: the purchase date `2024-11-25` evaluated on `2026-11-24` has completed one calendar year, not two, because its second anniversary is the following day.
- Corrected the expected `1–2 years` bucket count from 1 to 2. Production age-bucketing logic was retained.
- Feature 17 remains unverified until the final-head unit tests and APK build pass.


### Feature 17 CI correction — second age-bucket expectation (2026-11-24)
- The final-head run #994 confirmed the remaining mismatch was the `2–5 years` expected count. The 2024-11-25 purchase date is still one completed year old on 2026-11-24, so that bucket must be zero.
- Corrected the test expectation only; production calculation remains unchanged. The Feature 18 vehicle-audit tests were included in the 99-test run and no failures were reported for them.
- Awaiting a fresh final-head run for all unit tests and APK build.


### Feature 18 review correction — ambiguous duplicate service dates (2026-11-24)
- A deeper code review found that simply skipping adjacent same-day pairs could still compare a later reading against an arbitrary one from a duplicated date.
- Updated the audit to exclude an entire date group whenever a vehicle has multiple valid readings on that date. Comparisons now use only unique-date readings, avoiding arbitrary ordering when the log has no time-of-day field.
- Added a regression test for an ambiguous middle date and adjusted the duplicate-date test expectation.
- Corrected report line-break escaping so the Android dialog displays the report on separate lines.
- CI/build: PASS — run #1002 on exact code commit `1087af8891a63f67f48328c56aa89f1764a85c2d`; physical Poco verification remains pending.


### Feature 19 — Purchase-date sorting (2026-11-24)
- **Canonical owner:** M19 Smart Asset Inventory; extends the existing Sort inventory action rather than adding a second inventory screen or sort menu.
- **Implementation:** adds a "Purchase date (newest first)" display mode using the existing owner-entered `purchase_date` field. Strict valid ISO dates are ordered newest-first; missing or malformed legacy values remain at the end. Values are trimmed for comparison. The result explicitly displays the purchase date alongside the item label.
- **Data behavior:** sorting uses an in-memory display copy only; the stored inventory array and item records are not reordered or modified.
- **Tests:** focused unit tests cover newest-first ordering, older/newer boundary order, missing and malformed dates, whitespace trimming and null-safe behavior.
- **Privacy:** local and owner-invoked; no new preference key, permission, background work, network request or external sharing.
- **Anti-duplication:** distinct from purchase-date text search (Feature 15) and purchase-age buckets (Feature 17); no second date store or summary engine. JARVIS only; Even untouched.
- **Implementation commits:** `c00478bee574002b2083abe52e6e43ed0982fbb2` (rules), `4d416a3027d1a7751342a53d672670e25164bf19` (tests), `68203ddc899974ec454223f91cf75cbd908b1ebe` (existing inventory UI).
- **CI/build:** PASS — GitHub Actions run #1007 (`45350837605`) on exact feature-and-tracker head `6e05964e9ae2848bd562ac9c682c6c9bde443f3e`; backend JavaScript syntax validation, Android unit tests, `gradle assembleDebug`, and APK artifact upload all succeeded.
- **APK artifact:** `jarvis-debug-apk`, ID `14900495794`, size 23,259,097 bytes, SHA-256 `b113fc4b7110a3d0686a40e7c8d5351133cc0beeb1b4882ebdfe2165a5c5c552`.
- **Poco M4 Pro 4G:** physical-device verification pending.


### Feature 20 — Purchase-date range filtering (2026-11-24)
- **Canonical owner:** M19 Smart Asset Inventory; extends the existing Filter inventory dialog and existing `purchase_date` field.
- **Implementation:** adds optional "purchased on/after" and "purchased on/before" boundaries in strict `YYYY-MM-DD` format. Bounds are inclusive and either boundary may be left blank. If both are blank, the purchase-date filter is inactive. Invalid or reversed bounds are rejected. When a range is active, missing or malformed legacy purchase dates do not match.
- **Composition:** date range is combined with the existing category, condition and warranty-status filters using AND semantics; filtered results show the purchase date. No second filter screen was created.
- **Data behavior:** read-only matching over the existing local inventory array; records are not edited, deleted, reordered or persisted.
- **Tests:** focused unit tests cover inclusive boundaries, older/newer exclusions, open-ended ranges, both-blank behavior, invalid dates, reversed bounds, unavailable/legacy purchase dates and whitespace trimming.
- **Privacy:** owner-invoked and local-only; no new preference key, Android permission, background work, network request or external sharing.
- **Anti-duplication:** distinct from Feature 15 date-text search, Feature 17 purchase-age buckets and Feature 19 purchase-date sorting. JARVIS only; Even untouched.
- **Implementation commits:** `40234d17fcde6b5ddfcb56d0b51d3fb9fbbedb63` (rules), `da81ae2f0599b6b1b3d3f4c87a80e60f0e79628a` (tests), `688e906e6ee2715de8a388a18dfeed2a9c5eeb07` (existing filter UI).
- **CI/build:** PASS — GitHub Actions run #1011 (`45351360603`) on exact feature-code head `688e906e6ee2715de8a388a18dfeed2a9c5eeb07`; backend syntax, Android unit tests, `gradle assembleDebug` and APK artifact upload all succeeded.
- **APK artifact:** `jarvis-debug-apk`, ID `14899249936`, size 23,260,104 bytes, SHA-256 `085960ed05ab0b2719ee89b6bd0c1c75eea6fbe79418379cff84d7ba2ee39740`.
- **Poco M4 Pro 4G:** physical-device verification pending.


### Feature 21 — Owner-selected receipt/manual references (2026-11-24)
- **Canonical owner:** M19 Smart Asset Inventory; implements a limited, privacy-preserving slice of the B24/B25 receipt/manual attachment backlog.
- **Implementation:** from the existing inventory menu, the owner selects one saved item and chooses a PDF, JPEG, PNG or WebP using Android's Storage Access Framework `ACTION_OPEN_DOCUMENT` picker. The owner can open, replace or remove that item's reference.
- **Record shape:** stores only the selected content URI, sanitized display name (maximum 100 characters) and MIME type inside the existing inventory JSON record and existing `owner_asset_inventory_v1` preference key. No file bytes are copied, parsed, OCR'd or uploaded. One document is linked to each inventory item; the same URI cannot be linked to a second item.
- **Access controls:** only a persistable READ grant is requested/stored; no WRITE grant or broad storage permission. Opening a file is a separate owner tap and hands the selected URI to an installed viewer. Unsupported providers/formats, missing read grants and invalid URIs fail closed without changing inventory.
- **Lifecycle:** replacing a reference releases the previous read grant only after the new reference is saved. Removing a reference, deleting its inventory item, or clearing the inventory releases saved read access after the inventory update succeeds. Removing a reference never deletes the original file from its provider. Existing edit-in-place behavior preserves the attachment metadata. Inventory export remains allowlisted and excludes document URI/name/MIME.
- **Tests:** focused JUnit coverage checks content-URI-only validation, rejection of file/web URIs, supported MIME allowlist, display-name path stripping and the 100-character bound.
- **Privacy and scope:** owner-invoked and on-device; no new permission, separate preference key, database, background work, scanning, upload, OCR or external sharing. Receipts may contain personal information, so the picker disclosure tells the owner to select only documents they are allowed to access.
- **Anti-duplication:** extends the existing M19 asset record and B24/B25 catalog; it does not add a second document manager, scanner or export system. JARVIS only; Even untouched.
- **Implementation commits:** `01d4b7d427df2c8c1b4b0225ef89f4656c3ba032` (validation rules), `e41a6232ffbb4c82b09bcb77a13c83e038768aaf` (tests), `c318dbce622032c66e351ab9c361a75602f2b685` (inventory UI), `7c9d99aa57f7bc079841b7cc019d70b98af33b47` (activity result routing), and subsequent hardening fixes through `244ecd9a6da109de52744073ac0c40a21aa35868`.
- **CI/build:** PASS — GitHub Actions run #1022 (`45352808375`) on exact code head `244ecd9a6da109de52744073ac0c40a21aa35868`; backend syntax validation, `gradle testDebugUnitTest`, `gradle assembleDebug` and APK artifact upload all succeeded.
- **APK artifact:** `jarvis-debug-apk`, ID `14900039529`, size 23,256,332 bytes, SHA-256 `4ea0da0fea9e00e9c833d4a92a284f43902391dfa3c6b792aeff500f8bb88e55`.
- **CI correction note:** intermediate integration runs #1014–#1021 exposed a checked `JSONException` from the indexed JSON array update in the remove-reference path. The write was guarded and failure now leaves the stored reference and permission intact; final run #1022 passed all workflow steps.
- **Poco M4 Pro 4G:** physical-device picker/open/replace/remove and permission-lifecycle verification remains pending.
- **Priority 38 limitation:** this adds a local reference attachment only. Reminder integration, encrypted export/restore, and full Priority 38/device verification remain pending.


### B27 — Priority 38 warranty reminder integration (2026-11-24)
- **Canonical owner:** M19 Smart Asset Inventory; extends the existing inventory menu and warranty-date records.
- **Implementation:** adds an owner-invoked "Set warranty reminder" action. The owner selects an inventory item with a valid current/future warranty date, then chooses a date and time on or before that warranty end date.
- **Scheduler reuse:** persists and schedules through the existing `ReminderStore`, `ReminderReceiver`, notification channel and snooze action. No second scheduler, reminder preference store, receiver, background collector or new Android permission is introduced.
- **Controls:** scheduling requires a deliberate item selection and date/time choice. Past times and dates after warranty expiry are rejected. If Android notifications are disabled, JARVIS offers a system-settings handoff and does not claim a reminder was scheduled.
- **Failure behavior:** if reminder persistence succeeds but alarm scheduling fails, the newly saved reminder record is rolled back. Existing reminders and inventory records are not modified.
- **Data minimization:** reminder title contains only the inventory item name; notes, receipt/manual URI, provider filename and other metadata are excluded.
- **Tests:** added focused coverage for bounded reminder-title generation and blank names. Runtime alarm/notification delivery still requires device verification.
- **Status:** code-present; CI/build PASS — GitHub Actions run #1032 (`45360490561`) on documented head `4a41e42681b855e2482926ab5d2806727f7a9d89`; Android unit tests, debug APK build and artifact upload succeeded. Poco M4 Pro 4G notification delivery, reboot restoration and snooze behavior remain pending.
- **Anti-duplication:** reuses the already implemented M07/JARVIS reminder infrastructure rather than creating another scheduler. JARVIS only; Even untouched.


### B28 — Encrypted Asset Inventory Backup & Restore (2026-11-24)
- **Canonical owner:** M19 Smart Asset Inventory; upgrades the existing inventory menu and uses the existing `owner_asset_inventory_v1` SharedPreferences key. No duplicate inventory store or backup subsystem was added.
- **Export:** owner selects the destination through Android Storage Access Framework. The versioned `.jva` archive contains only allowlisted item name, category, purchase-date label, condition, warranty end date, notes and creation timestamp. Receipt/manual content URIs, provider filenames, MIME metadata, persisted document permissions, app credentials and unrelated JARVIS data are excluded.
- **Encryption:** AES-256-GCM authenticated encryption with a fresh 16-byte salt, 12-byte IV, 128-bit authentication tag and PBKDF2-HMAC-SHA256 (150,000 iterations). Passphrase length is 12–128 characters; it is held transiently and is not saved. Lost passphrases cannot be recovered.
- **Restore:** selected archive is size-bounded, decrypted and authenticated before JSON parsing; version, required fields, existing inventory validators and the 100-record limit are checked. A preview offers merge (adds only items without an existing same-name/category match) or replace (separate destructive confirmation). Replace releases old persisted document read grants only after the preference update succeeds; original provider files are never deleted. Existing warranty reminders remain unchanged.
- **Failure behavior:** wrong passphrase, altered ciphertext, unsupported header/version, malformed records, oversized files or failed preference commits do not alter the existing inventory. No network transfer or new Android permission is used.
- **Tests:** focused JUnit tests cover round-trip encryption, ciphertext confidentiality, wrong-passphrase rejection, tamper detection and malformed/short-password rejection.
- **CI/build:** PASS — GitHub Actions run #1037 (`45362567473`) on exact final feature-code head `1c4c569220f627c76076bcf90a8e5439c1cf34ea`; backend syntax validation, Android unit tests, debug APK build and artifact upload succeeded. Artifact `jarvis-debug-apk`, ID `14904838906`, size 23,277,086 bytes; uploaded ZIP SHA-256 `ba55fb8809f04496d0606985111d52f13aaf7f73cf5ae51a25230f57f8ee597a`.
- **Poco M4 Pro 4G:** physical-device verification remains pending: create/open archive through document providers, wrong-password/tamper rejection, merge/replace behavior, and document-permission release.
- **Anti-duplication:** extends the existing asset inventory export/menu and preference key; does not create another scheduler, document manager, or data store. JARVIS only; Even untouched.


### B29 — Read-only Life Management Overview (2026-11-24)
- **Canonical owner:** Priority 38 Life Management Intelligence; extends the existing Tasks & Reminders hub with an overview action rather than adding a second planner or dashboard store.
- **Existing stores reused:** structured tasks (`structured_tasks_v1`), owner-created reminders (`jarvis_scheduled_reminders_v1`), manual checklists (`manual_workflows_v1`) and owner-curated personal timeline (`memory_timeline_v1`). No records are copied to a new store or changed by the overview.
- **Summary:** open/completed tasks, overdue tasks, tasks due today and in the next 7 days, malformed non-empty due dates needing review, upcoming reminder counts and next reminder, saved manual checklist count and personal timeline entry count. Shows only the first five due-task lines and indicates additional items.
- **Boundaries:** local read-only aggregation; no network, location, new permission, automatic calendar import, notification changes, task edits or reminder scheduling. Calendar events are explicitly described as not imported or monitored. Guest mode remains blocked from private overview data.
- **Tests:** focused JUnit coverage for due-date buckets, completed/open states, malformed dates, upcoming and expired reminders, empty stores, unavailable optional stores and inconsistent inputs.
- **CI/build:** PASS — GitHub Actions run #1039 (`45363566421`) on exact feature-code head `04c1f861290ed23ef780993f1b23218d80e7a2eb`; backend syntax validation, Android unit tests, Java compilation, debug APK build and artifact upload succeeded. Artifact `jarvis-debug-apk`, ID `14904734084`, size 23,275,421 bytes.
- **Poco M4 Pro 4G:** physical-device verification remains pending for local date/time boundaries, actual reminder entries, the overview refresh action and guest-mode behavior.
- **Anti-duplication:** extends the existing Tasks & Reminders hub and reuses canonical task/reminder/workflow/timeline stores. It does not create a new calendar, planner, task database, timeline or reminder scheduler. JARVIS only; Even untouched.


### B30 — Offline Emergency Preparedness Pack (2026-11-24)
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


### B31 — Vehicle Service Due-Date Tracker (2026-11-24)
- **Canonical owner:** M19 Vehicle Intelligence; extends the existing Local Vehicle Maintenance Log and `vehicle_service_log_v1` records. No second vehicle database or planner was added.
- **Record upgrade:** optional `next_service_due` date in strict real `YYYY-MM-DD` format. Existing service date, vehicle nickname, odometer and maintenance summary fields remain compatible; old records without the new field continue to load.
- **Overview:** read-only local report of due-date records, overdue, due today, next 30 days, later dates and malformed dates requiring correction. Vehicle nickname only is displayed; no licence plate, VIN, policy number or personal identity detail is requested.
- **Boundaries:** this is a family/guardian-managed administrative record, not a mechanical diagnosis, roadworthiness check or driving instruction. No automatic reminder was added in B31; reminder integration remains pending and must reuse the existing JARVIS ReminderStore if added later. No new permission, network request or external vehicle account integration.
- **Tests:** added validation tests for optional and malformed next-service dates, date-window count tests for overdue/today/next-30/later, invalid dates, empty data and mismatched inputs.
- **CI/build:** PASS — GitHub Actions run #1044 (`45365706990`) on exact code head `770e35592394a48905f4efee140e4c6ac5f0713a`; backend syntax validation, Android unit tests, Java compilation, debug APK build and artifact upload succeeded.
- **Artifact:** `jarvis-debug-apk`, ID `14906612856`; `https://github.com/sd2176845-glitch/jarvis-ai-assistant/actions/runs/45365706990/artifacts/14906612856`.
- **Poco M4 Pro 4G:** physical-device verification remains pending for legacy record compatibility, date entry, service due overview and local date boundaries.
- **Anti-duplication:** extends existing vehicle maintenance log and existing date-validation utilities; no duplicate inventory, reminder scheduler or vehicle data store. JARVIS only; Even untouched.


### B32 — Learning Notebook Progress & Revision Planning (2026-11-24)
- **Canonical owner:** Existing AI Learning Academy / `learning_notebook_v1`; expands the current Learning Notebook instead of creating another learning database.
- **Progress states:** each saved lesson can be marked Not started, In progress or Completed. A progress overview counts each state and flags unknown/legacy values for review.
- **Revision planning:** owner can set or clear an optional local `review_on` date using a date picker. The dashboard counts dates due, dates in the next 7 days and malformed values. Older lessons without the new field remain compatible.
- **Boundaries:** local notebook data only; status/revision changes are owner-invoked. Revision dates are planning notes and do not create notifications, a new scheduler, background activity, external calendar events, network calls or new permissions.
- **Tests:** focused JUnit coverage for progress status aggregation, unknown/empty statuses, revision date due/upcoming buckets, impossible dates and unreadable/mismatched data.
- **CI/build:** PASS — GitHub Actions run #1046 (`45366614260`) on exact code head `11dfe5709a80c0ad48ccd9918db6c010e2f322ef`; backend syntax validation, Android unit tests, Java compilation, debug APK build and artifact upload succeeded.
- **Artifact:** `jarvis-debug-apk`, ID `14906797649`; `https://github.com/sd2176845-glitch/jarvis-ai-assistant/actions/runs/45366614260/artifacts/14906797649`.
- **Poco M4 Pro 4G:** physical-device verification remains pending for existing notebook compatibility, saving/reloading progress status, setting/clearing revision dates and local date boundaries.
- **Anti-duplication:** upgrades `learning_notebook_v1` and existing AI Learning Academy menu; no duplicate learning store or scheduler. JARVIS only; Even untouched.


### B33 — Environmental Intelligence expansion (20-capability milestone; started 2026-11-24)
- **Progress:** 1/20 net-new capabilities code-present and CI-verified. Existing manual CPCB AQI observation logging, saved-observation review/summary, category guide and CPCB source handoff are pre-existing baseline features and are not counted as new B33 work.
- **Canonical owner:** existing Priority 38 Environmental Intelligence entry in PracticalIntelligenceStudio; extends the existing EnvironmentIntelligenceStudio and LiveInfoStudio city-search infrastructure. No second environmental dashboard or city-search system is added.
- **Feature 1 — Live modelled air quality by city:** owner enters a city/town, chooses a geocoding match, then requests current modelled U.S. AQI, European AQI, PM2.5, PM10, ozone, nitrogen dioxide, sulphur dioxide and UV index from Open-Meteo Air Quality API. The response includes provider model time and timezone.
- **Index separation:** the result labels U.S. and European indices separately and explicitly warns they are not India's CPCB National AQI scale. The result is a regional model-grid estimate, not a CPCB monitoring-station measurement, official alert or medical advice.
- **Privacy and behavior:** owner-invoked city-only lookup; no GPS, home address, location permission, automatic background polling, saved location, or automatic observation-log write. Existing manual CPCB station log and its local storage remain unchanged.
- **Code:** shared city selection helper now serves both existing weather and new air-quality lookup. Numeric model values are bounded/validated before display; missing or malformed measurements show as unavailable.
- **Tests:** added U.S. AQI band-boundary tests and output-sanitization tests, including distinction from CPCB scale and rejection of malformed pollutant values.
- **CI/build:** PASS — GitHub Actions run #1055 (`45381052956`) on exact code head `8d7a25e20429efe8aeff5b6c64b3e8a1d76561d5`; backend syntax, Android unit tests, debug APK build and artifact upload all succeeded.
- **Artifact:** `jarvis-debug-apk`, ID `14912749355`, size 23,291,488 bytes, SHA-256 `379a41b77de167f1c9b9db1949429cbbf84804fa06c860a831dac7a8548aa79e`; [Actions artifact](https://github.com/sd2176845-glitch/jarvis-ai-assistant/actions/runs/45381052956/artifacts/14912749355).
- **Poco M4 Pro 4G:** physical-device verification remains pending for city matching, network failure, model timestamp display, result layout and confirmation that no location permission or background polling is used.
- **Anti-duplication:** extends the existing city geocoder and environmental menu; manual CPCB station observations remain distinct from modelled Open-Meteo estimates. JARVIS only; Even untouched.
- **Next:** continue with a distinct B33 capability only after reviewing current Environmental and Live Information code; keep each increment separately tested and update this 20-capability count only after implementation.


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


### B33 Feature 3 — 24-hour modelled UV-index forecast (2026-11-24)
- **Progress:** 3/20 net-new Environmental Intelligence capabilities are code-present and CI-verified. GitHub Actions run #1064 (`45386198143`) passed backend syntax checks, all 139 Android unit tests, debug APK build and artifact upload. Artifact `14916741150`; Poco M4 M4 Pro physical-device verification remains pending.
- **Capability:** owner chooses a city using the existing shared city picker and views up to 24 hourly UV-index estimates plus a separately labelled clear-sky reference from the Open-Meteo Air Quality API.
- **Interpretation:** uses WHO Global Solar UV Index exposure bands (Low, Moderate, High, Very high, Extreme). Protection guidance begins at UVI 3; very high/extreme levels are highlighted. This is general public-safety information, not a personal exposure calculation or medical advice.
- **Data limits:** CAMS model-grid estimates are not direct ground measurements. Clear-sky values assume cloud-free conditions and are not presented as expected actual exposure.
- **Privacy:** owner-invoked city-only lookup; reuses existing geocoder/network helper; no GPS, location permission, home address, background polling, saved location or automatic history.
- **Anti-duplication:** a distinct UV outlook, not another air-quality dashboard or AQI forecast; no new data store, location system, scheduler or permissions.
- **Tests:** added JUnit coverage for WHO UV band boundaries, missing/malformed values, sanitized labels and the 24-entry output cap.
- **Poco M4 Pro 4G:** physical-device verification remains pending.


## B33 Feature 4 — 24-hour modelled weather watch outlook (2026-11-24)
- **Progress:** 4/20 net-new Environmental Intelligence capabilities are code-present and CI-verified.
- **Capability:** owner chooses a city using the existing shared city picker and views up to 24 hourly Open-Meteo weather estimates. The output flags thunderstorm/hail, heavy-rain or intense-shower, freezing-rain, snow, high precipitation probability, heavy hourly precipitation, wind/gust, apparent-temperature and low-visibility indicators.
- **Interpretation:** planning thresholds are heuristics, not official IMD warning levels or emergency alerts. A missing flag does not prove conditions are safe. The screen directs users to current local official advisories.
- **Privacy / anti-duplication:** owner-invoked city-only lookup; reuses the existing city picker and network helper. No GPS, location permission, saved location, background monitoring, automatic history or notifications.
- **Tests / CI:** unit tests cover hazard flags, missing/malformed values, label sanitization and the 24-hour cap. GitHub Actions run #1070 (`45392316586`) passed backend syntax, Android unit tests, debug APK build and artifact upload on exact head `15820ec4f928b211f0deabf2073aef1b5f589446`.
- **APK artifact:** `jarvis-debug-apk`, ID `14918882809`, SHA-256 `5789e8fd76cf8ba2a2c1313a2ef953d71053aace2433b9a6e7b5db4606dab811`.
- **Poco M4 Pro 4G:** physical-device verification remains pending.

## B33 prioritization rule (applies to Features 5–20)
- Select the next increment by public-safety importance, source authority and reliability, user impact, then privacy and implementation risk—not by convenience or module numbering.
- First priority after Feature 4: read-only access to official NDMA SACHET disaster alerts, using the published CAP/RSS interface and required ETag/304 caching. Do not present modelled forecasts or heuristic thresholds as official government warnings.
- Next priority tiers: verified environmental observations and flood/heat/pollution preparedness; then useful forecast and historical context; then lower-impact environmental reference features.
- Every feature remains owner-invoked, source-labelled, timestamped and fail-closed on malformed or unavailable data. No background location collection or automatic emergency action.

## B33 Feature 5 — Official NDMA SACHET CAP disaster-alert reader (2026-11-24)
- **Progress:** 5/20 net-new Environmental Intelligence capabilities are code-present and CI-verified.
- **Capability:** added an owner-invoked read-only reader for the All-India NDMA SACHET RSS feed. The owner may optionally enter a state or district keyword; JARVIS filters locally against CAP `areaDesc` values after reading alert details.
- **CAP fields:** issuer/sender, sent time, status, message type, event, urgency, severity, certainty, effective/expiry times, headline, description, official instruction and affected areas, when supplied by the source.
- **Source security:** RSS-provided CAP links are accepted only when they use HTTPS, the exact `sachet.ndma.gov.in` host and the expected `FetchXMLFile` path with an identifier. Untrusted links are skipped.
- **Caching:** CAP XML and ETag are stored in the existing private `jarvis_preferences` store. Subsequent requests send `If-None-Match`; HTTP 304 reuses cached XML, while HTTP 200 replaces the XML and ETag. This follows the NDMA SACHET CAP integration guide.
- **Privacy / limits:** owner-invoked only; no GPS, location permission, background polling, push notification, automatic emergency action, external sharing or location history. The feed viewer is separate from the existing offline emergency-preparedness guide.
- **Tests:** URL allow-listing, source-labelled CAP detail formatting, missing-field handling and text sanitization.
- **CI/build:** PASS — GitHub Actions run #1079 (`45393701084`) on exact head `ed3a0e4286b76b017d6e48b4876b238d6f8a2d72`; backend syntax checks, Android unit tests, debug APK build and artifact upload all succeeded.
- **APK artifact:** `jarvis-debug-apk`, ID `14919538306`, SHA-256 `55478cd5dfa16890da95c4961b8e4ceaf4f7d6cd7902d922c94414bc5b92705a`.
- **Poco M4 Pro 4G:** physical-device verification remains pending.

## B33 priority rule for Features 6–20
- Continue by public-safety importance, source authority/reliability and impact, not convenience or numbering.
- Next candidates must use authoritative, independently verifiable environmental observations or advisories where available; do not present model output or heuristics as government warnings.
- Keep all lookups owner-invoked, timestamped, source-labelled, privacy-preserving and fail-closed on malformed or unavailable data.

## Priority sequence — Feature 1/15: Human-Like Understanding & Natural Conversation Engine (2026-11-24)
- **Canonical owner:** the existing AI conversation pipeline in `JarvisAiClient.ask`, `ConversationContextRules`, the existing `jarvis_ai_memory` conversation preference and the backend `SYSTEM_PROMPT`. This is an in-place upgrade, not a parallel assistant or memory store.
- **Existing behavior preserved:** the current two-hour context freshness rule, assistant modes, conservative Hindi/Hinglish/English speech-transcript correction, standalone memory-isolated requests and local confirmation-aware phone command router remain intact.
- **Upgrade:** cap reused conversation history at the latest 12 non-empty lines and 4,000 characters; cap each stored transcript entry at 1,600 characters; normalize newly stored multi-line turns so user text cannot spoof additional transcript-role lines; append exchanges through the existing conversation key; and synchronize the existing preference update.
- **Conversation behavior:** strengthen backend instructions to carry forward clear goals and constraints, resolve unambiguous short follow-ups, distinguish user-stated facts from assumptions, ask one concise clarification when ambiguity materially changes the answer, recognize informal/code-switched Hindi/Hinglish, resist instruction-like text embedded in pasted/history content, and correct contradictions without exposing private reasoning.
- **Tests:** retain TTL/future-timestamp checks and add coverage for safe line normalization, bounded recent-turn retention, the character cap, prompt context labels, current-message handling and null/empty inputs.
- **CI/build:** PASS — GitHub Actions run #1106 (ID `45404048888`) on exact feature-code head `f5880630b761c35785a2edb9178f05752e60b21a`; backend JavaScript syntax checks, Android unit tests, debug APK build and artifact upload all succeeded.
- **APK artifact:** `jarvis-debug-apk`, ID `14922914686`, size 23,326,773 bytes. Poco M4 Pro 4G physical-device verification remains pending; CI success is not device verification.
- **Anti-duplication / scope:** reuses the existing conversation client, TTL rule and memory key; no second chat engine, memory database, permission path or background collector. No phone action is performed by the text-only chat endpoint. JARVIS only; the separate Even project is untouched.

## Priority sequence — Feature 2/15: Core AI Brain & Reasoning Engine (2026-11-24)
- **Canonical owner:** the existing backend processing tiers, `chooseProcessing`, `runParallelUltra`, Groq completion helper and the existing `server.js` chat endpoint. This is an upgrade to the current AI brain, not a second model router or chat backend.
- **Tier routing:** added a testable rule module that retains the existing Light / Advanced / Ultra tiers and automatic length/complexity routing, adds Hinglish/Hindi complexity cues, honours valid explicit tier overrides and reports a safe reason code without exposing private reasoning.
- **Parallel reasoning:** keeps the existing planner, analyst and reviewer roles; strengthens their bounded tasks to identify goals/constraints, distinguish evidence from assumptions, and flag contradictions and risks.
- **Worker validation:** only known worker roles and successful, non-empty outputs are accepted; duplicate role results are discarded and each worker note is capped at 6,000 characters before synthesis.
- **Synthesis and fallback:** two or more workers use the existing Ultra coordinator; a single surviving worker is synthesized through the existing default model; if all workers or synthesis fail, JARVIS attempts one bounded default-model answer. The response exposes only tier-selection and high-level fallback metadata, never worker notes or hidden chain-of-thought.
- **Timeout and privacy boundaries:** coordinator/fallback timeouts are bounded to remain within the existing Android chat request budget; API credentials remain backend-only; no new Android permission, memory store, background collection or phone-action capability was added.
- **Tests:** added 11 Node tests for simple/advanced/complex routing, Hinglish cues, long prompts, explicit/invalid modes, worker failure filtering, role de-duplication, text caps, private synthesis instructions and malformed inputs. The GitHub workflow now runs `node --check` on both backend files and `npm test` before Android regression tests.
- **CI/build:** PASS — GitHub Actions run #1116 (ID `45405600581`) on exact implementation head `c835b18b600a2c26e9a23d90d1352f3188cd28fe`; backend syntax checks, Node tests (11/11), Android unit tests, debug APK build and APK artifact upload all succeeded.
- **APK artifact:** `jarvis-debug-apk`, ID `14925851000`, size 23,326,769 bytes.
- **Poco M4 Pro 4G:** physical-device/backend live-request verification remains pending; CI success is not device or live-provider verification.
- **Anti-duplication / scope:** reuses the existing Light/Advanced/Ultra processing tiers, existing Groq provider configuration, existing three-worker orchestration and chat endpoint. No second AI provider registry, model router, conversation store or parallel chat engine was created. JARVIS only; Even untouched.

## Priority sequence — Feature 3/15: Local, Global & Hybrid Intelligence Modes (2026-11-24)
- **Canonical owner:** existing `ConnectivityIntelligenceStudio`, `ConnectivityIntelligenceRules`, `jarvis_preferences` and `JarvisAiClient` chat / standalone / speech-understanding entry points. This extends the existing Connectivity Intelligence module rather than adding another network manager, AI client or data store.
- **Owner-selected modes:** added Local, Global and Hybrid choices to the existing Connectivity Intelligence menu. The selection is stored under the existing private `jarvis_preferences` key `ai_connectivity_mode`; invalid or missing values safely default to Hybrid.
- **Local mode:** blocks cloud AI requests from `JarvisAiClient.ask`, `askStandalone` and `understandCommand`. Speech text is passed directly to the existing local command router. The UI and response clearly state that this build has no on-device generative language model; supported local app functions remain available where Android permits.
- **Global mode:** preserves cloud-first AI chat, standalone AI prompts and speech correction. Provider or network failures are reported rather than being presented as an offline model answer.
- **Hybrid mode:** tries the existing cloud AI service first; if it fails, it returns a limited offline capability explanation and does not claim to have answered with a local LLM. Fallback text is not stored as a user conversation memory entry.
- **Scope disclosure:** the setting applies to AI text requests routed through `JarvisAiClient`. Dedicated public-data lookup studios remain separately owner-invoked internet lookups with their own disclosures and confirmation.
- **Tests:** expanded `ConnectivityIntelligenceRulesTest` for supported/default/invalid mode normalization, cloud gating, mode disclosures and honest local/hybrid fallback language.
- **CI/build:** PASS — GitHub Actions run #1123 (ID `45406921180`) on exact implementation head `9494689010607ebca31985fd87da612df1ca9dc1`; backend syntax, 11 Node reasoning tests, Android unit tests, debug APK build and artifact upload all succeeded.
- **APK artifact:** `jarvis-debug-apk`, ID `14926097021`, size 23,324,303 bytes.
- **Poco M4 Pro 4G:** physical-device tests remain pending, especially switching modes, confirming Local-mode requests never reach the backend, Global failure messaging and Hybrid fallback behavior.
- **Anti-duplication / scope:** upgrades the existing M17 Connectivity Intelligence menu and current `JarvisAiClient`; reuses the existing private preferences file and local command router. No new Android permission, background connectivity monitor, offline LLM claim or second data store. JARVIS only; Even untouched.

## Priority sequence — Feature 4/15: Independent Connectivity — Live Network State Monitor (2026-11-24)
- **Canonical owner:** existing M17 `ConnectivityIntelligenceStudio` and `ConnectivityIntelligenceRules`; extends the existing Network Health Snapshot rather than adding a duplicate network dashboard.
- **Capability:** added an owner-opened Live Connectivity Monitor. It registers an Android network callback, refreshes the active default-route snapshot when Android reports availability/loss/capability changes, and shows the reported transport, INTERNET capability, Android validated-internet state and metered state.
- **Local-only interpretation:** if Android reports an active network without the INTERNET capability, JARVIS labels it as a *potential local-only route*. It explicitly says that peer/server reachability has not been tested; no local peer discovery or connection is attempted.
- **Foreground and lifecycle:** monitoring stops when the dialog closes and is also unregistered from `MainActivity.onPause` when JARVIS leaves the foreground. No background monitor or persistent service was added.
- **Privacy / permissions:** uses the existing `ACCESS_NETWORK_STATE` declaration. No new permission. Does not scan nearby Wi-Fi networks, read SSIDs/passwords/IP/MAC identifiers, inspect local devices, change settings or initiate network connections.
- **Tests:** added JUnit coverage for active local-only route wording, unvalidated internet versus no-network states, capability-unavailable handling, and the absence of credential/SSID output fields.
- **CI/build:** PASS — GitHub Actions run #1130 (ID `45408537407`) on exact implementation head `98461a97427d1400560bab38a7d69348980a81b7`; backend syntax, 11 Node tests, Android unit tests, debug APK build and artifact upload all succeeded.
- **APK artifact:** `jarvis-debug-apk`, ID `14926853322`, size 23,320,689 bytes.
- **Poco M4 Pro 4G:** physical-device verification remains pending for Wi-Fi/mobile/VPN transitions, local-only network behavior, monitoring stop on close/background, and API 23 callback fallback.
- **Anti-duplication / scope:** extends the existing Connectivity Intelligence / Network Health Snapshot and reuses the existing permission. This is a foreground monitor of Android-reported default connectivity, not a private mesh network, peer discovery, LAN file transfer or independent internet service. Those require separate, explicitly authorized hardware/protocol implementations. JARVIS only; Even untouched.

## Priority sequence — Feature 5/15: Adaptive Network-Aware AI Failover (2026-11-24)
- **Canonical owner:** existing `JarvisAiClient`, `ConnectivityIntelligenceRules`, the saved Local/Global/Hybrid mode in the existing `jarvis_preferences`, and the current `ACCESS_NETWORK_STATE` permission.
- **Cloud route decision:** before AI chat, standalone AI prompts or speech-transcript correction are sent, JARVIS checks Android's active default network. Local mode continues to block cloud requests. Global and Hybrid only proceed when a default network exists and Android reports the INTERNET capability.
- **Unknown-state behavior:** if Android cannot provide capability details, or access to network-state information is unavailable, JARVIS fails open and allows the existing bounded HTTP request. An INTERNET capability is treated as a route hint, not proof that the JARVIS provider is reachable; an unvalidated internet-capable route is still allowed to attempt the request.
- **No-route behavior:** Hybrid and Global return mode-specific, truthful offline/network messages immediately without opening a cloud request. Speech correction returns the original transcript to the existing local confirmation-aware command router.
- **Privacy / permissions:** reuses the existing network-state permission and existing preferences; no new permission, persistent listener, background service, network scan, credential collection, network connection attempt or local data store.
- **Tests:** added JUnit coverage for Local-mode blocking, missing active route, active local-only route, internet-capable route, unknown-capability fail-open behavior, and mode-specific no-internet responses.
- **CI/build:** PASS — GitHub Actions run #1135 (ID `45409604272`) on exact implementation head `3d1d2cebce48e70e9ee25be1357879885c35e734`; backend syntax, 11 Node tests, Android unit tests, debug APK build and artifact upload all succeeded.
- **APK artifact:** `jarvis-debug-apk`, ID `14926233271`, size 23,321,180 bytes.
- **Poco M4 Pro 4G:** physical-device tests remain pending for Local/Global/Hybrid routing with Wi-Fi disconnected, mobile data active, local-only Wi-Fi, captive portal/unvalidated network and Android capability-state unavailable cases.
- **Anti-duplication / scope:** extends Feature 3's existing Local/Global/Hybrid mode and reuses Feature 4's Android network-state foundation. It does not add another connectivity monitor, scheduler, AI client, local model or memory store. JARVIS only; Even untouched.

## B26 — 50-feature Batch 1 (in progress)

- **Batch target:** 50 distinct new capabilities or meaningful extensions; progress is tracked individually. The batch is not complete until all 50 are implemented and the combined regression/build checks pass.
- **Feature 1/50 — Warranty Reminder Integration:** added an owner-invoked “Set reminder” action to an inventory item with an active warranty. It reuses the existing B22/B23 ReminderStore, AlarmManager delivery, quiet-hours handling and snooze action; no second scheduler or reminder database is created.
- The owner chooses a local date/time; the date picker is capped at the warranty expiry date. A reminder must be in the future and no later than the inclusive warranty-expiry day. The default suggestion is one day before expiry at 9 AM when possible.
- The reminder title is normalized and bounded; only the item nickname and chosen reminder time are stored in the existing reminder store. No external sharing, cloud upload, background inventory scan or new Android permission.
- Added a pure-Java unit test for reminder-title normalization and bounds.
- **Branch:** feature/b26-50-feature-batch-1, based on B25; draft PR and CI pending.
- **Verification:** branch-head CI PASS on commit `788f6be832051bf9d979a6799709e4804e5018a6` (unit tests, debug APK build and artifact upload); Poco M4 Pro 4G physical-device testing remains pending.
- **Anti-duplication:** extends B24/B25 inventory details and reuses the existing reminder scheduler; no duplicate inventory, task, reminder or timeline module.
- JARVIS only; Even untouched. No merge without explicit owner approval.
## B26 — Feature 2/50 — Owner Reminder Manager

- Added “Manage reminders” inside the existing Tasks & reminders hub; no second reminder hub or storage location.
- Lists valid upcoming JARVIS-created reminders in scheduled-time order and shows each title and local date/time.
- Owner can inspect a selected reminder and explicitly confirm cancellation. Cancellation removes only that JARVIS reminder's saved record, matching Android alarm and its notification.
- Added pure-Java validation coverage for stored reminder records. Malformed/expired records are left untouched by the read-only list operation.
- No access to other apps' notifications, no new permissions, no cloud upload or background collection.
- **Status:** CI PASS on final Feature 2 head `01a49811edd243f15110bdaa798c96a2d8174f3d`, workflow run #918 (unit tests, debug APK build and artifact upload); artifact `jarvis-debug-apk`, ID `14790574055`, SHA-256 `f59c21d944eb541b1af5c5bf9816e88df78fc16258e34078063c1318e8a4db40`. Poco M4 Pro 4G physical-device testing remains pending.

## B26 — Feature 3/50 — Owner Reminder Rescheduling

- Added a “Reschedule” action to the existing reminder details view; it uses the same reminder ID, local record, AlarmManager and receiver.
- Owner selects a new local date/time; past times are rejected. On scheduling failure, JARVIS attempts to restore the previous saved time and alarm.
- No duplicate reminder manager, scheduler, database, permission, other-app notification access or cloud upload.
- **Status:** CI PASS on commit `38a68b7408231c89298505a610517b64f14ab7f2`, workflow run #921 (Android unit tests, debug APK build and artifact upload); Poco M4 Pro 4G physical-device testing remains pending.


## B26 — Feature 4/50 — Duplicate Reminder Protection

- Prevents creating or rescheduling a JARVIS reminder when another owner-created reminder has the same normalized title (case-insensitive) and exact local trigger time.
- Reuses the existing ReminderStore; duplicate lookup is read-only and leaves malformed/older records unchanged.
- Rescheduling excludes the reminder's own ID, so keeping the same reminder details remains allowed.
- Added pure-Java unit coverage for matching title/time, same-ID exclusion, different time and different title.
- No new permission, second scheduler, cloud upload, access to other apps' notifications or background collection.
- **Status:** code added on B26 branch; final-head CI and Poco M4 Pro 4G physical-device testing pending.

## B26 — Feature 5/50 — Reminder Search & Filtering

- Upgraded the existing Manage reminders view with an owner-controlled, live title search; matching is case-insensitive, whitespace-normalized and local-only.
- Search filters only the already-loaded upcoming reminders. It does not alter stored reminders, scheduling, notification state or reminder ordering.
- Selecting a filtered result continues to use the existing details, reschedule and explicit cancellation actions.
- Added pure-Java tests for case-insensitive partial matching, whitespace normalization, empty queries and non-matches.
- No second reminder manager, scheduler or store; no new permission, external notification access, cloud upload or background collection.
- **Status:** code added; final-head CI pending; Poco M4 Pro 4G physical-device verification pending. JARVIS only; Even untouched.


## M13 / M14 source reconciliation — 2026-11-27
- The current default-branch source already contains `ProductivityStudio.java` (M13 Personal Productivity Studio) and `DiagnosticsStudio.java` (M14 Diagnostics Studio).
- `MainActivity.java` already routes the Command Center productivity entry to `ProductivityStudio.show(...)` and the existing System safety check entry to `DiagnosticsStudio.show(...)`. This reuses the existing navigation and avoids duplicate menus or duplicate modules.
- Older tracker/feature-branch notes that describe these modules as unmerged are historical and must not be used to justify cherry-picking their stale branches over the current default branch.
- **Current status:** source presence and entry-point integration confirmed; a fresh CI build for the current default-branch head and Poco M4 Pro 4G verification are still pending. No claim of a newly built APK or device test is made.


## B26 — Feature 6/50 — Reminder Time-Window Overview (2026-11-27)

- Extended the existing Manage reminders view with a live local-calendar summary for Today, Tomorrow, This week and Later. Counts update together with the existing title search.
- Calendar buckets use the device's local calendar and calendar-day arithmetic, not fixed 24-hour windows, so daylight-saving transitions and month/year boundaries do not shift the labels.
- Reuses the existing ReminderStore and Manage reminders screen. The summary reads only the already-loaded upcoming JARVIS reminders; it does not create or mutate records, alarms, notifications or reminder settings.
- Added pure-Java unit coverage for Today, Tomorrow, This week, Later and Past boundaries.
- No new permission, scheduler, data store, network request, background process or access to other apps' notifications.
- **Code commit:** `f43405853784edbb78556f3548209e3c67fc5e00`. GitHub Actions run #1305 is running; Android unit-test and APK-build result not yet confirmed. Poco M4 Pro 4G physical-device verification remains pending.


## M15 / M16 — Dashboard Profiles & Integration Registry (2026-11-27)

- **M15 — Dashboard Profiles:** added owner-selectable Compact, Standard and Spacious layouts to the existing dashboard. The selected spacing profile persists in the existing private `jarvis_preferences` store and is applied at app startup; the selector changes only the existing dashboard container padding. It does not change Android display density or system settings.
- Added pure-Java tests for profile spacing order and invalid-profile fallback.
- **M16 — Integration Registry:** added an owner-controlled registry for the existing Smart Home Studio and Connected Systems entry points. Each entry displays its declared scope and can be enabled/disabled locally; disabled entries are blocked when selected from the existing Command Center.
- Registry state reuses `jarvis_preferences`; defaults preserve the current enabled behavior. It does not install or execute third-party plugins, request new Android permissions, scan devices, pair accessories or grant remote access.
- Added pure-Java tests for stable, distinct registry keys.
- **Integration:** appended two entries to the existing Command Center; existing menu entries and prior features remain in place.
- **Code commit:** `277bb06ab3955eeee50ddb2b89eeccd855ccd6a8`.
- **CI/build:** GitHub Actions run # pending for this commit; final Android unit-test and APK-build result not yet confirmed.
- **Physical device:** pending on Poco M4 Pro 4G; verify all three dashboard profiles and restart persistence, plus disabling/re-enabling each registered integration.
- JARVIS only; Even untouched.

## 2026-11-27 — M01–M19 batch-to-blueprint reconciliation

**Audit basis:** current main source tree, Command Center dispatch in MainActivity.java, available focused JUnit test files, JARVIS_MASTER_BLUEPRINT_CONSOLIDATED.md, and the historical batch records below. “Code-present” is not the same as full blueprint completion or Poco device verification.

### Small feature batches B05–B20

- **B05 / M05 Public Knowledge:** PublicKnowledgeStudio.java is present and connected to the Command Center; country, India PIN-code and dictionary lookup batch remains a defined three-tool slice, not proof that every M05 research capability is complete.
- **B06 / M06 Live Information:** LiveInfoStudio.java is present and connected; weather, currency and public-holiday lookups are implemented as the recorded batch scope.
- **B07 / M10 Learning Academy:** LearningAcademyStudio.java, LearningAcademyRules.java and focused tests are present; coding tutor, science explorer and learning-roadmap batch is represented.
- **B08 / M11 Software Development:** SoftwareDevelopmentStudio.java is present and connected; project architecture, bug explanation and code-review tools are the recorded batch scope.
- **B09 / M12 Data Intelligence:** DataIntelligenceStudio.java is present and connected; local dataset profiling, data-quality inspection and aggregate insight report are represented.
- **B10 / M13 Productivity:** ProductivityStudio.java is present and connected; project workspace, priority matrix and weekly review extend the existing task store rather than creating a second task database.
- **B10–B11 / M13–M15 Creative Media:** CreativeMediaStudio.java is present and connected; image/video concept, script/storyboard and presentation-planning tools are present as planning workflows, not claims of finished media rendering.
- **B11 / M16 Smart Home:** SmartHomeStudio.java is present behind the existing integration-registry enable check; setup guidance, local inventory and routine planning do not imply unattended device control.
- **B12 / M17 Connectivity:** ConnectivityIntelligenceStudio.java and its tests are present and connected; snapshot, offline capability matrix and owner-confirmed strategy planning are represented.
- **B13 / M18 Simulation:** SimulationEngineeringStudio.java and its tests are present and connected; text-only world simulation, safe conceptual experiment and 3D design brief are represented, not physical actuation or CAD output.
- **B14 / M19 Practical Intelligence:** PR #40 is merged into main. MainActivity dispatches the existing Practical Intelligence Studio; emergency readiness, legal excerpt explanation and local vehicle maintenance log are present.
- **B15 / M01 Privacy:** OwnerPrivacyStudio.java, OwnerPrivacyRules.java and tests are present; the permission review, local data inventory and sanitized export are the recorded batch slice.
- **B16 / M02 Voice Transcript Review:** VoiceTranscriptReviewRules.java is present and used by the owner-review correction flow in MainActivity; this is not permission to silently replace transcripts.
- **B17 / M15 Display & Accessibility:** DisplayAccessibilityStudio.java, rules and tests are present and connected; CI run #1306 passed. Poco physical-device verification remains pending.
- **B18 / M16 Authorized Bluetooth Handoff:** SmartHomeAccessoryRules.java and the existing Smart Home inventory/settings handoff are present; CI run #1306 passed. No background scanning or automatic pairing is implied.
- **B19 / M17 Hardware Compatibility:** HardwareCompatibilityRules.java is present and used by the read-only device report; this is not a guarantee of third-party accessory compatibility.
- **B20 / M17 Battery Snapshot:** BatterySnapshotRules.java is present and used by the read-only resource report; it is an Android-reported snapshot, not a battery-health diagnosis.

### M19 expansion reconciliation

- The Smart Asset Inventory implementation is recorded as **20/20 inventory capabilities code-present**, including search/filter/sort, warranty summaries, purchase-date analysis, receipt/manual references, and data-quality checks. Vehicle odometer consistency is separately tracked under Vehicle Intelligence.
- Warranty reminders reuse the existing M07 reminder scheduler. Encrypted inventory backup/restore uses the existing inventory record and authenticated encryption. Offline emergency guidance, environmental information and vehicle service due-date reporting are also present in the current source tree.
- Historical PR #53 and PR #54 are closed without merge; do not treat those PR states as the source-of-truth for current source presence. Verify the current main source and tests before proposing any cherry-pick or duplicate.
- **Blueprint gaps identified:** an owner-reviewed journey-planning workflow and a collaborative project/milestone planning workflow were not exposed by the existing M19 studio. Added both to the existing PracticalIntelligenceStudio on feature/m19-blueprint-completion / draft PR #71. Journey planning is a draft only, with guardian review for minors and no bookings, purchases or live-route claims. Project planning drafts role-based responsibilities and owner-editable milestones, but does not invite people, sync data or share private memory; approved tasks use the existing Tasks & Reminders / Project Workspace.
- PR #71 merged to main as `d7ad95a70c64d32aee41975f6c7b326cf596c62e`; its journey-planning and collaborative project-planning foundation is code-present. It still does not invite people, sync private data or perform bookings/purchases.
- Main-branch baseline commit b25441fc518a0b708e5fc9a8af063dcff8f811b1 passed run #1316 (45966559793), including Android unit tests and debug APK artifact upload.
- **Still not claimed complete:** the full M19 shared-collaboration network/sync, actual booking or purchasing actions, and any other unchecked M19 blueprint requirements. The target Poco M4 Pro 4G physical-device verification remains pending for the audited modules and the new M19 additions.

JARVIS only; the separate Even project remains untouched.


## M03 — Academic Paper Explorer mainline integration (2026-11-27)

- Ported the previously CI-verified curated arXiv Academic Paper Explorer onto the latest main branch after the older feature branch was found to be 30 commits behind and diverged.
- Reuses the existing Public Knowledge Studio and its existing owner-invoked public lookup flow; adds a curated safe-topic picker, bounded request (maximum five metadata/abstract records), source links, authors, publication/update dates, and local retrieval timestamp.
- Parser rejects DOCTYPE/external entities, limits response size and abstract length, validates canonical arXiv links, caps results, and labels records as preprints rather than peer-reviewed conclusions.
- Sends only the selected topic to arXiv; no contacts, conversation history, notes, precise location, PDF download or saved search history.
- Added focused JVM parser/query validation tests. Exact-head PR CI run #1367 passed backend tests, Android unit tests, debug APK build and artifact upload; PR #75 merged as `00ffdb42ea8c6765ce641e82b09afc151b76024a`. Poco M4 Pro 4G physical-device verification remains pending.
- This is a focused M03 research slice, not completion of all M03 knowledge/search/research/learning requirements. JARVIS only; Even untouched.


## M01 / M02 / M06 / M13 / M14 / M16–M19 current-mainline reconciliation (2026-11-27)

**Mainline status:** merged to `main` by PR #76 as `f346fe2b7d66f9281d03515c0f58c5c1eed8d1df`. The branch ported reviewed work from stale/diverged feature branches and added the missing enforcement/recovery/handoff code without merging those stale branches.

### Code added or corrected

- **M01 — Consent enforcement:** added explicit owner-consent gates to foreground and background voice entry points, contact lookups (including call, SMS, email and WhatsApp recipient selection), incoming call/SMS announcements, local diagnostics and outgoing SMS sending. Android runtime permissions remain separate. Incoming SMS announcement speaks only that a new message arrived; it does not read message text or OTPs. Outgoing SMS has its own default-off consent, exact recipient/message confirmation and Android `SEND_SMS` runtime permission.
- **M02 — Human Intelligence:** integrated the focused text-only cue classifier, tentative supportive responses and prompt guardrails for empathy/social-context uncertainty. No raw audio/camera emotion analysis or persistent emotion labels.
- **M03 — Academic Research:** PR #75 merged to main as `00ffdb42ea8c6765ce641e82b09afc151b76024a`; curated arXiv metadata/abstract lookup is present. This is not full M03 completion.
- **M06 — Communication:** added reliable original-transcript addressed SMS parsing; editable email and WhatsApp draft handoffs with recipient choice; and owner-confirmed direct SMS via Android SmsManager. SMS requires separate owner consent, final recipient/message confirmation and runtime permission. Carrier submission is not delivery confirmation. No email or WhatsApp auto-send.
- **M13 — Secure Evidence Vault:** added an integrity-verification action that decrypts the selected AES-GCM file using the existing Android Keystore key and checks the plaintext SHA-256 and byte count against local metadata. Missing keys do not trigger creation of a replacement key during verification. Invalid entry IDs are blocked. This is not legal chain-of-custody certification.
- **M14 — Diagnostics & recovery:** added a local checkpoint/restore/rollback workflow for a strict allowlist of JARVIS voice/display preferences only. It excludes Android system settings, permissions, guest/security state, credentials, contacts, notes, tasks and memory. Type validation is enforced before restoring.
- **M16 — Integration Registry:** added an owner-triggered shareable setup summary containing only built-in integration states and declared scopes. It is a manual handoff, not device sync or remote access.
- **M17 — Connectivity:** added a read-only readiness advisor based on Android network capability flags; it does not probe networks, run speed tests or alter settings.
- **M18 — Simulation:** added a text-only conceptual scenario comparison with safety boundaries; no physical equipment control or robotics actuation.
- **M19 — Practical Intelligence:** added an owner-selected share option for collaborative project-plan drafts. It does not invite users or sync private data.

### CI corrections and validation boundaries

- Early integration runs failed while the M16 share handoff was being wired (missing `Intent` import and a status variable outside its scope); both source errors were corrected.
- An intermediate M14 build failed while the allowlist API was being moved into the shared recovery rules class; the class and call sites were reconciled and subsequent runs passed.
- Exact PR-head CI passed in run #1443 (ID `46049760374`) on `f3d91ad0723c90ae36c09264466e381f8ace920b`.
- Post-merge main CI passed in run #1445 (ID `46049961444`) on `f346fe2b7d66f9281d03515c0f58c5c1eed8d1df`.
- Poco M4 Pro 4G physical-device testing remains pending. SMS sending, Android Keystore decrypt verification, Bluetooth settings handoff and UI recovery flows still require on-device verification.

### Explicitly not implemented

- Live M16 device/collaboration sync, M17 private wireless or satellite-provider service, M18 physical robotics hardware control, and M19 real booking/payment transactions remain provider-, account-, hardware- or permission-dependent. The current code offers safe local/manual handoffs only; it does not claim those external integrations are complete.
- JARVIS only. The separate Even project remains untouched.


## M02 — Negation-aware text cue correction (2026-11-28)

- **Branch:** `fix/m02-negation-aware-human-cues` (based on current `main`; not merged).
- Corrected the existing text-only Human Intelligence cue matcher so a negative word immediately following a matched cue (for example, “bahut khush nahi hoon”) is not misread as a positive emotion signal.
- Preserved independent positive cues later in the same sentence (for example, “bahut khush hoon, lekin tension nahi hai”).
- Added JVM unit tests for post-cue negation, mixed positive/negative clauses, and preventing an incorrect supportive reply.
- **Validation:** source and tests are committed on the feature branch; GitHub Actions result pending. No claim of passing tests or device verification until the exact branch-head workflow completes.
- **Scope:** text/transcript only. No raw audio/camera inference, persistent emotion labels, new permissions, or changes to Even.
