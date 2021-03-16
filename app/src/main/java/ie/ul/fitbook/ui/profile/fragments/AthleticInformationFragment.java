package ie.ul.fitbook.ui.profile.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import ie.ul.fitbook.R;
import ie.ul.fitbook.profile.Profile;
import ie.ul.fitbook.ui.profile.ProfileCreationActivity;
import ie.ul.fitbook.ui.profile.viewmodels.ProfileViewModel;
import ie.ul.fitbook.utils.Utils;

/**
 * This fragment deals with entering athletic information
 */
public class AthleticInformationFragment extends Fragment implements PersistentEditFragment {
    /**
     * The profile being edited
     */
    private Profile profile;
    /**
     * The text field for entering the date of birth
     */
    private EditText dateOfBirthField;
    /**
     * The spinner for gender
     */
    private Spinner genderField;
    /**
     * The text field for entering weight
     */
    private EditText weightField;
    /**
     * The activity this fragment is part of
     */
    private ProfileCreationActivity activity;
    /**
     * A flag to keep track of if we are editing or not
     */
    private boolean editing;
    /**
     * A flag to determine if entered fields are valid
     */
    private boolean valid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_athletic_information, container, false);
    }

    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * has returned, but before any saved state has been restored in to the view.
     * This gives subclasses a chance to initialize themselves once
     * they know their view hierarchy has been completely created.  The fragment's
     * view hierarchy is not however attached to its parent at this point.
     *
     * @param view               The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FragmentActivity activity = requireActivity();

        if (!(activity instanceof ProfileCreationActivity)) {
            throw new IllegalStateException("AthleticInformationFragment needs to be in the context of a ProfileCreationActivity");
        } else {
            this.activity = (ProfileCreationActivity)activity;
        }

        editing = this.activity.isInEditMode();
        this.activity.setCurrentFragment(this);

        Button cancel = view.findViewById(R.id.cancel2);
        cancel.setOnClickListener(v -> this.activity.onCancel());

        Button next = view.findViewById(R.id.submit);
        next.setOnClickListener(v -> onSubmit());

        dateOfBirthField = view.findViewById(R.id.dateBirthTextField);
        genderField = view.findViewById(R.id.genderDropdown);
        weightField = view.findViewById(R.id.weightTextField);

        setupProfile();
        setUpGenderSpinner();
        fillFieldsWithProfile();
        setupDateOfBirthField();
    }

    /**
     * Retrieves the calendar for use with date of birth picker
     * @return the calendar for dob picker
     */
    private Calendar getCalendar() {
        Calendar calendar = Calendar.getInstance();

        if (editing) {
            String dateOfBirth = dateOfBirthField.getText().toString();
            int[] values = new int[3];
            int i = 0;

            for (String s : dateOfBirth.split("/")) {
                values[i++] = Integer.parseInt(s);
            }

            calendar.set(Calendar.DAY_OF_MONTH, values[0]);
            calendar.set(Calendar.MONTH, values[1] - 1);
            calendar.set(Calendar.YEAR, values[2]);
        }

        return calendar;
    }

    /**
     * Sets up the date of birth field's listeners etc
     */
    private void setupDateOfBirthField() {
        final Calendar calendar = getCalendar();
        DatePickerDialog.OnDateSetListener listener = (view, year, monthOfYear, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDobField(calendar);
        };

        dateOfBirthField.setOnClickListener(view -> new DatePickerDialog(getActivity(), listener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show());
    }

    /**
     * Updates the date of birth field from the provided calendar
     * @param calendar the calendar representing the date of birth
     */
    private void updateDobField(Calendar calendar) {
        String format = "dd/MM/yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, Locale.getDefault());

        dateOfBirthField.setText(simpleDateFormat.format(calendar.getTime()));
    }

    /**
     * Set up the profile instance being edited
     */
    private void setupProfile() {
        ProfileViewModel profileViewModel = new ViewModelProvider(this.activity).get(ProfileViewModel.class);
        this.profile = profileViewModel.getSelectedProfile().getValue();
    }

    /**
     * This method sets up the gender spinner
     */
    private void setUpGenderSpinner() {
        List<String> genders = new ArrayList<>();

        for (Profile.AthleticInformation.Gender gender : Profile.AthleticInformation.Gender.values()) {
            String genderString = Utils.capitalise(gender.toString());
            genders.add(genderString);
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, genders);
        genderField.setAdapter(arrayAdapter);
    }

    /**
     * This method fills the fields with information from the logged in profile
     */
    private void fillFieldsWithProfile() {
        if (editing) {
            Profile.AthleticInformation athleticInformation = profile.getAthleticInformation();
            dateOfBirthField.setText(athleticInformation.getDateOfBirth());
            weightField.setText(String.format(Locale.getDefault(), "%,.2f", athleticInformation.getWeight()));
            String gender = athleticInformation.getGender();
            SpinnerAdapter adapter = genderField.getAdapter();

            for (int i = 0; i < adapter.getCount(); i++) {
                if (adapter.getItem(i).equals(gender)) {
                    genderField.setSelection(i);
                    break;
                }
            }
        }
    }

    /**
     * Handles the submit button being clicked
     */
    private void onSubmit() {
        saveEditState(profile);

        if (valid)
            activity.onSubmit(); // we are finished creating the profile and we want to submit
    }

    /**
     * This method is used to save the edit state of the current editing page to the provided profile
     * object.
     *
     * @param profile the profile that any edits should be saved to
     */
    @Override
    public void saveEditState(Profile profile) {
        valid = true;

        String dateOfBirth = dateOfBirthField.getText().toString();

        if (dateOfBirth.isEmpty()) {
            dateOfBirthField.setError("Date of birth can't be empty");
            valid = false;
        } else if (!dateOfBirth.matches(Profile.AthleticInformation.DOB_REGEX)) {
            dateOfBirthField.setError("Invalid date format");
            valid = false;
        }

        String gender = (String)genderField.getSelectedItem();
        valid = gender != null || !gender.isEmpty();

        String weightText = weightField.getText().toString();
        double weight = 0;
        if (weightText.isEmpty()) {
            weightField.setError("Weight can't be empty");
            valid = false;
        } else {
            try {
                weight = Double.parseDouble(weightText);

                if (weight <= 0) {
                    weightField.setError("Weight needs to be greater than 0");
                    valid = false;
                }
            } catch (NumberFormatException ex) {
                weightField.setError("You can only enter numeric values for weight");
                valid = false;
            }
        }

        if (!editing) {
            Profile.AthleticInformation athleticInformation =
                    new Profile.AthleticInformation(dateOfBirth, Profile.AthleticInformation.Gender.convertToGender(gender), weight);
            profile.setAthleticInformation(athleticInformation);
        } else {
            Profile.AthleticInformation athleticInformation = profile.getAthleticInformation();
            athleticInformation.setDateOfBirth(dateOfBirth);
            athleticInformation.setGender(Profile.AthleticInformation.Gender.convertToGender(gender));
            athleticInformation.setWeight(weight);
        }
    }
}