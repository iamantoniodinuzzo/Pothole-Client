package com.indisparte.pothole.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.indisparte.pothole.R
import com.indisparte.pothole.util.Constant
import com.indisparte.pothole.view.MainActivity

/**
 * Allows the user's location to be tracked with an interval of [com.indisparte.pothole.util.Constant.LOCATION_INTERVAL_MILLIS].
 * The data transmitted by this service can be accessed from [com.indisparte.pothole.view.MapsFragment]
 *
 * @author Antonio Di Nuzzo (Indisparte)
 */
class LocationTrackingService : Service() {
    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            val location = locationResult.lastLocation
            if (location != null) {
                Log.d(
                    TAG,
                    "onLocationResult: latitude " + location.latitude + ", longitude" + location.longitude
                )
                onNewLocation(location)
            }
        }
    }

    private fun onNewLocation(lastLocation: Location) {
        // Notify anyone listening for broadcasts about the new location.
        val intent = Intent(Constant.ACTION_BROADCAST)
        intent.putExtra(Constant.EXTRA_LOCATION, lastLocation)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val action = intent.action
        if (action != null) {
            if (action == Constant.ACTION_START_LOCATION_SERVICE) {
                Log.d(TAG, "onStartCommand: start location service")
                startLocationService()
            } else if (action == Constant.ACTION_STOP_LOCATION_SERVICE) {
                Log.d(TAG, "onStartCommand: stop location service")
                stopLocationService()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    @SuppressLint("MissingPermission")
    private fun startLocationService() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Check if the notification channel is already created or not
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationManager.getNotificationChannel(
                CHANNEL_ID
            ) == null
        ) {
            // Create the notification channel if not already created
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                "Location tracking service",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.description = "This channel is used by location service"
            notificationManager.createNotificationChannel(notificationChannel)
        }

        // Build the notification
        val builder = buildNotification()

        // Create a location request
        val locationRequest = LocationRequest.create().apply {
            interval = Constant.LOCATION_INTERVAL_MILLIS.toLong() //4 seconds
            fastestInterval = Constant.FASTEST_INTERVAL_MILLIS.toLong()
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        // Request location updates
        LocationServices.getFusedLocationProviderClient(this)
            .requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

        // Start the foreground service with the notification
        startForeground(Constant.LOCATION_SERVICE_ID, builder.build())
    }


    private fun stopLocationService() {
        LocationServices.getFusedLocationProviderClient(this)
            .removeLocationUpdates(locationCallback)
        stopForeground(true)
        stopSelf()
    }

    @SuppressLint("NewApi")
    private fun buildNotification(): NotificationCompat.Builder {
        val resultIntent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationChannelId = "LocationTrackingService"
        val notificationChannelName = "Location tracking service"
        val notificationChannel = NotificationChannel(
            notificationChannelId,
            notificationChannelName,
            NotificationManager.IMPORTANCE_HIGH
        )

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)

        val notificationBuilder = NotificationCompat.Builder(
            applicationContext,
            notificationChannelId
        )

        val notificationIcon = R.drawable.ic_placeholder

        notificationBuilder.setSmallIcon(notificationIcon)
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    resources,
                    R.mipmap.ic_launcher
                )
            )
            .setContentTitle(getString(R.string.location_tracking_service_title))
            .setContentText(getString(R.string.location_tracking_service_text))
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)

        return notificationBuilder
    }


    companion object {
        const val CHANNEL_ID = "location_notification_channel"
        private val TAG = LocationServices::class.java.simpleName
    }
}