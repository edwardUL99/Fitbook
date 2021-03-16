package ie.ul.fitbook.ui;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import ie.ul.fitbook.R;

/**
 * This activity provides the main navigation activity
 */
public class HomeActivity extends AppCompatActivity {
    /**
     * The navigation controller aiding navigation
     */
    private NavController navController;
    /**
     * This is used as a means to identify the last used fragment id
     */
    public static final String FRAGMENT_ID = "ie.ul.fitbook.FRAG_ID";
    /**
     * The current destination ID
     */
    private int currentDestination;
    /**
     * Keeps track of the current destination id just before navigating to different destination
     */
    private int lastNavigationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_new_activity, R.id.navigation_messages, R.id.navigation_profile)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        Intent intent = getIntent();
        if (savedInstanceState != null || intent.hasExtra(FRAGMENT_ID)) {
            int id = savedInstanceState != null ? savedInstanceState.getInt(FRAGMENT_ID, 0):intent.getIntExtra(FRAGMENT_ID, 0);

            if (id != 0)
                navController.navigate(id);
        }

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            lastNavigationId = currentDestination; /* before we change our current destination,
                                                      set lastNavigationId to the old value
                                                      so that it will have the value of the navigation
                                                      before the one we have changed to now */
            currentDestination = destination.getId();
        });
    }

    /**
     * Gets the last navigation ID before navigating to the one currently selected
     * @return the ID of the last navigation, before navigating to the current one. 0 if no navigation has taken place
     */
    public int getLastNavigationId() {
        return lastNavigationId;
    }

    /**
     * Retrieves the navigation controller behind this bottom navigation
     * @return the NavController instance controlling bottom navigation
     */
    public NavController getNavController() {
        return navController;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(FRAGMENT_ID, navController.getCurrentDestination().getId());
    }

    /**
     * This method is called after {@link #onStart} when the activity is
     * being re-initialized from a previously saved state, given here in
     * <var>savedInstanceState</var>.  Most implementations will simply use {@link #onCreate}
     * to restore their state, but it is sometimes convenient to do it here
     * after all of the initialization has been done or to allow subclasses to
     * decide whether to use your default implementation.  The default
     * implementation of this method performs a restore of any view state that
     * had previously been frozen by {@link #onSaveInstanceState}.
     *
     * <p>This method is called between {@link #onStart} and
     * {@link #onPostCreate}. This method is called only when recreating
     * an activity; the method isn't invoked if {@link #onStart} is called for
     * any other reason.</p>
     *
     * @param savedInstanceState the data most recently supplied in {@link #onSaveInstanceState}.
     * @see #onCreate
     * @see #onPostCreate
     * @see #onResume
     * @see #onSaveInstanceState
     */
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        int id = savedInstanceState.getInt(FRAGMENT_ID);
        if (id != navController.getCurrentDestination().getId())
            navController.navigate(id);
    }

    /**
     * Handle onNewIntent() to inform the fragment manager that the
     * state is not saved.  If you are handling new intents and may be
     * making changes to the fragment state, you want to be sure to call
     * through to the super-class here first.  Otherwise, if your state
     * is saved but the activity is not stopped, you could get an
     * onNewIntent() call which happens before onResume() and trying to
     * perform fragment operations at that point will throw IllegalStateException
     * because the fragment manager thinks the state is still saved.
     *
     * @param intent the intent to set
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }
}