package com.chupchia.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chupchia.R;
import com.chupchia.models.EditHistory;
import com.chupchia.utils.DateTimeUtils;

import java.util.List;

public class EditHistoryAdapter extends RecyclerView.Adapter<EditHistoryAdapter.HistoryViewHolder> {
    
    private List<EditHistory> historyList;
    
    public EditHistoryAdapter(List<EditHistory> historyList) {
        this.historyList = historyList;
    }
    
    public EditHistoryAdapter(android.content.Context context) {
        this.historyList = new java.util.ArrayList<>();
    }
    
    public void setHistoryList(List<EditHistory> historyList) {
        this.historyList = historyList;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_edit_history, parent, false);
        return new HistoryViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        EditHistory history = historyList.get(position);
        
        // Show/hide vertical line based on position
        boolean isLast = position == historyList.size() - 1;
        holder.viewLine.setVisibility(isLast ? View.INVISIBLE : View.VISIBLE);
        
        // Set content
        holder.tvAction.setText(history.getChanges());
        holder.tvUser.setText(history.getUserName());
        holder.tvTime.setText(DateTimeUtils.getTimeAgo(history.getTimestamp()));
    }
    
    @Override
    public int getItemCount() {
        return historyList.size();
    }
    
    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        View viewLine;
        View viewDot;
        TextView tvAction;
        TextView tvUser;
        TextView tvTime;
        
        HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            viewLine = itemView.findViewById(R.id.view_line);
            viewDot = itemView.findViewById(R.id.view_dot);
            tvAction = itemView.findViewById(R.id.tv_action);
            tvUser = itemView.findViewById(R.id.tv_user);
            tvTime = itemView.findViewById(R.id.tv_time);
        }
    }
}
