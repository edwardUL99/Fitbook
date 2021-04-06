package ie.ul.fitbook.ui.home;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
import java.sql.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import ie.ul.fitbook.R;
import ie.ul.fitbook.login.Login;

public class AddPost extends AppCompatActivity {

    private static final int  PICK_IMAGE_REQUEST = 1;
    EditText t3;
    Button b2, imageButton;
    FirebaseFirestore db;
    ImageView imageView;
    Uri imageUri;
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

        db = FirebaseFirestore.getInstance();

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String post = t3.getText().toString().trim();
                String userId = Login.getUserId();
                uploadData(userId, post);
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
                        Toast.makeText(AddPost.this, "DocumentSnapshot written with ID: "
                                + documentReference.getId(), Toast.LENGTH_SHORT).show();

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


                        //Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(AddPost.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                });




    }
    //Toast.makeText(AddPost.this, "Not working", Toast.LENGTH_SHORT).show();
}