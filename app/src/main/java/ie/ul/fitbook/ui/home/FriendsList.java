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

public class FriendsList extends AppCompatActivity {

    List<FriendModel> friendModelList;
    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager layoutManager;
    FirebaseFirestore db;
    FriendsListCustomAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;


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
