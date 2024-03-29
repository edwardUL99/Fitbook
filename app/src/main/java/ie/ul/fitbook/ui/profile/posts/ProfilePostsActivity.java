package ie.ul.fitbook.ui.profile.posts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import ie.ul.fitbook.R;
import ie.ul.fitbook.ui.custom.LoadingBar;
import ie.ul.fitbook.login.Login;
import ie.ul.fitbook.profile.Profile;
import ie.ul.fitbook.ui.home.CustomAdapter;
import ie.ul.fitbook.ui.home.Model;
import ie.ul.fitbook.utils.ProfileUtils;

/**
 * This class displays posts for a specified profile
 */
public class ProfilePostsActivity extends AppCompatActivity {
    /**
     * Use this extra to pass in the user id of the user to display users of. If this is not passed in,
     * the uid is assumed to be that of the currently logged in user.
     */
    public static final String USER_ID_EXTRA = "ie.ul.fitbook.POSTS_USER_ID";

    /**
     * The reference to a user's posts
     */
    private CollectionReference postsReference;
    /**
     * The layout holding our recycler view
     */
    private SwipeRefreshLayout swipeRefreshLayout;
    /**
     * The recycler view for the posts
     */
    private RecyclerView recyclerView;
    /**
     * The progress bar to display on loading
     */
    private LoadingBar progressBar;
    /**
     * The profile downloaded for this user
     */
    protected Profile profile;
    /**
     * The userId for this activity
     */
    private String userId;
    /**
     * The posts adapter for displaying them
     */
    private CustomAdapter postsAdapter;
    /**
     * The text view displaying that there's no posts
     */
    private TextView noPostsMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_posts);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Posts");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent received = getIntent();

        String userId;

        if (received.hasExtra(USER_ID_EXTRA)) {
            userId = received.getStringExtra(USER_ID_EXTRA);
        } else {
            userId = Login.getUserId();
        }

        if (userId == null)
            throw new IllegalStateException("No UserID has been passed to this Activity");

        this.userId = userId;

        postsReference = FirebaseFirestore.getInstance().collection("posts");

        noPostsMessage = findViewById(R.id.noPostsMessage);
        noPostsMessage.setText("No posts found");

        recyclerView = findViewById(R.id.postsRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ConstraintLayout postsContainer = findViewById(R.id.postsContainer);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setLoadedLayout(postsContainer);

        swipeRefreshLayout = findViewById(R.id.postsRefresh);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            progressBar.show();

            if (profile == null) {
                loadProfile();
            } else {
                getPosts();
            }

            swipeRefreshLayout.setRefreshing(false);
        });

        progressBar.show();
        loadProfile();
    }

    /**
     * Handles profile load
     * @param profile the profile to load
     */
    private void onProfileLoad(Profile profile) {
        this.profile = profile;
        getPosts();
    }

    /**
     * Handle load of activities failure
     * @param e an exception if any
     */
    private void onLoadFailed(Exception e) {
        if (e != null)
            e.printStackTrace();

        Toast.makeText(this, "An error occurred retrieving posts", Toast.LENGTH_SHORT)
                .show();
        progressBar.hideBoth();
    }

    /**
     * Retrieves the profile object
     */
    private void loadProfile() {
        ProfileUtils.downloadProfile(userId, this::onProfileLoad, () -> onLoadFailed(null),
                null, this, false);
    }

    /**
     * Retrieves the posts
     */
    private void getPosts() {
        List<Model> modelList = new ArrayList<>();
        postsReference
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(success -> {
                    List<DocumentSnapshot> snapshots = success.getResult().getDocuments();

                    if (snapshots.size() == 0) {
                        noPostsMessage.setVisibility(View.VISIBLE);
                    } else {
                        noPostsMessage.setVisibility(View.GONE);
                        for (DocumentSnapshot snapshot : success.getResult().getDocuments()) {
                            Model model = new Model(snapshot.getId(), snapshot.getString("userId"), snapshot.getString("post"),
                                    String.valueOf(snapshot.get("createdAt")));
                            modelList.add(model);
                        }

                        postsAdapter = new CustomAdapter(this, modelList, profile);
                        recyclerView.setAdapter(postsAdapter);
                    }

                    progressBar.hide();
                })
                .addOnFailureListener(this::onLoadFailed);
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
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}