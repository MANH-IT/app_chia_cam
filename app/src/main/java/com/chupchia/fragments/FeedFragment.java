package com.chupchia.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chupchia.R;
import com.chupchia.activities.CameraActivity;
import com.chupchia.activities.EditBillActivity;
import com.chupchia.activities.MainActivity;
import com.chupchia.adapters.FeedAdapter;
import com.chupchia.database.AppDatabase;
import com.chupchia.dialogs.BillDetailDialog;
import com.chupchia.models.Bill;
import com.chupchia.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

public class FeedFragment extends Fragment {
    
    // ===== VIEWS =====
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvFeed;
    private LinearLayout llEmptyState;
    private ProgressBar progressBar;
    
    // ===== VARIABLES =====
    private FeedAdapter adapter;
    private List<Bill> bills = new ArrayList<>();
    private boolean isLoading = true;
    
    private final BroadcastReceiver newBillReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.chupchia.ACTION_NEW_BILL".equals(intent.getAction())) {
                refreshBills();
            }
        }
    };
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feed, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        loadBills();
        
        // Đăng ký receiver với cờ Android 14+
        IntentFilter filter = new IntentFilter("com.chupchia.ACTION_NEW_BILL");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(newBillReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            requireContext().registerReceiver(newBillReceiver, filter);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Làm mới khi quay lại từ EditBillActivity hoặc CameraActivity
        refreshBills();
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            requireContext().unregisterReceiver(newBillReceiver);
        } catch (Exception ignored) {}
    }
    
    /**
     * Khởi tạo giao diện
     */
    private void initViews(View view) {
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        rvFeed = view.findViewById(R.id.rv_feed);
        llEmptyState = view.findViewById(R.id.ll_empty_state);
        progressBar = view.findViewById(R.id.progress_bar);
        
        view.findViewById(R.id.btn_create_first_bill).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                Intent intent = new Intent(getActivity(), CameraActivity.class);
                startActivity(intent);
            }
        });
    }
    
    /**
     * Cấu hình RecyclerView
     */
    private void setupRecyclerView() {
        adapter = new FeedAdapter(requireContext());
        adapter.setOnBillClickListener(new FeedAdapter.OnBillClickListener() {
            @Override
            public void onBillClick(Bill bill) {
                showBillDetail(bill);
            }
            
            @Override
            public void onActorClick(Bill bill) {
                Toast.makeText(requireContext(), bill.getActorName(), Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onImageClick(Bill bill) {
                showBillDetail(bill);
            }
        });
        
        adapter.setOnReactionClickListener((bill, reactionType, position) -> {
            updateReaction(bill, reactionType, position);
        });
        
        rvFeed.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvFeed.setAdapter(adapter);
        rvFeed.setHasFixedSize(true);
    }
    
    /**
     * Hiển thị BillDetailDialog
     */
    private void showBillDetail(Bill bill) {
        String groupAdminId = SharedPrefManager.getInstance(requireContext()).getUserId();
        
        BillDetailDialog dialog = new BillDetailDialog(
            requireContext(), bill, groupAdminId,
            new BillDetailDialog.OnBillActionListener() {
                @Override
                public void onEditClick(Bill bill) {
                    // Điều hướng đến EditBillActivity
                    Intent intent = new Intent(requireContext(), EditBillActivity.class);
                    intent.putExtra("bill", bill);
                    startActivity(intent);
                }
                
                @Override
                public void onDeleteClick(Bill bill) {
                    deleteBill(bill);
                }
            }
        );
        dialog.show();
    }
    
    /**
     * Xóa hóa đơn từ Room DB
     */
    private void deleteBill(Bill bill) {
        Context ctx = getContext();
        if (ctx == null) return;
        final Context appCtx = ctx.getApplicationContext();
        
        new Thread(() -> {
            AppDatabase.getInstance(appCtx).billDao().deleteBill(bill);
            
            new Handler(Looper.getMainLooper()).post(() -> {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Đã xóa hóa đơn", Toast.LENGTH_SHORT).show();
                    refreshBills();
                }
            });
        }).start();
    }
    
    /**
     * Cấu hình SwipeRefreshLayout
     */
    private void setupSwipeRefresh() {
        swipeRefresh.setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.primary));
        swipeRefresh.setOnRefreshListener(() -> {
            refreshBills();
        });
    }
    
    /**
     * Tải hóa đơn từ cơ sở dữ liệu Room
     */
    private void loadBills() {
        showLoading(true);
        
        Context context = getContext();
        if (context == null) return;
        
        final Context appContext = context.getApplicationContext();
        
        new Thread(() -> {
            List<Bill> loadedBills = AppDatabase.getInstance(appContext)
                    .billDao().getAllBills();
            
            new Handler(Looper.getMainLooper()).post(() -> {
                if (isAdded()) {
                    this.bills = loadedBills != null ? loadedBills : new ArrayList<>();
                    showLoading(false);
                    if (this.bills.isEmpty()) {
                        llEmptyState.setVisibility(View.VISIBLE);
                        rvFeed.setVisibility(View.GONE);
                    } else {
                        llEmptyState.setVisibility(View.GONE);
                        rvFeed.setVisibility(View.VISIBLE);
                        adapter.setBills(this.bills);
                    }
                    swipeRefresh.setRefreshing(false);
                }
            });
        }).start();
    }
    
    /**
     * Làm mới hóa đơn
     */
    private void refreshBills() {
        loadBills();
    }
    
    /**
     * Cập nhật cảm xúc và lưu vào Room DB
     */
    private void updateReaction(Bill bill, String reactionType, int position) {
        // Cập nhật cục bộ
        int currentCount = bill.getReactionCount(reactionType);
        bill.getReactions().put(reactionType, currentCount + 1);
        adapter.updateBill(bill, position);
        
        // Lưu context trước luồng nền để tránh crash nếu Fragment tách ra
        Context ctx = getContext();
        if (ctx == null) return;
        final Context appCtx = ctx.getApplicationContext();
        
        // Lưu vào Room DB
        new Thread(() -> {
            AppDatabase.getInstance(appCtx).billDao().updateBill(bill);
        }).start();
    }
    
    /**
     * Hiện/ẩn chỉ báo loading
     */
    private void showLoading(boolean show) {
        isLoading = show;
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        if (!show && bills.isEmpty()) {
            llEmptyState.setVisibility(View.VISIBLE);
            rvFeed.setVisibility(View.GONE);
        } else {
            llEmptyState.setVisibility(View.GONE);
            rvFeed.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * Cập nhật hiển thị trạng thái trống
     */
    private void updateEmptyState() {
        if (!isLoading && bills.isEmpty()) {
            llEmptyState.setVisibility(View.VISIBLE);
            rvFeed.setVisibility(View.GONE);
        } else {
            llEmptyState.setVisibility(View.GONE);
            rvFeed.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * Thêm hóa đơn mới vào bảng tin
     */
    public void addNewBill(Bill bill) {
        adapter.addBill(bill);
        rvFeed.smoothScrollToPosition(0);
        updateEmptyState();
    }
    
    public void refresh() {
        refreshBills();
    }
}
