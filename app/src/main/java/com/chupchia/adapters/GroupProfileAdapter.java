package com.chupchia.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chupchia.R;
import com.chupchia.models.Group;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupProfileAdapter extends RecyclerView.Adapter<GroupProfileAdapter.GroupViewHolder> {

    private Context context;
    private List<Group> groups;
    private OnGroupClickListener clickListener;

    public interface OnGroupClickListener {
        void onGroupClick(Group group);
    }

    public GroupProfileAdapter(Context context) {
        this.context = context;
        this.groups = new ArrayList<>();
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
        notifyDataSetChanged();
    }

    public void setOnGroupClickListener(OnGroupClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_group_profile, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group group = groups.get(position);

        holder.tvGroupName.setText(group.getName());
        holder.tvMemberCount.setText(group.getMemberCount() + " thành viên");

        // Set role badge
        String currentUserId = com.chupchia.utils.SharedPrefManager.getInstance(context).getUserId();
        if (currentUserId != null && currentUserId.equals(group.getAdminId())) {
            holder.tvRole.setVisibility(View.VISIBLE);
            holder.tvRole.setText("Admin");
        } else {
            holder.tvRole.setVisibility(View.GONE);
        }

        // Load group avatar
        if (group.getAvatarUrl() != null && !group.getAvatarUrl().isEmpty()) {
            Glide.with(context)
                    .load(group.getAvatarUrl())
                    .placeholder(R.drawable.ic_default_group_avatar)
                    .into(holder.ivGroupAvatar);
        } else {
            holder.ivGroupAvatar.setImageResource(R.drawable.ic_default_group_avatar);
        }

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onGroupClick(group);
            }
        });
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivGroupAvatar;
        TextView tvGroupName;
        TextView tvMemberCount;
        TextView tvRole;
        ImageView ivNext;

        GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            ivGroupAvatar = itemView.findViewById(R.id.iv_group_avatar);
            tvGroupName = itemView.findViewById(R.id.tv_group_name);
            tvMemberCount = itemView.findViewById(R.id.tv_member_count);
            tvRole = itemView.findViewById(R.id.tv_role);
            ivNext = itemView.findViewById(R.id.iv_next);
        }
    }
}
