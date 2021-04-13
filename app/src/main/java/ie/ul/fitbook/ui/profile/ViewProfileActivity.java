package ie.ul.fitbook.ui.profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ie.ul.fitbook.R;
import ie.ul.fitbook.ui.custom.LoadingBar;
import ie.ul.fitbook.ui.custom.TraceableScrollView;
import ie.ul.fitbook.database.UserDatabase;
import ie.ul.fitbook.login.Login;
import ie.ul.fitbook.profile.Profile;
import ie.ul.fitbook.sports.Sport;
import ie.ul.fitbook.statistics.WeeklyStat;
import ie.ul.fitbook.statistics.WeeklyStatistics;
import ie.ul.fitbook.ui.MainActivity;
import ie.ul.fitbook.ui.home.ProfilesActivity;
import ie.ul.fitbook.ui.profile.cache.ProfileCache;
import ie.ul.fitbook.ui.profile.goals.GoalsActivity;
import ie.ul.fitbook.ui.profile.activities.ListActivitiesActivity;
import ie.ul.fitbook.ui.profile.posts.ProfilePostsActivity;
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
     * The profile option buttons to view different user data
     */
    private CardView profileOptions;
    /**
     * The userId this profile view is for
     */
    private String userId;
    /**
     * The profile passed in
     */
    private Profile profile;
    /**
     * Use this flag to determine if cache should be used
     */
    private boolean useCache;
    /**
     * The profile image to load when the activity is launched
     */
    private static Bitmap imageToLoad;

    /**
     * Sets the profile image of the user to load. This should be called along with the USER_PROFILE_EXTRA
     * since a profile image is too large to pass in a bundle
     * @param bitmap the bitmap to load
     */
    public static void setProfileImage(Bitmap bitmap) {
        imageToLoad = bitmap;
    }

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
            if (imageToLoad != null) {
                profile.setProfileImage(imageToLoad);
                imageToLoad = null;
            }

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
     * Retrieves the source appropriate for the cached values for this profile
     * @return the source to use in queries
     */
    private Source getCacheSource() {
        if (useCache) {
            if (ProfileCache.hasUserBeenCached(getUserId()))
                return Source.CACHE;
            else
                return Source.SERVER;
        } else {
            return Source.SERVER;
        }
    }

    /**
     * Retrieves the User ID represented by this profile activity
     * @return the user ID of the profile
     */
    private String getUserId() {
        return profile == null ? this.userId:profile.getUserId();
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
        profileOptions = findViewById(R.id.profileOptions);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            useCache = false;
            loadingBar.show();
            refreshProfile();
        });

        profileImage = findViewById(R.id.profilePicture);
        nameView = findViewById(R.id.name);
        addressView = findViewById(R.id.address);
        favouriteActivityView = findViewById(R.id.favActivityView);
        friendsButton = findViewById(R.id.friendsButton);
        friendsView = findViewById(R.id.friends);

        useCache = true;
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
        cycleReference.get(getCacheSource())
                .addOnCompleteListener(task -> {
                    try {
                        DocumentSnapshot snapshot = task.getResult();
                        Map<String, Object> data;
                        if (snapshot != null && (data = snapshot.getData()) != null) {
                            WeeklyStat weeklyStat = WeeklyStat.from(data);
                            TextView distance = findViewById(R.id.cycleDistance);
                            distance.setText(String.format(Locale.getDefault(), "%,.01fkm", weeklyStat.getDistance()));
                            TextView time = findViewById(R.id.cycleTime);
                            time.setText(Utils.durationToHoursMinutes(weeklyStat.getTime()));
                            TextView elevation = findViewById(R.id.cycleElevation);
                            elevation.setText(String.format(Locale.getDefault(), "%dm", weeklyStat.getElevation()));
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
    }

    /**
     * Sets the running stats
     * @param runReference the document reference for the run stats
     */
    private void setRunStats(DocumentReference runReference) {
        runReference.get(getCacheSource())
                .addOnCompleteListener(task -> {
                    try {
                        DocumentSnapshot snapshot = task.getResult();
                        Map<String, Object> data;
                        if (snapshot != null && (data = snapshot.getData()) != null) {
                            WeeklyStat weeklyStat = WeeklyStat.from(data);
                            TextView distance = findViewById(R.id.runDistance);
                            distance.setText(String.format(Locale.getDefault(), "%,.01fkm", weeklyStat.getDistance()));
                            TextView time = findViewById(R.id.runTime);
                            time.setText(Utils.durationToHoursMinutes(weeklyStat.getTime()));
                            TextView elevation = findViewById(R.id.runElevation);
                            elevation.setText(String.format(Locale.getDefault(), "%dm", weeklyStat.getElevation()));
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
    }

    /**
     * Sets the walking stats
     * @param walkReference the document reference for the cycle stats
     */
    private void setWalkStats(DocumentReference walkReference) {
        walkReference.get(getCacheSource())
                .addOnCompleteListener(task -> {
                    try {
                        DocumentSnapshot snapshot = task.getResult();
                        Map<String, Object> data;
                        if (snapshot != null && (data = snapshot.getData()) != null) {
                            WeeklyStat weeklyStat = WeeklyStat.from(data);
                            TextView distance = findViewById(R.id.walkDistance);
                            distance.setText(String.format(Locale.getDefault(), "%,.01fkm", weeklyStat.getDistance()));
                            TextView time = findViewById(R.id.walkTime);
                            time.setText(Utils.durationToHoursMinutes(weeklyStat.getTime()));
                            TextView elevation = findViewById(R.id.walkElevation);
                            elevation.setText(String.format(Locale.getDefault(), "%dm", weeklyStat.getElevation()));
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
    }

    /**
     * Refreshes the profile for this activity
     */
    private void refreshProfile() {
        if (profile == null) {
            Source source = getCacheSource();
            boolean useCache = source == Source.CACHE;
            if (!useCache)
                loadingBar.show();

            ProfileUtils.downloadProfile(userId, this::onProfileRefresh, this::onProfileRefreshFail,
                    profileImage, useCache, this);
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
        if (profile == null)
            throw new IllegalStateException("A profile cannot be null on successful profile refresh");

        profileImage.setImageBitmap(profile.getProfileImage());
        nameView.setText(profile.getName());
        String address = profile.getCity() + ", " + profile.getState();
        addressView.setText(address);
        favouriteActivityView.setText(profile.getFavouriteSport());

        setupBioTextView(profile);

        onFriendsSync(profile);
    }

    /**
     * Initialize the contents of the Activity's standard options menu.  You
     * should place your menu items in to <var>menu</var>.
     *
     * <p>This is only called once, the first time the options menu is
     * displayed.  To update the menu every time it is displayed, see
     * {@link #onPrepareOptionsMenu}.
     *
     * <p>The default implementation populates the menu with standard system
     * menu items.  These are placed in the {@link Menu#CATEGORY_SYSTEM} group so that
     * they will be correctly ordered with application-defined menu items.
     * Deriving classes should always call through to the base implementation.
     *
     * <p>You can safely hold on to <var>menu</var> (and any items created
     * from it), making modifications to it as desired, until the next
     * time onCreateOptionsMenu() is called.
     *
     * <p>When you add items to the menu, you can implement the Activity's
     * {@link #onOptionsItemSelected} method to handle them there.
     *
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed;
     * if you return false it will not be shown.
     * @see #onPrepareOptionsMenu
     * @see #onOptionsItemSelected
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (Login.getUserId().equals(getUserId())) {
            getMenuInflater().inflate(R.menu.action_bar_profile, menu);
            return true;
        }

        return false;
    }

    /**
     * This method is called when the edit profile button is pressed
     */
    private void onEditProfileClicked() {
        Intent intent = new Intent(this, ProfileCreationActivity.class);
        intent.putExtra(ProfileCreationActivity.EDIT_USER_PROFILE, true);
        intent.putExtra(ProfileCreationActivity.RETURN_TO_PREVIOUS, true); // we want to return to here, not HomeActivity on cancel or submit
        startActivity(intent);
    }

    /**
     * Handles the clicking of the number of friends to view a friends list
     */
    private void onFriendsClicked() {
        // TODO show friends list here
        Toast.makeText(this, "Friends list will display when done", Toast.LENGTH_SHORT)
                .show();
    }

    /**
     * This method is called when friends has been retrieved successfully and the user's friend status has been checked
     * @param profile the profile object representing the profile being displayed here
     */
    private void onFriendsSync(Profile profile) {
        String userId = profile.getUserId();


        String ownId = Login.getUserId();
        //boolean friends, ownProfile;


        UserDatabase userDb = new UserDatabase(userId);
        userDb.getChildCollection("friends")
                .whereEqualTo("id", ownId)
                .get()
                .addOnSuccessListener(query -> {

                    boolean pending = false;
                    boolean requested = false;
                    boolean exists = query.size() >0;


                    if(exists){

                        List<DocumentSnapshot> documents = query.getDocuments();
                        Map<String,Object> test = documents.get(0).getData();

                        if( test.containsKey("status")){
                            pending = test.get("status").equals("requested");

                        }
                        if( test.containsKey("status")){
                            requested = test.get("status").equals("pending");

                        }
                    }
                    boolean ownProfile = userId.equals(Login.getUserId());

                    if (exists && !ownProfile && !pending && !requested) {
                        friendsButton.setText("Remove Friend");
                        friendsButton.setOnClickListener(view -> removeFriend(userId, ownId));
                        profileOptions.setVisibility(View.VISIBLE);
                    } else if (ownProfile) {
                        friendsButton.setText("Add Friends");
                        friendsButton.setOnClickListener(view -> launchProfilesActivity());
                        profileOptions.setVisibility(View.VISIBLE);
                        friendsView.setOnClickListener(view -> onFriendsClicked());
                    } else if(exists && pending) {
                        friendsButton.setText("Accept");
                        friendsButton.setOnClickListener(view -> acceptFriend(userId, ownId));
                        profileOptions.setVisibility(View.GONE);
                    } else if(exists && requested) {
                        friendsButton.setText("Cancel Request");
                        friendsButton.setOnClickListener(view -> cancelRequest(userId, ownId));
                        profileOptions.setVisibility(View.GONE);
                    } else{
                        friendsButton.setText("Add Friend");
                        friendsButton.setOnClickListener(view -> addFriend(userId, ownId));
                        profileOptions.setVisibility(View.GONE);
                    }

                    loadingBar.hide();
                    swipeRefreshLayout.setRefreshing(false);

                    userDb.getDatabase()
                            .get()
                            .addOnSuccessListener(success -> {
                                Map<String, Object> data = success.getData();

                                if (data != null) {
                                    Long friendsNum = (Long)data.get("friends-count");
                                    friendsNum = friendsNum == null ? 0:friendsNum;
                                    String numberFriends = "" + friendsNum;
                                    friendsView.setText(numberFriends);
                                } else {
                                    String zero = "" + 0;
                                    friendsView.setText(zero);
                                }

                                ProfileCache.setUserCached(getUserId(), true);
                            })
                            .addOnFailureListener(fail -> onProfileRefreshFail());
                })
                .addOnFailureListener(fail -> onProfileRefreshFail());
    }

    /**
     * This method removes the profile being viewed by the current user as a friend
     */
    private void removeFriend(String userId, String ownId) {

        UserDatabase userDb = new UserDatabase(ownId);
        userDb.getChildCollection("friends").document(userId).delete();
        userDb = new UserDatabase(userId);
        userDb.getChildCollection("friends").document(ownId).delete();

    }

    private void cancelRequest(String userId, String ownId) {
        UserDatabase userDb = new UserDatabase(ownId);
        userDb.getChildCollection("friends").document(userId).delete();
        userDb = new UserDatabase(userId);
        userDb.getChildCollection("friends").document(ownId).delete();
    }

    /**
     * Adds the profile being viewed as a friend
     */
    private void addFriend(String userId, String ownId) {

        Map<String, Object> requested = new HashMap<>();
        requested.put("id", userId);
        requested.put("status", "requested");

        UserDatabase userDb = new UserDatabase(ownId);
        userDb.getChildCollection("friends").document(userId).set(requested).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
                Toast.makeText(ViewProfileActivity.this, "Adding friend failed!", Toast.LENGTH_SHORT).show();
            }
        });

        requested = new HashMap<>();
        requested.put("id", ownId);
        requested.put("status", "pending");

        userDb = new UserDatabase(userId);
        userDb.getChildCollection("friends").document(ownId).set(requested).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
                Toast.makeText(ViewProfileActivity.this, "Adding friend failed!", Toast.LENGTH_SHORT).show();
            }
        });

        Map<String, Object> notification = new HashMap<>();
        notification.put("userId", Login.getUserId());
        notification.put("notificationType", "New Friend");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users" + "/" + userId + "/notifications")
                .add(notification)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {

                    }
                });
    }

    private void acceptFriend(String userId, String ownId){
        Map<String, Object> accepted = new HashMap<>();
        accepted.put("id", userId);
        accepted.put("status", "accepted");

        UserDatabase userDb = new UserDatabase(ownId);
        userDb.getChildCollection("friends").document(userId).set(accepted).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
                Toast.makeText(ViewProfileActivity.this, "Adding friend failed!", Toast.LENGTH_SHORT).show();
            }
        });

        accepted = new HashMap<>();
        accepted.put("id", ownId);
        accepted.put("status", "accepted");

        userDb = new UserDatabase(userId);
        userDb.getChildCollection("friends").document(ownId).set(accepted).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
                Toast.makeText(ViewProfileActivity.this, "Adding friend failed!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Launches the profile activity
     */
    private void launchProfilesActivity() {
        Intent intent = new Intent(this, ProfilesActivity.class);
        startActivity(intent);
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
        ProfileCache.setUserCached(getUserId(), false);
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
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.edit) {
            onEditProfileClicked();
            return true;
        } else if (itemId == R.id.signOut) {
            onSignOutClicked();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Handles when the sign out button is clicked
     */
    private void onSignOutClicked() {
        new AlertDialog.Builder(this)
                .setCancelable(true)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Confirm", (dialog, which) -> onSignOutConfirmed())
                .setNegativeButton(android.R.string.cancel, ((dialog, which) -> dialog.dismiss()))
                .create()
                .show();
    }

    /**
     * This method handles when sign out is confirmed
     */
    private void onSignOutConfirmed() {
        Login.logout(this, e -> {
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        });

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // add this flag so that we return to an existing MainActivity if it exists rather than creating a new one
        Login.setManualLogin(true);
        Login.forceLogin(); // force re-login so that when we sign-out we are brought to the login screen. Login.logout() occurs asynchronously so without calling this, we may not go to the login screen

        startActivity(intent);
    }
}