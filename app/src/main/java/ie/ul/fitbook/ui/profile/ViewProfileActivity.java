package ie.ul.fitbook.ui.profile;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Locale;
import java.util.Map;

import ie.ul.fitbook.R;
import ie.ul.fitbook.custom.LoadingBar;
import ie.ul.fitbook.custom.TraceableScrollView;
import ie.ul.fitbook.login.Login;
import ie.ul.fitbook.profile.Profile;
import ie.ul.fitbook.sports.Sport;
import ie.ul.fitbook.statistics.WeeklyStat;
import ie.ul.fitbook.statistics.WeeklyStatistics;
import ie.ul.fitbook.ui.profile.goals.GoalsActivity;
import ie.ul.fitbook.ui.profile.activities.ListActivitiesActivity;
import ie.ul.fitbook.ui.profile.posts.ProfilePostsActivity;
import ie.ul.fitbook.ui.profile.statistics.StatisticsActivity;
import ie.ul.fitbook.utils.ProfileUtils;
import ie.ul.fitbook.utils.Utils;

/**
 * This activity is used to view another profile that is not your own one.
 * To determine which profile to view, use the following extras:
 * <ol>
 *     <li>{@link #USER_ID_EXTRA} - Pass in a String uid here</li>
 *     <li>{@link #USER_PROFILE_EXTRA} - Pass in a Profile instance as a Parcelable. This is useful if
 *     you have already downloaded profiles for a search and don't want to download again.
 *     If this is passed in, {@link #USER_ID_EXTRA} is ignored</li>
 * </ol>
 */
public class ViewProfileActivity extends AppCompatActivity {
    /**
     * Use this extra to pass in the user id of the user to display users of. If this is not passed in,
     * the uid is assumed to be that of the currently logged in user.
     */
    public static final String USER_ID_EXTRA = "ie.ul.fitbook.PROFILE_VIEW_USER_ID";
    /**
     * Use this extra to pass in a Profile instance
     */
    public static final String USER_PROFILE_EXTRA = "ie.ul.fitbook.PROFILE_VIEW_USER";

    /**
     * The progress bar for saying the profile is loading
     */
    private LoadingBar loadingBar;
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
     * The layout holding our profile biography
     */
    private CardView profileBio;
    /**
     * The userId this profile view is for
     */
    private String userId;
    /**
     * The profile passed in
     */
    private Profile profile;

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

        if (received.hasExtra(USER_PROFILE_EXTRA)) {
            profile = received.getParcelableExtra(USER_PROFILE_EXTRA);

            if (profile == null)
                throw new IllegalStateException("USER_PROFILE_EXTRA used but null profile has been passed to this Activity");
        } else {
            if (received.hasExtra(USER_ID_EXTRA)) {
                userId = received.getStringExtra(USER_ID_EXTRA);
            } else {
                userId = Login.getUserId();
            }

            if (userId == null)
                throw new IllegalStateException("No UserID has been passed to this Activity");
        }

        setupActivity();
    }

    /**
     * This method sets up and initialises the activity
     */
    private void setupActivity() {
        loadingBar = findViewById(R.id.loadingBar);
        profileContainer = findViewById(R.id.profileContainer);
        loadingBar.setLoadedLayout(profileContainer);

        swipeRefreshLayout = findViewById(R.id.profileRefresh);
        profileContainer.setOnScrollDetected(this::onScrollDetected);
        profileContainer.setOnScrollFinished(this::onScrollReleaseDetected);

        profileBio = findViewById(R.id.biographyLayout);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadingBar.show();
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
     * Refreshes the weekly stats for this profile
     */
    private void refreshWeeklyStats() {
        TextView weekView = findViewById(R.id.weekView);
        weekView.setText(WeeklyStatistics.getWeek());

        String userId = Login.getUserId();
        DocumentReference cycleReference = WeeklyStatistics.getSportWeeklyStat(userId, Sport.CYCLING);
        DocumentReference runReference = WeeklyStatistics.getSportWeeklyStat(userId, Sport.RUNNING);
        DocumentReference walkReference = WeeklyStatistics.getSportWeeklyStat(userId, Sport.WALKING);

        setCycleStats(cycleReference);
        setRunStats(runReference);
        setWalkStats(walkReference);
    }

    /**
     * Sets the cycling stats
     * @param cycleReference the document reference for the cycle stats
     */
    private void setCycleStats(DocumentReference cycleReference) {
        cycleReference.get()
                .addOnCompleteListener(task -> {
                    DocumentSnapshot snapshot = task.getResult();
                    Map<String, Object> data;
                    if (snapshot != null && (data = snapshot.getData()) != null) {
                        WeeklyStat weeklyStat = WeeklyStat.from(data);
                        TextView distance = findViewById(R.id.cycleDistance);
                        distance.setText(String.format(Locale.getDefault(), "%,.01fkm", weeklyStat.getDistance()));
                        TextView time = findViewById(R.id.cycleTime);
                        time.setText(Utils.durationToHoursMinutes(weeklyStat.getTime()));
                        TextView elevation = findViewById(R.id.cycleElevation);
                        elevation.setText(weeklyStat.getElevation());
                    }
                })
                .addOnFailureListener(failure -> Toast.makeText(this, "Failed to load cycling statistics", Toast.LENGTH_SHORT).show());
    }

    /**
     * Sets the running stats
     * @param runReference the document reference for the run stats
     */
    private void setRunStats(DocumentReference runReference) {
        runReference.get()
                .addOnCompleteListener(task -> {
                    DocumentSnapshot snapshot = task.getResult();
                    Map<String, Object> data;
                    if (snapshot != null && (data = snapshot.getData()) != null) {
                        WeeklyStat weeklyStat = WeeklyStat.from(data);
                        TextView distance = findViewById(R.id.runDistance);
                        distance.setText(String.format(Locale.getDefault(), "%,.01fkm", weeklyStat.getDistance()));
                        TextView time = findViewById(R.id.runTime);
                        time.setText(Utils.durationToHoursMinutes(weeklyStat.getTime()));
                        TextView elevation = findViewById(R.id.runElevation);
                        elevation.setText(weeklyStat.getElevation());
                    }
                })
                .addOnFailureListener(failure -> Toast.makeText(this, "Failed to load running statistics", Toast.LENGTH_SHORT).show());
    }

    /**
     * Sets the walking stats
     * @param walkReference the document reference for the cycle stats
     */
    private void setWalkStats(DocumentReference walkReference) {
        walkReference.get()
                .addOnCompleteListener(task -> {
                    DocumentSnapshot snapshot = task.getResult();
                    Map<String, Object> data;
                    if (snapshot != null && (data = snapshot.getData()) != null) {
                        WeeklyStat weeklyStat = WeeklyStat.from(data);
                        TextView distance = findViewById(R.id.walkDistance);
                        distance.setText(String.format(Locale.getDefault(), "%,.01fkm", weeklyStat.getDistance()));
                        TextView time = findViewById(R.id.walkTime);
                        time.setText(Utils.durationToHoursMinutes(weeklyStat.getTime()));
                        TextView elevation = findViewById(R.id.walkElevation);
                        elevation.setText(weeklyStat.getElevation());
                    }
                })
                .addOnFailureListener(failure -> Toast.makeText(this, "Failed to load walking statistics", Toast.LENGTH_SHORT).show());
    }

    /**
     * Refreshes the profile for this activity
     */
    private void refreshProfile() {
        if (profile == null) {
            loadingBar.show();
            ProfileUtils.downloadProfile(userId, this::onProfileRefresh, this::onProfileRefreshFail);
        } else {
            onProfileRefresh(profile);
            profile = null; // subsequent loads should refresh the profile
        }

        refreshWeeklyStats();
    }

    /**
     * This method is called when the profile is refreshed
     * @param profile the profile that has been downloaded by ProfileUtils.downloadProfile
     */
    private void onProfileRefresh(Profile profile) {
        loadingBar.hide();

        if (profile == null)
            throw new IllegalStateException("A profile cannot be null on successful profile refresh");

        profileImage.setImageBitmap(profile.getProfileImage());
        nameView.setText(profile.getName());
        String address = profile.getCity() + ", " + profile.getState();
        addressView.setText(address);
        favouriteActivityView.setText(profile.getFavouriteSport());
        // TODO calculate number of friends here and set the text view and refresh the add friends button to the approriate value

        setupBioTextView(profile);

        swipeRefreshLayout.setRefreshing(false);
    }

    /**
     * Sets up the biography text view
     * @param profile profile which the bio belongs to
     */
    @SuppressLint("ClickableViewAccessibility")
    private void setupBioTextView(Profile profile) {
        String bio = profile.getBio();

        if (bio != null && !bio.isEmpty()) {
            profileBio.setVisibility(View.VISIBLE);
            TextView textView = profileBio.findViewById(R.id.biographyTextView);
            textView.setText(bio);

            textView.setMovementMethod(new ScrollingMovementMethod());

            textView.setOnTouchListener((v, event) -> {
                swipeRefreshLayout.setEnabled(false);
                profileContainer.disableScrolling();
                if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
                    profileContainer.enableScrolling();
                    swipeRefreshLayout.setEnabled(true);
                }

                return false;
            });
        } else {
            profileBio.setVisibility(View.GONE);
        }
    }

    /**
     * This method is called when profile refresh fails
     */
    private void onProfileRefreshFail() {
        loadingBar.hideBoth();
        Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT)
                .show();

        swipeRefreshLayout.setRefreshing(false);
    }

    /**
     * Sets up all the options included in profile_options_layout
     */
    private void setupProfileOptions() {
        String userId;

        if (profile == null) {
            userId = this.userId;

            if (userId == null)
                throw new IllegalStateException("ViewProfileActivity profile is null, but so is it's userId. One of either must not be null");
        } else {
            userId = profile.getUserId();

            if (userId == null)
                throw new IllegalStateException("ViewProfileActivity's profile#getUserId() returned null. Was setUserId called when the profile was retrieved?");
        }

        final String finalUserId = userId;
        ConstraintLayout goalLayout = findViewById(R.id.goalsLayout);
        goalLayout.setOnClickListener(v -> {
            Intent intent = new Intent(this, GoalsActivity.class);
            intent.putExtra(GoalsActivity.USER_ID_EXTRA, finalUserId);
            startActivity(intent);
        });

        ConstraintLayout activitiesLayout = findViewById(R.id.activitiesLayout);
        activitiesLayout.setOnClickListener(v -> {
            Intent intent = new Intent(this, ListActivitiesActivity.class);
            intent.putExtra(ListActivitiesActivity.USER_ID_EXTRA, finalUserId);
            startActivity(intent);
        });

        ConstraintLayout statisticsLayout = findViewById(R.id.statisticsLayout);
        statisticsLayout.setOnClickListener(v -> {
            Intent intent = new Intent(this, StatisticsActivity.class);
            intent.putExtra(StatisticsActivity.USER_ID_EXTRA, finalUserId);
            startActivity(intent);
        });

        ConstraintLayout postsLayout = findViewById(R.id.postsLayout);
        postsLayout.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfilePostsActivity.class);
            intent.putExtra(ProfilePostsActivity.USER_ID_EXTRA, finalUserId);
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
}