package ie.ul.fitbook.sports;

/**
 * This enum represents different sports available
 */
public enum Sport {
    /**
     * Enum representing the sport of walking
     */
    WALKING,
    /**
     * Enum representing the sport of running
     */
    RUNNING,
    /**
     * Enum representing the sport of cycling
     */
    CYCLING;

    /**
     * Returns the value of the provided string as an enum value
     * @param string the string value to try and parse
     * @return the Gender enum value if found
     * @throws IllegalArgumentException if the provided string does not match any enum value
     */
    public static Sport convertToSport(String string) {
        Sport[] values = Sport.values();
        string = string.trim();

        for (Sport value : values) {
            String valueString = value.toString();

            if (valueString.equalsIgnoreCase(string))
                return value;
        }

        throw new IllegalArgumentException("The provided String " + string + " does not match any Sport enum value");
    }
}
