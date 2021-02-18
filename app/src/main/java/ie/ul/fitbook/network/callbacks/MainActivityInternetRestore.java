package ie.ul.fitbook.network.callbacks;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.widget.Toast;

import ie.ul.fitbook.network.NetworkUtils;
import ie.ul.fitbook.ui.MainActivity;
import ie.ul.fitbook.utils.Utils;

/**
 * This class is intended to be registered by the MainActivity when it calls isNetworkConnected so that when
 * it is restored, the MainActivity is restarted
 */
public class MainActivityInternetRestore extends NetworkCallBack {
    /**
     * Constructs a MainActivityInternetRestore
     */
    public MainActivityInternetRestore() {
        super(new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build());
    }

    @Override
    public void onAvailable(Network network) {
        if (NetworkUtils.wasInternetLost()) {
            doAction();
        }
    }

    /**
     * Enables the callback for the provided callback
     *
     * @param context the context to enable it for
     */
    @Override
    public void enable(Context context) {
        if (!(context instanceof MainActivity))
            throw new IllegalArgumentException("The only context applicable to this NetworkCallBack is MainActivity");

        super.enable(context);
    }

    /**
     * This method performs the action this callback is required to do
     */
    @Override
    public void doAction() {
        if (context == null)
            throw new IllegalStateException("This NetworkCallBack has not been enabled. Use enable(context)");

        Toast.makeText(context, "Internet connection restored", Toast.LENGTH_SHORT)
                .show();

        Utils.downloadProfileImageBackground();

        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        connectivityManager.unregisterNetworkCallback(this);
    }
}
