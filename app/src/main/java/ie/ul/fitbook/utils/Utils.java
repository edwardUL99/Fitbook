package ie.ul.fitbook.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import org.threeten.bp.Duration;

import java.util.Locale;

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

    /**
     * Converts the provided hours and minutes to a Duration object.
     * This method does not check if the values are valid.
     * @param hours the hours for the duration
     * @param minutes the minutes for the duration
     * @return duration representing the hours and minutes
     */
    public static Duration hoursMinutesToDuration(int hours, int minutes) {
        int hoursInMins = hours * 60;
        minutes += hoursInMins;
        long millis = minutes * 60000;

        return Duration.ofMillis(millis);
    }

    /**
     * Converts duration to hours minutes string in format Xh YYm
     * @param duration duration to convert
     * @return duration in hours minutes
     */
    public static String durationToHoursMinutes(Duration duration) {
        long seconds = Math.abs(duration.getSeconds());

        return String.format(Locale.getDefault(),"%dh %02dm", seconds / 3600, (seconds % 3600) / 60);
    }
}
