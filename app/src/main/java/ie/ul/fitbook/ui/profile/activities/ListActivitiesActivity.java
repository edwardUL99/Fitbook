package ie.ul.fitbook.ui.profile.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.List;
import java.util.Map;

import ie.ul.fitbook.R;
import ie.ul.fitbook.ui.custom.LoadingBar;
import ie.ul.fitbook.database.ActivitiesDatabase;
import ie.ul.fitbook.login.Login;
import ie.ul.fitbook.profile.Profile;
import ie.ul.fitbook.recording.RecordedActivity;
import ie.ul.fitbook.utils.ProfileUtils;

/**
 * This activity lists activities of a specified user.
 * A user ID needs to be provided either with the USER_ID_EXTRA or the currently logged in user.
 * If the user ID is null, an IllegalStateException is thrown by {@link #onCreate(Bundle)}.
 */
public class ListActivitiesActivity extends AppCompatActivity {
    /**
     * Use this extra to pass in the user id of the user to display users of. If this is not passed in,
     * the uid is assumed to be that of the currently logged in user.
     */
    public static final String USER_ID_EXTRA = "ie.ul.fitbook.ACTIVITIES_USER_ID";

    /**
     * The layout holding our recycler view
     */
    private SwipeRefreshLayout swipeRefreshLayout;
    /**
     * The progress bar to display on loading
     */
    private LoadingBar progressBar;
    /**
     * The profile downloaded for this user
     */
    protected Profile profile;
    /**
     * The userId for this activity
     */
    private String userId;
    /**
     * The activities adapter for displaying them
     */
    private ActivitiesAdapter activitiesAdapter;
    /**
     * The text view displaying that there's no activities
     */
    private TextView noActivitiesMessage;
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

        this.userId = userId;

        activitiesReference = new ActivitiesDatabase().getDatabase();

        noActivitiesMessage = findViewById(R.id.noActivitiesMessage);
        noActivitiesMessage.setText("No activities found");

        activitiesAdapter = new ActivitiesAdapter(null, this);

        RecyclerView recyclerView = findViewById(R.id.activitiesRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(activitiesAdapter);

        ConstraintLayout activitiesContainer = findViewById(R.id.activitiesContainer);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setLoadedLayout(activitiesContainer);

        swipeRefreshLayout = findViewById(R.id.activitiesRefresh);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            progressBar.show();
            if (profile == null) {
                loadProfile();
            } else {
                getActivities();
            }
            swipeRefreshLayout.setRefreshing(false);
        });

        progressBar.show();
        if (userId.equals(Login.getUserId())) {
            Profile profile = Login.getProfile();

            if (profile != null) {
                this.profile = profile;
                getActivities();
            } else {
                loadProfile();
            }
        } else {
            loadProfile();
        }
    }

    /**
     * Retrieve the activities for this user
     */
    private void getActivities() {
        activitiesAdapter.clear();
        activitiesReference
                .whereEqualTo(RecordedActivity.USER_ID_KEY, userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(success -> {
                    List<DocumentSnapshot> snapshots = success.getResult().getDocuments();

                    if (snapshots.size() == 0) {
                        noActivitiesMessage.setVisibility(View.VISIBLE);
                    } else {
                        noActivitiesMessage.setVisibility(View.GONE);
                        for (DocumentSnapshot snapshot : success.getResult().getDocuments()) {
                            Map<String, Object> data = snapshot.getData();

                            if (data != null) {
                                RecordedActivity activity = RecordedActivity.from(data);

                                if (activity != null) {
                                    activity.setFirestoreId(snapshot.getId());
                                    activitiesAdapter.addActivity(activity);
                                }
                            }
                        }
                    }

                    progressBar.hide();
                })
                .addOnFailureListener(this::onLoadFailed);
    }

    /**
     * Handles profile load
     * @param profile the profile to load
     */
    private void onProfileLoad(Profile profile) {
        this.profile = profile;
        getActivities();
    }

    /**
     * Load the profile into this activity
     */
    private void loadProfile() {
        ProfileUtils.downloadProfile(userId, this::onProfileLoad, () -> onLoadFailed(null),
                null, false, this, false);
    }

    /**
     * Handle load of activities failure
     * @param e an exception if any
     */
    private void onLoadFailed(Exception e) {
        if (e != null)
            e.printStackTrace();

        Toast.makeText(this, "An error occurred retrieving activities", Toast.LENGTH_SHORT)
                .show();
        progressBar.hideBoth();
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