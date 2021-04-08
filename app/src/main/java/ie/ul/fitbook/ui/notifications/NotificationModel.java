package ie.ul.fitbook.ui.notifications;

public class NotificationModel{

    private String notificationType;
    private String userId;

    public NotificationModel(String notificationType, String userId){
        this.notificationType = notificationType;
        this.userId = userId;
    }

    public String getNotificationType(){
        return notificationType;
    }

    public String getUserId(){
        return userId;
    }

}