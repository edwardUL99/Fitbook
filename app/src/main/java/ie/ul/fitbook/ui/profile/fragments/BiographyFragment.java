package ie.ul.fitbook.ui.profile.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import java.util.ArrayList;
import java.util.List;

import ie.ul.fitbook.R;
import ie.ul.fitbook.profile.Profile;
import ie.ul.fitbook.sports.Sport;
import ie.ul.fitbook.ui.profile.ProfileCreationActivity;
import ie.ul.fitbook.ui.profile.viewmodels.ProfileViewModel;
import ie.ul.fitbook.utils.Utils;

/**
 * The fragment for entering user's biography
 */
public class BiographyFragment extends Fragment implements PersistentEditFragment {
    /**
     * The profile being edited
     */
    private Profile profile;
    /**
     * The text field for the biography
     */
    private EditText biographyField;
    /**
     * The spinner to choose favourite activity
     */
    private Spinner activityField;
    /**
     * The activity behind this fragment
     */
    private ProfileCreationActivity activity;
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
        return inflater.inflate(R.layout.fragment_biography, container, false);
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
        FragmentActivity fragmentActivity = requireActivity();

        ProfileCreationActivity activity;

        if (!(fragmentActivity instanceof ProfileCreationActivity)) {
            throw new IllegalStateException("BiographyFragment needs to be in the context of a ProfileCreationActivity");
        } else {
            activity = (ProfileCreationActivity)fragmentActivity;
        }

        this.activity = activity;
        this.activity.setCurrentFragment(this);

        setupProfile();

        Button cancel = view.findViewById(R.id.cancel1);
        cancel.setOnClickListener(v -> activity.onCancel());

        Button next = view.findViewById(R.id.next1);
        next.setOnClickListener(v -> onNext(view));

        biographyField = view.findViewById(R.id.biographyTextField);
        activityField = view.findViewById(R.id.activityDropdown);

        setUpActivitySpinner();
        fillFieldsWithProfile();
    }

    /**
     * This method sets up the values for the activity spinner
     */
    private void setUpActivitySpinner() {
        List<String> values = new ArrayList<>();

        for (Sport sport : Sport.values()) {
            String value = Utils.capitalise(sport.toString());
            values.add(value);
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, values);
        activityField.setAdapter(arrayAdapter);
    }

    /**
     * Set up the profile instance being edited
     */
    private void setupProfile() {
        ProfileViewModel profileViewModel = new ViewModelProvider(this.activity).get(ProfileViewModel.class);
        this.profile = profileViewModel.getSelectedProfile().getValue();
    }

    /**
     * This method fills the fields with information from the logged in profile
     */
    private void fillFieldsWithProfile() {
        String bio = profile.getBio();
        bio = bio == null ? "":bio;
        biographyField.setText(bio);
        String activity = profile.getFavouriteSport();

        if (activity != null) {
            SpinnerAdapter adapter = activityField.getAdapter();

            for (int i = 0; i < adapter.getCount(); i++) {
                if (adapter.getItem(i).equals(activity)) {
                    activityField.setSelection(i);
                    break;
                }
            }
        }
    }

    /**
     * The button handler for the next screen
     * @param view the view for the fragment (not the button on click listener)
     */
    private void onNext(View view) {
        saveEditState(profile);

        if (valid)
            Navigation.findNavController(view).navigate(R.id.action_biographyFragment_to_athleticInformationFragment);
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

        String biography = biographyField.getText().toString();
        profile.setBio(biography);

        String activityValue = (String)activityField.getSelectedItem();
        if (activityValue.isEmpty()) {
            valid = false;
        }

        profile.setFavouriteSport(Sport.convertToSport(activityValue));
    }
}