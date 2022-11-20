package com.indisparte.pothole.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.indisparte.pothole.model.Filter;
import com.indisparte.pothole.model.Pothole;

import java.util.List;

/**
 * @author Antonio Di Nuzzo (Indisparte)
 */
public class PotholeRepository {
    private static PotholeRepository instance;


    public static PotholeRepository getInstance() {
        if (instance == null) {
            instance = new PotholeRepository();
        }
        return instance;
    }

    public LiveData<List<Pothole>> getPotholes() {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public LiveData<List<Pothole>> getFilteredPotholes(@NonNull Filter filter) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public LiveData<Double> getThreshold() {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public void insertPothole(Pothole pothole) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

}
