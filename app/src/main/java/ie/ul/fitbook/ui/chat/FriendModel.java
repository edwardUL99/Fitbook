package ie.ul.fitbook.ui.chat;

public class FriendModel implements Comparable<FriendModel>{

    String userId;
    String timeStamp;
    //ImageView profileImage;

    public FriendModel(String userId){
        this.userId = userId;
    }

    public FriendModel(String userId, String timeStamp){
        this.userId = userId;
        this.timeStamp = timeStamp;
    }

    public String getUserId(){

        return userId;
    }

    public String getTime(){
        return timeStamp;
    }

    @Override
    public int compareTo(FriendModel o) {
        if (getTime() == null || o.getTime() == null) {
            return 0;
        } else
            return getTime().compareTo(o.getTime());
    }
}
