package ie.ul.fitbook.ui.profile.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

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

    /**
     * This hook is called whenever an item in your options menu is selected.
     * The default implementation simply returns false to have the normal
     * processing happen (calling the item's Runnable or sending a message to
     * its Handler as appropriate).  You can use this method for any items
     * for which you would like to do processing without those other
     * facilities.
     *
     * <p>Derived classes should call through to the base class for it to
     * perform the default menu handling.</p>
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to
     * proceed, true to consume it here.
     * @see #onCreateOptionsMenu
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}