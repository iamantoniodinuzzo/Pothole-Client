package com.indisparte.pothole.util;

import android.Manifest;

/**
 * @author Antonio Di Nuzzo (Indisparte)
 */
public class Constant {
    //Location
    public static final int LOCATION_REQUEST_CODE = 1001;
    public static final int ERROR_DIALOG_REQUEST_CODE = 1234;
    public static final String ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    public static final String[] permissions = {ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION};

    //service and broadcast
    public static final int LOCATION_INTERVAL_MILLIS = 4000;
    public static final int FASTEST_INTERVAL_MILLIS = 200;
    public static final int LOCATION_SERVICE_ID = 175;
    public static final String ACTION_START_LOCATION_SERVICE = "startLocationTrackingService";
    public static final String ACTION_STOP_LOCATION_SERVICE = "stopLocationTrackingService";
    public static final String PACKAGE_NAME = "com.indisparte.locationapplication";
    public static final String EXTRA_LOCATION = PACKAGE_NAME + ".location";
    public static final String ACTION_BROADCAST = PACKAGE_NAME + ".broadcast";

    //Map customization
    public static final int DEFAULT_RANGE = 100;
    public static final String DEFAULT_CAMERA_ZOOM = "15f";
    public static final String ZOOM_PREFERENCE_KEY = "zoom_preference";
    public static final String PRECISION_RANGE_KEY = "precision_range";
    public static final String MAP_TYPE_PREFERENCE_KEY = "map_type_preference";
    public static final String DARK_MODE_PRECISION_KEY = "dark_mode";
}
