package com.example.onlykids_mobile_application;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class dialog_loading_helper {

    public static AlertDialog showLoadingDialog(Context context, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_loading_screen, null);
        TextView messageText = view.findViewById(R.id.loadingMessage);
        messageText.setText(message);  // Set custom message

        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.show();
        return dialog;
    }

    public static void hideLoadingDialog(AlertDialog dialog) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}

