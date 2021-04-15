package ie.ul.fitbook.ui.notifications;

import java.text.ParseException;

public class NotificationModel implements Comparable<NotificationModel>{

    private String notificationType;
    private String userId;
    private String postId;
    private String timeStamp;

    public NotificationModel(String notificationType, String userId, String timeStamp){
        this.notificationType = notificationType;
        this.userId = userId;
        this.timeStamp = timeStamp;
    }

    public NotificationModel(String notificationType, String userId, String postId, String timeStamp){
        this.notificationType = notificationType;
        this.userId = userId;
        this.postId = postId;
        this.timeStamp = timeStamp;
    }

    public String getNotificationType(){
        return notificationType;
    }

    public String getUserId(){
        return userId;
    }

    public String getPostId() {
        return postId;
    }

    public String getTime() throws ParseException {
        return timeStamp;
    }

    @Override
    public int compareTo(NotificationModel o) {
        try {
            if (getTime() == null || o.getTime() == null) {
                return 0;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        try {
            return getTime().compareTo(o.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
}