# J.A.R.V.I.S. — Personal AI Assistant

An Android starter project for a Stark-inspired personal assistant, designed to build on a phone-friendly GitHub workflow.

## Starter features
- Futuristic blue JARVIS-style interface
- Tap-to-speak using Android speech recognition
- Spoken replies using Android Text-to-Speech
- Basic demo commands: greeting, current time, open YouTube, open Chrome, identify JARVIS
- GitHub Actions workflow to build a debug APK

## Build
Open the **Actions** tab and run **Build JARVIS APK**, or push a commit to the `main` branch. Download the `jarvis-debug-apk` artifact from a successful workflow run.

## Important notes
- This is a starter, not the full movie JARVIS.
- Speech recognition may use the Android device's configured speech service and may require internet.
- Android requires microphone permission at runtime; this starter will need a permission-request flow added if recognition is blocked.
- Background wake-word listening, AI model integration, secure memory, and advanced phone controls are not included yet.
