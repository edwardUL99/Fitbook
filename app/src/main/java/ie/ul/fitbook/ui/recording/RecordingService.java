package ie.ul.fitbook.ui.recording;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Chronometer;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Locale;

public class RecordingService extends Service {
    /**
     * The binder to return in onBind
     */
    private final LocationServiceBinder binder = new LocationServiceBinder();
    /**
     * The list of locations being recorded
     */
    private ArrayList<Location> locations;
    /**
     * The chronometer counting our duration
     */
    private Chronometer chronometer;
    /**
     * Our location client
     */
    private FusedLocationProviderClient fusedLocationClient;
    /**
     * The callback for location results
     */
    private LocationCallback locationCallback;
    /**
     * A flag to track whether the service is running or not
     */
    private boolean running;
    /**
     * The distance being added up
     */
    private float distance;
    /**
     * The current speed the user is moving at
     */
    private float speed;
    /**
     * The last location to calculate distance against
     */
    private Location lastLocation;

    /**
     * Our location request object for requesting location updates
     */
    private static final LocationRequest LOCATION_REQUEST = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(700);
    /**
     * The code for starting the service in the foreground
     */
    private static final int SERVICE_CODE = 12345678;
    /**
     * The ID of the channel
     */
    private static final String CHANNEL_ID = "channel_01";
    /**
     * The notification channel name
     */
    private static final String CHANNEL_NAME = "Fitbook Recording";
    /**
     * The minimum speed required to add distance
     */
    private static final float MINIMUM_SPEED_REQUIRED = 1.0f;
    /**
     * The maximum accuracy allowed
     */
    private static final int MAXIMUM_ACCURACY = 20;
    /**
     * The factor to multiply m/s by to convert it to km/h
     */
    private static final float MPS_TO_KMH = 3.6f;

    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     */
    @Override
    public void onCreate() {
        startForeground(SERVICE_CODE, getNotification());
        chronometer = new Chronometer(this);
        locations = new ArrayList<>();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    if (location.getAccuracy() <= MAXIMUM_ACCURACY) {
                        locations.add(location);
                        speed = location.getSpeed();
                        speed *= MPS_TO_KMH;

                        if (speed >= MINIMUM_SPEED_REQUIRED) {
                            distance += lastLocation.distanceTo(location); // add the distance from our last location onto this one
                            lastLocation = location; // this location now becomes the next "step" to add distance from
                        }
                    }
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(success -> {
                        if (success != null) {
                            locations.add(0, success);
                            lastLocation = success;
                            startLocationUpdates();
                            chronometer.start();
                        }
                    });
        } else {
            throw new IllegalStateException("Necessary permissions are not granted");
        }
    }

    /**
     * Called by the system every time a client explicitly starts the service by calling
     * {@link Context#startService}, providing the arguments it supplied and a
     * unique integer token representing the start request.  Do not call this method directly.
     *
     * <p>For backwards compatibility, the default implementation calls
     * {@link #onStart} and returns either {@link #START_STICKY}
     * or {@link #START_STICKY_COMPATIBILITY}.
     *
     * <p class="caution">Note that the system calls this on your
     * service's main thread.  A service's main thread is the same
     * thread where UI operations take place for Activities running in the
     * same process.  You should always avoid stalling the main
     * thread's event loop.  When doing long-running operations,
     * network calls, or heavy disk I/O, you should kick off a new
     * thread, or use {@link AsyncTask}.</p>
     *
     * @param intent  The Intent supplied to {@link Context#startService},
     *                as given.  This may be null if the service is being restarted after
     *                its process has gone away, and it had previously returned anything
     *                except {@link #START_STICKY_COMPATIBILITY}.
     * @param flags   Additional data about this start request.
     * @param startId A unique integer representing this specific request to
     *                start.  Use with {@link #stopSelfResult(int)}.
     * @return The return value indicates what semantics the system should
     * use for the service's current started state.  It may be one of the
     * constants associated with the {@link #START_CONTINUATION_MASK} bits.
     * @see #stopSelfResult(int)
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    /**
     * Start location updates
     */
    private void startLocationUpdates() {
        if (running)
            stopLocationUpdates();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(LOCATION_REQUEST, locationCallback, Looper.getMainLooper());
            running = true;
        } else {
            throw new IllegalStateException("Necessary permissions are not granted");
        }
    }

    /**
     * Stop all location updates
     */
    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
        running = false;
    }

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed.  The
     * service should clean up any resources it holds (threads, registered
     * receivers, etc) at this point.  Upon return, there will be no more calls
     * in to this Service object and it is effectively dead.  Do not call this method directly.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }

    /**
     * Handles binding to the service
     * @param intent the intent bound to it
     * @return the binder object
     */
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    /**
     * Retrieves the notification to display in the status bar when the service is running
     * @return the notification to display in the status bar
     */
    private Notification getNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            Notification.Builder builder = new Notification.Builder(getApplicationContext(), CHANNEL_ID)
                    .setAutoCancel(true);
            return builder.build();
        } else {
            Notification.Builder builder = new Notification.Builder(getApplicationContext())
                    .setAutoCancel(true);
            return builder.build();
        }
    }

    /**
     * Retrieve the distance currently totalled by this recording service
     * @return the distance currently achieved
     */
    public float getDistance() {
        return distance;
    }

    /**
     * Retrieve the current speed the service has recorded at the time of this call
     * @return the speed recorded at the time of this call
     */
    public float getSpeed() {
        return speed;
    }

    /**
     * Provides a binder for this recording service
     */
    public class LocationServiceBinder extends Binder {
        /**
         * Retrieve the service behind this binder
         *
         * @return the service for the binder
         */
        public RecordingService getService() {
            return RecordingService.this;
        }
    }
}