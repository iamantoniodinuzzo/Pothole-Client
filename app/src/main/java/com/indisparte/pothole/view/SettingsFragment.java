package com.indisparte.pothole.view;

import static com.indisparte.pothole.util.Constant.DARK_MODE_PRECISION_KEY;
import static com.indisparte.pothole.util.Constant.PRECISION_RANGE_KEY;
import static com.indisparte.pothole.util.Constant.ZOOM_PREFERENCE_KEY;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreferenceCompat;

import com.indisparte.pothole.R;

public class SettingsFragment extends PreferenceFragmentCompat {
    private static final String TAG = SettingsFragment.class.getSimpleName();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupToolbar(view);
    }

    private void setupToolbar(@NonNull View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        NavController navController = Navigation.findNavController(view);
        AppBarConfiguration appBarConfiguration =
                new AppBarConfiguration.Builder(R.id.mapsFragment).build();
        NavigationUI.setupWithNavController(
                toolbar, navController, appBarConfiguration);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);


        SeekBarPreference precision_range = findPreference(PRECISION_RANGE_KEY);
        if (precision_range != null) {
            precision_range.setOnPreferenceChangeListener((preference, newValue) -> {
                Log.d(TAG, "onPreferenceChange: new range " + (int) newValue);
                return true;
            });
        }


        ListPreference camera_zoom = findPreference(ZOOM_PREFERENCE_KEY);
        if (camera_zoom != null) {
            camera_zoom.setOnPreferenceChangeListener((preference, newValue) -> {
                String zoom_string_value = (String) newValue;
                float zoom_float_value = Float.parseFloat(zoom_string_value);
                Log.d(TAG, "onPreferenceChange: new zoom " + zoom_float_value);
                return true;
            });
        }


        SwitchPreferenceCompat darkMode = findPreference(DARK_MODE_PRECISION_KEY);
        if (darkMode != null) {
            darkMode.setOnPreferenceChangeListener((preference, enabled) -> {
                boolean active = (boolean) enabled;
                Log.d(TAG, "onCreatePreferences: dark mode enabled: " + active);
                if (active) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

                }
                return true;
            });
        }
    }
}