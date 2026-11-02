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
