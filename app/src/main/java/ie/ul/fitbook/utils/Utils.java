package ie.ul.fitbook.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import ie.ul.fitbook.database.Database;
import ie.ul.fitbook.database.Databases;
import ie.ul.fitbook.interfaces.ActionHandler;
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
            return null;
        }
    }

    /**
     * This method takes a string and capitalises the first letter, leaving the rest as lowercase
     * @param string the string to capitalise
     * @return capitalised string
     */
    public static String capitalise(String string) {
        int length = string.length();
        if (length == 0) {
            return string;
        } else if (length == 1) {
            return string.toUpperCase();
        } else {
            String[] split = string.split("\\s");

            StringBuilder result = new StringBuilder();
            for (String s : split) {
                s = ("" + s.charAt(0)).toUpperCase() + s.substring(1).toLowerCase();
                result.append(s).append(" ");
            }

            result.deleteCharAt(result.length() - 1);

            return result.toString();
        }
    }
}
