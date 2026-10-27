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

## Remaining feature backlog

### Priority 1 — Personality and emotion engine
- [ ] Full emotion engine: happy and thinking states, plus automatic mood-aware adaptation (manual Caring/Funny/Serious modes now have initial code).
- [ ] Mood-aware response style, without overconfident mood assumptions.
- [ ] Consistent butler-inspired personality: composed, discreet, respectful, helpful and occasionally witty.
- [ ] Fine-grained voice delivery and orb animation for all emotion states (mode-specific pulse speed now has initial code).
- [x] Explicit mode selector and safe default mode (code present — build/device verification pending).

### Priority 2 — Memory and personal secretary
- [ ] User-approved long-term preferences with review/edit/delete controls.
- [ ] Notes and to-do lists.
- [ ] Persistent reminders and scheduled notifications.
- [ ] Morning briefing and daily schedule.
- [x] Study, Gaming, Travel and Night modes (initial selector, voice commands and AI guidance are in code; reminders/workflows are not included yet).

### Priority 3 — Online intelligence and offline fallback
- [ ] Actual live web search integration with source-aware summaries.
- [ ] Weather, news and sports integrations.
- [ ] Hindi/Bengali/English translation workflow.
- [ ] Multi-step commands using supported actions, with confirmation for sensitive steps.
- [ ] Offline basic commands and saved notes; clearly report when online AI is unavailable.

### Priority 4 — Advanced integrations
- [ ] Camera vision for user-selected images and text.
- [ ] Notification reader with explicit user-granted access and privacy controls.
- [ ] Smart-home integration for compatible devices.
- [ ] Digital wellbeing / screen-time overview where Android provides the data.
- [ ] Coding helper and daily learning tips.
- [ ] Headphone/Bluetooth interaction and selected context-aware events.

### Priority 5 — Reliability, UI and safety
- [ ] Verify background listening and screen-off operation on the Poco device.
- [ ] Test MIUI battery restrictions and document settings needed.
- [ ] Show microphone/listening state clearly and provide a stop control.
- [ ] Verify all permissions and avoid requesting permissions until a feature needs them.
- [ ] Keep API credentials server-side; use HTTPS.
- [ ] Test install, launch, voice, AI response, app commands, memory clear, and service stop on the actual phone.
- [ ] Verify final APK build and install before calling any feature complete.

## Implementation rules
1. Do not label a feature complete merely because its button or class exists.
2. Implement and test a coherent group of features at a time; keep the app buildable after each group.
3. Do not store secrets (Groq/API keys, passwords, tokens) in Android code or the repository.
4. Ask before sending messages, sharing location, placing a call, or performing other sensitive actions.
5. Keep the user in control of saved memory, microphone use, and notification access.
6. The assistant must be honest about actions, live information, and limitations.
