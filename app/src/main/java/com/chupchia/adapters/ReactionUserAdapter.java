package com.chupchia.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chupchia.R;
import com.chupchia.models.ReactionGroup;

import java.util.ArrayList;
import java.util.List;

public class ReactionUserAdapter extends RecyclerView.Adapter<ReactionUserAdapter.ReactionViewHolder> {

    private Context context;
    private List<ReactionGroup> reactionGroups;

    public ReactionUserAdapter(Context context) {
        this.context = context;
        this.reactionGroups = new ArrayList<>();
    }

    public void setReactionGroups(List<ReactionGroup> reactionGroups) {
        this.reactionGroups = reactionGroups;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reaction_user, parent, false);
        return new ReactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReactionViewHolder holder, int position) {
        ReactionGroup group = reactionGroups.get(position);
        holder.tvEmoji.setText(group.getEmoji());
        holder.tvUsers.setText(group.getUserNames());
    }

    @Override
    public int getItemCount() {
        return reactionGroups.size();
    }

    static class ReactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmoji;
        TextView tvUsers;

        ReactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmoji = itemView.findViewById(R.id.tv_emoji);
            tvUsers = itemView.findViewById(R.id.tv_users);
        }
    }
}
