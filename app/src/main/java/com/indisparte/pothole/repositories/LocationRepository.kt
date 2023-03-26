package com.indisparte.pothole.repositories

import android.annotation.SuppressLint
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import javax.inject.Inject


/**
 * @author Antonio Di Nuzzo (Indisparte)
 */
class LocationRepository
@Inject
constructor(
    private val fusedLocationProviderClient: FusedLocationProviderClient
) {

    private val locationRequest = LocationRequest.create().apply {
        interval = 1000
        fastestInterval = 500
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    @SuppressLint("MissingPermission")
    fun getLocationUpdates(locationCallback: LocationCallback) {
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }


    fun removeLocationUpdates(locationCallback: LocationCallback) {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }
}




