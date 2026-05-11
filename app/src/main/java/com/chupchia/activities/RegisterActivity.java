package com.chupchia.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.chupchia.R;
import com.chupchia.utils.SharedPrefManager;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    // ===== PATTERNS =====
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(0|\\+84)(3|5|7|8|9)\\d{8}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L}\\s]{2,}$");
    private static final Pattern PASSWORD_STRONG_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$");
    
    // ===== CONSTANTS =====
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MIN_NAME_LENGTH = 2;
    
    // ===== VIEWS =====
    private View ivLogo;
    private TextView tvTitle;
    private TextView tvSubtitle;
    private TextInputLayout tilFullname;
    private TextInputLayout tilPhone;
    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputLayout tilConfirm;
    private EditText etFullname;
    private EditText etPhone;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirm;
    private MaterialButton btnRegister;
    private TextView tvGoToLogin;
    private TextView tvTerms;
    private TextView tvPrivacy;
    
    // ===== VARIABLES =====
    private ProgressDialog progressDialog;
    private boolean isLoading = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        
        initViews();
        setupListeners();
        setupClearErrorOnType();
        startEnterAnimation();
    }
    
    /**
     * Khởi tạo giao diện
     */
    private void initViews() {
        ivLogo = findViewById(R.id.iv_logo);
        tvTitle = findViewById(R.id.tv_title);
        tvSubtitle = findViewById(R.id.tv_subtitle);
        
        tilFullname = findViewById(R.id.til_fullname);
        tilPhone = findViewById(R.id.til_phone);
        tilEmail = findViewById(R.id.til_email);
        tilPassword = findViewById(R.id.til_password);
        tilConfirm = findViewById(R.id.til_confirm);
        
        etFullname = findViewById(R.id.et_fullname);
        etPhone = findViewById(R.id.et_phone);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirm = findViewById(R.id.et_confirm);
        
        btnRegister = findViewById(R.id.btn_register);
        tvGoToLogin = findViewById(R.id.tv_go_to_login);
        tvTerms = findViewById(R.id.tv_terms);
        tvPrivacy = findViewById(R.id.tv_privacy);
        
        // Cấu hình hộp thoại tiến trình
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang tạo tài khoản...");
        progressDialog.setCancelable(false);
    }
    
    /**
     * Hiệu ứng khi mở màn hình
     */
    private void startEnterAnimation() {
        // Hiệu ứng mờ dần vào cho logo
        ivLogo.setAlpha(0f);
        ivLogo.animate()
            .alpha(1f)
            .setDuration(600)
            .setInterpolator(new AccelerateDecelerateInterpolator())
            .start();
        
        // Hiệu ứng trượt lên cho tiêu đề
        tvTitle.setTranslationY(30f);
        tvTitle.setAlpha(0f);
        tvTitle.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(500)
            .setStartDelay(200)
            .start();
        
        // Hiệu ứng trượt lên cho phụ đề
        tvSubtitle.setTranslationY(30f);
        tvSubtitle.setAlpha(0f);
        tvSubtitle.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(500)
            .setStartDelay(300)
            .start();
        
        // Hiệu ứng mờ dần vào cho các ô nhập liệu
        float startDelay = 400;
        View[] views = {tilFullname, tilPhone, tilEmail, tilPassword, tilConfirm, btnRegister};
        for (View view : views) {
            view.setAlpha(0f);
            view.animate()
                .alpha(1f)
                .setDuration(400)
                .setStartDelay((long) startDelay)
                .start();
            startDelay += 100;
        }
    }
    
    /**
     * Gán sự kiện cho các nút
     */
    private void setupListeners() {
        btnRegister.setOnClickListener(v -> performRegistration());
        tvGoToLogin.setOnClickListener(v -> navigateToLogin());
        
        tvTerms.setOnClickListener(v -> showTermsDialog());
        tvPrivacy.setOnClickListener(v -> showPrivacyDialog());
    }
    
    /**
     * Tự động xóa lỗi khi người dùng bắt đầu gõ
     */
    private void setupClearErrorOnType() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                clearErrorForFocusedField();
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        };
        
        etFullname.addTextChangedListener(textWatcher);
        etPhone.addTextChangedListener(textWatcher);
        etEmail.addTextChangedListener(textWatcher);
        etPassword.addTextChangedListener(textWatcher);
        etConfirm.addTextChangedListener(textWatcher);
        
        // Lắng nghe thay đổi focus
        View.OnFocusChangeListener focusListener = (v, hasFocus) -> {
            if (hasFocus) {
                clearErrorForFocusedField();
            }
        };
        
        etFullname.setOnFocusChangeListener(focusListener);
        etPhone.setOnFocusChangeListener(focusListener);
        etEmail.setOnFocusChangeListener(focusListener);
        etPassword.setOnFocusChangeListener(focusListener);
        etConfirm.setOnFocusChangeListener(focusListener);
    }
    
    /**
     * Xóa lỗi cho field đang được focus
     */
    private void clearErrorForFocusedField() {
        if (etFullname.isFocused()) {
            tilFullname.setError(null);
            tilFullname.setErrorEnabled(false);
        }
        if (etPhone.isFocused()) {
            tilPhone.setError(null);
            tilPhone.setErrorEnabled(false);
        }
        if (etEmail.isFocused()) {
            tilEmail.setError(null);
            tilEmail.setErrorEnabled(false);
        }
        if (etPassword.isFocused()) {
            tilPassword.setError(null);
            tilPassword.setErrorEnabled(false);
        }
        if (etConfirm.isFocused()) {
            tilConfirm.setError(null);
            tilConfirm.setErrorEnabled(false);
        }
    }
    
    /**
     * Thực hiện đăng ký
     */
    private void performRegistration() {
        if (isLoading) return;
        
        String fullname = etFullname.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirm.getText().toString().trim();
        
        // Xác thực tất cả trường
        if (!validateFullname(fullname)) return;
        if (!validatePhone(phone)) return;
        if (!validateEmail(email)) return; // Optional field, only validate if not empty
        if (!validatePassword(password)) return;
        if (!validateConfirmPassword(password, confirmPassword)) return;
        
        // Tất cả xác thực đã qua
        showLoading(true);
        
        // TODO: Gọi API đăng ký thực tế
        simulateRegistrationApi(fullname, phone, email, password);
    }
    
    /**
     * Giả lập API đăng ký (thay thế bằng API thực tế)
     */
    private void simulateRegistrationApi(String fullname, String phone, String email, String password) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            showLoading(false);
            
            // Giả lập kiểm tra số điện thoại đã tồn tại
            // Trong thực tế, gọi API qua Retrofit
            if (phone.equals("0987654321")) {
                showError(getString(R.string.error_phone_exists));
            } else {
                // Lưu dữ liệu tạm người dùng
                saveTempUserData(fullname, phone, email, password);
                // Điều hướng đến xác thực OTP
                navigateToVerifyOtp(phone, fullname, email, password);
            }
        }, 1500);
    }
    
    /**
     * Xác thực họ và tên
     */
    private boolean validateFullname(String fullname) {
        if (TextUtils.isEmpty(fullname)) {
            tilFullname.setError(getString(R.string.error_fullname_empty));
            tilFullname.requestFocus();
            shakeView(tilFullname);
            return false;
        }
        if (fullname.length() < MIN_NAME_LENGTH) {
            tilFullname.setError(getString(R.string.error_fullname_short));
            tilFullname.requestFocus();
            shakeView(tilFullname);
            return false;
        }
        if (!NAME_PATTERN.matcher(fullname).matches()) {
            tilFullname.setError(getString(R.string.error_fullname_invalid));
            tilFullname.requestFocus();
            shakeView(tilFullname);
            return false;
        }
        tilFullname.setError(null);
        return true;
    }
    
    /**
     * Xác thực số điện thoại
     */
    private boolean validatePhone(String phone) {
        if (TextUtils.isEmpty(phone)) {
            tilPhone.setError(getString(R.string.error_phone_empty));
            tilPhone.requestFocus();
            shakeView(tilPhone);
            return false;
        }
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            tilPhone.setError(getString(R.string.error_phone_invalid));
            tilPhone.requestFocus();
            shakeView(tilPhone);
            return false;
        }
        tilPhone.setError(null);
        return true;
    }
    
    /**
     * Xác thực email (không bắt buộc)
     */
    private boolean validateEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            // Email không bắt buộc
            tilEmail.setError(null);
            return true;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.error_email_invalid));
            tilEmail.requestFocus();
            shakeView(tilEmail);
            return false;
        }
        tilEmail.setError(null);
        return true;
    }
    
    /**
     * Xác thực mật khẩu
     */
    private boolean validatePassword(String password) {
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError(getString(R.string.error_password_empty));
            tilPassword.requestFocus();
            shakeView(tilPassword);
            return false;
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            tilPassword.setError(getString(R.string.error_password_short));
            tilPassword.requestFocus();
            shakeView(tilPassword);
            return false;
        }
        
        // Cảnh báo mật khẩu yếu (không chặn)
        if (!PASSWORD_STRONG_PATTERN.matcher(password).matches()) {
            tilPassword.setError(getString(R.string.error_password_weak));
            // Không return false, chỉ cảnh báo
        } else {
            tilPassword.setError(null);
        }
        return true;
    }
    
    /**
     * Xác thực xác nhận mật khẩu
     */
    private boolean validateConfirmPassword(String password, String confirmPassword) {
        if (TextUtils.isEmpty(confirmPassword)) {
            tilConfirm.setError(getString(R.string.error_confirm_empty));
            tilConfirm.requestFocus();
            shakeView(tilConfirm);
            return false;
        }
        if (!password.equals(confirmPassword)) {
            tilConfirm.setError(getString(R.string.error_confirm_mismatch));
            tilConfirm.requestFocus();
            shakeView(tilConfirm);
            return false;
        }
        tilConfirm.setError(null);
        return true;
    }
    
    /**
     * Lưu thông tin tạm thời trước khi xác thực OTP
     */
    private void saveTempUserData(String fullname, String phone, String email, String password) {
        SharedPrefManager.getSharedPreferences().edit()
            .putString("temp_fullname", fullname)
            .putString("temp_phone", phone)
            .putString("temp_email", email)
            .putString("temp_password", password)
            .apply();
    }
    
    /**
     * Điều hướng đến màn hình xác thực OTP
     */
    private void navigateToVerifyOtp(String phone, String fullname, String email, String password) {
        Intent intent = new Intent(RegisterActivity.this, VerifyOtpActivity.class);
        intent.putExtra("phone", phone);
        intent.putExtra("fullname", fullname);
        intent.putExtra("email", email);
        intent.putExtra("password", password);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
    
    /**
     * Điều hướng về màn hình đăng nhập
     */
    private void navigateToLogin() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
    
    /**
     * Hiển thị dialog điều khoản dịch vụ
     */
    private void showTermsDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Điều khoản dịch vụ")
            .setMessage("Điều khoản dịch vụ của Chia Cam sẽ được cập nhật sau...")
            .setPositiveButton(R.string.ok, null)
            .show();
    }
    
    /**
     * Hiển thị dialog chính sách bảo mật
     */
    private void showPrivacyDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Chính sách bảo mật")
            .setMessage("Chính sách bảo mật của Chia Cam sẽ được cập nhật sau...")
            .setPositiveButton(R.string.ok, null)
            .show();
    }
    
    /**
     * Hiệu ứng rung khi có lỗi
     */
    private void shakeView(View view) {
        ObjectAnimator shakeX = ObjectAnimator.ofFloat(view, "translationX", 0f, 15f, -15f, 10f, -10f, 5f, -5f, 0f);
        shakeX.setDuration(500);
        shakeX.start();
    }
    
    /**
     * Hiển thị loading
     */
    private void showLoading(boolean show) {
        isLoading = show;
        if (show) {
            progressDialog.show();
            btnRegister.setEnabled(false);
            btnRegister.setAlpha(0.6f);
            btnRegister.setText(R.string.register_button_loading);
        } else {
            progressDialog.dismiss();
            btnRegister.setEnabled(true);
            btnRegister.setAlpha(1f);
            btnRegister.setText(R.string.register_button);
        }
    }
    
    /**
     * Hiển thị thông báo lỗi
     */
    private void showError(String message) {
        new AlertDialog.Builder(this)
            .setTitle(R.string.error_register_failed)
            .setMessage(message)
            .setPositiveButton(R.string.ok, null)
            .show();
    }
    
    /**
     * Xử lý nút back
     */
    @Override
    public void onBackPressed() {
        navigateToLogin();
    }
}
