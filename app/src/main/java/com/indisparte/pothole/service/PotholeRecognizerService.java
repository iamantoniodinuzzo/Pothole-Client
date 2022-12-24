package com.indisparte.pothole.service;

import static com.indisparte.pothole.util.Constant.ACTION_BROADCAST;
import static com.indisparte.pothole.util.Constant.EXTRA_DELTA_Z;

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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.indisparte.pothole.di.component.PotholeApplication;
import com.indisparte.pothole.util.Constant;

/**
 * @author Antonio Di Nuzzo (Indisparte)
 */
public class PotholeRecognizerService extends Service implements SensorEventListener {
    private static final int MIN_SEC_FOR_ANOTHER_EVENT_REGISTRATION = 2;
    private static final double GRAVITY = 9.18;
    private static final String TAG = PotholeRecognizerService.class.getSimpleName();
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gravity;
    private double x_accel, y_accel, z_accel, x_gravity, y_gravity, z_gravity;
    private long lastEvent = System.currentTimeMillis();

    public PotholeRecognizerService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent!=null){
            final String action = intent.getAction();
            if (action!=null){
                if (action.equals(Constant.ACTION_START_POTHOLE_SERVICE)){
                    Log.d(TAG, "onStartCommand: start pothole service");
                    startPotholeRecogniserService();
                }else if (action.equals(Constant.ACTION_STOP_POTHOLE_SERVICE)){
                    Log.d(TAG, "onStartCommand: stop location service");
                    stopPotholeRecognizerService();
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void registerSensor(Sensor sensor, String success_message, String error_message) {
        if (sensor != null) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, success_message);
        } else {
            Log.e(TAG, error_message);
            Toast.makeText(PotholeApplication.getContext(), error_message, Toast.LENGTH_SHORT).show();
        }
    }

    private void initSensors() {
        Log.d(TAG, "initSensors: Initializing sensors");
        sensorManager = (SensorManager) PotholeApplication.getContext().getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
    }

    private void startPotholeRecogniserService(){
        try {
            Log.d(TAG, "Service started!");

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
            Log.e(TAG, "PotholeSensorListener start: Exception->" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void stopPotholeRecognizerService(){
        unregisterSensor();
        stopForeground(true);
        stopSelf();
        Log.d(TAG, "stopPotholeRecognizerService: service stopped");
    }

   /* @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterSensor();
    }*/


  /*  @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopForeground(true);
        unregisterSensor();
        stopSelf();
    }*/

    private void unregisterSensor() {
        Log.d(TAG, "Service stopped!");
        sensorManager.unregisterListener(this, accelerometer);
        sensorManager.unregisterListener(this, gravity);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;

        getAxisAcceleration(event, sensor);
        getAxisGravity(event, sensor);

        double deltaZ = Math.abs(getVerticalAccel());

        if (deltaZ > Constant.DEFAULT_ACCELERATION_THRESHOLD) {

            /*
            This block checks if at least MIN_SEC_FOR_ANOTHER_EVENT_REGISTRATION
             seconds have passed since the last event
             */
            long newEvent = System.currentTimeMillis();
            long secondPassed = (newEvent - lastEvent) / 1000;
            if (secondPassed < MIN_SEC_FOR_ANOTHER_EVENT_REGISTRATION)
                return;

            lastEvent = newEvent;

            Log.d(TAG, "onSensorChanged: found pothole with deltaZ = " + deltaZ);

            onPotholeFound(deltaZ);
        }
    }

    private void onPotholeFound(double deltaZ){
        //Notify anyone listening for broadcasts about the new Pothole
        Intent intent = new Intent(ACTION_BROADCAST);
        intent.putExtra(EXTRA_DELTA_Z,deltaZ);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
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
