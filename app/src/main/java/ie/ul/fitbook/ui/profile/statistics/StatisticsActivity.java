package ie.ul.fitbook.ui.profile.statistics;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import ie.ul.fitbook.R;
import ie.ul.fitbook.login.Login;

/**
 * This class displays statistics for a specified user
 */
public class StatisticsActivity extends AppCompatActivity {
    /**
     * Use this extra to pass in the user id of the user to display users of. If this is not passed in,
     * the uid is assumed to be that of the currently logged in user.
     */
    public static final String USER_ID_EXTRA = "ie.ul.fitbook.STATISTICS_USER_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Activities");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent received = getIntent();

        String userId;

        if (received.hasExtra(USER_ID_EXTRA)) {
            userId = received.getStringExtra(USER_ID_EXTRA);
        } else {
            userId = Login.getUserId();
        }

        if (userId == null)
            throw new IllegalStateException("No UserID has been passed to this Activity");

        // TODO do something with ID here, maybe retrieve stats file (or should have been retrieved on profile sync
    }
}