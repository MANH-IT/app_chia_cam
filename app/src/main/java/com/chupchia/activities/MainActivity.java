package com.chupchia.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.chupchia.R;
import com.chupchia.adapters.MainViewPagerAdapter;
import com.chupchia.utils.PermissionUtils;
import com.chupchia.utils.SharedPrefManager;

public class MainActivity extends AppCompatActivity {

    // ===== VIEWS =====
    private Toolbar toolbar;
    private BottomNavigationView bottomNavigation;
    private ViewPager2 viewPager;
    private FloatingActionButton fabCamera;
    
    // ===== VARIABLES =====
    private MainViewPagerAdapter viewPagerAdapter;
    private int notificationCount = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        setupToolbar();
        setupViewPager();
        setupBottomNavigation();
        setupListeners();
    }
    
    /**
     * Khởi tạo views
     */
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        viewPager = findViewById(R.id.view_pager);
        fabCamera = findViewById(R.id.fab_camera);
    }
    
    /**
     * Setup toolbar
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }
    
    /**
     * Setup ViewPager2 with 3 tabs
     */
    private void setupViewPager() {
        viewPagerAdapter = new MainViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOffscreenPageLimit(2);
        viewPager.setUserInputEnabled(true);
        
        // Sync ViewPager2 → BottomNavigation
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0:
                        bottomNavigation.setSelectedItemId(R.id.nav_feed);
                        break;
                    case 1:
                        bottomNavigation.setSelectedItemId(R.id.nav_notification);
                        break;
                    case 2:
                        bottomNavigation.setSelectedItemId(R.id.nav_profile);
                        break;
                }
            }
        });
    }
    
    /**
     * Setup BottomNavigationView synced with ViewPager2
     */
    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_feed) {
                viewPager.setCurrentItem(0, true);
                return true;
            } else if (id == R.id.nav_notification) {
                viewPager.setCurrentItem(1, true);
                // Clear notification badge when viewing
                clearNotificationBadge();
                return true;
            } else if (id == R.id.nav_profile) {
                viewPager.setCurrentItem(2, true);
                return true;
            }
            return false;
        });
    }
    
    /**
     * Setup button listeners
     */
    private void setupListeners() {
        fabCamera.setOnClickListener(v -> {
            checkCameraPermissionAndLaunch();
        });
    }

    private void checkCameraPermissionAndLaunch() {
        if (!PermissionUtils.hasCameraPermission(this)) {
            PermissionUtils.requestCameraPermission(this);
        } else {
            launchCamera();
        }
    }

    private void launchCamera() {
        Intent intent = new Intent(MainActivity.this, CameraActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            android.widget.Toast.makeText(this, "Bạn cần cấp quyền Camera để sử dụng tính năng này", android.widget.Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Create options menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    
    /**
     * Handle menu item clicks
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_settings) {
            // TODO: Open SettingsActivity
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * Update notification badge on bottom nav
     */
    public void updateNotificationBadge(int count) {
        notificationCount = count;
        BadgeDrawable badge = bottomNavigation.getOrCreateBadge(R.id.nav_notification);
        if (count > 0) {
            badge.setVisible(true);
            badge.setNumber(count);
            badge.setBackgroundColor(getResources().getColor(R.color.error, getTheme()));
            badge.setBadgeTextColor(getResources().getColor(R.color.white, getTheme()));
        } else {
            badge.setVisible(false);
            badge.clearNumber();
        }
    }
    
    /**
     * Clear notification badge
     */
    private void clearNotificationBadge() {
        notificationCount = 0;
        BadgeDrawable badge = bottomNavigation.getOrCreateBadge(R.id.nav_notification);
        badge.setVisible(false);
        badge.clearNumber();
    }
    
    /**
     * Handle back press - move to background instead of closing
     */
    @Override
    public void onBackPressed() {
        // If not on feed tab, go to feed first
        if (viewPager.getCurrentItem() != 0) {
            viewPager.setCurrentItem(0, true);
        } else {
            moveTaskToBack(true);
        }
    }
    
    /**
     * Switch to Feed tab programmatically
     */
    public void switchToFeed() {
        if (viewPager != null) {
            viewPager.setCurrentItem(0, true);
        }
    }
}
