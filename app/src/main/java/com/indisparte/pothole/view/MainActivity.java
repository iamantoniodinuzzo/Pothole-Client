package com.indisparte.pothole.view;


import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.indisparte.pothole.databinding.ActivityMainBinding;
import com.indisparte.pothole.view.viewModel.SharedViewModel;
import com.indisparte.pothole.view.viewModel.SharedViewModelFactory;

/**
 * @author Antonio Di Nuzzo (Indisparte)
 */
public class MainActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();
    private final static int REQUEST_CODE = 100;
    private ActivityMainBinding binding;
    private SharedViewModel sharedViewModel;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        sharedViewModel = new ViewModelProvider(
                this,
                new SharedViewModelFactory(this)
        ).get(SharedViewModel.class);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult: ask request permission");
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0) {
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "onRequestPermissionsResult: permission not granted");
                        return;
                    }
                }
                Log.d(TAG, "onRequestPermissionsResult: permission granted");
                sharedViewModel.getDeviceLocation();
            }
        }
    }



}