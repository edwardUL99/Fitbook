package ie.ul.fitbook.ui.recording;

import androidx.appcompat.app.AppCompatActivity;

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
}