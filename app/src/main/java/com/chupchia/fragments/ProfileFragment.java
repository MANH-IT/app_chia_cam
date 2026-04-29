package com.chupchia.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import com.chupchia.R;
import com.chupchia.activities.CreateGroupActivity;
import com.chupchia.activities.EditProfileActivity;
import com.chupchia.activities.InviteMembersActivity;
import com.chupchia.activities.LoginActivity;
import com.chupchia.activities.MainActivity;
import com.chupchia.activities.SettingsActivity;
import com.chupchia.adapters.GroupProfileAdapter;
import com.chupchia.models.Group;
import com.chupchia.utils.CurrencyUtils;
import com.chupchia.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    // ===== VIEWS =====
    private SwipeRefreshLayout swipeRefresh;
    private CircleImageView ivAvatar;
    private View flCameraBadge;
    private TextView tvUserName;
    private TextView tvUserContact;
    private MaterialButton btnEditProfile;
    private TextView tvTotalSpent;
    private TextView tvGroupCount;
    private TextView tvBillCount;
    private RecyclerView rvGroups;
    private View llCreateGroup;
    private LinearLayout llSettings;
    private LinearLayout llNotificationSettings;
    private LinearLayout llInviteFriends;
    private LinearLayout llLogout;
    private TextView tvVersion;

    // ===== VARIABLES =====
    private GroupProfileAdapter groupAdapter;
    private List<Group> groupList = new ArrayList<>();
    private ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        loadUserData();
        loadStatistics();
        loadGroups();
        setupListeners();
        setupSwipeRefresh();

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(false);
    }

    private void initViews(View view) {
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        ivAvatar = view.findViewById(R.id.iv_avatar);
        flCameraBadge = view.findViewById(R.id.fl_camera_badge);
        tvUserName = view.findViewById(R.id.tv_user_name);
        tvUserContact = view.findViewById(R.id.tv_user_contact);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        tvTotalSpent = view.findViewById(R.id.tv_total_spent);
        tvGroupCount = view.findViewById(R.id.tv_group_count);
        tvBillCount = view.findViewById(R.id.tv_bill_count);
        rvGroups = view.findViewById(R.id.rv_groups);
        llCreateGroup = view.findViewById(R.id.ll_create_group);
        llSettings = view.findViewById(R.id.ll_settings);
        llNotificationSettings = view.findViewById(R.id.ll_notification_settings);
        llInviteFriends = view.findViewById(R.id.ll_invite_friends);
        llLogout = view.findViewById(R.id.ll_logout);
        tvVersion = view.findViewById(R.id.tv_version);

        tvVersion.setText(String.format("Phiên bản %s", "1.0.0")); // Manual for now or BuildConfig.VERSION_NAME
    }

    private void setupRecyclerView() {
        groupAdapter = new GroupProfileAdapter(getContext());
        groupAdapter.setOnGroupClickListener(group -> {
            // Switch to feed of selected group
            SharedPrefManager.getInstance(getContext()).setCurrentGroupId(group.getId());
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).switchToFeed();
            }
        });

        rvGroups.setLayoutManager(new LinearLayoutManager(getContext()));
        rvGroups.setAdapter(groupAdapter);
    }

    private void loadUserData() {
        SharedPrefManager pref = SharedPrefManager.getInstance(getContext());

        String userName = pref.getUserName();
        String userEmail = pref.getUserEmail();
        String userAvatar = pref.getUserAvatar();

        tvUserName.setText(TextUtils.isEmpty(userName) ? "Người dùng" : userName);

        if (!TextUtils.isEmpty(userEmail)) {
            tvUserContact.setText(userEmail);
        } else {
            tvUserContact.setText("Chưa cập nhật");
        }

        if (!TextUtils.isEmpty(userAvatar)) {
            Glide.with(this)
                    .load(userAvatar)
                    .placeholder(R.drawable.ic_default_avatar)
                    .into(ivAvatar);
        } else {
            ivAvatar.setImageResource(R.drawable.ic_default_avatar);
        }
    }

    private void loadStatistics() {
        new Thread(() -> {
            com.chupchia.database.BillDao billDao = com.chupchia.database.AppDatabase.getInstance(getContext()).billDao();
            long totalSpent = billDao.getTotalSpent();
            int billCount = billDao.getBillCount();
            
            // For now, groups are not fully implemented in DB, so keep as 0 or 1
            int groupCount = 0; 
            
            new Handler(Looper.getMainLooper()).post(() -> {
                if (isAdded()) {
                    tvTotalSpent.setText(CurrencyUtils.formatVND(totalSpent));
                    tvGroupCount.setText(String.valueOf(groupCount));
                    tvBillCount.setText(String.valueOf(billCount));
                }
            });
        }).start();
    }

    private void loadGroups() {
        // Clear demo groups - new account should be empty
        groupList.clear();
        groupAdapter.setGroups(groupList);
        
        // TODO: Call API to get user's real groups from database/server
    }

    private void setupListeners() {
        flCameraBadge.setOnClickListener(v -> showImagePickerDialog());
        btnEditProfile.setOnClickListener(v -> navigateToEditProfile());
        llCreateGroup.setOnClickListener(v -> navigateToCreateGroup());
        llSettings.setOnClickListener(v -> navigateToSettings());
        llNotificationSettings.setOnClickListener(v -> Toast.makeText(getContext(), "Chức năng đang phát triển", Toast.LENGTH_SHORT).show());
        llInviteFriends.setOnClickListener(v -> navigateToInviteFriends());
        llLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setColorSchemeResources(R.color.primary);
        swipeRefresh.setOnRefreshListener(() -> {
            loadUserData();
            loadStatistics();
            loadGroups();
            swipeRefresh.setRefreshing(false);
        });
    }

    private void showImagePickerDialog() {
        String[] options = {"Chụp ảnh mới", "Chọn từ thư viện"};

        new AlertDialog.Builder(requireContext())
                .setTitle("Chọn ảnh đại diện")
                .setItems(options, (dialog, which) -> {
                    Toast.makeText(getContext(), "Chức năng đang phát triển", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void navigateToEditProfile() {
        Intent intent = new Intent(getContext(), EditProfileActivity.class);
        startActivity(intent);
    }

    private void navigateToCreateGroup() {
        Intent intent = new Intent(getContext(), CreateGroupActivity.class);
        startActivity(intent);
    }

    private void navigateToSettings() {
        Intent intent = new Intent(getContext(), SettingsActivity.class);
        startActivity(intent);
    }

    private void navigateToInviteFriends() {
        Intent intent = new Intent(getContext(), InviteMembersActivity.class);
        startActivity(intent);
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.logout_title)
                .setMessage(R.string.logout_message)
                .setPositiveButton(R.string.logout_confirm, (dialog, which) -> logout())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void logout() {
        progressDialog.setMessage("Đang đăng xuất...");
        progressDialog.show();

        // Clear SharedPrefs
        SharedPrefManager.getInstance(getContext()).logout();
        
        // Clear Room Database
        new Thread(() -> {
            com.chupchia.database.AppDatabase.getInstance(getContext()).clearAllTables();
            
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                progressDialog.dismiss();

                Intent intent = new Intent(getContext(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

                if (getActivity() != null) {
                    getActivity().finish();
                }
            }, 1000);
        }).start();
    }
}
