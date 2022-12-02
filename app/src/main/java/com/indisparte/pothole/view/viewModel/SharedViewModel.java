package com.indisparte.pothole.view.viewModel;

import android.location.Location;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.model.LatLng;
import com.indisparte.pothole.util.Mode;

/**
 * @author Antonio Di Nuzzo (Indisparte)
 */
public class SharedViewModel extends ViewModel {

    private final MutableLiveData<LatLng> currentLatLng;
    private final MutableLiveData<Location> currentLocation;
    private final MutableLiveData<Boolean> isPermissionGranted;
    private final MutableLiveData<Mode> appMode;

    public SharedViewModel() {
        currentLatLng = new MutableLiveData<>();
        currentLocation = new MutableLiveData<>();
        isPermissionGranted = new MutableLiveData<>(false);
        appMode = new MutableLiveData<>(Mode.LOCATION);
    }


    public MutableLiveData<Mode> getAppMode() {
        return appMode;
    }

    public void setAppMode(Mode appMode) {
        this.appMode.postValue(appMode);
    }

    public void setCurrentLatLng(@NonNull LatLng currentLatLng) {
        this.currentLatLng.postValue(currentLatLng);
    }

    public MutableLiveData<Location> getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation.postValue(currentLocation);
    }

    public void setIsPermissionGranted(@NonNull Boolean isPermissionGranted) {
        this.isPermissionGranted.postValue(isPermissionGranted);
    }

    public MutableLiveData<LatLng> getCurrentLatLng() {
        return currentLatLng;
    }

    public MutableLiveData<Boolean> getIsPermissionGranted() {
        return isPermissionGranted;
    }

}
