package ie.ul.fitbook.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import ie.ul.fitbook.R;
import ie.ul.fitbook.database.Database;
import ie.ul.fitbook.database.Databases;
import ie.ul.fitbook.login.Login;
import ie.ul.fitbook.network.NetworkUtils;
import ie.ul.fitbook.network.callbacks.MainActivityInternetRestore;
import ie.ul.fitbook.profile.Profile;
import ie.ul.fitbook.ui.profile.ProfileCreationActivity;
import ie.ul.fitbook.storage.Storage;
import ie.ul.fitbook.storage.Stores;
import ie.ul.fitbook.utils.Utils;

/**
 * The MainActivity launching this application. It basically acts as an intermediary checking status of login,
 * user profile and other initialisation tasks before launching other activities
 *
 * You can pass in extra LoginActivity.DISPLAY_SIGN_IN to the intent for this activity and it will pass it along.
 * Passing in MainActivity.SIGN_OUT forces a re-login. This essentially says we are coming to this activity
 * after a sign-out, so display the login activity
 */
public class MainActivity extends AppCompatActivity {
    /**
     * The storage reference that was used to download a profile image
     */
    private StorageReference imageDownloadRef;
    /**
     * The path of the downloaded file
     */
    private String imageDownloadPath;
    /**
     * A code for logging in
     */
    private static final int RC_LOGIN = 123;
    /**
     * Extra key for storing download reference for profile picture
     */
    private static final String DOWNLOAD_PROGRESS = "ie.ul.fitbook.DOWNLOAD_REF";
    /**
     * A key for the path of the downloaded image
     */
    private static final String DOWNLOADED_IMAGE_PATH = "ie.ul.fitbook.DOWNLOAD_IMAGE_PATH";
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
                            initialiseProfile(snapshot);
                        } else {
                            Toast.makeText(this, "Please set-up your user profile", Toast.LENGTH_SHORT)
                                .show();
                            Intent intent = new Intent(this, ProfileCreationActivity.class);
                            startActivity(intent); // we need to setup a profile if the profile doesn't already exist
                            finish(); // we don't want to come back here after these activities using the back button
                        }
                    } else {
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
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish(); // we don't want to come back here after these activities using the back button
    }

    /**
     * Downloads the profile picture
     */
    private void downloadProfilePicture() {
        StorageReference storageReference = Objects.requireNonNull(Storage.getInstance(Stores.USERS)).getChildFolder(Profile.PROFILE_IMAGE_PATH);

        File file = Utils.getProfileImageLocation(this);

        if (file == null) {
            onProfileImgDownloadFail();
            startHomeActivity();
            return;
        }

        AtomicBoolean startHomeActivity = new AtomicBoolean(true);

        boolean exists = file.exists();

        if (exists) {
            onProfileImgDownload(file);
            startHomeActivity(); // only start home activity when the profile has been fully initialised (i.e. profile picture downloaded)
            startHomeActivity.set(false);
        }

        if (NetworkUtils.isNetworkConnected(this, new MainActivityInternetRestore())) { // sync up profile picture in background. If internet was lost, use the MainInternetRestoredCallback to restart the application when it is restored
            try {
                final File finalFile = file;
                imageDownloadPath = file.getAbsolutePath();
                storageReference.getFile(file)
                        .addOnSuccessListener(this, success -> {
                            onProfileImgDownload(finalFile);
                            if (startHomeActivity.get())
                                startHomeActivity(); // this would be false if we are just downloading it in the background
                        })
                        .addOnFailureListener(failed -> onProfileImgDownloadFail());
                imageDownloadRef = storageReference;
            } catch (Exception ex) {
                onProfileImgDownloadFail();
                startHomeActivity();
            }
        } else if (!exists) {
            Toast.makeText(this, "No internet connection, failed to download profile image", Toast.LENGTH_SHORT)
                    .show();
            startHomeActivity();
        } else {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT)
                    .show();
            startHomeActivity();
        }
    }

    /**
     * This method initialises the user's profile by extracting the profile from the provided document snapshot and creates a profile from it
     * and calls Login.setProfile and then downloading the profile picture. Starts the home activity when everything has been initialised
     * @param documentSnapshot the document representing the profile
     */
    private void initialiseProfile(DocumentSnapshot documentSnapshot) {
        Map<String, Object> data = documentSnapshot.getData();

        if (data != null) {
            Login.setProfile(Profile.from(data));
            downloadProfilePicture();
        }
    }

    /**
     * Handles successful profile image download
     * @param destination the download file
     */
    private void onProfileImgDownload(File destination) {
        onProfileImgDownload(this, destination);
    }

    /**
     * Set the image with a different context than this activity's context
     * @param context the context to use
     * @param destination destination file
     */
    private void onProfileImgDownload(Context context, File destination) {
        Bitmap bitmap = Utils.getBitmapFromFile(context, Uri.fromFile(destination));
        Profile profile = Login.getProfile();
        if (profile != null)
            profile.setProfileImage(bitmap);
    }

    /**
     * Handles when downloading profile image fails
     */
    private void onProfileImgDownloadFail() {
        Toast.makeText(this, "Failed to download profile image from server", Toast.LENGTH_SHORT)
            .show();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (imageDownloadRef != null) {
            outState.putString(DOWNLOAD_PROGRESS, imageDownloadRef.toString());
            outState.putString(DOWNLOADED_IMAGE_PATH, imageDownloadPath);
        }
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

        String ref = savedInstanceState.getString(DOWNLOAD_PROGRESS);
        String path = savedInstanceState.getString(DOWNLOADED_IMAGE_PATH);

        if (ref == null)
            return;

        imageDownloadRef = FirebaseStorage.getInstance().getReferenceFromUrl(ref);

        List<FileDownloadTask> tasks = imageDownloadRef.getActiveDownloadTasks();
        if (tasks.size() > 0) {
            FileDownloadTask task = tasks.get(0); // there should be only one task running here

            task.addOnSuccessListener(this, success -> onProfileImgDownload(new File(path)))
                .addOnFailureListener(failed -> onProfileImgDownloadFail());
        }
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