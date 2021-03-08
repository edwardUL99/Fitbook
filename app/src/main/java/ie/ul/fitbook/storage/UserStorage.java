package ie.ul.fitbook.storage;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;

/**
 * This class represents storage for the current user
 */
public class UserStorage extends Storage {
    /**
     * Constructs a UserStorage object for the provided user ID
     * @param userId the user ID of the user
     */
    public UserStorage(String userId) {
        super(getStoragePath(userId));
    }

    /**
     * Constructs an UserStorage instance
     * @throws IllegalStateException if Firebase current user is null
     */
    public UserStorage() {
        super(getLoggedInStoragePath());
    }

    /**
     * Retrieves the storage path to use
     * @return the main storage path
     * @throws IllegalStateException if Firebase current user is null
     */
    private static String getLoggedInStoragePath() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null)
            throw new IllegalStateException("Cannot create a UserStorage for a user that is not logged in");

        return getStoragePath(user.getUid());
    }

    /**
     * Retrieves the storage path for the provided user ID
     * @param userId the id of the User
     * @return the storage path to use
     */
    private static String getStoragePath(String userId) {
        return "users/" + userId;
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
