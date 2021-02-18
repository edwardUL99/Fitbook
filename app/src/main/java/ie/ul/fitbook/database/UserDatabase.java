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
     * @throws IllegalStateException if Firebase current user is null
     */
    protected UserDatabase() {
        super(getCollectionPath());
    }

    /**
     * Retrieves the collection path to use
     * @return the main collection path
     * @throws IllegalStateException if Firebase current user is null
     */
    private static String getCollectionPath() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null)
            throw new IllegalStateException("Cannot create a UserDatabase for a user that is not logged in");

        return "/users/" + user.getUid();
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
     * Returns the collection reference referring to this database
     *
     * @return the collection reference representing the database
     */
    @Override
    public CollectionReference getDatabase() {
        return database.collection(mainCollection);
    }
}
