package com.chupchia.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.chupchia.R;
import com.chupchia.adapters.OnboardingAdapter;
import com.chupchia.models.OnboardingSlide;
import com.chupchia.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    // ===== VIEWS =====
    private TextView tvSkip;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private MaterialButton btnNext;
    
    // ===== VARIABLES =====
    private OnboardingAdapter adapter;
    private List<OnboardingSlide> slides;
    private int currentPosition = 0;
    private boolean isCompleting = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);
        
        initData();
        initViews();
        setupViewPager();
        setupTabLayout();
        setupButtons();
    }
    
    /**
     * Khởi tạo dữ liệu từ strings.xml
     */
    private void initData() {
        slides = new ArrayList<>();
        slides.add(new OnboardingSlide(
            getString(R.string.onboarding_icon_1),
            getString(R.string.onboarding_title_1),
            getString(R.string.onboarding_desc_1),
            getString(R.string.onboarding_sub_1)
        ));
        slides.add(new OnboardingSlide(
            getString(R.string.onboarding_icon_2),
            getString(R.string.onboarding_title_2),
            getString(R.string.onboarding_desc_2),
            getString(R.string.onboarding_sub_2)
        ));
        slides.add(new OnboardingSlide(
            getString(R.string.onboarding_icon_3),
            getString(R.string.onboarding_title_3),
            getString(R.string.onboarding_desc_3),
            getString(R.string.onboarding_sub_3)
        ));
    }
    
    /**
     * Khởi tạo giao diện
     */
    private void initViews() {
        tvSkip = findViewById(R.id.tv_skip);
        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);
        btnNext = findViewById(R.id.btn_next);
    }
    
    /**
     * Cấu hình ViewPager2
     */
    private void setupViewPager() {
        adapter = new OnboardingAdapter(slides);
        viewPager.setAdapter(adapter);
        
        // Hàm gọi lại khi thay đổi trang
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentPosition = position;
                updateButtonText(position);
                animateButtonScale();
            }
        });
    }
    
    /**
     * Hiệu ứng phóng to cho nút
     */
    private void animateButtonScale() {
        btnNext.animate()
            .scaleX(1.05f)
            .scaleY(1.05f)
            .setDuration(150)
            .withEndAction(() -> {
                btnNext.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(150)
                    .start();
            })
            .start();
    }
    
    /**
     * Cấu hình TabLayout với ViewPager2
     */
    private void setupTabLayout() {
        new TabLayoutMediator(tabLayout, viewPager,
            (tab, position) -> {
                // Chỉ báo dấu chấm
            }
        ).attach();
    }
    
    /**
     * Cập nhật text cho nút theo slide hiện tại
     */
    private void updateButtonText(int position) {
        if (position == slides.size() - 1) {
            btnNext.setText(R.string.onboarding_start);
        } else {
            btnNext.setText(R.string.onboarding_next);
        }
    }
    
    /**
     * Cấu hình các nút bấm
     */
    private void setupButtons() {
        // Nút Next/Bắt đầu
        btnNext.setOnClickListener(v -> {
            if (currentPosition == slides.size() - 1) {
                completeOnboarding();
            } else {
                viewPager.setCurrentItem(currentPosition + 1, true);
            }
        });
        
        // Nút Bỏ qua
        tvSkip.setOnClickListener(v -> {
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(tvSkip, "alpha", 1f, 0f);
            fadeOut.setDuration(200);
            fadeOut.start();
            completeOnboarding();
        });
    }
    
    /**
     * Hoàn thành onboarding, lưu trạng thái và chuyển sang màn hình đăng nhập
     */
    private void completeOnboarding() {
        if (isCompleting) return;
        isCompleting = true;
        
        // Lưu trạng thái đã xem onboarding
        SharedPrefManager.getInstance(this).setHasSeenOnboarding(true);
        
        // Hiệu ứng mờ dần ra cho toàn bộ màn hình
        View rootView = findViewById(android.R.id.content);
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(rootView, "alpha", 1f, 0f);
        fadeOut.setDuration(400);
        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Chuyển sang màn hình đăng nhập
                Intent intent = new Intent(OnboardingActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        });
        fadeOut.start();
    }
    
    @Override
    public void onBackPressed() {
        if (currentPosition > 0) {
            viewPager.setCurrentItem(currentPosition - 1, true);
        } else {
            super.onBackPressed();
        }
    }
}
