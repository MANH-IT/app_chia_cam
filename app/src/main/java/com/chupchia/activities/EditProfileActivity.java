package com.chupchia.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.chupchia.R;
import com.chupchia.utils.SharedPrefManager;

public class EditProfileActivity extends AppCompatActivity {

    private TextInputEditText etName;
    private TextInputEditText etPhone;
    private TextInputEditText etEmail;
    private TextInputLayout tilName;
    private MaterialButton btnSave;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initViews();
        setupToolbar();
        loadUserData();
        setupListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etName = findViewById(R.id.et_name);
        etPhone = findViewById(R.id.et_phone);
        etEmail = findViewById(R.id.et_email);
        btnSave = findViewById(R.id.btn_save);
        
        // Lấy TextInputLayout cha để xử lý lỗi
        tilName = (TextInputLayout) etName.getParent().getParent();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.edit_profile_title);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadUserData() {
        SharedPrefManager pref = SharedPrefManager.getInstance(this);
        
        String name = pref.getUserName();
        String phone = pref.getUserPhone();
        String email = pref.getUserEmail();
        
        if (!TextUtils.isEmpty(name)) etName.setText(name);
        if (!TextUtils.isEmpty(phone)) etPhone.setText(phone);
        if (!TextUtils.isEmpty(email)) etEmail.setText(email);
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveProfile());
        
        // Xóa lỗi khi nhập
        etName.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && tilName != null) {
                tilName.setError(null);
                tilName.setErrorEnabled(false);
            }
        });
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        // Kiểm tra tính hợp lệ của tên
        if (TextUtils.isEmpty(name)) {
            if (tilName != null) {
                tilName.setError("Vui lòng nhập họ tên");
                tilName.setErrorEnabled(true);
            }
            etName.requestFocus();
            return;
        }

        // Xác thực định dạng email nếu có
        if (!TextUtils.isEmpty(email) && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            etEmail.requestFocus();
            return;
        }

        // Xác thực định dạng số điện thoại nếu có
        if (!TextUtils.isEmpty(phone) && phone.length() < 9) {
            Toast.makeText(this, "Số điện thoại không hợp lệ", Toast.LENGTH_SHORT).show();
            etPhone.requestFocus();
            return;
        }

        showLoading(true);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            showLoading(false);

            // Lưu vào SharedPreferences
            SharedPrefManager pref = SharedPrefManager.getInstance(this);
            pref.setUserName(name);
            if (!TextUtils.isEmpty(phone)) pref.setUserPhone(phone);
            if (!TextUtils.isEmpty(email)) pref.setUserEmail(email);

            Toast.makeText(this, R.string.edit_profile_save_success, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        }, 500);
    }

    private void showLoading(boolean show) {
        btnSave.setEnabled(!show);
        btnSave.setText(show ? "Đang lưu..." : getString(R.string.save));
        btnSave.setAlpha(show ? 0.6f : 1f);
    }
}
