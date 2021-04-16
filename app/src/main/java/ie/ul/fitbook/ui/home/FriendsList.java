package ie.ul.fitbook.ui.home;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
 * A FriendList class for displaying friends list
 */

public class FriendsList extends AppCompatActivity {
    /**
     * An array list of type FriendModel is generated here for the adpater
     */
    List<FriendModel> friendModelList;

    /**
     * A RecyclerView mRecyclerView
     */
    RecyclerView mRecyclerView;

    /**
     * A LayoutManager layoutManager
     */
    RecyclerView.LayoutManager layoutManager;

    /**
     * An instance of firestore db
     */
    FirebaseFirestore db;

    /**
     * A FriendsListCustomAdapter adapter
     */
    FriendsListCustomAdapter adapter;

    /**
     * A SwipeRefreshLayout swipeRefreshLayout
     */
    private SwipeRefreshLayout swipeRefreshLayout;

    /**
     * This hook is called whenever an item in your options menu is selected.
     * The default implementation simply returns false to have the normal
     * processing happen (calling the item's Runnable or sending a message to
     * its Handler as appropriate).  You can use this method for any items
     * for which you would like to do processing without those other
     * facilities.
     *
     * <p>Derived classes should call through to the base class for it to
     * perform the default menu handling.</p>
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to
     * proceed, true to consume it here.
     * @see #onCreateOptionsMenu
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle("Friends List");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mRecyclerView = findViewById(R.id.recyclerView2);
        mRecyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(FriendsList.this);
        mRecyclerView.setLayoutManager(layoutManager);
        db = FirebaseFirestore.getInstance();

        swipeRefreshLayout = findViewById(R.id.friendsRefresh);
        swipeRefreshLayout.setOnRefreshListener(() -> {

            friendModelList.clear();
            showData();


            swipeRefreshLayout.setRefreshing(false);
        });
        friendModelList = new ArrayList<>();


        showData();


    }

    /**
     * Grabs friends information from the database and populates an array list of type friend model by it.
     * This array list is then sent to the adapter for the recycler view
     */

    private void showData(){
        db.collection("users/" + Login.getUserId() +"/friends")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for(DocumentSnapshot doc: task.getResult()){
                            FriendModel model = new FriendModel(doc.getId());
                            if(doc.getString("status").equals("accepted")){
                            friendModelList.add(model);}
                        }
                        adapter = new FriendsListCustomAdapter(FriendsList.this, friendModelList);
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
