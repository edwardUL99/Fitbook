package ie.ul.fitbook.goals;


import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentReference;

import org.threeten.bp.LocalDateTime;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import ie.ul.fitbook.sports.Sport;

/**
 * This class represents a goal for a specified user.
 * Goals, therefore, reside within a users collection in the /users database
 */
public abstract class Goal implements Parcelable {
    /**
     * The reference to the document representing this Goal in storage
     */
    protected DocumentReference documentReference;
    /**
     * The user ID of the user that this goal belongs to
     */
    protected String userId;
    /**
     * The sport that this goal represents
     */
    protected Sport sport;
    /**
     * The type of this goal.
     */
    protected final GoalType type;
    /**
     * The date and time this goal should be completed by
     */
    protected LocalDateTime targetDate;

    /**
     * The collection path inside the users uid folder of /users/uid
     */
    public static final String COLLECTION_PATH = "goals";
    /**
     * The key for storing the sport this goal is associated with
     */
    public static final String SPORT_KEY = "sport";
    /**
     * The key for storing the type this goal is of
     */
    public static final String TYPE_KEY = "type";
    /**
     * The key for storing the target date timestamp for this goal
     */
    public static final String TARGET_DATE_KEY = "target_date";
    /**
     * The for storing the target value for this goal
     */
    public static final String TARGET_VALUE_KEY = "target_value";
    /**
     * The key for storing the achieved value so far
     */
    public static final String ACHIEVED_VALUE_KEY = "achieved_value";

    /**
     * Constructs a goal instance with the provided parameters
     *
     * @param userId     the ID of the user this goal belongs to
     * @param sport      the sport this goal is to be used for
     * @param type       the type of the goal. This type should be used to parse the value retrieved from the database into the appropriate goal object
     * @param targetDate the date/time this goal should be completed by
     */
    protected Goal(String userId, Sport sport, GoalType type, LocalDateTime targetDate) {
        this.userId = userId;
        this.sport = sport;
        this.type = type;
        this.targetDate = targetDate;
    }

    /**
     * Construct this goal from the provided parcel
     * @param in the parcel to construct from
     */
    protected Goal(Parcel in) {
        userId = in.readString();
        sport = Sport.convertToSport(in.readString());
        type = GoalType.convertToGoalType(in.readString());
        targetDate = LocalDateTime.parse(in.readString());
    }

    /**
     * The document reference representing this goal in storage.
     * @return the document reference for this goal. May be null if it wasn't saved or retrieved from Firestore
     */
    public DocumentReference getDocumentReference() {
        return documentReference;
    }

    /**
     * Sets the document reference for this goal. It does not check if this is a valid goal reference,
     * the responsibility of that is left to the caller of this method.
     *
     * This should be set when saving the goal to the Firestore or on retrieval of this goal from it
     * @param documentReference the document to set.
     */
    public void setDocumentReference(@NonNull DocumentReference documentReference) {
        this.documentReference = documentReference;
    }

    /**
     * Retrieves the ID of this document in firebase. This may be null, unless setDocumentId has been called
     *
     * @return the ID of the firebase document
     */
    public String getDocumentId() {
        if (documentReference != null)
            return documentReference.getId();

        return null;
    }

    /**
     * Retrieves the user id of the user that this goal belongs to
     *
     * @return the user ID of the user this goal belongs to
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Changes the user who owns this goal
     *
     * @param userId the ID of the user that will own this goal
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Retrieves the sport this goal is set for
     *
     * @return sport goal is set for
     */
    public Sport getSport() {
        return sport;
    }

    /**
     * Sets sport that this goal will be set for
     *
     * @param sport the sport this
     */
    public void setSport(Sport sport) {
        this.sport = sport;
    }

    /**
     * Gets the type of this goal
     *
     * @return the type of this goal
     */
    public GoalType getType() {
        return type;
    }

    /**
     * Gets the timestamp for when this goal is due
     *
     * @return the timestamp for when the goal is due
     */
    public LocalDateTime getTargetDate() {
        return targetDate;
    }

    /**
     * Sets the timestamp for when this goal should be due for
     *
     * @param targetDate the timestamp to set
     */
    public void setTargetDate(LocalDateTime targetDate) {
        this.targetDate = targetDate;
    }

    /**
     * Returns the object representing the target value for this goal.
     * This could be an Integer for distance and elevation or it could be a time for a time goal
     *
     * @return the object representing the target value
     */
    public abstract Object getTargetValue();

    /**
     * Sets the target value for this goal.
     *
     * @param targetValue the object representing the target value. The implementing classes have
     *                    the responsibility of making sure the targetValue is the correct instance and value for that type
     */
    public abstract void setTargetValue(Object targetValue);

    /**
     * Retrieves the achieved value for this goal
     * @return the value that has been achieved so far
     */
    public abstract Object getAchievedValue();

    /**
     * Checks if isExpired returns true and if so, throws UnsupportedOperationException
     */
    protected void checkExpiration() {
        if (isExpired())
            throw new UnsupportedOperationException("Cannot call this method when Goal#isExpired returns true");
    }

    /**
     * Adds the given value to the achieved value. This method shouldn't be called if isExpired() returns true.
     * The achieved value will never go above the target value.
     * @param value the value to add
     */
    public abstract void addAchievedValue(Object value);

    /**
     * Subtracts the given value from the achieved value. This method shouldn't be called if isExpired() returns true.
     * The achieved value will never go below 0.
     * @param value the value to subtract
     */
    public abstract void subtractAchievedValue(Object value);

    /**
     * Returns true if the current date and time has gone past the target date
     *
     * @return true if target date has been passed, false if not
     */
    public boolean isExpired() {
        return targetDate.isBefore(LocalDateTime.now()); // if the target timestamp is before the current timestamp, it has expired
    }

    /**
     * Returns true if this goal has been completed or not
     * @return true if completed
     */
    public abstract boolean isCompleted();

    /**
     * Return the hash code of this Goal. Required to be implemented by sub-classes
     *
     * @return the hash code of this goal
     */
    @Override
    public abstract int hashCode();

    /**
     * Check if this goal equals the object provided. Required to be implemented by sub-classes
     *
     * @param obj the object to check equality with
     * @return true if equal, false if not
     */
    @Override
    public abstract boolean equals(Object obj);

    /**
     * Converts the goal to a mapping of the String keys to its data
     *
     * @return this object in a mapped form that can be used with a Firestore document
     */
    public abstract Map<String, Object> toData();

    /**
     * Checks if the data object contains the valid keys for a goal object
     *
     * @param data the data to check
     */
    protected static void checkKeysValidity(Map<String, Object> data) {
        List<String> keys = Arrays.asList(SPORT_KEY, TYPE_KEY, TARGET_DATE_KEY, TARGET_VALUE_KEY, ACHIEVED_VALUE_KEY);

        for (String key : data.keySet()) {
            if (!keys.contains(key))
                throw new IllegalArgumentException("The provided data contains an invalid key: " + key + ". One of " + keys + " expected.");
        }
    }

    /**
     * Flatten this object in to a Parcel. Sub-classes should call this first to add their own fields to the
     * destination
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    @CallSuper
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userId);
        dest.writeString(sport.toString());
        dest.writeString(type.toString());
        dest.writeString(targetDate.toString());
    }
}
