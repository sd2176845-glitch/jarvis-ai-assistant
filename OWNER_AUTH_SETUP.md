# JARVIS Owner Authentication — Setup & Implementation Plan

This document defines the safe path for adding Owner Login to the Android app. It is intentionally a setup plan, not a claim that authentication is already implemented.

## Goals
- Let the owner sign in with Google or a verified phone number (SMS OTP).
- Keep a small owner profile: Firebase UID, display name, email (if available), phone (if verified), and profile photo URL (if available).
- Restore the signed-in session after app restart.
- Provide sign-out and account-state visibility.
- Keep JARVIS usable in a clearly labelled guest mode until sign-in is configured.

## Required Firebase setup by the repository owner
1. Create a Firebase project in the Firebase console.
2. Register the Android app using the exact application ID: `com.jarvis.assistant`.
3. Download the project's `google-services.json` and place it at `app/google-services.json` locally. Do not paste its contents into chat or commit it to a public repository.
4. Enable Google and Phone providers in Firebase Authentication.
5. For Google sign-in, configure the app's SHA-1 and SHA-256 signing certificate fingerprints in Firebase and use the current Android Credential Manager sign-in flow.
6. For phone sign-in, configure allowed SMS regions and review Firebase phone-auth quotas and billing requirements. OTP delivery depends on Firebase and carrier availability.
7. Keep test phone numbers limited to Firebase's test-number facility. Never hardcode a real OTP or bypass verification.

## Android implementation design
- Add the Google Services Gradle plugin and Firebase Authentication dependency only after the Firebase project configuration is available.
- Use Credential Manager for Google account selection; exchange the returned Google ID token with Firebase Auth.
- Use Firebase Phone Auth's verification callbacks for SMS OTP. Do not build a custom SMS interception flow.
- Add an `OwnerProfile` model with only the fields listed above.
- Store no passwords or OTPs. Let Firebase manage authentication tokens and session persistence.
- Show explicit states: `GUEST`, `SIGNING_IN`, `SIGNED_IN`, and `SIGN_IN_ERROR`.
- Provide a visible sign-out action and a way to continue as guest.
- Do not request contacts, microphone, SMS-reading, or call permissions as a condition of signing in.
- Never send contact lists, message contents, precise location, or other phone data to Firebase as profile fields.
- Do not log ID tokens, phone verification codes, access tokens, or full authentication responses.

## Owner profile data minimization
Suggested profile fields:
- `uid` (Firebase UID; required)
- `displayName` (optional)
- `email` (optional)
- `phoneNumber` (optional; only after successful phone verification)
- `photoUrl` (optional)

Do not collect date of birth, home address, contacts, call history, SMS contents, or unrelated personal details for the owner profile.

## Acceptance checks before release
- Google sign-in succeeds and survives app restart.
- Phone OTP succeeds with a Firebase-configured test number and with a real number only after explicit user action.
- Incorrect/expired OTP displays a clear error and never signs in.
- Cancelled sign-in returns safely to guest mode.
- Sign-out clears the visible owner session.
- No secrets, OTPs, or tokens appear in Git history or logs.
- Core JARVIS features still open in guest mode.
- Build and test on the target Poco device before marking authentication verified.

## Current status
- Repository owner has not yet supplied a Firebase project configuration.
- Google and Phone providers have not yet been wired into the Android app.
- This file is a security/setup specification only; it is not a working login screen or authentication implementation.
