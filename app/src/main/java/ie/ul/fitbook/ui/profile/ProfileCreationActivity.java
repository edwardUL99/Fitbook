package ie.ul.fitbook.ui.profile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.firestore.DocumentReference;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import ie.ul.fitbook.ui.HomeActivity;
import ie.ul.fitbook.ui.MainActivity;
import ie.ul.fitbook.R;
import ie.ul.fitbook.database.Database;
import ie.ul.fitbook.database.Databases;
import ie.ul.fitbook.login.Login;
import ie.ul.fitbook.profile.Profile;
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
     * Use this as an EXTRA key with a boolean value of true in the passed in intent to edit the profile
     * retrieved by Login.getProfile()
     */
    public static final String EDIT_USER_PROFILE = "ie.ul.fitbook.profile.EDIT_USER_PROFILE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_creation);

        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        Intent intent = getIntent();

        if (intent.hasExtra(EDIT_USER_PROFILE)) {
            editProfile = intent.getBooleanExtra(EDIT_USER_PROFILE, false);
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
        startActivity(intent);
    }

    /**
     * This method handles a successful profile creation
     */
    public void onSubmit() {
        Profile profile;

        if (!editProfile) {
            profile = profileViewModel.getSelectedProfile().getValue();

            if (profile == null)
                throw new IllegalStateException("Cannot submit ProfileCreationActivity without a profile");
        } else {
            profile = Login.getProfile();
        }

        saveProfile(profile);
        Login.setProfile(profile);

        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        finish(); // we don't want to come back this activity on the press of the back key
        startActivity(intent);
    }

    /**
     * Saves the profile to the database
     * @param profile the profile to save
     */
    private void saveProfile(Profile profile) {
        DocumentReference documentReference = Objects.requireNonNull(Database.getInstance(Databases.USERS))
                .getChildDocument(Profile.PROFILE_DOCUMENT);

        Map<String, Object> data = new HashMap<>();
        saveProfileInfo(profile, data);
        saveAthleticInformation(profile.getAthleticInformation(), data);

        documentReference.set(data); // save the data to the database
    }

    /**
     * Saves the profile info to the data map
     * @param profile the profile to save
     * @param data the map where data is being stored to
     */
    private void saveProfileInfo(Profile profile, Map<String, Object> data) {
        data.put(Profile.NAME_KEY, profile.getName());
        data.put(Profile.CITY_KEY, profile.getCity());
        data.put(Profile.STATE_KEY, profile.getState());
        data.put(Profile.FAV_SPORT_KEY, profile.getFavouriteSport());
        data.put(Profile.BIOGRAPHY_KEY, profile.getBio());
    }

    private void saveAthleticInformation(Profile.AthleticInformation athleticInformation, Map<String, Object> data) {
        data.put(Profile.AthleticInformation.DOB_FIELD, athleticInformation.getDateOfBirth());
        data.put(Profile.AthleticInformation.GENDER_FIELD, athleticInformation.getGender());
        data.put(Profile.AthleticInformation.WEIGHT_FIELD, athleticInformation.getWeight());
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
}