package ie.ul.fitbook.goals;

import android.os.Parcel;

import org.threeten.bp.LocalDateTime;

import java.util.HashMap;
import java.util.Map;

import ie.ul.fitbook.login.Login;
import ie.ul.fitbook.sports.Sport;

/**
 * This class represents a goal for distance in km
 */
public final class DistanceGoal extends Goal {
    /**
     * The target value for this goal
     */
    private Double targetValue;
    /**
     * The achieved value for this goal
     */
    private Double achievedValue;

    /**
     * The creator class for a distance goal
     */
    public static final Creator<DistanceGoal> CREATOR = new Creator<DistanceGoal>() {
        @Override
        public DistanceGoal createFromParcel(Parcel source) {
            return new DistanceGoal(source);
        }

        @Override
        public DistanceGoal[] newArray(int size) {
            return new DistanceGoal[size];
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
    public DistanceGoal(String userId, Sport sport, LocalDateTime targetDate, Double targetValue) {
        this(userId, sport, targetDate, targetValue, 0.0);
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
    public DistanceGoal(String userId, Sport sport, LocalDateTime targetDate, Double targetValue, Double achievedValue) {
        super(userId, sport, GoalType.DISTANCE, targetDate);
        setTargetValue(targetValue);
        this.achievedValue = achievedValue;
    }

    /**
     * Constructs a DistanceGoal from the provided parcel
     * @param in the parcel to construct the goal from
     */
    public DistanceGoal(Parcel in) {
        super(in);
        targetValue = in.readDouble();
        achievedValue = in.readDouble();
    }

    /**
     * Returns the object representing the target value for this goal.
     * This could be an Integer for distance and elevation or it could be a time for a time goal
     *
     * @return the object representing the target value
     */
    @Override
    public Double getTargetValue() {
        return targetValue;
    }

    /**
     * Sets the target value for this goal. It needs to be greater than 0
     *
     * @param targetValue the object representing the target value. The implementing classes have
     *                    the responsibility of making sure the targetValue is the correct instance and value for that type
     */
    @Override
    public void setTargetValue(Object targetValue) {
        if (!(targetValue instanceof Double))
            throw new IllegalArgumentException("The value provided to DistanceGoal#setTargetValue should be an Integer");

        Double doubleTarget = (Double)targetValue;
        if (doubleTarget <= 0)
            throw new IllegalArgumentException("The value provided needs to be greater than 0");

        this.targetValue = doubleTarget;
    }

    /**
     * Retrieves the achieved value for this goal
     *
     * @return the value that has been achieved so far
     */
    @Override
    public Double getAchievedValue() {
        return achievedValue;
    }

    /**
     * Checks the type of the value to ensure it is correct type
     * @param value the value to check
     */
    private void checkValueType(Object value) {
        if (!(value instanceof Double))
            throw new IllegalArgumentException("The value provided should be an instance of Double");
    }

    /**
     * Adds the given value to the achieved value. For DistanceGoal, value should be an Integer.
     * The value will never go over the target value
     * @param value the value to add
     */
    @Override
    public void addAchievedValue(Object value) {
        checkExpiration();
        checkValueType(value);
        Double doubleVal = (Double)value;
        double total = achievedValue + doubleVal;

        if (total < targetValue)
            achievedValue = total;
        else
            achievedValue = targetValue;
    }

    /**
     * Subtracts the given value from the achieved value. For DistanceGoal, value should be an Integer.
     * The achieved value will never go below 0.
     *
     * @param value the value to subtract
     */
    @Override
    public void subtractAchievedValue(Object value) {
        checkExpiration();
        checkValueType(value);
        Double doubleVal = (Double)value;
        double total = achievedValue - doubleVal;

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
        if (!(obj instanceof DistanceGoal))
            return false;

        if (obj == this) {
            return true;
        } else {
            DistanceGoal goal = (DistanceGoal)obj;
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
     * Converts the provided data object to a DistanceGoal
     * @param data the data to convert
     * @return corresponding distance goal
     */
    public static DistanceGoal fromData(Map<String, Object> data) {
        checkKeysValidity(data);
        String sport = (String)data.get(Goal.SPORT_KEY);
        String type = (String)data.get(Goal.TYPE_KEY);
        String targetDate = (String)data.get(Goal.TARGET_DATE_KEY);

        GoalType goalType = GoalType.convertToGoalType(type);
        if (goalType != GoalType.DISTANCE)
            throw new IllegalArgumentException("Provided data does not represent a DISTANCE goal. Type provided is: " + goalType);

        Object targetValue = data.get(Goal.TARGET_VALUE_KEY);
        Object achievedValue = data.get(Goal.ACHIEVED_VALUE_KEY);

        if (targetValue == null || achievedValue == null || targetDate == null)
            throw new IllegalStateException("Expected fields are missing from the data object");

        // sometimes data retrieved from firestore may be a long instead of a double so do that check here
        Double targetDouble = targetValue instanceof Long ? Double.parseDouble("" + targetValue):(Double)targetValue;
        Double achievedDouble = achievedValue instanceof Long ? Double.parseDouble("" + achievedValue):(Double)achievedValue;

        return new DistanceGoal(Login.getUserId(), Sport.convertToSport(sport), LocalDateTime.parse(targetDate), targetDouble, achievedDouble);
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
        super.writeToParcel(dest, flags);
        dest.writeDouble(targetValue);
        dest.writeDouble(achievedValue);
    }
}
