package ie.ul.fitbook.network.callbacks;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkRequest;

/**
 * This class provides a callback for registering with the connectivity manager
 */
public abstract class NetworkCallBack extends ConnectivityManager.NetworkCallback {
    /**
     * The network request to register for the callback
     */
    protected final NetworkRequest networkRequest;
    /**
     * The context this callback works on
     */
    protected Context context;

    /**
     * Creates a call back for the provided network request
     * @param networkRequest the network request to register
     */
    protected NetworkCallBack(NetworkRequest networkRequest) {
        this.networkRequest = networkRequest;
    }

    /**
     * Enables the callback for the provided callback
     * @param context the context to enable it for
     */
    public void enable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        connectivityManager.registerNetworkCallback(networkRequest, this);
        this.context = context;
    }

    /**
     * This method performs the action this callback is required to do
     */
    public abstract void doAction();
}
