package com.indisparte.pothole.service;

import static com.indisparte.pothole.util.Constant.ACTION_BROADCAST;
import static com.indisparte.pothole.util.Constant.ACTION_START_LOCATION_SERVICE;
import static com.indisparte.pothole.util.Constant.ACTION_STOP_LOCATION_SERVICE;
import static com.indisparte.pothole.util.Constant.EXTRA_LOCATION;
import static com.indisparte.pothole.util.Constant.FASTEST_INTERVAL_MILLIS;
import static com.indisparte.pothole.util.Constant.LOCATION_INTERVAL_MILLIS;
import static com.indisparte.pothole.util.Constant.LOCATION_SERVICE_ID;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.indisparte.pothole.R;

/**
 * Allows the user's location to be tracked with an interval of {@link com.indisparte.pothole.util.Constant#LOCATION_INTERVAL_MILLIS}.
 * The data transmitted by this service can be accessed from {@link com.indisparte.pothole.view.MapsFragment}
 *
 * @author Antonio Di Nuzzo (Indisparte)
 */
public class LocationTrackingService extends Service {
    public static final String CHANNEL_ID = "location_notification_channel";
    private static final String TAG = LocationServices.class.getSimpleName();

    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            Location location = locationResult.getLastLocation();
            if (location != null) {
                Log.d(TAG, "onLocationResult: latitude " + location.getLatitude() + ", longitude" + location.getLongitude());
                onNewLocation(location);
            }
        }

    };

    private void onNewLocation(Location lastLocation) {
        // Notify anyone listening for broadcasts about the new location.
        Intent intent = new Intent(ACTION_BROADCAST);
        intent.putExtra(EXTRA_LOCATION, lastLocation);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            final String action = intent.getAction();
            if (action != null) {
                if (action.equals(ACTION_START_LOCATION_SERVICE)) {
                    Log.d(TAG, "onStartCommand: start location service");
                    startLocationService();
                } else if (action.equals(ACTION_STOP_LOCATION_SERVICE)) {
                    Log.d(TAG, "onStartCommand: stop location service");
                    stopLocationService();
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @SuppressLint("MissingPermission")
    private void startLocationService() {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = buildNotification();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null && notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(
                        CHANNEL_ID,
                        "Location tracking service",
                        NotificationManager.IMPORTANCE_HIGH
                );
                notificationChannel.setDescription("This channel is used by location service");
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        LocationRequest locationRequest = new LocationRequest()
                .setInterval(LOCATION_INTERVAL_MILLIS)//4 seconds
                .setFastestInterval(FASTEST_INTERVAL_MILLIS)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.getFusedLocationProviderClient(this)
                .requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        startForeground(LOCATION_SERVICE_ID, builder.build());
    }


    private void stopLocationService() {
        LocationServices.getFusedLocationProviderClient(this)
                .removeLocationUpdates(locationCallback);
        stopForeground(true);
        stopSelf();
    }

    private NotificationCompat.Builder buildNotification() {

        Intent resultIntent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        return new NotificationCompat.Builder(
                getApplicationContext(),
                CHANNEL_ID
        ).setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(),
                        R.mipmap.ic_launcher))
                .setContentTitle("Location tracking service")
                .setContentText("Running")
                .setContentIntent(pendingIntent)
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_MAX);
    }
}

