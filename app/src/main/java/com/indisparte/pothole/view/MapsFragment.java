package com.indisparte.pothole.view;

import static com.indisparte.pothole.util.Constant.*;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.indisparte.pothole.databinding.FragmentMapsBinding;
import com.indisparte.pothole.util.Mode;
import com.indisparte.pothole.view.viewModel.SharedViewModel;


/**
 * @author Antonio Di Nuzzo (Indisparte)
 */
public class MapsFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = MapsFragment.class.getSimpleName();
    private FragmentMapsBinding binding;
    private SharedViewModel sharedViewModel;
    private LocationReceiver locationReceiver;
    private GoogleMap map;
    private Marker carMarker, locationMarker;
    private Circle userLocationAccuracyCircle;
    private SharedPreferences preferences;
    private float zoom;
    private int radius, mapType;
    private CircleOptions circleOptions;
    private SupportMapFragment mapFragment;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: init sharedViewModel, myReceiver and preferences");
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        locationReceiver = new LocationReceiver();
        preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMapsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        initMap();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadSettings();

        sharedViewModel.getIsPermissionGranted().observe(getViewLifecycleOwner(), areGranted -> {
            binding.setPermissions(areGranted);
            if (areGranted && !isLocationServiceRunning()) {
                getLocation();
            }
        });

        sharedViewModel.getAppMode().observe(getViewLifecycleOwner(), mode -> binding.setMode(mode));


        sharedViewModel.getCurrentLocation().observe(getViewLifecycleOwner(), this::updateMyTrackerMarker);

        binding.locateMe.setOnClickListener(locateMe -> {
            if (!isLocationServiceRunning()) getLocation();
        });

        binding.trackingBtn.setOnCheckedChangeListener((compoundButton, tracking) -> {
            if (tracking) {
                Log.d(TAG, "onViewCreated: start tracking mode");
                sharedViewModel.setAppMode(Mode.TRACKING);
                startLocationService();
                removeLocationMarker();
            } else {
                Log.d(TAG, "onViewCreated: stop tracking mode");
                sharedViewModel.setAppMode(Mode.LOCATION);
                stopLocationService();
                removeCarMarker();
                getLocation();
            }
        });
    }

    private void loadSettings() {
        String zoom_string_value = preferences.getString(ZOOM_PREFERENCE_KEY, DEFAULT_CAMERA_ZOOM);//default street level
        String map_type = preferences.getString(MAP_TYPE_PREFERENCE_KEY, "1");//default normal map
        zoom = Float.parseFloat(zoom_string_value);
        radius = preferences.getInt(PRECISION_RANGE_KEY, DEFAULT_RANGE);//default 100 meters
        mapType = Integer.parseInt(map_type);

        Log.d(TAG, "loadSettings: settings loaded, zoom: " + zoom + ", radius: " + radius + ", map type: " + map_type);
    }


    private void initMap() {
        Log.d(TAG, "initMap: init map");
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause: unregister receiver");
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(locationReceiver);
        super.onPause();
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume: register receiver");
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(locationReceiver,
                new IntentFilter(ACTION_BROADCAST));
        super.onResume();
    }

    private void getLocation() {
        try {
            Log.d(TAG, "getLocation: try to get location");
            @SuppressLint("MissingPermission") Task<Location> locationTask = LocationServices.getFusedLocationProviderClient(requireActivity()).getLastLocation();
            locationTask.addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Location location = task.getResult();
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    Log.d(TAG, "getLocation: successful, latitude = " + latLng.latitude + " longitude = " + latLng.longitude);
                    setMyLocationMarker(latLng);
                } else {
                    Log.d(TAG, "getLocation: failed, current location is null");
                    Toast.makeText(requireContext(), "Unable to get current location, please granted permissions", Toast.LENGTH_LONG).show();
                }
            });
        } catch (SecurityException e) {
            Log.e(TAG, "getLocation: SecurityException: " + e.getMessage());
            Toast.makeText(requireContext(), "SecurityException: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    private boolean isLocationServiceRunning() {
        Log.d(TAG, "isLocationServiceRunning: check if service is running");
        ActivityManager activityManager =
                (ActivityManager) requireActivity().getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            for (ActivityManager.RunningServiceInfo running_service :
                    activityManager.getRunningServices(Integer.MAX_VALUE)) {
                if (LocationTrackingService.class.getName().equals(running_service.service.getClassName())) {
                    if (running_service.foreground) {
                        Log.d(TAG, "isLocationServiceRunning: service is running");
                        return true;
                    }
                }
            }
            Log.d(TAG, "isLocationServiceRunning: service is not running");
            return false;
        }
        Log.d(TAG, "isLocationServiceRunning: service is not running");
        return false;
    }

    private void startLocationService() {
        if (!isLocationServiceRunning()) {
            Intent intent = new Intent(requireActivity().getApplicationContext(), LocationTrackingService.class);
            intent.setAction(ACTION_START_LOCATION_SERVICE);
            requireActivity().startService(intent);
            Log.d(TAG, "startLocationService: Location tracking service started");
            Toast.makeText(requireContext(), "Location tracking service started", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopLocationService() {
        if (isLocationServiceRunning()) {
            Intent intent = new Intent(requireActivity().getApplicationContext(), LocationTrackingService.class);
            intent.setAction(ACTION_STOP_LOCATION_SERVICE);
            requireActivity().startService(intent);
            Log.d(TAG, "stopLocationService: Location tracking service stopped");
            Toast.makeText(requireContext(), "Location tracking service stopped", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Set placeholder when user request location
     */
    private void setMyLocationMarker(@NonNull LatLng latLng) {
        if (locationMarker == null) {
            //create a new marker options and update marker
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latLng);
            locationMarker = map.addMarker(markerOptions);
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        } else {
            //use the previously marker
            locationMarker.setPosition(latLng);
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        }
        setUserAccuracyCircle(latLng);
    }

    /**
     * The circle around location
     */
    private void setUserAccuracyCircle(LatLng latLng) {
        if (userLocationAccuracyCircle == null) {
            circleOptions = new CircleOptions()
                    .center(latLng)
                    .strokeWidth(4)
                    .strokeColor(Color.argb(255, 255, 0, 0))//TODO customize
                    .fillColor(Color.argb(32, 255, 0, 0))//TODO customize
                    .radius(radius);
            userLocationAccuracyCircle = map.addCircle(circleOptions);
        } else {
            userLocationAccuracyCircle.setRadius(radius);
            userLocationAccuracyCircle.setCenter(latLng);
        }
    }

    /**
     * Update tracker when user is in tracking mode so tracker is movable
     */
    private void updateMyTrackerMarker(@NonNull Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (map != null) {
            if (carMarker == null) {
                //create a new marker
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.blu_car))
                        .anchor((float) 0.5, (float) 0.5)
                        .rotation(location.getBearing());
                carMarker = map.addMarker(markerOptions);
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
            } else {
                //use the previously marker
                carMarker.setPosition(latLng);
                carMarker.setRotation(location.getBearing());
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
            }
        } else {
            initMap();
        }

    }

    private void removeLocationMarker() {
        if (locationMarker != null && userLocationAccuracyCircle != null) {
            locationMarker.remove();
            userLocationAccuracyCircle.remove();
            userLocationAccuracyCircle = null;
            locationMarker = null;
        }
    }

    private void removeCarMarker() {
        if (carMarker != null) {
            carMarker.remove();
            carMarker = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * In this case, we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to
     * install it inside the SupportMapFragment. This method will only be triggered once the
     * user has installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        if (map == null) {
            Log.d(TAG, "onMapReady: map is null, set map ");
            map = googleMap;
        }

        //customize map
        map.setMapType(mapType);
        map.setBuildingsEnabled(true);
        map.getUiSettings().setMapToolbarEnabled(false);
        map.getUiSettings().setMyLocationButtonEnabled(false);
    }

    /**
     * Receiver for broadcasts sent by {@link LocationTrackingService}.
     */
    private class LocationReceiver extends BroadcastReceiver {
        private final String TAG = LocationReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(EXTRA_LOCATION);
            if (location != null) {
                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                Log.d(TAG, "onReceive: tracking location, current: " + currentLatLng);
//                sharedViewModel.setCurrentLatLng(currentLatLng);
                sharedViewModel.setCurrentLocation(location);
            }
        }
    }
}