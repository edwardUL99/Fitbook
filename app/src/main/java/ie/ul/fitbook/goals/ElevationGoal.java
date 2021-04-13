package ie.ul.fitbook.goals;

import android.os.Parcel;

import org.threeten.bp.LocalDateTime;

import java.util.HashMap;
import java.util.Map;

import ie.ul.fitbook.login.Login;
import ie.ul.fitbook.sports.Sport;

/**
 * This class represents a goal for elevation
 */
public final class ElevationGoal extends Goal {
    /**
     * The target value for this goal
     */
    private Integer targetValue;
    /**
     * The achieved value for this goal
     */
    private Integer achievedValue;

    /**
     * THe creator for our elevation goal
     */
    public static final Creator<ElevationGoal> CREATOR = new Creator<ElevationGoal>() {
        @Override
        public ElevationGoal createFromParcel(Parcel source) {
            return new ElevationGoal(source);
        }

        @Override
        public ElevationGoal[] newArray(int size) {
            return new ElevationGoal[size];
        }
    };

    /**
     * Constructs a goal instance with the provided parameters
     *
     * @param userId     the ID of the user this goal belongs to
     * @param sport      the sport this goal is to be used for
     * @param targetDate the date/time this goal should be completed by
     * @param targetValue the target value in km for this goal in metres
     */
    public ElevationGoal(String userId, Sport sport, LocalDateTime targetDate, Integer targetValue) {
        this(userId, sport, targetDate, targetValue, 0);
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
    public ElevationGoal(String userId, Sport sport, LocalDateTime targetDate, Integer targetValue, Integer achievedValue) {
        super(userId, sport, GoalType.ELEVATION, targetDate);
        setTargetValue(targetValue);
        this.achievedValue = achievedValue;
    }

    /**
     * Constructs an ElevationGoal from the provided parcel
     * @param in the parcel to construct from
     */
    public ElevationGoal(Parcel in) {
        super(in);
        targetValue = in.readInt();
        achievedValue = in.readInt();
    }

    /**
     * Returns the object representing the target value for this goal.
     * This could be an Integer for distance and elevation or it could be a time for a time goal
     *
     * @return the object representing the target value
     */
    @Override
    public Integer getTargetValue() {
        return targetValue;
    }

    /**
     * Sets the target value for this goal.
     *
     * @param targetValue the object representing the target value. The implementing classes have
     *                    the responsibility of making sure the targetValue is the correct instance and value for that type
     */
    @Override
    public void setTargetValue(Object targetValue) {
        if (!(targetValue instanceof Integer))
            throw new IllegalArgumentException("The value provided to ElevationGoal#setTargetValue should be an Integer");

        Integer intTarget = (Integer)targetValue;
        if (intTarget <= 0)
            throw new IllegalArgumentException("The value provided needs to be greater than 0");

        this.targetValue = intTarget;
    }

    /**
     * Retrieves the achieved value for this goal
     *
     * @return the value that has been achieved so far
     */
    @Override
    public Integer getAchievedValue() {
        return achievedValue;
    }

    /**
     * Checks the type of the value to ensure it is correct type
     * @param value the value to check
     */
    private void checkValueType(Object value) {
        if (!(value instanceof Integer))
            throw new IllegalArgumentException("The value provided should be an instance of Integer");
    }

    /**
     * Adds the given value to the achieved value. For ElevationGoal, value should be an Integer
     *
     * @param value the value to add
     */
    @Override
    public void addAchievedValue(Object value) {
        checkExpiration();
        checkValueType(value);
        Integer integer = (Integer)value;
        int total = achievedValue + integer;

        if (total < targetValue)
            achievedValue = total;
        else
            achievedValue = targetValue;
    }

    /**
     * Subtracts the given value from the achieved value. For ElevationGoal, value should be an Integer.
     * The achieved value will never go below 0.
     *
     * @param value the value to subtract
     */
    @Override
    public void subtractAchievedValue(Object value) {
        checkExpiration();
        checkValueType(value);
        Integer integer = (Integer)value;
        int total = achievedValue - integer;

        achievedValue = Math.max(total, 0);
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
        if (!(obj instanceof ElevationGoal))
            return false;

        if (obj == this) {
            return true;
        } else {
            ElevationGoal goal = (ElevationGoal) obj;
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
        data.put(Goal.TARGET_VALUE_KEY, targetValue);
        data.put(Goal.ACHIEVED_VALUE_KEY, achievedValue);

        return data;
    }

    /**
     * Converts the provided data object to an ElevationGoal
     * @param data the data to convert
     * @return corresponding elevation goal
     */
    public static ElevationGoal fromData(Map<String, Object> data) {
        checkKeysValidity(data);
        String sport = (String)data.get(Goal.SPORT_KEY);
        String type = (String)data.get(Goal.TYPE_KEY);
        String targetDate = (String)data.get(Goal.TARGET_DATE_KEY);

        GoalType goalType = GoalType.convertToGoalType(type);
        if (goalType != GoalType.ELEVATION)
            throw new IllegalArgumentException("Provided data does not represent an ELEVATION goal. Type provided is: " + goalType);

        Long targetLong = (Long)data.get(Goal.TARGET_VALUE_KEY);
        Long achievedLong = (Long)data.get(Goal.ACHIEVED_VALUE_KEY);

        if (targetLong == null || achievedLong == null || targetDate == null)
            throw new IllegalStateException("Expected fields are missing from the data object");


        Integer targetValue = Integer.parseInt(targetLong.toString());
        Integer achievedValue = Integer.parseInt(achievedLong.toString());

        return new ElevationGoal(Login.getUserId(), Sport.convertToSport(sport), LocalDateTime.parse(targetDate), targetValue, achievedValue);
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
        dest.writeInt(targetValue);
        dest.writeInt(achievedValue);
    }
}
