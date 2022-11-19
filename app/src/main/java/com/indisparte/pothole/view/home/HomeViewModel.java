package com.indisparte.pothole.view.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.indisparte.pothole.model.Filter;
import com.indisparte.pothole.model.Pothole;
import com.indisparte.pothole.repository.PotholeRepository;

import java.util.List;

/**
 * @author Antonio Di Nuzzo (Indisparte)
 */
public class HomeViewModel extends ViewModel {
    private PotholeRepository repository;

    // Expose screen UI state
    private MutableLiveData<List<Pothole>> potholes, filteredPotholes;
    private MutableLiveData<Boolean> isServiceRunning;

    public HomeViewModel() {
        repository = PotholeRepository.getInstance();
    }

    public LiveData<List<Pothole>> getPotholes() {
        if (potholes == null) {
            potholes = new MutableLiveData<>();
            getAllPotholes();
        }
        return potholes;
    }

    public LiveData<List<Pothole>> getFilteredPotholes(@NonNull Filter filter) {
        if (filteredPotholes == null) {
            filteredPotholes = new MutableLiveData<>();
            getPotholesByFilter(filter);
        }
        return filteredPotholes;
    }


    // Handle business logic
    private void getAllPotholes() {
        // Do an asynchronous operation to fetch all potholes.

    }

    // Handle business logic
    private void getPotholesByFilter(Filter filter) {
        // Do an asynchronous operation to fetch potholes by filter.
    }
}
