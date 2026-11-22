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
- **CI/build:** pending on this branch. Android unit tests/lint and Poco M4 Pro 4G device verification are not claimed.
- **Merge policy:** keep stacked on PR #28; do not merge to parent PR #28 or `main` without owner approval.
