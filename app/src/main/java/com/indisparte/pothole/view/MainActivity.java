package com.indisparte.pothole.view;


import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.indisparte.pothole.databinding.ActivityMainBinding;

/**
 * @author Antonio Di Nuzzo (Indisparte)
 */
public class MainActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();

    private ActivityMainBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

    }



}