package ie.ul.fitbook.ui.chat;

import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ie.ul.fitbook.login.Login;

import static java.lang.Integer.parseInt;

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

//    public String getTimeStamp(String loginId){
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        db.collection("users/" + loginId +"/messages/" + userId + "/recent")
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//
//                        for(DocumentSnapshot doc: task.getResult()){
//
//
//                            String s =doc.getString(String.valueOf("timeStamp"));
//
//
//                        }
//
//
//
//
//
//
//                    }
//
//
//                });
//
//
//
//
//
//
//
//
//    }

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
