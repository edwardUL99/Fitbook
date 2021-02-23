package ie.ul.fitbook.ui.profile.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import ie.ul.fitbook.ui.HomeActivity;
import ie.ul.fitbook.ui.MainActivity;
import ie.ul.fitbook.R;
import ie.ul.fitbook.login.Login;
import ie.ul.fitbook.ui.profile.ProfileCreationActivity;
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
    private ConstraintLayout profileContainer;

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

        if (Login.isProfileOutOfSync()) {
            setLoading(true);
            ProfileUtils.syncProfile(this::onProfileSync, this::onProfileSyncFail);
        } else {
            setLoading(false);
        }

        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.profileRefresh);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            setLoading(true);
            ProfileUtils.syncProfile(() -> {
                this.onProfileSync();
                swipeRefreshLayout.setRefreshing(false);
            }, () -> {
                this.onProfileSyncFail();
                swipeRefreshLayout.setRefreshing(false);
            });
        });

        setHasOptionsMenu(true);
    }

    /**
     * Handles when profile is synced
     */
    private void onProfileSync() {
        // TODO set appropriate fields with synced profile here when profile fragment is fully implemented
        setLoading(false);
        Login.setProfileOutOfSync(false);
    }

    /**
     * Handles when profile sync fails
     */
    private void onProfileSyncFail() {
        setLoading(false);
        Toast.makeText(activity, "Failed to load profile", Toast.LENGTH_SHORT)
                .show();
        activity.getNavController().navigate(R.id.navigation_home);
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
        if (!loading) {
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
}