package com.indisparte.pothole.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.indisparte.pothole.di.component.PotholeApplication;

/**
 * Used to load username from the preferences
 *
 * @author Antonio Di Nuzzo (Indisparte)
 */
public class UserPreferenceManager {
    private static final String USERNAME_PREF_KEY = "pref_username";
    private static final String KEY = "username";
    private static UserPreferenceManager INSTANCE = null;
    private SharedPreferences mPreferenceManager;

    private UserPreferenceManager() {
        mPreferenceManager = PotholeApplication
                .getContext()
                .getSharedPreferences(USERNAME_PREF_KEY, Context.MODE_PRIVATE);
    }

    public static UserPreferenceManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new UserPreferenceManager();
        }
        return INSTANCE;
    }

    public static String getUserName() {
        return UserPreferenceManager.getInstance().mPreferenceManager.getString(KEY, null);
    }

    public static void saveUsername(@NonNull String username) {
        SharedPreferences.Editor editor =
                UserPreferenceManager.getInstance().mPreferenceManager.edit();
        editor.putString(KEY, username);
        editor.apply();
    }


}
