package com.chupchia.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.chupchia.R;
import com.chupchia.utils.SharedPrefManager;

public class SplashActivity extends AppCompatActivity {

    // ===== CONSTANTS =====
    private static final int SPLASH_MIN_DURATION = 1500;      // 1.5 giây tối thiểu
    private static final int FADE_IN_DURATION = 800;          // 0.8 giây fade in
    private static final int SCALE_DURATION = 800;            // 0.8 giây scale
    private static final int TYPING_DELAY = 50;               // 50ms mỗi ký tự
    
    // ===== VIEWS =====
    private LinearLayout containerMain;
    private ImageView ivLogo;
    private ProgressBar progressBar;
    private TextView tvLoading;
    private TextView tvSlogan;
    private View circle1, circle2, circle3;
    
    // ===== VARIABLES =====
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable navigationRunnable;
    private long startTime;
    private boolean isNavigating = false;
    private final String fullSlogan = "Chụp là chia – Chia là cam";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        startTime = System.currentTimeMillis();
        initViews();
        startAnimations();
        startTypingAnimation();
        startLoadingSequence();
    }
    
    /**
     * Khởi tạo views
     */
    private void initViews() {
        containerMain = findViewById(R.id.container_main);
        ivLogo = findViewById(R.id.iv_logo);
        progressBar = findViewById(R.id.progress_bar);
        tvLoading = findViewById(R.id.tv_loading);
        tvSlogan = findViewById(R.id.tv_slogan);
        circle1 = findViewById(R.id.circle1);
        circle2 = findViewById(R.id.circle2);
        circle3 = findViewById(R.id.circle3);
    }
    
    /**
     * Chạy tất cả animations cùng lúc
     */
    private void startAnimations() {
        // Fade in cho container chính
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(containerMain, "alpha", 0f, 1f);
        fadeIn.setDuration(FADE_IN_DURATION);
        fadeIn.setInterpolator(new AccelerateDecelerateInterpolator());
        
        // Scale animation cho logo
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 0.7f, 1f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 0.7f, 1f);
        ObjectAnimator scaleLogo = ObjectAnimator.ofPropertyValuesHolder(ivLogo, scaleX, scaleY);
        scaleLogo.setDuration(SCALE_DURATION);
        scaleLogo.setInterpolator(new BounceInterpolator());
        
        // Rotation nhẹ cho logo
        ObjectAnimator rotation = ObjectAnimator.ofFloat(ivLogo, "rotation", 0f, 360f);
        rotation.setDuration(SCALE_DURATION * 2);
        rotation.setInterpolator(new AccelerateDecelerateInterpolator());
        
        // Animation cho các circle floating
        startCircleAnimation(circle1, 0);
        startCircleAnimation(circle2, 500);
        startCircleAnimation(circle3, 1000);
        
        // Chạy đồng thời
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(fadeIn, scaleLogo, rotation);
        animatorSet.start();
    }
    
    /**
     * Animation cho circle floating
     */
    private void startCircleAnimation(View circle, long delay) {
        circle.setAlpha(0f);
        circle.setScaleX(0.5f);
        circle.setScaleY(0.5f);
        
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(circle, "alpha", 0f, 0.3f);
        fadeIn.setDuration(500);
        fadeIn.setStartDelay(delay);
        
        ObjectAnimator scaleUp = ObjectAnimator.ofFloat(circle, "scaleX", 0.5f, 1.2f);
        scaleUp.setDuration(2000);
        scaleUp.setStartDelay(delay);
        scaleUp.setRepeatCount(ValueAnimator.INFINITE);
        scaleUp.setRepeatMode(ValueAnimator.REVERSE);
        
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(circle, "scaleY", 0.5f, 1.2f);
        scaleUpY.setDuration(2000);
        scaleUpY.setStartDelay(delay);
        scaleUpY.setRepeatCount(ValueAnimator.INFINITE);
        scaleUpY.setRepeatMode(ValueAnimator.REVERSE);
        
        AnimatorSet set = new AnimatorSet();
        set.playTogether(fadeIn, scaleUp, scaleUpY);
        set.start();
    }
    
    /**
     * Typing animation cho slogan
     */
    private void startTypingAnimation() {
        handler.post(new Runnable() {
            int index = 0;
            
            @Override
            public void run() {
                if (index <= fullSlogan.length()) {
                    tvSlogan.setText(fullSlogan.substring(0, index));
                    index++;
                    handler.postDelayed(this, TYPING_DELAY);
                }
            }
        });
    }
    
    /**
     * Bắt đầu sequence loading
     */
    private void startLoadingSequence() {
        navigationRunnable = new Runnable() {
            @Override
            public void run() {
                checkNavigation();
            }
        };
        handler.postDelayed(navigationRunnable, SPLASH_MIN_DURATION);
    }
    
    /**
     * Kiểm tra trạng thái và điều hướng
     */
    private void checkNavigation() {
        if (isNavigating) return;
        
        try {
            isNavigating = true;
            SharedPrefManager prefManager = SharedPrefManager.getInstance(this);
            
            // Kiểm tra đã xem onboarding chưa
            if (!prefManager.hasSeenOnboarding()) {
                navigateToOnboarding();
                return;
            }
            
            // Kiểm tra đăng nhập và token
            if (prefManager.isLoggedIn() && prefManager.getAuthToken() != null) {
                // Kiểm tra token hết hạn
                if (prefManager.isTokenExpired()) {
                    prefManager.clearAuthData();
                    navigateToLogin();
                } else {
                    checkTokenWithServer(prefManager.getAuthToken());
                }
            } else {
                navigateToLogin();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Nếu có lỗi nghiêm trọng (vd: lỗi database), reset lại navigation để user có thể thử lại hoặc xem thông báo
            isNavigating = false;
            navigateToLogin(); // Fallback an toàn
        }
    }
    
    /**
     * Kiểm tra token với server
     */
    private void checkTokenWithServer(String token) {
        showLoading(true);
        
        // Mock: giả lập delay và coi token là hợp lệ
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                showLoading(false);
                navigateToMain();
            }
        }, 800);
    }
    
    /**
     * Hiển thị loading
     */
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        tvLoading.setVisibility(show ? View.VISIBLE : View.GONE);
        
        if (show) {
            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(progressBar, "alpha", 0f, 1f);
            fadeIn.setDuration(300);
            fadeIn.start();
            
            ObjectAnimator textFadeIn = ObjectAnimator.ofFloat(tvLoading, "alpha", 0f, 1f);
            textFadeIn.setDuration(300);
            textFadeIn.start();
        }
    }
    
    /**
     * Hiển thị dialog lỗi mạng
     */
    private void showNetworkErrorDialog() {
        isNavigating = false;
        
        new AlertDialog.Builder(this)
            .setTitle("Không có kết nối mạng")
            .setMessage("Vui lòng kiểm tra kết nối internet và thử lại.")
            .setPositiveButton("Thử lại", (dialog, which) -> {
                isNavigating = false;
                checkNavigation();
            })
            .setNegativeButton("Thoát", (dialog, which) -> {
                finishAffinity();
            })
            .setCancelable(false)
            .show();
    }
    
    /**
     * Điều hướng đến Onboarding
     */
    private void navigateToOnboarding() {
        ensureMinDuration(() -> {
            Intent intent = new Intent(SplashActivity.this, OnboardingActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }
    
    /**
     * Điều hướng đến Login
     */
    private void navigateToLogin() {
        ensureMinDuration(() -> {
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }
    
    /**
     * Điều hướng đến Main
     */
    private void navigateToMain() {
        ensureMinDuration(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }
    
    /**
     * Đảm bảo splash hiển thị ít nhất SPLASH_MIN_DURATION
     */
    private void ensureMinDuration(Runnable action) {
        long elapsedTime = System.currentTimeMillis() - startTime;
        long remainingTime = SPLASH_MIN_DURATION - elapsedTime;
        
        if (remainingTime > 0) {
            handler.postDelayed(action, remainingTime);
        } else {
            action.run();
        }
    }
    
    @Override
    public void onBackPressed() {
        // Do nothing
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && navigationRunnable != null) {
            handler.removeCallbacks(navigationRunnable);
        }
    }
}
