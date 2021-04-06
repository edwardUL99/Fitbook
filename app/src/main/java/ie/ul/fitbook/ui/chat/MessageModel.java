package ie.ul.fitbook.ui.chat;

import java.text.SimpleDateFormat;
import java.util.Date;

import ie.ul.fitbook.ui.home.Model;

public class MessageModel implements Comparable<MessageModel>{


    String sender, content, timeStamp;

    public MessageModel(String sender, String content, String timeStamp){


        this.sender = sender;
        this.content = content;
        this.timeStamp = timeStamp;

    }

    public String getDate(){

        long num = Long.parseLong(timeStamp);


        SimpleDateFormat formatter= new SimpleDateFormat("dd-MM-yyyy 'at' HH:mm:ss");

        Date date = new Date(num);
        String dateString = formatter.format(date);

        return dateString;

    }

    public String getSender(){

        return sender;
    }

    public String getContent(){

        return content;
    }
    public String getTime() {
        return timeStamp;
    }


    @Override
    public int compareTo(MessageModel o) {
        if (getTime() == null || o.getTime() == null) {
            return 0;
        }
        return getTime().compareTo(o.getTime());
    }
}
