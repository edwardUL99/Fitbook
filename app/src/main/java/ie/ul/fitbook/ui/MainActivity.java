package ie.ul.fitbook.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.List;
import java.util.Objects;

import ie.ul.fitbook.R;
import ie.ul.fitbook.database.Database;
import ie.ul.fitbook.database.Databases;
import ie.ul.fitbook.login.Login;
import ie.ul.fitbook.network.NetworkUtils;
import ie.ul.fitbook.profile.Profile;
import ie.ul.fitbook.ui.profile.ProfileCreationActivity;
import ie.ul.fitbook.utils.ProfileUtils;
import ie.ul.fitbook.utils.Utils;

/**
 * The MainActivity launching this application. It basically acts as an intermediary checking status of login,
 * user profile and other initialisation tasks before launching other activities
 */
public class MainActivity extends AppCompatActivity {
    /**
     * A code for logging in
     */
    private static final int RC_LOGIN = 123;
    /**
     * The application context that has a lifecycle for the entire program
     */
    public static Context APPLICATION_CONTEXT;

    /**
     * Handles the creation of the MainActivity
     * @param savedInstanceState the bundle of the saved instance if any, null otherwise
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (APPLICATION_CONTEXT == null)
            APPLICATION_CONTEXT = getApplicationContext();

        setContentView(R.layout.activity_main);

        if (!Login.checkLogin(this)) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, RC_LOGIN);
        } else {
            checkProfileStatus();
        }
    }

    /**
     * Checks if the user has a profile or not and if not, opens the ProfileCreationActivity
     */
    private void checkProfileStatus() {
        DocumentReference documentReference = Objects.requireNonNull(Database.getInstance(Databases.USERS))
                .getChildDocument(Profile.PROFILE_DOCUMENT);
        documentReference.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot snapshot = task.getResult();

                        if (snapshot != null && snapshot.exists()) {
                            startHomeActivity();
                            ProfileUtils.setProfileSnapshot(snapshot); // use this snapshot for the next time Utils.syncProfile is called
                        } else {
                            if (NetworkUtils.isNetworkConnected(this)) {
                                Toast.makeText(this, "Please set-up your user profile", Toast.LENGTH_SHORT)
                                        .show();
                                Intent intent = new Intent(this, ProfileCreationActivity.class);
                                startActivity(intent); // we need to setup a profile if the profile doesn't already exist
                                finish(); // we don't want to come back here after these activities using the back button
                            } else {
                                new AlertDialog.Builder(this)
                                        .setTitle("Profile Setup")
                                        .setMessage("You are not connected to the internet, so a profile cannot be setup."
                                            + " Please re-open the app with an internet connection to try again")
                                        .setPositiveButton("Ok", (dialog, which) -> finishAffinity())
                                        .create()
                                        .show();
                            }
                        }
                    } else {
                        Exception exception = task.getException();

                        Toast.makeText(this, "An unknown error occurred launching the application, exiting...", Toast.LENGTH_SHORT)
                                .show();
                        System.exit(1);
                    }
                });
    }

    /**
     * Dispatch incoming result to the correct fragment.
     *
     * @param requestCode the request code to identify the result
     * @param resultCode the result code identifying the success
     * @param data the intent containing all required data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_LOGIN) {
            if (resultCode == RESULT_OK) {
                checkProfileStatus();
            } else {
                Intent intent = new Intent(this, LoginActivity.class);
                Login.setManualLogin(true); // return to manual login
                startActivityForResult(intent, RC_LOGIN);
            }
        }
    }

    /**
     * This method starts the home activity
     */
    private void startHomeActivity() {
        if (!NetworkUtils.isNetworkConnected(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT)
                    .show();
        }

        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish(); // we don't want to come back here after these activities using the back button
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /**
     * This method is called after {@link #onStart} when the activity is
     * being re-initialized from a previously saved state, given here in
     * <var>savedInstanceState</var>.  Most implementations will simply use {@link #onCreate}
     * to restore their state, but it is sometimes convenient to do it here
     * after all of the initialization has been done or to allow subclasses to
     * decide whether to use your default implementation.  The default
     * implementation of this method performs a restore of any view state that
     * had previously been frozen by {@link #onSaveInstanceState}.
     *
     * <p>This method is called between {@link #onStart} and
     * {@link #onPostCreate}. This method is called only when recreating
     * an activity; the method isn't invoked if {@link #onStart} is called for
     * any other reason.</p>
     *
     * @param savedInstanceState the data most recently supplied in {@link #onSaveInstanceState}.
     * @see #onCreate
     * @see #onPostCreate
     * @see #onResume
     * @see #onSaveInstanceState
     */
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    /**
     * This needs to be done as extras are not getting handled by this class
     * @param intent the intent to replace our intent with
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }
}