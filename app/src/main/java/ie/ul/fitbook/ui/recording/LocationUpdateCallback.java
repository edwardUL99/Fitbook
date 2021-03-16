package ie.ul.fitbook.ui.recording;

import android.content.Context;
import android.location.Location;

import androidx.annotation.NonNull;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;

import java.util.HashMap;

import ie.ul.fitbook.interfaces.ActionHandlerConsumer;

/**
 * A callback for handling location updates
 */
public class LocationUpdateCallback extends LocationCallback {
    /**
     * The context for this callback
     */
    private final Context context;
    /**
     * The handler for the location result
     */
    private ActionHandlerConsumer<Location> locationHandler;
    /**
     * A boolean flag to determine if this callback is listening for updates
     */
    private boolean listening;
    /**
     * A HashMap consisting of callbacks defined for the given contexts
     */
    private static final HashMap<Context, LocationUpdateCallback> CONTEXT_CALLBACKS = new HashMap<>();

    /**
     * Constructs a LocationUpdateCallback for the provided context
     * @param context the context to register this callback with
     */
    private LocationUpdateCallback(Context context) {
        this.context = context;
        listening = true;
    }

    /**
     * Sets the location handler to handler the location results
     * @param locationHandler the location handler for processing results
     */
    public void setLocationHandler(ActionHandlerConsumer<Location> locationHandler) {
        this.locationHandler = locationHandler;
    }

    /**
     * Stops this callback from receiving location updates and removes it from the registered callbacks for its context
     */
    public void stopListening() {
        listening = false;
    }

    /**
     * Processes the location result using the handler, if not null on each location in the result.
     * If stopListening() has been called, this is a no-op
     * @param locationResult the location result to process
     */
    @Override
    public void onLocationResult(@NonNull LocationResult locationResult) {
        if (listening) {
            if (locationHandler != null) {
                for (Location location : locationResult.getLocations()) {
                    locationHandler.doAction(location);
                }
            }
        }
    }

    /**
     * Gets the instance of location callback registered for the provided context
     * @param context the context to retrieve the instance for
     * @return the instance of LOcationUpdateCallback for the context
     */
    public static LocationUpdateCallback getInstance(Context context) {
        LocationUpdateCallback locationUpdateCallback = CONTEXT_CALLBACKS.get(context);
        return locationUpdateCallback == null ? new LocationUpdateCallback(context):locationUpdateCallback;
    }
}
