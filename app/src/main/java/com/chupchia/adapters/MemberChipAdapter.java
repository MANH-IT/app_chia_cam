package com.chupchia.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.chupchia.R;
import com.chupchia.models.Member;

import java.util.ArrayList;
import java.util.List;

public class MemberChipAdapter extends RecyclerView.Adapter<MemberChipAdapter.ChipViewHolder> {

    private Context context;
    private List<Member> members;
    private String currentUserId;
    private OnMemberDeleteListener deleteListener;

    public interface OnMemberDeleteListener {
        void onDeleteClick(Member member);
    }

    public MemberChipAdapter(Context context, String currentUserId) {
        this.context = context;
        this.currentUserId = currentUserId;
        this.members = new ArrayList<>();
    }

    public void setMembers(List<Member> members) {
        this.members = members;
        notifyDataSetChanged();
    }

    public void addMember(Member member) {
        this.members.add(member);
        notifyItemInserted(members.size() - 1);
    }

    public void removeMember(Member member) {
        int position = members.indexOf(member);
        if (position >= 0) {
            members.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void setOnMemberDeleteListener(OnMemberDeleteListener listener) {
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public ChipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_member_chip, parent, false);
        return new ChipViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChipViewHolder holder, int position) {
        Member member = members.get(position);
        
        holder.chip.setText(member.getName());
        holder.chip.setChipIcon(ContextCompat.getDrawable(context, R.drawable.ic_person));
        
        // Kiểm tra nếu đây là người dùng hiện tại (Admin)
        if (member.getId() != null && member.getId().equals(currentUserId)) {
            holder.chip.setCloseIconVisible(false);
        } else {
            holder.chip.setCloseIconVisible(true);
            holder.chip.setOnCloseIconClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDeleteClick(member);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    static class ChipViewHolder extends RecyclerView.ViewHolder {
        Chip chip;

        ChipViewHolder(@NonNull View itemView) {
            super(itemView);
            chip = itemView.findViewById(R.id.chip_member);
        }
    }
}
