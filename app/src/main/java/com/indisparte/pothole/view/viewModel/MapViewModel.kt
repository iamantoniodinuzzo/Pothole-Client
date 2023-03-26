package com.indisparte.pothole.view.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import com.indisparte.pothole.repositories.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


/**
 * @author Antonio Di Nuzzo (Indisparte)
 */
@HiltViewModel
class MapViewModel
@Inject constructor(
    private val locationRepository: LocationRepository) : ViewModel() {

    private val _currentLocation = MutableLiveData<LatLng>()
    val currentLocation: LiveData<LatLng>
        get() = _currentLocation

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let {
                _currentLocation.postValue(LatLng(it.latitude, it.longitude))
            }
        }
    }

    fun startLocationUpdates() {
        locationRepository.getLocationUpdates(locationCallback)
    }



    fun stopLocationUpdates() {
        locationRepository.removeLocationUpdates(locationCallback)
    }
}


