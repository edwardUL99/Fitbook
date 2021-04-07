package ie.ul.fitbook.ui.profile.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CustomCap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import de.hdodenhof.circleimageview.CircleImageView;
import ie.ul.fitbook.R;
import ie.ul.fitbook.database.UserDatabase;
import ie.ul.fitbook.goals.DistanceGoal;
import ie.ul.fitbook.goals.ElevationGoal;
import ie.ul.fitbook.goals.Goal;
import ie.ul.fitbook.goals.GoalType;
import ie.ul.fitbook.goals.TimeGoal;
import ie.ul.fitbook.login.Login;
import ie.ul.fitbook.profile.Profile;
import ie.ul.fitbook.recording.RecordedActivity;
import ie.ul.fitbook.statistics.WeeklyStat;
import ie.ul.fitbook.statistics.WeeklyStatistics;
import ie.ul.fitbook.ui.HomeActivity;
import ie.ul.fitbook.utils.Utils;

/**
 * This activity displays a recorded activity.
 * It needs extras RECORDED_ACTIVITY and ACTIVITY_PROFILE passed in with setProfilePhoto called also.
 *
 * If HomeActivity.FRAGMENT_ID extra is passed in, pressing back will go to HomeActivity with that fragment ID
 */
public class ViewRecordedActivity extends AppCompatActivity implements OnMapReadyCallback {
    /**
     * The key for the recorded activity extra
     */
    public static final String RECORDED_ACTIVITY = "ie.ul.fitbook.RECORDED_ACTIVITY";
    /**
     * The key to store the profile who recorded the activity in
     */
    public static final String ACTIVITY_PROFILE = "ie.ul.fitbook.ACTIVITY_PROFILE";
    /**
     * The profile image to display
     */
    private static Bitmap profileImage;
    /**
     * The activity recorded
     */
    private RecordedActivity activity;
    /**
     * True if you should return to home on back pressed
     */
    private boolean returnToHome;
    /**
     * The UserId this activity belongs to
     */
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_recorded);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Activity");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        Profile profile;

        if (intent.hasExtra(RECORDED_ACTIVITY)) {
            activity = intent.getParcelableExtra(RECORDED_ACTIVITY);
        } else {
            throw new IllegalStateException("No RecordedActivity provided to activity");
        }

        if (intent.hasExtra(ACTIVITY_PROFILE)) {
            profile = intent.getParcelableExtra(ACTIVITY_PROFILE);
            userID = profile.getUserId();

            if (userID == null)
                throw new IllegalStateException("The profile provided does not have a User ID set");
        } else {
            throw new IllegalStateException("No Profile provided to activity");
        }

        returnToHome = getIntent().hasExtra(HomeActivity.FRAGMENT_ID);

        if (activity != null && profile != null) {
            if (profileImage != null) {
                CircleImageView imageView = findViewById(R.id.userProfilePhoto);
                imageView.setImageBitmap(profileImage);
            }

            TextView nameView = findViewById(R.id.nameView);
            nameView.setText(profile.getName());
            TextView dateView = findViewById(R.id.dateView);
            dateView.setText(activity.getTimestamp().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy HH:mm")));
            TextView sportType = findViewById(R.id.sportType);
            sportType.setText(Utils.capitalise(activity.getSport().toString()));

            TextView distanceField = findViewById(R.id.distanceField);
            distanceField.setText(String.format(Locale.getDefault(), "%,.02f", activity.getDistance()));
            TextView timeField = findViewById(R.id.timeField);
            timeField.setText(Utils.durationToHoursMinutes(activity.getRecordedDuration()));
            TextView avgSpeed = findViewById(R.id.avgSpeedField);
            avgSpeed.setText(String.format(Locale.getDefault(), "%,.01f", activity.getAverageSpeed()));
            TextView elevationGain = findViewById(R.id.elevationGainField);
            String elevation = "" + (int)activity.getElevationGain();
            elevationGain.setText(elevation);
            String calories = "" + activity.getCaloriesBurned();
            TextView caloriesBurned = findViewById(R.id.caloriesField);
            caloriesBurned.setText(calories);
        }

        setupMapContainer();
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

    /**
     * Set the profile image to be displayed
     * @param profileImage the image to display
     */
    public static void setProfileImage(Bitmap profileImage) {
        ViewRecordedActivity.profileImage = profileImage;
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
        if (returnToHome) {
            int fragmentId = getIntent().getIntExtra(HomeActivity.FRAGMENT_ID, 0);
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(HomeActivity.FRAGMENT_ID, fragmentId);
            startActivity(intent);
            finish();
        } else {
            super.onBackPressed();
        }

        profileImage = null;
    }

    /**
     * Called when the activity is destroyed
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        profileImage = null;
    }

    /**
     * Handles when the google map is ready
     * @param googleMap the google map to display the route
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        List<LatLng> locations = activity.getRecordedLocations();
        LatLng initial = locations.get(0);

        Bitmap start = Utils.drawableToBitmap(this, R.drawable.ic_route_start);
        Bitmap end = Utils.drawableToBitmap(this, R.drawable.ic_route_end);
        googleMap.addPolyline(new PolylineOptions()
            .addAll(locations)
            .color(Color.RED)
            .startCap(new CustomCap(BitmapDescriptorFactory.fromBitmap(start), 12))
                .endCap(new CustomCap(BitmapDescriptorFactory.fromBitmap(end), 12)));

        CameraUpdate location = CameraUpdateFactory.newLatLngZoom(initial, 11f);
        googleMap.animateCamera(location);
    }

    /**
     * Initialize the contents of the Activity's standard options menu.  You
     * should place your menu items in to <var>menu</var>.
     *
     * <p>This is only called once, the first time the options menu is
     * displayed.  To update the menu every time it is displayed, see
     * {@link #onPrepareOptionsMenu}.
     *
     * <p>The default implementation populates the menu with standard system
     * menu items.  These are placed in the {@link Menu#CATEGORY_SYSTEM} group so that
     * they will be correctly ordered with application-defined menu items.
     * Deriving classes should always call through to the base implementation.
     *
     * <p>You can safely hold on to <var>menu</var> (and any items created
     * from it), making modifications to it as desired, until the next
     * time onCreateOptionsMenu() is called.
     *
     * <p>When you add items to the menu, you can implement the Activity's
     * {@link #onOptionsItemSelected} method to handle them there.
     *
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed;
     * if you return false it will not be shown.
     * @see #onPrepareOptionsMenu
     * @see #onOptionsItemSelected
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (userID.equals(Login.getUserId())) {
            getMenuInflater().inflate(R.menu.activity_menu, menu);
            return true;
        }

        return false;
    }

    /**
     * Handles when an error occurs deleting activity
     * @param e the exception that occurred if any
     */
    private void doDeleteError(Exception e) {
        if (e != null)
            e.printStackTrace();

        Toast.makeText(this, "Cannot delete activity due to an error", Toast.LENGTH_SHORT)
                .show();
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
     * Adjusts any uncompleted and non-expired goals after the activity has been deleted
     */
    private void adjustGoalsAfterDeletion() {
        CollectionReference collectionReference = new UserDatabase().getChildCollection(Goal.COLLECTION_PATH);
        collectionReference
                .whereEqualTo("sport", activity.getSport().toString())
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
                                            case DISTANCE: achievedValue = (double)activity.getDistance();
                                                break;
                                            case TIME: achievedValue = activity.getRecordedDuration();
                                                break;
                                            case ELEVATION: achievedValue = (int)activity.getElevationGain();
                                        }

                                        if (achievedValue != null) {
                                            goal.subtractAchievedValue(achievedValue);

                                            DocumentReference documentReference = collectionReference.document(snapshot.getId());
                                            documentReference.set(goal.toData())
                                                    .addOnFailureListener(this::doDeleteError);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Intent intent = new Intent(this, ListActivitiesActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(this::doDeleteError);
    }

    /**
     * Adjusts the weekly statistics after the activity is deleted
     */
    private void adjustStatisticsAfterDeletion() {
        LocalDateTime timestamp = activity.getTimestamp();
        LocalDateTime startDate = LocalDateTime.of(WeeklyStatistics.getStartOfWeek(), LocalTime.of(0, 0));
        LocalDateTime endDate = LocalDateTime.of(WeeklyStatistics.getEndOfWeek(), LocalTime.of(23, 59));
        if (!(timestamp.isBefore(startDate) || timestamp.isAfter(endDate))) { // only adjust this week's stats if the activity occurred within the bounds of the week
            DocumentReference reference = WeeklyStatistics.getSportWeeklyStat(userID, activity.getSport());
            reference.get()
                    .addOnSuccessListener(success -> {
                        Map<String, Object> data = success.getData();

                        if (data != null) {
                            WeeklyStat weeklyStat = WeeklyStat.from(data);
                            weeklyStat.subtractDistance((double) activity.getDistance());
                            weeklyStat.subtractElevation((int) activity.getElevationGain());
                            weeklyStat.subtractTime(activity.getRecordedDuration());

                            weeklyStat.save(reference, null, this::doDeleteError);
                        }

                        adjustGoalsAfterDeletion();
                    })
                    .addOnFailureListener(this::doDeleteError);
        } else {
            adjustGoalsAfterDeletion();
        }
    }

    /**
     * Delete the activity recorded
     */
    private void deleteActivity() {
        String documentId = activity.getFirestoreId();

        if (documentId != null) {
            new UserDatabase()
                    .getChildCollection(RecordedActivity.ACTIVITIES_PATH)
                    .document(documentId)
                    .delete()
                    .addOnSuccessListener(success -> adjustStatisticsAfterDeletion())
                    .addOnFailureListener(this::doDeleteError);
        } else {
            doDeleteError(new IllegalStateException("No document ID provided in the RecordedActivity"));
        }
    }

    /**
     * Handles when delete is clicked
     */
    private void onDeleteClicked() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Activity")
                .setMessage("Are you sure you want to delete this activity? It cannot be reversed")
                .setPositiveButton("Yes", (alertDialog, which) -> deleteActivity())
                .setNegativeButton("No", (alertDialog, which) -> alertDialog.dismiss())
                .show();
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
     * perform the default menu handling.</p>
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to
     * proceed, true to consume it here.
     * @see #onCreateOptionsMenu
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.deleteActivity) {
            onDeleteClicked();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}