package ie.ul.fitbook.goals;

/**
 * This enum represents the type a Goal can represent
 */
public enum GoalType {
    /**
     * This goal represents a target distance
     */
    DISTANCE,
    /**
     * This goal represents a target time
     */
    TIME,
    /**
     * This goal represents a target elevation
     */
    ELEVATION;

    /**
     * Returns the value of the provided string as an enum value
     * @param string the string value to try and parse
     * @return the GoalType enum value if found
     * @throws IllegalArgumentException if the provided string does not match any enum value
     */
    public static GoalType convertToGoalType(String string) {
        GoalType[] values = GoalType.values();
        string = string.trim();

        for (GoalType value : values) {
            String valueString = value.toString();

            if (valueString.equalsIgnoreCase(string))
                return value;
        }

        throw new IllegalArgumentException("The provided String " + string + " does not match any GoalType enum value");
    }
}
