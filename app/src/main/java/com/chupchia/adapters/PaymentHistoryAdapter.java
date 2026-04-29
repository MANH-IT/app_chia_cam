package com.chupchia.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chupchia.R;
import com.chupchia.models.PaymentHistory;
import com.chupchia.utils.CurrencyUtils;
import com.chupchia.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.List;

public class PaymentHistoryAdapter extends RecyclerView.Adapter<PaymentHistoryAdapter.PaymentViewHolder> {
    
    private List<PaymentHistory> paymentList;
    
    public PaymentHistoryAdapter() {
        this.paymentList = new ArrayList<>();
    }
    
    public void setPaymentList(List<PaymentHistory> paymentList) {
        this.paymentList = paymentList;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_payment_history, parent, false);
        return new PaymentViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        PaymentHistory payment = paymentList.get(position);
        
        holder.tvTitle.setText(payment.getDisplayTitle());
        holder.tvDate.setText(DateTimeUtils.formatDate(payment.getTimestamp()));
        holder.tvAmount.setText(CurrencyUtils.formatVND(payment.getAmount()));
    }
    
    @Override
    public int getItemCount() {
        return paymentList.size();
    }
    
    static class PaymentViewHolder extends RecyclerView.ViewHolder {
        TextView ivIcon;
        TextView tvTitle;
        TextView tvDate;
        TextView tvAmount;
        
        PaymentViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvAmount = itemView.findViewById(R.id.tv_amount);
        }
    }
}
