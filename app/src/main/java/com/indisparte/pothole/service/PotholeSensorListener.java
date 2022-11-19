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
    public static final int MIN_SEC_FOR_ANOTHER_EVENT_REGISTRATION = 2;
    private static final double ACCELERATION_THRESHOLD = 25.000;//TODO should be customizable
    private static final double GRAVITY = 9.18;
    private static final String POTHOLE_SERVICE_NAME = PotholeSensorListener.class.getName();
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gravity;
    private double x_accel, y_accel, z_accel, x_gravity, y_gravity, z_gravity;
    private long lastEvent = System.currentTimeMillis();

    private PotholeSensorListener() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        try {
            Log.d(POTHOLE_SERVICE_NAME, "Service started!");

            initSensors();

            registerSensor(
                    accelerometer,
                    "Acceleration sensor initialized",
                    "Acceleration sensor not found"
            );

            registerSensor(
                    gravity,
                    "Gravity sensor initialized",
                    "Gravity sensor not found"
            );

        } catch (Exception e) {
            Log.e(POTHOLE_SERVICE_NAME, "PotholeSensorListener start: Exception->" + e.getMessage());
            e.printStackTrace();
        }

        return START_STICKY;
    }

    private void registerSensor(Sensor sensor, String success_message, String error_message) {
        if (sensor != null) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(POTHOLE_SERVICE_NAME, success_message);
        } else {
            Log.d(POTHOLE_SERVICE_NAME, error_message);
            Toast.makeText(PotholeApplication.appContext, error_message, Toast.LENGTH_SHORT).show();
        }
    }

    private void initSensors() {
        sensorManager = (SensorManager) PotholeApplication.appContext.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterSensor();
    }


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopForeground(true);
        unregisterSensor();
        stopSelf();
    }

    private void unregisterSensor() {
        Log.d(POTHOLE_SERVICE_NAME, "Service stopped!");
        sensorManager.unregisterListener(this, accelerometer);
        sensorManager.unregisterListener(this, gravity);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;

        getAxisAcceleration(event, sensor);
        getAxisGravity(event, sensor);

        double deltaZ = Math.abs(getVerticalAccel());

        if (deltaZ > ACCELERATION_THRESHOLD) {
            // If not Access fine location permission, return

            /*
            This block checks if at least MIN_SEC_FOR_ANOTHER_EVENT_REGISTRATION
             seconds have passed since the last event
             */
            long newEvent = System.currentTimeMillis();
            long secondPassed = (newEvent - lastEvent) / 1000;
            if (secondPassed < MIN_SEC_FOR_ANOTHER_EVENT_REGISTRATION)
                return;

            lastEvent = newEvent;

            //TODO build pothole object

            //TODO send pothole to server

            Log.d(PotholeApplication.LOG_TAG, "onSensorChanged: deltaZ-> " + deltaZ);
            Toast.makeText(PotholeApplication.appContext, "Pothole found!", Toast.LENGTH_SHORT).show();
        }
    }

    private double getVerticalAccel() {
        return (x_accel * x_gravity / GRAVITY) +
                (y_accel * y_gravity / GRAVITY) +
                (z_accel * z_gravity / GRAVITY);
    }

    private void getAxisGravity(SensorEvent event, Sensor sensor) {
        if (sensor.getType() == Sensor.TYPE_GRAVITY) {
            x_gravity = (double) event.values[0];
            y_gravity = (double) event.values[1];
            z_gravity = (double) event.values[2];
        }
    }

    private void getAxisAcceleration(SensorEvent event, Sensor sensor) {
        if (sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            x_accel = (double) event.values[0];
            y_accel = (double) event.values[1];
            z_accel = (double) event.values[2];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
