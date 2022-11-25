package com.indisparte.pothole.view.home;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.indisparte.pothole.R;
import com.indisparte.pothole.databinding.FragmentHomeBinding;
import com.indisparte.pothole.service.PotholeRecognizerService;
import com.indisparte.pothole.view.viewModel.SharedViewModel;

/**
 * @author Antonio Di Nuzzo (Indisparte)
 */
public class HomeFragment extends Fragment {
    private static final String TAG = HomeFragment.class.getSimpleName();
    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private Intent potholeService;
    private static final float DEFAULT_ZOOM = 18f;
    public static final int DEFAULT_RADIUS = 100;

    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private Circle circle;
    private CircleOptions circleOptions;
    private SharedViewModel sharedViewModel;
    private MarkerOptions options;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        potholeService = new Intent(getContext(), PotholeRecognizerService.class);


        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //init viewModel
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        //init options
        options = new MarkerOptions();

        initCircleOptions();

        checkPermissionInitMap();

        //observe LatLng changes
        sharedViewModel.getLatLngMutable().observe(getViewLifecycleOwner(), this::moveCamera);

        binding.findMeBtn.setOnClickListener(view1 -> {
            if (sharedViewModel.isServiceOk() &&
                    sharedViewModel.isLocationPermissionGranted()) {
                sharedViewModel.getDeviceLocation();
            }
        });

        binding.radiusBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                circle.setRadius(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(requireContext(), "New range set", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onStopTrackingTouch: send new range to server");

            }
        });
        binding.toggleButton.addOnButtonCheckedListener((toggleButton, checkedId, isChecked)->{
            //Respond to button selection
            final MaterialButton recording = binding.recordingBtn;
            if (isChecked) {
                startRecordingSession();
                recording.setText("Recording...");
            }else {
                stopRecordingSession();
                recording.setText("Start recording");
            }
        });
    }

    private void startRecordingSession() {
        requireContext().startService(potholeService);
        Log.d(TAG, "Start recording session!");
        Toast.makeText(requireContext(), "Start recording session!", Toast.LENGTH_SHORT).show();
    }

    private void stopRecordingSession(){
        requireContext().stopService(potholeService);
        Log.d(TAG, "Stop recording session!");
        Toast.makeText(requireContext(), "Stop recording session!", Toast.LENGTH_SHORT).show();
    }

    private void initCircleOptions() {
        //init circle options
        circleOptions = new CircleOptions()
                .fillColor(Color.parseColor("#BDBDBDBD"))//customize
                .strokeColor(R.color.teal_700)//customize
                .radius(DEFAULT_RADIUS);
    }

    private void checkPermissionInitMap() {
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        //check if permission granted
        if (mapFragment != null &&
                sharedViewModel.isServiceOk() &&
                sharedViewModel.isLocationPermissionGranted()) {
            Log.d(TAG, "onViewCreated: permission granted");
            initMap(mapFragment);
        }
    }


    private void moveCamera(@NonNull LatLng latLng) {
        Log.d(TAG, "moveCamera: moving the camera to lat: " + latLng.latitude + " lng: " + latLng.longitude);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));

        options.position(latLng)
                .title("My location");

        circle = map.addCircle(
                circleOptions.center(latLng)
        );

        map.addMarker(options);
    }

    private void initMap(SupportMapFragment mapFragment) {
        Log.d(TAG, "initMap: initializing map");
        mapFragment.getMapAsync(googleMap -> {
            map = googleMap;
            if (ActivityCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(requireContext(),
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            map.setMyLocationEnabled(false);
            map.getUiSettings().setMapToolbarEnabled(false);
            //remove get my location default button
            //map.getUiSettings().setMyLocationButtonEnabled(false);
        });
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        stopRecordingSession();
    }

}