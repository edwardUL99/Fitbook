package ie.ul.fitbook.recording;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import org.threeten.bp.Duration;
import org.threeten.bp.LocalDateTime;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import ie.ul.fitbook.sports.Sport;

/**
 * This class stores an activity that has been recorded
 */
public class RecordedActivity implements Parcelable {
    /**
     * The timestamp the recorded activity was created at
     */
    private final LocalDateTime timestamp;
    /**
     * The list of recorded locations for this activity
     */
    private final List<LatLng> recordedLocations;
    /**
     * This holds the duration the activity ran for
     */
    private final Duration recordedDuration;
    /**
     * The sport that the activity has been recorded for
     */
    private final Sport sport;
    /**
     * The distance in kilometres for this activity
     */
    private final float distance;
    /**
     * The average speed in km/h for this activity
     */
    private final float averageSpeed;
    /**
     * The elevation gain for this activity in metres
     */
    private final float elevationGain;
    /**
     * The number of calories burned by the user
     */
    private final int caloriesBurned;
    /**
     * The Firestore Id this recorded activity is stored in. An optional field for saving,
     * but it should be set when retrieving activities so that they can be deleted
     */
    private String firestoreId;

    /**
     * Pass this into user DB.getChildCollection to get recorded activities
     */
    public static final String ACTIVITIES_PATH = "activities";
    /**
     * The key to store the timestamp on firestore
     */
    public static final String TIMESTAMP_KEY = "timestamp";
    /**
     * The key to store the recorded locations on firestore
     */
    public static final String LOCATIONS_KEY = "recorded_locations";
    /**
     * The key to store the recorded duration on firestore
     */
    public static final String DURATION_KEY = "recorded_duration";
    /**
     * The key to store sport on firestore
     */
    public static final String SPORT_KEY = "sport";
    /**
     * The key to store distance on firestore
     */
    public static final String DISTANCE_KEY = "distance";
    /**
     * The key to store average speed on firestore
     */
    public static final String AVERAGE_SPEED_KEY = "average_speed";
    /**
     * The key to store elevation gain on firestore
     */
    public static final String ELEVATION_GAIN_KEY = "elevation_gain";
    /**
     * The key to store calories burned on firestore
     */
    public static final String CALORIES_BURNED_KEY = "calories_burned";

    /**
     * Construct a RecordedActivity object to represent an activity that has been recorded
     * @param recordedLocations the array list of locations that were recorded
     * @param recordedDuration the duration of the activity
     * @param sport the sport this activity was recorded for
     * @param distance the distance this activity covered in kilometres
     * @param averageSpeed the average speed of this activity in km/h
     * @param elevationGain the elevation gained for this activity in metres
     * @param caloriesBurned the number of calories burned for this activity
     */
    public RecordedActivity(List<LatLng> recordedLocations, Duration recordedDuration,
                            Sport sport, float distance, float averageSpeed, float elevationGain, int caloriesBurned) {
        this(LocalDateTime.now(), recordedLocations, recordedDuration, sport, distance, averageSpeed, elevationGain, caloriesBurned);
    }

    /**
     * Construct a RecordedActivity object to represent an activity that has been recorded
     * @param timestamp the timestamp this activity was created at
     * @param recordedLocations the array list of locations that were recorded
     * @param recordedDuration the duration of the activity
     * @param sport the sport this activity was recorded for
     * @param distance the distance this activity covered in kilometres
     * @param averageSpeed the average speed of this activity in km/h
     * @param elevationGain the elevation gained for this activity in metres
     * @param caloriesBurned the number of calories burned for this activity
     */
    protected RecordedActivity(LocalDateTime timestamp, List<LatLng> recordedLocations, Duration recordedDuration,
                               Sport sport, float distance, float averageSpeed, float elevationGain, int caloriesBurned) {
        this.timestamp = timestamp;
        this.recordedLocations = recordedLocations;
        this.recordedDuration = recordedDuration;
        this.sport = sport;
        this.distance = distance;
        this.averageSpeed = averageSpeed;
        this.elevationGain = elevationGain;
        this.caloriesBurned = caloriesBurned;
    }

    /**
     * Construct the activity from the parcel
     * @param in the parcel to construct the activity from
     */
    protected RecordedActivity(Parcel in) {
        timestamp = LocalDateTime.parse(in.readString());
        recordedLocations = in.createTypedArrayList(LatLng.CREATOR);
        recordedDuration = Duration.ofMillis(in.readLong());
        sport = Sport.convertToSport(in.readString());
        distance = in.readFloat();
        averageSpeed = in.readFloat();
        elevationGain = in.readFloat();
        caloriesBurned = in.readInt();
        firestoreId = in.readString();
    }

    public static final Creator<RecordedActivity> CREATOR = new Creator<RecordedActivity>() {
        @Override
        public RecordedActivity createFromParcel(Parcel in) {
            return new RecordedActivity(in);
        }

        @Override
        public RecordedActivity[] newArray(int size) {
            return new RecordedActivity[size];
        }
    };

    /**
     * Returns the timestamp of when this activity was created
     * @return the timestamp of activity creation
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Returns an unmodifiable list of the recorded locations for this activity
     * @return the list of recorded locations
     */
    public List<LatLng> getRecordedLocations() {
        return Collections.unmodifiableList(recordedLocations);
    }

    /**
     * Retrieve the recorded duration for this activity
     * @return the recorded duration
     */
    public Duration getRecordedDuration() {
        return recordedDuration;
    }

    /**
     * Retrieve the sport that this activity was recorded for
     * @return sport activity was recorded for
     */
    public Sport getSport() {
        return sport;
    }

    /**
     * Retrieve the distance this activity achieved in kilometres
     * @return the distance achieved
     */
    public float getDistance() {
        return distance;
    }

    /**
     * The average speed recorded in km/h
     * @return recorded average speed
     */
    public float getAverageSpeed() {
        return averageSpeed;
    }

    /**
     * Retrieves the elevation gain that has been recorded for this activity
     * @return elevation gain in metres
     */
    public float getElevationGain() {
        return elevationGain;
    }

    /**
     * Retrieves the number of calories burned for this activity
     * @return number of calories burned in this activity
     */
    public int getCaloriesBurned() {
        return caloriesBurned;
    }

    /**
     * Converts this RecordedActivity to a data object to store on FireStore.
     * Locations are stored as an encoded string returned from PolyUtils.encode
     * @return a map of the data to store
     */
    public Map<String, Object> toData() {
        Map<String, Object> data = new HashMap<>();

        data.put(TIMESTAMP_KEY, timestamp.toString());
        data.put(LOCATIONS_KEY, PolyUtil.encode(recordedLocations));
        data.put(DURATION_KEY, recordedDuration.toMillis());
        data.put(SPORT_KEY, sport.toString());
        data.put(DISTANCE_KEY, distance);
        data.put(AVERAGE_SPEED_KEY, averageSpeed);
        data.put(ELEVATION_GAIN_KEY, elevationGain);
        data.put(CALORIES_BURNED_KEY, caloriesBurned);

        return data;
    }

    /**
     * Converts the data to a RecordedActivity
     * @param data data to convert
     * @return the RecordedActivity object or null if an error occurs
     */
    public static RecordedActivity from(Map<String, Object> data) {
        Object timestampObj = data.get(TIMESTAMP_KEY);
        Object locationsObj = data.get(LOCATIONS_KEY);
        Object durationObj = data.get(DURATION_KEY);
        Object sportObj = data.get(SPORT_KEY);
        Object distanceObj = data.get(DISTANCE_KEY);
        Object avgSpeedObj = data.get(AVERAGE_SPEED_KEY);
        Object elevationObj = data.get(ELEVATION_GAIN_KEY);
        Object caloriesObj = data.get(CALORIES_BURNED_KEY);

        if (!(timestampObj instanceof String) || !(locationsObj instanceof String) || !(durationObj instanceof Long)
            || !(sportObj instanceof String) || !(distanceObj instanceof Double) || !(avgSpeedObj instanceof Double) || !(elevationObj instanceof Double)
            || !(caloriesObj instanceof Long))
            return null;

        LocalDateTime timestamp = LocalDateTime.parse((String)timestampObj);
        String encoded = (String)locationsObj;
        List<LatLng> locations = PolyUtil.decode(encoded);

        Duration duration = Duration.ofMillis((Long)durationObj);
        Sport sport = Sport.convertToSport((String)sportObj);
        double distance = (Double)distanceObj;
        double avgSpeed = (Double)avgSpeedObj;
        double elevation = (Double)elevationObj;
        long calories = (Long)caloriesObj;

        return new RecordedActivity(timestamp, locations, duration, sport, (float)distance, (float)avgSpeed, (float)elevation, (int)calories);
    }

    /**
     * Describe the kinds of special objects contained in this Parcelable
     * instance's marshaled representation. For example, if the object will
     * include a file descriptor in the output of {@link #writeToParcel(Parcel, int)},
     * the return value of this method must include the
     * {@link #CONTENTS_FILE_DESCRIPTOR} bit.
     *
     * @return a bitmask indicating the set of special object types marshaled
     * by this Parcelable object instance.
     */
    @Override
    public int describeContents() {
        return hashCode();
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(timestamp.toString());
        dest.writeTypedList(recordedLocations);
        dest.writeLong(recordedDuration.toMillis());
        dest.writeString(sport.toString());
        dest.writeFloat(distance);
        dest.writeFloat(averageSpeed);
        dest.writeFloat(elevationGain);
        dest.writeInt(caloriesBurned);
        dest.writeString(firestoreId);
    }

    /**
     * Retrieve the Firestore ID for the document related to this object
     * @return firestore document ID
     */
    public String getFirestoreId() {
        return firestoreId;
    }

    /**
     * Sets the Firestore document id referring to this RecordedActivity.
     * For a save, this is optional but if retrieving from Firestore, it should be set so that activities can
     * be deleted.
     * @param firestoreId the document ID
     */
    public void setFirestoreId(String firestoreId) {
        this.firestoreId = firestoreId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecordedActivity activity = (RecordedActivity) o;
        return Float.compare(activity.distance, distance) == 0 &&
                Float.compare(activity.averageSpeed, averageSpeed) == 0 &&
                Float.compare(activity.elevationGain, elevationGain) == 0 &&
                caloriesBurned == activity.caloriesBurned &&
                timestamp.equals(activity.timestamp) &&
                recordedLocations.equals(activity.recordedLocations) &&
                recordedDuration.equals(activity.recordedDuration) &&
                sport == activity.sport &&
                Objects.equals(firestoreId, activity.firestoreId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, recordedLocations, recordedDuration, sport, distance, averageSpeed, elevationGain, caloriesBurned, firestoreId);
    }
}
