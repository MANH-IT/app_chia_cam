package com.chupchia.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.chupchia.R;
import com.chupchia.models.ReactionItem;

import java.util.List;

public class ReactionGridAdapter extends BaseAdapter {

    private Context context;
    private List<ReactionItem> reactions;
    private String currentReaction;
    private OnReactionClickListener clickListener;

    public interface OnReactionClickListener {
        void onReactionClick(ReactionItem reaction);
    }

    public ReactionGridAdapter(Context context, List<ReactionItem> reactions, String currentReaction) {
        this.context = context;
        this.reactions = reactions;
        this.currentReaction = currentReaction;
    }

    public void setOnReactionClickListener(OnReactionClickListener listener) {
        this.clickListener = listener;
    }

    public void setCurrentReaction(String currentReaction) {
        this.currentReaction = currentReaction;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return reactions.size();
    }

    @Override
    public Object getItem(int position) {
        return reactions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_reaction, parent, false);
            holder = new ViewHolder();
            holder.tvEmoji = convertView.findViewById(R.id.tv_emoji);
            holder.tvName = convertView.findViewById(R.id.tv_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ReactionItem reaction = reactions.get(position);
        holder.tvEmoji.setText(reaction.getEmoji());
        holder.tvName.setText(reaction.getName());

        // Làm nổi bật nếu đây là cảm xúc hiện tại
        if (reaction.getEmoji().equals(currentReaction)) {
            convertView.setBackgroundResource(R.drawable.bg_reaction_selected);
            holder.tvEmoji.setScaleX(1.1f);
            holder.tvEmoji.setScaleY(1.1f);
        } else {
            convertView.setBackgroundResource(R.drawable.bg_reaction_item);
            holder.tvEmoji.setScaleX(1f);
            holder.tvEmoji.setScaleY(1f);
        }

        // Sự kiện nhấp with bounce animation
        View finalConvertView = convertView;
        convertView.setOnClickListener(v -> {
            // Hiệu ứng nảy
            v.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(100)
                .withEndAction(() -> {
                    v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start();
                })
                .start();

            if (clickListener != null) {
                clickListener.onReactionClick(reaction);
            }
        });

        return convertView;
    }

    static class ViewHolder {
        TextView tvEmoji;
        TextView tvName;
    }
}
