package com.chupchia.adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chupchia.R;
import com.chupchia.models.Member;
import com.chupchia.utils.CurrencyUtils;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MemberSplitAdapter extends RecyclerView.Adapter<MemberSplitAdapter.MemberViewHolder> {

    private List<Member> members;
    private String splitType = "equal"; // equal, percent, custom
    private long totalAmount = 0;
    private OnMemberValueChangedListener listener;
    
    public interface OnMemberValueChangedListener {
        void onMemberSelectedChanged();
        void onMemberValueChanged();
    }
    
    public MemberSplitAdapter(List<Member> members) {
        this.members = new ArrayList<>(members);
    }
    
    public void setSplitType(String type) {
        this.splitType = type;
        
        // Reset custom values when switching split type
        if (type.equals("equal")) {
            for (Member member : members) {
                member.setCustomValue(0);
            }
        }
        
        notifyDataSetChanged();
        if (listener != null) {
            listener.onMemberValueChanged();
        }
    }
    
    public void setTotalAmount(long totalAmount) {
        this.totalAmount = totalAmount;
        notifyDataSetChanged();
    }
    
    public void setOnMemberValueChangedListener(OnMemberValueChangedListener listener) {
        this.listener = listener;
    }
    
    public List<Member> getSelectedMembers() {
        List<Member> selected = new ArrayList<>();
        for (Member member : members) {
            if (member.isSelected()) {
                selected.add(member);
            }
        }
        return selected;
    }
    
    public void selectAll(boolean select) {
        for (Member member : members) {
            member.setSelected(select);
        }
        notifyDataSetChanged();
        if (listener != null) {
            listener.onMemberSelectedChanged();
        }
    }
    
    public boolean hasSelectedMembers() {
        for (Member member : members) {
            if (member.isSelected()) return true;
        }
        return false;
    }
    
    public int getTotalPercent() {
        int total = 0;
        for (Member member : members) {
            if (member.isSelected()) {
                total += member.getCustomValue();
            }
        }
        return total;
    }
    
    public long getTotalCustomAmount() {
        long total = 0;
        for (Member member : members) {
            if (member.isSelected()) {
                total += member.getCustomValue();
            }
        }
        return total;
    }
    
    /**
     * Get per person amount for equal split display
     */
    public long getPerPersonAmount() {
        List<Member> selected = getSelectedMembers();
        if (totalAmount <= 0 || selected.isEmpty()) return 0;
        return totalAmount / selected.size();
    }
    
    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_member_split, parent, false);
        return new MemberViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        Member member = members.get(position);
        
        holder.tvName.setText(member.getName());
        holder.tvRole.setText(member.getRole().equals("admin") ? "Admin" : "Thành viên");
        
        // Remove listeners before setting state
        holder.cbSelect.setOnCheckedChangeListener(null);
        holder.cbSelect.setChecked(member.isSelected());
        
        // Load avatar
        if (member.getAvatarUrl() != null && !member.getAvatarUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                .load(member.getAvatarUrl())
                .placeholder(R.drawable.ic_profile)
                .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_profile);
        }
        
        // Show amount display for equal split
        if (splitType.equals("equal")) {
            holder.flCustomInput.setVisibility(View.GONE);
            holder.tvAmountDisplay.setVisibility(View.VISIBLE);
            long perPersonAmount = getPerPersonAmount();
            holder.tvAmountDisplay.setText(CurrencyUtils.formatVND(perPersonAmount));
        } else {
            holder.tvAmountDisplay.setVisibility(View.GONE);
            holder.flCustomInput.setVisibility(member.isSelected() ? View.VISIBLE : View.GONE);
            
            // Set unit text
            if (splitType.equals("percent")) {
                holder.tvUnit.setText("%");
                holder.etCustomAmount.setHint("0");
            } else {
                holder.tvUnit.setText("đ");
                holder.etCustomAmount.setHint("0");
            }
            
            // Set value
            int value = member.getCustomValue();
            holder.etCustomAmount.setText(value > 0 ? String.valueOf(value) : "");
            
            // Remove existing TextWatcher to avoid loops
            if (holder.textWatcher != null) {
                holder.etCustomAmount.removeTextChangedListener(holder.textWatcher);
            }
            
            // Create new TextWatcher
            holder.textWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                
                @Override
                public void afterTextChanged(Editable s) {
                    String text = s.toString();
                    int value = text.isEmpty() ? 0 : Integer.parseInt(text);
                    member.setCustomValue(value);
                    if (listener != null) {
                        listener.onMemberValueChanged();
                    }
                }
            };
            
            holder.etCustomAmount.addTextChangedListener(holder.textWatcher);
        }
        
        // Checkbox listener
        holder.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            member.setSelected(isChecked);
            
            // Update custom input visibility
            if (!splitType.equals("equal")) {
                holder.flCustomInput.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                if (!isChecked) {
                    member.setCustomValue(0);
                    holder.etCustomAmount.setText("");
                }
            }
            
            if (listener != null) {
                listener.onMemberSelectedChanged();
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return members.size();
    }
    
    static class MemberViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbSelect;
        CircleImageView ivAvatar;
        TextView tvName;
        TextView tvRole;
        TextView tvAmountDisplay;
        FrameLayout flCustomInput;
        EditText etCustomAmount;
        TextView tvUnit;
        TextWatcher textWatcher;
        
        MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            cbSelect = itemView.findViewById(R.id.cb_select);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvName = itemView.findViewById(R.id.tv_name);
            tvRole = itemView.findViewById(R.id.tv_role);
            tvAmountDisplay = itemView.findViewById(R.id.tv_amount_display);
            flCustomInput = itemView.findViewById(R.id.fl_custom_input);
            etCustomAmount = itemView.findViewById(R.id.et_custom_amount);
            tvUnit = itemView.findViewById(R.id.tv_unit);
        }
    }
}
