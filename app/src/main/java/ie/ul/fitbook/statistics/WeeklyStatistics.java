package ie.ul.fitbook.statistics;

import com.google.firebase.firestore.DocumentReference;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.temporal.TemporalAdjusters;

import java.util.Map;

import ie.ul.fitbook.database.UserDatabase;
import ie.ul.fitbook.sports.Sport;

/**
 * This class provides access to weekly statistics for a specified user
 */
public final class WeeklyStatistics {
    /**
     * The collection path for our weekly statistics. This is to be appended onto the user database.
     * Weekly will have sub collections CYCLING, WALKING, RUNNING, which will be a collection of each week. Each week document is the weekly statistics of that week
     * for that sport
     */
    private static final String WEEKLY_COLLECTION_PATH = "statistics/weekly";

    /**
     * Retrieves the document reference representing the WeeklyStat for the provided reference.
     * To retrieve the actual WeeklyStat object, call reference.get() and in the success listener,
     * pass data into {@link WeeklyStat#from(Map)}
     * @param userId the user id to retrieve the weekly stat for
     * @param sport the sport this weekly stat is for
     * @return the DocumentReference for the document
     */
    public static DocumentReference getSportWeeklyStat(String userId, Sport sport) {
        return new UserDatabase(userId)
                .getChildDocument(WEEKLY_COLLECTION_PATH + "/" + getWeek() + "/" + sport.toString());
    }

    /**
     * Gets the current week
     * @return current week in form FirstDay Month - LastDay Month
     */
    public static String getWeek() {
        LocalDate monday = getStartOfWeek();
        LocalDate sunday = getEndOfWeek();

        String pattern = "dd MMMM";
        return monday.format(DateTimeFormatter.ofPattern(pattern))
                + " - "
                + sunday.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Gets the local date representing start of week
     * @return the local date representing start of week
     */
    public static LocalDate getStartOfWeek() {
        return LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    /**
     * Gets the local date representing end of week
     * @return the local date representing end of week
     */
    public static LocalDate getEndOfWeek() {
        return LocalDate.now()
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    }
}
