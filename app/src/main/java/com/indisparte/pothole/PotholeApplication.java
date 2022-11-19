package com.indisparte.pothole;

import android.app.Application;
import android.content.Context;

/**
 * @author Antonio Di Nuzzo (Indisparte)
 */
public class PotholeApplication extends Application {
    public static final String LOG_TAG = "Pothole application";
    public static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;
    }
}
