package ie.ul.fitbook.interfaces;

import com.firebase.ui.auth.FirebaseUiException;

/**
 * This interface describes an activity that manages the sign in of users.
 * It provides callbacks for handling success and errors etc.
 */
public interface SignInHandler {
    /**
     * This method is the callback that should be called when sign in is successful
     */
    void onSignInSuccessful();

    /**
     * This method is the callback that should be called when a sign in error occurs
     * @param error the error that has been thrown by the Firebase UI login process
     */
    void onSignInError(FirebaseUiException error);
}
