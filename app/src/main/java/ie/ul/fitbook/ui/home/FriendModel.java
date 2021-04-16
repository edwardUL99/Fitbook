package ie.ul.fitbook.ui.home;

/**
 * A FriendModel class for the friendsList,
 * actually I mmight have just saved these
 * as an array of strings thinking about it
 * but I guess I expected more to go in here
 */
public class FriendModel{
    /**
     * A string userId for a friend userId
     */

    String userId;

    /**
     * A constructor
     * @param userId
     */

    public FriendModel(String userId){
        this.userId = userId;
    }

    /**
     * Returns the userId for this friend
     * @return
     */

    public String getUserId(){
        return userId;
    }

}
