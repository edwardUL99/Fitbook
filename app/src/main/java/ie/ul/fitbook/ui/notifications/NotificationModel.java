package ie.ul.fitbook.ui.notifications;

public class NotificationModel{

    private String notificationType;
    private String userId;
    private String postId;

    public NotificationModel(String notificationType, String userId){
        this.notificationType = notificationType;
        this.userId = userId;
    }

    public NotificationModel(String notificationType, String userId, String postId){
        this.notificationType = notificationType;
        this.userId = userId;
        this.postId = postId;
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
}