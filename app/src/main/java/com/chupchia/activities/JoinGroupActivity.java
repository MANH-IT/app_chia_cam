package com.chupchia.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.chupchia.R;
import com.chupchia.models.Group;
import com.chupchia.models.Member;
import com.chupchia.utils.SharedPrefManager;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class JoinGroupActivity extends AppCompatActivity {

    // ===== VIEWS =====
    private Toolbar toolbar;
    private ProgressBar progressBar;
    private LinearLayout contentContainer;
    private LinearLayout errorContainer;
    private TextView tvError;
    private MaterialButton btnRetry;
    
    private CircleImageView ivGroupAvatar;
    private TextView tvGroupName;
    private TextView tvMemberCount;
    private CircleImageView ivInviterAvatar;
    private TextView tvInviterName;
    private CardView cardDescription;
    private TextView tvDescription;
    private MaterialButton btnCancel;
    private MaterialButton btnJoin;
    
    // ===== VARIABLES =====
    private String inviteCode;
    private Group groupInfo;
    private Member inviterInfo;
    private boolean isJoining = false;
    private boolean isLoading = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_group);
        
        initViews();
        setupToolbar();
        parseDeepLink();
        loadGroupInfo();
        setupListeners();
    }
    
    /**
     * Initialize views
     */
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progress_bar);
        contentContainer = findViewById(R.id.content_container);
        errorContainer = findViewById(R.id.error_container);
        tvError = findViewById(R.id.tv_error);
        btnRetry = findViewById(R.id.btn_retry);
        
        ivGroupAvatar = findViewById(R.id.iv_group_avatar);
        tvGroupName = findViewById(R.id.tv_group_name);
        tvMemberCount = findViewById(R.id.tv_member_count);
        ivInviterAvatar = findViewById(R.id.iv_inviter_avatar);
        tvInviterName = findViewById(R.id.tv_inviter_name);
        cardDescription = findViewById(R.id.card_description);
        tvDescription = findViewById(R.id.tv_description);
        btnCancel = findViewById(R.id.btn_cancel);
        btnJoin = findViewById(R.id.btn_join);
    }
    
    /**
     * Setup toolbar
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> cancelAndGoBack());
    }
    
    /**
     * Parse deep link to get invite code
     */
    private void parseDeepLink() {
        Intent intent = getIntent();
        Uri data = intent.getData();
        
        if (data != null) {
            // URL format: https://chia.cam/join/ABC123
            // Or chiacam://join/ABC123
            String host = data.getHost();
            List<String> pathSegments = data.getPathSegments();
            
            if ("chia.cam".equals(host) || "join".equals(host)) {
                if (pathSegments.size() > 0) {
                    inviteCode = pathSegments.get(pathSegments.size() - 1);
                }
            }
            
            // Get from query parameter
            if (inviteCode == null) {
                inviteCode = data.getQueryParameter("code");
            }
        }
        
        // Get from extras
        if (inviteCode == null) {
            inviteCode = getIntent().getStringExtra("invite_code");
        }
        
        // For testing - demo code
        if (inviteCode == null) {
            inviteCode = "ABC123";
        }
    }
    
    /**
     * Load group information from server using invite code
     */
    private void loadGroupInfo() {
        if (isLoading) return;
        
        isLoading = true;
        showLoading(true);
        
        // TODO: Call API to get group info by invite code
        simulateLoadGroupInfo();
    }
    
    /**
     * Simulate API call to load group info
     */
    private void simulateLoadGroupInfo() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            isLoading = false;
            showLoading(false);
            
            groupInfo = new Group();
            groupInfo.setId("group_1");
            groupInfo.setName("Nhà mình");
            groupInfo.setAvatarUrl(null);
            groupInfo.setDescription("Nhóm chi tiêu gia đình - cùng nhau quản lý chi phí ăn uống, sinh hoạt hàng ngày. Mọi người cùng nhau chia sẻ và theo dõi chi tiêu một cách minh bạch và dễ dàng.");
            // Add dummy members for count
            for (int i = 0; i < 4; i++) {
                groupInfo.addMember(new Member());
            }
            
            inviterInfo = new Member();
            inviterInfo.setId("user_1");
            inviterInfo.setName("Mạnh Nguyễn");
            inviterInfo.setAvatarUrl("");
            
            updateUI();
            contentContainer.setVisibility(View.VISIBLE);
        }, 1000);
    }
    
    /**
     * Update UI with group info
     */
    private void updateUI() {
        if (groupInfo == null) return;
        
        tvGroupName.setText(groupInfo.getName());
        tvMemberCount.setText(String.format(getString(R.string.join_group_member_count), groupInfo.getMemberCount()));
        
        if (inviterInfo != null) {
            tvInviterName.setText(inviterInfo.getName());
            if (inviterInfo.getAvatarUrl() != null && !inviterInfo.getAvatarUrl().isEmpty()) {
                Glide.with(this)
                    .load(inviterInfo.getAvatarUrl())
                    .placeholder(R.drawable.ic_default_avatar)
                    .into(ivInviterAvatar);
            } else {
                ivInviterAvatar.setImageResource(R.drawable.ic_default_avatar);
            }
        }
        
        if (groupInfo.getAvatarUrl() != null && !groupInfo.getAvatarUrl().isEmpty()) {
            Glide.with(this)
                .load(groupInfo.getAvatarUrl())
                .placeholder(R.drawable.ic_default_group_avatar)
                .into(ivGroupAvatar);
        } else {
            ivGroupAvatar.setImageResource(R.drawable.ic_default_group_avatar);
        }
        
        if (!TextUtils.isEmpty(groupInfo.getDescription())) {
            cardDescription.setVisibility(View.VISIBLE);
            tvDescription.setText(groupInfo.getDescription());
        } else {
            cardDescription.setVisibility(View.GONE);
        }
    }
    
    /**
     * Setup button listeners
     */
    private void setupListeners() {
        btnCancel.setOnClickListener(v -> cancelAndGoBack());
        btnJoin.setOnClickListener(v -> joinGroup());
        btnRetry.setOnClickListener(v -> loadGroupInfo());
    }
    
    /**
     * Join group
     */
    private void joinGroup() {
        if (isJoining) return;
        
        // Check if user is logged in
        SharedPrefManager prefManager = SharedPrefManager.getInstance(this);
        
        if (!prefManager.isLoggedIn()) {
            // Save pending invite for after login
            prefManager.setPendingInviteCode(inviteCode);
            
            Toast.makeText(this, R.string.join_group_login_required, Toast.LENGTH_LONG).show();
            
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra("redirect_after_login", true);
            startActivity(intent);
            finish();
            return;
        }
        
        isJoining = true;
        showLoading(true);
        btnJoin.setEnabled(false);
        btnJoin.setText(R.string.join_group_joining);
        
        // TODO: Call API to join group
        simulateJoinGroup();
    }
    
    /**
     * Simulate API call to join group
     */
    private void simulateJoinGroup() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            isJoining = false;
            showLoading(false);
            btnJoin.setEnabled(true);
            btnJoin.setText(R.string.join_group_btn_join);
            
            Toast.makeText(this, String.format(getString(R.string.join_group_success), groupInfo.getName()), Toast.LENGTH_LONG).show();
            
            // Save current group
            SharedPrefManager.getInstance(this).setCurrentGroupId(groupInfo.getId());
            
            // Navigate to MainActivity
            Intent intent = new Intent(JoinGroupActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("selected_group_id", groupInfo.getId());
            startActivity(intent);
            finish();
        }, 1500);
    }
    
    /**
     * Show/hide loading indicator
     */
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            contentContainer.setVisibility(View.GONE);
            errorContainer.setVisibility(View.GONE);
        }
    }
    
    /**
     * Cancel and go back
     */
    private void cancelAndGoBack() {
        if (isTaskRoot()) {
            if (SharedPrefManager.getInstance(this).isLoggedIn()) {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }
        }
        finish();
    }
}
