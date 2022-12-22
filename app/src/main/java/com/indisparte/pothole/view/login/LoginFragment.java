package com.indisparte.pothole.view.login;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.indisparte.pothole.R;
import com.indisparte.pothole.data.network.Common;
import com.indisparte.pothole.data.network.PotholeRepository;
import com.indisparte.pothole.databinding.FragmentLoginBinding;
import com.indisparte.pothole.util.AlertUtil;
import com.indisparte.pothole.util.UserPreferenceManager;

import java.io.IOException;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * @author Antonio Di Nuzzo (Indisparte)
 */
@AndroidEntryPoint
public class LoginFragment extends Fragment {
    private static final String TAG = LoginFragment.class.getSimpleName();
    private FragmentLoginBinding binding;
    private String username;
    @Inject
    protected PotholeRepository mPotholeRepository;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connectToServer();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.loginBtn.setOnClickListener(login_btn -> {
            saveUsernamePref();
            login();

        });

        binding.usernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                username = s.toString().trim();
                Log.d(TAG, "afterTextChanged, username set to " + username);
                if (!TextUtils.isEmpty(username)) {
                    binding.loginBtn.setEnabled(true);
                }
            }
        });


    }

    private void connectToServer() {
        if (Common.isConnectedToInternet(requireContext())) {//check internet connection
            if (mPotholeRepository == null || !mPotholeRepository.isConnect()) {
                final ProgressDialog progressDialog = AlertUtil.createProgressDialog(requireContext(), getString(R.string.dialog_connection_server_msg));
                AsyncTask.execute(() -> {
                    try {
                        Log.d(TAG, "connectToServer: Try to connect to the server");
                        requireActivity().runOnUiThread(progressDialog::show);
                        mPotholeRepository.connect();
                        Log.d(TAG, "connectToTheServer: Connected to server");
                        checkIfUserAlreadyHaveUsername();
                        requireActivity().runOnUiThread(progressDialog::hide);
                    } catch (IOException e) {
                        Log.e(TAG, "connectToTheServer: Error in server connection:" + e.getMessage());
                        requireActivity().runOnUiThread(progressDialog::hide);
                        mPotholeRepository = null;
                        requireActivity().runOnUiThread(this::connectionServerErrorDialog);
                    }

                });
            }
        } else
            Log.e(TAG, "connectToServer: No internet connection");
    }


    private void checkIfUserAlreadyHaveUsername() {
        // check if user have the username saved in the preferences, if not, user need to insert one
        username = UserPreferenceManager.getUserName();
        if (username != null) {
            Log.d(TAG, "checkIfUserAlreadyHaveUsername: User already have an username, so login");
            // login to the server
            login();
        } else
            Log.d(TAG, "checkIfUserAlreadyHaveUsername: User don't have a username set");
    }

    private void connectionServerErrorDialog() {
        Log.e(TAG, "connectionServerErrorDialog: No connection with server");
        AlertUtil.createSimpleOkCancelDialog(
                requireContext(),
                getString(R.string.dialog_error_connection_server_msg),
                getString(R.string.dialog_error_connection_server_neg_btn),
                null,
                (dialogInterface, i) -> requireActivity().finish(),
                null
        ).show();

    }

    private void login() {
        if (mPotholeRepository != null) {
            if (username != null) {
                // ProgressDialog loginProgressDialog = AlertUtil.createProgressDialog(requireContext(), getString(R.string.dialog_progress_login_msg));
                AsyncTask.execute(() -> {
                    try {
                        //requireActivity().runOnUiThread(loginProgressDialog::show);
                        Log.d(TAG, "login: Try to login...");
                        mPotholeRepository.setUsername(username);
                        // requireActivity().runOnUiThread(loginProgressDialog::hide);
                        Log.d(TAG, "login: username send successfully");
                        requireActivity().runOnUiThread(() -> {
                            //clear edittext
                            binding.usernameEditText.setText("");
                            navigateHome();
                        });
                    } catch (IOException e) {
                        //requireActivity().runOnUiThread(loginProgressDialog::hide);
                        Log.e(TAG, "login: Connection server error: " + e.getMessage());
                        connectionServerErrorDialog();
                    }
                });


            } else {
                Log.d(TAG, "login: username is null");
            }
        } else
            Log.e(TAG, "login: repository is null");
    }

    private void navigateHome() {
        requireActivity()
                .runOnUiThread(() -> NavHostFragment
                        .findNavController(this)
                        .navigate(R.id.action_loginFragment_to_mapsFragment));
    }

    private void saveUsernamePref() {
        UserPreferenceManager.saveUsername(username);
        Log.d(TAG, "saveUsernamePref: username (" + username + ") saved");
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}