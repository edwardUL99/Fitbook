package ie.ul.fitbook.ui.chat;

/**
 * FriendModel class used to create FriendModel objects for messages recyclerview
 */
public class FriendModel implements Comparable<FriendModel>{
    /**
     * userId field is the userId of the user
     */
    String userId;
    /**
     * This timestamp is the timeStamp of the document and it re-set again and again. Messages
     * in the subcolleciton have their timeStamps also and the most recent one of these sets our
     * friend's timeStamp field in this document. This is used to sort chats with users
     */
    String timeStamp;
    //ImageView profileImage;

    /**
     * FriendModel constuctor using just userId
     * @param userId
     */
    public FriendModel(String userId){
        this.userId = userId;
    }

    /**
     * FriendModel constructor user userId, timeStamp
     * @param userId
     * @param timeStamp
     */
    public FriendModel(String userId, String timeStamp){
        this.userId = userId;
        this.timeStamp = timeStamp;
    }

    /**
     * Returns the userId
     * @return
     */

    public String getUserId(){

        return userId;
    }

    /**
     * Returns the timeStamp, used in comparator
     * @return
     */

    public String getTime(){
        return timeStamp;
    }

    /**
     * Override of compareTo method of comparator
     * @param o
     * @return
     */
    @Override
    public int compareTo(FriendModel o) {
        if (getTime() == null || o.getTime() == null) {
            return 0;
        } else
            return getTime().compareTo(o.getTime());
    }
}
