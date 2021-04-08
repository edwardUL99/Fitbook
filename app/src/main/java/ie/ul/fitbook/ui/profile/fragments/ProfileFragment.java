package ie.ul.fitbook.ui.profile.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Locale;
import java.util.Map;

import ie.ul.fitbook.custom.LoadingBar;
import ie.ul.fitbook.custom.TraceableScrollView;
import ie.ul.fitbook.database.UserDatabase;
import ie.ul.fitbook.profile.Profile;
import ie.ul.fitbook.sports.Sport;
import ie.ul.fitbook.statistics.WeeklyStat;
import ie.ul.fitbook.statistics.WeeklyStatistics;
import ie.ul.fitbook.ui.HomeActivity;
import ie.ul.fitbook.ui.MainActivity;
import ie.ul.fitbook.R;
import ie.ul.fitbook.login.Login;
import ie.ul.fitbook.ui.home.FriendsList;
import ie.ul.fitbook.ui.home.ProfilesActivity;
import ie.ul.fitbook.ui.profile.activities.ListActivitiesActivity;
import ie.ul.fitbook.ui.profile.goals.GoalsActivity;
import ie.ul.fitbook.ui.profile.ProfileCreationActivity;
import ie.ul.fitbook.ui.profile.posts.ProfilePostsActivity;
import ie.ul.fitbook.utils.ProfileUtils;
import ie.ul.fitbook.utils.Utils;

/**
 * Fragment for displaying profile
 */
public class ProfileFragment extends Fragment {
    /**
     * The activity behind this fragment
     */
    private HomeActivity activity;
    /**
     * The progress bar for saying the profile is loading
     */
    private LoadingBar loadingBar;
    /**
     * If loaded already once, don't display the progress bar, just refresh while still allowing
     * profile to be viewed
     */
    private static boolean LOADED_ONCE;
    /**
     * The container containing profile items
     */
    private TraceableScrollView profileContainer;
    /**
     * The CardView holding the biography
     */
    private CardView profileBio;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * has returned, but before any saved state has been restored in to the view.
     * This gives subclasses a chance to initialize themselves once
     * they know their view hierarchy has been completely created.  The fragment's
     * view hierarchy is not however attached to its parent at this point.
     *
     * @param view               The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        activity = (HomeActivity)requireActivity();
        loadingBar = view.findViewById(R.id.loadingBar);
        profileContainer = view.findViewById(R.id.profileContainer);
        loadingBar.setLoadedLayout(profileContainer);
        profileBio = view.findViewById(R.id.biographyLayout);

        swipeRefreshLayout = view.findViewById(R.id.profileRefresh);
        profileContainer.setOnScrollDetected(this::onScrollDetected);
        profileContainer.setOnScrollFinished(this::onScrollReleaseDetected);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (!LOADED_ONCE)
                loadingBar.show();

            refreshWeeklyStats(view);
            ProfileUtils.syncProfile(activity, () -> {
                this.onProfileSync(view);
                swipeRefreshLayout.setRefreshing(false);
            }, () -> {
                this.onProfileSyncFail();
                swipeRefreshLayout.setRefreshing(false);
            }, profileImage);
        });

        setHasOptionsMenu(true);

        profileImage = view.findViewById(R.id.profilePicture);
        nameView = view.findViewById(R.id.name);
        addressView = view.findViewById(R.id.address);
        favouriteActivityView = view.findViewById(R.id.favActivityView);
        friendsButton = view.findViewById(R.id.friendsButton);
        friendsButton.setOnClickListener(v -> onAddFriendsClicked());
        friendsView = view.findViewById(R.id.friends);






        friendsView.setOnClickListener(v -> onFriendsClicked());
        setupProfileOptions(view);

        if (Login.isProfileOutOfSync()) {
            LOADED_ONCE = false;
            loadingBar.show();
            ProfileUtils.syncProfile(activity, () -> onProfileSync(view), this::onProfileSyncFail, profileImage);
        } else {
            loadingBar.show();
            onProfileSync(view);
        }
    }

    /**
     * Refreshes the weekly stats for this profile
     * @param view the view containing the stats layout
     */
    private void refreshWeeklyStats(View view) {
        TextView weekView = view.findViewById(R.id.weekView);
        weekView.setText(WeeklyStatistics.getWeek());

        String userId = Login.getUserId();
        DocumentReference cycleReference = WeeklyStatistics.getSportWeeklyStat(userId, Sport.CYCLING);
        DocumentReference runReference = WeeklyStatistics.getSportWeeklyStat(userId, Sport.RUNNING);
        DocumentReference walkReference = WeeklyStatistics.getSportWeeklyStat(userId, Sport.WALKING);

        setCycleStats(view, cycleReference);
        setRunStats(view, runReference);
        setWalkStats(view, walkReference);
    }

    /**
     * Sets the cycling stats
     * @param view the view for the layout
     * @param cycleReference the document reference for the cycle stats
     */
    private void setCycleStats(View view, DocumentReference cycleReference) {
        cycleReference.get()
                .addOnCompleteListener(task -> {
                    try {
                        DocumentSnapshot snapshot = task.getResult();
                        Map<String, Object> data;
                        if (snapshot != null && (data = snapshot.getData()) != null) {
                            WeeklyStat weeklyStat = WeeklyStat.from(data);
                            TextView distance = view.findViewById(R.id.cycleDistance);
                            distance.setText(String.format(Locale.getDefault(), "%,.01fkm", weeklyStat.getDistance()));
                            TextView time = view.findViewById(R.id.cycleTime);
                            time.setText(Utils.durationToHoursMinutes(weeklyStat.getTime()));
                            TextView elevation = view.findViewById(R.id.cycleElevation);
                            elevation.setText(String.format(Locale.getDefault(), "%dm", weeklyStat.getElevation()));
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
    }

    /**
     * Sets the running stats
     * @param view the view for the layout
     * @param runReference the document reference for the run stats
     */
    private void setRunStats(View view, DocumentReference runReference) {
        runReference.get()
                .addOnCompleteListener(task -> {
                    try {
                        DocumentSnapshot snapshot = task.getResult();
                        Map<String, Object> data;
                        if (snapshot != null && (data = snapshot.getData()) != null) {
                            WeeklyStat weeklyStat = WeeklyStat.from(data);
                            TextView distance = view.findViewById(R.id.runDistance);
                            distance.setText(String.format(Locale.getDefault(), "%,.01fkm", weeklyStat.getDistance()));
                            TextView time = view.findViewById(R.id.runTime);
                            time.setText(Utils.durationToHoursMinutes(weeklyStat.getTime()));
                            TextView elevation = view.findViewById(R.id.runElevation);
                            elevation.setText(String.format(Locale.getDefault(), "%dm", weeklyStat.getElevation()));
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
    }

    /**
     * Sets the walking stats
     * @param view the view for the layout
     * @param walkReference the document reference for the cycle stats
     */
    private void setWalkStats(View view, DocumentReference walkReference) {
        walkReference.get()
                .addOnCompleteListener(task -> {
                    try {
                        DocumentSnapshot snapshot = task.getResult();
                        Map<String, Object> data;
                        if (snapshot != null && (data = snapshot.getData()) != null) {
                            WeeklyStat weeklyStat = WeeklyStat.from(data);
                            TextView distance = view.findViewById(R.id.walkDistance);
                            distance.setText(String.format(Locale.getDefault(), "%,.01fkm", weeklyStat.getDistance()));
                            TextView time = view.findViewById(R.id.walkTime);
                            time.setText(Utils.durationToHoursMinutes(weeklyStat.getTime()));
                            TextView elevation = view.findViewById(R.id.walkElevation);
                            elevation.setText(String.format(Locale.getDefault(), "%dm", weeklyStat.getElevation()));
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
    }

    /**
     * Sets up all the options included in profile_options_layout
     * @param view the view containing this layout
     */
    private void setupProfileOptions(View view) {
        ConstraintLayout goalLayout = view.findViewById(R.id.goalsLayout);
        goalLayout.setOnClickListener(v -> {
            Intent intent = new Intent(activity, GoalsActivity.class);
            intent.putExtra(HomeActivity.FRAGMENT_ID, activity.getNavController().getCurrentDestination().getId());
            startActivity(intent);
        });

        ConstraintLayout activitiesLayout = view.findViewById(R.id.activitiesLayout);
        activitiesLayout.setOnClickListener(v -> {
            Intent intent = new Intent(activity, ListActivitiesActivity.class);
            intent.putExtra(HomeActivity.FRAGMENT_ID, activity.getNavController().getCurrentDestination().getId());
            startActivity(intent);
        });

        ConstraintLayout postsLayout = view.findViewById(R.id.postsLayout);
        postsLayout.setOnClickListener(v -> {
            Intent intent = new Intent(activity, ProfilePostsActivity.class);
            intent.putExtra(HomeActivity.FRAGMENT_ID, activity.getNavController().getCurrentDestination().getId());
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
     * Handles the click of the add friends button
     */
    private void onAddFriendsClicked() {
        Intent intent = new Intent(activity, ProfilesActivity.class);
        // TODO may need to add a new extra for an id to return to which will override any Last ID passed into the intent
        startActivity(intent);
    }

    /**
     * Handles the clicking of the number of friends to view a friends list
     */
    private void onFriendsClicked() {
        // TODO show friends list here
        //Toast.makeText(activity, "Friends list will display when done", Toast.LENGTH_SHORT)
             //   .show();

        Intent intent = new Intent(getActivity(), FriendsList.class);
        startActivity(intent);
        ((Activity) getActivity()).overridePendingTransition(0, 0);
    }

    /**
     * Handles when profile is synced
     * @param view the view for this profile fragment
     */
    private void onProfileSync(View view) {
        Profile profile = Login.getProfile();

        if (profile == null)
            throw new IllegalStateException("A profile cannot be null when displaying the ProfileFragment");

        profileImage.setImageBitmap(profile.getProfileImage());
        nameView.setText(profile.getName());
        String address = profile.getCity() + ", " + profile.getState();
        addressView.setText(address);
        favouriteActivityView.setText(profile.getFavouriteSport());

        setupBioTextView(profile);
        onFriendsSync();
        loadingBar.hide();
        refreshWeeklyStats(view);
    }

    /**
     * Syncs the number of friends this user has
     */
    private void onFriendsSync() {
        new UserDatabase()
                .getDatabase()
                .get()
                .addOnSuccessListener(success -> {
                    Map<String, Object> data = success.getData();

                    if (data != null) {
                        Long friendsNum = (Long)data.get("friends-count");
                        friendsNum = friendsNum == null ? 0:friendsNum;
                        String friendsNumber = "" + friendsNum;
                        friendsView.setText(friendsNumber);
                        loadingBar.hide();
                        LOADED_ONCE = true;
                        Login.setProfileOutOfSync(false); // we have now fully synced our profile
                    } else {
                        String zero = "" + 0;
                        friendsView.setText(zero);
                    }

                    LOADED_ONCE = true;
                    Login.setProfileOutOfSync(false); // we have now fully synced our profile
                })
                .addOnFailureListener(fail -> onProfileSyncFail());
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
     * Handles when profile sync fails
     */
    private void onProfileSyncFail() {
        loadingBar.hideBoth();
        Toast.makeText(activity, "Failed to load profile", Toast.LENGTH_SHORT)
                .show();
    }

    /**
     * Initialize the contents of the Fragment host's standard options menu.  You
     * should place your menu items in to <var>menu</var>.  For this method
     * to be called, you must have first called {@link #setHasOptionsMenu}.
     * for more information.
     *
     * @param menu     The options menu in which you place your items.
     * @param inflater
     * @see #setHasOptionsMenu
     * @see #onPrepareOptionsMenu
     * @see #onOptionsItemSelected
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.action_bar_profile, menu);
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
     * perform the default menu handling.
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to
     * proceed, true to consume it here.
     * @see #onCreateOptionsMenu
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (!loadingBar.isLoading()) {
            int id = item.getItemId();

            if (id == R.id.edit) {
                onEditProfileClicked();
                return true;
            } else if (id == R.id.signOut) {
                onSignOutClicked();
                return true;
            }

            return super.onOptionsItemSelected(item);
        }

        return false;
    }

    /**
     * Handles when the sign out button is clicked
     */
    private void onSignOutClicked() {
        new AlertDialog.Builder(getActivity()) // this doesn't work with using the activity instance field for some reason
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
        Login.logout(activity, e -> {
            Toast.makeText(activity, "Logged out successfully", Toast.LENGTH_SHORT).show();
        });

        Intent intent = new Intent(activity, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // add this flag so that we return to an existing MainActivity if it exists rather than creating a new one
        Login.setManualLogin(true);
        Login.forceLogin(); // force re-login so that when we sign-out we are brought to the login screen. Login.logout() occurs asynchronously so without calling this, we may not go to the login screen

        startActivity(intent);
    }

    /**
     * This method is called when the edit profile button is pressed
     */
    private void onEditProfileClicked() {
        Intent intent = new Intent(activity, ProfileCreationActivity.class);
        intent.putExtra(ProfileCreationActivity.EDIT_USER_PROFILE, true);
        intent.putExtra(HomeActivity.FRAGMENT_ID, activity.getNavController().getCurrentDestination().getId());
        startActivity(intent);
    }

    /**
     * Called to ask the fragment to save its current dynamic state, so it
     * can later be reconstructed in a new instance of its process is
     * restarted.  If a new instance of the fragment later needs to be
     * created, the data you place in the Bundle here will be available
     * in the Bundle given to {@link #onCreate(Bundle)},
     * {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}, and
     * {@link #onActivityCreated(Bundle)}.
     *
     * Activity.onSaveInstanceState(Bundle)} and most of the discussion there
     * applies here as well.  Note however: <em>this method may be called
     * at any time before {@link #onDestroy()}</em>.  There are many situations
     * where a fragment may be mostly torn down (such as when placed on the
     * back stack with no UI showing), but its state will not be saved until
     * its owning activity actually needs to save its state.
     *
     * @param outState Bundle in which to place your saved state.
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }


}