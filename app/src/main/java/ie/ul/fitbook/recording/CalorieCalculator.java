package ie.ul.fitbook.recording;

import org.threeten.bp.Duration;

import java.util.HashMap;

import ie.ul.fitbook.profile.Profile;
import ie.ul.fitbook.sports.Sport;

/**
 * This class counts calories for a user based on provided sport and profile. It uses the general field from the
 * activities on this page: https://sites.google.com/site/compendiumofphysicalactivities/Activity-Categories
 */
public final class CalorieCalculator {
    /**
     * A HashMap mapping the sport to a map of the according MET values.
     */
    private static final HashMap<Sport, Float> METS = new HashMap<>();
    /**
     * Return value for calories if the sport does not have a MET value for it
     */
    public static final int SPORT_NOT_DEFINED = -1;
    /**
     * Return value for calories if the profile does not have an AthleticInformation object
     */
    public static final int NO_ATHLETIC_INFORMATION = -2;

    static {
        METS.put(Sport.CYCLING, 7.5f);
        METS.put(Sport.RUNNING, 7.0f);
        METS.put(Sport.WALKING, 3.5f);
    }

    /**
     * Calculate calories for the provided profile (with athletic information)
     * @param profile the profile containing athletic information
     * @param duration the duration the exercise was carried out for
     * @param sport the sport to calculate calories for
     * @return calories for the given profile or {@link #SPORT_NOT_DEFINED} or {@link #NO_ATHLETIC_INFORMATION}
     */
    public static int calculateCalories(Profile profile, Duration duration, Sport sport) {
        Float met = METS.get(sport);

        if (met == null) {
            return SPORT_NOT_DEFINED;
        }

        Profile.AthleticInformation athleticInformation = profile.getAthleticInformation();

        if (athleticInformation == null) {
            return NO_ATHLETIC_INFORMATION;
        }

        double weight = athleticInformation.getWeight();
        double minutes = duration.toMillis() / 60000.00;

        return (int)(minutes * (met * 3.5 * weight) / 200);
    }
}
