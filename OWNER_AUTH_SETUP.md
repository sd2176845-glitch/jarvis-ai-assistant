# JARVIS Owner Authentication — Appwrite Implementation Plan

This document is the implementation specification for M01 Owner Authentication & Account Setup. Appwrite is the authentication backend; Firebase is not used for this module.

## Appwrite project
- Endpoint: https://sgp.cloud.appwrite.io/v1
- Project ID: 6b06d3aa003d717934a0
- Android application ID: com.jarvis.assistant
- Android SDK: io.appwrite:sdk-for-android:31.2.0

## Feature scope
1. Google Sign-In through Appwrite OAuth2.
2. Phone-number sign-in through Appwrite Phone Token + OTP session verification.
3. Owner profile using the authenticated Appwrite user record: user ID, name, email, phone, and verification flags. Do not store provider tokens or OTPs in the profile.
4. Session restore by asking Appwrite for the current account when the app starts/resumes; the SDK owns session-cookie persistence.
5. Sign-out by deleting the current Appwrite session and clearing only local UI/account-state caches.
6. Guest Mode remains available without creating an account. Keep the existing local Guest Mode privacy gates, and integrate them with explicit GUEST / SIGNED_IN state without treating Android device-unlock as an Appwrite login.

## Security rules
- Never put an Appwrite API key in the Android app. Client apps use only endpoint + project ID.
- Never log OTPs, session secrets, cookies, access tokens, or full authentication responses.
- Never request SMS-reading permission for OTP verification. The user enters the code manually; no SMS interception.
- Keep private database rows and files owner-scoped with Appwrite permissions. Guest mode must not gain owner access.
- Use E.164 phone format (for India, +91 followed by the number). Do not send an SMS until the user explicitly starts a phone login.
- Do not store passwords or OTPs locally.
- Do not collect unrelated phone data for the owner profile.

## Required Appwrite Console setup
- Register an Android platform with application ID `com.jarvis.assistant`.
- Enable Google in Auth > Social providers and configure the OAuth client credentials/redirect URI shown by Appwrite.
- Enable Phone authentication in Auth settings and review SMS delivery/country availability and pricing before any real-number test.
- Confirm the Appwrite project endpoint and platform configuration in the console.

## Implementation sequence
- [x] Add the official Appwrite Android SDK dependency and OAuth callback activity.
- [x] Add the Appwrite client configuration for the JARVIS project.
- [ ] Implement Google OAuth sign-in and callback account refresh.
- [ ] Implement phone token request and OTP verification.
- [ ] Implement owner profile display/state.
- [ ] Implement session restore and sign-out.
- [ ] Integrate guest mode with authentication state.
- [ ] Add automated tests, run Android build/unit tests, and verify on Poco M4 Pro.

## Acceptance checks
- Google sign-in succeeds only after the provider is configured and returns to JARVIS.
- Phone OTP succeeds with Appwrite mock/test numbers first; a real SMS test requires explicit user action and may incur a charge.
- Incorrect or expired OTP never authenticates.
- App restart restores a valid session; expired/revoked sessions return to guest state.
- Sign-out invalidates the current session.
- Guest mode cannot read owner-only resources.
- No secrets, OTPs, or session tokens appear in Git history or logs.
- Build and tests pass; target-device verification is recorded separately.

## Current status
The SDK/client foundation is being added. Authentication flows and Appwrite Console provider/platform configuration are not yet verified. Do not mark M01 authentication complete until every acceptance check passes.
