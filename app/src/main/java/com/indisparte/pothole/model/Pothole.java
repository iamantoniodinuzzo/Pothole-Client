package com.indisparte.pothole.model;

import androidx.annotation.NonNull;

/**
 * @author Antonio Di Nuzzo (Indisparte)
 */
public class Pothole {
    private Double latitude, longitude, variation;

    public Pothole(Double latitude, Double longitude, Double variation) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.variation = variation;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getVariation() {
        return variation;
    }

    public void setVariation(Double variation) {
        this.variation = variation;
    }

    @NonNull
    @Override
    public String toString() {
        return "Pothole{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", variation=" + variation +
                '}';
    }
}
