package com.chupchia.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chupchia.R;
import com.chupchia.models.Member;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SuggestedMemberAdapter extends RecyclerView.Adapter<SuggestedMemberAdapter.ViewHolder> {

    private Context context;
    private List<Member> members;
    private OnMemberSelectionListener listener;

    public interface OnMemberSelectionListener {
        void onMemberSelected(Member member, boolean isSelected);
    }

    public SuggestedMemberAdapter(Context context, List<Member> members, OnMemberSelectionListener listener) {
        this.context = context;
        this.members = members;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_suggested_member, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Member member = members.get(position);
        
        holder.tvName.setText(member.getName());
        holder.tvPhone.setText(member.getId()); // Dùng ID làm số điện thoại tạm
        holder.cbSelect.setChecked(member.isSelected());
        
        if (member.getAvatarUrl() != null && !member.getAvatarUrl().isEmpty()) {
            Glide.with(context).load(member.getAvatarUrl()).placeholder(R.drawable.ic_default_avatar).into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_default_avatar);
        }
        
        holder.itemView.setOnClickListener(v -> {
            boolean newState = !member.isSelected();
            member.setSelected(newState);
            holder.cbSelect.setChecked(newState);
            if (listener != null) listener.onMemberSelected(member, newState);
        });
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivAvatar;
        TextView tvName, tvPhone;
        CheckBox cbSelect;
        
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvName = itemView.findViewById(R.id.tv_name);
            tvPhone = itemView.findViewById(R.id.tv_phone);
            cbSelect = itemView.findViewById(R.id.cb_select);
        }
    }
}
