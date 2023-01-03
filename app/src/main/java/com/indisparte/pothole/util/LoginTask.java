package com.indisparte.pothole.util;

import android.widget.Toast;

/**
 * @author Antonio Di Nuzzo (Indisparte)
 */
public class LoginTask extends BaseTask<String>{


    @Override
    public String doWork(Object... objects) throws Exception {
        return null;
    }

    @Override
    public void onResult(String result) {

    }

    @Override
    public void onError() {
        Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
    }
}
