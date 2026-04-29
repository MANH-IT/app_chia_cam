package com.chupchia.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.chupchia.R;
import com.chupchia.adapters.MemberChipAdapter;
import com.chupchia.dialogs.AddMemberDialog;
import com.chupchia.models.Group;
import com.chupchia.models.Member;
import com.chupchia.utils.PermissionUtils;
import com.chupchia.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateGroupActivity extends AppCompatActivity {

    // ===== CONSTANTS =====
    private static final int REQUEST_IMAGE_PICK = 100;
    private static final int REQUEST_CAMERA = 101;
    private static final int REQUEST_CONTACT_PERMISSION = 102;
    
    // ===== VIEWS =====
    private Toolbar toolbar;
    private CircleImageView ivGroupAvatar;
    private View flCameraBadge;
    private TextInputEditText etGroupName;
    private TextInputEditText etDescription;
    private TextView tvMemberCount;
    private RecyclerView rvMembers;
    private MaterialButton btnAddMember;
    private SwitchMaterial switchDefaultSplit;
    private SwitchMaterial switchMemberInvite;
    private SwitchMaterial switchAutoReminder;
    private MaterialButton btnCreateGroup;
    
    // ===== VARIABLES =====
    private List<Member> membersList = new ArrayList<>();
    private MemberChipAdapter memberAdapter;
    private String currentUserId;
    private String currentUserName;
    private Uri selectedImageUri;
    private ProgressDialog progressDialog;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        
        loadCurrentUser();
        initViews();
        setupToolbar();
        setupRecyclerView();
        addCreatorAsMember();
        setupListeners();
    }
    
    /**
     * Initialize views
     */
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivGroupAvatar = findViewById(R.id.iv_group_avatar);
        flCameraBadge = findViewById(R.id.fl_camera_badge);
        etGroupName = findViewById(R.id.et_group_name);
        etDescription = findViewById(R.id.et_description);
        tvMemberCount = findViewById(R.id.tv_member_count);
        rvMembers = findViewById(R.id.rv_members);
        btnAddMember = findViewById(R.id.btn_add_member);
        switchDefaultSplit = findViewById(R.id.switch_default_split);
        switchMemberInvite = findViewById(R.id.switch_member_invite);
        switchAutoReminder = findViewById(R.id.switch_auto_reminder);
        btnCreateGroup = findViewById(R.id.btn_create_group);
        
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
     * Setup RecyclerView for member chips
     */
    private void setupRecyclerView() {
        memberAdapter = new MemberChipAdapter(this, currentUserId);
        memberAdapter.setOnMemberDeleteListener(member -> {
            membersList.remove(member);
            memberAdapter.setMembers(membersList);
            updateMemberCount();
        });
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, 
                LinearLayoutManager.HORIZONTAL, false);
        rvMembers.setLayoutManager(layoutManager);
        rvMembers.setAdapter(memberAdapter);
    }
    
    /**
     * Load current user from SharedPreferences
     */
    private void loadCurrentUser() {
        SharedPrefManager prefManager = SharedPrefManager.getInstance(this);
        currentUserId = prefManager.getUserId();
        currentUserName = prefManager.getUserName();
        
        if (TextUtils.isEmpty(currentUserName)) {
            currentUserName = "Tôi";
        }
    }
    
    /**
     * Add creator as first member (admin)
     */
    private void addCreatorAsMember() {
        Member creator = new Member(currentUserId, currentUserName, "", "admin");
        creator.setSelected(true);
        membersList.add(creator);
        memberAdapter.setMembers(membersList);
        updateMemberCount();
    }
    
    /**
     * Update member count display
     */
    private void updateMemberCount() {
        tvMemberCount.setText(String.format("(%d thành viên)", membersList.size()));
    }
    
    /**
     * Setup button listeners
     */
    private void setupListeners() {
        flCameraBadge.setOnClickListener(v -> showImagePickerDialog());
        
        btnAddMember.setOnClickListener(v -> {
            if (PermissionUtils.hasContactPermission(this)) {
                showAddMemberDialog();
            } else {
                PermissionUtils.requestContactPermission(this);
            }
        });
        
        btnCreateGroup.setOnClickListener(v -> createGroup());
    }
    
    // Using PermissionUtils instead
    
    /**
     * Show image picker dialog
     */
    private void showImagePickerDialog() {
        String[] options = {"Chụp ảnh mới", "Chọn từ thư viện", "Xóa ảnh"};
        
        new AlertDialog.Builder(this)
                .setTitle("Chọn ảnh nhóm")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openCamera();
                    } else if (which == 1) {
                        openGallery();
                    } else if (which == 2) {
                        selectedImageUri = null;
                        ivGroupAvatar.setImageResource(R.drawable.ic_default_group_avatar);
                    }
                })
                .show();
    }
    
    /**
     * Open camera to take photo
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
     * Open gallery to pick image
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }
    
    /**
     * Show add member dialog
     */
    private void showAddMemberDialog() {
        AddMemberDialog dialog = new AddMemberDialog(this, null, members -> {
            for (Member member : members) {
                if (!isMemberExists(member.getId())) {
                    membersList.add(member);
                }
            }
            memberAdapter.setMembers(membersList);
            updateMemberCount();
            Toast.makeText(this, "Đã thêm " + members.size() + " thành viên", Toast.LENGTH_SHORT).show();
        });
        dialog.show();
    }
    
    /**
     * Check if member already exists
     */
    private boolean isMemberExists(String memberId) {
        for (Member member : membersList) {
            if (member.getId() != null && member.getId().equals(memberId)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Create new group
     */
    private void createGroup() {
        String groupName = etGroupName.getText().toString().trim();
        
        if (TextUtils.isEmpty(groupName)) {
            TextInputLayout tilGroupName = findViewById(R.id.til_group_name);
            tilGroupName.setError(getString(R.string.create_group_error_name));
            etGroupName.requestFocus();
            return;
        }
        
        if (membersList.isEmpty()) {
            Toast.makeText(this, R.string.create_group_error_members, Toast.LENGTH_SHORT).show();
            return;
        }
        
        progressDialog.setMessage(getString(R.string.create_group_creating));
        progressDialog.show();
        
        // Create group object
        Group newGroup = new Group();
        newGroup.setName(groupName);
        newGroup.setDescription(etDescription.getText().toString().trim());
        newGroup.setAdminId(currentUserId);
        newGroup.setAdminName(currentUserName);
        newGroup.setMembers(membersList);
        newGroup.setInviteCode(generateInviteCode());
        newGroup.setSettings(new Group.GroupSettings(
                switchDefaultSplit.isChecked(),
                switchMemberInvite.isChecked(),
                switchAutoReminder.isChecked()
        ));
        
        if (selectedImageUri != null) {
            newGroup.setAvatarUrl(selectedImageUri.toString());
            // TODO: Upload image to server
        }
        
        // TODO: Call API to create group
        simulateCreateGroup(newGroup);
    }
    
    /**
     * Generate random invite code
     */
    private String generateInviteCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }
    
    /**
     * Simulate API call to create group
     */
    private void simulateCreateGroup(Group group) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isFinishing()) {
                progressDialog.dismiss();
                
                Toast.makeText(this, R.string.create_group_success, Toast.LENGTH_SHORT).show();
                
                Intent resultIntent = new Intent();
                resultIntent.putExtra("group_created", true);
                resultIntent.putExtra("group", group);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        }, 1500);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_IMAGE_PICK) {
                selectedImageUri = data.getData();
                Glide.with(this).load(selectedImageUri).into(ivGroupAvatar);
            } else if (requestCode == REQUEST_CAMERA) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    android.graphics.Bitmap imageBitmap = (android.graphics.Bitmap) extras.get("data");
                    ivGroupAvatar.setImageBitmap(imageBitmap);
                    selectedImageUri = getImageUri(imageBitmap);
                }
            }
        }
    }
    
    /**
     * Convert bitmap to Uri
     */
    private Uri getImageUri(android.graphics.Bitmap bitmap) {
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "GroupAvatar", null);
        return Uri.parse(path);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == REQUEST_CONTACT_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showAddMemberDialog();
            } else {
                Toast.makeText(this, "Cần quyền truy cập danh bạ để thêm thành viên", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
