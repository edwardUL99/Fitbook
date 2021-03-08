package ie.ul.fitbook.database;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * An abstract class providing an abstraction to database access.
 * It abstracts a main collection, e.g /users to being a database
 */
public abstract class Database {
    /**
     * This provides the main collection to search when retrieving other collections
     */
    protected final String mainCollection;
    /**
     * The database instance
     */
    protected final FirebaseFirestore database;

    /**
     * Constructs a Database object with the provided main collection.
     * @param mainCollection the main collection that represents this database, e.g. /users
     */
    protected Database(String mainCollection) {
        this.mainCollection = mainCollection;
        database = FirebaseFirestore.getInstance();
    }

    /**
     * Finds a child collection of the collection this Database represents. E.g. if this
     * database represented the database for a User, the collection name would be appended to
     * /users/[uid]/collectionName.
     * @param collectionName the name of the child collection, e.g /activities
     * @return the collection reference referring to the collectionName
     */
    public abstract CollectionReference getChildCollection(String collectionName);

    /**
     * Gets a child document of the collection this Database represents. E.g. a profile document of a user
     * @param documentName the name of the child document
     * @return the DocumentReference referring to the document
     */
    public abstract DocumentReference getChildDocument(String documentName);

    /**
     * Returns the collection reference referring to this database
     * @return the collection reference representing the database
     */
    public abstract CollectionReference getDatabase();
}
