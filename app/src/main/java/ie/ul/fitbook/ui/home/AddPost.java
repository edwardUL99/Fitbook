package ie.ul.fitbook.ui.home;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ie.ul.fitbook.R;
import ie.ul.fitbook.login.Login;

public class AddPost extends AppCompatActivity {

    private static final int  PICK_IMAGE_REQUEST = 1;
    EditText t3;
    Button b2, imageButton;
    FirebaseFirestore db;
    ImageView imageView;
    Uri imageUri;
    boolean imageSet;
    private StorageReference mStorageRef;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle("Add Post");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        EditText t3 = findViewById(R.id.textView3);
        Button b2 = findViewById(R.id.button2);
        Button imageButton = findViewById(R.id.imageButton);
        imageView = findViewById(R.id.imageView);
        mStorageRef = FirebaseStorage.getInstance().getReference("posts");
        imageSet=false;

        db = FirebaseFirestore.getInstance();


        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String post = t3.getText().toString().trim();
                String userId = Login.getUserId();

                if(imageSet){
                uploadData(userId, post);
                }
                else{

                    Toast.makeText(AddPost.this, "Must add a picture", Toast.LENGTH_SHORT).show();
                }

                finish();
            }
        });
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });


    }

    private void openFileChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        imageSet = true;
        startActivityForResult(intent, PICK_IMAGE_REQUEST);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);



        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
            && data != null && data.getData() != null){



            imageUri = data.getData();

            imageView.setImageURI(imageUri);
        }
    }

    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadData(String userId, String postText) {

        //Timestamp timeStamp = new Timestamp(System.currentTimeMillis());

        Date mDate = new Date();
        long timeInMilliseconds = mDate.getTime();

        Map<String, Object> post = new HashMap<>();
        post.put("userId", userId);
        post.put("post", postText);
        post.put("createdAt", timeInMilliseconds);

        db.collection("posts")
                .add(post)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {

                        db.collection("users/" + Login.getUserId() +"/friends")
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        for(DocumentSnapshot doc: task.getResult()){
                                            Map<String, Object> notification = new HashMap<>();
                                            notification.put("userId", Login.getUserId());
                                            notification.put("notificationType", "New Post");
                                            notification.put("postId", documentReference.getId());
                                            notification.put("createdAt", timeInMilliseconds);


                                            db.collection("users" + "/" + doc.getId() + "/notifications")
                                                    .add(notification)
                                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                        @Override
                                                        public void onSuccess(DocumentReference documentReference) {

                                                        }
                                                    });
                                        }
                                    }
                                });

                        if(imageUri != null){

                            StorageReference fileReference =mStorageRef.child(documentReference.getId()
                                    + "." + getFileExtension(imageUri));

                            fileReference.putFile(imageUri)
                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(AddPost.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                });

//        db.collection("users/" + Login.getUserId() +"/friends")
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        for(DocumentSnapshot doc: task.getResult()){
//                            Map<String, Object> notification = new HashMap<>();
//                            notification.put("userId", Login.getUserId());
//                            notification.put("notificationType", "userPost");
//
//                            db.collection("users" + "/" + doc.getId() + "/notifications")
//                                    .add(notification)
//                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                                        @Override
//                                        public void onSuccess(DocumentReference documentReference) {
//
//                                        }
//                                    });
//                        }
//                    }
//                });


    }
    //Toast.makeText(AddPost.this, "Not working", Toast.LENGTH_SHORT).show();
}