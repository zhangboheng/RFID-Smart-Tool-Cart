package com.example.keyadministrator.ui.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.keyadministrator.R;

public class PermissionDegreeDialog extends DialogFragment {

    private Callback callback;

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View dialogView = inflater.inflate(R.layout.dialog_permission_agree_dialog, container);
        initView(dialogView);
        return dialogView;
    }

    private void initView(View dialogView) {
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnConfirm = dialogView.findViewById(R.id.btn_confirm);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) {
                    callback.onRefuse(true);
                }
                dismissAllowingStateLoss();
            }
        });
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) {
                    callback.onRefuse(false);
                }
                dismissAllowingStateLoss();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
    }

    public interface Callback {

        void onRefuse(boolean refuse);
    }
}
