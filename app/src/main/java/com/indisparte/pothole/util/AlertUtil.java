package com.indisparte.pothole.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import androidx.annotation.NonNull;

import com.indisparte.pothole.R;

/**
 * Display custom alert dialog
 *
 * @author Antonio Di Nuzzo (Indisparte)
 */
public class AlertUtil {

    /**
     * Show an alert with a message and a positive button with an action
     *
     * @param activity         The activity where display this alert
     * @param message          The message for the alert
     * @param positive_btn_msg The message to display on positive button
     * @param onClickListener  The action associate with positive button
     */
    public static void showMessagePositiveBtn(
            @NonNull Activity activity,
            @NonNull String message,
            @NonNull String positive_btn_msg,
            DialogInterface.OnClickListener onClickListener) {

        new AlertDialog.Builder(activity, R.style.CustomAlertDialog)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(positive_btn_msg, onClickListener)
                .create()
                .show();
    }
}
