package ie.ul.fitbook.login;

import android.content.Context;
import android.content.SharedPreferences;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import ie.ul.fitbook.profile.Profile;
import ie.ul.fitbook.utils.Utils;

/**
 * This class provides static utility classes for handling login
 */
public final class Login {
    /**
     * Key used for determining a saved login
     */
    private static final String LOGIN_SAVED_KEY = "user_loggedin";
    /**
     * The profile of the user logged in
     */
    private static Profile profile;
    /**
     * Keep track of the logged in status
     */
    private static boolean loggedIn;
    /**
     * Variable to keep track of a forced login
     */
    private static boolean forcedLogin;
    /**
     * A variable to record if we should do manual login
     */
    private static boolean manualLogin;
    /**
     * A flag to keep track of the profile sync status. It may be out of sync because the app was started
     * without network and needs to be opened on the next profile launch
     */
    private static boolean profileOutOfSync = true;

    /**
     * Checks if there is a user logged in for the provided context. If you want to override this
     * behaviour and make it always return false, call forceLogin before calling this. It is a more
     * convenient way rather than setting intents to indicate a return to MainActivity means a sign out
     * and should re-login.
     *
     * @param context the context to check
     * @return true if logged in, false if not
     */
    public static boolean checkLogin(Context context) {
        if (forcedLogin) {
            forcedLogin = false;
            return false;
        } else {
            SharedPreferences sharedPreferences = context.getSharedPreferences(Utils.SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
            return doCheckLogin(sharedPreferences);
        }
    }

    /**
     * Checks if the shared preferences file says there has been a login saved for this before and if
     * the firebase user is not null
     * @param sharedPreferences the shared preferences to check
     * @return true if logged in, false if not
     */
    private static boolean doCheckLogin(SharedPreferences sharedPreferences) {
        boolean loginRecorded = sharedPreferences.getBoolean(LOGIN_SAVED_KEY, false); // check if a login has been recorded by another session

        loggedIn = loginRecorded && FirebaseAuth.getInstance().getCurrentUser() != null; // if current user is null, force a re-login
        return loggedIn;
    }

    /**
     * Saves the context login to the SharedPreferences file so that on next sessions, a login can be
     * remembered.
     * @param context the context which is logged in
     */
    public static void saveLogin(Context context) {
        // we save login so that if data is cleared, checkLogin() will always return false regardless of FirebaseAuth current user nullity, therefore forcing a re-login on the next launch of MainActivity
        SharedPreferences sharedPreferences = context.getSharedPreferences(Utils.SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(LOGIN_SAVED_KEY, true);
            editor.apply();
        }
    }

    /**
     * Removes any saved history of a login so that subsequent sessions will force a re-login
     * @param context the context to remove the login of
     */
    public static void deleteLogin(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(Utils.SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE).edit();
        editor.putBoolean(LOGIN_SAVED_KEY, false);
        editor.apply();
        loggedIn = false;
    }

    /**
     * Starts a logout process with the provided context and a listener to callback on if logout is successful.
     * Regardless of logout result this method calls deleteLogin(context)
     * @param context the context to logout with
     * @param onCompleteListener the listener to call back on when logout is complete
     */
    public static void logout(Context context, OnCompleteListener<Void> onCompleteListener) {
        Task<Void> signOut = AuthUI.getInstance().signOut(context);
        if (onCompleteListener != null)
                signOut.addOnCompleteListener(onCompleteListener);
        deleteLogin(context);
    }

    /**
     * Returns the profile of the logged in user
     * @return user profile
     */
    public static Profile getProfile() {
        return profile;
    }

    /**
     * Sets the profile for the logged in user
     * @param profile the profile of the user to login
     */
    public static void setProfile(Profile profile) {
        Login.profile = profile;
    }

    /**
     * Call this method to essentially force a re-login. After calling this,
     * checkLogin will always return false, so any client class using it will think the user is not
     * logged in and should force a re-login.
     * Once this is called, the only way for checkLogin to return to normal behaviour is to make 1
     * call to it and all subsequent calls will behave normally.
     */
    public static void forceLogin() {
        forcedLogin = true;
    }

    /**
     * Passing true into this method means that any LoginActivity will display a sign-in button instead
     * of automatically trying to login. It overrides automatic login
     * @param manualLogin true to override automatic login, false to re-enable automatic login
     */
    public static void setManualLogin(boolean manualLogin) {
        Login.manualLogin = manualLogin;
    }

    /**
     * Returns true if automatic login should be overridden and use manual sign in instead
     * @return true if auto login is overridden, false if not
     */
    public static boolean isManualLogin() {
        return manualLogin;
    }

    /**
     * Returns true if the profile is out of sync for whatever reason
     * @return true if out of sync, false if not
     */
    public static boolean isProfileOutOfSync() {
        return profileOutOfSync;
    }

    /**
     * Tells Login that the profile associated with it is out of sync because it may not have been downloaded to begin with
     * @param profileOutOfSync true if out of sync, false if not
     */
    public static void setProfileOutOfSync(boolean profileOutOfSync) {
        Login.profileOutOfSync = profileOutOfSync;
    }

    /**
     * Retrieves the user ID currently logged in
     * @return user ID of logged in user
     */
    public static String getUserId() {
        return FirebaseAuth.getInstance().getUid();
    }
}
