package ie.ul.fitbook.ui.profile;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import ie.ul.fitbook.R;
import ie.ul.fitbook.custom.TraceableScrollView;
import ie.ul.fitbook.login.Login;
import ie.ul.fitbook.profile.Profile;
import ie.ul.fitbook.ui.profile.goals.GoalsActivity;
import ie.ul.fitbook.ui.profile.activities.ListActivitiesActivity;
import ie.ul.fitbook.ui.profile.posts.ProfilePostsActivity;
import ie.ul.fitbook.ui.profile.statistics.StatisticsActivity;
import ie.ul.fitbook.utils.ProfileUtils;

/**
 * This activity is used to view another profile that is not your own one
 */
public class ViewProfileActivity extends AppCompatActivity {
    /**
     * Use this extra to pass in the user id of the user to display users of. If this is not passed in,
     * the uid is assumed to be that of the currently logged in user.
     */
    public static final String USER_ID_EXTRA = "ie.ul.fitbook.PROFILE_VIEW_USER_ID";

    /**
     * A boolean variable to determine if the profile fragment is loading for whatever reason,
     * most likely because the Login.isProfileOutOfSync() returned true
     */
    private boolean loading;
    /**
     * The progress bar for saying the profile is loading
     */
    private ProgressBar loadingBar;
    /**
     * The container containing profile items
     */
    private TraceableScrollView profileContainer;
    /**
     * The swipe refresh layout allowing us to refresh the profile
     */
    private SwipeRefreshLayout swipeRefreshLayout;
    /**
     * The image view for displaying the profile
     */
    private ImageView profileImage;
    /**
     * The TextView for displaying the user's name
     */
    private TextView nameView;
    /**
     * The TextView for displaying the user's address
     */
    private TextView addressView;
    /**
     * The TextView for displaying the user's favourite activity
     */
    private TextView favouriteActivityView;
    /**
     * The button for adding friends
     */
    private Button friendsButton;
    /**
     * The TextView for displaying number of friends a user has
     */
    private TextView friendsView;
    /**
     * The userId this profile view is for
     */
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Profile");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent received = getIntent();

        if (received.hasExtra(USER_ID_EXTRA)) {
            userId = received.getStringExtra(USER_ID_EXTRA);
        } else {
            userId = Login.getUserId();
        }

        if (userId == null)
            throw new IllegalStateException("No UserID has been passed to this Activity");

        setupActivity();
    }

    /**
     * This method sets up and initialises the activity
     */
    private void setupActivity() {
        loadingBar = findViewById(R.id.loadingBar);
        profileContainer = findViewById(R.id.profileContainer);

        swipeRefreshLayout = findViewById(R.id.profileRefresh);
        profileContainer.setOnScrollDetected(this::onScrollDetected);
        profileContainer.setOnScrollFinished(this::onScrollReleaseDetected);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            setLoading(true);
            refreshProfile();
        });

        profileImage = findViewById(R.id.profilePicture);
        nameView = findViewById(R.id.name);
        addressView = findViewById(R.id.address);
        favouriteActivityView = findViewById(R.id.favActivityView);
        friendsButton = findViewById(R.id.friendsButton); // TODO need to display add friend or remove friend if this is a different user
        friendsView = findViewById(R.id.friends);

        setupProfileOptions();
        refreshProfile();
    }

    /**
     * Refreshes the profile for this activity
     */
    private void refreshProfile() {
        ProfileUtils.downloadProfile(userId, this::onProfileRefresh, this::onProfileRefreshFail);
    }

    /**
     * This method is called when the profile is refreshed
     * @param profile the profile that has been downloaded by ProfileUtils.downloadProfile
     */
    private void onProfileRefresh(Profile profile) {
        setLoading(false);

        if (profile == null)
            throw new IllegalStateException("A profile cannot be null on successful profile refresh");

        profileImage.setImageBitmap(profile.getProfileImage());
        nameView.setText(profile.getName());
        String address = profile.getCity() + ", " + profile.getState();
        addressView.setText(address);
        favouriteActivityView.setText(profile.getFavouriteSport());
        // TODO calculate number of friends here and set the text view and refresh the add friends button to the approriate value

        swipeRefreshLayout.setRefreshing(false);
    }

    /**
     * This method is called when profile refresh fails
     */
    private void onProfileRefreshFail() {
        setLoading(false);
        hideProfile();
        Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT)
                .show();

        swipeRefreshLayout.setRefreshing(false);
    }

    /**
     * Sets up all the options included in profile_options_layout
     */
    private void setupProfileOptions() {
        ConstraintLayout goalLayout = findViewById(R.id.goalsLayout);
        goalLayout.setOnClickListener(v -> {
            Intent intent = new Intent(this, GoalsActivity.class);
            intent.putExtra(GoalsActivity.USER_ID_EXTRA, userId);
            startActivity(intent);
        });

        ConstraintLayout activitiesLayout = findViewById(R.id.activitiesLayout);
        activitiesLayout.setOnClickListener(v -> {
            Intent intent = new Intent(this, ListActivitiesActivity.class);
            intent.putExtra(ListActivitiesActivity.USER_ID_EXTRA, userId);
            startActivity(intent);
        });

        ConstraintLayout statisticsLayout = findViewById(R.id.statisticsLayout);
        statisticsLayout.setOnClickListener(v -> {
            Intent intent = new Intent(this, StatisticsActivity.class);
            intent.putExtra(StatisticsActivity.USER_ID_EXTRA, userId);
            startActivity(intent);
        });

        ConstraintLayout postsLayout = findViewById(R.id.postsLayout);
        postsLayout.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfilePostsActivity.class);
            intent.putExtra(ProfilePostsActivity.USER_ID_EXTRA, userId);
            startActivity(intent);
        });
    }

    /**
     * Handle when a scroll is detected
     */
    private void onScrollDetected() {
        swipeRefreshLayout.setEnabled(false); // don't allow refreshes if we are scrolling
    }

    /**
     * Handle when the scroll release is done
     */
    private void onScrollReleaseDetected() {
        if (profileContainer.getScrollY() == 0)
            swipeRefreshLayout.setEnabled(true); // we are back to scroll 0 so allow refreshes again
    }

    /**
     * Sets this fragment to either a loading state or a non-loading state
     * @param loading true if loading, false if not
     */
    private void setLoading(boolean loading) {
        int containerVisibility = loading ? View.GONE:View.VISIBLE;
        int loadingVisibility = loading ? View.VISIBLE:View.GONE;

        profileContainer.setVisibility(containerVisibility);
        loadingBar.setVisibility(loadingVisibility);

        this.loading = loading;
    }

    /**
     * This method is called to hide the profile if it wasn't loaded correctly
     */
    private void hideProfile() {
        profileContainer.setVisibility(View.GONE);
        loadingBar.setVisibility(View.GONE);
    }
}