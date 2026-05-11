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

    // Các thành phần giao diện người dùng
    private TextInputEditText etUsername, etPassword;
    private TextInputLayout tilUsername, tilPassword;
    private MaterialButton btnLogin;
    private TextView tvForgotPassword, tvGoToRegister;
    private CardView cvZalo, cvGoogle, cvFacebook;

    // Hộp thoại đang tải
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        setupListeners();
    }

    /**
     * Khởi tạo tất cả thành phần giao diện
     */
    private void initViews() {
        // Các trường nhập liệu
        tilUsername = findViewById(R.id.til_username);
        tilPassword = findViewById(R.id.til_password);
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);

        // Các nút bấm
        btnLogin = findViewById(R.id.btn_login);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        tvGoToRegister = findViewById(R.id.tv_go_to_register);

        // Nút đăng nhập mạng xã hội
        cvZalo = findViewById(R.id.cv_zalo);
        cvGoogle = findViewById(R.id.cv_google);
        cvFacebook = findViewById(R.id.cv_facebook);

        // Hộp thoại tiến trình
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang đăng nhập...");
        progressDialog.setCancelable(false);
    }

    /**
     * Cấu hình tất cả sự kiện nhấp
     */
    private void setupListeners() {
        // Sự kiện nhấp nút đăng nhập
        btnLogin.setOnClickListener(v -> handleLogin());

        // Sự kiện nhấp quên mật khẩu
        tvForgotPassword.setOnClickListener(v -> handleForgotPassword());

        // Sự kiện nhấp đi đến đăng ký
        tvGoToRegister.setOnClickListener(v -> navigateToRegister());

        // Sự kiện nhấp đăng nhập mạng xã hội
        cvZalo.setOnClickListener(v -> handleZaloLogin());
        cvGoogle.setOnClickListener(v -> handleGoogleLogin());
        cvFacebook.setOnClickListener(v -> handleFacebookLogin());

        // Xóa lỗi khi người dùng bắt đầu nhập
        setupClearErrorOnType();
    }

    /**
     * Xóa thông báo lỗi khi người dùng bắt đầu nhập
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
     * Xử lý đăng nhập với tên/số điện thoại và mật khẩu
     */
    private void handleLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Kiểm tra tính hợp lệ của đầu vào
        if (!validateUsername(username)) return;
        if (!validatePassword(password)) return;

        // Hiển thị đang tải
        showLoading();

        // Giả lập đăng nhập
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
            
            SharedPrefManager pref = SharedPrefManager.getInstance(LoginActivity.this);
            pref.setLoggedIn(true);
            pref.saveToken("demo_token_" + System.currentTimeMillis());
            
            // Xác định tên đăng nhập là số điện thoại hay email
            String userId = "user_" + System.currentTimeMillis();
            String phone = null;
            String email = null;
            
            if (username.contains("@")) {
                email = username;
            } else {
                phone = username;
            }
            
            // Lưu người dùng với dữ liệu nhập làm định danh
            // Tên mặc định là số điện thoại/email vì không có trường tên trong đăng nhập
            String displayName = phone != null ? phone : username.split("@")[0];
            pref.saveUser(userId, displayName, phone, email, null);
            
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
            
            SharedPrefManager pref = SharedPrefManager.getInstance(LoginActivity.this);
            pref.setLoggedIn(true);
            pref.saveToken("zalo_token_" + System.currentTimeMillis());
            pref.saveUser(
                "zalo_" + System.currentTimeMillis(),
                "Người dùng Zalo",
                null, null, null
            );
            
            navigateToMain();
        }, 1500);
    }

    private void handleGoogleLogin() {
        showLoading();
        new android.os.Handler().postDelayed(() -> {
            hideLoading();
            Toast.makeText(LoginActivity.this, "Đăng nhập Google thành công!", Toast.LENGTH_SHORT).show();
            
            SharedPrefManager pref = SharedPrefManager.getInstance(LoginActivity.this);
            pref.setLoggedIn(true);
            pref.saveToken("google_token_" + System.currentTimeMillis());
            pref.saveUser(
                "google_" + System.currentTimeMillis(),
                "Người dùng Google",
                null, null, null
            );
            
            navigateToMain();
        }, 1500);
    }

    private void handleFacebookLogin() {
        showLoading();
        new android.os.Handler().postDelayed(() -> {
            hideLoading();
            Toast.makeText(LoginActivity.this, "Đăng nhập Facebook thành công!", Toast.LENGTH_SHORT).show();
            
            SharedPrefManager pref = SharedPrefManager.getInstance(LoginActivity.this);
            pref.setLoggedIn(true);
            pref.saveToken("fb_token_" + System.currentTimeMillis());
            pref.saveUser(
                "fb_" + System.currentTimeMillis(),
                "Người dùng Facebook",
                null, null, null
            );
            
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
