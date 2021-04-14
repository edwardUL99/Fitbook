package ie.ul.fitbook.ui.chat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ie.ul.fitbook.R;
import ie.ul.fitbook.database.UserDatabase;
import ie.ul.fitbook.login.Login;
import ie.ul.fitbook.profile.Profile;
import ie.ul.fitbook.storage.UserStorage;

public class NewMessageActivity extends AppCompatActivity {

    ImageView profilePic;
    TextView name, address;
    String userId;
    EditText editText;
    Button button;
    FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_message);

        profilePic = findViewById(R.id.profilePicture9);
        TextView name = findViewById(R.id.address9);
        TextView address = findViewById(R.id.name9);
        userId = getIntent().getStringExtra("userId");
        EditText editText = findViewById(R.id.textView17);
        button = findViewById(R.id.button3);
        db = FirebaseFirestore.getInstance();





        new UserDatabase(userId).getChildDocument(Profile.PROFILE_DOCUMENT)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {


                        DocumentSnapshot snapshot = task.getResult();
                        Map<String, Object> data = snapshot.getData();
                        Profile profile = Profile.from(data);
                        name.setText(profile.getName());
                        address.setText(profile.getCity());


                        StorageReference reference = new UserStorage(userId).getChildFolder(Profile.PROFILE_IMAGE_PATH);

                        //StorageReference reference2 = new PostsStorage(id).getChildFolder("jpg");


                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri downloadUrl) {
                                String uri = downloadUrl.toString();
                                //Picasso.get().load(uri).into(holder.postsPic);
                                Picasso.get().load(uri).into(profilePic);

                            }
                        });
                    }
                });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(editText != null)  {
                   String userId = getIntent().getStringExtra("userId");

                   db.collection("users/" + Login.getUserId() + "/unmessaged").document(userId)
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {


                            }

                        });
                  db.collection("users").document( Login.getUserId() + "/messages/" + userId)
                        .set(new HashMap<String, Object>())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                            }
                        });

                   db.collection("users/" + userId + "/unmessaged").document(Login.getUserId())
                           .delete()
                           .addOnSuccessListener(new OnSuccessListener<Void>() {
                               @Override
                               public void onSuccess(Void aVoid) {


                               }

                           });
                   db.collection("users").document( userId + "/messages/" + Login.getUserId())
                           .set(new HashMap<String, Object>())
                           .addOnSuccessListener(new OnSuccessListener<Void>() {
                               @Override
                               public void onSuccess(Void aVoid) {

                               }
                           });

                   Date mDate = new Date();
                   long timeInMilliseconds = mDate.getTime();

                   Map<String, Object> message = new HashMap<>();
                   message.put("sender", Login.getUserId());
                   message.put("content", editText.getText().toString());
                   message.put("timeStamp", timeInMilliseconds);

                   Map<String, Object> recent = new HashMap<>();
                   recent.put("timeStamp", timeInMilliseconds);


                   UserDatabase userDb = new UserDatabase(Login.getUserId());
                   userDb.getChildCollection("messages").document(userId).set(recent).addOnFailureListener(new OnFailureListener() {
                       @Override
                       public void onFailure(@NonNull Exception e) {
                           e.printStackTrace();

                       }
                   });

                   userDb = new UserDatabase(userId);
                   userDb.getChildCollection("messages").document(Login.getUserId()).set(recent).addOnFailureListener(new OnFailureListener() {
                       @Override
                       public void onFailure(@NonNull Exception e) {
                           e.printStackTrace();

                       }
                   });

                   System.out.println("here here" + userId + Login.getUserId());


//                   db.collection("users").document( Login.getUserId() + "/messages/" + userId  + "/message" + "/" + id)
//                           .set(message)
//                           .addOnSuccessListener(new OnSuccessListener<Void>() {
//                               @Override
//                               public void onSuccess(Void aVoid) {
//
//                               }
//                           });
//
//                   db.collection("users").document( userId + "/messages/" + Login.getUserId()  + "/message"+ "/" + id)
//                           .set(message)
//                           .addOnSuccessListener(new OnSuccessListener<Void>() {
//                               @Override
//                               public void onSuccess(Void aVoid) {
//
//                               }
//                           });
                   db.collection("users" + "/" + Login.getUserId() + "/messages/" + userId  + "/message")
                           .add(message)
                           .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                               @Override
                               public void onSuccess(DocumentReference documentReference) {

                               }
                           });

                   db.collection("users" + "/" + userId + "/messages/" + Login.getUserId()  + "/message")
                           .add(message)
                           .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                               @Override
                               public void onSuccess(DocumentReference documentReference) {

                               }
                           });

//                   db.collection("users" + "/" + Login.getUserId() + "/messages").document(userId)
//                           .update({"timeStamp": timeInMilliseconds; });


//                   db.collection("users" + "/" + userId + "/messages/" + Login.getUserId()  + "/rece t")
//                           .add(recent)
//                           .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                               @Override
//                               public void onSuccess(DocumentReference documentReference) {
//
//                               }
//                           });

                   Map<String, Object> notification = new HashMap<>();
                   notification.put("userId", Login.getUserId());
                   notification.put("notificationType", "message");

                   db.collection("users" + "/" + userId + "/notifications")
                           .add(notification)
                           .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                               @Override
                               public void onSuccess(DocumentReference documentReference) {

                               }
                           });


                   Intent intent = new Intent(NewMessageActivity.this, MessageActivity.class);
                   intent.putExtra("userId", userId);
                   startActivity(intent);
                   finish();
}
            }
        });
    }}

