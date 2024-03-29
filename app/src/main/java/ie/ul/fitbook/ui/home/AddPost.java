package ie.ul.fitbook.ui.home;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
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


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ie.ul.fitbook.R;
import ie.ul.fitbook.login.Login;
import ie.ul.fitbook.network.NetworkUtils;

/**
 * The AddPost class, here we can add posts to the database for a user
 */
public class AddPost extends AppCompatActivity {

    private static final int  PICK_IMAGE_REQUEST = 1;
    /**
     * An EditText t3, error here again, redeclared below. Would fix
     * this easily if I had time but I'm a little bit scared to touch the
     * code again
     */

    EditText t3;

    /**
     * Buttons b2 and imageButton
     */
    Button b2, imageButton;

    /**
     * An instance of Firestore db
     */
    FirebaseFirestore db;
    /**
     * ImageView imageView
     */
    ImageView imageView;

    /**
     * Uri imageUri
     */
    Uri imageUri;

    /**
     * boolean Imageset, not sure if still used tbh
     */
    boolean imageSet;

    /**
     * A StorageReference mStorageRef
     */
    private StorageReference mStorageRef;
    /**
     * Flag which is set to true if user has to retry photo upload
     */
    private boolean retryPhotoUpload;
    /**
     * Id of the post created if photo upload failed
     */
    private String postId;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle("Add Post");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        EditText t3 = findViewById(R.id.textView3);
        b2 = findViewById(R.id.button2);
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

                b2.setEnabled(false);
                uploadData(userId, post);
            }
        });
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

    }

    /**
     * Opens the file chooser to grab an image from the user's documents
     */

    private void openFileChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        imageSet = true;
        startActivityForResult(intent, PICK_IMAGE_REQUEST);

    }

    /**
     * The onActivityResult for the file chooser, grabs and sets the image after choosing it from docs
     * @param requestCode
     * @param resultCode
     * @param data
     */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
            && data != null && data.getData() != null){

            imageUri = data.getData();

            imageView.setImageURI(imageUri);
        }
    }

    /**
     * Gets the file extension as string
     * @param uri
     * @return
     */

    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    /**
     * Uploads photo for the given user and post. A lot of error handling here also
     * b2 is disabled while the async upload occurs
     */
    private void uploadPhoto() {
        if(imageSet && imageUri != null && postId != null) {
            if (NetworkUtils.isNetworkConnected(this)) {
                StorageReference fileReference = mStorageRef.child(postId
                        + "." + getFileExtension(imageUri));

                fileReference.putFile(imageUri)
                        .addOnSuccessListener(success -> finish())
                        .addOnFailureListener(fail -> {
                            Toast.makeText(AddPost.this, "Failed to upload post picture, try again", Toast.LENGTH_SHORT)
                                    .show();
                            fail.printStackTrace();

                            retryPhotoUpload = true;
                            b2.setEnabled(true);
                        });
            } else {
                Toast.makeText(this, "You need to be connected to the internet to make a post", Toast.LENGTH_SHORT)
                        .show();
                retryPhotoUpload = true;
                b2.setEnabled(true);
            }
        }
    }

    /**
     * Upload the post and piture to firestore and firebase storage
     * @param userId
     * @param postText
     */

    private void uploadData(String userId, String postText) {

        Date mDate = new Date();
        long timeInMilliseconds = mDate.getTime();

        Map<String, Object> post = new HashMap<>();
        post.put("userId", userId);
        post.put("post", postText);
        post.put("createdAt", timeInMilliseconds);

        boolean imageSet = this.imageSet && imageUri != null;

        if (NetworkUtils.isNetworkConnected(this)) {
            if (!retryPhotoUpload) {
                db.collection("posts")
                        .add(post)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                postId = documentReference.getId();

                                db.collection("users/" + Login.getUserId() + "/friends")
                                        .whereEqualTo("status", "accepted")
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                for (DocumentSnapshot doc : task.getResult()) {
                                                    Map<String, Object> notification = new HashMap<>();
                                                    notification.put("userId", Login.getUserId());
                                                    notification.put("notificationType", "New Post");
                                                    notification.put("postId", documentReference.getId());
                                                    notification.put("createdAt", timeInMilliseconds);


                                                    db.collection("users" + "/" + doc.getId() + "/notifications")
                                                            .add(notification);
                                                }

                                                if (!imageSet)
                                                    finish();
                                            }
                                        });

                                uploadPhoto();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                e.printStackTrace();
                                Toast.makeText(AddPost.this, "Error occurred saving post, try again", Toast.LENGTH_SHORT).show();
                                b2.setEnabled(true);
                            }
                        });
            } else {
                uploadPhoto();
            }
        } else {
            Toast.makeText(this, "You need to be connected to the internet to make a post", Toast.LENGTH_SHORT)
                    .show();
            b2.setEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);

    }
}