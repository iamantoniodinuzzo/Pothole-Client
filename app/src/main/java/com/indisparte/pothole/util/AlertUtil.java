package com.indisparte.pothole.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

import androidx.annotation.NonNull;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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


    /**
     * Mostra una semplice dialog di errore con del testo con un positive button legato ad una azione
     *
     * @param message                     il messaggio da mostrare nella dialog
     * @param positive_text_btn           il testo da far comparire sul positive button
     * @param negative_text_btn           il testo da far comparire sul negative button
     * @param positiveButtonClickListener l'azione da svolgere al click del positive button
     * @param cancelButtonOnClickListener l'azione da svolgere al click del negative button
     * @return
     */
    public static Dialog createSimpleOkCancelDialog(@NonNull Context context,
                                                    @NonNull String message,
                                                    @NonNull String positive_text_btn,
                                                    String negative_text_btn,
                                                    @NonNull DialogInterface.OnClickListener positiveButtonClickListener,
                                                    DialogInterface.OnClickListener cancelButtonOnClickListener) {
        final MaterialAlertDialogBuilder alertDialog = new MaterialAlertDialogBuilder(context)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(positive_text_btn, positiveButtonClickListener);
        if (negative_text_btn != null && cancelButtonOnClickListener != null)
            alertDialog.setNegativeButton(negative_text_btn, cancelButtonOnClickListener);

        return alertDialog.create();
    }


    /**
     * Crea una semplice custom load dialog con un indicatore di progresso e un button per terminare l'azione
     *
     * @param message                     il messaggio da affiancare al caricamento
     * @return progressDialog
     */
    public static ProgressDialog createProgressDialog(@NonNull Context context,
                                                      @NonNull String message) {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        return progressDialog;
    }

}
