package com.chupchia.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chupchia.R;
import com.chupchia.adapters.DebtPersonAdapter;
import com.chupchia.adapters.OptimizedTransactionAdapter;
import com.chupchia.adapters.PaymentHistoryAdapter;
import com.chupchia.models.Bill;
import com.chupchia.models.DebtPerson;
import com.chupchia.models.PaymentHistory;
import com.chupchia.models.Transaction;
import com.chupchia.utils.CurrencyUtils;
import com.chupchia.utils.DateTimeUtils;
import com.chupchia.utils.DebtOptimizer;
import com.chupchia.utils.SharedPrefManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DebtActivity extends AppCompatActivity {

    // ===== VIEWS =====
    private Toolbar toolbar;
    private Spinner spinnerGroup;
    private SwipeRefreshLayout swipeRefresh;
    private TextView tvTotalOwe;
    private TextView tvTotalOwed;
    private RecyclerView rvOweList;
    private RecyclerView rvOwedList;
    private MaterialButton btnReminder;
    private MaterialButton btnOptimize;
    private CardView cardPaymentHistory;
    private RecyclerView rvPaymentHistory;
    
    // ===== VARIABLES =====
    private String currentUserId;
    private String currentGroupId;
    private List<DebtPerson> oweList = new ArrayList<>();  // You owe others
    private List<DebtPerson> owedList = new ArrayList<>(); // Others owe you
    private List<PaymentHistory> paymentHistoryList = new ArrayList<>();
    private DebtPersonAdapter oweAdapter;
    private DebtPersonAdapter owedAdapter;
    private PaymentHistoryAdapter paymentHistoryAdapter;
    private Map<String, String> userIdToNameMap = new HashMap<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debt);
        
        currentUserId = SharedPrefManager.getInstance(this).getUserId();
        
        initViews();
        setupToolbar();
        setupAdapters();
        setupListeners();
        loadGroups();
        loadPaymentHistory();
        calculateDebt();
    }
    
    /**
     * Initialize views
     */
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        spinnerGroup = findViewById(R.id.spinner_group);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        tvTotalOwe = findViewById(R.id.tv_total_owe);
        tvTotalOwed = findViewById(R.id.tv_total_owed);
        rvOweList = findViewById(R.id.rv_owe_list);
        rvOwedList = findViewById(R.id.rv_owed_list);
        btnReminder = findViewById(R.id.btn_reminder);
        btnOptimize = findViewById(R.id.btn_optimize);
        cardPaymentHistory = findViewById(R.id.card_payment_history);
        rvPaymentHistory = findViewById(R.id.rv_payment_history);
    }
    
    /**
     * Setup toolbar
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.debt_title);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
    
    /**
     * Setup adapters
     */
    private void setupAdapters() {
        oweAdapter = new DebtPersonAdapter(true);
        owedAdapter = new DebtPersonAdapter(false);
        paymentHistoryAdapter = new PaymentHistoryAdapter();
        
        oweAdapter.setOnDebtClickListener((debtPerson, isOweType) -> {
            showBillDetailsDialog(debtPerson, isOweType);
        });
        
        owedAdapter.setOnDebtClickListener((debtPerson, isOweType) -> {
            showBillDetailsDialog(debtPerson, isOweType);
        });
        
        rvOweList.setLayoutManager(new LinearLayoutManager(this));
        rvOweList.setAdapter(oweAdapter);
        
        rvOwedList.setLayoutManager(new LinearLayoutManager(this));
        rvOwedList.setAdapter(owedAdapter);
        
        rvPaymentHistory.setLayoutManager(new LinearLayoutManager(this));
        rvPaymentHistory.setAdapter(paymentHistoryAdapter);
    }
    
    /**
     * Setup listeners
     */
    private void setupListeners() {
        swipeRefresh.setColorSchemeColors(ContextCompat.getColor(this, R.color.primary));
        swipeRefresh.setOnRefreshListener(() -> {
            calculateDebt();
            swipeRefresh.setRefreshing(false);
        });
        
        btnReminder.setOnClickListener(v -> showReminderDialog());
        btnOptimize.setOnClickListener(v -> showOptimizeDialog());
    }
    
    /**
     * Load groups into spinner
     */
    private void loadGroups() {
        // TODO: Load actual groups from API
        List<String> groupNames = new ArrayList<>();
        List<String> groupIds = new ArrayList<>();
        
        groupNames.add("Nhà mình");
        groupIds.add("group_1");
        groupNames.add("Bạn bè thân");
        groupIds.add("group_2");
        groupNames.add("Công ty");
        groupIds.add("group_3");
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, groupNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGroup.setAdapter(adapter);
        
        currentGroupId = groupIds.get(0);
        
        spinnerGroup.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentGroupId = groupIds.get(position);
                calculateDebt();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
    
    /**
     * Load payment history
     */
    private void loadPaymentHistory() {
        // TODO: Load actual payment history from API
        paymentHistoryList.clear();
        
        // Demo data
        PaymentHistory payment1 = new PaymentHistory("1", "user_1", "Mạnh", "user_2", "Lan", 
                75000, System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000, 
                "Trả tiền trà sữa", "momo");
        PaymentHistory payment2 = new PaymentHistory("2", "user_3", "Bình", currentUserId, "Tôi", 
                50000, System.currentTimeMillis() - 5 * 24 * 60 * 60 * 1000, 
                "Trả tiền ăn sáng", "cash");
        
        paymentHistoryList.add(payment1);
        paymentHistoryList.add(payment2);
        
        if (!paymentHistoryList.isEmpty()) {
            cardPaymentHistory.setVisibility(View.VISIBLE);
            paymentHistoryAdapter.setPaymentList(paymentHistoryList);
        } else {
            cardPaymentHistory.setVisibility(View.GONE);
        }
    }
    
    /**
     * Calculate debt from bills
     */
    private void calculateDebt() {
        // TODO: Load actual bills from API
        List<Bill> bills = getDemoBills();
        
        if (bills.isEmpty()) {
            showEmptyState(true);
            return;
        }
        
        // Calculate net balance for each member
        Map<String, Long> balances = new HashMap<>();
        userIdToNameMap.clear();
        
        // Add current user to map
        userIdToNameMap.put(currentUserId, "Tôi");
        
        for (Bill bill : bills) {
            // Add user names to map
            userIdToNameMap.put(bill.getActorId(), bill.getActorName());
            userIdToNameMap.put(bill.getCreatorId(), bill.getActorName());
            
            // Person who paid gets positive balance
            balances.put(bill.getCreatorId(), 
                balances.getOrDefault(bill.getCreatorId(), 0L) + bill.getAmount());
            
            // Split among members (simplified - each split is equal amount)
            int splitCount = bill.getSplitCount();
            if (splitCount <= 0) splitCount = 1;
            long perPersonAmount = bill.getAmount() / splitCount;
            
            // Simplified: subtract from current user if they are part of the split
            // In real app, check if currentUserId is in bill's member list
            // For demo, assume current user is always in the split if they didn't create it
            if (!currentUserId.equals(bill.getCreatorId())) {
                balances.put(currentUserId, 
                    balances.getOrDefault(currentUserId, 0L) - perPersonAmount);
            }
        }
        
        // Split into owe and owed lists
        oweList.clear();
        owedList.clear();
        long totalOwe = 0;
        long totalOwed = 0;
        
        for (Map.Entry<String, Long> entry : balances.entrySet()) {
            String userId = entry.getKey();
            long balance = entry.getValue();
            
            if (userId.equals(currentUserId)) continue;
            
            String userName = userIdToNameMap.getOrDefault(userId, "Thành viên");
            
            if (balance > 0) {
                // Others owe you
                DebtPerson debt = new DebtPerson(userId, userName, "", balance);
                debt.addReason("Các khoản đã chi");
                owedList.add(debt);
                totalOwed += balance;
            } else if (balance < 0) {
                // You owe others
                DebtPerson debt = new DebtPerson(userId, userName, "", -balance);
                debt.addReason("Các khoản đã chia");
                oweList.add(debt);
                totalOwe += -balance;
            }
        }
        
        // Update UI
        tvTotalOwe.setText(CurrencyUtils.formatVND(totalOwe));
        tvTotalOwed.setText(CurrencyUtils.formatVND(totalOwed));
        
        oweAdapter.setDebtList(oweList);
        owedAdapter.setDebtList(owedList);
        
        showEmptyState(bills.isEmpty());
    }
    
    /**
     * Show empty state when no debts
     */
    private void showEmptyState(boolean isEmpty) {
        if (isEmpty) {
            findViewById(R.id.tv_no_debt).setVisibility(View.VISIBLE);
            rvOweList.setVisibility(View.GONE);
            rvOwedList.setVisibility(View.GONE);
        } else {
            findViewById(R.id.tv_no_debt).setVisibility(View.GONE);
            rvOweList.setVisibility(View.VISIBLE);
            rvOwedList.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * Get demo bills for testing
     */
    private List<Bill> getDemoBills() {
        List<Bill> demoBills = new ArrayList<>();
        
        Bill bill1 = new Bill();
        bill1.setId("1");
        bill1.setProductName("Trà sữa trân châu");
        bill1.setAmount(150000);
        bill1.setActorName("Mạnh");
        bill1.setActorId("user_1");
        bill1.setCreatorId("user_1");
        bill1.setSplitCount(3);
        bill1.setTimestamp(System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000);
        demoBills.add(bill1);
        
        Bill bill2 = new Bill();
        bill2.setId("2");
        bill2.setProductName("Đi ăn buffet");
        bill2.setAmount(800000);
        bill2.setActorName("Lan");
        bill2.setActorId("user_2");
        bill2.setCreatorId("user_2");
        bill2.setSplitCount(4);
        bill2.setTimestamp(System.currentTimeMillis() - 5 * 24 * 60 * 60 * 1000);
        demoBills.add(bill2);
        
        Bill bill3 = new Bill();
        bill3.setId("3");
        bill3.setProductName("Ăn sáng");
        bill3.setAmount(60000);
        bill3.setActorName(currentUserId.equals("user_1") ? "Mạnh" : "Bình");
        bill3.setActorId(currentUserId);
        bill3.setCreatorId(currentUserId);
        bill3.setSplitCount(2);
        bill3.setTimestamp(System.currentTimeMillis() - 1 * 24 * 60 * 60 * 1000);
        demoBills.add(bill3);
        
        return demoBills;
    }
    
    /**
     * Show bill details dialog
     */
    private void showBillDetailsDialog(DebtPerson debtPerson, boolean isOweType) {
        StringBuilder message = new StringBuilder();
        message.append("📋 Chi tiết các bill:\n\n");
        
        if (debtPerson.getRelatedBills() != null && !debtPerson.getRelatedBills().isEmpty()) {
            for (Bill bill : debtPerson.getRelatedBills()) {
                message.append("• ").append(bill.getProductName())
                       .append(" - ").append(CurrencyUtils.formatVND(bill.getAmount()))
                       .append(" (").append(DateTimeUtils.getTimeAgo(bill.getTimestamp())).append(")\n");
            }
        } else {
            message.append("• Trà sữa - ").append(CurrencyUtils.formatVND(75000))
                   .append(" (").append(DateTimeUtils.getTimeAgo(System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000)).append(")\n");
            message.append("• Đi ăn - ").append(CurrencyUtils.formatVND(150000))
                   .append(" (").append(DateTimeUtils.getTimeAgo(System.currentTimeMillis() - 5 * 24 * 60 * 60 * 1000)).append(")\n");
        }
        
        String title = isOweType ? "Bạn nợ " + debtPerson.getName() : debtPerson.getName() + " nợ bạn";
        
        new AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message.toString())
            .setPositiveButton(R.string.close, null)
            .show();
    }
    
    /**
     * Show reminder dialog with copyable message
     */
    private void showReminderDialog() {
        StringBuilder message = new StringBuilder();
        message.append("🍊 ").append(getString(R.string.reminder_title)).append("\n\n");
        message.append(getString(R.string.reminder_intro)).append("\n");
        message.append(getString(R.string.reminder_body)).append("\n\n");
        
        if (!owedList.isEmpty()) {
            for (DebtPerson debt : owedList) {
                String reasons = debt.getReasons() != null && !debt.getReasons().isEmpty() ?
                        String.join(", ", debt.getReasons()) : "các khoản đã chi";
                message.append(String.format(getString(R.string.reminder_item_format),
                        debt.getName(), CurrencyUtils.formatVND(debt.getAmount()), reasons));
                message.append("\n");
            }
        }
        
        if (!oweList.isEmpty() && owedList.isEmpty()) {
            for (DebtPerson debt : oweList) {
                String reasons = debt.getReasons() != null && !debt.getReasons().isEmpty() ?
                        String.join(", ", debt.getReasons()) : "các khoản đã chia";
                message.append(String.format(getString(R.string.reminder_owe_item_format),
                        debt.getName(), CurrencyUtils.formatVND(debt.getAmount()), reasons));
                message.append("\n");
            }
        }
        
        message.append(getString(R.string.reminder_outro));
        
        new AlertDialog.Builder(this)
            .setTitle(R.string.reminder_title)
            .setMessage(message.toString())
            .setPositiveButton(R.string.copy, (dialog, which) -> {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("reminder", message.toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, R.string.reminder_copy_success, Toast.LENGTH_LONG).show();
            })
            .setNegativeButton(R.string.close, null)
            .show();
    }
    
    /**
     * Show optimize debt dialog
     */
    private void showOptimizeDialog() {
        // Collect all debt transactions
        List<Transaction> originalTransactions = new ArrayList<>();
        
        for (DebtPerson debt : oweList) {
            originalTransactions.add(new Transaction(currentUserId, debt.getId(), debt.getAmount()));
        }
        for (DebtPerson debt : owedList) {
            originalTransactions.add(new Transaction(debt.getId(), currentUserId, debt.getAmount()));
        }
        
        // Optimize using graph algorithm
        List<Transaction> optimized = DebtOptimizer.optimize(originalTransactions, userIdToNameMap);
        
        // Inflate dialog view
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_optimize_debt, null);
        TextView tvOriginalCount = dialogView.findViewById(R.id.tv_original_count);
        TextView tvOptimizedCount = dialogView.findViewById(R.id.tv_optimized_count);
        RecyclerView rvOptimized = dialogView.findViewById(R.id.rv_optimized_transactions);
        
        tvOriginalCount.setText(String.valueOf(originalTransactions.size()));
        tvOptimizedCount.setText(String.valueOf(optimized.size()));
        
        OptimizedTransactionAdapter adapter = new OptimizedTransactionAdapter(this, currentUserId, userIdToNameMap);
        adapter.setTransactions(optimized);
        rvOptimized.setLayoutManager(new LinearLayoutManager(this));
        rvOptimized.setAdapter(adapter);
        
        new AlertDialog.Builder(this)
            .setTitle(R.string.optimize_title)
            .setView(dialogView)
            .setPositiveButton(R.string.copy, (dialog, which) -> copyOptimizedResult(optimized))
            .setNegativeButton(R.string.close, null)
            .show();
    }
    
    /**
     * Copy optimized transactions to clipboard
     */
    private void copyOptimizedResult(List<Transaction> transactions) {
        StringBuilder message = new StringBuilder();
        message.append("📋 Danh sách thanh toán cuối tháng:\n\n");
        
        for (Transaction tx : transactions) {
            String fromName = tx.getFromUserName() != null ? tx.getFromUserName() : "Người dùng";
            String toName = tx.getToUserName() != null ? tx.getToUserName() : "Người dùng";
            
            if (tx.getFromUserId().equals(currentUserId)) {
                fromName = "Bạn";
            }
            if (tx.getToUserId().equals(currentUserId)) {
                toName = "Bạn";
            }
            
            message.append("• ").append(fromName)
                   .append(" chuyển ").append(CurrencyUtils.formatVND(tx.getAmount()))
                   .append(" cho ").append(toName).append("\n");
        }
        
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("optimized_debt", message.toString());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, R.string.optimize_copy_success, Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.debt_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            calculateDebt();
            loadPaymentHistory();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
