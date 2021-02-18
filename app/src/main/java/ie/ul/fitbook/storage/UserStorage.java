package ie.ul.fitbook.storage;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;

/**
 * This class represents storage for the current user
 */
public class UserStorage extends Storage {
    /**
     * Constructs an UserStorage instance
     * @throws IllegalStateException if Firebase current user is null
     */
    protected UserStorage() {
        super(getStoragePath());
    }

    /**
     * Retrieves the storage path to use
     * @return the main storage path
     * @throws IllegalStateException if Firebase current user is null
     */
    private static String getStoragePath() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null)
            throw new IllegalStateException("Cannot create a UserStorage for a user that is not logged in");

        return "users/" + user.getUid();
    }

    /**
     * Gets the main storage folder reference
     *
     * @return main storage folder reference
     */
    @Override
    public StorageReference getStorageFolder() {
        return storage.getReference(mainFolder);
    }

    /**
     * Gets the main storage child reference
     *
     * @param childFolder the child folder
     * @return the storage reference for the folder
     */
    @Override
    public StorageReference getChildFolder(String childFolder) {
        return storage.getReference(mainFolder + "/" + childFolder);
    }
}
