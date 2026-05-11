package com.chupchia.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.chupchia.R;
import com.chupchia.models.Contact;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactInviteAdapter extends RecyclerView.Adapter<ContactInviteAdapter.ContactViewHolder> {

    private Context context;
    private List<Contact> contacts;
    private OnInviteClickListener inviteListener;

    public interface OnInviteClickListener {
        void onInviteClick(Contact contact, int position);
    }

    public ContactInviteAdapter(Context context) {
        this.context = context;
        this.contacts = new ArrayList<>();
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
        notifyDataSetChanged();
    }

    public void setOnInviteClickListener(OnInviteClickListener listener) {
        this.inviteListener = listener;
    }

    public void updateInviteStatus(int position, boolean invited) {
        if (position >= 0 && position < contacts.size()) {
            contacts.get(position).setInvited(invited);
            notifyItemChanged(position);
        }
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_contact_invite, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = contacts.get(position);

        holder.tvName.setText(contact.getName());
        holder.tvPhone.setText(contact.getDisplayPhone());

        // Tải ảnh đại diện nếu có
        if (contact.getAvatarUri() != null && !contact.getAvatarUri().isEmpty()) {
            Glide.with(context)
                    .load(contact.getAvatarUri())
                    .placeholder(R.drawable.ic_profile)
                    .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_profile);
        }

        // Cập nhật trạng thái nút
        if (contact.isMember()) {
            holder.btnInvite.setText("Thành viên");
            holder.btnInvite.setEnabled(false);
            holder.btnInvite.setAlpha(0.5f);
        } else if (contact.isInvited()) {
            holder.btnInvite.setText(context.getString(R.string.invited));
            holder.btnInvite.setEnabled(false);
            holder.btnInvite.setAlpha(0.6f);
        } else {
            holder.btnInvite.setText(context.getString(R.string.invite));
            holder.btnInvite.setEnabled(true);
            holder.btnInvite.setAlpha(1f);
        }

        holder.btnInvite.setOnClickListener(v -> {
            if (inviteListener != null && !contact.isMember() && !contact.isInvited()) {
                inviteListener.onInviteClick(contact, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivAvatar;
        TextView tvName;
        TextView tvPhone;
        MaterialButton btnInvite;

        ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvName = itemView.findViewById(R.id.tv_name);
            tvPhone = itemView.findViewById(R.id.tv_phone);
            btnInvite = itemView.findViewById(R.id.btn_invite);
        }
    }
}
