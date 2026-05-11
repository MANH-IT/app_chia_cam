package com.chupchia.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chupchia.R;
import com.chupchia.models.Member;

import java.util.ArrayList;
import java.util.List;

public class MemberSelectionAdapter extends RecyclerView.Adapter<MemberSelectionAdapter.MemberViewHolder> {
    
    private List<Member> members;
    private List<Member> membersFull; // Copy for filtering
    private String selectionMode = "multiple"; // "multiple" or "single"
    private OnMemberSelectedListener listener;
    
    public interface OnMemberSelectedListener {
        void onMemberSelected(List<String> selectedIds);
    }
    
    public MemberSelectionAdapter(List<Member> members) {
        this.members = members;
        this.membersFull = new ArrayList<>(members);
    }
    
    public void filter(String text) {
        members.clear();
        if (text.isEmpty()) {
            members.addAll(membersFull);
        } else {
            text = text.toLowerCase();
            for (Member item : membersFull) {
                if (item.getName().toLowerCase().contains(text)) {
                    members.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }
    
    public void setSelectionMode(String mode) {
        this.selectionMode = mode;
        notifyDataSetChanged();
    }
    
    public void setOnMemberSelectedListener(OnMemberSelectedListener listener) {
        this.listener = listener;
    }
    
    public List<String> getSelectedIds() {
        List<String> selectedIds = new ArrayList<>();
        for (Member member : members) {
            if (member.isSelected()) {
                selectedIds.add(member.getId());
            }
        }
        return selectedIds;
    }
    
    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_member_selection, parent, false);
        return new MemberViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        Member member = members.get(position);
        
        holder.tvName.setText(member.getName());
        holder.tvRole.setText(member.getRole());
        
        // Xóa listener trước đó để tránh đệ quy hoặc cập nhật sai
        holder.cbMember.setOnCheckedChangeListener(null);
        holder.rbMember.setOnCheckedChangeListener(null);

        if (selectionMode.equals("multiple")) {
            holder.cbMember.setVisibility(View.VISIBLE);
            holder.rbMember.setVisibility(View.GONE);
            holder.cbMember.setChecked(member.isSelected());
            holder.cbMember.setOnCheckedChangeListener((buttonView, isChecked) -> {
                member.setSelected(isChecked);
                if (listener != null) {
                    listener.onMemberSelected(getSelectedIds());
                }
            });
        } else {
            holder.cbMember.setVisibility(View.GONE);
            holder.rbMember.setVisibility(View.VISIBLE);
            holder.rbMember.setChecked(member.isSelected());
            holder.rbMember.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    // Xóa tất cả lựa chọn
                    for (Member m : members) {
                        m.setSelected(false);
                    }
                    member.setSelected(true);
                    notifyDataSetChanged();
                    if (listener != null) {
                        listener.onMemberSelected(getSelectedIds());
                    }
                }
            });
        }
    }
    
    @Override
    public int getItemCount() {
        return members.size();
    }
    
    static class MemberViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbMember;
        RadioButton rbMember;
        TextView tvName;
        TextView tvRole;
        
        MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            cbMember = itemView.findViewById(R.id.cb_member);
            rbMember = itemView.findViewById(R.id.rb_member);
            tvName = itemView.findViewById(R.id.tv_name);
            tvRole = itemView.findViewById(R.id.tv_role);
        }
    }
}
