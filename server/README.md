# JARVIS Gemini Proxy

The Android app must call this HTTPS service. The Gemini API key belongs in the hosting provider’s secret/environment settings, never in Android code, `BuildConfig`, Git, or SharedPreferences.

## Run

1. Install Node.js 20 or newer.
2. Run `npm install` inside `server/`.
3. Set `GEMINI_API_KEY` in your server environment (do not commit a `.env` file). Optional: set `PORT` and `GEMINI_MODEL`.
4. Run `npm start`.
5. Deploy behind HTTPS. Keep the key as a hosting secret.

Endpoints:
- `GET /api/jarvis/status` reports whether the server has a key configured.
- `POST /api/jarvis/chat` accepts `{"message":"Hello"}` and returns `{"text":"..."}`.

## Connect the Android app

In `app/build.gradle`, replace `https://YOUR_SERVER_DOMAIN/api/jarvis/chat` with your deployed HTTPS endpoint. Then run the Android build workflow. Until this is configured and deployed, online Gemini replies will not work.

The included in-memory rate limit is only a basic guardrail; use platform-level rate limiting for production.
