package com.indisparte.pothole.model;

import androidx.annotation.NonNull;

/**
 * @author Antonio Di Nuzzo (Indisparte)
 */
public class Filter {
    private final String radius;
    private final Double latitude;
    private final Double longitude;

    public Filter(@NonNull String radius, @NonNull Double latitude, @NonNull Double longitude) {
        this.radius = radius;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getRadius() {
        return radius;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }
}
