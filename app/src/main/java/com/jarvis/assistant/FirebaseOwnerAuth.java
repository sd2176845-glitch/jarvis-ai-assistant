package com.jarvis.assistant;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

/**
 * Firebase-backed owner identity. Configuration is read from Gradle properties
 * into BuildConfig; no Firebase keys or OTPs are persisted by this class.
 */
public final class FirebaseOwnerAuth {
    public interface Result {
        void onSuccess(FirebaseUser user);
        void onError(String message);
    }

    private static final String PREFS = "jarvis_firebase_owner";
    private static final String KEY_UID = "last_owner_uid";
    private static FirebaseAuth auth;

    private FirebaseOwnerAuth() {}

    public static synchronized boolean initialize(Context context) {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                if (TextUtils.isEmpty(BuildConfig.FIREBASE_API_KEY)
                        || TextUtils.isEmpty(BuildConfig.FIREBASE_APP_ID)
                        || TextUtils.isEmpty(BuildConfig.FIREBASE_PROJECT_ID)
                        || TextUtils.isEmpty(BuildConfig.FIREBASE_SENDER_ID)) {
                    return false;
                }
                FirebaseOptions options = new FirebaseOptions.Builder()
                        .setApiKey(BuildConfig.FIREBASE_API_KEY)
                        .setApplicationId(BuildConfig.FIREBASE_APP_ID)
                        .setProjectId(BuildConfig.FIREBASE_PROJECT_ID)
                        .setGcmSenderId(BuildConfig.FIREBASE_SENDER_ID)
                        .build();
                FirebaseApp.initializeApp(context.getApplicationContext(), options);
            }
            auth = FirebaseAuth.getInstance();
            return true;
        } catch (Exception e) {
            auth = null;
            return false;
        }
    }

    public static boolean isConfigured(Context context) {
        return initialize(context);
    }

    public static FirebaseUser currentUser(Context context) {
        return initialize(context) && auth != null ? auth.getCurrentUser() : null;
    }

    public static boolean isOwnerAuthenticated(Context context) {
        return currentUser(context) != null;
    }

    public static String ownerLabel(Context context) {
        FirebaseUser user = currentUser(context);
        if (user == null) return "Guest";
        if (!TextUtils.isEmpty(user.getDisplayName())) return user.getDisplayName();
        if (!TextUtils.isEmpty(user.getPhoneNumber())) return user.getPhoneNumber();
        if (!TextUtils.isEmpty(user.getEmail())) return user.getEmail();
        return "Verified owner";
    }

    public static void rememberOwner(Context context, FirebaseUser user) {
        if (user == null) return;
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString(KEY_UID, user.getUid()).apply();
    }

    public static void signOut(Context context) {
        if (initialize(context) && auth != null) auth.signOut();
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(KEY_UID).apply();
        JarvisSessionManager.enterGuest();
    }

    public static void signInWithGoogleToken(Context context, String idToken, Result result) {
        if (!initialize(context)) {
            result.onError("Firebase is not configured. Follow FIREBASE_OWNER_AUTH_SETUP.md.");
            return;
        }
        if (TextUtils.isEmpty(idToken)) {
            result.onError("Google sign-in did not return an ID token.");
            return;
        }
        AuthCredential credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null);
        FirebaseUser existing = auth.getCurrentUser();
        if (existing != null) {
            existing.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) result.onSuccess(existing);
                else result.onError(task.getException() == null ? "Google re-authentication failed." : task.getException().getMessage());
            });
            return;
        }
        auth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().getUser() != null) {
                rememberOwner(context, task.getResult().getUser());
                result.onSuccess(task.getResult().getUser());
            } else {
                result.onError(task.getException() == null ? "Google sign-in failed." : task.getException().getMessage());
            }
        });
    }

    public static void requestPhoneOtp(Activity activity, String phoneNumber,
            PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks) {
        if (!initialize(activity)) {
            callbacks.onVerificationFailed(new FirebaseAuthInvalidCredentialsException(
                    "ERROR_FIREBASE_NOT_CONFIGURED",
                    "Firebase is not configured. Follow FIREBASE_OWNER_AUTH_SETUP.md."));
            return;
        }
        if (TextUtils.isEmpty(phoneNumber) || !phoneNumber.matches("^\\+[1-9]\\d{7,14}$")) {
            callbacks.onVerificationFailed(new FirebaseAuthInvalidCredentialsException(
                    "ERROR_INVALID_PHONE_NUMBER",
                    "Enter a valid phone number with country code, for example +91XXXXXXXXXX."));
            return;
        }
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    public static void verifyPhoneCredential(Context context, PhoneAuthCredential credential, Result result) {
        if (!initialize(context)) {
            result.onError("Firebase is not configured. Follow FIREBASE_OWNER_AUTH_SETUP.md.");
            return;
        }
        FirebaseUser existing = auth.getCurrentUser();
        if (existing != null) {
            existing.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) result.onSuccess(existing);
                else result.onError(task.getException() == null ? "Phone re-authentication failed." : task.getException().getMessage());
            });
            return;
        }
        auth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().getUser() != null) {
                rememberOwner(context, task.getResult().getUser());
                result.onSuccess(task.getResult().getUser());
            } else {
                result.onError(task.getException() == null ? "Phone verification failed." : task.getException().getMessage());
            }
        });
    }

    public static void reauthenticate(AuthCredential credential, Result result) {
        FirebaseUser user = auth == null ? null : auth.getCurrentUser();
        if (user == null) {
            result.onError("No owner is signed in. Sign in again.");
            return;
        }
        user.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) result.onSuccess(user);
            else result.onError(task.getException() == null ? "Owner re-authentication failed." : task.getException().getMessage());
        });
    }
}
