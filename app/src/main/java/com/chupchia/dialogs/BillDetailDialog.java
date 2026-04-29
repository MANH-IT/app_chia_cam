package com.chupchia.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.chupchia.R;
import com.chupchia.adapters.EditHistoryAdapter;
import com.chupchia.adapters.ReactionUserAdapter;
import com.chupchia.adapters.SplitDetailAdapter;
import com.chupchia.models.Bill;
import com.chupchia.models.EditHistory;
import com.chupchia.models.ReactionGroup;
import com.chupchia.models.SplitData;
import com.chupchia.utils.CurrencyUtils;
import com.chupchia.utils.DateTimeUtils;
import com.chupchia.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BillDetailDialog extends BottomSheetDialog {

    private Context context;
    private Bill bill;
    private String groupAdminId;
    private OnBillActionListener listener;
    
    // Views
    private PhotoView ivFullImage;
    private ImageView ivClose;
    private TextView tvProductName;
    private TextView tvAmount;
    private TextView tvActorTime;
    private RecyclerView rvSplitList;
    private RecyclerView rvReactions;
    private TextView tvNoReaction;
    private TextView tvHistoryTitle;
    private RecyclerView rvEditHistory;
    private TextView tvNoteTitle;
    private TextView tvNote;
    private View cardWarning;
    private TextView tvWarning;
    private MaterialButton btnEdit;
    private MaterialButton btnDelete;
    
    // Adapters
    private SplitDetailAdapter splitAdapter;
    private ReactionUserAdapter reactionAdapter;
    private EditHistoryAdapter editHistoryAdapter;
    
    public interface OnBillActionListener {
        void onEditClick(Bill bill);
        void onDeleteClick(Bill bill);
    }
    
    public BillDetailDialog(@NonNull Context context, Bill bill, String groupAdminId, OnBillActionListener listener) {
        super(context);
        this.context = context;
        this.bill = bill;
        this.groupAdminId = groupAdminId;
        this.listener = listener;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_bill_detail);
        
        initViews();
        setupBottomSheetBehavior();
        loadBillData();
        setupSplitList();
        setupReactionList();
        setupEditHistory();
        setupListeners();
        checkPermissions();
    }
    
    private void initViews() {
        ivFullImage = findViewById(R.id.iv_full_image);
        ivClose = findViewById(R.id.iv_close);
        tvProductName = findViewById(R.id.tv_product_name);
        tvAmount = findViewById(R.id.tv_amount);
        tvActorTime = findViewById(R.id.tv_actor_time);
        rvSplitList = findViewById(R.id.rv_split_list);
        rvReactions = findViewById(R.id.rv_reactions);
        tvNoReaction = findViewById(R.id.tv_no_reaction);
        tvHistoryTitle = findViewById(R.id.tv_history_title);
        rvEditHistory = findViewById(R.id.rv_edit_history);
        tvNoteTitle = findViewById(R.id.tv_note_title);
        tvNote = findViewById(R.id.tv_note);
        cardWarning = findViewById(R.id.card_warning);
        tvWarning = findViewById(R.id.tv_warning);
        btnEdit = findViewById(R.id.btn_edit);
        btnDelete = findViewById(R.id.btn_delete);
        
        splitAdapter = new SplitDetailAdapter(context);
        reactionAdapter = new ReactionUserAdapter(context);
        editHistoryAdapter = new EditHistoryAdapter(context);
        
        rvSplitList.setLayoutManager(new LinearLayoutManager(context));
        rvSplitList.setAdapter(splitAdapter);
        
        rvReactions.setLayoutManager(new LinearLayoutManager(context));
        rvReactions.setAdapter(reactionAdapter);
        
        rvEditHistory.setLayoutManager(new LinearLayoutManager(context));
        rvEditHistory.setAdapter(editHistoryAdapter);
    }
    
    private void setupBottomSheetBehavior() {
        View bottomSheet = findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setPeekHeight(600);
            behavior.setSkipCollapsed(false);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }
    
    private void loadBillData() {
        if (bill == null) return;
        
        // Load image with pinch to zoom
        if (bill.getImageUrl() != null && !bill.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(bill.getImageUrl())
                    .placeholder(R.drawable.ic_placeholder)
                    .into(ivFullImage);
        }
        
        tvProductName.setText(bill.getProductName());
        tvAmount.setText(CurrencyUtils.formatVND(bill.getAmount()));
        
        String actorTime = bill.getActorName() + " mua • " + DateTimeUtils.formatDateTime(bill.getTimestamp());
        tvActorTime.setText(actorTime);
        
        // Set note
        if (!TextUtils.isEmpty(bill.getNote())) {
            tvNoteTitle.setVisibility(View.VISIBLE);
            tvNote.setVisibility(View.VISIBLE);
            tvNote.setText(bill.getNote());
        } else {
            tvNoteTitle.setVisibility(View.GONE);
            tvNote.setVisibility(View.GONE);
        }
    }
    
    private void setupSplitList() {
        // Create demo split data or from bill if available
        List<SplitData> splits = new ArrayList<>();
        int count = Math.max(1, bill.getSplitCount());
        long perPersonAmount = bill.getAmount() / count;
        
        splits.add(new SplitData("user_1", "Mạnh Nguyễn", null, perPersonAmount));
        splits.add(new SplitData("user_2", "Lan Vũ", null, perPersonAmount));
        splits.add(new SplitData("user_3", "Bình Trần", null, perPersonAmount));
        
        splitAdapter.setSplits(splits);
    }
    
    private void setupReactionList() {
        // Build reaction groups
        Map<String, List<String>> reactionMap = new HashMap<>();
        
        // Demo data
        List<String> happyUsers = new ArrayList<>();
        happyUsers.add("Mạnh Nguyễn");
        happyUsers.add("Lan Vũ");
        reactionMap.put("😂", happyUsers);
        
        List<ReactionGroup> reactionGroups = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : reactionMap.entrySet()) {
            ReactionGroup group = new ReactionGroup(entry.getKey());
            for (String userName : entry.getValue()) {
                group.addUser(new ReactionGroup.ReactionUser("", userName, null));
            }
            reactionGroups.add(group);
        }
        
        if (!reactionGroups.isEmpty()) {
            tvNoReaction.setVisibility(View.GONE);
            rvReactions.setVisibility(View.VISIBLE);
            reactionAdapter.setReactionGroups(reactionGroups);
        } else {
            tvNoReaction.setVisibility(View.VISIBLE);
            rvReactions.setVisibility(View.GONE);
        }
    }
    
    private void setupEditHistory() {
        List<EditHistory> historyList = bill.getEditHistory();
        if (historyList != null && !historyList.isEmpty()) {
            tvHistoryTitle.setVisibility(View.VISIBLE);
            findViewById(R.id.card_edit_history).setVisibility(View.VISIBLE);
            editHistoryAdapter.setHistoryList(historyList);
        } else {
            tvHistoryTitle.setVisibility(View.GONE);
            findViewById(R.id.card_edit_history).setVisibility(View.GONE);
        }
    }
    
    private void setupListeners() {
        ivClose.setOnClickListener(v -> dismiss());
        
        btnEdit.setOnClickListener(v -> {
            dismiss();
            if (listener != null) {
                listener.onEditClick(bill);
            }
        });
        
        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle(R.string.bill_detail_delete_confirm_title)
                    .setMessage(R.string.bill_detail_delete_confirm_message)
                    .setPositiveButton(R.string.delete, (dialog, which) -> {
                        dismiss();
                        if (listener != null) {
                            listener.onDeleteClick(bill);
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        });
    }
    
    private void checkPermissions() {
        String currentUserId = SharedPrefManager.getInstance(context).getUserId();
        boolean isCreator = currentUserId != null && currentUserId.equals(bill.getCreatorId());
        boolean isAdmin = currentUserId != null && currentUserId.equals(groupAdminId);
        
        // Check if bill is expired (older than 7 days)
        long daysDiff = (System.currentTimeMillis() - bill.getTimestamp()) / (24 * 60 * 60 * 1000);
        boolean isExpired = daysDiff > 7;
        
        if (isExpired) {
            cardWarning.setVisibility(View.VISIBLE);
            tvWarning.setText(R.string.bill_detail_expired_warning);
            btnEdit.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
        } else if (isCreator || isAdmin) {
            cardWarning.setVisibility(View.GONE);
            btnEdit.setVisibility(View.VISIBLE);
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            cardWarning.setVisibility(View.GONE);
            btnEdit.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
        }
    }
}
