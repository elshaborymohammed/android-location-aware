package com.smart.tracking;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.location.LocationManagerCompat;

public class ManagerTrackingService extends Service {

    public static final String TAG = ManagerTrackingService.class.getCanonicalName();
    private static long time = 0;

    private final IBinder mBinder = new ManagerTrackingService.LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "in onBind()");
//        stopForeground(true);
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.i(TAG, "in onRebind()");
//        stopForeground(true);
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "Last client unbound from service");
//        startForeground(1, buildNotification(this));
        stopSelf();
        return true; // Ensures onRebind() is called when a client re-binds.
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        criteria.setAltitudeRequired(true);
        criteria.setCostAllowed(true);
        criteria.setBearingRequired(true);
        criteria.setSpeedRequired(false);
        //API level 9 and up
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);

        Log.d(TAG, "onCreate() called");
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        LocationManagerCompat.isLocationEnabled(locationManager);
        for (String provider : locationManager.getAllProviders()) {
            System.out.println("ManagerTrackingService.onCreate: " + provider);
        }
        locationManager.getBestProvider(criteria, true);
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                100, 0,
                location -> Log.d(TAG, "onLocationChanged() called with: location = [" + location + "]")

        );

        startForeground(1, buildNotification(this));
    }

    private Notification buildNotification(Context context) {
        //Build Channel For Notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "tracking";
            String channelDescription = "we can track your location";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("tracker", channelName, importance);
            channel.setDescription(channelDescription);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "tracker")
                .setContentTitle("ManagerTrackingService")
                .setContentText("Tracking is on...")
                .setOngoing(true)
                .setChannelId("tracker")
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.ic_launcher);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1, builder.build());
        return builder.build();
    }

    /**
     * Class used for the client Binder.  Since this service runs in the same process as its
     * clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        ManagerTrackingService getService() {
            return ManagerTrackingService.this;
        }
    }

    /**
     * Returns true if this is a foreground service.
     *
     * @param context The {@link Context}.
     */
    public boolean serviceIsRunningInForeground(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(
                Integer.MAX_VALUE)) {
            if (getClass().getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("ManagerTrackingService.onDestroy");
    }
}
