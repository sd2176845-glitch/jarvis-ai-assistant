# JARVIS Implementation Tracker

## Project boundaries
- This repository is the separate JARVIS assistant project.
- Do not modify the user's separate Even assistant project.
- Preserve Android permission boundaries, user-visible controls, and confirmation for sensitive external actions.
- Blueprint inclusion, code presence, automated validation, and physical-device verification are separate statuses.

## Current working branch
- Branch: `feature/m01-owner-privacy-studio-3pack` (stacked on M19 Personal & Practical Intelligence / PR #40; JARVIS only).
- Parent draft PR: #37 — M16 Smart Home Studio. PR #37 remains open and unmerged.
- Parent draft PR: #39 — M18 Simulation & Engineering Intelligence. New M19 batch is to be opened as draft PR #40 against `feature/m18-virtual-engineering-studio-3pack`.
- `main` has not been changed or merged by this work; Even remains untouched.

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
- **Code-present:** read-only status summary for microphone, contacts, phone state, direct calling, SMS receipt, Android notifications (where runtime permission applies), display-over-other-apps and JARVIS accessibility service.
- **Controls:** links to Android App Info and Accessibility settings. The screen never requests, grants, revokes or changes permissions.
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

**Branch:** `feature/m15-accessibility-display-studio-3pack`, stacked on M02 Voice Transcript Review / PR #42. Draft PR targets `feature/m02-voice-transcript-review-3pack`; unmerged.

### Feature — Owner-controlled readability profiles
- **Code-present:** adds a single Display & Accessibility Studio entry to the existing Command Center. Owner can choose Standard, Large text, Extra-large text, High contrast, or Reset to standard.
- Applies app-local text sizing and readable foreground colors to the existing JARVIS status and response text; the selected profile persists in the existing private `jarvis_preferences` store.
- The profile is applied on app startup and after an owner selection. It does not change Android system font/display settings, alter screen density, hide controls, or add permissions.
- **Anti-duplication:** extends the existing M15 Command Center and existing status/response views; no duplicate dashboard, settings page, or accessibility service.
- **Automated tests:** three JUnit tests cover profile sizing, high-contrast selection, and invalid-profile fallback.
- **Physical device:** pending on Poco M4 Pro 4G. Check all profiles, restart persistence, contrast in sunlight/darkness, text clipping and screen-reader announcements.
- **CI/build:** pending; do not mark as passed until the workflow completes.
- **Merge:** draft PR only; no merge into PR #42 or `main` without owner approval. JARVIS only; Even untouched.


## Batch B18 — M16 Authorized Bluetooth Accessory Handoff (2026-11-22)

**Branch:** `feature/m16-authorized-accessory-hub-3pack`, stacked on M15 / PR #43. Draft PR targets `feature/m15-accessibility-display-studio-3pack`; unmerged.

### Feature — Owner-controlled Bluetooth accessory setup
- **Code-present:** upgrades the existing Smart Home Studio inventory menu with an owner-entered Bluetooth accessory label flow and a direct handoff to Android Bluetooth settings.
- Bluetooth labels reuse the existing local smart-home inventory and its 30-item limit; no second inventory database or sync service is created.
- Pairing and unpairing remain in Android's visible system UI. JARVIS does not scan nearby devices, read Bluetooth identifiers, pair/connect, send commands, or access credentials.
- Removing a Bluetooth label deletes only the local JARVIS label; the UI explains that actual unpairing must be done through Android Bluetooth settings > Forget.
- **Automated tests:** four pure-Java JUnit tests cover trimmed labels, empty/oversized input and Bluetooth label classification.
- **Anti-duplication:** extends existing M16 Smart Home Studio and reuses its inventory store, settings handoff conventions and owner-controlled removal flow.
- **CI/build:** pending; verify tests and debug APK before marking passed.
- **Physical device:** pending on Poco M4 Pro 4G; verify Bluetooth settings handoff and that no scan/pair request occurs.
- **Merge:** draft PR only; do not merge into PR #43 or `main` without owner approval. JARVIS only; Even untouched.


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
- **Status:** planning/scope lock only at this commit. No B21 Android or backend feature is claimed implemented, built, tested, or verified yet.
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
