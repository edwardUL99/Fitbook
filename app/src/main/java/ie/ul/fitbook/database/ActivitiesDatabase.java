package ie.ul.fitbook.database;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;

/**
 * This class represents the class to save and retrieve activities
 */
public class ActivitiesDatabase extends Database {
    /**
     * Constructs a Database object with the provided main collection
     */
    public ActivitiesDatabase() {
        super("/activities/");
    }

    /**
     * Finds a child collection of the collection this Database represents. E.g. if this
     * database represented the database for a User, the collection name would be appended to
     * /users/[uid]/collectionName.
     *
     * @param collectionName the name of the child collection, e.g /activities
     * @return the collection reference referring to the collectionName
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
    public CollectionReference getDatabase() {
        return database.collection(mainCollection);
    }
}
