package ie.ul.fitbook.storage;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * This class provides an abstracted way of accessing Firebase Storage
 */
public abstract class Storage {
    /**
     * This provides the main folder to search when retrieving storage entries
     */
    protected final String mainFolder;
    /**
     * The storage instance
     */
    protected final FirebaseStorage storage;

    /**
     * Constructs a Storage instance
     * @param mainFolder the main folder to start from and construct child folders from
     */
    protected Storage(String mainFolder) {
        this.mainFolder = mainFolder;
        this.storage = FirebaseStorage.getInstance();
    }

    /**
     * Gets the main storage folder reference
     * @return main storage folder reference
     */
    public abstract StorageReference getStorageFolder();

    /**
     * Gets the main storage child reference
     * @param childFolder the child folder
     * @return the storage reference for the folder
     */
    public abstract StorageReference getChildFolder(String childFolder);

    /**
     * Returns the storage instance matching the provided store folder
     * @param store the store enum representing the main storage folder
     * @return the appropriate instance
     */
    public static Storage getInstance(Stores store) {
        if (store == Stores.USERS) {
            return new UserStorage();
        }

        return null;
    }
}
