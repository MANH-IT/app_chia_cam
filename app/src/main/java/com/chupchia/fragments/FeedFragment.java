package com.chupchia.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chupchia.R;
import com.chupchia.activities.CameraActivity;
import com.chupchia.activities.MainActivity;
import com.chupchia.adapters.FeedAdapter;
import com.chupchia.models.Bill;

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
        
        // Register receiver
        requireContext().registerReceiver(newBillReceiver, new IntentFilter("com.chupchia.ACTION_NEW_BILL"));
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            requireContext().unregisterReceiver(newBillReceiver);
        } catch (Exception ignored) {}
    }
    
    /**
     * Khởi tạo views
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
     * Setup RecyclerView
     */
    private void setupRecyclerView() {
        adapter = new FeedAdapter(requireContext());
        adapter.setOnBillClickListener(new FeedAdapter.OnBillClickListener() {
            @Override
            public void onBillClick(Bill bill) {
                // TODO: Show bill detail dialog
                Toast.makeText(requireContext(), "Click bill: " + bill.getProductName(), Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onActorClick(Bill bill) {
                // TODO: Show user profile
                Toast.makeText(requireContext(), "Click actor: " + bill.getActorName(), Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onImageClick(Bill bill) {
                // TODO: Show fullscreen image
                Toast.makeText(requireContext(), "Click image", Toast.LENGTH_SHORT).show();
            }
        });
        
        adapter.setOnReactionClickListener((bill, reactionType, position) -> {
            // TODO: Call API to update reaction
            updateReaction(bill, reactionType, position);
        });
        
        rvFeed.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvFeed.setAdapter(adapter);
        rvFeed.setHasFixedSize(true);
    }
    
    /**
     * Setup SwipeRefreshLayout
     */
    private void setupSwipeRefresh() {
        swipeRefresh.setColorSchemeColors(getResources().getColor(R.color.primary));
        swipeRefresh.setOnRefreshListener(() -> {
            refreshBills();
        });
    }
    
    /**
     * Load bills from API/Firebase
     */
    private void loadBills() {
        showLoading(true);
        
        // Đọc dữ liệu từ Room Database thay vì data giả
        new Thread(() -> {
            List<Bill> bills = com.chupchia.database.AppDatabase.getInstance(getContext())
                    .billDao().getAllBills();
            
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                showLoading(false);
                if (bills == null || bills.isEmpty()) {
                    llEmptyState.setVisibility(View.VISIBLE);
                    rvFeed.setVisibility(View.GONE);
                } else {
                    llEmptyState.setVisibility(View.GONE);
                    rvFeed.setVisibility(View.VISIBLE);
                    adapter.setBills(bills);
                }
                swipeRefresh.setRefreshing(false);
            }, 800);
        }).start();
    }
    
    /**
     * Refresh bills
     */
    private void refreshBills() {
        // Clear local list and reload from DB (or API in the future)
        loadBills();
    }
    
    /**
     * Update reaction on server
     */
    private void updateReaction(Bill bill, String reactionType, int position) {
        // TODO: Call API to update reaction
        // For demo, update locally
        int currentCount = bill.getReactionCount(reactionType);
        bill.getReactions().put(reactionType, currentCount + 1);
        adapter.updateBill(bill, position);
    }
    
    /**
     * Create demo bills for testing (Deprecated - new accounts should be empty)
     */
    private List<Bill> createDemoBills() {
        return new ArrayList<>();
    }
    
    /**
     * Show/hide loading indicator
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
     * Update empty state visibility
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
     * Add new bill to feed
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
