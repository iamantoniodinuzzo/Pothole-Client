package com.indisparte.pothole.model;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

/**
 * @author Antonio Di Nuzzo (Indisparte)
 */
public class Pothole {
    private final LatLng latLng;
    private final Double variation;

    public Pothole(@NonNull LatLng latLng, @NonNull Double variation) {
        this.latLng = latLng;
        this.variation = variation;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public double getVariation() {
        return variation;
    }

    @NonNull
    @Override
    public String toString() {
        return "Pothole{" +
                "latLng=" + latLng +
                ", variation=" + variation +
                '}';
    }
}
