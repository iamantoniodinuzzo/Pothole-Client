package com.indisparte.pothole.util;

import android.Manifest;

public class Constant {
    //Location
    public static final int LOCATION_REQUEST_CODE = 1001;
    public static final int ERROR_DIALOG_REQUEST_CODE = 1234;
    public static final String ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    public static final String[] permissions = {ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION};
}

