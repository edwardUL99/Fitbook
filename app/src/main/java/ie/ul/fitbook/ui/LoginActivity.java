package ie.ul.fitbook.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.FirebaseUiException;
import com.firebase.ui.auth.IdpResponse;

import java.util.Collections;
import java.util.List;

import ie.ul.fitbook.R;
import ie.ul.fitbook.interfaces.SignInHandler;
import ie.ul.fitbook.login.Login;

/**
 * This activity provides the login functionality to the application.
 * It is intended to be started with a startActivityForResult call
 */
public class LoginActivity extends AppCompatActivity implements SignInHandler {
    /**
     * A code for logging in
     */
    private static final int RC_LOGIN = 123;
    /**
     * Tag used for logging
     */
    private static final String TAG = "LoginActivity";
    /**
     * Our shared preferences instance
     */
    private SharedPreferences sharedPreferences;

    /**
     * The status check TextView
     */
    private TextView statusCheck;

    /**
     * The sign in button
     */
    private Button signInButton;

    /**
     * Handles the creation of the MainActivity
     * @param savedInstanceState the bundle of the saved instance if any, null otherwise
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        statusCheck = findViewById(R.id.statusCheck);
        signInButton = findViewById(R.id.signIn);

        if (Login.isManualLogin()) {
            setSignInComponentsVisibility(true);
        } else {
            setSignInComponentsVisibility(false);
            autoLogin();
        }
    }

    /**
     * This method handles the automatic login of a user.
     * It may not login the user but instead realise a sign in needs to be done
     */
    private void autoLogin() {
        setSignInComponentsVisibility(false); // default is to have status check visible
        Log.i(TAG, "onCreate: No user has been logged in successfully, forcing a re-login");
        Toast.makeText(this, "No user logged in, opening sign-in page", Toast.LENGTH_SHORT).show();
        signIn(null);
    }

    /**
     * Opens the sign up page
     * @param view the view associated with this invocation
     */
    public void signIn(View view) {
        List<AuthUI.IdpConfig> providers =
                Collections.singletonList(new AuthUI.IdpConfig.EmailBuilder().build());

        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setLockOrientation(true)
                .setAvailableProviders(providers)
                .build();

        startActivityForResult(signInIntent, RC_LOGIN);
    }

    /**
     * Record the fact that a session has been recorded successfully to the shared preferences file
     */
    private void recordLoginStatus() {
        Log.i(TAG, "recordLoginStatus: Recording user logged in to the SharedPreferences file");
        Login.saveLogin(this);
    }

    /**
     * This method is the callback that should be called when sign in is successful
     */
    @Override
    public void onSignInSuccessful() {
        recordLoginStatus();
        displaySignInButton();

        setResult(RESULT_OK);
        finish();
    }

    /**
     * Display the sign in button and hide status check
     */
    private void displaySignInButton() {
        setSignInComponentsVisibility(true);
    }

    /**
     * Displays components visibility
     * @param signIn if sign in is true the status check is hidden and sign in button displayed,
     *               opposite if false
     */
    private void setSignInComponentsVisibility(boolean signIn) {
        if (signIn) {
            statusCheck.setVisibility(View.INVISIBLE);
            signInButton.setVisibility(View.VISIBLE);
        } else {
            statusCheck.setVisibility(View.VISIBLE);
            signInButton.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * This method is to be called on a sign in error. It handles the visibility of the relevant buttons
     */
    @Override
    public void onSignInError(FirebaseUiException error) {
        displaySignInButton();

        if (error != null) {
            Log.e(TAG, "onActivityResult: Error occurred on sign-in", error);

            if (error.getErrorCode() == ErrorCodes.NO_NETWORK) {
                Toast.makeText(this, "There is no network available to sign-in", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Toast.makeText(this, "An unknown error occurred during sign-in", Toast.LENGTH_SHORT).show();

        setResult(RESULT_CANCELED);
        finish();
    }

    /**
     * Called when the activity has detected the user's press of the back
     * key. The {@link #getOnBackPressedDispatcher() OnBackPressedDispatcher} will be given a
     * chance to handle the back button before the default behavior of
     * {@link Activity#onBackPressed()} is invoked.
     *
     * @see #getOnBackPressedDispatcher()
     */
    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    /**
     * Dispatch incoming result to the correct fragment.
     *
     * @param requestCode the request code to identify the result
     * @param resultCode the result code identifying the success
     * @param data the intent containing all required data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_LOGIN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                Log.i(TAG, "onActivityResult: User signed in successfully");
                Toast.makeText(this, "User signed in successfully", Toast.LENGTH_SHORT).show();
                onSignInSuccessful();
            } else {
                if (response == null) {
                    Log.i(TAG, "onActivityResult: Sign in cancelled");
                    Toast.makeText(this, "Sign-in Cancelled!", Toast.LENGTH_SHORT).show();

                    setResult(RESULT_CANCELED);
                    finish();
                } else {
                    onSignInError(response.getError());
                }
            }
        }
    }

    /**
     * Saves the visibility state of the status check and the sign in button
     * @param outState the bundle to save state to
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /**
     * This needs to be done as extras are not getting handled by this class
     * @param intent the intent to replace our intent with
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }
}