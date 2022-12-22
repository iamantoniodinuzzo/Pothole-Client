package com.indisparte.pothole.data.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.view.Gravity;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * @author Antonio Di Nuzzo (Indisparte)
 */
public class NetworkChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Common.isConnectedToInternet(context)) {//Internet is not connected
            // Do something
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
            //Show dialog
            AlertDialog dialog = builder.create();
            dialog.show();
            dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Retry", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialog.dismiss();
                    onReceive(context, intent);
                }
            });
            dialog.setCancelable(false);
            dialog.getWindow().setGravity(Gravity.CENTER);
        }
    }
}
