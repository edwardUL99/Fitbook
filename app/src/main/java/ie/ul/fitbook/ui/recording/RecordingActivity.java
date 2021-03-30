package ie.ul.fitbook.ui.recording;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import ie.ul.fitbook.R;
import ie.ul.fitbook.sports.Sport;
import ie.ul.fitbook.utils.Utils;

/**
 * This activity handles the recording of an activity and viewing the current speed etc.
 * Choose the sport to record by passing in the value with ACTIVITY_TO_RECORD
 */
public class RecordingActivity extends AppCompatActivity {
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
        stopButton = findViewById(R.id.stop);
        stopButton.setOnClickListener(view -> stopRecording());

        setupChosenActivity();
        serviceIntent = new Intent(this, RecordingService.class);

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

            }
        };

        Application application = getApplication();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            application.startForegroundService(serviceIntent);
        } else {
            application.startService(serviceIntent);
        }

        application.bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
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
     * Stops recording
     */
    private void stopRecording() {
        Application application = getApplication();
        application.stopService(serviceIntent);
        application.unbindService(serviceConnection);

        Toast.makeText(this, "Distance travelled: " + recordingService.getDistance() + "m", Toast.LENGTH_SHORT).show();
    }
}