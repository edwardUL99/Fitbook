package ie.ul.fitbook.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.Objects;

import ie.ul.fitbook.login.Login;
import ie.ul.fitbook.network.NetworkUtils;
import ie.ul.fitbook.profile.Profile;
import ie.ul.fitbook.storage.Storage;
import ie.ul.fitbook.storage.Stores;
import ie.ul.fitbook.ui.MainActivity;

/**
 * This class provides various utility methods
 */
public final class Utils {
    /**
     * The name of our shared preferences file. This is where any key-value preferences will be stored
     */
    public static final String SHARED_PREFERENCES_FILE = "FitbookPrefs";

    private Utils() {
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

        if (!file.isDirectory() && !file.mkdir())
            return null;
        else
            return new File(file, "profile_pic.png");
    }

    /**
     * This gets the bitmap from the provided file
     * @param context the context requesting this conversion
     * @param imageURI the URI of the image
     * @return bitmap or null if an error occurred
     */
    public static Bitmap getBitmapFromFile(Context context, Uri imageURI) {
        try {
            if (Build.VERSION.SDK_INT < 28) {
                return MediaStore.Images.Media.getBitmap(context.getContentResolver(),
                        imageURI);
            } else {
                ImageDecoder.Source source = ImageDecoder.createSource(context.getContentResolver(), imageURI);
                return ImageDecoder.decodeBitmap(source);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * This method downloads the profile image in the background using the application context.
     * It is assumed that you have network connectivity
     */
    public static void downloadProfileImageBackground() {
        Profile profile = Login.getProfile();

        if (profile != null) {
            StorageReference storageReference = Objects.requireNonNull(Storage.getInstance(Stores.USERS)).getChildFolder(Profile.PROFILE_IMAGE_PATH);

            File file = Utils.getProfileImageLocation(MainActivity.APPLICATION_CONTEXT);

            if (file != null && NetworkUtils.isNetworkConnected(MainActivity.APPLICATION_CONTEXT)) {
                storageReference.getFile(file)
                        .addOnSuccessListener(success -> {
                            Bitmap bitmap = getBitmapFromFile(MainActivity.APPLICATION_CONTEXT, Uri.fromFile(file));
                            Profile profile1 = Login.getProfile(); // profile may have became null by the time download finished

                            if (profile1 != null)
                                profile1.setProfileImage(bitmap);
                        });
            }
        }
    }
}
