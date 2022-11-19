package com.indisparte.pothole.view;


import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.indisparte.pothole.databinding.ActivityMainBinding;
import com.indisparte.pothole.util.PotholeSensorListener;

/**
 * @author Antonio Di Nuzzo (Indisparte)
 */
public class MainActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();

    private ActivityMainBinding binding;
    private final PotholeSensorListener potholeSensorListener = PotholeSensorListener.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

    }


    @Override
    protected void onResume() {
        super.onResume();
        potholeSensorListener.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        potholeSensorListener.stop();
    }
}