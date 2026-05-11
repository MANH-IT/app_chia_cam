package com.chupchia.adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.chupchia.R;
import com.chupchia.models.Bill;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.BillViewHolder> {

    private Context context;
    private List<Bill> bills;
    private OnBillClickListener listener;
    private OnReactionClickListener reactionListener;
    
    private static final String[] REACTION_TYPES = {"😂", "😭", "👍", "❤️"};
    
    public interface OnBillClickListener {
        void onBillClick(Bill bill);
        void onActorClick(Bill bill);
        void onImageClick(Bill bill);
    }
    
    public interface OnReactionClickListener {
        void onReactionClick(Bill bill, String reactionType, int position);
    }
    
    public FeedAdapter(Context context) {
        this.context = context;
        this.bills = new ArrayList<>();
    }
    
    public void setBills(List<Bill> newBills) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return bills.size();
            }

            @Override
            public int getNewListSize() {
                return newBills.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return bills.get(oldItemPosition).getId().equals(newBills.get(newItemPosition).getId());
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return bills.get(oldItemPosition).equals(newBills.get(newItemPosition));
            }
        });
        
        this.bills = new ArrayList<>(newBills);
        diffResult.dispatchUpdatesTo(this);
    }
    
    public void addBill(Bill bill) {
        this.bills.add(0, bill);
        notifyItemInserted(0);
    }
    
    public void updateBill(Bill updatedBill, int position) {
        if (position >= 0 && position < bills.size()) {
            bills.set(position, updatedBill);
            notifyItemChanged(position);
        }
    }
    
    public void setOnBillClickListener(OnBillClickListener listener) {
        this.listener = listener;
    }
    
    public void setOnReactionClickListener(OnReactionClickListener reactionListener) {
        this.reactionListener = reactionListener;
    }
    
    @NonNull
    @Override
    public BillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_bill, parent, false);
        return new BillViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull BillViewHolder holder, int position) {
        Bill bill = bills.get(position);
        
        // Tải ảnh bằng Glide
        Glide.with(context)
            .load(bill.getImageUrl())
            .placeholder(R.drawable.ic_placeholder)
            .error(R.drawable.ic_placeholder)
            .transform(new CenterCrop(), new RoundedCorners(16))
            .into(holder.ivProductImage);
        
        // Đặt thông tin cơ bản
        holder.tvProductName.setText(bill.getProductName());
        holder.tvAmount.setText(bill.getFormattedAmount());
        holder.tvActor.setText(bill.getActorName() + " • chia " + bill.getSplitCount() + " người");
        holder.tvTimeBadge.setText(getTimeAgo(bill.getTimestamp()));
        
        // Đặt huy hiệu kiểu chia
        setSplitTypeBadge(holder.tvSplitType, bill.getSplitType());
        
        // Đặt biểu tượng cảm xúc
        updateReactionCounts(holder, bill);
        
        // Sự kiện nhấp
        holder.cardBill.setOnClickListener(v -> {
            if (listener != null) listener.onBillClick(bill);
        });
        
        holder.tvActor.setOnClickListener(v -> {
            if (listener != null) listener.onActorClick(bill);
        });
        
        holder.ivProductImage.setOnClickListener(v -> {
            if (listener != null) listener.onImageClick(bill);
        });
        
        // Sự kiện nhấp biểu tượng cảm xúc với hiệu ứng
        holder.llReaction1.setOnClickListener(v -> {
            animateReaction(holder.llReaction1);
            if (reactionListener != null) {
                reactionListener.onReactionClick(bill, REACTION_TYPES[0], holder.getAdapterPosition());
            }
        });
        
        holder.llReaction2.setOnClickListener(v -> {
            animateReaction(holder.llReaction2);
            if (reactionListener != null) {
                reactionListener.onReactionClick(bill, REACTION_TYPES[1], holder.getAdapterPosition());
            }
        });
        
        holder.llReaction3.setOnClickListener(v -> {
            animateReaction(holder.llReaction3);
            if (reactionListener != null) {
                reactionListener.onReactionClick(bill, REACTION_TYPES[2], holder.getAdapterPosition());
            }
        });
        
        holder.llReaction4.setOnClickListener(v -> {
            animateReaction(holder.llReaction4);
            if (reactionListener != null) {
                reactionListener.onReactionClick(bill, REACTION_TYPES[3], holder.getAdapterPosition());
            }
        });
    }
    
    /**
     * Cập nhật hiển thị số lượng cảm xúc
     */
    private void updateReactionCounts(BillViewHolder holder, Bill bill) {
        holder.tvReaction1Count.setText(String.valueOf(bill.getReactionCount("😂")));
        holder.tvReaction2Count.setText(String.valueOf(bill.getReactionCount("😭")));
        holder.tvReaction3Count.setText(String.valueOf(bill.getReactionCount("👍")));
        holder.tvReaction4Count.setText(String.valueOf(bill.getReactionCount("❤️")));
    }
    
    /**
     * Đặt văn bản huy hiệu kiểu chia
     */
    private void setSplitTypeBadge(TextView badge, String splitType) {
        if (splitType == null) splitType = "shared";
        switch (splitType) {
            case "shared":
                badge.setText(R.string.camera_btn_shared);
                badge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(context.getColor(R.color.primary_light)));
                badge.setTextColor(context.getColor(R.color.primary));
                break;
            case "help":
                badge.setText(R.string.camera_btn_help);
                badge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(context.getColor(R.color.secondary_light)));
                badge.setTextColor(context.getColor(R.color.secondary));
                break;
            case "alone":
                badge.setText(R.string.camera_btn_alone);
                badge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(context.getColor(R.color.gray_light)));
                badge.setTextColor(context.getColor(R.color.gray_dark));
                break;
            default:
                badge.setText(R.string.camera_btn_shared);
                badge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(context.getColor(R.color.primary_light)));
                badge.setTextColor(context.getColor(R.color.primary));
                break;
        }
    }
    
    /**
     * Lấy chuỗi thời gian trước đây
     */
    private String getTimeAgo(long timestamp) {
        long diff = System.currentTimeMillis() - timestamp;
        
        if (diff < 60000) {
            return context.getString(R.string.just_now);
        } else if (diff < 3600000) {
            return (diff / 60000) + " " + context.getString(R.string.minutes_ago);
        } else if (diff < 86400000) {
            return (diff / 3600000) + " " + context.getString(R.string.hours_ago);
        } else if (diff < 604800000) {
            return (diff / 86400000) + " " + context.getString(R.string.days_ago);
        } else {
            return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date(timestamp));
        }
    }
    
    /**
     * Hiệu ứng nút cảm xúc khi nhấp
     */
    private void animateReaction(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.4f, 1f);
        scaleX.setDuration(200);
        
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.4f, 1f);
        scaleY.setDuration(200);
        
        scaleX.start();
        scaleY.start();
    }
    
    @Override
    public int getItemCount() {
        return bills.size();
    }
    
    static class BillViewHolder extends RecyclerView.ViewHolder {
        CardView cardBill;
        ImageView ivProductImage;
        TextView tvTimeBadge;
        TextView tvSplitType;
        TextView tvProductName;
        TextView tvAmount;
        TextView tvActor;
        LinearLayout llReaction1, llReaction2, llReaction3, llReaction4;
        TextView tvReaction1Count, tvReaction2Count, tvReaction3Count, tvReaction4Count;
        
        BillViewHolder(@NonNull View itemView) {
            super(itemView);
            cardBill = itemView.findViewById(R.id.card_bill);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            tvTimeBadge = itemView.findViewById(R.id.tv_time_badge);
            tvSplitType = itemView.findViewById(R.id.tv_split_type);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvActor = itemView.findViewById(R.id.tv_actor);
            llReaction1 = itemView.findViewById(R.id.ll_reaction_1);
            llReaction2 = itemView.findViewById(R.id.ll_reaction_2);
            llReaction3 = itemView.findViewById(R.id.ll_reaction_3);
            llReaction4 = itemView.findViewById(R.id.ll_reaction_4);
            tvReaction1Count = itemView.findViewById(R.id.tv_reaction_1_count);
            tvReaction2Count = itemView.findViewById(R.id.tv_reaction_2_count);
            tvReaction3Count = itemView.findViewById(R.id.tv_reaction_3_count);
            tvReaction4Count = itemView.findViewById(R.id.tv_reaction_4_count);
        }
    }
}
