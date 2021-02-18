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
import android.widget.Button;
import android.widget.EditText;

import ie.ul.fitbook.R;
import ie.ul.fitbook.login.Login;
import ie.ul.fitbook.profile.Profile;
import ie.ul.fitbook.ui.profile.ProfileCreationActivity;
import ie.ul.fitbook.ui.profile.viewmodels.ProfileViewModel;

/**
 * The fragment for entering user's biography
 */
public class BiographyFragment extends Fragment {
    /**
     * The profile being edited
     */
    private Profile profile;
    /**
     * The text field for the biography
     */
    private EditText biographyField;
    /**
     * The activity behind this fragment
     */
    private FragmentActivity activity;
    /**
     * A flag to keep track of if we're editing
     */
    private boolean editing;


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

        this.activity = fragmentActivity;
        editing = activity.isInEditMode();

        setupProfile();

        Button cancel = view.findViewById(R.id.cancel1);
        cancel.setOnClickListener(v -> activity.onCancel());

        Button next = view.findViewById(R.id.next1);
        next.setOnClickListener(v -> onNext(view));

        biographyField = view.findViewById(R.id.biographyTextField);
        fillFieldsWithProfile();
    }

    /**
     * Set up the profile instance being edited
     */
    private void setupProfile() {
        if (!editing) {
            ProfileViewModel profileViewModel = new ViewModelProvider(this.activity).get(ProfileViewModel.class);

            Profile profile = profileViewModel.getSelectedProfile().getValue();
            if (profile == null)
                throw new IllegalStateException("On the BiographyFragment stage of ProfileCreationActivity, you should have a profile being edited");

            this.profile = profile;
        } else {
            Profile loggedIn = Login.getProfile();

            if (loggedIn == null)
                throw new IllegalStateException("Login.getProfile() returned null, has the logged in user's profile been set?");

            this.profile = loggedIn;
        }
    }

    /**
     * This method fills the fields with information from the logged in profile
     */
    private void fillFieldsWithProfile() {
        if (editing) {
            biographyField.setText(profile.getBio());
        }
    }

    /**
     * The button handler for the next screen
     * @param view the view for the fragment (not the button on click listener)
     */
    private void onNext(View view) {
        String biography = biographyField.getText().toString();
        profile.setBio(biography);

        Navigation.findNavController(view).navigate(R.id.action_biographyFragment_to_athleticInformationFragment);
    }
}