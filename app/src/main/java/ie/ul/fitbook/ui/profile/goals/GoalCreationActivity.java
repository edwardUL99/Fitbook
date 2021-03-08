package ie.ul.fitbook.ui.profile.goals;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import org.threeten.bp.Duration;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import ie.ul.fitbook.R;
import ie.ul.fitbook.database.UserDatabase;
import ie.ul.fitbook.goals.DistanceGoal;
import ie.ul.fitbook.goals.ElevationGoal;
import ie.ul.fitbook.goals.Goal;
import ie.ul.fitbook.goals.GoalType;
import ie.ul.fitbook.goals.TimeGoal;
import ie.ul.fitbook.login.Login;
import ie.ul.fitbook.sports.Sport;
import ie.ul.fitbook.utils.Utils;

/**
 * This activity provides the ability to create a new goal
 */
public class GoalCreationActivity extends AppCompatActivity {
    /**
     * The spinner for choosing sports
     */
    private Spinner sportSpinner;
    /**
     * The constraint layout housing the distance fields
     */
    private ConstraintLayout distanceFields;
    /**
     * The constraint layout housing the time fields
     */
    private ConstraintLayout timeFields;
    /**
     * The constraint layout housing the elevation fields
     */
    private ConstraintLayout elevationFields;
    /**
     * The last layout with visible fields
     */
    private ConstraintLayout lastVisibleFields;
    /**
     * The target date picker text box
     */
    private EditText targetDate;
    /**
     * The selected goal type
     */
    private GoalType selectedType;
    /**
     * The time set by the target date field
     */
    private String time = "";
    /**
     * The date set by the target date field
     */
    private String date = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_creation);

        distanceFields = findViewById(R.id.distanceFields);
        timeFields = findViewById(R.id.timeFields);
        elevationFields = findViewById(R.id.elevationFields);
        targetDate = findViewById(R.id.targetDate);

        Button saveGoal = findViewById(R.id.saveGoal);
        saveGoal.setOnClickListener(view -> saveGoal());

        setupSportSpinner();
        setupTypeGroup();
        setupTargetDate();

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle("New Goal");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Sets up the spinner for choosing sports
     */
    private void setupSportSpinner() {
        List<String> sports = new ArrayList<>();

        for (Sport sport : Sport.values())
            sports.add(Utils.capitalise(sport.toString()));

        sportSpinner = findViewById(R.id.activitiesSpinner);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sports);
        sportSpinner.setAdapter(arrayAdapter);
    }

    /**
     * Sets up the radio group for choosing
     */
    private void setupTypeGroup() {
        RadioGroup typeGroup = findViewById(R.id.typeGroup);
        lastVisibleFields = distanceFields;
        selectedType = GoalType.DISTANCE;

        typeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.distanceRadio) {
                lastVisibleFields.setVisibility(View.INVISIBLE);
                distanceFields.setVisibility(View.VISIBLE);
                lastVisibleFields = distanceFields;
                selectedType = GoalType.DISTANCE;
            } else if (checkedId == R.id.timeRadio) {
                lastVisibleFields.setVisibility(View.INVISIBLE);
                timeFields.setVisibility(View.VISIBLE);
                lastVisibleFields = timeFields;
                selectedType = GoalType.TIME;
            } else if (checkedId == R.id.elevationRadio) {
                lastVisibleFields.setVisibility(View.INVISIBLE);
                elevationFields.setVisibility(View.VISIBLE);
                lastVisibleFields = elevationFields;
                selectedType = GoalType.ELEVATION;
            }
        });
    }

    /**
     * Sets up the target date field
     */
    private void setupTargetDate() {
        final Calendar calendar = Calendar.getInstance();

        TimePickerDialog.OnTimeSetListener listener = (view, hourOfDay, minute) -> {
            time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
            String dateTime = date + " " + time;
            targetDate.setText(dateTime);
            targetDate.setError(null); // clear any error that may have been displayed
        };

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, listener,
                calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);

        DatePickerDialog.OnDateSetListener listener1 = (view, year, monthOfYear, dayOfMonth) -> {
            date = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, (monthOfYear + 1), year);
            timePickerDialog.show();
        };

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, listener1,
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        targetDate.setOnClickListener(view -> datePickerDialog.show());
    }

    /**
     * Parses the distance target value
     * @return distance target value, null if an error occurs
     */
    private Double parseDistance() {
        String error = "You need to fill in this field";

        EditText distance = distanceFields.findViewById(R.id.kmField);
        String text = distance.getText().toString();

        if (text.isEmpty()) {
            distance.setError(error);
        } else {
            double value = Double.parseDouble(text);

            if (value <= 0) {
                distance.setError("Distance must be greater than 0");
            } else {
                return value;
            }
        }

        return null;
    }

    /**
     * Parses the time target value
     * @return time target value, null if an error occurs
     */
    private Duration parseDuration() {
        String error;
        EditText hours = timeFields.findViewById(R.id.hoursField);
        EditText minutes = timeFields.findViewById(R.id.minutesField);

        String hoursText = hours.getText().toString();
        String minutesText = minutes.getText().toString();

        if (hoursText.isEmpty() && minutesText.isEmpty()) {
            error = "You need to fill in one of these fields";
            hours.setError(error);
        } else {
            int hoursVal = hoursText.isEmpty() ? 0:Integer.parseInt(hoursText);
            int minutesVal = minutesText.isEmpty() ? 0:Integer.parseInt(minutesText);

            if (hoursVal == 0 && minutesVal == 0) {
                error = "Both these fields cannot be 0";
                hours.setError(error);
            } else if (hoursVal < 0) {
                hours.setError("Hours must be 0 or more");
            } else if (minutesVal < 0 || minutesVal >= 60) {
                minutes.setError("Minutes must be 0 or more and less than 60");
            } else {
                return Utils.hoursMinutesToDuration(hoursVal, minutesVal);
            }
        }

        return null;
    }

    /**
     * Parses the elevation target value
     * @return the target value, null if an error occurs
     */
    private Integer parseElevation() {
        String error = "You need to fill in this field";

        EditText elevation = elevationFields.findViewById(R.id.metresField);
        String text = elevation.getText().toString();

        if (text.isEmpty()) {
            elevation.setError(error);
        } else {
            int value = Integer.parseInt(text);

            if (value <= 0) {
                elevation.setError("Elevation must be greater than 0");
            } else {
                return value;
            }
        }

        return null;
    }

    /**
     * Parses the target date
     * @return target date, or null if an error occurs
     */
    private LocalDateTime parseTargetDate() {
        if (time.isEmpty() || date.isEmpty()) {
            targetDate.setError(""); // we can't view the error as clicking the field will display the date and time picker, so display a toast instead
            Toast.makeText(this, "A date and time needs to be selected", Toast.LENGTH_SHORT)
                    .show();
            return null;
        } else {
            String dateTime = date + " " + time;
            LocalDateTime dateTime1 = LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            if (!dateTime1.isAfter(LocalDateTime.now())) {
                targetDate.setError("");
                Toast.makeText(this, "The target date needs to be some time in the future", Toast.LENGTH_SHORT)
                        .show();
                return null;
            } else {
                return dateTime1;
            }
        }
    }

    /**
     * Handles the saving of the goal
     */
    private void saveGoal() {
        Sport sport = Sport.convertToSport((String)sportSpinner.getSelectedItem());
        LocalDateTime dateTime = parseTargetDate();

        Goal goal = null;
        String userID = Login.getUserId();

        if (selectedType == GoalType.DISTANCE) {
            Double target = parseDistance();
            goal = target != null ? new DistanceGoal(userID, sport, dateTime, target):null;
        } else if (selectedType == GoalType.TIME) {
            Duration target = parseDuration();
            goal = target != null ? new TimeGoal(userID, sport, dateTime, target):null;
        } else if (selectedType == GoalType.ELEVATION) {
            Integer target = parseElevation();
            goal = target != null ? new ElevationGoal(userID, sport, dateTime, target):null;
        }

        if (goal != null) {
            new UserDatabase()
                    .getChildCollection(Goal.COLLECTION_PATH)
                    .add(goal.toData())
                    .addOnSuccessListener(success -> {
                        Toast.makeText(this, "Goal Saved Successfully", Toast.LENGTH_SHORT)
                                .show();
                        finish();
                    })
                    .addOnFailureListener(failure -> {
                        failure.printStackTrace();
                        Toast.makeText(this, "Goal not saved successfully", Toast.LENGTH_SHORT)
                                .show();
                    });
        }
    }
}