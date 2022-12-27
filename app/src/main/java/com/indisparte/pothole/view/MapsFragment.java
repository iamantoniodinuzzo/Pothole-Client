package com.indisparte.pothole.view;

import static com.indisparte.pothole.util.Constant.ACTION_BROADCAST;
import static com.indisparte.pothole.util.Constant.ACTION_START_LOCATION_SERVICE;
import static com.indisparte.pothole.util.Constant.ACTION_START_POTHOLE_SERVICE;
import static com.indisparte.pothole.util.Constant.ACTION_STOP_LOCATION_SERVICE;
import static com.indisparte.pothole.util.Constant.ACTION_STOP_POTHOLE_SERVICE;
import static com.indisparte.pothole.util.Constant.DEFAULT_CAMERA_ZOOM;
import static com.indisparte.pothole.util.Constant.DEFAULT_RANGE;
import static com.indisparte.pothole.util.Constant.EXTRA_DELTA_Z;
import static com.indisparte.pothole.util.Constant.EXTRA_LOCATION;
import static com.indisparte.pothole.util.Constant.MAP_TYPE_PREFERENCE_KEY;
import static com.indisparte.pothole.util.Constant.PRECISION_RANGE_KEY;
import static com.indisparte.pothole.util.Constant.ZOOM_PREFERENCE_KEY;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

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
import com.indisparte.pothole.R;
import com.indisparte.pothole.data.model.Filter;
import com.indisparte.pothole.data.model.Pothole;
import com.indisparte.pothole.data.network.PotholeRepository;
import com.indisparte.pothole.databinding.FragmentMapsBinding;
import com.indisparte.pothole.service.LocationTrackingService;
import com.indisparte.pothole.service.PotholeRecognizerService;
import com.indisparte.pothole.util.Mode;
import com.indisparte.pothole.util.UserPreferenceManager;
import com.indisparte.pothole.view.viewModel.SharedViewModel;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;


/**
 * @author Antonio Di Nuzzo (Indisparte)
 */
@AndroidEntryPoint
public class MapsFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = MapsFragment.class.getSimpleName();
    private FragmentMapsBinding binding;
    private SharedViewModel sharedViewModel;
    private LocationReceiver mLocationReceiver;
    private PotholeReceiver mPotholeReceiver;
    private GoogleMap map;
    private Marker carMarker, locationMarker;
    private Circle userLocationAccuracyCircle;
    private SharedPreferences preferences;
    private float zoom;
    private NavController mNavController;
    private Toolbar mToolbar;
    private int radius, mapType;
    private CircleOptions circleOptions;
    private SupportMapFragment mapFragment;
    @Inject
    protected PotholeRepository mPotholeRepository;
    private double mThreshold;
    private HashSet<Marker> potholeMarkers;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: init sharedViewModel, myReceiver and preferences");
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        mLocationReceiver = new LocationReceiver();
        mPotholeReceiver = new PotholeReceiver();
        potholeMarkers = new HashSet<>();
        preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMapsBinding.inflate(inflater, container, false);
        mToolbar = binding.toolbar;

        initMap();
        updateThreshold();
        ((AppCompatActivity) requireActivity()).setSupportActionBar(binding.toolbar);
        setHasOptionsMenu(true);
        return binding.getRoot();
    }

    private void updateThreshold() {
        AsyncTask.execute(() -> {
            try {
                mThreshold = mPotholeRepository.getThreshold();
                Log.d(TAG, "updateThreshold, successfully retrieve threshold: " + mThreshold);
            } catch (IOException e) {
                Log.e(TAG, "updateThreshold, Error: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupToolbar(view);
        loadSettings();

        sharedViewModel.getAppMode().observe(getViewLifecycleOwner(), mode -> binding.setMode(mode));

        sharedViewModel.getIsPermissionGranted().observe(getViewLifecycleOwner(), areGranted -> {
            binding.setPermissions(areGranted);
            if (areGranted && !isThisServiceRunning(LocationTrackingService.class)) {
                getLocation();
            }
        });

        sharedViewModel.getAppMode().observe(getViewLifecycleOwner(), mode -> binding.setMode(mode));


        sharedViewModel.getCurrentLocation().observe(getViewLifecycleOwner(), this::updateMyTrackerMarker);

        binding.locateMe.setOnClickListener(locateMe -> {
            if (!isThisServiceRunning(LocationTrackingService.class)) getLocation();
        });

        binding.trackingBtn.setOnCheckedChangeListener((compoundButton, tracking) -> {
            if (tracking) {
                Log.d(TAG, "onViewCreated: start tracking mode");
                sharedViewModel.setAppMode(Mode.TRACKING);
                startService(LocationTrackingService.class, ACTION_START_LOCATION_SERVICE);
                startService(PotholeRecognizerService.class, ACTION_START_POTHOLE_SERVICE);
                removeLocationMarker();
            } else {
                Log.d(TAG, "onViewCreated: stop tracking mode");
                sharedViewModel.setAppMode(Mode.LOCATION);
                stopService(LocationTrackingService.class, ACTION_STOP_LOCATION_SERVICE);
                stopService(PotholeRecognizerService.class, ACTION_STOP_POTHOLE_SERVICE);
                removeCarMarker();
                getLocation();
            }
        });
    }

    private void setupToolbar(@NonNull View view) {
        mNavController = Navigation.findNavController(view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(R.id.settingsFragment, R.id.mapsFragment).build();
        NavigationUI.setupWithNavController(mToolbar, mNavController, appBarConfiguration);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return NavigationUI.onNavDestinationSelected(item, mNavController) || super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_menu, menu);
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
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(requireContext());
        Log.d(TAG, "onPause: unregister Location receiver");
        localBroadcastManager.unregisterReceiver(mLocationReceiver);

        Log.d(TAG, "onPause: unregister Pothole receiver");
        localBroadcastManager.unregisterReceiver(mPotholeReceiver);
        super.onPause();
    }

    @Override
    public void onResume() {
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(requireContext());

        Log.d(TAG, "onResume: register receiver for Location recognizer");
        localBroadcastManager.registerReceiver(mLocationReceiver, new IntentFilter(ACTION_BROADCAST));
        Log.d(TAG, "onResume: register receiver for Pothole recognizer");
        localBroadcastManager.registerReceiver(mPotholeReceiver, new IntentFilter(ACTION_BROADCAST));

        super.onResume();
    }

    private void getLocation() {
        try {
            Log.d(TAG, "getLocation: try to get location");
            @SuppressLint("MissingPermission") Task<Location> locationTask =
                    LocationServices.getFusedLocationProviderClient(requireActivity()).getLastLocation();
            locationTask.addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Location location = task.getResult();
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    Log.d(TAG, "getLocation: successful, latitude = " + latLng.latitude + " longitude = " + latLng.longitude);
                    setMyLocationMarker(latLng);
                    getPotholesInMyArea(latLng);
                } else {
                    Log.e(TAG, "getLocation: failed, current location is null");
                    Toast.makeText(requireContext(), "Unable to get current location, please granted permissions from settings", Toast.LENGTH_LONG).show();
                }
            });
        } catch (SecurityException e) {
            Log.e(TAG, "getLocation: SecurityException: " + e.getMessage());
            Toast.makeText(requireContext(), "SecurityException: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    private void getPotholesInMyArea(LatLng latLng) {
        AsyncTask.execute(() -> {
            try {
                Set<Pothole> potholes = mPotholeRepository.getPotholesByRange(new Filter(radius, latLng));
                Log.d(TAG, "getPotholesInMyArea, get all potholes successfully : " + potholes);
                putPotholesOnMap(potholes);
            } catch (IOException e) {
                Log.e(TAG, "getPotholesInMyArea, error: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void putPotholesOnMap(Set<Pothole> potholes) {
        requireActivity().runOnUiThread(() -> {
            for (Pothole p : potholes) {
                setPotholeMarker(p);
            }
        });
    }

    /**
     * Check if a specific service is running.
     *
     * @param service The service class
     * @param <T>     Must extends {@link Service}
     * @return True if service is running, false otherwise.
     */
    private <T extends Service> boolean isThisServiceRunning(@NonNull Class<T> service) {
        final String serviceName = service.getName();
        Log.d(TAG, "isThisServiceRunning: check if service (" + serviceName + ") is running");
        ActivityManager activityManager = (ActivityManager) requireActivity().getSystemService(Context.ACTIVITY_SERVICE);
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
     * @param action  An action, can be null
     * @param <T>     Must extends {@link Service}
     */
    private <T extends Service> void startService(@NonNull Class<T> service, @NonNull String action) {
        if (!isThisServiceRunning(service)) {
            Intent intent = new Intent(requireActivity().getApplicationContext(), service);
            intent.setAction(action);
            if (mThreshold != 0)
                intent.putExtra("threshold", mThreshold);
            requireActivity().startService(intent);
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
    private <T extends Service> void stopService(@NonNull Class<T> service, @NonNull String action) {
        if (isThisServiceRunning(service)) {
            Intent intent = new Intent(requireActivity().getApplicationContext(), service);
            intent.setAction(action);
            requireActivity().startService(intent);
            Log.d(TAG, "stopService: Service (" + service.getName() + ") stopped");
        }
    }

    /**
     * Set placeholder when user request location
     */
    private void setMyLocationMarker(@NonNull LatLng latLng) {
        if (locationMarker == null) {
            //create a new marker options and update marker
            MarkerOptions positionMarkerOptions = new MarkerOptions().position(latLng);
            locationMarker = map.addMarker(positionMarkerOptions);
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        } else {
            //use the previously marker
            locationMarker.setPosition(latLng);
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        }
        setUserAccuracyCircle(latLng);
    }

    private void setPotholeMarker(@NonNull Pothole pothole) {
        MarkerOptions potholeMarkerOptions = new MarkerOptions()
                .position(new LatLng(pothole.getLat(), pothole.getLon()))
                .snippet("Found by " + pothole.getUser())
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_hole));
        potholeMarkers.add(map.addMarker(potholeMarkerOptions));
        Log.d(TAG, "setPotholeMarker: Pothole added");
    }

    private void removeAllPotholeMarkers() {
        for (Marker m : potholeMarkers) {
            m.remove();
        }
        potholeMarkers.clear();
        Log.d(TAG, "removeAllPotholeMarkers: All potholes removed");
    }

    /**
     * The circle around location
     */
    private void setUserAccuracyCircle(LatLng latLng) {
        if (userLocationAccuracyCircle == null) {
            circleOptions = new CircleOptions()
                    .center(latLng).
                    strokeWidth(4)
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
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.blu_car)) // TODO: 26/12/2022 Customize
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
        removeAllPotholeMarkers();
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
     * Receiver for broadcasts sent by {@link LocationTrackingService}..
     * This receiver's main action is to receive location changes.
     */
    private class LocationReceiver extends BroadcastReceiver {
        private final String TAG = LocationReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(EXTRA_LOCATION);

            if (location != null) {
                Log.d(TAG, "onReceive: tracking location, current: " + location);
                sharedViewModel.setCurrentLocation(location);
            }
        }
    }

    /**
     * Receiver for broadcasts sent by {@link PotholeRecognizerService}.
     * This receiver's main action is to receive acceleration changes on Z axs (pothole),
     * and create e new pothole object with last found location.
     * Then this service send the new pothole to the server.
     */
    private class PotholeReceiver extends BroadcastReceiver {
        private final String TAG = PotholeReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            double deltaZ = intent.getDoubleExtra(EXTRA_DELTA_Z, 0);

            Location location = sharedViewModel.getLastLocation();
            if (location != null && deltaZ != 0) {
                Toast.makeText(context, "Pothole found!", Toast.LENGTH_SHORT).show();// TODO: 24/12/2022 make a sound
                Pothole newPothole = new Pothole(UserPreferenceManager.getUserName(), location.getLatitude(), location.getLongitude(), deltaZ);
                Log.d(TAG, "onReceive: Pothole (" + newPothole + ") found ");
                sendNewPotholeToServer(newPothole);
                setPotholeMarker(newPothole);
            }

        }

        private void sendNewPotholeToServer(Pothole pothole) {
            AsyncTask.execute(() -> {
                try {
                    mPotholeRepository.addPothole(pothole);
                    Log.d(TAG, "sendNewPotholeToServer: New Pothole send successfully");
                } catch (IOException e) {
                    Log.e(TAG, "sendNewPotholeToServer error : " + e.getMessage());
                    e.printStackTrace();
                }

            });
        }
    }


}