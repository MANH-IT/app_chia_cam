package com.chupchia.dialogs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.chupchia.R;
import com.chupchia.adapters.ReactionGridAdapter;
import com.chupchia.models.ReactionItem;

import java.util.ArrayList;
import java.util.List;

public class ReactionPickerDialog extends BottomSheetDialog {

    private Context context;
    private String currentReaction;
    private OnReactionSelectedListener listener;
    
    private TextView tvTitle;
    private GridView gvReactions;
    private LinearLayout llRemoveReaction;
    
    private List<ReactionItem> reactionList;
    private ReactionGridAdapter adapter;
    
    public interface OnReactionSelectedListener {
        void onReactionSelected(String reactionEmoji);
        void onReactionRemoved();
    }
    
    public ReactionPickerDialog(@NonNull Context context, String currentReaction, OnReactionSelectedListener listener) {
        super(context);
        this.context = context;
        this.currentReaction = currentReaction;
        this.listener = listener;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_reaction_picker);
        
        initViews();
        setupReactionData();
        setupGridView();
        setupListeners();
        setupBottomSheetBehavior();
    }
    
    private void initViews() {
        tvTitle = findViewById(R.id.tv_title);
        gvReactions = findViewById(R.id.gv_reactions);
        llRemoveReaction = findViewById(R.id.ll_remove_reaction);
    }
    
    private void setupReactionData() {
        reactionList = new ArrayList<>();
        
        // Get reactions from resources
        String[] emojis = context.getResources().getStringArray(R.array.reaction_emojis);
        String[] names = context.getResources().getStringArray(R.array.reaction_names);
        
        for (int i = 0; i < emojis.length; i++) {
            reactionList.add(new ReactionItem(emojis[i], names[i]));
        }
    }
    
    private void setupGridView() {
        adapter = new ReactionGridAdapter(context, reactionList, currentReaction);
        adapter.setOnReactionClickListener(reaction -> {
            animateAndDismiss(false, reaction.getEmoji());
        });
        
        gvReactions.setAdapter(adapter);
    }
    
    private void setupListeners() {
        llRemoveReaction.setOnClickListener(v -> {
            animateAndDismiss(true, null);
        });
    }
    
    private void setupBottomSheetBehavior() {
        View bottomSheet = findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setPeekHeight(400);
            behavior.setSkipCollapsed(false);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }
    
    private void animateAndDismiss(boolean isRemove, String selectedReaction) {
        View bottomSheet = findViewById(com.google.android.material.R.id.design_bottom_sheet);
        
        if (bottomSheet != null) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(bottomSheet, "translationY", 
                    0, bottomSheet.getHeight());
            animator.setDuration(300);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (isRemove) {
                        if (listener != null) {
                            listener.onReactionRemoved();
                        }
                    } else {
                        if (listener != null && selectedReaction != null) {
                            listener.onReactionSelected(selectedReaction);
                        }
                    }
                    dismiss();
                }
            });
            animator.start();
        } else {
            if (isRemove) {
                if (listener != null) {
                    listener.onReactionRemoved();
                }
            } else {
                if (listener != null && selectedReaction != null) {
                    listener.onReactionSelected(selectedReaction);
                }
            }
            dismiss();
        }
    }
    
    /**
     * Update the current reaction highlight without recreating dialog
     */
    public void updateCurrentReaction(String newReaction) {
        this.currentReaction = newReaction;
        if (adapter != null) {
            adapter.setCurrentReaction(newReaction);
        }
    }
}
