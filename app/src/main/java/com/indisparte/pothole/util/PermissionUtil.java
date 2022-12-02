package com.indisparte.pothole.util;


import static com.indisparte.pothole.util.Constant.*;

import android.app.Activity;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

/**
 * Provides location permission management
 * @author Antonio Di Nuzzo (Indisparte)
 */
public class PermissionUtil {
    private static PermissionUtil INSTANCE = null;
    private Activity activity;
    private String[] permissions;
    private static final String TAG = PermissionUtil.class.getSimpleName();

    //need to be private
    private PermissionUtil() {
    }

    /**
     *
     * @param activity The activity
     * @param permissions Array of needed permissions
     * @return {@link PermissionUtil} class instance
     */
    public static PermissionUtil getInstance(@NonNull Activity activity, @NonNull String[] permissions) {
        if (INSTANCE == null) {
            INSTANCE = new PermissionUtil();
            INSTANCE.permissions = permissions;
            INSTANCE.activity = activity;
        }
        return INSTANCE;
    }

    /**
     * Check if all permission needed, specified into {@link PermissionUtil#permissions},
     * are granted.
     *
     * @return True if all permissions are granted, false otherwise
     */
    public boolean areMyPermissionGranted() {
        for (String permission : permissions) {
            if (!checkPermission(permission)) return false;
        }
        return true;
    }

    /**
     * Check if permission is granted
     *
     * @param permission The permission needed
     * @return True if permission is granted, false otherwise
     */
    private boolean checkPermission(@NonNull String permission) {
        return ContextCompat.checkSelfPermission(
                activity,
                permission
        ) == PackageManager.PERMISSION_GRANTED;
    }

    public void askLocationPermission() {
        if (!areMyPermissionGranted()) {
            Log.d(TAG, "askLocationPermission: permission not granted, asking permissions");
            ActivityCompat.requestPermissions(
                    activity,
                    permissions,
                    LOCATION_REQUEST_CODE
            );
        } else {
            Log.d(TAG, "askLocationPermission: location permission granted");
        }
    }

    /**
     * Check if google service is enabled in this device.
     * If not and user can resolve, display an alert dialog.
     */
    public boolean isGoogleServiceOk() {
        Log.d(TAG, "isServiceOk: checking google service version ");
        final GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        final int available = googleApiAvailability.isGooglePlayServicesAvailable(activity);

        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServiceOk: Google Play Services is working");
            return true;
        } else if (googleApiAvailability.isUserResolvableError(available)) {
            //an error occurred but we can resolve it
            Log.e(TAG, "isServiceOk: an error occurred but we can fix it");
            Dialog dialog = googleApiAvailability.getErrorDialog(
                    activity,
                    available,
                    ERROR_DIALOG_REQUEST_CODE);
            dialog.show();
        } else {
            Log.e(TAG, "isGoogleServiceOk: Can't map request");
        }
        return false;
    }

    private void showPermissionRational(@NonNull String deniedPermission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, deniedPermission)) {
                AlertUtil.showMessagePositiveBtn(
                        activity,
                        "You need to allow access to the permission(s)!",
                        "Ask permissions",
                        (dialogInterface, i) -> {
                            askLocationPermission();
                        });
            }
        }
    }


    /**
     * Redirect the onRequestPermissionsResult and show a rational if a permission is not granted
     *
     * @param requestCode  The request code
     * @param permissions  The request permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions.
     * @return True if all permissions are granted, false otherwise
     */
    public boolean onRequestPermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    Log.d(TAG, "onRequestPermissionsResult: permission not granted, show rationale");
                    showPermissionRational(permissions[i]);
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
