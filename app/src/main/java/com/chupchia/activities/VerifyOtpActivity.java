package com.chupchia.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.material.button.MaterialButton;
import com.chupchia.R;
import com.chupchia.receivers.SmsBroadcastReceiver;
import com.chupchia.utils.SharedPrefManager;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class VerifyOtpActivity extends AppCompatActivity {

    // ===== CONSTANTS =====
    private static final int OTP_LENGTH = 6;
    private static final int TIMER_DURATION = 60000; // 60 seconds
    private static final int MAX_VERIFY_ATTEMPTS = 5;
    private static final int MAX_RESEND_COUNT = 3;
    private static final long RESEND_LOCKOUT_DURATION = 10 * 60 * 1000; // 10 minutes
    
    // ===== VIEWS =====
    private View ivLogo;
    private TextView tvTitle;
    private TextView tvSubtitle;
    private TextView tvPhoneDisplay;
    private LinearLayout otpContainer;
    private EditText[] otpFields = new EditText[OTP_LENGTH];
    private TextView tvTimer;
    private TextView tvTimerSeconds;
    private TextView tvAutoRead;
    private MaterialButton btnResend;
    private MaterialButton btnVerify;
    private TextView tvBackToRegister;
    private LinearLayout llTimer;
    
    // ===== VARIABLES =====
    private String phoneNumber;
    private String fullname;
    private String email;
    private String password;
    private String expectedOtp;
    private CountDownTimer countDownTimer;
    private ProgressDialog progressDialog;
    private int resendCount = 0;
    private int verifyAttempts = 0;
    private long resendLockoutEndTime = 0;
    private boolean isVerifying = false;
    private SmsBroadcastReceiver smsBroadcastReceiver;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);
        
        getIntentData();
        initViews();
        setupOtpFields();
        setupListeners();
        startEnterAnimation();
        startSmsRetriever();
        sendOtp();
        startTimer();
        loadResendState();
    }
    
    /**
     * Lấy dữ liệu từ Intent
     */
    private void getIntentData() {
        phoneNumber = getIntent().getStringExtra("phone");
        fullname = getIntent().getStringExtra("fullname");
        email = getIntent().getStringExtra("email");
        password = getIntent().getStringExtra("password");
        
        if (phoneNumber == null) {
            // Try to get from SharedPreferences
            phoneNumber = SharedPrefManager.getSharedPreferences().getString("temp_phone", null);
            fullname = SharedPrefManager.getSharedPreferences().getString("temp_fullname", null);
            email = SharedPrefManager.getSharedPreferences().getString("temp_email", null);
            password = SharedPrefManager.getSharedPreferences().getString("temp_password", null);
        }
    }
    
    /**
     * Khởi tạo views
     */
    private void initViews() {
        ivLogo = findViewById(R.id.iv_logo);
        tvTitle = findViewById(R.id.tv_title);
        tvSubtitle = findViewById(R.id.tv_subtitle);
        tvPhoneDisplay = findViewById(R.id.tv_phone_display);
        otpContainer = findViewById(R.id.otp_container);
        tvTimer = findViewById(R.id.tv_timer);
        tvTimerSeconds = findViewById(R.id.tv_timer_seconds);
        tvAutoRead = findViewById(R.id.tv_auto_read);
        btnResend = findViewById(R.id.btn_resend);
        btnVerify = findViewById(R.id.btn_verify);
        tvBackToRegister = findViewById(R.id.tv_back_to_register);
        llTimer = findViewById(R.id.ll_timer);
        
        // Format phone number display (mask middle digits)
        String maskedPhone = maskPhoneNumber(phoneNumber);
        tvPhoneDisplay.setText(maskedPhone);
        
        // Setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.verify_otp_verifying));
        progressDialog.setCancelable(false);
    }
    
    /**
     * Mask phone number (e.g., 0987654321 -> 098*****21)
     */
    private String maskPhoneNumber(String phone) {
        if (phone == null || phone.length() < 10) return phone;
        String start = phone.substring(0, 3);
        String end = phone.substring(phone.length() - 2);
        return start + "*****" + end;
    }
    
    /**
     * Setup 6 OTP input fields
     */
    private void setupOtpFields() {
        int[] otpIds = {
            R.id.et_otp_1, R.id.et_otp_2, R.id.et_otp_3,
            R.id.et_otp_4, R.id.et_otp_5, R.id.et_otp_6
        };
        
        for (int i = 0; i < OTP_LENGTH; i++) {
            otpFields[i] = findViewById(otpIds[i]);
            setupOtpField(i);
        }
        
        // Setup paste functionality on first field
        otpFields[0].setOnLongClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            if (clipboard.hasPrimaryClip()) {
                ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
                String pastedText = item.getText().toString();
                if (pastedText.length() == OTP_LENGTH && pastedText.matches("\\d+")) {
                    fillOtpFields(pastedText);
                    return true;
                }
            }
            return false;
        });
    }
    
    /**
     * Setup individual OTP field
     */
    private void setupOtpField(final int index) {
        // Text change listener to auto move to next field
        otpFields[index].addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1 && index < OTP_LENGTH - 1) {
                    otpFields[index + 1].requestFocus();
                }
                checkOtpComplete();
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Key listener to handle backspace
        otpFields[index].setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DEL && 
                event.getAction() == KeyEvent.ACTION_DOWN &&
                otpFields[index].getText().toString().isEmpty() && 
                index > 0) {
                otpFields[index - 1].requestFocus();
                otpFields[index - 1].setText("");
                return true;
            }
            return false;
        });
    }
    
    /**
     * Fill all OTP fields with pasted code
     */
    private void fillOtpFields(String otp) {
        for (int i = 0; i < OTP_LENGTH && i < otp.length(); i++) {
            otpFields[i].setText(String.valueOf(otp.charAt(i)));
        }
        // Focus on next empty field
        for (int i = 0; i < OTP_LENGTH; i++) {
            if (otpFields[i].getText().toString().isEmpty()) {
                otpFields[i].requestFocus();
                return;
            }
        }
        // All fields filled, hide keyboard
        hideKeyboard();
        autoVerifyOtp();
    }
    
    /**
     * Check if all OTP fields are filled
     */
    private void checkOtpComplete() {
        boolean isComplete = true;
        StringBuilder otpBuilder = new StringBuilder();
        
        for (EditText field : otpFields) {
            String text = field.getText().toString();
            if (text.isEmpty()) {
                isComplete = false;
                break;
            }
            otpBuilder.append(text);
        }
        
        btnVerify.setEnabled(isComplete);
        btnVerify.setAlpha(isComplete ? 1f : 0.5f);
        
        if (isComplete && !isVerifying) {
            // Auto verify when all fields are filled
            new Handler(Looper.getMainLooper()).postDelayed(this::autoVerifyOtp, 300);
        }
    }
    
    /**
     * Auto verify OTP
     */
    private void autoVerifyOtp() {
        if (isVerifying) return;
        
        StringBuilder otpBuilder = new StringBuilder();
        for (EditText field : otpFields) {
            otpBuilder.append(field.getText().toString());
        }
        String enteredOtp = otpBuilder.toString();
        
        if (enteredOtp.length() == OTP_LENGTH) {
            verifyOtp(enteredOtp);
        }
    }
    
    /**
     * Verify OTP with server
     */
    private void verifyOtp(String enteredOtp) {
        if (isVerifying) return;
        isVerifying = true;
        
        showLoading(true);
        
        // TODO: Call actual OTP verification API
        simulateVerifyOtp(enteredOtp);
    }
    
    /**
     * Simulate OTP verification (replace with actual API)
     */
    private void simulateVerifyOtp(String enteredOtp) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            showLoading(false);
            
            // For demo, any 6-digit number is valid
            // In production, verify with server
            if (enteredOtp.length() == 6 && enteredOtp.matches("\\d+")) {
                createAccount();
            } else {
                verifyAttempts++;
                if (verifyAttempts >= MAX_VERIFY_ATTEMPTS) {
                    showMaxAttemptsError();
                } else {
                    showOtpError();
                }
            }
        }, 1500);
    }
    
    /**
     * Create user account after OTP verification
     */
    private void createAccount() {
        SharedPrefManager prefManager = SharedPrefManager.getInstance(this);
        
        // Generate user ID
        String userId = "user_" + System.currentTimeMillis();
        String token = "token_" + System.currentTimeMillis() + "_" + userId;
        
        // Save user data
        prefManager.setLoggedIn(true);
        prefManager.setAuthToken(token);
        prefManager.setUserId(userId);
        prefManager.setUserName(fullname);
        prefManager.setUserEmail(email != null ? email : "");
        prefManager.setTokenExpiry(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000); // 7 days
        
        // Clear temp data
        SharedPrefManager.getSharedPreferences().edit()
            .remove("temp_fullname")
            .remove("temp_phone")
            .remove("temp_email")
            .remove("temp_password")
            .apply();
        
        // Navigate to Main
        navigateToMain();
    }
    
    /**
     * Show OTP error
     */
    private void showOtpError() {
        isVerifying = false;
        
        // Highlight all OTP fields in red
        for (EditText field : otpFields) {
            field.setBackgroundResource(R.drawable.bg_otp_box_error);
            
            // Shake animation
            ObjectAnimator shakeX = ObjectAnimator.ofFloat(field, "translationX", 0f, 10f, -10f, 5f, -5f, 0f);
            shakeX.setDuration(400);
            shakeX.start();
        }
        
        Toast.makeText(this, R.string.verify_otp_error, Toast.LENGTH_SHORT).show();
        
        // Clear fields after error
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            for (EditText field : otpFields) {
                field.setText("");
                field.setBackgroundResource(R.drawable.bg_otp_box);
            }
            otpFields[0].requestFocus();
        }, 1500);
    }
    
    /**
     * Show max attempts error
     */
    private void showMaxAttemptsError() {
        new AlertDialog.Builder(this)
            .setTitle(R.string.error_register_failed)
            .setMessage(R.string.verify_otp_too_many_attempts)
            .setPositiveButton(R.string.ok, (dialog, which) -> {
                finish();
            })
            .setCancelable(false)
            .show();
    }
    
    /**
     * Send OTP via SMS
     */
    private void sendOtp() {
        // Show auto-read indicator
        tvAutoRead.setVisibility(View.VISIBLE);
        
        // TODO: Call API to send OTP
        String message = String.format(getString(R.string.verify_otp_sent), maskPhoneNumber(phoneNumber));
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        
        // For demo, generate fake OTP
        expectedOtp = String.format("%06d", new Random().nextInt(999999));
        
        // Auto-fill OTP for demo (remove in production)
        // fillOtpFields(expectedOtp);
    }
    
    /**
     * Start countdown timer for resend
     */
    private void startTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        
        llTimer.setVisibility(View.VISIBLE);
        btnResend.setVisibility(View.GONE);
        
        countDownTimer = new CountDownTimer(TIMER_DURATION, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                tvTimerSeconds.setText(String.valueOf(seconds));
            }
            
            @Override
            public void onFinish() {
                llTimer.setVisibility(View.GONE);
                btnResend.setVisibility(View.VISIBLE);
                tvAutoRead.setVisibility(View.GONE);
            }
        }.start();
    }
    
    /**
     * Resend OTP
     */
    private void resendOtp() {
        if (System.currentTimeMillis() < resendLockoutEndTime) {
            long remainingMinutes = (resendLockoutEndTime - System.currentTimeMillis()) / 1000 / 60;
            Toast.makeText(this, 
                String.format(getString(R.string.verify_otp_resend_limit), remainingMinutes), 
                Toast.LENGTH_LONG).show();
            return;
        }
        
        if (resendCount >= MAX_RESEND_COUNT) {
            resendLockoutEndTime = System.currentTimeMillis() + RESEND_LOCKOUT_DURATION;
            saveResendState();
            Toast.makeText(this, R.string.verify_otp_resend_limit, Toast.LENGTH_LONG).show();
            return;
        }
        
        resendCount++;
        saveResendState();
        
        // Clear OTP fields
        for (EditText field : otpFields) {
            field.setText("");
        }
        otpFields[0].requestFocus();
        
        // Send new OTP
        sendOtp();
        startTimer();
    }
    
    /**
     * Save resend state to SharedPreferences
     */
    private void saveResendState() {
        SharedPrefManager.getSharedPreferences().edit()
            .putInt("otp_resend_count", resendCount)
            .putLong("otp_resend_lockout", resendLockoutEndTime)
            .apply();
    }
    
    /**
     * Load resend state from SharedPreferences
     */
    private void loadResendState() {
        resendCount = SharedPrefManager.getSharedPreferences().getInt("otp_resend_count", 0);
        resendLockoutEndTime = SharedPrefManager.getSharedPreferences().getLong("otp_resend_lockout", 0);
        
        if (System.currentTimeMillis() < resendLockoutEndTime) {
            btnResend.setEnabled(false);
            btnResend.setAlpha(0.5f);
        }
    }
    
    /**
     * Start SMS retriever for auto-read OTP
     */
    private void startSmsRetriever() {
        SmsRetrieverClient client = SmsRetriever.getClient(this);
        client.startSmsRetriever()
            .addOnSuccessListener(aVoid -> {
                // Successfully started
            })
            .addOnFailureListener(e -> {
                // Failed to start
                tvAutoRead.setVisibility(View.GONE);
            });
        
        // Register broadcast receiver
        smsBroadcastReceiver = new SmsBroadcastReceiver();
        smsBroadcastReceiver.setOTPReceivedListener(new SmsBroadcastReceiver.OTPReceivedListener() {
            @Override
            public void onOTPReceived(String otp) {
                runOnUiThread(() -> {
                    fillOtpFields(otp);
                    tvAutoRead.setText("✓ Đã tự động nhập mã OTP");
                    tvAutoRead.setTextColor(getColor(R.color.success));
                });
            }
            
            @Override
            public void onOTPTimeout() {
                runOnUiThread(() -> {
                    tvAutoRead.setVisibility(View.GONE);
                });
            }
        });
        
        // Register receiver (use LocalBroadcastManager or register system broadcast)
        // For simplicity, we'll skip the full implementation here
    }
    
    /**
     * Animation khi mở màn hình
     */
    private void startEnterAnimation() {
        ivLogo.setAlpha(0f);
        ivLogo.animate()
            .alpha(1f)
            .setDuration(600)
            .setInterpolator(new AccelerateDecelerateInterpolator())
            .start();
        
        tvTitle.setTranslationY(30f);
        tvTitle.setAlpha(0f);
        tvTitle.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(500)
            .setStartDelay(200)
            .start();
        
        tvSubtitle.setTranslationY(30f);
        tvSubtitle.setAlpha(0f);
        tvSubtitle.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(500)
            .setStartDelay(300)
            .start();
        
        tvPhoneDisplay.setScaleX(0.8f);
        tvPhoneDisplay.setScaleY(0.8f);
        tvPhoneDisplay.setAlpha(0f);
        tvPhoneDisplay.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(400)
            .setStartDelay(400)
            .start();
        
        otpContainer.setAlpha(0f);
        otpContainer.animate()
            .alpha(1f)
            .setDuration(400)
            .setStartDelay(500)
            .start();
    }
    
    /**
     * Setup button listeners
     */
    private void setupListeners() {
        btnVerify.setOnClickListener(v -> {
            StringBuilder otpBuilder = new StringBuilder();
            for (EditText field : otpFields) {
                otpBuilder.append(field.getText().toString());
            }
            verifyOtp(otpBuilder.toString());
        });
        
        btnResend.setOnClickListener(v -> resendOtp());
        tvBackToRegister.setOnClickListener(v -> navigateToRegister());
    }
    
    /**
     * Show/hide loading
     */
    private void showLoading(boolean show) {
        if (show) {
            progressDialog.show();
            btnVerify.setEnabled(false);
            btnVerify.setAlpha(0.5f);
        } else {
            progressDialog.dismiss();
            btnVerify.setEnabled(true);
            btnVerify.setAlpha(1f);
            isVerifying = false;
        }
    }
    
    /**
     * Hide keyboard
     */
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View currentFocus = getCurrentFocus();
        if (currentFocus != null) {
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }
    
    /**
     * Navigate to Main
     */
    private void navigateToMain() {
        Intent intent = new Intent(VerifyOtpActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
    
    /**
     * Navigate back to Register
     */
    private void navigateToRegister() {
        Intent intent = new Intent(VerifyOtpActivity.this, RegisterActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
    
    /**
     * Handle back press
     */
    @Override
    public void onBackPressed() {
        navigateToRegister();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        // Unregister receiver if registered
    }
}
