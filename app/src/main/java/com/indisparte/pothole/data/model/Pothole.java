package com.indisparte.pothole.data.model;

import androidx.annotation.NonNull;

/**
 * @author Antonio Di Nuzzo (Indisparte)
 */
public class Pothole {
    private final String user;
    private final Double lat, lon, var;

    public Pothole(@NonNull String user,
                   @NonNull Double lat,
                   @NonNull Double lon,
                   @NonNull Double var
    ) {
        this.user = user;
        this.lat = lat;
        this.lon = lon;
        this.var = var;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLon() {
        return lon;
    }

    public Double getVar() {
        return var;
    }

    public String getUser() {
        return user;
    }

    @NonNull
    @Override
    public String toString() {
        return "Pothole{" +
                "user='" + user + '\'' +
                ", latitude=" + lat +
                ", longitude=" + lon +
                ", variation=" + var +
                '}';
    }
}
