package com.indisparte.pothole.view.viewModel;

import androidx.lifecycle.ViewModel;

import com.indisparte.pothole.data.network.PotholeRepository;
import com.indisparte.pothole.util.UserPreferenceManager;

import javax.inject.Inject;

/**
 * @author Antonio Di Nuzzo (Indisparte)
 */
public class LoginViewModel extends ViewModel {
    private static final String TAG = LoginViewModel.class.getSimpleName();
    private final String username = UserPreferenceManager.getUserName();
    @Inject
    protected PotholeRepository mPotholeRepository;


    private void login() {
        // TODO: 27/12/2022 asynctask
    }
}
