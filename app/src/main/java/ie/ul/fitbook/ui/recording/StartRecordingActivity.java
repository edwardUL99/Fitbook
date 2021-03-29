package ie.ul.fitbook.ui.recording;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Map;

import ie.ul.fitbook.R;
import ie.ul.fitbook.login.Login;
import ie.ul.fitbook.profile.Profile;
import ie.ul.fitbook.sports.Sport;
import ie.ul.fitbook.ui.HomeActivity;
import ie.ul.fitbook.utils.ProfileUtils;
import ie.ul.fitbook.utils.Utils;

/**
 * This activity displays the screen to start recording to the user.
 * In the intent starting this activity, pass in HomeActivity.FRAGMENT_ID to navigate back to that fragment
 * on return from this activity
 */
public class StartRecordingActivity extends AppCompatActivity implements
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback {

    /**
     * The spinner allowing us choose activities
     */
    private Spinner activitySpinner;
    /**
     * The items used in the activity spinner
     */
    private String[] activitySpinnerItems;
    /**
     * A flag to check whether permissions have been denied or not
     */
    private boolean permissionDenied;
    /**
     * The map we will be displaying
     */
    private GoogleMap map;
    /**
     * Our fused location client
     */
    private FusedLocationProviderClient fusedLocationClient;
    /**
     * The button to start recording
     */
    private Button startButton;
    /**
     * A switch for enabling traffic view
     */
    private SwitchCompat trafficEnabled;
    /**
     * A switch for enabling terrain view
     */
    private SwitchCompat terrainEnabled;
    /**
     * This variable stores the map's camera position that has been saved in onSaveInstanceState
     */
    private CameraPosition cameraPosition;

    /**
     * The request code for requesting permissions
     */
    private static final int RC_PERMISSIONS = 123;
    /**
     * Permissions required for location
     */
    private static final String[] PERMISSIONS_REQUIRED = {Manifest.permission.ACCESS_FINE_LOCATION};
    /**
     * A key used to retain the same chosen activity
     */
    private static final String CHOSEN_ACTIVITY = "ie.ul.fitbook.CHOSEN_ACTIVITY";
    /**
     * A key used to retain the value of the traffic switch
     */
    private static final String TRAFFIC_ENABLED = "ie.ul.fitbook.TRAFFIC_ENABLED";
    /**
     * A key used to retain the value of the terrain switch
     */
    private static final String TERRAIN_ENABLED = "ie.ul.fitbook.TERRAIN_ENABLED";
    /**
     * The key to store the map's camera position
     */
    private static final String MAP_CAMERA_POSITION = "ie.ul.fitbook.MAP_CAMERA_POSITION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_recording);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle("Start Recording");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        activitySpinner = findViewById(R.id.activitySpinner);

        startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(view -> startRecordingActivity());

        trafficEnabled = findViewById(R.id.trafficSwitch);
        trafficEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> onTrafficEnabled(isChecked));

        terrainEnabled = findViewById(R.id.terrainSwitch);
        terrainEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> onTerrainEnabled(isChecked));

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        setupActivitySpinner();
        setupMapContainer();

        if (savedInstanceState != null)
            updateUI(savedInstanceState);
    }

    /**
     * Start the recording activity
     */
    private void startRecordingActivity() {
        Intent intent = new Intent(this, RecordingActivity.class);
        intent.putExtra(RecordingActivity.ACTIVITY_TO_RECORD, (String)activitySpinner.getSelectedItem());
        startActivity(intent);
    }

    /**
     * Update the UI's state from the saved instance state
     * @param savedInstanceState the state to update the UI from
     */
    private void updateUI(@NonNull Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(CHOSEN_ACTIVITY)) {
            String chosen = savedInstanceState.getString(CHOSEN_ACTIVITY);

            if (chosen != null) {
                for (int i = 0; i < activitySpinnerItems.length; i++) {
                    if (activitySpinnerItems[i].equalsIgnoreCase(chosen)) {
                        activitySpinner.setSelection(i);
                        break;
                    }
                }
            }
        }

        trafficEnabled.setChecked(savedInstanceState.getBoolean(TRAFFIC_ENABLED));
        terrainEnabled.setChecked(savedInstanceState.getBoolean(TERRAIN_ENABLED));

        if (savedInstanceState.containsKey(MAP_CAMERA_POSITION))
            cameraPosition = savedInstanceState.getParcelable(MAP_CAMERA_POSITION);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(CHOSEN_ACTIVITY, (String)activitySpinner.getSelectedItem());
        outState.putBoolean(TRAFFIC_ENABLED, trafficEnabled.isChecked());
        outState.putBoolean(TERRAIN_ENABLED, terrainEnabled.isChecked());

        if (map != null)
            outState.putParcelable(MAP_CAMERA_POSITION, map.getCameraPosition());

        super.onSaveInstanceState(outState);
    }

    /**
     * Handles the checking of the traffic enabled being checked
     * @param isChecked true if checked, false if not
     */
    private void onTrafficEnabled(boolean isChecked) {
        if (map != null)
            map.setTrafficEnabled(isChecked);
    }

    /**
     * Handles the checking of the terrain enabled being checked
     * @param isChecked true if checked, false if not
     */
    private void onTerrainEnabled(boolean isChecked) {
        if (map != null) {
            int mapType = isChecked ? GoogleMap.MAP_TYPE_TERRAIN:GoogleMap.MAP_TYPE_NORMAL;
            map.setMapType(mapType);
        }
    }

    /**
     * Requests the necessary permissions for this activity
     */
    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(PERMISSIONS_REQUIRED, RC_PERMISSIONS);
        }
    }

    /**
     * Enables the my location button in the map
     */
    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);

            if (cameraPosition == null) { // we don't want to reset our camera position that was saved
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(success -> {
                            if (success != null) {
                                LatLng latLng = new LatLng(success.getLatitude(), success.getLongitude());
                                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));
                            }
                        });
            } else {
                cameraPosition = null; // we are now finished with the saved camera position so, set it to null
            }
        } else {
            requestPermissions();
        }
    }

    /**
     * Display a toast message saying permissions have been denied
     */
    private void doPermissionsDenied() {
        Toast.makeText(this, "Permissions have been denied", Toast.LENGTH_SHORT)
                .show();
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RC_PERMISSIONS) {
            if (grantResults.length > 0) {
                boolean succeeded = true;

                for (int i : grantResults) {
                    if (i != PackageManager.PERMISSION_GRANTED) {
                        succeeded = false;
                        break;
                    }
                }

                permissionDenied = !succeeded;

                if (succeeded) {
                    enableMyLocation();
                } else {
                    doPermissionsDenied();
                }
            } else {
                permissionDenied = true;
                doPermissionsDenied();
            }
        }
    }

    /**
     * Setup the activity spinner
     */
    private void setupActivitySpinner() {
        Sport[] sports = Sport.values();
        activitySpinnerItems = new String[sports.length];

        Profile profile = Login.getProfile();

        if (profile == null) {
            DocumentSnapshot documentSnapshot = ProfileUtils.getProfileSnapshot();

            Map<String, Object> data;
            if (documentSnapshot != null && (data = documentSnapshot.getData()) != null)
                profile = Profile.from(data);
        }

        String favSport = profile != null ? profile.getFavouriteSport():"";

        int sportIndex = 0;

        for (int i = 0; i < sports.length; i++) {
            String sport = Utils.capitalise(sports[i].toString());
            activitySpinnerItems[i] = Utils.capitalise(sport);

            if (favSport.equalsIgnoreCase(sport))
                sportIndex = i;
        }

        ArrayAdapter<String> spinnerAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, activitySpinnerItems);

        activitySpinner.setAdapter(spinnerAdapter);
        activitySpinner.setSelection(sportIndex);
    }

    /**
     * Sets up the map container
     */
    private void setupMapContainer() {
        SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager()
                .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Toast.makeText(this, "Failed to setup map", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        // no-op
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnMyLocationButtonClickListener(this);
        map.setOnMyLocationClickListener(this);
        enableMyLocation();
        map.setBuildingsEnabled(true);
        UiSettings settings = map.getUiSettings();
        settings.setMapToolbarEnabled(true);
        settings.setCompassEnabled(true);

        // This map may have been called after the activity has been restored in onCreate, so restore the state of the map
        onTrafficEnabled(trafficEnabled.isChecked());
        onTerrainEnabled(terrainEnabled.isChecked());

        if (cameraPosition != null) {
            // we have restored this activity so restore the camera position
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    /**
     * This is the fragment-orientated version of {@link #onResume()} that you
     * can override to perform operations in the Activity at the same point
     * where its fragments are resumed.  Be sure to always call through to
     * the super-class.
     */
    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();

        if (permissionDenied) {
            doPermissionsDenied();
            permissionDenied = false;
        }
    }

    /**
     * Navigates back to the home activity.
     * Passes back any fragment ID that may have been passed to this.
     */
    private void navigateBackToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(HomeActivity.FRAGMENT_ID, getIntent().getIntExtra(HomeActivity.FRAGMENT_ID, 0));
        startActivity(intent);
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
        navigateBackToHome();
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     * The default implementation simply returns false to have the normal
     * processing happen (calling the item's Runnable or sending a message to
     * its Handler as appropriate).  You can use this method for any items
     * for which you would like to do processing without those other
     * facilities.
     *
     * <p>Derived classes should call through to the base class for it to
     * perform the default menu handling.
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to
     * proceed, true to consume it here.
     * @see #onCreateOptionsMenu
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            navigateBackToHome();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}