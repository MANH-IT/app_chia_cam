package com.chupchia.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.chupchia.R;
import com.chupchia.models.Transaction;
import com.chupchia.utils.CurrencyUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OptimizedTransactionAdapter extends RecyclerView.Adapter<OptimizedTransactionAdapter.TransactionViewHolder> {

    private Context context;
    private List<Transaction> transactions;
    private String currentUserId;
    private Map<String, String> userNameMap;

    public OptimizedTransactionAdapter(Context context, String currentUserId, Map<String, String> userNameMap) {
        this.context = context;
        this.currentUserId = currentUserId;
        this.userNameMap = userNameMap;
        this.transactions = new ArrayList<>();
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_optimized_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);

        // Set sender (from)
        if (transaction.getFromUserId() != null && transaction.getFromUserId().equals(currentUserId)) {
            holder.tvFrom.setText("Bạn");
            holder.tvFrom.setTextColor(ContextCompat.getColor(context, R.color.error));
        } else {
            String userName = transaction.getFromUserName();
            if (userName == null && userNameMap != null) {
                userName = userNameMap.get(transaction.getFromUserId());
            }
            holder.tvFrom.setText(userName != null ? userName : "Thành viên");
            holder.tvFrom.setTextColor(ContextCompat.getColor(context, R.color.gray_dark));
        }

        // Set receiver (to)
        if (transaction.getToUserId() != null && transaction.getToUserId().equals(currentUserId)) {
            holder.tvTo.setText("Bạn");
            holder.tvTo.setTextColor(ContextCompat.getColor(context, R.color.success));
        } else {
            String userName = transaction.getToUserName();
            if (userName == null && userNameMap != null) {
                userName = userNameMap.get(transaction.getToUserId());
            }
            holder.tvTo.setText(userName != null ? userName : "Thành viên");
            holder.tvTo.setTextColor(ContextCompat.getColor(context, R.color.success));
        }

        // Set amount
        holder.tvAmount.setText(CurrencyUtils.formatVND(transaction.getAmount()));
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvFrom;
        TextView tvTo;
        TextView tvAmount;
        ImageView ivArrow;

        TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFrom = itemView.findViewById(R.id.tv_from);
            tvTo = itemView.findViewById(R.id.tv_to);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            ivArrow = itemView.findViewById(R.id.iv_arrow);
        }
    }
}
