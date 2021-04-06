package ie.ul.fitbook.ui.home;

import android.widget.ImageView;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;

import static java.lang.Integer.parseInt;

public class FriendModel{

    String userId;
    //ImageView profileImage;



    public FriendModel(String userId){

        this.userId = userId;


    }


    public String getUserId(){

        return userId;
    }

}
