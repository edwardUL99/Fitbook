package ie.ul.fitbook.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;

/**
 * Provides a utility class for checking network connectivity
 */
public final class NetworkUtils {

    private NetworkUtils() {
        // prevent instantiation
    }

    /**
     * Checks if there is a network available
     * @param connectivityManager the connectivity manager to use
     * @return true if available, false if not
     */
    private static boolean isNetworkAvailable(ConnectivityManager connectivityManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();

            if (network == null) return false;
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            return  capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                                                || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
        } else {
            return connectivityManager.getActiveNetworkInfo() != null;
        }
    }

    /**
     * This method checks if the network is connected for the provided context
     * Note that if this returns false, wasInternetLost() will return true
     * @param context the context checking network connection
     * @return true if connected, false if not.
     */
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (isNetworkAvailable(cm)) {
            try {
                String command = "ping -c 1 google.ie"; // check if the connection is working
                boolean connected = (Runtime.getRuntime().exec(command).waitFor() == 0);

                if (connected)
                    return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return false;
    }
}
