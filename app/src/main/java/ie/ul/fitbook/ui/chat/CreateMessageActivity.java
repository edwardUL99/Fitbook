package ie.ul.fitbook.ui.chat;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import ie.ul.fitbook.R;
import ie.ul.fitbook.login.Login;


/**
 * This activity runs when a user clicks the whatsappy like thing in the messages fragment.
 * The purpose of this activity is to display a recycler view of friends belonging to the user but
 * that the user has not messaged before. If a user sends a message to one of these friends, they will
 * be removed from this recycler view and inserted into the one corresponding to the message fragment recycler.
 */
public class CreateMessageActivity extends AppCompatActivity {
    /**
     * An list of friends for this particular user
     */
    List<FriendModel> friendModelList;

    /**
     * A recycler view belonging to this activity
     */

    RecyclerView mRecyclerView;
    /**
     * A layout manager belong to this activity
     */
    RecyclerView.LayoutManager layoutManager;
    /**
     * An instance of FirebaseFirestore belong to this activity
     */
    FirebaseFirestore db;
    /**
     * An adapter belonging to this activity
     */
    FriendsListCustomAdapter adapter;

    /**
     *
     * @param savedInstanceState
     * The onCreate call for this activity
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_message);
/**
 * An ActionBar belonging to this activity
 */
        ActionBar actionBar = getSupportActionBar();
        /**
         * Sets the title for this action bar title for this activity
         */

        if (actionBar != null) {
            actionBar.setTitle("Select Contact");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mRecyclerView = findViewById(R.id.recyclerView4);
        mRecyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(CreateMessageActivity.this);
        mRecyclerView.setLayoutManager(layoutManager);
        db = FirebaseFirestore.getInstance();
        friendModelList = new ArrayList<>();


        showData();
    }

    /**
     *
     * @param item
     * @return
     * This sets the back button for this activity
     */

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * This method generates the friendModelList with calls to the firestore database
     * Friends who exist in the collection "unmessaged" are added to the list before it
     * is given to the adapater.
     *
     * Probably could have sorted friends alphabetically here or something, but just forgot to do it
     */
    private void showData(){



        db.collection("users/" + Login.getUserId() +"/unmessaged")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        for(DocumentSnapshot doc: task.getResult()){


                            FriendModel model = new FriendModel(doc.getId());
                            friendModelList.add(model);
                        }

                        //Collections.sort(modelList);
                        //Collections.reverse(modelList);


                        adapter = new FriendsListCustomAdapter(CreateMessageActivity.this, friendModelList);
                        mRecyclerView.setAdapter(adapter);



                    }

                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });



    }
}
