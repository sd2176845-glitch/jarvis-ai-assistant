package com.jarvis.assistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class OwnerAccountActivity extends Activity {
    private static final int GOOGLE_SIGN_IN_REQUEST = 7401;
    private static final String STATE_VERIFICATION_ID = "verification_id";
    private String verificationId;
    private TextView status;
    private EditText phoneInput;
    private EditText otpInput;
    private Button verifyButton;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (state != null) verificationId = state.getString(STATE_VERIFICATION_ID);
        buildUi();
        refreshStatus();
    }

    @Override protected void onSaveInstanceState(Bundle out) {
        out.putString(STATE_VERIFICATION_ID, verificationId);
        super.onSaveInstanceState(out);
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(28, 32, 28, 24);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setBackgroundColor(0xFF101010);

        TextView title = new TextView(this);
        title.setText("J.A.R.V.I.S. • OWNER ACCOUNT");
        title.setTextColor(0xFFFF3B30);
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER);
        root.addView(title, matchWrap());

        status = new TextView(this);
        status.setTextColor(0xFFE0E0E0);
        status.setTextSize(15);
        status.setPadding(0, 18, 0, 18);
        root.addView(status, matchWrap());

        Button google = button("Continue with Google");
        google.setOnClickListener(v -> startGoogleSignIn());
        root.addView(google, matchWrap());

        phoneInput = new EditText(this);
        phoneInput.setSingleLine(true);
        phoneInput.setInputType(InputType.TYPE_CLASS_PHONE);
        phoneInput.setHint("+91XXXXXXXXXX");
        phoneInput.setTextColor(0xFFFFFFFF);
        phoneInput.setHintTextColor(0xFF999999);
        root.addView(phoneInput, matchWrap());

        Button sendOtp = button("Send phone OTP");
        sendOtp.setOnClickListener(v -> sendOtp());
        root.addView(sendOtp, matchWrap());

        otpInput = new EditText(this);
        otpInput.setSingleLine(true);
        otpInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        otpInput.setHint("Enter OTP");
        otpInput.setTextColor(0xFFFFFFFF);
        otpInput.setHintTextColor(0xFF999999);
        root.addView(otpInput, matchWrap());

        verifyButton = button("Verify OTP");
        verifyButton.setEnabled(false);
        verifyButton.setOnClickListener(v -> verifyOtp());
        root.addView(verifyButton, matchWrap());

        Button signOut = button("Sign out / Switch to Guest");
        signOut.setOnClickListener(v -> {
            FirebaseOwnerAuth.signOut(this);
            refreshStatus();
            Toast.makeText(this, "Guest mode enabled.", Toast.LENGTH_SHORT).show();
        });
        root.addView(signOut, matchWrap());

        Button close = button("Close");
        close.setOnClickListener(v -> finish());
        root.addView(close, matchWrap());

        setContentView(root);
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private Button button(String label) {
        Button b = new Button(this);
        b.setText(label);
        return b;
    }

    private void startGoogleSignIn() {
        if (!FirebaseOwnerAuth.isConfigured(this)) {
            showSetupRequired();
            return;
        }
        if (BuildConfig.FIREBASE_WEB_CLIENT_ID == null || BuildConfig.FIREBASE_WEB_CLIENT_ID.trim().isEmpty()) {
            showSetupRequired();
            return;
        }
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.FIREBASE_WEB_CLIENT_ID)
                .requestEmail()
                .build();
        startActivityForResult(GoogleSignIn.getClient(this, options).getSignInIntent(), GOOGLE_SIGN_IN_REQUEST);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != GOOGLE_SIGN_IN_REQUEST || data == null) return;
        try {
            String token = GoogleSignIn.getSignedInAccountFromIntent(data)
                    .getResult(ApiException.class).getIdToken();
            FirebaseOwnerAuth.signInWithGoogleToken(this, token, authResult());
        } catch (ApiException e) {
            showError("Google sign-in cancelled or failed: " + e.getStatusCode());
        }
    }

    private void sendOtp() {
        String phone = phoneInput.getText().toString().trim();
        FirebaseOwnerAuth.requestPhoneOtp(this, phone,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override public void onVerificationCompleted(PhoneAuthCredential credential) {
                FirebaseOwnerAuth.verifyPhoneCredential(OwnerAccountActivity.this, credential, authResult());
            }
            @Override public void onVerificationFailed(Exception e) {
                showError(e.getMessage());
            }
            @Override public void onCodeSent(String id, PhoneAuthProvider.ForceResendingToken token) {
                verificationId = id;
                verifyButton.setEnabled(true);
                status.setText("OTP sent. Enter the code received on your phone.");
                Toast.makeText(OwnerAccountActivity.this, "OTP sent.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verifyOtp() {
        String code = otpInput.getText().toString().trim();
        if (verificationId == null || code.length() < 6) {
            showError("Enter the OTP received on your phone.");
            return;
        }
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        FirebaseOwnerAuth.verifyPhoneCredential(this, credential, authResult());
    }

    private FirebaseOwnerAuth.Result authResult() {
        return new FirebaseOwnerAuth.Result() {
            @Override public void onSuccess(FirebaseUser user) {
                runOnUiThread(() -> {
                    JarvisSessionManager.enterGuest();
                    status.setText("OWNER SIGNED IN\n" + FirebaseOwnerAuth.ownerLabel(OwnerAccountActivity.this)
                            + "\nSensitive actions will still request the local privacy PIN when enabled.");
                    Toast.makeText(OwnerAccountActivity.this, "Owner account verified.", Toast.LENGTH_LONG).show();
                });
            }
            @Override public void onError(String message) { runOnUiThread(() -> showError(message)); }
        };
    }

    private void refreshStatus() {
        FirebaseUser user = FirebaseOwnerAuth.currentUser(this);
        if (user == null) {
            status.setText(FirebaseOwnerAuth.isConfigured(this)
                    ? "No owner account signed in. Google or phone OTP sign-in is available."
                    : "Firebase setup is required before online owner sign-in can work.");
        } else {
            status.setText("OWNER ACCOUNT VERIFIED\n" + FirebaseOwnerAuth.ownerLabel(this));
        }
    }

    private void showSetupRequired() {
        new AlertDialog.Builder(this).setTitle("Firebase setup required")
                .setMessage("Add the Firebase project values to your local Gradle properties and follow FIREBASE_OWNER_AUTH_SETUP.md. Do not put private keys or OTPs in source code.")
                .setPositiveButton("OK", null).show();
    }

    private void showError(String message) {
        status.setText("AUTHENTICATION ERROR\n" + (message == null ? "Unknown error" : message));
        Toast.makeText(this, message == null ? "Authentication failed." : message, Toast.LENGTH_LONG).show();
    }
}
