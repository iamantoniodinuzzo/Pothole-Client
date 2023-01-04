package com.indisparte.pothole.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

/**
 * @author Antonio Di Nuzzo (Indisparte)
 */
public class ServiceUtil {
    private static final String TAG = ServiceUtil.class.getSimpleName();
    /**
     * Check if a specific service is running.
     *
     * @param service The service class
     * @param <T>     Must extends {@link Service}
     * @return True if service is running, false otherwise.
     */
    public static <T extends Service> boolean isThisServiceRunning(@NonNull Activity activity, @NonNull Class<T> service) {
        final String serviceName = service.getName();
        Log.d(TAG, "isThisServiceRunning: check if service (" + serviceName + ") is running");
        ActivityManager activityManager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            for (ActivityManager.RunningServiceInfo running_service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceName.equals(running_service.service.getClassName())) {
                    Log.d(TAG, "isThisServiceRunning: service (" + serviceName + ") is running");
                    return true;
                }
            }
            Log.e(TAG, "isThisServiceRunning: service (" + serviceName + ") is NOT running");
            return false;
        }
        Log.e(TAG, "isThisServiceRunning: service (" + serviceName + ") is NOT running");
        return false;
    }

    /**
     * Start a specific service
     *
     * @param service The service class
     * @param action  An action
     * @param <T>     Must extends {@link Service}
     */
    public static <T extends Service> void startService(@NonNull Activity activity,@NonNull Class<T> service, @NonNull String action, double mThreshold) {
        if (!isThisServiceRunning(activity,service)) {
            Intent intent = new Intent(activity.getApplicationContext(), service);
            intent.setAction(action);
            if (mThreshold != 0)
                intent.putExtra("threshold", mThreshold);
            activity.startService(intent);
            Log.d(TAG, "startService: Service (" + service.getName() + ") started");
        }
    }

    /**
     * Stop a specific service
     *
     * @param service The service class
     * @param action  An action, can be null. If is null service is only stopped.
     * @param <T>     Must extends {@link Service}
     */
    public static  <T extends Service> void stopService(@NonNull Activity activity,@NonNull Class<T> service, @NonNull String action) {
        if (isThisServiceRunning(activity,service)) {
            Intent intent = new Intent(activity.getApplicationContext(), service);
            intent.setAction(action);
            activity.startService(intent);
            Log.d(TAG, "stopService: Service (" + service.getName() + ") stopped");
        }
    }
}
