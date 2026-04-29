package com.chupchia.dialogs;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.chupchia.R;
import com.chupchia.adapters.OptimizedTransactionAdapter;
import com.chupchia.models.DebtPerson;
import com.chupchia.models.Transaction;
import com.chupchia.utils.CurrencyUtils;
import com.chupchia.utils.DebtOptimizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OptimizeDebtDialog extends BottomSheetDialog {

    private Context context;
    private List<DebtPerson> oweList;
    private List<DebtPerson> owedList;
    private String currentUserId;
    private Map<String, String> userNameMap;
    
    // Views
    private ProgressBar progressBar;
    private LinearLayout contentContainer;
    private LinearLayout llNoDebt;
    private TextView tvExplanation;
    private RecyclerView rvOptimizedTransactions;
    private TextView tvOriginalCount;
    private TextView tvOptimizedCount;
    private TextView tvSaved;
    private MaterialButton btnCopy;
    
    // Adapters
    private OptimizedTransactionAdapter adapter;
    
    // Data
    private List<Transaction> originalTransactions;
    private List<Transaction> optimizedTransactions;
    
    public OptimizeDebtDialog(@NonNull Context context, 
                              List<DebtPerson> oweList, 
                              List<DebtPerson> owedList,
                              String currentUserId,
                              Map<String, String> userNameMap) {
        super(context);
        this.context = context;
        this.oweList = oweList != null ? oweList : new ArrayList<>();
        this.owedList = owedList != null ? owedList : new ArrayList<>();
        this.currentUserId = currentUserId;
        this.userNameMap = userNameMap != null ? userNameMap : new HashMap<>();
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_optimize_debt);
        
        initViews();
        setupBottomSheetBehavior();
        processOptimization();
    }
    
    private void initViews() {
        progressBar = findViewById(R.id.progress_bar);
        contentContainer = findViewById(R.id.content_container);
        llNoDebt = findViewById(R.id.ll_no_debt);
        tvExplanation = findViewById(R.id.tv_explanation);
        rvOptimizedTransactions = findViewById(R.id.rv_optimized_transactions);
        tvOriginalCount = findViewById(R.id.tv_original_count);
        tvOptimizedCount = findViewById(R.id.tv_optimized_count);
        tvSaved = findViewById(R.id.tv_saved);
        btnCopy = findViewById(R.id.btn_copy);
        
        adapter = new OptimizedTransactionAdapter(context, currentUserId, userNameMap);
        rvOptimizedTransactions.setLayoutManager(new LinearLayoutManager(context));
        rvOptimizedTransactions.setAdapter(adapter);
        
        btnCopy.setOnClickListener(v -> copyToClipboard());
    }
    
    private void setupBottomSheetBehavior() {
        View bottomSheet = findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            bottomSheet.setBackgroundResource(R.drawable.bg_bottom_sheet);
        }
    }
    
    private void processOptimization() {
        showLoading(true);
        
        // Simulate processing delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            prepareTransactions();
            
            if (originalTransactions.isEmpty()) {
                showNoDebt();
                return;
            }
            
            // Run optimization
            optimizedTransactions = DebtOptimizer.optimize(originalTransactions, userNameMap);
            
            showOptimizedResult();
            showLoading(false);
        }, 500);
    }
    
    private void prepareTransactions() {
        originalTransactions = new ArrayList<>();
        
        // Add debts where current user owes others
        for (DebtPerson debt : oweList) {
            originalTransactions.add(new Transaction(
                currentUserId,
                debt.getId(),
                debt.getAmount()
            ));
        }
        
        // Add debts where others owe current user
        for (DebtPerson debt : owedList) {
            originalTransactions.add(new Transaction(
                debt.getId(),
                currentUserId,
                debt.getAmount()
            ));
        }
    }
    
    private void showOptimizedResult() {
        int originalCount = originalTransactions.size();
        int optimizedCount = optimizedTransactions.size();
        int saved = originalCount - optimizedCount;
        
        // Update explanation
        tvExplanation.setText(String.format(
            context.getString(R.string.optimize_debt_explanation),
            originalCount, optimizedCount
        ));
        
        // Update stats
        tvOriginalCount.setText(String.valueOf(originalCount));
        tvOptimizedCount.setText(String.valueOf(optimizedCount));
        
        if (saved > 0) {
            tvSaved.setVisibility(View.VISIBLE);
            tvSaved.setText(String.format(context.getString(R.string.optimize_debt_saved), saved));
        } else {
            tvSaved.setVisibility(View.GONE);
        }
        
        // Show optimized transactions
        adapter.setTransactions(optimizedTransactions);
        contentContainer.setVisibility(View.VISIBLE);
    }
    
    private void showNoDebt() {
        progressBar.setVisibility(View.GONE);
        contentContainer.setVisibility(View.GONE);
        llNoDebt.setVisibility(View.VISIBLE);
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        if (!show) {
            contentContainer.setVisibility(View.VISIBLE);
        } else {
            contentContainer.setVisibility(View.GONE);
            llNoDebt.setVisibility(View.GONE);
        }
    }
    
    private void copyToClipboard() {
        String copyText = generateCopyText();
        
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("optimized_debt", copyText);
        clipboard.setPrimaryClip(clip);
        
        Toast.makeText(context, R.string.optimize_debt_copy_success, Toast.LENGTH_SHORT).show();
        dismiss();
    }
    
    private String generateCopyText() {
        StringBuilder sb = new StringBuilder();
        
        sb.append(context.getString(R.string.optimize_copy_title)).append("\n\n");
        
        // Stats
        int originalCount = originalTransactions != null ? originalTransactions.size() : 0;
        int optimizedCount = optimizedTransactions != null ? optimizedTransactions.size() : 0;
        int saved = originalCount - optimizedCount;
        
        sb.append(String.format(context.getString(R.string.optimize_copy_stats), 
                originalCount, optimizedCount, saved)).append("\n\n");
        
        // Transactions
        if (optimizedTransactions != null && !optimizedTransactions.isEmpty()) {
            sb.append(context.getString(R.string.optimize_copy_transactions_title)).append("\n");
            for (Transaction t : optimizedTransactions) {
                String fromName = t.getFromUserId().equals(currentUserId) ? "Bạn" : t.getFromUserName();
                String toName = t.getToUserId().equals(currentUserId) ? "Bạn" : t.getToUserName();
                if (fromName == null) fromName = "Thành viên";
                if (toName == null) toName = "Thành viên";
                sb.append(String.format(context.getString(R.string.optimize_copy_transaction_format),
                        fromName, CurrencyUtils.formatVND(t.getAmount()), toName)).append("\n");
            }
        }
        
        sb.append(context.getString(R.string.optimize_copy_footer));
        
        return sb.toString();
    }
}
