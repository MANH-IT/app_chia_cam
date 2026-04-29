package com.chupchia.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.chupchia.R;

public class SplitOptionsDialog extends DialogFragment {
    
    private SplitTypeListener listener;
    
    public interface SplitTypeListener {
        void onSplitTypeSelected(String type);
    }
    
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof SplitTypeListener) {
            listener = (SplitTypeListener) context;
        }
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        View view = requireActivity().getLayoutInflater().inflate(R.layout.dialog_split_options, null);
        
        TextView tvEqual = view.findViewById(R.id.tv_split_equal);
        TextView tvPercent = view.findViewById(R.id.tv_split_percent);
        TextView tvCustom = view.findViewById(R.id.tv_split_custom);
        
        tvEqual.setOnClickListener(v -> {
            if (listener != null) listener.onSplitTypeSelected("equal");
            dismiss();
        });
        
        tvPercent.setOnClickListener(v -> {
            if (listener != null) listener.onSplitTypeSelected("percent");
            dismiss();
        });
        
        tvCustom.setOnClickListener(v -> {
            if (listener != null) listener.onSplitTypeSelected("custom");
            dismiss();
        });
        
        builder.setView(view);
        return builder.create();
    }
}
