package ie.ul.fitbook.ui.profile.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import ie.ul.fitbook.profile.Profile;

/**
 * This provides a ViewModel for constructing our profile during a ProfileCreationActivity
 */
public class ProfileViewModel extends ViewModel {
    /**
     * Our data within this view model
     */
    private final MutableLiveData<Profile> profileData = new MutableLiveData<>();

    /**
     * Selects the provided profile to be a part of this view model
     * @param profile the profile to select
     */
    public void selectProfile(Profile profile) {
        profileData.setValue(profile);
    }

    /**
     * Returns the profile data that is selected by this view model
     * @return the selected profile
     */
    public LiveData<Profile> getSelectedProfile() {
        return profileData;
    }
}
