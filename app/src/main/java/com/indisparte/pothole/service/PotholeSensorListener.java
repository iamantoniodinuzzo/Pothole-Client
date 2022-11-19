package com.indisparte.pothole.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.indisparte.pothole.PotholeApplication;

/**
 * @author Antonio Di Nuzzo (Indisparte)
 */
public class PotholeSensorListener extends Service implements SensorEventListener {
    private static PotholeSensorListener instance;
    private SensorManager sensorManager;
    private Sensor sensor;
    private static final double ACCELERATION_THRESHOLD = 25.000;//TODO should be customizable
    private final String POTHOLE_SERVICE_NAME = PotholeSensorListener.class.getName();
    private float previous_z_acceleration;
    private boolean initialized;

    private PotholeSensorListener() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static PotholeSensorListener getInstance() {
        if (instance == null)
            instance = new PotholeSensorListener();
        return instance;
    }

    public void start() {
        try {
            sensorManager = (SensorManager) PotholeApplication.appContext.getSystemService(Context.SENSOR_SERVICE);
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

            if (sensor != null) {
                sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
                Log.d(PotholeApplication.LOG_TAG, "Acceleration sensor initialized");
            } else {
                Log.d(PotholeApplication.LOG_TAG, "Acceleration sensor not found");
                Toast.makeText(PotholeApplication.appContext, "Acceleration sensor not found", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e(PotholeApplication.LOG_TAG, "PotholeSensorListener start: Exception->" + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            if (sensor != null && sensorManager != null) {
                sensorManager.unregisterListener(instance);
                sensorManager = null;
                sensor = null;
            }
        } catch (Exception e) {
            Log.e(PotholeApplication.LOG_TAG, "PotholeSensorListener stop: Exception-> " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float current_z_value = event.values[2];

        if (!initialized) {
            previous_z_acceleration = current_z_value;
            initialized = true;
        }
        float deltaZ = Math.abs(previous_z_acceleration - current_z_value);

        if (deltaZ > ACCELERATION_THRESHOLD) {
            // TODO Pothole found, build a pothole instance
            Log.d(PotholeApplication.LOG_TAG, "onSensorChanged: deltaZ-> " + deltaZ);
            Toast.makeText(PotholeApplication.appContext, "Pothole found!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
