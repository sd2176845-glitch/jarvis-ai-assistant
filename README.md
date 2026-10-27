# J.A.R.V.I.S. — Personal AI Assistant

A phone-first Android personal assistant project for the Poco M4 Pro 4G. This is separate from the Even project.

## Code currently present

- Dark blue/cyan JARVIS dashboard with a layered vector AI orb, pulse animation, and cyan divider.
- Tap-to-speak speech recognition and spoken replies.
- An initial foreground-service listener for “Hey JARVIS”, with a visible notification and stop control.
- Online AI chat through the Render backend and Groq. The Groq key belongs in the backend environment, never in Android source or the APK.
- Basic local commands for common apps, time/date/battery, contacts/call flow, alarm handoff, web search, and message drafts.
- Contact calling in the background listener asks for a spoken confirmation before calling.
- Local notes, recent conversation context with a clear-memory control, eight assistant modes, a daily briefing, privacy controls, SOS/dialer options, phone-event announcements, and accessibility-based in-call controls.
- Camera and Google Lens shortcuts; task/reminder and connected-system hubs.

## Build an APK

Open the Actions tab and choose a successful “Build JARVIS APK” run. Download the jarvis-debug-apk artifact from that run. A failed workflow run does not produce a new APK.

## Important limitations

This is an early implementation, not a complete movie-style JARVIS. A feature being present in code does not prove it works on every phone.

- Background listening uses Android speech recognition in a foreground service; continuous listening, screen-off behavior, and language availability depend on Android, the speech provider, and Poco/MIUI battery settings.
- AI chat needs internet and a healthy backend. The live backend was not verified by this repository build.
- Web and weather features currently open browser search pages; they are not native, source-aware live-data integrations.
- The reminder hub opens Android Clock; it does not schedule JARVIS-owned reminder notifications.
- The vision option opens the camera or Google Lens; there is no in-app image-understanding pipeline yet.
- Notes and recent chat history are local. Long-term editable preferences, structured task management, and a full offline AI fallback are not complete.
- Actual installation, calls, SMS announcements, accessibility controls, background listening, and the UI still need a device test on the target Poco phone.
- Grant microphone/contact/call/phone-event permissions only when needed. The foreground notification provides a way to stop listening.

See JARVIS_MASTER_BLUEPRINT.md for the implementation inventory and remaining backlog.
