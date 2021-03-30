package ie.ul.fitbook.ui.home;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import ie.ul.fitbook.R;
import ie.ul.fitbook.login.Login;

public class AddPost extends AppCompatActivity {

    EditText t3;
    Button b2;
    FirebaseFirestore db;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        EditText t3 = findViewById(R.id.textView3);
        Button b2 = findViewById(R.id.button2);
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
    }

    private void uploadData(String userId, String postText) {

        Map<String, Object> post = new HashMap<>();
        post.put("userId", userId);
        post.put("post", postText);

        db.collection("posts")
                .add(post)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(AddPost.this, "Post added!", Toast.LENGTH_SHORT).show();


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