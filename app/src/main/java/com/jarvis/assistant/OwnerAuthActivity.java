package com.jarvis.assistant;

import androidx.activity.ComponentActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;

import io.appwrite.ID;
import io.appwrite.coroutines.CoroutineCallback;
import io.appwrite.exceptions.AppwriteException;
import io.appwrite.enums.OAuthProvider;
import io.appwrite.services.Account;

public class OwnerAuthActivity extends ComponentActivity {
    private AppwriteAuthManager authManager;
    private Account account;
    private TextView status;
    private TextView profile;
    private EditText phoneInput;
    private EditText otpInput;
    private String pendingPhoneUserId = "";

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authManager = new AppwriteAuthManager(getApplicationContext());
        account = authManager.getAccount();
        buildScreen();
        refreshSession();
    }

    private void buildScreen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(22), dp(28), dp(22), dp(24));
        root.setBackgroundColor(0xFF080509);

        TextView title = new TextView(this);
        title.setText("J.A.R.V.I.S. • OWNER ACCESS");
        title.setTextColor(0xFFFF4652);
        title.setTextSize(21);
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        status = new TextView(this);
        status.setText("CHECKING APPWRITE SESSION…");
        status.setTextColor(0xFFE7C9CE);
        status.setTextSize(13);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, dp(14), 0, dp(10));
        root.addView(status, new LinearLayout.LayoutParams(-1, -2));

        profile = new TextView(this);
        profile.setText("No owner session loaded.");
        profile.setTextColor(0xFFFFFFFF);
        profile.setTextSize(14);
        profile.setGravity(Gravity.CENTER);
        profile.setPadding(0, dp(8), 0, dp(18));
        root.addView(profile, new LinearLayout.LayoutParams(-1, -2));

        Button google = makeButton("CONTINUE WITH GOOGLE");
        google.setOnClickListener(v -> startGoogleSignIn());
        root.addView(google);

        phoneInput = new EditText(this);
        phoneInput.setSingleLine(true);
        phoneInput.setHint("Phone number (+91XXXXXXXXXX)");
        phoneInput.setInputType(InputType.TYPE_CLASS_PHONE);
        phoneInput.setTextColor(0xFFFFFFFF);
        phoneInput.setHintTextColor(0xFFB99DA2);
        root.addView(phoneInput, fieldParams());

        Button sendOtp = makeButton("SEND PHONE OTP");
        sendOtp.setOnClickListener(v -> requestPhoneOtp());
        root.addView(sendOtp);

        otpInput = new EditText(this);
        otpInput.setSingleLine(true);
        otpInput.setHint("Enter the 6-digit OTP");
        otpInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        otpInput.setTextColor(0xFFFFFFFF);
        otpInput.setHintTextColor(0xFFB99DA2);
        root.addView(otpInput, fieldParams());

        Button verifyOtp = makeButton("VERIFY OTP & SIGN IN");
        verifyOtp.setOnClickListener(v -> verifyPhoneOtp());
        root.addView(verifyOtp);

        Button refresh = makeButton("REFRESH OWNER SESSION");
        refresh.setOnClickListener(v -> refreshSession());
        root.addView(refresh);

        Button signOut = makeButton("SIGN OUT");
        signOut.setOnClickListener(v -> signOut());
        root.addView(signOut);

        Button guest = makeButton("CONTINUE AS GUEST");
        guest.setOnClickListener(v -> {
            getSharedPreferences("jarvis_preferences", MODE_PRIVATE)
                    .edit().putBoolean("guest_mode_enabled", true).apply();
            status.setText("GUEST MODE • OWNER-ONLY ACCESS REMAINS LOCKED");
            Toast.makeText(this, "Guest mode selected", Toast.LENGTH_SHORT).show();
            finish();
        });
        root.addView(guest);

        setContentView(root);
    }

    private LinearLayout.LayoutParams fieldParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(52));
        params.topMargin = dp(10);
        return params;
    }

    private Button makeButton(String label) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextColor(0xFFFFFFFF);
        button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF641B29));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(50));
        params.topMargin = dp(9);
        button.setLayoutParams(params);
        return button;
    }

    private void setStatus(String message) {
        runOnUiThread(() -> status.setText(message));
    }

    private void startGoogleSignIn() {
        setStatus("OPENING APPWRITE GOOGLE SIGN-IN…");
        try {
            account.createOAuth2Session(
                    this,
                    OAuthProvider.GOOGLE,
                    null,
                    null,
                    Collections.emptyList(),
                    null,
                    new CoroutineCallback<>((result, error) -> runOnUiThread(() -> {
                        if (error != null) {
                            setStatus("GOOGLE SIGN-IN FAILED: " + safeError(error));
                        } else {
                            setStatus("GOOGLE SIGN-IN FLOW RETURNED • CHECKING SESSION…");
                            refreshSession();
                        }
                    }))
            );
        } catch (AppwriteException e) {
            setStatus("GOOGLE SIGN-IN ERROR: " + safeError(e));
        } catch (Exception e) {
            setStatus("GOOGLE SIGN-IN COULD NOT START");
        }
    }

    private void requestPhoneOtp() {
        String phone = phoneInput.getText().toString().trim();
        if (!phone.matches("^\\+[1-9][0-9]{7,14}$")) {
            phoneInput.setError("Use international format, e.g. +91XXXXXXXXXX");
            return;
        }
        setStatus("REQUESTING OTP…");
        try {
            account.createPhoneToken(
                    ID.unique(),
                    phone,
                    new CoroutineCallback<>((token, error) -> runOnUiThread(() -> {
                        if (error != null) {
                            setStatus("OTP REQUEST FAILED: " + safeError(error));
                            return;
                        }
                        pendingPhoneUserId = token.getUserId();
                        setStatus("OTP SENT. ENTER THE CODE RECEIVED ON YOUR PHONE.");
                    }))
            );
        } catch (AppwriteException e) {
            setStatus("OTP REQUEST ERROR: " + safeError(e));
        } catch (Exception e) {
            setStatus("OTP REQUEST COULD NOT START");
        }
    }

    private void verifyPhoneOtp() {
        String otp = otpInput.getText().toString().trim();
        if (pendingPhoneUserId.isEmpty()) {
            setStatus("REQUEST A NEW OTP FIRST.");
            return;
        }
        if (!otp.matches("^[0-9]{6}$")) {
            otpInput.setError("Enter the 6-digit code");
            return;
        }
        setStatus("VERIFYING OTP…");
        try {
            account.createSession(
                    pendingPhoneUserId,
                    otp,
                    new CoroutineCallback<>((session, error) -> runOnUiThread(() -> {
                        if (error != null) {
                            setStatus("OTP VERIFICATION FAILED: " + safeError(error));
                            return;
                        }
                        pendingPhoneUserId = "";
                        otpInput.setText("");
                        getSharedPreferences("jarvis_preferences", MODE_PRIVATE)
                                .edit().putBoolean("guest_mode_enabled", false).apply();
                        setStatus("PHONE VERIFIED • LOADING OWNER PROFILE…");
                        refreshSession();
                    }))
            );
        } catch (AppwriteException e) {
            setStatus("OTP VERIFICATION ERROR: " + safeError(e));
        } catch (Exception e) {
            setStatus("OTP VERIFICATION COULD NOT START");
        }
    }

    private void refreshSession() {
        setStatus("CHECKING APPWRITE SESSION…");
        try {
            account.get(new CoroutineCallback<>((user, error) -> runOnUiThread(() -> {
                if (error != null) {
                    profile.setText("Guest / signed out. No active Appwrite owner session.");
                    setStatus("GUEST MODE • NO ACTIVE OWNER SESSION");
                    return;
                }
                String name = user.getName() == null ? "" : user.getName();
                String email = user.getEmail() == null ? "" : user.getEmail();
                String phone = user.getPhone() == null ? "" : user.getPhone();
                profile.setText("OWNER PROFILE\n"
                        + "Name: " + name + "\n"
                        + "Email: " + email + "\n"
                        + "Phone: " + phone + "\n"
                        + "Appwrite ID: " + user.getId());
                getSharedPreferences("jarvis_preferences", MODE_PRIVATE)
                        .edit().putBoolean("guest_mode_enabled", false).apply();
                setStatus("OWNER SESSION ACTIVE");
            })));
        } catch (AppwriteException e) {
            setStatus("SESSION CHECK FAILED: " + safeError(e));
        } catch (Exception e) {
            setStatus("SESSION CHECK UNAVAILABLE");
        }
    }

    private void signOut() {
        setStatus("SIGNING OUT…");
        try {
            account.deleteSession("current", new CoroutineCallback<>((result, error) -> runOnUiThread(() -> {
                if (error != null) {
                    setStatus("SIGN-OUT FAILED: " + safeError(error));
                    return;
                }
                profile.setText("No owner session loaded.");
                getSharedPreferences("jarvis_preferences", MODE_PRIVATE)
                        .edit().putBoolean("guest_mode_enabled", true).apply();
                setStatus("SIGNED OUT • GUEST MODE ACTIVE");
            })));
        } catch (AppwriteException e) {
            setStatus("SIGN-OUT ERROR: " + safeError(e));
        } catch (Exception e) {
            setStatus("SIGN-OUT COULD NOT START");
        }
    }

    private String safeError(Throwable error) {
        String message = error.getMessage();
        if (message == null || message.trim().isEmpty()) return "Please check Appwrite settings and try again.";
        return message.length() > 140 ? message.substring(0, 140) : message;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (account != null && status != null) refreshSession();
    }
}
