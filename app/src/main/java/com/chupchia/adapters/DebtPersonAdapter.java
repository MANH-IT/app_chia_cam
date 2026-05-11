package com.chupchia.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chupchia.R;
import com.chupchia.models.DebtPerson;
import com.chupchia.utils.CurrencyUtils;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class DebtPersonAdapter extends RecyclerView.Adapter<DebtPersonAdapter.DebtViewHolder> {
    
    private List<DebtPerson> debtList;
    private OnDebtClickListener listener;
    private boolean isOweType; // true = you owe, false = owed to you
    
    public interface OnDebtClickListener {
        void onDebtClick(DebtPerson debtPerson, boolean isOweType);
    }
    
    public DebtPersonAdapter(boolean isOweType) {
        this.debtList = new ArrayList<>();
        this.isOweType = isOweType;
    }
    
    public void setDebtList(List<DebtPerson> debtList) {
        this.debtList = debtList;
        notifyDataSetChanged();
    }
    
    public void setOnDebtClickListener(OnDebtClickListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public DebtViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_debt_person, parent, false);
        return new DebtViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull DebtViewHolder holder, int position) {
        DebtPerson debt = debtList.get(position);
        
        holder.tvName.setText(debt.getName());
        
        // Định dạng lý do
        if (debt.getReasons() != null && !debt.getReasons().isEmpty()) {
            StringBuilder reasons = new StringBuilder();
            for (int i = 0; i < Math.min(debt.getReasons().size(), 3); i++) {
                if (i > 0) reasons.append(", ");
                reasons.append(debt.getReasons().get(i));
            }
            if (debt.getReasons().size() > 3) {
                reasons.append("...");
            }
            holder.tvReason.setText(reasons.toString());
        } else {
            holder.tvReason.setText(isOweType ? "Bạn nợ" : "Nợ bạn");
        }
        
        // Đặt số tiền và màu
        holder.tvAmount.setText(CurrencyUtils.formatVND(debt.getAmount()));
        if (isOweType) {
            holder.tvAmount.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.error));
        } else {
            holder.tvAmount.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.success));
        }
        
        // Tải ảnh đại diện
        if (debt.getAvatarUrl() != null && !debt.getAvatarUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                .load(debt.getAvatarUrl())
                .placeholder(R.drawable.ic_profile)
                .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_profile);
        }
        
        // Sự kiện nhấp
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDebtClick(debt, isOweType);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return debtList.size();
    }
    
    static class DebtViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivAvatar;
        TextView tvName;
        TextView tvReason;
        TextView tvAmount;
        
        DebtViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvName = itemView.findViewById(R.id.tv_name);
            tvReason = itemView.findViewById(R.id.tv_reason);
            tvAmount = itemView.findViewById(R.id.tv_amount);
        }
    }
}
