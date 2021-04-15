package ie.ul.fitbook.recording.services;

import com.google.android.gms.maps.model.LatLng;

/**
 * This class is an immutable snapshot of a location recorded at a specific point in time.
 * It contains all fields of interest that have been recorded by the RecordingService
 */
public final class RecordedLocation {
    /**
     * The distance that has been recorded so far at this point in time in metres
     */
    private final float distance;
    /**
     * The speed that has been recorded at this point in time in km/h
     */
    private final float speed;
    /**
     * The average speed that has been recorded so far in km/h
     */
    private final float averageSpeed;
    /**
     * The elevation gained so far in metres
     */
    private final double elevation;
    /**
     * The Latitude and Longitude object recorded. It may be null if the location recorded was not accurate enough
     */
    private final LatLng latLng;

    /**
     * Constructs a RecordedLocation object with the provided parameters
     * @param distance the distance that has been recorded so far in metres
     * @param speed the speed that has been recorded at this point in time in km/h
     * @param averageSpeed the average speed that has been recorded so far in km/h
     * @param elevation the elevation gained so far in metres
     * @param latLng the latitude and longitude object for the location recorded. Can be null if the location wasn't accurate enough
     */
    protected RecordedLocation(float distance, float speed, float averageSpeed, double elevation, LatLng latLng) {
        this.distance = distance;
        this.speed = speed;
        this.averageSpeed = averageSpeed;
        this.elevation = elevation;
        this.latLng = latLng;
    }

    /**
     * Retrieves the distance achieved so far in metres
     * @return the distance achieved so far
     */
    public float getDistance() {
        return distance;
    }

    /**
     * The speed that has been recorded at this point of time
     * @return the speed recorded in km/h
     */
    public float getSpeed() {
        return speed;
    }

    /**
     * The average speed achieved so far
     * @return average speed in km/h
     */
    public float getAverageSpeed() {
        return averageSpeed;
    }

    /**
     * The elevation gained so far
     * @return the elevation gained in metres
     */
    public double getElevation() {
        return elevation;
    }

    /**
     * Gets the latitude and longitude object recorded by this location.
     * Can be null if the location recorded wasn't accurate enough
     * @return latlng object recorded. Can be null
     */
    public LatLng getLatLng() {
        return latLng;
    }
}
