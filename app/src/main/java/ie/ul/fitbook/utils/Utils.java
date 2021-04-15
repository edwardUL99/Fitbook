package ie.ul.fitbook.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ImageDecoder;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.threeten.bp.Duration;

import java.util.Locale;

import ie.ul.fitbook.R;

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
     * Converts the hours, minutes, seconds to duration
     * @param hours the hours for the duration
     * @param minutes the minutes for the duration
     * @param seconds the seconds for the duration
     * @return duration representing the hours, minutes and seconds
     */
    public static Duration hoursMinutesSecondsToDuration(int hours, int minutes, int seconds) {
        Duration duration = hoursMinutesToDuration(hours, minutes);
        return duration.plusSeconds(seconds);
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

    /**
     * Retrieves a duration in hours:minutes:seconds
     * @param duration the duration to convert
     * @return formatted time string
     */
    public static String durationToHoursMinutesSeconds(Duration duration) {
        long seconds = duration.getSeconds();
        long absSeconds = Math.abs(seconds);
        String positive = String.format(Locale.getDefault(),
                "%02d:%02d:%02d",
                absSeconds / 3600,
                (absSeconds % 3600) / 60,
                absSeconds % 60);
        return seconds < 0 ? "-" + positive : positive;
    }

    /**
     * Converts drawable to bitmap
     * @param context the context to retrieve the bitmap with
     * @param drawableId the id of the drawable
     * @return bitmap of the drawable
     * @throws android.content.res.Resources.NotFoundException if the drawable can't be found
     */
    public static Bitmap drawableToBitmap(Context context, int drawableId) throws Resources.NotFoundException {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);

        if (drawable == null)
            throw new Resources.NotFoundException("The drawable with id " + drawableId + " cannot be found");

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        Bitmap bitmap;

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * Downloads an image into the provided image view
     * @param reference the firebase storage reference to the image
     * @param into the imageview to download the image into
     */
    public static void downloadImage(StorageReference reference, ImageView into) {
        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri downloadUrl) {
                String uri = downloadUrl.toString();
                Picasso.get()
                        .load(uri)
                        .placeholder(R.drawable.profile)
                        .error(R.drawable.profile)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(into, new Callback() {
                            @Override
                            public void onSuccess() {
                                // no-op
                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get()
                                        .load(uri)
                                        .into(into);
                            }
                        });
            }
        });
    }

}
