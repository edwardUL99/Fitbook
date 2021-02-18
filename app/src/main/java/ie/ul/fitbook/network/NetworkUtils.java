package ie.ul.fitbook.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;

import ie.ul.fitbook.network.callbacks.NetworkCallBack;

/**
 * Provides a utility class for checking network connectivity
 */
public final class NetworkUtils {
    /**
     * Tracks if there was no internet connection since the last call to isNetworkConnected
     */
    private static boolean internetConnectionLost;

    private NetworkUtils() {
        // prevent instantiation
    }

    /**
     * This method checks if the network is connected for the provided context, registering no callback
     * Note that if this returns false, wasInternetLost() will return true
     * @param context the context to check network connectivity for
     * @return true if connected, false if not
     */
    public static boolean isNetworkConnected(Context context) {
        return isNetworkConnected(context, null);
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
     * This method checks if the network is connected for the provided context and optionally an InternetConnectivityChecker if there is no connection.
     * Note that if this returns false, wasInternetLost() will return true
     * @param context the context checking network connection
     * @param callback the callback to register with the network for if you want to check some condition related to the state of the network after this check
     * @return true if connected, false if not.
     */
    public static boolean isNetworkConnected(Context context, NetworkCallBack callback) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (isNetworkAvailable(cm)) {
            try {
                String command = "ping -c 1 google.ie"; // check if the connection is working
                boolean connected = (Runtime.getRuntime().exec(command).waitFor() == 0);
                internetConnectionLost = !connected;

                return connected;
            } catch (Exception e) {
                internetConnectionLost = true;
            }
        } else {
            internetConnectionLost = true;
        }

        if (callback != null)
            callback.enable(context);

        return false;
    }

    /**
     * Returns whether since a last call to isNetworkConnected, it was found that there was no internet connection
     * @return true if there was no internet connection since the last call to isNetworkConnected, false if it was not lost
     */
    public static boolean wasInternetLost() {
        return internetConnectionLost;
    }
}
