package ie.ul.fitbook.ui.chat;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ie.ul.fitbook.R;
import ie.ul.fitbook.database.UserDatabase;
import ie.ul.fitbook.login.Login;
import ie.ul.fitbook.profile.Profile;
import ie.ul.fitbook.storage.UserStorage;

/**
 * This is the main message activity. The layouts are inflated here and overlaid
 *
 */

public class MessageActivity extends AppCompatActivity {

    /**
     * We create a array list of type MessageModel here to send to the adapter
     */

    List<MessageModel> modelList;
    /**
     * A profile image for the user
     */
    ImageView profilePic;
    TextView name, address;
    /**
     * A user id for the user
     */
    String userId;
    EditText editText;
    /**
     * The send button
     */
    Button button;
    /**
     * A recvclerview mRecyclerView
     */

    RecyclerView mRecyclerView;
    /**
     * A layout manager layoutManager
     */
    RecyclerView.LayoutManager layoutManager;
    /**
     * A instance of Firestore db
     */
    FirebaseFirestore db;
    /**
     * A adapter for the messages
     */
    MessageAdapter adapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        profilePic = findViewById(R.id.profilePicture9);
        TextView name = findViewById(R.id.address9);
        TextView address = findViewById(R.id.name9);
        userId = getIntent().getStringExtra("userId");
        mRecyclerView = findViewById(R.id.recycler_view_messages);
        mRecyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(MessageActivity.this);
        ((LinearLayoutManager) layoutManager).setReverseLayout(true);
        mRecyclerView.setLayoutManager(layoutManager);
        db = FirebaseFirestore.getInstance();
        modelList = new ArrayList<>();
        EditText editText = findViewById(R.id.textView18);
        button = findViewById(R.id.button18);

        /**
         * Gets the users profile image and loads it into profilePic
         */
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

                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri downloadUrl) {
                                String uri = downloadUrl.toString();
                                Picasso.get().load(uri).into(profilePic);

                            }
                        });
                    }
                });

        showMessages();

        /**
         *
         * On clicking send we have the save the message in both sender and receivers appropriate collections
         * Message is saved with values of sender, content, and timeStamp. Below we have two .add(message) which saves
         * the message in the databases of both users. We also have two .set(recent) which grabs the timeStamp of the message
         * and sets the timeStamp value of the main document to it. This is used for sorting messages. We also send a notification
         * to the recipient that they have received a message
         *
         */

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editText != null)  {
                    String userId = getIntent().getStringExtra("userId");

                    Date mDate = new Date();
                    long timeInMilliseconds = mDate.getTime();

                    Map<String, Object> message = new HashMap<>();
                    message.put("sender", Login.getUserId());
                    message.put("content", editText.getText().toString());
                    message.put("timeStamp", timeInMilliseconds);

                    Map<String, Object> recent = new HashMap<>();
                    recent.put("timeStamp", timeInMilliseconds);


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



                    Map<String, Object> notification = new HashMap<>();
                    notification.put("userId", Login.getUserId());
                    notification.put("notificationType", "New Message");

                    Date mDateMessage = new Date();
                    long timeInMillisecondsMessage = mDateMessage.getTime();

                    notification.put("createdAt", timeInMillisecondsMessage);

                    db.collection("users" + "/" + userId + "/notifications")
                            .add(notification)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {

                                }
                            });


                    showMessages();
                    editText.setText(null);
                }
            }
        });
    }

    /**
     * showMessages method gets messages from the database for a given friend. These are then sorted by timeStamp and sent to the adapter
     */

    public void showMessages(){
        db.collection("users/" + Login.getUserId() +"/messages" + "/" + getIntent().getStringExtra("userId") + "/message")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        modelList.clear();

                        for(DocumentSnapshot doc: task.getResult()){
                            MessageModel model = new MessageModel(doc.getString("sender"), doc.getString("content"),String.valueOf(doc.get("timeStamp")));
                            modelList.add(model);

                        }

                        Collections.sort(modelList);
                        Collections.reverse(modelList);


                        adapter = new MessageAdapter(MessageActivity.this, modelList);
                        mRecyclerView.setAdapter(adapter);

                    }

                });




    }
}
