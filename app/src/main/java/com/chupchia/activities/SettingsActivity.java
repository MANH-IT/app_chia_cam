package com.chupchia.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.chupchia.R;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREF_THEME_MODE = "theme_mode";
    private TextView tvThemeValue;
    private TextView tvLanguageValue;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("ChiaCamSettings", MODE_PRIVATE);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.settings_title);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        setupSettings();
    }

    private void setupSettings() {
        tvThemeValue = findViewById(R.id.tv_theme_value);
        tvLanguageValue = findViewById(R.id.tv_language_value);

        // Tải cài đặt giao diện đã lưu
        int savedMode = prefs.getInt(PREF_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        updateThemeLabel(savedMode);

        // Sự kiện nhấp giao diện
        LinearLayout llTheme = findViewById(R.id.ll_theme);
        llTheme.setOnClickListener(v -> showThemeDialog());

        // Sự kiện nhấp ngôn ngữ
        LinearLayout llLanguage = findViewById(R.id.ll_language);
        llLanguage.setOnClickListener(v -> showLanguageDialog());

        // Xóa bộ nhớ đệm
        LinearLayout llClearCache = findViewById(R.id.ll_clear_cache);
        llClearCache.setOnClickListener(v -> showClearCacheDialog());

        // Giới thiệu
        LinearLayout llAbout = findViewById(R.id.ll_about);
        llAbout.setOnClickListener(v -> showAboutDialog());
    }

    private void updateThemeLabel(int mode) {
        switch (mode) {
            case AppCompatDelegate.MODE_NIGHT_YES:
                tvThemeValue.setText("Tối");
                break;
            case AppCompatDelegate.MODE_NIGHT_NO:
                tvThemeValue.setText("Sáng");
                break;
            default:
                tvThemeValue.setText("Theo hệ thống");
                break;
        }
    }

    private void showThemeDialog() {
        String[] themes = {"Sáng", "Tối", "Theo hệ thống"};
        int savedMode = prefs.getInt(PREF_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        int checkedItem;
        switch (savedMode) {
            case AppCompatDelegate.MODE_NIGHT_NO:
                checkedItem = 0;
                break;
            case AppCompatDelegate.MODE_NIGHT_YES:
                checkedItem = 1;
                break;
            default:
                checkedItem = 2;
                break;
        }

        new AlertDialog.Builder(this)
            .setTitle("Chọn giao diện")
            .setSingleChoiceItems(themes, checkedItem, (dialog, which) -> {
                int newMode;
                switch (which) {
                    case 0:
                        newMode = AppCompatDelegate.MODE_NIGHT_NO;
                        break;
                    case 1:
                        newMode = AppCompatDelegate.MODE_NIGHT_YES;
                        break;
                    default:
                        newMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                        break;
                }
                
                // Lưu cài đặt giao diện
                prefs.edit().putInt(PREF_THEME_MODE, newMode).apply();
                updateThemeLabel(newMode);
                AppCompatDelegate.setDefaultNightMode(newMode);
                dialog.dismiss();
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    private void showLanguageDialog() {
        String[] languages = {"Tiếng Việt", "English"};
        new AlertDialog.Builder(this)
            .setTitle("Chọn ngôn ngữ")
            .setSingleChoiceItems(languages, 0, (dialog, which) -> {
                if (which == 1) {
                    Toast.makeText(this, "English will be supported in future updates", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Đã chọn Tiếng Việt", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    private void showClearCacheDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Xóa bộ nhớ đệm")
            .setMessage("Dữ liệu tạm (ảnh cache, file tạm) sẽ bị xóa. Dữ liệu hóa đơn và nhóm sẽ không bị ảnh hưởng.")
            .setPositiveButton("Xóa", (dialog, which) -> {
                // Xóa cache Glide trên luồng nền
                new Thread(() -> {
                    try {
                        com.bumptech.glide.Glide.get(getApplicationContext()).clearDiskCache();
                    } catch (Exception ignored) {}
                }).start();
                // Xóa cache bộ nhớ trên luồng chính (yêu cầu của Glide)
                com.bumptech.glide.Glide.get(this).clearMemory();
                
                Toast.makeText(this, "Đã xóa bộ nhớ đệm", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Về ứng dụng")
            .setMessage("🍊 Chia Cam - Chụp là chia, chia là cam\n\n"
                + "Phiên bản: 1.0.0\n"
                + "Package: com.chupchia\n\n"
                + "Ứng dụng chia bill thông minh sử dụng AI/OCR nhận diện hóa đơn tự động.\n\n"
                + "© 2026 ChupChia Team")
            .setPositiveButton(R.string.close, null)
            .show();
    }

    /**
     * Gọi hàm này từ Application.onCreate() để khôi phục giao diện đã lưu
     */
    public static void applyPersistedTheme(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences("ChiaCamSettings", MODE_PRIVATE);
        int savedMode = prefs.getInt(PREF_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(savedMode);
    }
}
