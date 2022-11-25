package com.indisparte.pothole.view.viewModel;

import static com.indisparte.pothole.util.Constant.COURSE_LOCATION;
import static com.indisparte.pothole.util.Constant.FINE_LOCATION;

import android.app.Activity;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;

public class SharedViewModel extends ViewModel {
    private static final String TAG = SharedViewModel.class.getSimpleName();
    private final static int REQUEST_CODE = 100;
    private static final int ERROR_DIALOG_REQUEST_CODE = 1234;
    private Activity activity;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private final MutableLiveData<LatLng> latLngMutable;


    public SharedViewModel(Activity activity) {
        this.activity = activity;
        latLngMutable = new MutableLiveData<>();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity);

    }
    public MutableLiveData<LatLng> getLatLngMutable() {
        return latLngMutable;
    }


    public void setLatLngMutable(@NonNull LatLng latLngMutable) {
        this.latLngMutable.postValue(latLngMutable);
    }

    public void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the device current location");
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity);
        try {
            if (isLocationPermissionGranted()) {
                Task<Location> location = fusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener((task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "getDeviceLocation: found location");
                        Location currentLocation = task.getResult();
                        //update viewModel
                        LatLng latLng = new LatLng(
                                currentLocation.getLatitude(),
                                currentLocation.getLongitude()
                        );
                        setLatLngMutable(latLng);

                    } else {
                        Log.d(TAG, "getDeviceLocation: current location is null");
                        Toast.makeText(activity, "Unable to get current location", Toast.LENGTH_SHORT).show();
                    }
                }));
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    //check for location services enabled
    public boolean isLocationPermissionGranted() {
        Log.d(TAG, "isLocationPermissionGranted: check if location permission granted");
        String[] permissions = {
                FINE_LOCATION,
                COURSE_LOCATION
        };

        if (isPermissionGranted(FINE_LOCATION) && isPermissionGranted(COURSE_LOCATION)) {
            Log.d(TAG, "isLocationPermissionGranted: granted");
            return true;
        } else {
            Log.d(TAG, "isLocationPermissionGranted: request permission");
            ActivityCompat.requestPermissions(
                    activity,
                    permissions,
                    REQUEST_CODE
            );
        }
        return false;
    }

    private boolean isPermissionGranted(@NonNull String permission) {
        return ContextCompat.checkSelfPermission(
                activity,
                permission
        ) == PackageManager.PERMISSION_GRANTED;
    }

    //check if google service is ok
    public boolean isServiceOk() {
        Log.d(TAG, "isServiceOk: checking google service version ");
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity);

        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServiceOk: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occurred but we can resolve it
            Log.d(TAG, "isServiceOk: an error occurred but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(
                    activity,
                    available,
                    ERROR_DIALOG_REQUEST_CODE);
            dialog.show();
        } else {
            Toast.makeText(activity, "You can't map request", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

}
