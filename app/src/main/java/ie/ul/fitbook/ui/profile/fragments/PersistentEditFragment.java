package ie.ul.fitbook.ui.profile.fragments;

import ie.ul.fitbook.profile.Profile;

/**
 * This interface specifies a fragment where it's edits can be persisted to the profile object being edited.
 * This interface is useful to have so that on a back pressed when navigating through the profile edit/creation
 * processes any changes can be saved, so that when you navigate back to the page, fields are filled with the correct values
 */
public interface PersistentEditFragment {
    /**
     * This method is used to save the edit state of the current editing page to the provided profile
     * object.
     * @param profile the profile that any edits should be saved to
     */
    void saveEditState(Profile profile);
}
