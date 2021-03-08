package ie.ul.fitbook.statistics;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;

import org.threeten.bp.Duration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ie.ul.fitbook.interfaces.ActionHandlerConsumer;
import ie.ul.fitbook.sports.Sport;

/**
 * This class represents a single Weekly statistic unit (i.e. Distance, Time and Elevation)
 */
public class WeeklyStat {
    /**
     * The sport this weekly stat belongs to
     */
    private final Sport sport;
    /**
     * The distance for this week
     */
    private Double distance;
    /**
     * The time for this week
     */
    private Duration time;
    /**
     * The elevation gained for this week
     */
    private Integer elevation;
    /**
     * Key for storing distance in the database
     */
    private static final String DISTANCE_KEY = "distance";
    /**
     * Key for storing time in the database
     */
    private static final String TIME_KEY = "time";
    /**
     * Key for storing elevation in the database
     */
    private static final String ELEVATION_KEY = "elevation";
    /**
     * Key for storing sport in the database
     */
    private static final String SPORT_KEY = "sport";

    /**
     * Constructs a WeeklyStat with everything set to 0
     * @param sport the sport the stat belongs to
     */
    public WeeklyStat(Sport sport) {
        this(sport,0.0, Duration.ZERO, 0);
    }

    /**
     * Constructs a WeeklyStat with the provided initial value
     * @param sport the sport the stat belongs to
     * @param distance the distance value for this week
     * @param time the time value for this week
     * @param elevation the elevation value for this week
     */
    public WeeklyStat(Sport sport, Double distance, Duration time, Integer elevation) {
        this.sport = sport;
        this.distance = distance;
        this.time = time;
        this.elevation = elevation;
    }

    /**
     * Gets the sport this weekly statistic belongs to
     * @return the sport the stat belongs to
     */
    public Sport getSport() {
        return sport;
    }

    /**
     * Retrieves the distance for this week
     * @return distance for this week
     */
    public Double getDistance() {
        return distance;
    }

    /**
     * Retrieves the time for this week
     * @return time for this week
     */
    public Duration getTime() {
        return time;
    }

    /**
     * Retrieves the elevation for this week
     * @return elevation for this week
     */
    public Integer getElevation() {
        return elevation;
    }

    /**
     * Adds distance to this weekly statistic
     * @param distance the distance to add. If negative, nothing happens
     */
    public void addDistance(Double distance) {
        if (distance >= 0) {
            this.distance += distance;
        }
    }

    /**
     * Subtracts distance from this weekly statistic. Distance will never be below 0
     * @param distance distance to subtract. If negative, nothing happens
     */
    public void subtractDistance(Double distance) {
        if (distance >= 0) {
            this.distance -= distance;

            if (this.distance < 0.0)
                this.distance = 0.0;
        }
    }

    /**
     * Add this time onto this weekly time
     * @param time the time to add. If negative, nothing happens
     */
    public void addTime(Duration time) {
        if (!time.isNegative()) {
            this.time = this.time.plus(time);
        }
    }

    /**
     * Subtracts this time from this weekly time. Will never be below 0
     * @param time the time to subtract.
     */
    public void subtractTime(Duration time) {
        if (!time.isNegative()) {
            this.time = this.time.minus(time);

            if (this.time.compareTo(Duration.ZERO) < 0)
                this.time = Duration.ZERO;
        }
    }

    /**
     * Adds this elevation to the weekly elevation
     * @param elevation the elevation to add. If negative, nothing happens
     */
    public void addElevation(Integer elevation) {
        if (elevation >= 0) {
            this.elevation += elevation;
        }
    }

    /**
     * Subtracts this elevation from the from the weekly elevation
     * @param elevation the elevation to subtract
     */
    public void subtractElevation(Integer elevation) {
        if (elevation >= 0) {
            this.elevation -= elevation;

            if (this.elevation < 0)
                this.elevation = 0;
        }
    }

    /**
     * Saves this weekly stat using the provided DocumentReference
     * @param documentReference the document reference to save this to.
     * @param onSuccess the handler for when this is successful. Can be null
     * @param onFail the handler for when this isn't successful. Can be null
     */
    public void save(DocumentReference documentReference, ActionHandlerConsumer<Task<Void>> onSuccess, ActionHandlerConsumer<Exception> onFail) {
        Map<String, Object> data = new HashMap<>();

        data.put(DISTANCE_KEY, distance);
        data.put(TIME_KEY, time.toMillis());
        data.put(ELEVATION_KEY, elevation);
        data.put(SPORT_KEY, sport.toString());

        documentReference.set(data)
                .addOnCompleteListener(task -> {
                    if (onSuccess != null)
                        onSuccess.doAction(task);
                })
                .addOnFailureListener(task -> {
                    if (onFail != null)
                        onFail.doAction(task);
                });
    }

    /**
     * Checks if this data is valid
     * @param data the data to check
     */
    private static void checkKeyValidity(Map<String, Object> data) {
        List<String> keys = Arrays.asList(DISTANCE_KEY, TIME_KEY, ELEVATION_KEY, SPORT_KEY);

        for (String key : data.keySet()) {
            if (!keys.contains(key))
                throw new IllegalArgumentException("The provided data contains an invalid key: " + key + ". One of " + keys + " expected.");
        }
    }

    /**
     * Return an instance of WeeklyStat from the provided data
     * @param data the data to parse the WeeklyStat from
     * @return parsed WeeklyStat object
     */
    public static WeeklyStat from(Map<String, Object> data) {
        checkKeyValidity(data);

        Object sportValue = data.get(SPORT_KEY);
        Object distanceValue = data.get(DISTANCE_KEY);
        Object timeValue = data.get(TIME_KEY);
        Object elevationValue = data.get(ELEVATION_KEY);

        if (sportValue == null || distanceValue == null || timeValue == null || elevationValue == null)
            throw new IllegalArgumentException("The provided data is missing a key");

        double distance = Double.parseDouble(distanceValue.toString());
        long time = Long.parseLong(timeValue.toString());
        int elevation = Integer.parseInt(elevationValue.toString());

        return new WeeklyStat(Sport.convertToSport((String)sportValue), distance, Duration.ofMillis(time), elevation);
    }
}
