# Firebase Owner Authentication Setup

This branch adds Firebase-backed owner identity with Google sign-in and phone-number OTP. The local Owner PIN remains a second privacy-lock step; it is not a substitute for Firebase identity.

## 1. Create/register the Firebase Android app

1. Open Firebase Console and create or select the project.
2. Add an Android app with package name `com.jarvis.assistant`.
3. Add the SHA-1 and SHA-256 signing certificate fingerprints for the build you will install. For a local debug build, run `./gradlew signingReport` from the repository and copy the debug fingerprints.
4. In Firebase Authentication, enable **Google** and **Phone** providers.
5. For Google, configure the OAuth consent screen and use the **Web application** OAuth client ID as the web client ID below.
6. For Phone, configure the allowed countries and any test phone numbers in Firebase Console. Firebase quotas and SMS delivery rules apply.

## 2. Add local Gradle properties (do not commit)

Add these entries to your user-level Gradle properties file (normally `~/.gradle/gradle.properties`), not to Git:

```properties
JARVIS_FIREBASE_API_KEY=your_firebase_web_api_key
JARVIS_FIREBASE_APP_ID=your_firebase_android_app_id
JARVIS_FIREBASE_PROJECT_ID=your_firebase_project_id
JARVIS_FIREBASE_SENDER_ID=your_firebase_messaging_sender_id
JARVIS_FIREBASE_WEB_CLIENT_ID=your_oauth_web_client_id
```

The values are available in Firebase Project settings / Android app configuration and Google Cloud OAuth clients. These are client configuration values, not service-account credentials. Restrict the Firebase API key to the Android package and signing certificate where supported. Never add service-account JSON, private keys, SMS codes, passwords, or user tokens to the repository.

## 3. Build and verify

1. Sync Gradle so Firebase Auth and Google Play Services Auth dependencies resolve.
2. Build the debug APK.
3. Open **Command Center → Owner Account • Google / Phone OTP**.
4. Test Google sign-in and phone OTP with a test number first.
5. Confirm the signed-in owner is shown, then open **Command Center → Owner / Guest Mode** and set/unlock the local 6–12 digit privacy PIN.
6. Verify owner-only notes, reminders, SOS settings, and Personal Media Vault are blocked when signed out or in Guest mode.
7. Select **Sign out / Switch to Guest** and verify that Firebase owner identity and the local owner session are both cleared.

## 4. Current behavior and limitations

- Firebase Auth persists the signed-in identity using its SDK-managed session; passwords and OTP values are not written to app preferences.
- Sensitive app features require Firebase owner sign-in when Firebase configuration is present, plus the existing local privacy PIN session.
- When Firebase configuration is absent, the existing local PIN flow remains available as a temporary compatibility path. Do not treat that as production-grade owner identity.
- Google/phone re-authentication uses the selected provider credential when an account is already signed in.
- Real provider sign-in, SMS delivery, account linking, API-key restrictions, and Poco/MIUI behavior cannot be verified until the project values are configured and a real-device test is completed.
- Phone-number account linking and recovery for users who sign in with different providers should be tested and configured deliberately before production use.
