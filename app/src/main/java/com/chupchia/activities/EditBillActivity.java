package com.chupchia.activities;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chupchia.R;
import com.chupchia.adapters.EditHistoryAdapter;
import com.chupchia.adapters.MemberSplitAdapter;
import com.chupchia.dialogs.ConfirmDeleteDialog;
import com.chupchia.models.Bill;
import com.chupchia.models.EditHistory;
import com.chupchia.models.Member;
import com.chupchia.utils.CurrencyUtils;
import com.chupchia.utils.DateTimeUtils;
import com.chupchia.utils.SharedPrefManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class EditBillActivity extends AppCompatActivity implements ConfirmDeleteDialog.OnDeleteConfirmListener {

    // ===== VIEWS =====
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
    private CardView cardWarning;
    private TextView tvWarning;
    private CardView cardEditHistory;
    private RecyclerView rvEditHistory;
    private MaterialButton btnCancel;
    private MaterialButton btnDelete;
    private MaterialButton btnSave;
    
    // ===== VARIABLES =====
    private Bill originalBill;
    private Bill currentBill;
    private List<Member> members = new ArrayList<>();
    private MemberSplitAdapter memberAdapter;
    private EditHistoryAdapter editHistoryAdapter;
    private String currentSplitType = "equal";
    private long totalAmount = 0;
    private Calendar selectedDate = Calendar.getInstance();
    private String currentUserId;
    private boolean canEdit = false;
    private boolean canDelete = false;
    private boolean isBillExpired = false;
    private boolean isSaving = false;
    private ProgressDialog progressDialog;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_bill);
        
        initViews();
        setupToolbar();
        loadBillData();
        loadMembers();
        setupAmountFormatting();
        setupDatePicker();
        setupSplitTypeButtons();
        setupMemberAdapter();
        checkPermissions();
        applyPermissionRestrictions();
        loadEditHistory();
        setupListeners();
    }
    
    /**
     * Initialize views
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
        cardWarning = findViewById(R.id.card_warning);
        tvWarning = findViewById(R.id.tv_warning);
        cardEditHistory = findViewById(R.id.card_edit_history);
        rvEditHistory = findViewById(R.id.rv_edit_history);
        btnCancel = findViewById(R.id.btn_cancel);
        btnDelete = findViewById(R.id.btn_delete);
        btnSave = findViewById(R.id.btn_save);
        
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        
        currentUserId = SharedPrefManager.getInstance(this).getUserId();
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
     * Load bill data from intent
     */
    private void loadBillData() {
        originalBill = (Bill) getIntent().getSerializableExtra("bill");
        if (originalBill == null) {
            // For demo, create a dummy bill
            originalBill = new Bill("1", "", "Cơm trưa văn phòng", 175000, 
                "Mạnh Nguyễn", "1", "1", "1", "group1", 5, "equal", 
                System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000), "Ăn trưa cùng team");
        }
        
        // Create a copy for editing
        currentBill = originalBill.clone();
        
        // Load image
        if (currentBill.getImageUrl() != null && !currentBill.getImageUrl().isEmpty()) {
            Glide.with(this)
                .load(currentBill.getImageUrl())
                .centerCrop()
                .into(ivImagePreview);
        } else {
            ivImagePreview.setImageResource(R.drawable.ic_placeholder);
        }
        
        // Load text fields
        etProductName.setText(currentBill.getProductName());
        totalAmount = currentBill.getAmount();
        etAmount.setText(CurrencyUtils.formatNumber(totalAmount));
        etNote.setText(currentBill.getNote());
        
        // Load date
        selectedDate.setTimeInMillis(currentBill.getTimestamp());
        etDate.setText(DateTimeUtils.formatDate(selectedDate.getTime()));
        
        currentSplitType = currentBill.getSplitType();
        selectSplitType(currentSplitType);
    }
    
    /**
     * Load members (demo data)
     */
    private void loadMembers() {
        // TODO: Load actual members from group
        members.add(new Member("1", "Mạnh Nguyễn", "", "admin"));
        members.add(new Member("2", "Lan Trần", "", "member"));
        members.add(new Member("3", "Huy Phạm", "", "member"));
        members.add(new Member("4", "Linh Lê", "", "member"));
        members.add(new Member("5", "Hoa Ngô", "", "member"));
        
        // Set selected members based on bill data (demo: all selected)
        for (Member member : members) {
            member.setSelected(true);
        }
    }
    
    /**
     * Setup amount formatting
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
                if (!s.toString().equals(current) && canEdit) {
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
                    if (memberAdapter != null) {
                        memberAdapter.setTotalAmount(totalAmount);
                    }
                }
            }
        });
    }
    
    /**
     * Setup date picker
     */
    private void setupDatePicker() {
        etDate.setOnClickListener(v -> {
            if (!canEdit) return;
            
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
     * Setup split type buttons
     */
    private void setupSplitTypeButtons() {
        btnSplitEqual.setOnClickListener(v -> {
            if (canEdit) selectSplitType("equal");
        });
        btnSplitPercent.setOnClickListener(v -> {
            if (canEdit) selectSplitType("percent");
        });
        btnSplitCustom.setOnClickListener(v -> {
            if (canEdit) selectSplitType("custom");
        });
    }
    
    /**
     * Select split type
     */
    private void selectSplitType(String type) {
        currentSplitType = type;
        currentBill.setSplitType(type);
        
        // Reset button styles
        btnSplitEqual.setBackgroundTintList(getColorStateList(R.color.gray_light));
        btnSplitPercent.setBackgroundTintList(getColorStateList(R.color.gray_light));
        btnSplitCustom.setBackgroundTintList(getColorStateList(R.color.gray_light));
        
        // Highlight selected
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
        
        if (memberAdapter != null) {
            memberAdapter.setSplitType(type);
            memberAdapter.setTotalAmount(totalAmount);
        }
        updateSplitPreview();
    }
    
    /**
     * Setup member adapter
     */
    private void setupMemberAdapter() {
        memberAdapter = new MemberSplitAdapter(members);
        memberAdapter.setTotalAmount(totalAmount);
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
        updateSelectAllCheckbox();
    }
    
    /**
     * Update select all checkbox
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
     * Update split preview
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
                    perPersonAmount = totalAmount / selectedMembers.size();
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
     * Check permissions for editing/deleting
     */
    private void checkPermissions() {
        String billCreatorId = currentBill.getCreatorId();
        String groupAdminId = currentBill.getGroupAdminId();
        
        // Check if bill is expired (older than 7 days)
        long daysDiff = (System.currentTimeMillis() - currentBill.getTimestamp()) / (24 * 60 * 60 * 1000);
        isBillExpired = daysDiff > 7;
        
        if (isBillExpired) {
            canEdit = false;
            canDelete = false;
        } else if (currentUserId.equals(billCreatorId)) {
            canEdit = true;
            canDelete = true;
        } else if (currentUserId.equals(groupAdminId)) {
            canEdit = true;
            canDelete = true;
        } else {
            canEdit = false;
            canDelete = false;
        }
    }
    
    /**
     * Apply permission restrictions to UI
     */
    private void applyPermissionRestrictions() {
        if (isBillExpired) {
            cardWarning.setVisibility(View.VISIBLE);
            tvWarning.setText(R.string.edit_bill_warning_expired);
            disableEditing();
        } else if (!canEdit) {
            cardWarning.setVisibility(View.VISIBLE);
            tvWarning.setText(R.string.edit_bill_warning_no_permission);
            disableEditing();
        }
        
        if (!canDelete) {
            btnDelete.setEnabled(false);
            btnDelete.setAlpha(0.5f);
        }
    }
    
    /**
     * Disable editing for all fields
     */
    private void disableEditing() {
        btnSave.setEnabled(false);
        btnSave.setAlpha(0.5f);
        etProductName.setEnabled(false);
        etAmount.setEnabled(false);
        etDate.setEnabled(false);
        etNote.setEnabled(false);
        btnSplitEqual.setEnabled(false);
        btnSplitPercent.setEnabled(false);
        btnSplitCustom.setEnabled(false);
        cbSelectAll.setEnabled(false);
        btnRecapture.setEnabled(false);
    }
    
    /**
     * Load edit history
     */
    private void loadEditHistory() {
        List<EditHistory> historyList = currentBill.getEditHistory();
        if (historyList != null && !historyList.isEmpty()) {
            cardEditHistory.setVisibility(View.VISIBLE);
            editHistoryAdapter = new EditHistoryAdapter(historyList);
            rvEditHistory.setLayoutManager(new LinearLayoutManager(this));
            rvEditHistory.setAdapter(editHistoryAdapter);
        } else {
            cardEditHistory.setVisibility(View.GONE);
        }
    }
    
    /**
     * Setup button listeners
     */
    private void setupListeners() {
        cbSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (canEdit) {
                memberAdapter.selectAll(isChecked);
            }
        });
        
        btnCancel.setOnClickListener(v -> onBackPressed());
        
        btnSave.setOnClickListener(v -> saveBill());
        
        btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog());
        
        btnRecapture.setOnClickListener(v -> {
            // Retake photo logic
            Toast.makeText(this, "Chức năng đang phát triển", Toast.LENGTH_SHORT).show();
        });
    }
    
    /**
     * Save bill changes
     */
    private void saveBill() {
        if (isSaving || !canEdit) return;
        
        String productName = etProductName.getText().toString().trim();
        if (TextUtils.isEmpty(productName)) {
            etProductName.setError(getString(R.string.add_bill_error_product));
            etProductName.requestFocus();
            return;
        }
        
        if (totalAmount <= 0) {
            Toast.makeText(this, R.string.add_bill_error_amount, Toast.LENGTH_SHORT).show();
            etAmount.requestFocus();
            return;
        }
        
        List<Member> selectedMembers = memberAdapter.getSelectedMembers();
        if (selectedMembers.isEmpty()) {
            Toast.makeText(this, R.string.add_bill_error_members, Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Validate percent sum
        if (currentSplitType.equals("percent")) {
            int totalPercent = memberAdapter.getTotalPercent();
            if (totalPercent != 100) {
                Toast.makeText(this, getString(R.string.add_bill_error_percent_sum) + " (Hiện tại: " + totalPercent + "%)", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        
        // Validate custom sum
        if (currentSplitType.equals("custom")) {
            long totalCustom = memberAdapter.getTotalCustomAmount();
            if (totalCustom != totalAmount) {
                long diff = Math.abs(totalCustom - totalAmount);
                Toast.makeText(this, getString(R.string.add_bill_error_custom_sum) + " (Chênh lệch: " + CurrencyUtils.formatVND(diff) + ")", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        
        // Update currentBill temporary object to check changes
        currentBill.setProductName(productName);
        currentBill.setAmount(totalAmount);
        currentBill.setNote(etNote.getText().toString().trim());
        currentBill.setTimestamp(selectedDate.getTimeInMillis());
        currentBill.setSplitType(currentSplitType);
        
        // Check if there are actual changes
        String changesSummary = getChangesSummary();
        if (changesSummary.equals(getString(R.string.edit_history_no_changes))) {
            Toast.makeText(this, R.string.edit_bill_no_changes, Toast.LENGTH_SHORT).show();
            return;
        }
        
        isSaving = true;
        btnSave.setEnabled(false);
        btnSave.setText("Đang lưu...");
        progressDialog.setMessage("Đang cập nhật giao dịch...");
        progressDialog.show();
        
        // Create edit history entry
        EditHistory editEntry = new EditHistory(
            currentUserId,
            SharedPrefManager.getInstance(this).getUserName(),
            System.currentTimeMillis(),
            changesSummary
        );
        
        currentBill.addEditHistory(editEntry);
        
        // Simulate API call
        simulateUpdateBill();
    }
    
    /**
     * Get summary of changes for edit history
     */
    private String getChangesSummary() {
        StringBuilder changes = new StringBuilder();
        
        if (!originalBill.getProductName().equals(currentBill.getProductName())) {
            changes.append(String.format(getString(R.string.edit_history_changed_name),
                    originalBill.getProductName(), currentBill.getProductName()));
        }
        
        if (originalBill.getAmount() != currentBill.getAmount()) {
            if (changes.length() > 0) changes.append("; ");
            changes.append(String.format(getString(R.string.edit_history_changed_amount),
                    CurrencyUtils.formatVND(originalBill.getAmount()),
                    CurrencyUtils.formatVND(currentBill.getAmount())));
        }
        
        if (!originalBill.getSplitType().equals(currentBill.getSplitType())) {
            if (changes.length() > 0) changes.append("; ");
            String typeName = "";
            switch (currentBill.getSplitType()) {
                case "equal": typeName = "Chia đều"; break;
                case "percent": typeName = "Chia theo %"; break;
                case "custom": typeName = "Chia tuỳ chỉnh"; break;
            }
            changes.append(String.format(getString(R.string.edit_history_changed_split_type), "", typeName));
        }
        
        if (changes.length() == 0) {
            return getString(R.string.edit_history_no_changes);
        }
        
        return changes.toString();
    }
    
    /**
     * Simulate update bill API call
     */
    private void simulateUpdateBill() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            isSaving = false;
            if (progressDialog.isShowing()) progressDialog.dismiss();
            
            Toast.makeText(this, R.string.edit_bill_update_success, Toast.LENGTH_SHORT).show();
            
            Intent resultIntent = new Intent();
            resultIntent.putExtra("updated_bill", currentBill);
            setResult(RESULT_OK, resultIntent);
            finish();
        }, 1500);
    }
    
    /**
     * Show delete confirmation dialog
     */
    private void showDeleteConfirmationDialog() {
        ConfirmDeleteDialog dialog = new ConfirmDeleteDialog();
        dialog.setOnDeleteConfirmListener(this);
        dialog.show(getSupportFragmentManager(), "confirm_delete");
    }
    
    /**
     * Handle delete confirmation
     */
    @Override
    public void onConfirm() {
        if (!canDelete) return;
        
        progressDialog.setMessage("Đang xóa giao dịch...");
        progressDialog.show();
        
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (progressDialog.isShowing()) progressDialog.dismiss();
            Toast.makeText(this, R.string.edit_bill_delete_success, Toast.LENGTH_SHORT).show();
            
            Intent resultIntent = new Intent();
            resultIntent.putExtra("bill_deleted", true);
            resultIntent.putExtra("bill_id", currentBill.getId());
            setResult(RESULT_OK, resultIntent);
            finish();
        }, 1500);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
