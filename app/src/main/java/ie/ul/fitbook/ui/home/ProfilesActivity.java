package ie.ul.fitbook.ui.home;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.MenuItem;

import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ie.ul.fitbook.R;
import ie.ul.fitbook.database.UserDatabase;
import ie.ul.fitbook.login.Login;
import ie.ul.fitbook.profile.Profile;

/**
 * This activity provides an activity for displaying and searching profiles
 * Honestly this acitivity again could have been a bit more fleshed out and if I had more time, I would have
 */
public class ProfilesActivity extends AppCompatActivity {

    /**
     * A SearchView searchView
     */


    SearchView searchView;

    /**
     * An instance of firestore db
     */
    FirebaseFirestore db;

    /**
     * An array lsit of type FriendModel
     */
    List<FriendModel> friendModelList;

    /**
     * A SearchAdapter adapter
     */
    SearchAdapter adapter;

    /**
     * A RecyclerView mRecyclerView
     */
    RecyclerView mRecyclerView;

    /**
     * A LayoutManager layoutManager
     */
    RecyclerView.LayoutManager layoutManager;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profiles);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Profiles");
        }

        searchView = findViewById(R.id.searchView);
        db = FirebaseFirestore.getInstance();
        friendModelList = new ArrayList<>();

        mRecyclerView = findViewById(R.id.recyclerView3);
        mRecyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(ProfilesActivity.this);
        mRecyclerView.setLayoutManager(layoutManager);

        adapter = new SearchAdapter(ProfilesActivity.this, friendModelList);
        mRecyclerView.setAdapter(adapter);

        searchView.setOnQueryTextListener(new OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.clear();
                searchData(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

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

    /**
     * This method searches the db using our string from SearchView
     * Honestly I did not implement this correctly, I was at this point in time
     * unaware of the whereEqualTo query. It works fine though. Again, I would have changed this if I had time
     * @param s
     */

    private void searchData(String s){

        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        for(DocumentSnapshot doc: task.getResult()){

                            new UserDatabase(doc.getId()).getChildDocument(Profile.PROFILE_DOCUMENT)
                                    .get()
                                    .addOnCompleteListener(innerTask -> {
                                        if (innerTask.isSuccessful()) {

                                            DocumentSnapshot snapshot = innerTask.getResult();
                                            Map<String, Object> data = snapshot.getData();
                                            Profile profile = Profile.from(data);

                                            String name = profile.getName();
                                            if(s.length() <= name.length()){
                                                String substring = name.substring(0,s.length()).toLowerCase();

                                                if(substring.equals(s.toLowerCase()) && !doc.getId().equals(Login.getUserId())){
                                                    FriendModel model = new FriendModel(doc.getId());
                                                    adapter.addFriend(model);
                                                }
                                            }
                                        }
                                    });
                            }
                    }
                });
        }
    }