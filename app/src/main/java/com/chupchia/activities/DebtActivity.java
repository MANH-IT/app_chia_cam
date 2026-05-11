package com.chupchia.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.chupchia.database.AppDatabase;
import com.chupchia.models.Bill;
import com.chupchia.models.DebtPerson;
import com.chupchia.models.Group;
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

    // ===== GIAO DIỆN =====
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
    
    // ===== BIẾN =====
    private String currentUserId;
    private String currentGroupId;
    private List<DebtPerson> oweList = new ArrayList<>();  // Bạn nợ người khác
    private List<DebtPerson> owedList = new ArrayList<>(); // Người khác nợ bạn
    private List<PaymentHistory> paymentHistoryList = new ArrayList<>();
    private DebtPersonAdapter oweAdapter;
    private DebtPersonAdapter owedAdapter;
    private PaymentHistoryAdapter paymentHistoryAdapter;
    private Map<String, String> userIdToNameMap = new HashMap<>();
    
    // Danh sách nhóm tải từ Room DB
    private List<Group> groupsList = new ArrayList<>();
    private List<String> groupNames = new ArrayList<>();
    private List<String> groupIds = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debt);
        
        String userId = SharedPrefManager.getInstance(this).getUserId();
        currentUserId = userId != null ? userId : "";
        
        initViews();
        setupToolbar();
        setupAdapters();
        setupListeners();
        loadGroups();
    }
    
    /**
     * Khởi tạo giao diện
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
     * Cấu hình thanh công cụ
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.debt_title);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }
    
    /**
     * Cấu hình adapter
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
     * Cấu hình sự kiện
     */
    private void setupListeners() {
        swipeRefresh.setColorSchemeColors(ContextCompat.getColor(this, R.color.primary));
        swipeRefresh.setOnRefreshListener(() -> {
            calculateDebt();
            loadPaymentHistory();
            // Lưu ý: setRefreshing(false) được gọi bên trong calculateDebt() sau khi hoàn thành bất đồng bộ
        });
        
        btnReminder.setOnClickListener(v -> showReminderDialog());
        btnOptimize.setOnClickListener(v -> showOptimizeDialog());
    }
    
    /**
     * Tải danh sách nhóm từ Room DB vào spinner
     */
    private void loadGroups() {
        new Thread(() -> {
            groupsList = AppDatabase.getInstance(this).groupDao().getAllGroups();
            
            new Handler(Looper.getMainLooper()).post(() -> {
                groupNames.clear();
                groupIds.clear();
                
                if (groupsList.isEmpty()) {
                    // Nếu không có nhóm nào, hiển thị "Tất cả" để hiện tất cả hóa đơn
                    groupNames.add("Tất cả hóa đơn");
                    groupIds.add("__all__");
                } else {
                    // Thêm tùy chọn "Tất cả" đầu tiên
                    groupNames.add("Tất cả hóa đơn");
                    groupIds.add("__all__");
                    
                    for (Group group : groupsList) {
                        groupNames.add(group.getName());
                        groupIds.add(group.getId());
                    }
                }
                
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
                        loadPaymentHistory();
                    }
                    
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
                
                // Tải lần đầu
                calculateDebt();
                loadPaymentHistory();
            });
        }).start();
    }
    
    /**
     * Tải lịch sử thanh toán từ Room DB
     */
    private void loadPaymentHistory() {
        new Thread(() -> {
            List<PaymentHistory> history = AppDatabase.getInstance(this)
                    .paymentHistoryDao().getByUser(currentUserId);
            
            new Handler(Looper.getMainLooper()).post(() -> {
                paymentHistoryList.clear();
                paymentHistoryList.addAll(history);
                
                if (!paymentHistoryList.isEmpty()) {
                    cardPaymentHistory.setVisibility(View.VISIBLE);
                    paymentHistoryAdapter.setPaymentList(paymentHistoryList);
                } else {
                    cardPaymentHistory.setVisibility(View.GONE);
                }
            });
        }).start();
    }
    
    /**
     * Tính toán công nợ từ hóa đơn lưu trong Room DB
     */
    private void calculateDebt() {
        new Thread(() -> {
            // Tải hóa đơn từ Room DB
            AppDatabase db = AppDatabase.getInstance(this);
            List<Bill> bills;
            
            if ("__all__".equals(currentGroupId)) {
                bills = db.billDao().getAllBills();
            } else {
                bills = db.billDao().getBillsByGroup(currentGroupId);
            }
            
            // Tải thêm lịch sử thanh toán để trừ các khoản đã thanh toán
            List<PaymentHistory> payments = db.paymentHistoryDao().getByUser(currentUserId);
            
            new Handler(Looper.getMainLooper()).post(() -> {
                processDebtCalculation(bills, payments);
            });
        }).start();
    }
    
    /**
     * Xử lý tính toán công nợ từ dữ liệu hóa đơn thật
     * 
     * Logic: Mỗi bill, người tạo (creator) chi trả toàn bộ → balance += amount
     * Tất cả thành viên (bao gồm cả creator) chia đều → balance -= perPerson
     * Kết quả: Creator có balance dương (được trả lại), others có balance âm (nợ)
     */
    private void processDebtCalculation(List<Bill> bills, List<PaymentHistory> payments) {
        if (bills == null || bills.isEmpty()) {
            showEmptyState(true);
            tvTotalOwe.setText(CurrencyUtils.formatVND(0));
            tvTotalOwed.setText(CurrencyUtils.formatVND(0));
            return;
        }
        
        // Tính số dư ròng cho từng thành viên
        // Số dư dương = đã trả nhiều hơn phần của mình (người khác nợ họ)
        // Số dư âm = đã trả ít hơn phần của mình (họ nợ người khác)
        Map<String, Long> balances = new HashMap<>();
        userIdToNameMap.clear();
        
        // Thêm người dùng hiện tại vào map
        String currentUserName = SharedPrefManager.getInstance(this).getUserName();
        userIdToNameMap.put(currentUserId, currentUserName != null ? currentUserName : "Tôi");
        
        for (Bill bill : bills) {
            String creatorId = bill.getCreatorId();
            
            // Thêm tên người dùng vào map
            if (bill.getActorId() != null) {
                userIdToNameMap.put(bill.getActorId(), bill.getActorName());
            }
            if (creatorId != null) {
                userIdToNameMap.put(creatorId, bill.getActorName());
            }
            
            int splitCount = bill.getSplitCount();
            if (splitCount <= 0) splitCount = 1;
            long perPersonAmount = bill.getAmount() / splitCount;
            
            // Người tạo đã trả toàn bộ → được ghi có
            balances.put(creatorId, 
                balances.getOrDefault(creatorId, 0L) + bill.getAmount());
            
            // Tất cả mọi người (kể cả người tạo) trả phần của mình → ghi nợ
            // Phần của người tạo cũng được trừ khỏi số dư của chính họ
            balances.put(creatorId,
                balances.getOrDefault(creatorId, 0L) - perPersonAmount);
            
            // Người dùng hiện tại trả phần của mình (nếu không phải người tạo)
            if (!currentUserId.equals(creatorId)) {
                balances.put(currentUserId, 
                    balances.getOrDefault(currentUserId, 0L) - perPersonAmount);
            }
        }
        
        // Trừ các khoản đã thanh toán
        if (payments != null) {
            for (PaymentHistory payment : payments) {
                if (currentUserId.equals(payment.getFromUserId())) {
                    // Người dùng hiện tại đã trả ai đó → giảm khoản nợ
                    balances.put(currentUserId,
                        balances.getOrDefault(currentUserId, 0L) + payment.getAmount());
                    balances.put(payment.getToUserId(),
                        balances.getOrDefault(payment.getToUserId(), 0L) - payment.getAmount());
                } else if (currentUserId.equals(payment.getToUserId())) {
                    // Ai đó đã trả người dùng hiện tại → giảm khoản được nợ
                    balances.put(payment.getFromUserId(),
                        balances.getOrDefault(payment.getFromUserId(), 0L) + payment.getAmount());
                    balances.put(currentUserId,
                        balances.getOrDefault(currentUserId, 0L) - payment.getAmount());
                }
            }
        }
        
        // Chia thành danh sách nợ và được nợ
        oweList.clear();
        owedList.clear();
        long totalOwe = 0;
        long totalOwed = 0;
        
        for (Map.Entry<String, Long> entry : balances.entrySet()) {
            String userId = entry.getKey();
            long balance = entry.getValue();
            
            // Bỏ qua người dùng hiện tại (chỉ hiển thị người khác)
            if (userId.equals(currentUserId)) continue;
            
            String userName = userIdToNameMap.getOrDefault(userId, "Thành viên");
            
            if (balance > 0) {
                // Người này đã trả nhiều hơn phần của họ → người dùng hiện tại nợ họ
                DebtPerson debt = new DebtPerson(userId, userName, "", balance);
                debt.addReason("Đã chi hộ bạn");
                oweList.add(debt);
                totalOwe += balance;
            } else if (balance < 0) {
                // Người này đã trả ít hơn phần của họ → họ nợ người dùng hiện tại
                DebtPerson debt = new DebtPerson(userId, userName, "", -balance);
                debt.addReason("Nợ bạn từ các bill");
                owedList.add(debt);
                totalOwed += -balance;
            }
        }
        
        // Cập nhật giao diện
        tvTotalOwe.setText(CurrencyUtils.formatVND(totalOwe));
        tvTotalOwed.setText(CurrencyUtils.formatVND(totalOwed));
        
        oweAdapter.setDebtList(oweList);
        owedAdapter.setDebtList(owedList);
        
        showEmptyState(oweList.isEmpty() && owedList.isEmpty());
        swipeRefresh.setRefreshing(false);
    }
    
    /**
     * Hiển thị trạng thái trống khi không có công nợ
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
     * Hiển thị hộp thoại chi tiết hóa đơn
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
            message.append("• Tổng nợ: ").append(CurrencyUtils.formatVND(debtPerson.getAmount())).append("\n");
        }
        
        String title = isOweType ? "Bạn nợ " + debtPerson.getName() : debtPerson.getName() + " nợ bạn";
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message.toString())
            .setNegativeButton(R.string.close, null);
        
        // Thêm nút "Đã thanh toán"
        if (isOweType) {
            builder.setPositiveButton("Đã thanh toán", (dialog, which) -> {
                markAsPaid(debtPerson);
            });
        }
        
        builder.show();
    }
    
    /**
     * Đánh dấu khoản nợ đã thanh toán — tạo bản ghi PaymentHistory
     */
    private void markAsPaid(DebtPerson debtPerson) {
        new AlertDialog.Builder(this)
            .setTitle("Xác nhận thanh toán")
            .setMessage("Bạn đã trả " + CurrencyUtils.formatVND(debtPerson.getAmount()) + " cho " + debtPerson.getName() + "?")
            .setPositiveButton("Xác nhận", (dialog, which) -> {
                new Thread(() -> {
                    PaymentHistory payment = new PaymentHistory(
                        "PAY_" + System.currentTimeMillis(),
                        currentUserId,
                        SharedPrefManager.getInstance(this).getUserName(),
                        debtPerson.getId(),
                        debtPerson.getName(),
                        debtPerson.getAmount(),
                        System.currentTimeMillis(),
                        "Thanh toán nợ",
                        "cash"
                    );
                    AppDatabase.getInstance(this).paymentHistoryDao().insert(payment);
                    
                    new Handler(Looper.getMainLooper()).post(() -> {
                        Toast.makeText(this, "Đã ghi nhận thanh toán!", Toast.LENGTH_SHORT).show();
                        calculateDebt();
                        loadPaymentHistory();
                    });
                }).start();
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }
    
    /**
     * Hiển thị hộp thoại nhắc nợ với tin nhắn có thể sao chép
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
     * Hiển thị hộp thoại tối ưu hóa công nợ
     */
    private void showOptimizeDialog() {
        // Thu thập tất cả giao dịch công nợ
        List<Transaction> originalTransactions = new ArrayList<>();
        
        for (DebtPerson debt : oweList) {
            originalTransactions.add(new Transaction(currentUserId, debt.getId(), debt.getAmount()));
        }
        for (DebtPerson debt : owedList) {
            originalTransactions.add(new Transaction(debt.getId(), currentUserId, debt.getAmount()));
        }
        
        if (originalTransactions.isEmpty()) {
            Toast.makeText(this, "Không có khoản nợ nào để tối ưu", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Tối ưu hóa sử dụng thuật toán đồ thị
        List<Transaction> optimized = DebtOptimizer.optimize(originalTransactions, userIdToNameMap);
        
        // Tạo giao diện hộp thoại
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
     * Sao chép giao dịch tối ưu vào bộ nhớ tạm
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
