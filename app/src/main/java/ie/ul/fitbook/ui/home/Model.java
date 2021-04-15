package ie.ul.fitbook.ui.home;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Model implements Comparable<Model>{

    String id, userId, post, timeStamp;

    public Model(){
}
    public Model(String id, String userId, String post, String timeStamp){

        this.id = id;
        this.userId = userId;
        this.post = post;
        this.timeStamp = timeStamp;
        //this.profileImage = profileImage;

    }

    public String getDate(){

         long num = Long.parseLong(timeStamp);


        SimpleDateFormat formatter= new SimpleDateFormat("dd-MM-yyyy 'at' HH:mm:ss");

        Date date = new Date(num);
        String dateString = formatter.format(date);

        return dateString;

    }






    //public String getId() {
      //  return id;
    //}

//    public void setId(String id) {
//        this.id = id;
//    }


    public String getId() {
    return id;
}


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPost() {
        return post;
    }

    public String getTime() throws ParseException {
        return timeStamp;
    }

    public void setPost(String post) {
        this.post = post;
    }


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
