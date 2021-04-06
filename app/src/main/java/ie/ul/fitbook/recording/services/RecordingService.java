package ie.ul.fitbook.recording.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import ie.ul.fitbook.R;
import ie.ul.fitbook.sports.Sport;

/**
 * This service provides the recording functionality which is intended to be run in the foreground.
 */
public class RecordingService extends Service {
    /**
     * The binder to return in onBind
     */
    private final LocationServiceBinder binder = new LocationServiceBinder();
    /**
     * The id of the notification icon
     */
    private int iconId;
    /**
     * The sport as a Utils.capitalised String this service is recording
     */
    private String sport;
    /**
     * The list of locations being recorded
     */
    private ArrayList<Location> locations;
    /**
     * The time used to record how long the service was running for. This is needed so that we can "pause" it.
     * Use this variable to set any activity time from
     */
    private long stopWatchTime;
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
     * A flag to say that the service has been resumed after a pause.
     */
    private boolean resumed;
    /**
     * The distance being added up
     */
    private float distance;
    /**
     * The current speed the user is moving at
     */
    private float speed;
    /**
     * The total speed being recorded
     */
    private float totalSpeed;
    /**
     * The last location to calculate distance against
     */
    private Location lastLocation;
    /**
     * An ArrayList of receivers capable of receiving the RecordedLocation objects.
     */
    private ArrayList<RecordedLocationReceiver> recordedLocationReceivers;
    /**
     * Pass a String in as an extra of the sport passed through Utils.capitalise(sport.toString()).
     * If this is not passed in, "Recording an Activity" is used in the notifications
     */
    public static final String SPORT_STRING = "ie.ul.fitbook.SPORT_STRING";

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
    private static final float MINIMUM_SPEED_REQUIRED = 0.5f;
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
        locations = new ArrayList<>();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        recordedLocationReceivers = new ArrayList<>();
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    if (location.getAccuracy() <= MAXIMUM_ACCURACY) {
                        speed = location.getSpeed();
                        speed *= MPS_TO_KMH;
                        LatLng latLng;

                        if (speed >= MINIMUM_SPEED_REQUIRED) {
                            totalSpeed += speed;
                            locations.add(location);
                            distance += lastLocation.distanceTo(location); // add the distance from our last location onto this one
                            lastLocation = location; // this location now becomes the next "step" to add distance from
                            latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        } else {
                            latLng = null;
                        }

                        RecordedLocation recordedLocation =
                                new RecordedLocation(distance, speed, getAverageSpeed(), 0.0f, latLng);
                        for (RecordedLocationReceiver receiver : recordedLocationReceivers) {
                            receiver.receive(recordedLocation);
                        }
                    }
                }
            }
        };
    }

    /**
     * Determine the sport type and sport type from the given intent
     * @param intent the intent to retrieve sport from
     */
    private void getSportNotificationType(Intent intent) {
        if (sport == null && intent != null) {
            if (intent.hasExtra(SPORT_STRING)) {
                String sportString = intent.getStringExtra(SPORT_STRING);
                Sport sportEnum = Sport.convertToSport(sportString);

                switch (sportEnum) {
                    case CYCLING:
                        iconId = R.drawable.ic_recording_notification_bike;
                        break;
                    case RUNNING:
                        iconId = R.drawable.ic_recording_notification_run;
                        break;
                    case WALKING:
                        iconId = R.drawable.ic_recording_notification_walk;
                        break;
                }

                sport = "a " + sportString + " activity";
            } else {
                iconId = R.drawable.ic_recording_notification_bike;
                sport = "an Activity";
            }
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
     * network calls, or heavy disk I/O,
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
        getSportNotificationType(intent);
        startForeground(SERVICE_CODE, getNotification());
        return START_STICKY;
    }

    /**
     * Start location updates
     */
    private void startLocationUpdates() {
        if (running && !resumed)
            return;

        resumed = false;
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(success -> {
                        if (success != null) {
                            locations.add(0, success);
                            lastLocation = success;
                            startLocationUpdates();
                            stopWatchTime = SystemClock.elapsedRealtime();
                        }
                    });
        } else {
            throw new IllegalStateException("Necessary permissions are not granted");
        }

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

            String content = "Recording " + sport;

            Notification.Builder builder = new Notification.Builder(getApplicationContext(), CHANNEL_ID)
                    .setAutoCancel(true)
                    .setSmallIcon(iconId)
                    .setContentText(content);
            return builder.build();
        } else {
            Notification.Builder builder = new Notification.Builder(getApplicationContext())
                    .setAutoCancel(true);
            return builder.build();
        }
    }

    /**
     * Get the time in milliseconds the time to base the activity duration from
     * @return time to base service duration from
     */
    public long getRunningTime() {
        return stopWatchTime == 0 ? SystemClock.elapsedRealtime():stopWatchTime;
    }

    /**
     * Retrieve the distance currently totalled by this recording service
     * @return the distance currently achieved in metres
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
     * Retrieve the average speed calculated
     * @return average speed in km/h
     */
    public float getAverageSpeed() {
        return totalSpeed / (float)locations.size();
    }

    /**
     * Retrieve the list of locations recorded by this service
     * @return list of locations recorded
     */
    public ArrayList<Location> getLocations() {
        return locations;
    }

    /**
     * Adds a receiver to receive recorded locations
     * @param recordedLocationReceiver the receiver to receive the RecordedLocation receiver
     */
    public void addRecordedLocationReceiver(RecordedLocationReceiver recordedLocationReceiver) {
        recordedLocationReceivers.add(recordedLocationReceiver);
    }

    /**
     * Removes a receiver from this service
     * @param recordedLocationReceiver the receiver to remove
     */
    public void removeRecordedLocationReceiver(RecordedLocationReceiver recordedLocationReceiver) {
        recordedLocationReceivers.remove(recordedLocationReceiver);
    }

    /**
     * Pause this service so that it doesn't record more locations until it is resumed
     */
    public void pause() {
        stopLocationUpdates();
        stopWatchTime = SystemClock.elapsedRealtime();
    }

    /**
     * Resume this service so it records more locations
     */
    public void resume() {
        resumed = true;
        startLocationUpdates();
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