package ie.ul.fitbook.utils;


import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.Map;

import ie.ul.fitbook.database.UserDatabase;
import ie.ul.fitbook.interfaces.ActionHandler;
import ie.ul.fitbook.interfaces.ActionHandlerConsumer;
import ie.ul.fitbook.login.Login;
import ie.ul.fitbook.network.NetworkUtils;
import ie.ul.fitbook.profile.Profile;
import ie.ul.fitbook.storage.UserStorage;
import ie.ul.fitbook.ui.MainActivity;

/**
 * This class provides various utility methods for managing profiles throughout the
 * application
 */
public final class ProfileUtils {
    /**
     * A variable to keep track of the profile snapshot retrieved
     */
    private static DocumentSnapshot profileSnapshot;
    /**
     * The handler for when a profile is downloaded/synced
     */
    private static ActionHandler successProfileDownloadHandler;
    /**
     * The handler for when profile download fails
     */
    private static ActionHandler failProfileDownloadHandler;

    private ProfileUtils() {
        // prevent instantiation
    }

    /**
     * Returns the file representing where to save the profile picture
     * @param context the context requesting this action
     * @return the file representing the image location, null if an error occurred
     */
    public static File getProfileImageLocation(Context context) {
        File file = context.getFilesDir();
        file = new File(file, "profile-picture");

        if (!file.isDirectory() && !file.mkdir()) {
            return null;
        } else {
            return new File(file, "profile_pic.png");
        }
    }

    /**
     * This method downloads the profile image in the background using the application context.
     * It is assumed that you have network connectivity
     */
    private static void downloadProfileImage() {
        File file = getProfileImageLocation(MainActivity.APPLICATION_CONTEXT);

        if (file == null)
            return;

        StorageReference storageReference = new UserStorage().getChildFolder(Profile.PROFILE_IMAGE_PATH);

        if (NetworkUtils.isNetworkConnected(MainActivity.APPLICATION_CONTEXT)) {
            storageReference.getFile(file)
                    .addOnSuccessListener(success -> {
                        Bitmap bitmap1 = Utils.getBitmapFromFile(MainActivity.APPLICATION_CONTEXT, Uri.fromFile(file));
                        Profile profile1 = Login.getProfile(); // profile may have became null by the time download finished

                        if (profile1 != null)
                            profile1.setProfileImage(bitmap1);
                        onSucceededProfileSync();
                    })
                    .addOnFailureListener(failed -> onFailedProfileSync());
        } else {
            onFailedProfileSync();
        }
    }

    /**
     * Returns the file representing where to save the profile picture
     * @param context the context requesting this action
     * @param userId the user ID of the user this profile image will belong to
     * @return the file representing the image location, null if an error occurred
     */
    private static File getUserProfileImageLocation(Context context, String userId) {
        File file = context.getFilesDir();
        file = new File(file, "profile-picture");

        if (!file.isDirectory() && !file.mkdir()) {
            return null;
        } else {
            return new File(file, "profile_pic_" + userId + ".png");
        }
    }

    /**
     * Downloads a profile image for a different profile than the logged in profile
     * @param profile the profile to download the image for
     * @param userId the User ID of the user to download the profile
     * @param onSuccess the handler for when the download succeeds
     * @param onFail the handler for when it fails
     */
    private static void downloadUserProfileImage(Profile profile, String userId, ActionHandlerConsumer<Profile> onSuccess, ActionHandler onFail) {
        File file = getUserProfileImageLocation(MainActivity.APPLICATION_CONTEXT, userId);

        if (file == null)
            return;

        file.deleteOnExit(); // we don't want this photo anymore when application closes

        StorageReference storageReference = new UserStorage(userId).getChildFolder(Profile.PROFILE_IMAGE_PATH);

        if (NetworkUtils.isNetworkConnected(MainActivity.APPLICATION_CONTEXT)) {
            storageReference.getFile(file)
                    .addOnSuccessListener(success -> {
                        Bitmap bitmap1 = Utils.getBitmapFromFile(MainActivity.APPLICATION_CONTEXT, Uri.fromFile(file));
                        profile.setProfileImage(bitmap1);
                        if (onSuccess != null)
                            onSuccess.doAction(profile);
                    })
                    .addOnFailureListener(failed -> {
                        if (onFail != null)
                            onFail.doAction();
                    });
        } else {
            if (onFail != null)
                onFail.doAction();
        }
    }

    /**
     * This method downloads the profile in the background.
     * It also downloads the profile picture in the background
     */
    private static void downloadProfile() {
        if (profileSnapshot == null) {
            DocumentReference documentReference = new UserDatabase()
                    .getChildDocument(Profile.PROFILE_DOCUMENT);
            documentReference.get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            profileSnapshot = task.getResult();
                            processProfileSnapshot();
                        } else {
                            Exception exception = task.getException();
                            if (exception != null)
                                exception.printStackTrace();

                            onFailedProfileSync();
                        }
                    })
                    .addOnFailureListener(task -> onFailedProfileSync());
        } else {
            processProfileSnapshot();
        }
    }

    /**
     * Download the profile of a user with the specified userID
     * @param userId the ID of the user to download
     * @param onSuccess the handler for when the profile is downloaded successfully
     * @param onFail the handler for when thr profile fails to be downloaded
     */
    public static void downloadProfile(String userId, ActionHandlerConsumer<Profile> onSuccess, ActionHandler onFail) {
        boolean onFailNonNull = onFail != null;
        DocumentReference documentReference = new UserDatabase(userId)
                .getChildDocument(Profile.PROFILE_DOCUMENT);
        documentReference.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot snapshot = task.getResult();

                        if (snapshot != null) {
                            Map<String, Object> data = snapshot.getData();

                            if (data != null) {
                                Profile profile = Profile.from(data);
                                profile.setUserId(userId);
                                downloadUserProfileImage(profile, userId, onSuccess, onFail);
                            } else if (onFailNonNull) {
                                onFail.doAction();
                            }
                        } else if (onFailNonNull) {
                            onFail.doAction();
                        }
                    } else {
                        Exception exception = task.getException();
                        if (exception != null)
                            exception.printStackTrace();

                        if (onFailNonNull)
                            onFail.doAction();
                    }
                })
                .addOnFailureListener(failed -> {
                    failed.printStackTrace();
                    if (onFailNonNull)
                        onFail.doAction();
                });
    }

    /**
     * Processes the profileSnapshot variable. To work on the snapshot, set the variable before calling
     * this method
     */
    private static void processProfileSnapshot() {
        if (profileSnapshot != null) {
            Map<String, Object> data = profileSnapshot.getData();

            if (data != null) {
                Login.setProfile(Profile.from(data));
            }

            downloadProfileImage();
        }
    }

    /**
     * Handles when profile sync fails by calling the fail handler if not null
     */
    private static void onFailedProfileSync() {
        if (failProfileDownloadHandler != null)
            failProfileDownloadHandler.doAction();
    }

    /**
     * Handles when profile sync completes by calling the fail handler if not null
     */
    private static void onSucceededProfileSync() {
        if (successProfileDownloadHandler != null)
            successProfileDownloadHandler.doAction();
    }

    /**
     * This method syncs the profile from the firebase database and calls the appropriate handler if not null.
     * If you have a DocumentSnapshot already retrieved that you don't want to download again from Firebase, call setProfileSnapshot.
     * Note that after a call to this method, the profileSnapshot is set to null, so for subsequent calls, it will need to be set again.
     * @param onSuccess the handler to call when successfully completed
     * @param onFail the handler to call on a failure
     */
    public static void syncProfile(ActionHandler onSuccess, ActionHandler onFail) {
        successProfileDownloadHandler = onSuccess;
        failProfileDownloadHandler = onFail;
        downloadProfile();
        profileSnapshot = null;
    }

    /**
     * Call this method before syncProfile to use an already retrieved snapshot. The behaviour is
     * undefined if a document snapshot passed in is not representative of a profile document.
     * A call to syncProfile resets the profileSnapshot to null.
     * @param profileSnapshot the profile snapshot to set
     */
    public static void setProfileSnapshot(DocumentSnapshot profileSnapshot) {
        ProfileUtils.profileSnapshot = profileSnapshot;
    }
}
