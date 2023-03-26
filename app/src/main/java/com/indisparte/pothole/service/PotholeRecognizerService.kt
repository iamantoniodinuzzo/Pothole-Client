package com.indisparte.pothole.service

import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.indisparte.pothole.di.component.PotholeApplication
import com.indisparte.pothole.service.PotholeRecognizerService
import com.indisparte.pothole.util.Constant

/**
 * @author Antonio Di Nuzzo (Indisparte)
 */
class PotholeRecognizerService : Service(), SensorEventListener {
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var gravity: Sensor? = null
    private var x_accel = 0.0
    private var y_accel = 0.0
    private var z_accel = 0.0
    private var x_gravity = 0.0
    private var y_gravity = 0.0
    private var z_gravity = 0.0
    private var lastEvent = System.currentTimeMillis()
    private var mThreshold = 0.0
    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        mThreshold = intent.getDoubleExtra("threshold", Constant.DEFAULT_ACCELERATION_THRESHOLD)
        Log.d(TAG, "onStartCommand: received threshold ($mThreshold)")
        val action = intent.action
        if (action != null) {
            if (action == Constant.ACTION_START_POTHOLE_SERVICE) {
                Log.d(TAG, "onStartCommand: start pothole service")
                startPotholeRecogniserService()
            } else if (action == Constant.ACTION_STOP_POTHOLE_SERVICE) {
                Log.d(TAG, "onStartCommand: stop location service")
                stopPotholeRecognizerService()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun registerSensor(sensor: Sensor?, success_message: String, error_message: String) {
        if (sensor != null) {
            sensorManager!!.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d(TAG, success_message)
        } else {
            Log.e(TAG, error_message)
            Toast.makeText(PotholeApplication.getContext(), error_message, Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun initSensors() {
        Log.d(TAG, "initSensors: Initializing sensors")
        sensorManager =
            PotholeApplication.getContext().getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager!!.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        gravity = sensorManager!!.getDefaultSensor(Sensor.TYPE_GRAVITY)
    }

    private fun startPotholeRecogniserService() {
        try {
            Log.d(TAG, "Service started!")
            initSensors()
            registerSensor(
                accelerometer,
                "Acceleration sensor initialized",
                "Acceleration sensor not found"
            )
            registerSensor(
                gravity,
                "Gravity sensor initialized",
                "Gravity sensor not found"
            )
        } catch (e: Exception) {
            Log.e(TAG, "PotholeSensorListener start: Exception->" + e.message)
            e.printStackTrace()
        }
    }

    private fun stopPotholeRecognizerService() {
        unregisterSensor()
        stopForeground(true)
        stopSelf()
        Log.d(TAG, "stopPotholeRecognizerService: service stopped")
    }

    private fun unregisterSensor() {
        Log.d(TAG, "Service stopped!")
        sensorManager!!.unregisterListener(this, accelerometer)
        sensorManager!!.unregisterListener(this, gravity)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val sensor = event.sensor
        getAxisAcceleration(event, sensor)
        getAxisGravity(event, sensor)
        val deltaZ = Math.abs(verticalAccel)
        if (deltaZ > mThreshold) {

            /*
            This block checks if at least MIN_SEC_FOR_ANOTHER_EVENT_REGISTRATION
             seconds have passed since the last event
             */
            val newEvent = System.currentTimeMillis()
            val secondPassed = (newEvent - lastEvent) / 1000
            if (secondPassed < MIN_SEC_FOR_ANOTHER_EVENT_REGISTRATION) return
            lastEvent = newEvent
            Log.d(TAG, "onSensorChanged: found pothole with deltaZ = $deltaZ")
            onPotholeFound(deltaZ)
        }
    }

    private fun onPotholeFound(deltaZ: Double) {
        //Notify anyone listening for broadcasts about the new Pothole
        val intent = Intent(Constant.ACTION_BROADCAST)
        intent.putExtra(Constant.EXTRA_DELTA_Z, deltaZ)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }

    private val verticalAccel: Double
        get() = x_accel * x_gravity / GRAVITY +
                y_accel * y_gravity / GRAVITY +
                z_accel * z_gravity / GRAVITY

    private fun getAxisGravity(event: SensorEvent, sensor: Sensor) {
        if (sensor.type == Sensor.TYPE_GRAVITY) {
            x_gravity = event.values[0].toDouble()
            y_gravity = event.values[1].toDouble()
            z_gravity = event.values[2].toDouble()
        }
    }

    private fun getAxisAcceleration(event: SensorEvent, sensor: Sensor) {
        if (sensor.type == Sensor.TYPE_LINEAR_ACCELERATION) {
            x_accel = event.values[0].toDouble()
            y_accel = event.values[1].toDouble()
            z_accel = event.values[2].toDouble()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    companion object {
        private const val MIN_SEC_FOR_ANOTHER_EVENT_REGISTRATION = 2
        private const val GRAVITY = 9.18
        private val TAG = PotholeRecognizerService::class.java.simpleName
    }
}