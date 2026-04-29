package com.chupchia.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.chupchia.R;
import com.chupchia.utils.SharedPrefManager;

public class LoginActivity extends AppCompatActivity {

    // UI Components
    private TextInputEditText etUsername, etPassword;
    private TextInputLayout tilUsername, tilPassword;
    private MaterialButton btnLogin;
    private TextView tvForgotPassword, tvGoToRegister;
    private CardView cvZalo, cvGoogle, cvFacebook;

    // Loading dialog
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        setupListeners();
    }

    /**
     * Initialize all UI components
     */
    private void initViews() {
        // Input fields
        tilUsername = findViewById(R.id.til_username);
        tilPassword = findViewById(R.id.til_password);
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);

        // Buttons
        btnLogin = findViewById(R.id.btn_login);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        tvGoToRegister = findViewById(R.id.tv_go_to_register);

        // Social login buttons
        cvZalo = findViewById(R.id.cv_zalo);
        cvGoogle = findViewById(R.id.cv_google);
        cvFacebook = findViewById(R.id.cv_facebook);

        // Progress Dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang đăng nhập...");
        progressDialog.setCancelable(false);
    }

    /**
     * Setup all click listeners
     */
    private void setupListeners() {
        // Login button click
        btnLogin.setOnClickListener(v -> handleLogin());

        // Forgot password click
        tvForgotPassword.setOnClickListener(v -> handleForgotPassword());

        // Go to register click
        tvGoToRegister.setOnClickListener(v -> navigateToRegister());

        // Social login clicks
        cvZalo.setOnClickListener(v -> handleZaloLogin());
        cvGoogle.setOnClickListener(v -> handleGoogleLogin());
        cvFacebook.setOnClickListener(v -> handleFacebookLogin());

        // Clear errors when user starts typing
        setupClearErrorOnType();
    }

    /**
     * Clear error messages when user starts typing
     */
    private void setupClearErrorOnType() {
        View.OnFocusChangeListener focusChangeListener = (v, hasFocus) -> {
            if (hasFocus) {
                View parent1 = (View) v.getParent();
                if (parent1 != null) {
                    View parent2 = (View) parent1.getParent();
                    if (parent2 instanceof TextInputLayout) {
                        TextInputLayout til = (TextInputLayout) parent2;
                        til.setError(null);
                        til.setErrorEnabled(false);
                    }
                }
            }
        };

        etUsername.setOnFocusChangeListener(focusChangeListener);
        etPassword.setOnFocusChangeListener(focusChangeListener);
    }

    /**
     * Handle login with username/phone and password
     */
    private void handleLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate inputs
        if (!validateUsername(username)) return;
        if (!validatePassword(password)) return;

        // Show loading
        showLoading();

        // Simulate login
        simulateLogin(username, password);
    }

    private boolean validateUsername(String username) {
        if (TextUtils.isEmpty(username)) {
            tilUsername.setError("Vui lòng nhập số điện thoại hoặc email");
            tilUsername.setErrorEnabled(true);
            etUsername.requestFocus();
            return false;
        }
        tilUsername.setError(null);
        tilUsername.setErrorEnabled(false);
        return true;
    }

    private boolean validatePassword(String password) {
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Vui lòng nhập mật khẩu");
            tilPassword.setErrorEnabled(true);
            etPassword.requestFocus();
            return false;
        }
        if (password.length() < 6) {
            tilPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            tilPassword.setErrorEnabled(true);
            etPassword.requestFocus();
            return false;
        }
        tilPassword.setError(null);
        tilPassword.setErrorEnabled(false);
        return true;
    }

    private void showLoading() {
        progressDialog.show();
        btnLogin.setEnabled(false);
        btnLogin.setAlpha(0.6f);
    }

    private void hideLoading() {
        progressDialog.dismiss();
        btnLogin.setEnabled(true);
        btnLogin.setAlpha(1f);
    }

    private void simulateLogin(String username, String password) {
        new android.os.Handler().postDelayed(() -> {
            hideLoading();
            Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
            SharedPrefManager.getInstance(LoginActivity.this).setLoggedIn(true);
            SharedPrefManager.getInstance(LoginActivity.this).saveToken("demo_token_" + System.currentTimeMillis());
            SharedPrefManager.getInstance(LoginActivity.this).saveUser(
                    "user_123", "Người Dùng", username,
                    username.contains("@") ? username : username + "@example.com", null
            );
            navigateToMain();
        }, 1500);
    }

    private void handleForgotPassword() {
        Toast.makeText(this, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
    }

    private void handleZaloLogin() {
        showLoading();
        new android.os.Handler().postDelayed(() -> {
            hideLoading();
            Toast.makeText(LoginActivity.this, "Đăng nhập Zalo thành công!", Toast.LENGTH_SHORT).show();
            SharedPrefManager.getInstance(LoginActivity.this).setLoggedIn(true);
            SharedPrefManager.getInstance(LoginActivity.this).saveToken("zalo_token_" + System.currentTimeMillis());
            navigateToMain();
        }, 1500);
    }

    private void handleGoogleLogin() {
        showLoading();
        new android.os.Handler().postDelayed(() -> {
            hideLoading();
            Toast.makeText(LoginActivity.this, "Đăng nhập Google thành công!", Toast.LENGTH_SHORT).show();
            SharedPrefManager.getInstance(LoginActivity.this).setLoggedIn(true);
            SharedPrefManager.getInstance(LoginActivity.this).saveToken("google_token_" + System.currentTimeMillis());
            navigateToMain();
        }, 1500);
    }

    private void handleFacebookLogin() {
        showLoading();
        new android.os.Handler().postDelayed(() -> {
            hideLoading();
            Toast.makeText(LoginActivity.this, "Đăng nhập Facebook thành công!", Toast.LENGTH_SHORT).show();
            SharedPrefManager.getInstance(LoginActivity.this).setLoggedIn(true);
            SharedPrefManager.getInstance(LoginActivity.this).saveToken("fb_token_" + System.currentTimeMillis());
            navigateToMain();
        }, 1500);
    }


    private void navigateToRegister() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
