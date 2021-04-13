package ie.ul.fitbook.ui.recording;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import ie.ul.fitbook.R;
import ie.ul.fitbook.database.ActivitiesDatabase;
import ie.ul.fitbook.database.UserDatabase;
import ie.ul.fitbook.goals.DistanceGoal;
import ie.ul.fitbook.goals.ElevationGoal;
import ie.ul.fitbook.goals.Goal;
import ie.ul.fitbook.goals.GoalType;
import ie.ul.fitbook.goals.TimeGoal;
import ie.ul.fitbook.login.Login;
import ie.ul.fitbook.profile.Profile;
import ie.ul.fitbook.recording.RecordedActivity;
import ie.ul.fitbook.sports.Sport;
import ie.ul.fitbook.statistics.WeeklyStat;
import ie.ul.fitbook.statistics.WeeklyStatistics;
import ie.ul.fitbook.ui.HomeActivity;
import ie.ul.fitbook.utils.ProfileUtils;
import ie.ul.fitbook.utils.Utils;

/**
 * This activity is used for saving a recorded activity or deleting it. Pass in the RecordedActivity object
 * using the RECORDED_ACTIVITY key
 */
public class SaveRecordingActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener {
    /**
     * The activity that has been recorded
     */
    private RecordedActivity recordedActivity;
    /**
     * The google map to put poly lines onto
     */
    private GoogleMap googleMap;

    /**
     * This key stores the recorded activity in the extra
     */
    public static final String RECORDED_ACTIVITY = "ie.ul.fitbook.RECORDED_ACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_recording);
        
        if (savedInstanceState == null) {
            Intent intent = getIntent();

            if (intent.hasExtra(RECORDED_ACTIVITY)) {
                recordedActivity = intent.getParcelableExtra(RECORDED_ACTIVITY);
            } else {
                throw new IllegalStateException("Cannot launch this activity without a RECORDED_ACTIVITY");
            }
        } else {
            if (savedInstanceState.containsKey(RECORDED_ACTIVITY)) {
                recordedActivity = savedInstanceState.getParcelable(RECORDED_ACTIVITY);
            } else {
                throw new IllegalStateException("Cannot launch this activity without a RECORDED_ACTIVITY");
            }
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Save Activity");
        }

        TextView distanceView = findViewById(R.id.distanceField);
        TextView timeView = findViewById(R.id.timeField);
        TextView averageSpeedView = findViewById(R.id.avgSpeedField);
        TextView elevationGainView = findViewById(R.id.elevationGainField);
        TextView caloriesView = findViewById(R.id.caloriesField);

        distanceView.setText(String.format(Locale.getDefault(), "%,.02f", recordedActivity.getDistance()));
        timeView.setText(Utils.durationToHoursMinutesSeconds(recordedActivity.getRecordedDuration()));
        averageSpeedView.setText(String.format(Locale.getDefault(), "%,.01f", recordedActivity.getAverageSpeed()));
        elevationGainView.setText(String.format(Locale.getDefault(), "%,.02f", recordedActivity.getElevationGain()));
        String calories = "" + recordedActivity.getCaloriesBurned();
        caloriesView.setText(calories);

        Button delete = findViewById(R.id.delete);
        delete.setOnClickListener(view -> onDeleteClicked());

        Button save = findViewById(R.id.save);
        save.setOnClickListener(view -> onSaveClicked());

        setupMapContainer();
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
        onDeleteClicked();
    }

    /**
     * Handles when delete is clicked
     */
    private void onDeleteClicked() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Activity")
                .setMessage("Are you sure you want to delete this activity? It cannot be reversed")
                .setPositiveButton("Yes", (alertDialog, which) -> onDeleteConfirmed())
                .setNegativeButton("No", (alertDialog, which) -> alertDialog.dismiss())
                .show();
    }

    /**
     * Handles when delete is confirmed
     */
    private void onDeleteConfirmed() {
        Intent intent = new Intent(this, StartRecordingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    /**
     * This method handles when saving statistics fails
     * @param exception the exception if any
     */
    private void onStatisticsFail(Exception exception) {
        if (exception != null)
            exception.printStackTrace();

        Toast.makeText(this, "An error occurred setting user's weekly statistics", Toast.LENGTH_SHORT).show();
    }

    /**
     * Get the goal from the provided data
     * @param data the data to retrieve from
     * @param goalType a reference to set the goal type of
     * @return the goal or null if an error occurs
     */
    private Goal getGoal(Map<String, Object> data, AtomicReference<GoalType> goalType) {
        Object type = data.get(Goal.TYPE_KEY);

        if (!(type instanceof String))
            return null;

        GoalType goalType1 = GoalType.convertToGoalType((String)type);
        goalType.set(goalType1);

        switch (goalType1) {
            case TIME: return TimeGoal.fromData(data);
            case ELEVATION: return ElevationGoal.fromData(data);
            case DISTANCE: return DistanceGoal.fromData(data);
            default: return null;
        }
    }

    /**
     * Handles goals saving failure
     * @param e the exception if any
     */
    private void onGoalsFailure(Exception e) {
        if (e != null)
            e.printStackTrace();

        Toast.makeText(this, "An error occurred saving user's goals", Toast.LENGTH_SHORT).show();
    }

    /**
     * Add to any goals the user might have that are not expired
     * @param recordedActivity the activity recorded
     */
    private void addToGoals(RecordedActivity recordedActivity) {
        CollectionReference collectionReference = new UserDatabase().getChildCollection(Goal.COLLECTION_PATH);
        collectionReference
                .whereEqualTo("sport", recordedActivity.getSport().toString())
                .get()
                .addOnSuccessListener(success -> {
                    if (success != null) {
                        for (DocumentSnapshot snapshot : success.getDocuments()) {
                            Map<String, Object> data = snapshot.getData();

                            if (data != null) {
                                AtomicReference<GoalType> goalTypeRef = new AtomicReference<>(null);
                                Goal goal = getGoal(data, goalTypeRef);
                                GoalType goalType = goalTypeRef.get();

                                if (goal != null && goalType != null) {
                                    if (!goal.isExpired() && !goal.isCompleted()) {
                                        Object achievedValue = null;

                                        switch (goalType) {
                                            case DISTANCE: achievedValue = (double)recordedActivity.getDistance();
                                                            break;
                                            case TIME: achievedValue = recordedActivity.getRecordedDuration();
                                                        break;
                                            case ELEVATION: achievedValue = (int)recordedActivity.getElevationGain();
                                        }

                                        if (achievedValue != null) {
                                            goal.addAchievedValue(achievedValue);

                                            DocumentReference documentReference = collectionReference.document(snapshot.getId());
                                            documentReference.set(goal.toData())
                                                    .addOnFailureListener(this::onGoalsFailure);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    launchViewActivity();
                })
                .addOnFailureListener(this::onGoalsFailure);
    }

    /**
     * Launch the activity to view the recorded activity
     */
    private void launchViewActivity() {
        Intent intent = new Intent(this, ViewRecordedActivity.class);
        intent.putExtra(HomeActivity.FRAGMENT_ID, getIntent().getIntExtra(HomeActivity.FRAGMENT_ID, 0));
        intent.putExtra(ViewRecordedActivity.RECORDED_ACTIVITY, recordedActivity);
        Profile profile = Login.getProfile();
        intent.putExtra(ViewRecordedActivity.ACTIVITY_PROFILE, profile);
        ViewRecordedActivity.setProfileImage(profile.getProfileImage());
        startActivity(intent);
        finish();
    }

    /**
     * Set the weekly statistics for the provided profile
     * @param profile the profile to set statistics for
     * @param recordedActivity the activity recorded
     */
    private void setStatistics(Profile profile, RecordedActivity recordedActivity) {
        String userId = profile.getUserId();
        Sport sport = recordedActivity.getSport();
        DocumentReference reference = WeeklyStatistics.getSportWeeklyStat(userId, recordedActivity.getSport());
        reference.get()
                .addOnCompleteListener(success -> {
                    if (success.isSuccessful()) {
                        DocumentSnapshot snapshot = success.getResult();

                        if (snapshot != null) {
                            Map<String, Object> data = snapshot.getData();

                            WeeklyStat weeklyStat;
                            if (data != null) {
                                weeklyStat = WeeklyStat.from(data);
                            } else {
                                weeklyStat = new WeeklyStat(sport);
                            }

                            weeklyStat.addDistance((double)recordedActivity.getDistance());
                            weeklyStat.addElevation((int)recordedActivity.getElevationGain());
                            weeklyStat.addTime(recordedActivity.getRecordedDuration());

                            weeklyStat.save(reference, null, this::onStatisticsFail);
                        }
                    } else {
                        onStatisticsFail(success.getException());
                    }
                })
                .addOnFailureListener(this::onStatisticsFail);

        addToGoals(recordedActivity);
    }

    /**
     * Handles when the profile has been downloaded if it had to be re-downloaded
     * @param profile the profile that was downloaded
     */
    private void onProfileDownloaded(Profile profile) {
        Login.setProfile(profile); // reset the profile as the Login in. We have to do this since ProfileUtils.syncProfile used to do it but does not download the profile image synchronously
        setStatistics(profile, recordedActivity);
    }

    /**
     * Handles when the save button is clicked
     */
    private void onSaveClicked() {
        new ActivitiesDatabase()
                .getDatabase()
                .add(recordedActivity.toData())
                .addOnSuccessListener(success -> {
                    Toast.makeText(this, "Activity saved", Toast.LENGTH_SHORT).show();

                    Profile profile = Login.getProfile();

                    recordedActivity.setFirestoreId(success.getId());
                    if (profile == null) {
                        ProfileUtils.downloadProfile(Login.getUserId(), this::onProfileDownloaded, () -> Toast.makeText(this, "Failed to save", Toast.LENGTH_SHORT).show(),
                                null, false, this, true); // download profile and profile image synchronously so that the image is available on activity save
                    } else {
                        setStatistics(profile, recordedActivity);
                    }
                })
                .addOnFailureListener(fail -> {
                    Toast.makeText(this, "Failed: " + fail.getMessage(), Toast.LENGTH_SHORT).show();
                    fail.printStackTrace();
                });
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
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        enableMyLocation();
        googleMap.setOnMyLocationButtonClickListener(this);
        googleMap.setOnMyLocationClickListener(this);

        this.googleMap.addPolyline(new PolylineOptions()
            .addAll(recordedActivity.getRecordedLocations())
            .color(Color.RED));
    }

    /**
     * Enables the my location button in the map
     */
    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);

            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(success -> {
                            if (success != null) {
                                LatLng latLng = new LatLng(success.getLatitude(), success.getLongitude());
                                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11f));
                            }
                        });
            }
        }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        //no-op
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(RECORDED_ACTIVITY, recordedActivity);
    }
}