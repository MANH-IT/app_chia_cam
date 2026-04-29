package com.chupchia.activities;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.chupchia.R;
import com.chupchia.adapters.GroupMemberAdapter;
import com.chupchia.models.Group;
import com.chupchia.models.Member;
import com.chupchia.utils.CurrencyUtils;
import com.chupchia.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupDetailActivity extends AppCompatActivity {

    // ===== CONSTANTS =====
    private static final int REQUEST_IMAGE_PICK = 100;
    private static final int REQUEST_CAMERA = 101;

    // ===== VIEWS =====
    private Toolbar toolbar;
    private CircleImageView ivGroupAvatar;
    private View flEditAvatar;
    private TextView tvGroupName;
    private ImageView ivEditName;
    private TextView tvMemberCount;
    private TextView tvInviteCode;
    private ImageView ivCopyCode;
    private TextView tvTotalSpent;
    private TextView tvBillCount;
    private TextView tvAvgPerPerson;
    private RecyclerView rvMembers;
    private CardView cardSettings;
    private MaterialButton btnInviteMembers;
    private View llEditName;
    private View llTransferAdmin;
    private View llDeleteGroup;

    // ===== VARIABLES =====
    private Group currentGroup;
    private List<Member> membersList = new ArrayList<>();
    private GroupMemberAdapter memberAdapter;
    private String currentUserId;
    private boolean isAdmin;
    private ProgressDialog progressDialog;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);

        currentUserId = SharedPrefManager.getInstance(this).getUserId();

        initViews();
        setupToolbar();
        loadGroupData();
        loadMembers();
        setupListeners();
    }

    /**
     * Initialize views
     */
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivGroupAvatar = findViewById(R.id.iv_group_avatar);
        flEditAvatar = findViewById(R.id.fl_edit_avatar);
        tvGroupName = findViewById(R.id.tv_group_name);
        ivEditName = findViewById(R.id.iv_edit_name);
        tvMemberCount = findViewById(R.id.tv_member_count);
        tvInviteCode = findViewById(R.id.tv_invite_code);
        ivCopyCode = findViewById(R.id.iv_copy_code);
        tvTotalSpent = findViewById(R.id.tv_total_spent);
        tvBillCount = findViewById(R.id.tv_bill_count);
        tvAvgPerPerson = findViewById(R.id.tv_avg_per_person);
        rvMembers = findViewById(R.id.rv_members);
        cardSettings = findViewById(R.id.card_settings);
        btnInviteMembers = findViewById(R.id.btn_invite_members);
        llEditName = findViewById(R.id.ll_edit_name);
        llTransferAdmin = findViewById(R.id.ll_transfer_admin);
        llDeleteGroup = findViewById(R.id.ll_delete_group);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
    }

    /**
     * Setup toolbar
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    /**
     * Load group data from intent or API
     */
    private void loadGroupData() {
        // TODO: Load actual group data from API
        // For demo, create sample data
        currentGroup = (Group) getIntent().getSerializableExtra("group");
        if (currentGroup == null) {
            currentGroup = new Group();
            currentGroup.setId("group_1");
            currentGroup.setName("Nhà mình");
            currentGroup.setDescription("Nhóm chi tiêu gia đình");
            currentGroup.setAdminId("user_1");
            currentGroup.setAdminName("Mạnh Nguyễn");
            currentGroup.setInviteCode("ABC123");
            currentGroup.setAvatarUrl(null);
        }

        // Update UI
        tvGroupName.setText(currentGroup.getName());
        tvInviteCode.setText(String.format(getString(R.string.group_detail_invite_code), currentGroup.getInviteCode()));

        // Load avatar
        if (currentGroup.getAvatarUrl() != null && !currentGroup.getAvatarUrl().isEmpty()) {
            Glide.with(this).load(currentGroup.getAvatarUrl()).into(ivGroupAvatar);
        }

        // Check if current user is admin
        isAdmin = currentUserId != null && currentUserId.equals(currentGroup.getAdminId());

        // Show/hide admin controls
        if (isAdmin) {
            flEditAvatar.setVisibility(View.VISIBLE);
            ivEditName.setVisibility(View.VISIBLE);
            cardSettings.setVisibility(View.VISIBLE);
        } else {
            flEditAvatar.setVisibility(View.GONE);
            ivEditName.setVisibility(View.GONE);
            cardSettings.setVisibility(View.GONE);
        }
    }

    /**
     * Load members list
     */
    private void loadMembers() {
        // TODO: Load actual members from API
        // For demo, create sample members
        membersList.clear();

        Member admin = new Member("user_1", "Mạnh Nguyễn", "", "admin");
        admin.setBalance(0);
        membersList.add(admin);

        Member member1 = new Member("user_2", "Lan Vũ", "", "member");
        member1.setBalance(75000);
        membersList.add(member1);

        Member member2 = new Member("user_3", "Bình Trần", "", "member");
        member2.setBalance(-50000);
        membersList.add(member2);

        Member member3 = new Member("user_4", "Hoa Lê", "", "member");
        member3.setBalance(-25000);
        membersList.add(member3);

        tvMemberCount.setText(membersList.size() + " " + getString(R.string.group_detail_members));

        // Setup adapter
        memberAdapter = new GroupMemberAdapter(this, currentUserId, isAdmin);
        memberAdapter.setMembers(membersList);
        memberAdapter.setOnMemberActionListener(new GroupMemberAdapter.OnMemberActionListener() {
            @Override
            public void onTransferAdmin(Member member) {
                transferAdmin(member);
            }

            @Override
            public void onRemoveMember(Member member) {
                removeMember(member);
            }

            @Override
            public void onLeaveGroup() {
                leaveGroup();
            }
        });

        rvMembers.setLayoutManager(new LinearLayoutManager(this));
        rvMembers.setAdapter(memberAdapter);

        // Load stats (demo data)
        tvTotalSpent.setText(CurrencyUtils.formatVND(2450000));
        tvBillCount.setText("24");
        tvAvgPerPerson.setText(CurrencyUtils.formatVND(612500));
    }

    /**
     * Setup button listeners
     */
    private void setupListeners() {
        flEditAvatar.setOnClickListener(v -> showImagePickerDialog());
        ivEditName.setOnClickListener(v -> editGroupName());
        ivCopyCode.setOnClickListener(v -> copyInviteCode());
        btnInviteMembers.setOnClickListener(v -> inviteMembers());

        llEditName.setOnClickListener(v -> editGroupName());
        llTransferAdmin.setOnClickListener(v -> showTransferAdminDialog());
        llDeleteGroup.setOnClickListener(v -> showDeleteGroupDialog());
    }

    /**
     * Show image picker dialog
     */
    private void showImagePickerDialog() {
        String[] options = {"Chụp ảnh mới", "Chọn từ thư viện"};

        new AlertDialog.Builder(this)
                .setTitle("Chọn ảnh nhóm")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openCamera();
                    } else {
                        openGallery();
                    }
                })
                .show();
    }

    /**
     * Open camera
     */
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CAMERA);
        } else {
            Toast.makeText(this, "Không thể mở camera", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Open gallery
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    /**
     * Edit group name
     */
    private void editGroupName() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.edit_group_name_title);

        final EditText input = new EditText(this);
        input.setText(currentGroup.getName());
        input.setSelection(input.getText().length());
        builder.setView(input);

        builder.setPositiveButton(R.string.save, (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!TextUtils.isEmpty(newName)) {
                // TODO: Call API to update group name
                currentGroup.setName(newName);
                tvGroupName.setText(newName);
                Toast.makeText(this, R.string.edit_group_name_success, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    /**
     * Copy invite code to clipboard
     */
    private void copyInviteCode() {
        String inviteText = String.format(getString(R.string.invite_message_body),
                currentGroup.getName(), currentGroup.getInviteCode());

        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("invite_code", inviteText);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(this, R.string.group_detail_copy_success, Toast.LENGTH_SHORT).show();
    }

    /**
     * Show transfer admin dialog
     */
    private void showTransferAdminDialog() {
        // Get non-admin members
        List<Member> potentialAdmins = new ArrayList<>();
        for (Member member : membersList) {
            if (!member.isAdmin() && member.getId() != null && !member.getId().equals(currentUserId)) {
                potentialAdmins.add(member);
            }
        }

        if (potentialAdmins.isEmpty()) {
            Toast.makeText(this, "Không có thành viên nào để chuyển quyền admin", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] names = new String[potentialAdmins.size()];
        for (int i = 0; i < potentialAdmins.size(); i++) {
            names[i] = potentialAdmins.get(i).getName();
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.transfer_admin_title)
                .setItems(names, (dialog, which) -> {
                    transferAdmin(potentialAdmins.get(which));
                })
                .show();
    }

    /**
     * Transfer admin to another member
     */
    private void transferAdmin(Member newAdmin) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.transfer_admin_title)
                .setMessage(String.format(getString(R.string.transfer_admin_message), newAdmin.getName()))
                .setPositiveButton(R.string.transfer_admin_title, (dialog, which) -> {
                    showLoading("Đang chuyển quyền...");
                    // TODO: Call API to transfer admin
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        hideLoading();
                        Toast.makeText(this, String.format(getString(R.string.transfer_admin_success), newAdmin.getName()), Toast.LENGTH_SHORT).show();
                        finish(); // Return to refresh
                    }, 1000);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    /**
     * Remove member from group
     */
    private void removeMember(Member member) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.remove_member_title)
                .setMessage(String.format(getString(R.string.remove_member_message), member.getName()))
                .setPositiveButton(R.string.remove, (dialog, which) -> {
                    showLoading("Đang xóa thành viên...");
                    // TODO: Call API to remove member
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        membersList.remove(member);
                        memberAdapter.setMembers(membersList);
                        tvMemberCount.setText(membersList.size() + " " + getString(R.string.group_detail_members));
                        hideLoading();
                        Toast.makeText(this, String.format(getString(R.string.remove_member_success), member.getName()), Toast.LENGTH_SHORT).show();
                    }, 1000);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    /**
     * Leave group
     */
    private void leaveGroup() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.leave_group_title)
                .setMessage(String.format(getString(R.string.leave_group_message), currentGroup.getName()))
                .setPositiveButton(R.string.leave, (dialog, which) -> {
                    showLoading("Đang rời nhóm...");
                    // TODO: Call API to leave group
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        hideLoading();
                        Toast.makeText(this, R.string.leave_group_success, Toast.LENGTH_SHORT).show();
                        finish();
                    }, 1000);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    /**
     * Show delete group dialog
     */
    private void showDeleteGroupDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_group_title)
                .setMessage(String.format(getString(R.string.delete_group_message), currentGroup.getName()))
                .setPositiveButton(R.string.delete_group_confirm, (dialog, which) -> {
                    showLoading("Đang xóa nhóm...");
                    // TODO: Call API to delete group
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        hideLoading();
                        Toast.makeText(this, R.string.delete_group_success, Toast.LENGTH_SHORT).show();
                        finish();
                    }, 1500);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    /**
     * Invite members (share invite code)
     */
    private void inviteMembers() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.invite_message_title));
        shareIntent.putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.invite_message_body),
                currentGroup.getName(), currentGroup.getInviteCode()));
        startActivity(Intent.createChooser(shareIntent, "Mời thành viên qua"));
    }

    /**
     * Show loading dialog
     */
    private void showLoading(String message) {
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    /**
     * Hide loading dialog
     */
    private void hideLoading() {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.group_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            loadMembers();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_IMAGE_PICK) {
                selectedImageUri = data.getData();
                Glide.with(this).load(selectedImageUri).into(ivGroupAvatar);
                // TODO: Upload image to server
            } else if (requestCode == REQUEST_CAMERA) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    android.graphics.Bitmap imageBitmap = (android.graphics.Bitmap) extras.get("data");
                    ivGroupAvatar.setImageBitmap(imageBitmap);
                    // TODO: Upload image to server
                }
            }
        }
    }
}
