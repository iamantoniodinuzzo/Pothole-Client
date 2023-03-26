package com.indisparte.pothole.service

import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.indisparte.pothole.util.Constant

/**
 * @author Antonio Di Nuzzo (Indisparte)
 */
class PotholeRecognizerService : Service(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private lateinit var gravity: Sensor
    private var lastEventTime: Long = 0L
    private var threshold = 0.0
    private var xAccel = 0.0
    private var yAccel = 0.0
    private var zAccel = 0.0
    private var xGravity = 0.0
    private var yGravity = 0.0
    private var zGravity = 0.0

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        threshold = intent.getDoubleExtra("threshold", DEFAULT_ACCELERATION_THRESHOLD)
        Log.d(TAG, "onStartCommand: received threshold ($threshold)")

        when (intent.action) {
            Constant.ACTION_START_POTHOLE_SERVICE -> startPotholeRecognizerService()
            Constant.ACTION_STOP_POTHOLE_SERVICE -> stopPotholeRecognizerService()
        }

        return START_NOT_STICKY
    }

    private fun registerSensor(sensor: Sensor, successMessage: String) {
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        Log.d(TAG, successMessage)
    }

    private fun initSensors() {
        Log.d(TAG, "initSensors: Initializing sensors")
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
            ?: error("Acceleration sensor not found")
        gravity =
            sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) ?: error("Gravity sensor not found")
    }

    private fun startPotholeRecognizerService() {
        try {
            Log.d(TAG, "Service started!")
            initSensors()
            registerSensor(accelerometer, "Acceleration sensor initialized")
            registerSensor(gravity, "Gravity sensor initialized")
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
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_LINEAR_ACCELERATION -> getAxisAcceleration(event)
            Sensor.TYPE_GRAVITY -> getAxisGravity(event)
        }

        val deltaZ = calculateDeltaZ()
        if (deltaZ > threshold && isEventRecentEnough()) {
            lastEventTime = System.currentTimeMillis()
            Log.d(TAG, "onSensorChanged: found pothole with deltaZ = $deltaZ")
            onPotholeFound(deltaZ)
        }
    }

    private fun isEventRecentEnough(): Boolean {
        val timeSinceLastEvent = System.currentTimeMillis() - lastEventTime
        return timeSinceLastEvent > MIN_TIME_BETWEEN_EVENTS_MILLIS
    }

    private fun onPotholeFound(deltaZ: Double) {
        val intent = Intent(Constant.ACTION_BROADCAST)
        intent.putExtra(Constant.EXTRA_DELTA_Z, deltaZ)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }

    private fun calculateDeltaZ(): Double {
        val verticalAccel = xAccel * xGravity / GRAVITY +
                yAccel * yGravity / GRAVITY +
                zAccel * zGravity / GRAVITY
        return Math.abs(verticalAccel)
    }

    private fun getAxisGravity(event: SensorEvent) {
        xGravity = event.values[0].toDouble()
        yGravity = event.values[1].toDouble()
        zGravity = event.values[2].toDouble()
    }

    private fun getAxisAcceleration(event: SensorEvent) {
        xAccel = event.values[0].toDouble()
        yAccel = event.values[1].toDouble()
        zAccel = event.values[2].toDouble()

    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    companion object {
        private const val MIN_TIME_BETWEEN_EVENTS_MILLIS = 2 // seconds
        private const val GRAVITY = 9.18
        private const val DEFAULT_ACCELERATION_THRESHOLD = 2.00
        private val TAG = PotholeRecognizerService::class.java.simpleName
    }
}