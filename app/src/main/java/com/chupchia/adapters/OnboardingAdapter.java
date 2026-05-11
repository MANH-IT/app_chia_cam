package com.chupchia.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chupchia.R;
import com.chupchia.models.OnboardingSlide;

import java.util.List;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {

    private List<OnboardingSlide> slides;
    private int lastPosition = -1;

    public OnboardingAdapter(List<OnboardingSlide> slides) {
        this.slides = slides;
    }

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_onboarding_slide, parent, false);
        return new OnboardingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        OnboardingSlide slide = slides.get(position);
        
        holder.tvIcon.setText(slide.getIcon());
        holder.tvTitle.setText(slide.getTitle());
        holder.tvDescription.setText(slide.getDescription());
        holder.tvSubDescription.setText(slide.getSubDescription());
        
        // Hiệu ứng cho mục
        if (lastPosition < position) {
            animateSlideUp(holder.itemView);
        }
        lastPosition = position;
    }
    
    private void animateSlideUp(View view) {
        view.setAlpha(0f);
        view.setTranslationY(50f);
        
        view.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(500)
            .setStartDelay(100)
            .start();
    }

    @Override
    public int getItemCount() {
        return slides.size();
    }

    static class OnboardingViewHolder extends RecyclerView.ViewHolder {
        TextView tvIcon;
        TextView tvTitle;
        TextView tvDescription;
        TextView tvSubDescription;
        
        OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIcon = itemView.findViewById(R.id.tv_icon);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvSubDescription = itemView.findViewById(R.id.tv_sub_description);
        }
    }
}
