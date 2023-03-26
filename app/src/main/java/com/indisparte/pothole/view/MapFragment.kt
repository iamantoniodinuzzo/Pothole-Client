package com.indisparte.pothole.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.indisparte.pothole.databinding.FragmentMapBinding
import com.indisparte.pothole.util.LocationPermissionHandler
import com.indisparte.pothole.view.viewModel.MapViewModel


/**
 * @author Antonio Di Nuzzo (Indisparte)
 */
class MapFragment : Fragment() {

    private lateinit var binding: FragmentMapBinding
    private lateinit var mapView: MapView
    private val mapViewModel: MapViewModel by activityViewModels()
    private lateinit var locationPermissionHandler: LocationPermissionHandler
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var map: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapBinding.inflate(inflater, container, false)
        locationPermissionHandler = LocationPermissionHandler(requireContext())
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())
        setupMapView(savedInstanceState)
        setupButton()
        return binding.root
    }


    private fun setupMapView(savedInstanceState: Bundle?) {
        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { googleMap ->
            map = googleMap
            map.uiSettings.isZoomControlsEnabled = true
            enableMyLocation()
        }
        mapViewModel.currentLocation.observe(viewLifecycleOwner) { latLng ->
            map.clear()
            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("Current location")
            )
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (locationPermissionHandler.hasLocationPermission()) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                    map.addMarker(MarkerOptions().position(latLng).title("You are here!"))
                }
            }
        } else
            locationPermissionHandler.requestLocationPermission(requireActivity())
    }

    private fun setupButton() {
        binding.trackingButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (locationPermissionHandler.hasLocationPermission())
                    startTracking()
                else
                    locationPermissionHandler.requestLocationPermission(
                        requireActivity()
                    )
            } else {
                stopTracking()
            }
        }
    }

    private fun startTracking() {
        mapViewModel.startLocationUpdates()

    }


    private fun stopTracking() {
        mapViewModel.stopLocationUpdates()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }


}
