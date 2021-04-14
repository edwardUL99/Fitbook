package ie.ul.fitbook.utils;


import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Source;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.Map;

import ie.ul.fitbook.R;
import ie.ul.fitbook.database.UserDatabase;
import ie.ul.fitbook.interfaces.ActionHandler;
import ie.ul.fitbook.interfaces.ActionHandlerConsumer;
import ie.ul.fitbook.login.Login;
import ie.ul.fitbook.network.NetworkUtils;
import ie.ul.fitbook.profile.Profile;
import ie.ul.fitbook.storage.UserStorage;

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
        Log.i("Fitbook" ,"getProfileImageLocation: reached get profile image location");

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
     * @param context the context to download the profile image with
     * @param imageView the image view to download into
     */
    private static void downloadProfileImage(Context context, ImageView imageView) {
        if (imageView != null) {
            File file = getProfileImageLocation(context);

            if (file == null)
                return;

            StorageReference storageReference = new UserStorage().getChildFolder(Profile.PROFILE_IMAGE_PATH);

            if (NetworkUtils.isNetworkConnected(context)) {
                storageReference.getFile(file)
                        .addOnSuccessListener(success -> {
                            Bitmap bitmap = Utils.getBitmapFromFile(context, Uri.fromFile(file));
                            Profile profile = Login.getProfile(); // profile may have became null by the time download finished

                            if (profile != null)
                                profile.setProfileImage(bitmap);

                            if (imageView != null) {
                                imageView.setImageBitmap(bitmap);
                            }
                        })
                        .addOnFailureListener(failed -> {
                            failed.printStackTrace();
                            onFailedProfilePhotoDownload(context, Login.getProfile(), imageView);
                        });
            } else {
                onFailedProfileSync();
            }
        }
    }

    /**
     * Handles when the profile photo could not be downloaded successfully
     * @param context the context the photo was being downloaded with
     * @param profile the profile to download it into
     * @param imageView the image view to also download the profile photo into
     */
    private static void onFailedProfilePhotoDownload(Context context, Profile profile, ImageView imageView) {
        Bitmap defaultPhoto = Utils.drawableToBitmap(context, R.drawable.profile);
        if (profile != null)
            profile.setProfileImage(defaultPhoto);

        if (imageView != null)
            imageView.setImageBitmap(defaultPhoto);
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
     * @param imageView the ImageView to download the profile image into
     * @param onFail the handler for when it fails
     * @param context the context to download profile image with
     */
    private static void downloadUserProfileImage(Profile profile, String userId, ImageView imageView,
                                                 ActionHandler onFail, Context context) {
        downloadUserProfileImageSync(profile, userId, imageView, null, onFail, context);
    }

    /**
     * Downloads a profile image for a different profile synchronously, i.e. waits for image to be downloaded before executing success handler
     * @param profile the profile to download the image for
     * @param userId the User ID of the user to download the profile
     * @param imageView the ImageView to download the profile image into
     * @param onSuccess the profile on success to execute when the image is downloaded. If null, this essentially acts as async download
     * @param onFail the handler for when it fails
     * @param context the context to download profile image with
     */
    private static void downloadUserProfileImageSync(Profile profile, String userId, ImageView imageView, ActionHandlerConsumer<Profile> onSuccess,
                                                 ActionHandler onFail, Context context) {
        File file = getUserProfileImageLocation(context, userId);

        if (file == null)
            return;

        file.deleteOnExit(); // we don't want this photo anymore when application closes

        if (file.exists()) {
            Bitmap bitmap = Utils.getBitmapFromFile(context, Uri.fromFile(file));
            profile.setProfileImage(bitmap);
            if (imageView != null)
                imageView.setImageBitmap(bitmap);

            if (onSuccess != null)
                onSuccess.doAction(profile);
        } else {
            StorageReference storageReference = new UserStorage(userId).getChildFolder(Profile.PROFILE_IMAGE_PATH);

            if (NetworkUtils.isNetworkConnected(context)) {
                storageReference.getFile(file)
                        .addOnSuccessListener(success -> {
                            Bitmap bitmap = Utils.getBitmapFromFile(context, Uri.fromFile(file));
                            profile.setProfileImage(bitmap);

                            if (imageView != null)
                                imageView.setImageBitmap(bitmap);

                            if (onSuccess != null)
                                onSuccess.doAction(profile);
                        })
                        .addOnFailureListener(failed -> {
                            failed.printStackTrace();
                            onFailedProfilePhotoDownload(context, profile, imageView);
                            if (onSuccess != null)
                                onSuccess.doAction(profile);
                        });
            } else {
                if (onFail != null)
                    onFail.doAction();
            }
        }
    }


    /**
     * This method downloads the profile in the background.
     * It also downloads the profile picture in the background
     * @param context the context to download the profile with
     * @param imageView image view to download profile picture into
     */
    private static void downloadProfile(Context context, ImageView imageView) {
        if (profileSnapshot == null) {
            DocumentReference documentReference = new UserDatabase()
                    .getChildDocument(Profile.PROFILE_DOCUMENT);
            documentReference.get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            profileSnapshot = task.getResult();
                            processProfileSnapshot(context, imageView);
                        } else {
                            Exception exception = task.getException();
                            if (exception != null)
                                exception.printStackTrace();

                            onFailedProfileSync();
                        }
                    })
                    .addOnFailureListener(task -> onFailedProfileSync());
        } else {
            processProfileSnapshot(context, imageView);
        }
    }

    /**
     * Download the profile of a user with the specified userID
     * @param userId the ID of the user to download
     * @param onSuccess the handler for when the profile is downloaded successfully
     * @param onFail the handler for when thr profile fails to be downloaded
     * @param imageView the Image view we want to set the profile image of
     * @param context the context to download the profile with
     */
    public static void downloadProfile(String userId, ActionHandlerConsumer<Profile> onSuccess,
                                       ActionHandler onFail, ImageView imageView, Context context) {
        downloadProfile(userId, onSuccess, onFail, imageView, context, true);
    }

    /**
     * Download the profile of a user with the specified userID
     * @param userId the ID of the user to download
     * @param onSuccess the handler for when the profile is downloaded successfully
     * @param onFail the handler for when thr profile fails to be downloaded
     * @param imageView the Image view we want to set the profile image of
     * @param context the context to download the profile with
     * @param profileImageAsync true to download image into the image view asynchronously
     */
    public static void downloadProfile(String userId, ActionHandlerConsumer<Profile> onSuccess,
                                       ActionHandler onFail, ImageView imageView,
                                       Context context, boolean profileImageAsync) {
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

                                if (profileImageAsync) {
                                    downloadUserProfileImage(profile, userId, imageView, onFail, context);
                                    if (onSuccess != null)
                                        onSuccess.doAction(profile);
                                } else {
                                    downloadUserProfileImageSync(profile, userId, imageView, onSuccess, onFail, context);
                                }
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
     * @param context the context to process the snapshot with
     * @param imageView the image view to download a profile picture into
     */
    private static void processProfileSnapshot(Context context, ImageView imageView) {
        if (profileSnapshot != null) {
            Map<String, Object> data = profileSnapshot.getData();

            if (data != null) {
                Login.setProfile(Profile.from(data));
            }

            downloadProfileImage(context, imageView);
            onSucceededProfileSync();
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
     * @param context the context to sync the profile with
     * @param onSuccess the handler to call when successfully completed
     * @param onFail the handler to call on a failure
     * @param imageView an image view to insert the image into. Can be null
     */
    public static void syncProfile(Context context, ActionHandler onSuccess, ActionHandler onFail, ImageView imageView) {
        successProfileDownloadHandler = onSuccess;
        failProfileDownloadHandler = onFail;
        downloadProfile(context, imageView);
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

    /**
     * Retrieve a pre-loaded document snapshot if set and a call to syncProfile has not been made.
     * @return document snapshot representing user's profile, null if not set
     */
    public static DocumentSnapshot getProfileSnapshot() {
        return profileSnapshot;
    }
}
