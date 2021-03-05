package ie.ul.fitbook.ui.profile.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.firestore.CollectionReference;

import ie.ul.fitbook.R;
import ie.ul.fitbook.database.UserDatabase;
import ie.ul.fitbook.login.Login;

/**
 * This activity lists activities of a specified user.
 * A user ID needs to be provided either with the USER_ID_EXTRA or the currently logged in user.
 * If the user ID is null, an IllegalStateException is thrown by {@link #onCreate(Bundle)}
 */
public class ListActivitiesActivity extends AppCompatActivity {
    /**
     * Use this extra to pass in the user id of the user to display users of. If this is not passed in,
     * the uid is assumed to be that of the currently logged in user.
     */
    public static final String USER_ID_EXTRA = "ie.ul.fitbook.ACTIVITIES_USER_ID";

    /**
     * The reference to a user's activities
     */
    private CollectionReference activitiesReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_activities);

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

        activitiesReference = new UserDatabase(userId)
                .getChildCollection("activities"); // TODO when activity class is created, replace this with Activity.COLLECTION_PATH constant
    }
}