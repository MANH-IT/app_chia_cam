package com.chupchia.adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputLayout;
import com.chupchia.R;
import com.chupchia.models.Member;
import com.chupchia.models.SplitData;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class SplitMemberAdapter extends RecyclerView.Adapter<SplitMemberAdapter.ViewHolder> {

    private Context context;
    private List<Member> members;
    private String splitType;
    private int totalAmount;
    private String inputHint = "đ";
    private OnSplitChangeListener listener;
    private NumberFormat currencyFormat;
    
    public interface OnSplitChangeListener {
        void onValueChanged();
        void onSelectionChanged();
    }
    
    public SplitMemberAdapter(Context context, List<Member> members, String splitType, 
                              int totalAmount, OnSplitChangeListener listener) {
        this.context = context;
        this.members = members;
        this.splitType = splitType;
        this.totalAmount = totalAmount;
        this.listener = listener;
        this.currencyFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        
        if ("equal".equals(splitType)) {
            calculateEqualSplit();
        }
    }
    
    private void calculateEqualSplit() {
        int selectedCount = getSelectedCount();
        if (selectedCount > 0) {
            int perPerson = totalAmount / selectedCount;
            for (Member member : members) {
                if (member.isSelected()) {
                    member.setSplitValue(perPerson);
                } else {
                    member.setSplitValue(0);
                }
            }
        }
    }
    
    public void setSplitType(String splitType) {
        this.splitType = splitType;
        for (Member member : members) {
            if ("equal".equals(splitType)) {
                calculateEqualSplit();
            } else {
                member.setSplitValue(0);
            }
        }
        notifyDataSetChanged();
        if (listener != null) listener.onValueChanged();
    }
    
    public void setTotalAmount(int totalAmount) {
        this.totalAmount = totalAmount;
        if ("equal".equals(splitType)) {
            calculateEqualSplit();
            notifyDataSetChanged();
            if (listener != null) listener.onValueChanged();
        }
    }
    
    public void setInputHint(String hint) {
        this.inputHint = hint;
        notifyDataSetChanged();
    }
    
    public void selectAll(boolean select) {
        for (Member member : members) {
            member.setSelected(select);
        }
        if ("equal".equals(splitType)) calculateEqualSplit();
        else {
            for (Member m : members) if (!select) m.setSplitValue(0);
        }
        notifyDataSetChanged();
        if (listener != null) {
            listener.onSelectionChanged();
            listener.onValueChanged();
        }
    }
    
    public int getSelectedCount() {
        int count = 0;
        for (Member member : members) if (member.isSelected()) count++;
        return count;
    }
    
    public List<Member> getSelectedMembers() {
        List<Member> selected = new ArrayList<>();
        for (Member member : members) if (member.isSelected()) selected.add(member);
        return selected;
    }
    
    public int getTotalSplitAmount() {
        int total = 0;
        for (Member member : members) if (member.isSelected()) total += member.getSplitValue();
        return total;
    }
    
    public int getTotalPercent() {
        int total = 0;
        for (Member member : members) if (member.isSelected()) total += member.getSplitValue();
        return total;
    }
    
    public List<SplitData> getSplitData() {
        List<SplitData> splitDataList = new ArrayList<>();
        for (Member member : members) {
            if (member.isSelected()) {
                if ("percent".equals(splitType)) {
                    splitDataList.add(new SplitData(member.getId(), member.getName(), member.getAvatarUrl(), member.getSplitValue()));
                } else {
                    splitDataList.add(new SplitData(member.getId(), member.getName(), member.getAvatarUrl(), (long) member.getSplitValue()));
                }
            }
        }
        return splitDataList;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_split_member, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Member member = members.get(position);
        holder.tvName.setText(member.getName());
        holder.tvRole.setText(member.getRole());
        holder.cbSelect.setChecked(member.isSelected());
        
        if (member.getAvatarUrl() != null && !member.getAvatarUrl().isEmpty()) {
            Glide.with(context).load(member.getAvatarUrl()).placeholder(R.drawable.ic_default_avatar).into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_default_avatar);
        }
        
        if (!"equal".equals(splitType)) {
            holder.tilInput.setVisibility(View.VISIBLE);
            holder.etInput.setHint("percent".equals(splitType) ? "%" : "đ");
            holder.etInput.setEnabled(member.isSelected());
            holder.tilInput.setEnabled(member.isSelected());
            
            if (member.getSplitValue() > 0) {
                holder.etInput.setText("percent".equals(splitType) ? String.valueOf(member.getSplitValue()) : currencyFormat.format(member.getSplitValue()));
            } else {
                holder.etInput.setText("");
            }
            
            holder.etInput.removeTextChangedListener(holder.textWatcher);
            holder.textWatcher = new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) {
                    String raw = s.toString().replaceAll("[^0-9]", "");
                    if (!raw.isEmpty()) {
                        int value = Integer.parseInt(raw);
                        if ("percent".equals(splitType) && value > 100) value = 100;
                        member.setSplitValue(value);
                        if (!"percent".equals(splitType)) {
                            holder.etInput.removeTextChangedListener(this);
                            holder.etInput.setText(currencyFormat.format(value));
                            holder.etInput.setSelection(holder.etInput.getText().length());
                            holder.etInput.addTextChangedListener(this);
                        }
                    } else {
                        member.setSplitValue(0);
                    }
                    if (listener != null) listener.onValueChanged();
                }
            };
            holder.etInput.addTextChangedListener(holder.textWatcher);
        } else {
            holder.tilInput.setVisibility(View.GONE);
        }
        
        holder.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            member.setSelected(isChecked);
            if ("equal".equals(splitType)) {
                calculateEqualSplit();
                notifyDataSetChanged();
            } else {
                holder.etInput.setEnabled(isChecked);
                holder.tilInput.setEnabled(isChecked);
                if (!isChecked) {
                    member.setSplitValue(0);
                    holder.etInput.setText("");
                }
            }
            if (listener != null) {
                listener.onSelectionChanged();
                listener.onValueChanged();
            }
        });
    }
    
    @Override public int getItemCount() { return members.size(); }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbSelect;
        CircleImageView ivAvatar;
        TextView tvName, tvRole;
        TextInputLayout tilInput;
        EditText etInput;
        TextWatcher textWatcher;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cbSelect = itemView.findViewById(R.id.cb_select);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvName = itemView.findViewById(R.id.tv_name);
            tvRole = itemView.findViewById(R.id.tv_role);
            tilInput = itemView.findViewById(R.id.til_input);
            etInput = itemView.findViewById(R.id.et_input);
        }
    }
}
