package com.indisparte.pothole.view;

import android.os.Bundle;
import android.preference.ListPreference;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

import com.indisparte.pothole.R;
import com.indisparte.pothole.util.Constant;

public class SettingsFragment extends PreferenceFragmentCompat {
    private static final String TAG = SettingsFragment.class.getSimpleName();

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        SeekBarPreference precision_range = findPreference(Constant.PRECISION_RANGE_KEY);
        if (precision_range != null) {
            precision_range.setOnPreferenceChangeListener((preference, newValue) -> {
                Log.d(TAG, "onPreferenceChange: new range " + (int) newValue);
                return true;
            });
        }


        ListPreference camera_zoom = findPreference(Constant.ZOOM_PREFERENCE_KEY);
        if (camera_zoom != null) {
            camera_zoom.setOnPreferenceChangeListener((preference, newValue) -> {
                String zoom_string_value = (String) newValue;
                float zoom_float_value = Float.parseFloat(zoom_string_value);
                Log.d(TAG, "onPreferenceChange: new zoom " + zoom_float_value);
                return true;
            });
        }

        SwitchPreferenceCompat darkMode = findPreference(Constant.DARK_MODE_PRECISION_KEY);
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