package ie.ul.fitbook.goals;

import android.os.Parcel;

import org.threeten.bp.Duration;
import org.threeten.bp.LocalDateTime;

import java.util.HashMap;
import java.util.Map;

import ie.ul.fitbook.login.Login;
import ie.ul.fitbook.sports.Sport;

/**
 * This class represents a goal for time
 */
public class TimeGoal extends Goal {
    /**
     * The target value for this goal. This is stored as milliseconds. When you retrieve the millis from
     * Firestore do, Duration.of(millis)
     */
    private Duration targetValue;
    /**
     * The achieved value
     */
    private Duration achievedValue;

    /**
     * The creator to create our TimeGoal from Parcelable
     */
    public static final Creator<TimeGoal> CREATOR = new Creator<TimeGoal>() {
        @Override
        public TimeGoal createFromParcel(Parcel source) {
            return new TimeGoal(source);
        }

        @Override
        public TimeGoal[] newArray(int size) {
            return new TimeGoal[size];
        }
    };

    /**
     * Constructs a goal instance with the provided parameters
     *
     * @param userId     the ID of the user this goal belongs to
     * @param sport      the sport this goal is to be used for
     * @param targetDate the date/time this goal should be completed by
     * @param targetValue the target value in km for this goal
     */
    public TimeGoal(String userId, Sport sport, LocalDateTime targetDate, Duration targetValue) {
        this(userId, sport, targetDate, targetValue, Duration.ZERO);
    }

    /**
     * Constructs a goal instance with the provided parameters
     *
     * @param userId     the ID of the user this goal belongs to
     * @param sport      the sport this goal is to be used for
     * @param targetDate the date/time this goal should be completed by
     * @param targetValue the target value in km for this goal
     * @param achievedValue achieved value of this goal if already exists
     */
    public TimeGoal(String userId, Sport sport, LocalDateTime targetDate, Duration targetValue, Duration achievedValue) {
        super(userId, sport, GoalType.TIME, targetDate);
        setTargetValue(targetValue);
        this.achievedValue = achievedValue;
    }

    /**
     * Constructs a TimeGoal from the provided parcel
     * @param in the parcel to construct the TimeGoal from
     */
    public TimeGoal(Parcel in) {
        super(in);
        targetValue = Duration.ofMillis(in.readLong());
        achievedValue = Duration.ofMillis(in.readLong());
    }

    /**
     * Returns the object representing the target value for this goal.
     * This could be an Integer for distance and elevation or it could be a time for a time goal
     *
     * @return the object representing the target value
     */
    @Override
    public Duration getTargetValue() {
        return targetValue;
    }

    /**
     * Sets the target value for this goal. The duration shouldn't be 0
     *
     * @param targetValue the object representing the target value. The implementing classes have
     *                    the responsibility of making sure the targetValue is the correct instance and value for that type
     */
    @Override
    public void setTargetValue(Object targetValue) {
        if (!(targetValue instanceof Duration))
            throw new IllegalArgumentException("The value provided to Duration#setTargetValue should be a Duration");

        Duration duration = (Duration)targetValue;
        if (duration.isZero())
            throw new IllegalArgumentException("The value provided shouldn't be an empty duration");

        this.targetValue = duration;
    }

    /**
     * Retrieves the achieved value for this goal
     *
     * @return the value that has been achieved so far
     */
    @Override
    public Duration getAchievedValue() {
        return achievedValue;
    }

    /**
     * Checks the type of the value to ensure it is correct type
     * @param value the value to check
     */
    private void checkValueType(Object value) {
        if (!(value instanceof Duration))
            throw new IllegalArgumentException("The value provided should be an instance of Duration");
    }

    /**
     * Adds the given value to the achieved value. For TimeGoal, the value should be a Duration
     *
     * @param value the value to add
     */
    @Override
    public void addAchievedValue(Object value) {
        checkExpiration();
        checkValueType(value);
        Duration duration = (Duration)value;

        Duration total = achievedValue.plus(duration);

        if (total.compareTo(achievedValue) < 0)
            achievedValue = total;
        else
            achievedValue = targetValue;
    }

    /**
     * Subtracts the given value from the achieved value. For TimeGoal, the value should be a Duration.
     * The achieved value will never go below 0.
     *
     * @param value the value to subtract
     */
    @Override
    public void subtractAchievedValue(Object value) {
        checkExpiration();
        checkValueType(value);
        Duration duration = (Duration)value;

        Duration total = achievedValue.minus(duration);

        if (total.compareTo(Duration.ZERO) < 0)
            achievedValue = Duration.ZERO;
        else
            achievedValue = total;
    }

    /**
     * Returns true if this goal has been completed or not
     *
     * @return true if completed
     */
    @Override
    public boolean isCompleted() {
        return achievedValue.equals(targetValue);
    }

    /**
     * Return the hash code of this Goal. Required to be implemented by sub-classes
     *
     * @return the hash code of this goal
     */
    @Override
    public int hashCode() {
        return userId.hashCode()
                + sport.hashCode()
                + type.hashCode()
                + targetDate.hashCode()
                + targetValue.hashCode()
                + achievedValue.hashCode();
    }

    /**
     * Check if this goal equals the object provided. Required to be implemented by sub-classes
     *
     * @param obj the object to check equality with
     * @return true if equal, false if not
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TimeGoal))
            return false;

        if (obj == this) {
            return true;
        } else {
            TimeGoal goal = (TimeGoal) obj;
            return userId.equals(goal.userId)
                    && sport == goal.sport
                    && type == goal.type
                    && targetDate.equals(goal.targetDate)
                    && targetValue.equals(goal.targetValue)
                    && achievedValue.equals(goal.achievedValue);
        }
    }

    /**
     * Converts the goal to a mapping of the String keys to its data
     *
     * @return this object in a mapped form that can be used with a Firestore document
     */
    @Override
    public Map<String, Object> toData() {
        Map<String, Object> data = new HashMap<>();

        data.put(Goal.SPORT_KEY, sport.toString());
        data.put(Goal.TYPE_KEY, type.toString());
        data.put(Goal.TARGET_DATE_KEY, targetDate.toString());
        data.put(Goal.TARGET_VALUE_KEY, targetValue.toMillis());
        data.put(Goal.ACHIEVED_VALUE_KEY, achievedValue.toMillis());

        return data;
    }

    /**
     * Converts the provided data object to a TimeGoal
     * @param data the data to convert
     * @return corresponding time goal
     */
    public static TimeGoal fromData(Map<String, Object> data) {
        checkKeysValidity(data);
        String sport = (String)data.get(Goal.SPORT_KEY);
        String type = (String)data.get(Goal.TYPE_KEY);
        String targetDate = (String)data.get(Goal.TARGET_DATE_KEY);

        GoalType goalType = GoalType.convertToGoalType(type);
        if (goalType != GoalType.TIME)
            throw new IllegalArgumentException("Provided data does not represent a TIME goal. Type provided is: " + goalType);

        Long targetLong = (Long)data.get(Goal.TARGET_VALUE_KEY); // target date in millis
        Long achievedLong = (Long)data.get(Goal.ACHIEVED_VALUE_KEY); // achieved value is also in millis

        if (targetLong == null || achievedLong == null || targetDate == null)
            throw new IllegalStateException("Expected fields are missing from the data object");

        return new TimeGoal(Login.getUserId(), Sport.convertToSport(sport), LocalDateTime.parse(targetDate), Duration.ofMillis(targetLong), Duration.ofMillis(achievedLong));
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
     * Flatten this object in to a Parcel. Sub-classes should call this first to add their own fields to the
     * destination
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeLong(targetValue.toMillis());
        dest.writeLong(achievedValue.toMillis());
    }
}
