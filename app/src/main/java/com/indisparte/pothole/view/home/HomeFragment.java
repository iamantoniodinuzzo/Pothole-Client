package com.indisparte.pothole.view.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.indisparte.pothole.databinding.FragmentHomeBinding;
import com.indisparte.pothole.service.PotholeRecognizerService;

/**
 * @author Antonio Di Nuzzo (Indisparte)
 */
public class HomeFragment extends Fragment {
    private static final String TAG = HomeFragment.class.getSimpleName();
    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private Intent potholeService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        potholeService = new Intent(getContext(), PotholeRecognizerService.class);


        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.toggleButton.addOnButtonCheckedListener((toggleButton, checkedId, isChecked)->{
            //Respond to button selection
            final MaterialButton recording = binding.recordingBtn;
            if (isChecked) {
                startRecordingSession();
                recording.setText("Recording...");
            }else {
                stopRecordingSession();
                recording.setText("Start recording");
            }
        });
    }

    private void startRecordingSession() {
        requireContext().startService(potholeService);
        Log.d(TAG, "Start recording session!");
        Toast.makeText(requireContext(), "Start recording session!", Toast.LENGTH_SHORT).show();
    }

    private void stopRecordingSession(){
        requireContext().stopService(potholeService);
        Log.d(TAG, "Stop recording session!");
        Toast.makeText(requireContext(), "Stop recording session!", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        stopRecordingSession();
    }
}