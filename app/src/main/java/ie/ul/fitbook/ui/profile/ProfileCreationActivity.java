package ie.ul.fitbook.ui.profile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.firestore.DocumentReference;

import java.util.Map;

import ie.ul.fitbook.database.UserDatabase;
import ie.ul.fitbook.ui.HomeActivity;
import ie.ul.fitbook.ui.MainActivity;
import ie.ul.fitbook.R;
import ie.ul.fitbook.login.Login;
import ie.ul.fitbook.profile.Profile;
import ie.ul.fitbook.ui.profile.fragments.PersistentEditFragment;
import ie.ul.fitbook.ui.profile.viewmodels.ProfileViewModel;

/**
 * This activity's task is prompting the user to enter their details on initial login if they don't have a profile.
 * With the extra EDIT_USER_PROFILE used in the intent, this becomes an activity to allow editing profile details
 */
public class ProfileCreationActivity extends AppCompatActivity {
    /**
     * The view model containing our profile
     */
    private ProfileViewModel profileViewModel;
    /**
     * Keep track of the flag editing profile instead of creating new one
     */
    private boolean editProfile;
    /**
     * True if the user is on the first page
     */
    private boolean onFirstPage;
    /**
     * The current fragment being edited
     */
    private PersistentEditFragment currentFragment;
    /**
     * The flag containing the value of return to previous
     */
    private boolean returnToPrevious;
    /**
     * Use this as an EXTRA key with a boolean value of true in the passed in intent to edit the profile
     * retrieved by Login.getProfile()
     */
    public static final String EDIT_USER_PROFILE = "ie.ul.fitbook.profile.EDIT_USER_PROFILE";
    /**
     * Use this to return to the previous activity instead of HomeActivity when EDIT_USER_PROFILE is used
     */
    public static final String RETURN_TO_PREVIOUS = "ie.ul.fitbook.profile.RETURN_TO_PREVIOUS;";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_creation);

        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        Intent intent = getIntent();

        if (intent.hasExtra(EDIT_USER_PROFILE)) {
            editProfile = intent.getBooleanExtra(EDIT_USER_PROFILE, false);
        }

        if (intent.hasExtra(RETURN_TO_PREVIOUS)) {
            returnToPrevious = intent.getBooleanExtra(RETURN_TO_PREVIOUS, false);
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setTitle((editProfile ? "Edit":"Create") + " Profile");

        setupProfile();
    }

    /**
     * Set up the profile view model based on the edit flags passed in
     */
    private void setupProfile() {
        if (editProfile) {
            Profile loggedIn = Login.getProfile();
            if (loggedIn == null)
                throw new IllegalStateException("Login.getProfile() returned null, has the logged in user's profile been set?");

            profileViewModel.selectProfile(Profile.copy(loggedIn));
        } else {
            profileViewModel.selectProfile(new Profile());
        }
    }

    /**
     * This flag indicates that the profile is being edited
     * @return true if currently logged in profile if being edited, false if not
     */
    public boolean isInEditMode() {
        return editProfile;
    }

    /**
     * This method handles the cancellation of profile creation
     */
    public void onCancel() {
        new AlertDialog.Builder(this) // this doesn't work with using the activity instance field for some reason
                .setCancelable(true)
                .setTitle("Discard Changes")
                .setMessage("Are you sure you want to discard all changes?")
                .setPositiveButton("Confirm", (dialog, which) -> onCancelConfirmed())
                .setNegativeButton(android.R.string.cancel, ((dialog, which) -> dialog.dismiss()))
                .create()
                .show();
    }

    /**
     * Called when the activity has detected the user's press of the back
     * key. The {@link #getOnBackPressedDispatcher() OnBackPressedDispatcher} will be given a
     * chance to handle the back button before the default behavior of
     * {@link Activity#onBackPressed()} is invoked.
     *
     * @see #getOnBackPressedDispatcher()
     */
    @Override
    public void onBackPressed() {
        if (onFirstPage)
            onCancel();
        else
            super.onBackPressed();

        if (currentFragment != null)
            currentFragment.saveEditState(profileViewModel.getSelectedProfile().getValue());
    }

    /**
     * Callback for when a cancel is confirmed
     */
    private void onCancelConfirmed() {
        Intent intent;

        if (editProfile) {
            intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            Intent receivedIntent = getIntent();
            if (receivedIntent.hasExtra(HomeActivity.FRAGMENT_ID)) {
                // check if the HomeActivity requested a specific fragment to navigate to on return
                intent.putExtra(HomeActivity.FRAGMENT_ID, receivedIntent.getIntExtra(HomeActivity.FRAGMENT_ID, 0));
            }
        } else {
            intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            Login.setManualLogin(true);
            Login.forceLogin();
            Login.logout(this, null);
        }

        finish(); // we don't want to come back this activity on the press of the back key

        if (!returnToPrevious)
            startActivity(intent);
    }

    /**
     * This method handles a successful profile creation
     */
    public void onSubmit() {
        Profile profile = profileViewModel.getSelectedProfile().getValue();

        if (profile == null)
            throw new IllegalStateException("Cannot submit ProfileCreationActivity without a profile");

        saveProfile(profile);
        Login.setProfile(profile);

        Intent receivedIntent = getIntent();

        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (receivedIntent.hasExtra(HomeActivity.FRAGMENT_ID))
            intent.putExtra(HomeActivity.FRAGMENT_ID, receivedIntent.getIntExtra(HomeActivity.FRAGMENT_ID, 0));

        finish(); // we don't want to come back this activity on the press of the back key

        if (!returnToPrevious)
            startActivity(intent);
    }

    /**
     * Saves the profile to the database
     * @param profile the profile to save
     */
    private void saveProfile(Profile profile) {
        DocumentReference documentReference = new UserDatabase()
                .getChildDocument(Profile.PROFILE_DOCUMENT);

        Map<String, Object> data = profile.toData();
        documentReference.set(data); // save the data to the database
    }

    /**
     * Dispatch incoming result to the correct fragment.
     * Required so fragments get the result
     *
     * @param requestCode the request code signifying the request
     * @param resultCode the result code signifying the result
     * @param data the data if present
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Save any fragment's instance states if they have it overridden
     * @param outState the state bundle to save
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /**
     * Call this when you are on the first editing page
     */
    public void onFirstPage() {
        onFirstPage = true;
    }

    /**
     * Call this when you move away from the first editing page
     */
    public void offFirstPage() {
        onFirstPage = false;
    }

    /**
     * Set the current fragment the user is on
     * @param currentFragment the current fragment
     */
    public void setCurrentFragment(PersistentEditFragment currentFragment) {
        this.currentFragment = currentFragment;
    }
}