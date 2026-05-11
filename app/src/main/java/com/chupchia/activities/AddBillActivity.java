package com.chupchia.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chupchia.R;
import com.chupchia.adapters.MemberSplitAdapter;
import com.chupchia.models.Member;
import com.chupchia.utils.CurrencyUtils;
import com.chupchia.utils.DateTimeUtils;
import com.chupchia.utils.OfflineBillQueue;
import com.chupchia.utils.SharedPrefManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddBillActivity extends AppCompatActivity {

    // ===== GIAO DIỆN =====
    private Toolbar toolbar;
    private ImageView ivImagePreview;
    private MaterialButton btnRecapture;
    private TextInputEditText etProductName;
    private TextInputEditText etAmount;
    private TextInputEditText etDate;
    private TextInputEditText etNote;
    private MaterialButton btnSplitEqual;
    private MaterialButton btnSplitPercent;
    private MaterialButton btnSplitCustom;
    private CheckBox cbSelectAll;
    private RecyclerView rvMembers;
    private MaterialCardView cardSplitPreview;
    private TextView tvPerPersonAmount;
    private MaterialButton btnCancel;
    private MaterialButton btnSave;
    
    // ===== BIẾN =====
    private Uri imageUri;
    private String groupId;
    private List<Member> members = new ArrayList<>();
    private MemberSplitAdapter memberAdapter;
    private String currentSplitType = "equal";
    private long totalAmount = 0;
    private Calendar selectedDate = Calendar.getInstance();
    private boolean isSaving = false;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bill);
        
        initViews();
        setupToolbar();
        loadIntentData();
        loadGroupMembers();
        setupAmountFormatting();
        setupDatePicker();
        setupSplitTypeButtons();
        setupMemberAdapter();
        setupListeners();
    }
    
    /**
     * Khởi tạo giao diện
     */
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivImagePreview = findViewById(R.id.iv_image_preview);
        btnRecapture = findViewById(R.id.btn_recapture);
        etProductName = findViewById(R.id.et_product_name);
        etAmount = findViewById(R.id.et_amount);
        etDate = findViewById(R.id.et_date);
        etNote = findViewById(R.id.et_note);
        btnSplitEqual = findViewById(R.id.btn_split_equal);
        btnSplitPercent = findViewById(R.id.btn_split_percent);
        btnSplitCustom = findViewById(R.id.btn_split_custom);
        cbSelectAll = findViewById(R.id.cb_select_all);
        rvMembers = findViewById(R.id.rv_members);
        cardSplitPreview = findViewById(R.id.card_split_preview);
        tvPerPersonAmount = findViewById(R.id.tv_per_person_amount);
        btnCancel = findViewById(R.id.btn_cancel);
        btnSave = findViewById(R.id.btn_save);
    }
    
    /**
     * Cấu hình thanh công cụ với nút quay lại
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
     * Tải dữ liệu từ intent (ảnh từ camera)
     */
    private void loadIntentData() {
        Intent intent = getIntent();
        if (intent == null) return;
        
        groupId = intent.getStringExtra("group_id");
        String imagePath = intent.getStringExtra("image_path");
        String amountText = intent.getStringExtra("amount");
        String productNameText = intent.getStringExtra("product_name");
        
        if (imagePath != null && !imagePath.isEmpty()) {
            imageUri = Uri.parse(imagePath);
            Glide.with(this)
                .load(imageUri)
                .centerCrop()
                .into(ivImagePreview);
        } else {
            ivImagePreview.setImageResource(R.drawable.ic_placeholder);
        }
        
        if (amountText != null && !amountText.isEmpty()) {
            etAmount.setText(amountText);
        }
        
        if (productNameText != null && !productNameText.isEmpty()) {
            etProductName.setText(productNameText);
        }
        
        // Đặt ngày mặc định là hôm nay
        etDate.setText(DateTimeUtils.formatDate(selectedDate.getTime()));
    }
    
    private void loadGroupMembers() {
        members.clear();
        
        // Luôn thêm người dùng hiện tại
        SharedPrefManager pref = SharedPrefManager.getInstance(this);
        members.add(new Member(pref.getUserId(), pref.getUserName(), null, "Chủ chi"));
        
        // Nếu có quyền danh bạ, tải danh bạ thật
        if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) 
                == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            
            new Thread(() -> {
                List<com.chupchia.models.Member> contacts = com.chupchia.utils.ContactUtils.getPhoneContacts(this);
                runOnUiThread(() -> {
                    if (!contacts.isEmpty()) {
                        members.addAll(contacts);
                    }
                    updateMemberAdapter();
                });
            }).start();
        } else {
            updateMemberAdapter();
        }
    }

    private void updateMemberAdapter() {
        if (memberAdapter != null) {
            memberAdapter.notifyDataSetChanged();
            updateSelectAllCheckbox();
        }
    }
    
    /**
     * Cấu hình định dạng số tiền với TextWatcher
     */
    private void setupAmountFormatting() {
        etAmount.addTextChangedListener(new TextWatcher() {
            private String current = "";
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(current)) {
                    etAmount.removeTextChangedListener(this);
                    
                    String cleanString = s.toString().replaceAll("[^0-9]", "");
                    if (!cleanString.isEmpty()) {
                        totalAmount = Long.parseLong(cleanString);
                        String formatted = CurrencyUtils.formatNumber(totalAmount);
                        current = formatted;
                        etAmount.setText(formatted);
                        etAmount.setSelection(formatted.length());
                    } else {
                        totalAmount = 0;
                        etAmount.setText("");
                    }
                    
                    etAmount.addTextChangedListener(this);
                    updateSplitPreview();
                }
            }
        });
    }
    
    /**
     * Cấu hình hộp thoại chọn ngày
     */
    private void setupDatePicker() {
        etDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(year, month, dayOfMonth);
                    etDate.setText(DateTimeUtils.formatDate(selectedDate.getTime()));
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });
    }
    
    /**
     * Cấu hình các nút kiểu chia
     */
    private void setupSplitTypeButtons() {
        btnSplitEqual.setOnClickListener(v -> selectSplitType("equal"));
        btnSplitPercent.setOnClickListener(v -> selectSplitType("percent"));
        btnSplitCustom.setOnClickListener(v -> selectSplitType("custom"));
    }
    
    /**
     * Chọn kiểu chia
     */
    private void selectSplitType(String type) {
        currentSplitType = type;
        
        // Đặt lại kiểu nút
        btnSplitEqual.setBackgroundTintList(getColorStateList(R.color.gray_light));
        btnSplitPercent.setBackgroundTintList(getColorStateList(R.color.gray_light));
        btnSplitCustom.setBackgroundTintList(getColorStateList(R.color.gray_light));
        
        // Làm nổi bật lựa chọn
        switch (type) {
            case "equal":
                btnSplitEqual.setBackgroundTintList(getColorStateList(R.color.primary_light));
                break;
            case "percent":
                btnSplitPercent.setBackgroundTintList(getColorStateList(R.color.primary_light));
                break;
            case "custom":
                btnSplitCustom.setBackgroundTintList(getColorStateList(R.color.primary_light));
                break;
        }
        
        memberAdapter.setSplitType(type);
        updateSplitPreview();
    }
    
    /**
     * Cấu hình adapter thành viên
     */
    private void setupMemberAdapter() {
        memberAdapter = new MemberSplitAdapter(members);
        memberAdapter.setOnMemberValueChangedListener(new MemberSplitAdapter.OnMemberValueChangedListener() {
            @Override
            public void onMemberSelectedChanged() {
                updateSelectAllCheckbox();
                updateSplitPreview();
            }
            
            @Override
            public void onMemberValueChanged() {
                updateSplitPreview();
            }
        });
        
        rvMembers.setLayoutManager(new LinearLayoutManager(this));
        rvMembers.setAdapter(memberAdapter);
    }
    
    /**
     * Cấu hình các sự kiện khác
     */
    private void setupListeners() {
        cbSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            memberAdapter.selectAll(isChecked);
        });
        
        btnRecapture.setOnClickListener(v -> {
            // TODO: Mở lại camera
            finish();
        });
        
        btnCancel.setOnClickListener(v -> onBackPressed());
        
        btnSave.setOnClickListener(v -> saveBill());
    }
    
    /**
     * Cập nhật trạng thái checkbox chọn tất cả
     */
    private void updateSelectAllCheckbox() {
        boolean allSelected = true;
        for (Member member : members) {
            if (!member.isSelected()) {
                allSelected = false;
                break;
            }
        }
        cbSelectAll.setChecked(allSelected);
    }
    
    /**
     * Cập nhật xem trước chia tiền
     */
    private void updateSplitPreview() {
        List<Member> selectedMembers = memberAdapter.getSelectedMembers();
        
        if (totalAmount <= 0 || selectedMembers.isEmpty()) {
            cardSplitPreview.setVisibility(View.GONE);
            return;
        }
        
        cardSplitPreview.setVisibility(View.VISIBLE);
        
        long perPersonAmount;
        switch (currentSplitType) {
            case "equal":
                perPersonAmount = totalAmount / selectedMembers.size();
                break;
            case "percent":
                int totalPercent = memberAdapter.getTotalPercent();
                if (totalPercent == 100 && totalPercent > 0) {
                    perPersonAmount = calculateWeightedAverage(selectedMembers);
                } else {
                    perPersonAmount = totalAmount / selectedMembers.size();
                }
                break;
            case "custom":
                long totalCustom = memberAdapter.getTotalCustomAmount();
                if (totalCustom == totalAmount && totalCustom > 0) {
                    perPersonAmount = totalCustom / selectedMembers.size();
                } else {
                    perPersonAmount = totalAmount / selectedMembers.size();
                }
                break;
            default:
                perPersonAmount = totalAmount / selectedMembers.size();
        }
        
        tvPerPersonAmount.setText(CurrencyUtils.formatVND(perPersonAmount));
    }
    
    /**
     * Tính trung bình có trọng số cho chia theo phần trăm
     */
    private long calculateWeightedAverage(List<Member> selectedMembers) {
        if (totalAmount <= 0 || selectedMembers.isEmpty()) return 0;
        
        long total = 0;
        for (Member member : selectedMembers) {
            int percent = member.getCustomValue();
            total += (totalAmount * percent) / 100;
        }
        return total / selectedMembers.size();
    }
    
    /**
     * Xác thực và lưu hóa đơn
     */
    private void saveBill() {
        if (isSaving) return;
        
        String productName = etProductName.getText().toString().trim();
        if (TextUtils.isEmpty(productName)) {
            ((TextInputLayout) findViewById(R.id.til_product_name)).setError(getString(R.string.add_bill_error_product));
            etProductName.requestFocus();
            return;
        }
        
        if (totalAmount <= 0) {
            ((TextInputLayout) findViewById(R.id.til_amount)).setError(getString(R.string.add_bill_error_amount));
            etAmount.requestFocus();
            return;
        }
        
        List<Member> selectedMembers = memberAdapter.getSelectedMembers();
        if (selectedMembers.isEmpty()) {
            Toast.makeText(this, R.string.add_bill_error_members, Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Xác thực tổng phần trăm
        if (currentSplitType.equals("percent")) {
            int totalPercent = memberAdapter.getTotalPercent();
            if (totalPercent != 100) {
                showErrorDialog(getString(R.string.add_bill_error_percent_sum) + " (Hiện tại: " + totalPercent + "%)");
                return;
            }
        }
        
        // Xác thực tổng tùy chỉnh
        if (currentSplitType.equals("custom")) {
            long totalCustom = memberAdapter.getTotalCustomAmount();
            if (totalCustom != totalAmount) {
                long diff = Math.abs(totalCustom - totalAmount);
                showErrorDialog(getString(R.string.add_bill_error_custom_sum) + " (Chênh lệch: " + CurrencyUtils.formatVND(diff) + ")");
                return;
            }
        }
        
        isSaving = true;
        btnSave.setText(R.string.add_bill_saving);
        btnSave.setEnabled(false);
        
        // TODO: Kiểm tra kết nối mạng
        boolean isNetworkAvailable = true;
        
        if (isNetworkAvailable) {
            saveBillToServer(productName, selectedMembers);
        } else {
            saveBillOffline(productName, selectedMembers);
        }
    }
    
    /**
     * Lưu hóa đơn lên server
     */
    private void saveBillToServer(String productName, List<Member> selectedMembers) {
        // Tạo object Bill thật từ dữ liệu đã nhập
        com.chupchia.models.Bill newBill = new com.chupchia.models.Bill();
        newBill.setId(String.valueOf(System.currentTimeMillis()));
        newBill.setProductName(productName);
        newBill.setAmount(totalAmount);
        newBill.setGroupId(groupId != null ? groupId : "default_group");
        newBill.setTimestamp(System.currentTimeMillis());
        newBill.setSplitCount(selectedMembers.size());
        newBill.setSplitType(currentSplitType);
        newBill.setNote(etNote.getText().toString());
        newBill.setImageUrl(imageUri != null ? imageUri.toString() : "");
        newBill.setActorName(com.chupchia.utils.SharedPrefManager.getInstance(this).getUserName());

        // Lưu vào Database
        new Thread(() -> {
            com.chupchia.database.AppDatabase.getInstance(this).billDao().insertBill(newBill);
            
            runOnUiThread(() -> {
                isSaving = false;
                btnSave.setText(R.string.add_bill_save);
                btnSave.setEnabled(true);
                
                Toast.makeText(AddBillActivity.this, "Đã lưu hóa đơn thành công!", Toast.LENGTH_SHORT).show();
                
                Intent intent = new Intent();
                intent.putExtra("bill_created", true);
                setResult(RESULT_OK, intent);
                finish();
            });
        }).start();
    }
    
    /**
     * Lưu hóa đơn ngoại tuyến
     */
    private void saveBillOffline(String productName, List<Member> selectedMembers) {
        List<String> memberIds = new ArrayList<>();
        for (Member member : selectedMembers) {
            memberIds.add(member.getId());
        }
        
        String imageUriString = imageUri != null ? imageUri.toString() : "";
        
        OfflineBillQueue.getInstance(this).addBill(
            imageUriString,
            productName,
            String.valueOf(totalAmount),
            currentSplitType,
            memberIds
        );
        
        Toast.makeText(this, R.string.add_bill_offline_saved, Toast.LENGTH_LONG).show();
        
        Intent intent = new Intent();
        intent.putExtra("bill_created", true);
        setResult(RESULT_OK, intent);
        finish();
    }
    
    /**
     * Hiển thị hộp thoại lỗi
     */
    private void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
            .setTitle(R.string.add_bill_error)
            .setMessage(message)
            .setPositiveButton(R.string.ok, null)
            .show();
    }
}
