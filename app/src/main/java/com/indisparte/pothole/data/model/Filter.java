package com.indisparte.pothole.data.model;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

/**
 * @author Antonio Di Nuzzo (Indisparte)
 */
public class Filter {
    private final String radius;
    private final LatLng latLng;

    public Filter(@NonNull String radius, @NonNull LatLng latLng) {
        this.radius = radius;
        this.latLng = latLng;
    }

    public String getRadius() {
        return radius;
    }

    public LatLng getLatLng() {
        return latLng;
    }
}
