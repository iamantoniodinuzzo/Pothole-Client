package com.indisparte.pothole.data.model;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

/**
 * @author Antonio Di Nuzzo (Indisparte)
 */
public class Filter {
    private final int radius;
    private final LatLng centerLatLng;

    public Filter(int radius, @NonNull LatLng latLng) {
        this.radius = radius;
        this.centerLatLng = latLng;
    }

    public int getRadius() {
        return radius;
    }

    public LatLng getCenterLatLng() {
        return centerLatLng;
    }
}
