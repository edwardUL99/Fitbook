package ie.ul.fitbook.database;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;

/**
 * This class represents the user database (i.e. /users collection)
 */
public class UserDatabase extends Database {
    /**
     * Constructs a Database object with the provided main collection and user id of the current FirebaseUser.
     * @param userID the userID to use
     * @throws IllegalStateException if Firebase current user is null
     */
    public UserDatabase(String userID) {
        super(getCollectionPath(userID));
    }

    /**
     * Construct a UserDatabase for the currently logged in user
     * @throws IllegalStateException if there is no logged in user
     */
    public UserDatabase() {
        super(getLoggedInConnectionPath());
    }

    /**
     * Retrieves the collection path to use
     * @return the main collection path
     */
    private static String getCollectionPath(String userID) {
        return "/users/" + userID;
    }

    /**
     * Gets the connection path of the logged in user
     * @return the collection path to use
     * @throws IllegalStateException if Firebase current user is null
     */
    private static String getLoggedInConnectionPath() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null)
            throw new IllegalStateException("Cannot create a UserDatabase for a user that is not logged in");

        return getCollectionPath(user.getUid());
    }

    /**
     * Finds a child collection of the collection this Database represents. E.g. if this
     * database represented the database for a User, the collection name would be appended to
     * /users/[uid]/collectionName.
     *
     * @param collectionName the name of the child collection
     * @return the reference to the collection
     */
    @Override
    public CollectionReference getChildCollection(String collectionName) {
        return database.collection(mainCollection + "/" + collectionName);
    }

    /**
     * Gets a child document of the collection this Database represents. E.g. a profile document of a user
     *
     * @param documentName the name of the child document
     * @return the DocumentReference referring to the document
     */
    @Override
    public DocumentReference getChildDocument(String documentName) {
        return database.document(mainCollection + "/" + documentName);
    }

    /**
     * Returns the reference referring to the directory referred to by this database.
     * This may be a collection or document depending on the database in Firestore we are representing
     *
     * @return the reference representing the database
     */
    @Override
    public DocumentReference getDatabase() {
        return database.document(mainCollection);
    }
}
