package com.chupchia.dialogs;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.button.MaterialButton;
import com.chupchia.R;

public class ConfirmDeleteDialog extends DialogFragment {

    // Loại xóa
    public enum DeleteType {
        BILL,
        GROUP,
        MEMBER,
        LEAVE_GROUP,
        LOGOUT
    }

    private DeleteType deleteType;
    private String targetName;
    private int affectedMemberCount = 0;
    private boolean requireExtraConfirm = false;
    
    private OnDeleteConfirmListener listener;
    
    // Giao diện
    private ImageView ivWarning;
    private TextView tvTitle;
    private TextView tvMessage;
    private TextView tvWarningImpact;
    private View llExtraConfirm;
    private EditText etConfirm;
    private MaterialButton btnCancel;
    private MaterialButton btnDelete;
    
    public interface OnDeleteConfirmListener {
        void onConfirm();
    }
    
    public static ConfirmDeleteDialog newInstance(DeleteType type, String targetName) {
        ConfirmDeleteDialog dialog = new ConfirmDeleteDialog();
        Bundle args = new Bundle();
        args.putSerializable("delete_type", type);
        args.putString("target_name", targetName);
        dialog.setArguments(args);
        return dialog;
    }
    
    public static ConfirmDeleteDialog newInstance(DeleteType type, String targetName, int affectedMemberCount) {
        ConfirmDeleteDialog dialog = new ConfirmDeleteDialog();
        Bundle args = new Bundle();
        args.putSerializable("delete_type", type);
        args.putString("target_name", targetName);
        args.putInt("affected_member_count", affectedMemberCount);
        dialog.setArguments(args);
        return dialog;
    }
    
    public void setOnDeleteConfirmListener(OnDeleteConfirmListener listener) {
        this.listener = listener;
    }
    
    public void setRequireExtraConfirm(boolean require) {
        this.requireExtraConfirm = require;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            deleteType = (DeleteType) getArguments().getSerializable("delete_type");
            targetName = getArguments().getString("target_name");
            affectedMemberCount = getArguments().getInt("affected_member_count", 0);
        }
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_confirm_delete, null);
        
        initViews(view);
        setupContent();
        setupListeners();
        
        builder.setView(view);
        return builder.create();
    }
    
    @Override
    public void onStart() {
        super.onStart();
        // Hiệu ứng rung cho biểu tượng cảnh báo
        Animation shake = AnimationUtils.loadAnimation(requireContext(), R.anim.shake);
        ivWarning.startAnimation(shake);
    }
    
    private void initViews(View view) {
        ivWarning = view.findViewById(R.id.iv_warning);
        tvTitle = view.findViewById(R.id.tv_title);
        tvMessage = view.findViewById(R.id.tv_message);
        tvWarningImpact = view.findViewById(R.id.tv_warning_impact);
        llExtraConfirm = view.findViewById(R.id.ll_extra_confirm);
        etConfirm = view.findViewById(R.id.et_confirm);
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnDelete = view.findViewById(R.id.btn_delete);
    }
    
    private void setupContent() {
        String confirmButtonText;
        
        switch (deleteType) {
            case BILL:
                tvTitle.setText(R.string.confirm_delete_title_bill);
                tvMessage.setText(String.format(getString(R.string.confirm_delete_message_bill), targetName != null ? targetName : ""));
                confirmButtonText = getString(R.string.confirm_delete_confirm);
                break;
                
            case GROUP:
                tvTitle.setText(R.string.confirm_delete_title_group);
                tvMessage.setText(String.format(getString(R.string.confirm_delete_message_group), targetName != null ? targetName : ""));
                confirmButtonText = getString(R.string.confirm_delete_confirm);
                
                // Hiển thị cảnh báo thành viên bị ảnh hưởng
                if (affectedMemberCount > 0) {
                    tvWarningImpact.setVisibility(View.VISIBLE);
                    tvWarningImpact.setText(String.format(getString(R.string.confirm_delete_warning_impact), affectedMemberCount));
                }
                
                // Hiển thị xác nhận bổ sung cho xóa nhóm
                if (requireExtraConfirm) {
                    llExtraConfirm.setVisibility(View.VISIBLE);
                }
                break;
                
            case MEMBER:
                tvTitle.setText(R.string.confirm_delete_title_member);
                tvMessage.setText(String.format(getString(R.string.confirm_delete_message_member), targetName != null ? targetName : ""));
                confirmButtonText = getString(R.string.confirm_delete_confirm);
                break;
                
            case LEAVE_GROUP:
                tvTitle.setText(R.string.confirm_delete_title_leave);
                tvMessage.setText(String.format(getString(R.string.confirm_delete_message_leave), targetName != null ? targetName : ""));
                confirmButtonText = getString(R.string.confirm_delete_leave_confirm);
                break;
                
            case LOGOUT:
                tvTitle.setText(R.string.confirm_delete_title_logout);
                tvMessage.setText(R.string.confirm_delete_message_logout);
                confirmButtonText = getString(R.string.confirm_delete_logout_confirm);
                break;
                
            default:
                confirmButtonText = getString(R.string.confirm_delete_confirm);
                break;
        }
        
        btnDelete.setText(confirmButtonText);
    }
    
    private void setupListeners() {
        btnCancel.setOnClickListener(v -> dismiss());
        
        btnDelete.setOnClickListener(v -> {
            // Kiểm tra xác nhận bổ sung cho xóa nhóm
            if (deleteType == DeleteType.GROUP && requireExtraConfirm) {
                String confirmText = etConfirm.getText().toString().trim();
                if (!"XÓA".equals(confirmText)) {
                    // Hiệu ứng rung lỗi
                    etConfirm.requestFocus();
                    etConfirm.setError(getString(R.string.confirm_delete_group_extra_error));
                    
                    ObjectAnimator shakeX = ObjectAnimator.ofFloat(etConfirm, "translationX", 0f, 20f, -20f, 10f, -10f, 5f, -5f, 0f);
                    shakeX.setDuration(500);
                    shakeX.start();
                    return;
                }
            }
            
            // Hiệu ứng nhấn nút
            btnDelete.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() -> {
                    if (listener != null) {
                        listener.onConfirm();
                    }
                    dismiss();
                })
                .start();
        });
    }
    
    /**
     * Phương thức hỗ trợ hiển thị hộp thoại với FragmentActivity
     */
    public void show(FragmentActivity activity) {
        show(activity.getSupportFragmentManager(), "ConfirmDeleteDialog");
    }
}
