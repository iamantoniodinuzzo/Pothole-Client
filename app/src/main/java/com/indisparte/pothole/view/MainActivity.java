package com.indisparte.pothole.view;

import static com.indisparte.pothole.util.Constant.DARK_MODE_PRECISION_KEY;
import static com.indisparte.pothole.util.Constant.LOCATION_REQUEST_CODE;
import static com.indisparte.pothole.util.Constant.permissions;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.indisparte.pothole.R;
import com.indisparte.pothole.data.network.PotholeRepository;
import com.indisparte.pothole.databinding.ActivityMainBinding;
import com.indisparte.pothole.util.PermissionUtil;
import com.indisparte.pothole.view.viewModel.SharedViewModel;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private PermissionUtil permissionUtil;
    private ActivityMainBinding binding;
    private SharedViewModel sharedViewModel;
    private NavController navController;
    private SharedPreferences preferences;
    @Inject
    protected PotholeRepository mPotholeRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadSettings();
        permissionUtil = PermissionUtil.getInstance(this, permissions);
        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        sharedViewModel.getAppMode().observe(this, mode -> binding.setMode(mode));

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }
        setSupportActionBar(binding.toolbar);
        NavigationUI.setupActionBarWithNavController(this, navController);


    }


    private void loadSettings() {
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean dark_theme = preferences.getBoolean(DARK_MODE_PRECISION_KEY, false);
        if (dark_theme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: check permissions");
        if (permissionUtil.areMyPermissionGranted()) {
            Log.d(TAG, "onStart: permission granted, check google service");
            if (permissionUtil.isGoogleServiceOk()) {
                Log.d(TAG, "onStart: google service ok, get location");
                sharedViewModel.setIsPermissionGranted(true);
            } else {
                //you can't map request
                sharedViewModel.setIsPermissionGranted(false);
                Log.e(TAG, "onStart: You can't map request, google service not available");
                Toast.makeText(this, "You can't map request, google service not available", Toast.LENGTH_SHORT).show();
            }
        } else {
            sharedViewModel.setIsPermissionGranted(false);
            Log.d(TAG, "onStart: permission not granted, ask location permission");
            permissionUtil.askLocationPermission();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            Log.d(TAG, "onRequestPermissionsResult: check if permissions granted");
            //redirect to util class
            boolean permission_granted = permissionUtil.onRequestPermissionResult(requestCode, permissions, grantResults);
            if (permission_granted) {
                Log.d(TAG, "onRequestPermissionsResult: Permission granted, get location");
                sharedViewModel.setIsPermissionGranted(true);
            } else {
                sharedViewModel.setIsPermissionGranted(false);
                Log.d(TAG, "onRequestPermissionsResult: Permission not granted");
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return NavigationUI.onNavDestinationSelected(item, navController)
                || super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}