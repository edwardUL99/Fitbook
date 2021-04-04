package ie.ul.fitbook.ui.chat;

public class MessageModel {


    String sender, content;

    public MessageModel(String sender, String content){


        this.sender = sender;
        this.content = content;

    }

    public String getSender(){

        return sender;
    }

    public String getContent(){

        return content;
    }


}
