package com.chupchia.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chupchia.R;
import com.chupchia.models.Member;
import com.chupchia.utils.CurrencyUtils;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberAdapter.MemberViewHolder> {

    private Context context;
    private List<Member> members;
    private String currentUserId;
    private boolean isAdmin;
    private OnMemberActionListener actionListener;

    public interface OnMemberActionListener {
        void onTransferAdmin(Member member);
        void onRemoveMember(Member member);
        void onLeaveGroup();
    }

    public GroupMemberAdapter(Context context, String currentUserId, boolean isAdmin) {
        this.context = context;
        this.currentUserId = currentUserId;
        this.isAdmin = isAdmin;
        this.members = new ArrayList<>();
    }

    public void setMembers(List<Member> members) {
        this.members = members;
        notifyDataSetChanged();
    }

    public void setOnMemberActionListener(OnMemberActionListener listener) {
        this.actionListener = listener;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_group_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        Member member = members.get(position);
        boolean isCurrentUser = member.getId() != null && member.getId().equals(currentUserId);

        holder.tvName.setText(member.getName());

        // Show role badge
        if (member.isAdmin()) {
            holder.tvRole.setVisibility(View.VISIBLE);
            holder.tvRole.setText("Admin");
            holder.tvRole.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_role_admin));
            holder.tvRole.setTextColor(ContextCompat.getColor(context, R.color.primary));
        } else {
            holder.tvRole.setVisibility(View.GONE);
        }

        // Set balance display
        int balance = member.getBalance();
        if (balance > 0) {
            holder.tvBalance.setText(String.format(context.getString(R.string.group_detail_balance_owed),
                    CurrencyUtils.formatVND(balance)));
            holder.tvBalance.setTextColor(ContextCompat.getColor(context, R.color.success));
        } else if (balance < 0) {
            holder.tvBalance.setText(String.format(context.getString(R.string.group_detail_balance_owe),
                    CurrencyUtils.formatVND(-balance)));
            holder.tvBalance.setTextColor(ContextCompat.getColor(context, R.color.error));
        } else {
            holder.tvBalance.setText(context.getString(R.string.group_detail_balance_zero));
            holder.tvBalance.setTextColor(ContextCompat.getColor(context, R.color.gray_medium));
        }

        // Load avatar
        if (member.getAvatarUrl() != null && !member.getAvatarUrl().isEmpty()) {
            Glide.with(context)
                    .load(member.getAvatarUrl())
                    .placeholder(R.drawable.ic_profile)
                    .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_profile);
        }

        // Setup menu button
        if (isAdmin && !isCurrentUser) {
            holder.ivMenu.setVisibility(View.VISIBLE);
            holder.ivMenu.setOnClickListener(v -> showAdminMemberMenu(member, v));
        } else if (isCurrentUser) {
            holder.ivMenu.setVisibility(View.VISIBLE);
            holder.ivMenu.setOnClickListener(v -> showSelfMemberMenu(v));
        } else {
            holder.ivMenu.setVisibility(View.GONE);
        }
    }

    /**
     * Show admin menu for other members
     */
    private void showAdminMemberMenu(Member member, View anchor) {
        PopupMenu popupMenu = new PopupMenu(context, anchor);
        popupMenu.getMenu().add(0, 1, 0, context.getString(R.string.menu_transfer_admin));
        popupMenu.getMenu().add(0, 2, 1, context.getString(R.string.menu_remove_member));

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                if (actionListener != null) {
                    actionListener.onTransferAdmin(member);
                }
            } else if (item.getItemId() == 2) {
                if (actionListener != null) {
                    actionListener.onRemoveMember(member);
                }
            }
            return true;
        });
        popupMenu.show();
    }

    /**
     * Show self menu for current user
     */
    private void showSelfMemberMenu(View anchor) {
        PopupMenu popupMenu = new PopupMenu(context, anchor);
        popupMenu.getMenu().add(0, 1, 0, context.getString(R.string.menu_leave_group));

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                if (actionListener != null) {
                    actionListener.onLeaveGroup();
                }
            }
            return true;
        });
        popupMenu.show();
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    static class MemberViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivAvatar;
        TextView tvName;
        TextView tvRole;
        TextView tvBalance;
        ImageView ivMenu;

        MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvName = itemView.findViewById(R.id.tv_name);
            tvRole = itemView.findViewById(R.id.tv_role);
            tvBalance = itemView.findViewById(R.id.tv_balance);
            ivMenu = itemView.findViewById(R.id.iv_menu);
        }
    }
}
