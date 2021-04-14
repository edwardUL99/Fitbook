package ie.ul.fitbook.ui.recording;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.threeten.bp.Duration;

import java.util.ArrayList;
import java.util.Locale;

import ie.ul.fitbook.login.Login;
import ie.ul.fitbook.profile.Profile;
import ie.ul.fitbook.recording.CalorieCalculator;
import ie.ul.fitbook.recording.RecordedActivity;
import ie.ul.fitbook.recording.RecordingUtils;
import ie.ul.fitbook.recording.services.RecordedLocation;
import ie.ul.fitbook.recording.services.RecordedLocationReceiver;
import ie.ul.fitbook.recording.services.RecordingService;
import ie.ul.fitbook.ui.HomeActivity;
import ie.ul.fitbook.utils.ProfileUtils;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import ie.ul.fitbook.R;
import ie.ul.fitbook.sports.Sport;
import ie.ul.fitbook.utils.Utils;

/**
 * This activity handles the recording of an activity and viewing the current speed etc.
 * Choose the sport to record by passing in the value with ACTIVITY_TO_RECORD
 */
public class RecordingActivity extends AppCompatActivity implements RecordedLocationReceiver {
    /**
     * The TextView for displaying the activity
     */
    private TextView activityView;
    /**
     * The TextView for displaying the current distance
     */
    private TextView distanceView;
    /**
     * The TextView for displaying the current speed
     */
    private TextView speedView;
    /**
     * The TextView for displaying the current average speed
     */
    private TextView avgSpeedView;
    /**
     * The TextView for displaying the current time duration
     */
    private Chronometer timeView;
    /**
     * The button to stop/pause the activity
     */
    private Button stopButton;
    /**
     * The button to resume after activity is paused
     */
    private Button resumeButton;
    /**
     * True if paused, false if not
     */
    private boolean paused;
    /**
     * A flag to determine if the service has been registered or not
     */
    private boolean serviceRegistered;
    /**
     * The intent used to start the service
     */
    private Intent serviceIntent;
    /**
     * The service connection used to connect to our service
     */
    ServiceConnection serviceConnection;
    /**
     * The service being used to record the activity
     */
    private RecordingService recordingService;

    /**
     * The extra to decide what activity to record. If this isn't passed in, it defaults to the first value in
     * the Sports enum
     */
    public static final String ACTIVITY_TO_RECORD = "ie.ul.fitbook.ACTIVITY_RECORD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording);

        activityView = findViewById(R.id.activityView);
        distanceView = findViewById(R.id.distanceView);
        speedView = findViewById(R.id.speedView);
        avgSpeedView = findViewById(R.id.avgSpeedView);
        timeView = findViewById(R.id.timeView);
        timeView.setOnChronometerTickListener((timeView) -> {
            long millis = SystemClock.elapsedRealtime() - timeView.getBase();
            Duration duration = Duration.ofMillis(millis);
            timeView.setText(Utils.durationToHoursMinutesSeconds(duration));
        });
        stopButton = findViewById(R.id.stop);
        stopButton.setOnClickListener(view -> stopRecording());
        resumeButton = findViewById(R.id.resume);
        resumeButton.setOnClickListener(view -> resumeRecording());
        resumeButton.setVisibility(View.GONE);

        setupChosenActivity();
        serviceIntent = new Intent(this, RecordingService.class);
        serviceIntent.putExtra(RecordingService.SPORT_STRING, activityView.getText());

        serviceConnection = new ServiceConnection() {
            /**
             * Called when a connection to the Service has been established, with
             * the {@link IBinder} of the communication channel to the
             * Service.
             *
             * <p class="note"><b>Note:</b> If the system has started to bind your
             * client app to a service, it's possible that your app will never receive
             * this callback. Your app won't receive a callback if there's an issue with
             * the service, such as the service crashing while being created.
             *
             * @param name    The concrete component name of the service that has
             *                been connected.
             * @param service The IBinder of the Service's communication channel,
             */
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                RecordingService.LocationServiceBinder binder = (RecordingService.LocationServiceBinder) service;
                recordingService = binder.getService();
                recordingService.addRecordedLocationReceiver(RecordingActivity.this);
                timeView.setBase(recordingService.getRunningTime());
                timeView.start();
            }

            /**
             * Called when a connection to the Service has been lost.  This typically
             * happens when the process hosting the service has crashed or been killed.
             * This does <em>not</em> remove the ServiceConnection itself -- this
             * binding to the service will remain active, and you will receive a call
             * to {@link #onServiceConnected} when the Service is next running.
             *
             * @param name The concrete component name of the service whose
             *             connection has been lost.
             */
            @Override
            public void onServiceDisconnected(ComponentName name) {
                recordingService.removeRecordedLocationReceiver(RecordingActivity.this);
            }
        };

        Application application = getApplication();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            application.startForegroundService(serviceIntent);
        } else {
            application.startService(serviceIntent);
        }

        application.bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);

        serviceRegistered = true;
        setupChosenActivity();
    }

    /**
     * Sets up the chosen activity
     */
    private void setupChosenActivity() {
        String activity = Utils.capitalise(Sport.values()[0].toString());
        Intent intent = getIntent();

        if (intent.hasExtra(ACTIVITY_TO_RECORD)) {
            activity = Utils.capitalise(intent.getStringExtra(ACTIVITY_TO_RECORD));
        }

        activityView.setText(activity);
    }

    /**
     * Retrieve the duration from the time view
     * @return the parsed duration
     */
    private Duration getDurationFromTimeView() {
        String text = timeView.getText().toString();
        String[] values = text.split(":");

        int hours = Integer.parseInt(values[0]);
        int minutes = Integer.parseInt(values[1]);
        int seconds = Integer.parseInt(values[2]);

        return Utils.hoursMinutesSecondsToDuration(hours, minutes, seconds);
    }

    /**
     * This method handles the creation of the recording object
     * @param recordingService the service that recorded the object
     * @param elevationGain the elevation gain in metres
     * @param profile the profile that will be saving this activity
     */
    private void createRecordedActivity(RecordingService recordingService, double elevationGain, Profile profile) {
        ArrayList<LatLng> latLngs = new ArrayList<>();
        for (Location location : recordingService.getLocations()) {
            latLngs.add(new LatLng(location.getLatitude(), location.getLongitude()));
        }

        Duration duration = getDurationFromTimeView();
        Sport sport = Sport.convertToSport(activityView.getText().toString());

        int calories = CalorieCalculator.calculateCalories(profile, duration, sport);

        RecordedActivity recordedActivity = new RecordedActivity(latLngs, duration,
                sport, recordingService.getDistance() / 1000,
                recordingService.getAverageSpeed(), (float)elevationGain, calories);

        Intent intent = new Intent(this, SaveRecordingActivity.class);
        intent.putExtra(SaveRecordingActivity.RECORDED_ACTIVITY, recordedActivity);
        intent.putExtra(HomeActivity.FRAGMENT_ID, getIntent().getIntExtra(HomeActivity.FRAGMENT_ID, 0));
        startActivity(intent);
        finish();
    }

    /**
     * Save the activity. Elevation gain should be retrieved from RecordingUtils.calculateElevationGain
     * @param recordingService the recording service that recorded the activity
     * @param elevationGain the elevation gain recorded in this activity
     */
    private void saveActivity(RecordingService recordingService, double elevationGain) {
        if (elevationGain == RecordingUtils.ELEVATION_GAIN_UNAVAILABLE) {
            Toast.makeText(this, "Failed to retrieve elevation gain information", Toast.LENGTH_SHORT)
                    .show();
            elevationGain = 0.0;
        }
        Profile profile = Login.getProfile();

        final double finalElevation = elevationGain;
        if (profile == null) {
            ProfileUtils.downloadProfile(Login.getUserId(), downloaded -> {
                createRecordedActivity(recordingService, finalElevation, downloaded);
                Login.setProfile(downloaded);
            }, () -> {
                Toast.makeText(this, "An error occurred, please try again", Toast.LENGTH_SHORT).show();
                stopButton.setEnabled(true);
                resumeButton.setVisibility(View.VISIBLE);
                paused = true;
            }, null, this, true); // download profile image synchronously so only save when fully complete
        } else {
            createRecordedActivity(recordingService, finalElevation, profile);
        }
    }

    /**
     * Stops recording
     */
    private void stopRecording() {
        if (paused) {
            stopButton.setEnabled(false);
            if (serviceRegistered) {
                Application application = getApplication();
                application.stopService(serviceIntent);
                application.unbindService(serviceConnection);
                recordingService.stopForeground(true);
                recordingService.stopSelf();
                serviceRegistered = false;
            }
            timeView.stop();

            Toast.makeText(this, "Retrieving elevation data, please wait", Toast.LENGTH_SHORT)
                    .show();
            resumeButton.setVisibility(View.GONE);
            stopButton.setEnabled(true);
            RecordingUtils.calculateElevationGain(recordingService, gain -> saveActivity(recordingService, gain));
        } else {
            stopButton.setText("Stop");
            paused = true;
            recordingService.pause();
            timeView.stop();
            resumeButton.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Handles the resuming of recording
     */
    private void resumeRecording() {
        if (paused) {
            recordingService.resume();
            long timeWhenStopped = timeView.getBase() - recordingService.getRunningTime();
            timeView.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
            timeView.start();
            paused = false;
            stopButton.setText("Pause");
            resumeButton.setVisibility(View.GONE);
        }
    }

    /**
     * Called when the activity has detected the user's press of the back
     * key. The {@link #getOnBackPressedDispatcher() OnBackPressedDispatcher} will be given a
     * chance to handle the back button before the default behavior of
     * {@link Activity#onBackPressed()} is invoked.
     *
     * @see #getOnBackPressedDispatcher()
     */
    @Override
    public void onBackPressed() {
        Toast.makeText(this, "To exit, press Pause > Stop", Toast.LENGTH_SHORT)
                .show();
    }

    /**
     * This method is used to "receive" a RecordedLocation from the RecordingService
     *
     * @param recordedLocation the recorded location
     */
    @Override
    public void receive(RecordedLocation recordedLocation) {
        float distance = recordedLocation.getDistance();
        float speed = recordedLocation.getSpeed();
        float averageSpeed = recordedLocation.getAverageSpeed();

        distanceView.setText(String.format(Locale.getDefault(), "%,.02f", distance / 1000));
        speedView.setText(String.format(Locale.getDefault(), "%,.01f", speed));
        avgSpeedView.setText(String.format(Locale.getDefault(), "%,.01f", averageSpeed));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (recordingService != null)
            recordingService.addRecordedLocationReceiver(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        recordingService.removeRecordedLocationReceiver(this);
    }
}