package ie.ul.fitbook.ui.profile.cache;

import java.util.HashMap;

/**
 * This class is used to determine if a profile has already been loaded and that it may reside in
 * the cache
 */
public final class ProfileCache {
    /**
     * A map to determine if the profile for the provided user ID has been loaded
     */
    private static final HashMap<String, Boolean> profileLoaded = new HashMap<>();

    private ProfileCache() {
        // prevent instantiation
    }

    /**
     * Returns the value for if the user has already been cached
     * @param userId the user ID of the profile to check
     * @return true if already retrieved, false if not
     */
    public static boolean hasUserBeenCached(String userId) {
        Boolean value = profileLoaded.get(userId);

        if (value == null)
            return false;

        return value;
    }

    /**
     * Sets the value for if this user has been cached
     * @param userId the user ID to set the cached value for
     * @param userCached true if cached, false if not
     */
    public static void setUserCached(String userId, boolean userCached) {
        profileLoaded.put(userId, userCached);
    }
}
