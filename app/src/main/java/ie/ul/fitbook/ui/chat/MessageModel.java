package ie.ul.fitbook.ui.chat;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A MessageModel class used to create MessageModel objects for the MessageActivity recyclerview
 */

public class MessageModel implements Comparable<MessageModel>{


    /**
     * String fields of sender, content, and timeStamp. These are all set in the layout and timeStamp is also used in sorting the messages
     */
    String sender, content, timeStamp;

    /**
     * Constructor
     * @param sender
     * @param content
     * @param timeStamp
     */

    public MessageModel(String sender, String content, String timeStamp){


        this.sender = sender;
        this.content = content;
        this.timeStamp = timeStamp;

    }

    /**
     * A getDate method which takes the timeStamp and returns a dateString for the layout
     * @return
     */

    public String getDate(){

        long num = Long.parseLong(timeStamp);


        SimpleDateFormat formatter= new SimpleDateFormat("dd-MM-yyyy 'at' HH:mm:ss");

        Date date = new Date(num);
        String dateString = formatter.format(date);

        return dateString;

    }

    /**
     * Returns the sender of the message
     * @return
     */

    public String getSender(){

        return sender;
    }

    /**
     * Returns the content of the message
     * @return
     */

    public String getContent(){

        return content;
    }

    /**
     * Returns the timeStamp. Used in the comparator
     * @return
     */
    public String getTime() {
        return timeStamp;
    }

    /**
     * The overrided compareTo method. Used to sort the messages by timeStamp
     * @param o
     * @return
     */


    @Override
    public int compareTo(MessageModel o) {
        if (getTime() == null || o.getTime() == null) {
            return 0;
        }
        return getTime().compareTo(o.getTime());
    }
}
