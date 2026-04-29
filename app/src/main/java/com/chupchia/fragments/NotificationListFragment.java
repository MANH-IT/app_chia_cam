package com.chupchia.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chupchia.R;
import com.chupchia.adapters.NotificationAdapter;
import com.chupchia.models.Notification;
import com.chupchia.utils.NotificationSwipeHelper;

import java.util.ArrayList;
import java.util.List;

public class NotificationListFragment extends Fragment {

    private static final String ARG_TAB_POSITION = "tab_position";
    
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvNotifications;
    private LinearLayout llEmpty;
    private TextView tvEmptyMessage;
    
    private NotificationAdapter adapter;
    private List<Notification> notificationList = new ArrayList<>();
    private int tabPosition;
    
    public static NotificationListFragment newInstance(int tabPosition) {
        Bundle args = new Bundle();
        args.putInt(ARG_TAB_POSITION, tabPosition);
        NotificationListFragment fragment = new NotificationListFragment();
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tabPosition = getArguments().getInt(ARG_TAB_POSITION);
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notification_list, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        loadNotifications();
    }
    
    private void initViews(View view) {
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        rvNotifications = view.findViewById(R.id.rv_notifications);
        llEmpty = view.findViewById(R.id.ll_empty);
        tvEmptyMessage = view.findViewById(R.id.tv_empty_message);
        
        // Set empty message based on tab
        switch (tabPosition) {
            case 0:
                tvEmptyMessage.setText(R.string.notification_empty_all);
                break;
            case 1:
                tvEmptyMessage.setText(R.string.notification_empty_unread);
                break;
            case 2:
                tvEmptyMessage.setText(R.string.notification_empty_read);
                break;
        }
    }
    
    private void setupRecyclerView() {
        adapter = new NotificationAdapter(requireContext());
        adapter.setOnNotificationClickListener((notification, position) -> {
            onNotificationClick(notification, position);
        });
        
        rvNotifications.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvNotifications.setAdapter(adapter);
        
        // Setup swipe to delete
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
            new NotificationSwipeHelper(requireContext(), adapter, (notification, position) -> {
                deleteNotification(notification, position);
            }));
        itemTouchHelper.attachToRecyclerView(rvNotifications);
    }
    
    private void setupSwipeRefresh() {
        swipeRefresh.setColorSchemeResources(R.color.primary);
        swipeRefresh.setOnRefreshListener(() -> {
            loadNotifications();
            swipeRefresh.setRefreshing(false);
        });
    }
    
    private void loadNotifications() {
        new Thread(() -> {
            com.chupchia.database.NotificationDao dao = com.chupchia.database.AppDatabase.getInstance(getContext()).notificationDao();
            List<Notification> list;
            
            switch (tabPosition) {
                case 1: // Unread
                    list = dao.getUnread();
                    break;
                case 2: // Read
                    list = dao.getRead();
                    break;
                default: // All
                    list = dao.getAll();
                    break;
            }
            
            new Handler(Looper.getMainLooper()).post(() -> {
                if (isAdded()) {
                    adapter.setNotifications(list);
                    updateEmptyState();
                    swipeRefresh.setRefreshing(false);
                }
            });
        }).start();
    }
    
    private void updateEmptyState() {
        if (adapter.getItemCount() == 0) {
            llEmpty.setVisibility(View.VISIBLE);
            rvNotifications.setVisibility(View.GONE);
        } else {
            llEmpty.setVisibility(View.GONE);
            rvNotifications.setVisibility(View.VISIBLE);
        }
    }
    
    private void onNotificationClick(Notification notification, int position) {
        // Mark as read if not already
        if (!notification.isRead()) {
            notification.setRead(true);
            adapter.notifyItemChanged(position);
        }
        
        // Navigate based on notification type
        Toast.makeText(requireContext(), "Mở: " + notification.getTitle(), Toast.LENGTH_SHORT).show();
    }
    
    private void deleteNotification(Notification notification, int position) {
        adapter.removeNotification(position);
        Toast.makeText(requireContext(), R.string.notification_deleted, Toast.LENGTH_SHORT).show();
        updateEmptyState();
    }
}
