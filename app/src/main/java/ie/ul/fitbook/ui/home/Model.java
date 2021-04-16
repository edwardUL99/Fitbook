package ie.ul.fitbook.ui.home;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A Model class used for creating Model objects. These are created in the home fragment for the recycler view
 * RecordingActivity also extends Model
 */

public class Model implements Comparable<Model>{

    /**
     * String fields of id, userId, post, and timeStamp
     * The first id is the post id which must be saved in the Model object
     * in order to later grab the post picture from Firebase Storage which has been
     * saved under a title of this id.
     *
     * The latter fields are as usual, userId, post, and timeStamp.
     */

    String id, userId, post, timeStamp;

    /**
     * An empty constructor
     */

    public Model(){
}

    /**
     * A constructor for the Model class
     * @param id
     * @param userId
     * @param post
     * @param timeStamp
     */
    public Model(String id, String userId, String post, String timeStamp){

        this.id = id;
        this.userId = userId;
        this.post = post;
        this.timeStamp = timeStamp;
        //this.profileImage = profileImage;

    }

    /**
     * Returns a date string after parsing the timeStamp
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
     * Returns the post id
     * @return
     */
    public String getId() {
    return id;
}

    /**
     * Returns the user Id
     * @return
     */


    public String getUserId() {
        return userId;
    }

    /**
     * Sets the userId
     * @param userId
     */

    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Returns the post
     * @return
     */

    public String getPost() {
        return post;
    }

    /**
     * Returns the timeStamp
     * @return
     * @throws ParseException
     */

    public String getTime() throws ParseException {
        return timeStamp;
    }

    /**
     * Sets the post
     * @param post
     */

    public void setPost(String post) {
        this.post = post;
    }

    /**
     * An override of the compareTo method
     * @param o
     * @return
     */


    @Override
    public int compareTo(Model o) {
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
