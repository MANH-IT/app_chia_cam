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
import com.chupchia.R;
import com.chupchia.utils.SharedPrefManager;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etName;
    private EditText etPhone;
    private EditText etEmail;
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
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.edit_profile_title);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadUserData() {
        SharedPrefManager pref = SharedPrefManager.getInstance(this);
        etName.setText(pref.getUserName());
        etPhone.setText(pref.getUserEmail()); // Note: SharedPrefManager has getUserEmail, but used for display phone logic before
        etEmail.setText(pref.getUserEmail());
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Vui lòng nhập họ tên");
            return;
        }

        // TODO: Call API to update profile
        showLoading(true);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            showLoading(false);

            // Save to SharedPreferences
            SharedPrefManager pref = SharedPrefManager.getInstance(this);
            pref.setUserName(name);
            if (!TextUtils.isEmpty(email)) pref.setUserEmail(email);

            Toast.makeText(this, R.string.edit_profile_save_success, Toast.LENGTH_SHORT).show();
            finish();
        }, 1000);
    }

    private void showLoading(boolean show) {
        btnSave.setEnabled(!show);
        btnSave.setText(show ? "Đang lưu..." : getString(R.string.save));
    }
}
