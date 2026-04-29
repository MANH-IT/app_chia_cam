package com.chupchia.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chupchia.R;
import com.chupchia.models.SplitData;
import com.chupchia.utils.CurrencyUtils;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SplitDetailAdapter extends RecyclerView.Adapter<SplitDetailAdapter.SplitViewHolder> {

    private Context context;
    private List<SplitData> splits;

    public SplitDetailAdapter(Context context) {
        this.context = context;
        this.splits = new ArrayList<>();
    }

    public void setSplits(List<SplitData> splits) {
        this.splits = splits;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SplitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_split_detail, parent, false);
        return new SplitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SplitViewHolder holder, int position) {
        SplitData split = splits.get(position);

        holder.tvName.setText(split.getUserName());
        holder.tvAmount.setText(CurrencyUtils.formatVND(split.getAmount()));

        if (split.getAvatarUrl() != null && !split.getAvatarUrl().isEmpty()) {
            Glide.with(context)
                    .load(split.getAvatarUrl())
                    .placeholder(R.drawable.ic_profile)
                    .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_profile);
        }
    }

    @Override
    public int getItemCount() {
        return splits.size();
    }

    static class SplitViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivAvatar;
        TextView tvName;
        TextView tvAmount;

        SplitViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvName = itemView.findViewById(R.id.tv_name);
            tvAmount = itemView.findViewById(R.id.tv_amount);
        }
    }
}
