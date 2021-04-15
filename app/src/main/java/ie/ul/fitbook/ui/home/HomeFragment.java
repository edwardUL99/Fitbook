package ie.ul.fitbook.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ie.ul.fitbook.R;
import ie.ul.fitbook.recording.RecordedActivity;
import ie.ul.fitbook.ui.custom.LoadingBar;
import ie.ul.fitbook.ui.notifications.NotificationsActivity;

public class HomeFragment extends Fragment {

    List<Model> modelList;
    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager layoutManager;
    FirebaseFirestore db;
    CustomAdapter adapter;
    String notificationId;
    private SwipeRefreshLayout swipeRefreshLayout;
    /**
     * The progress bar for saying the posts are loading
     */
    private LoadingBar loadingBar;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);

    }

    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * has returned, but before any saved state has been restored in to the view.
     * This gives subclasses a chance to initialize themselves once
     * they know their view hierarchy has been completely created.  The fragment's
     * view hierarchy is not however attached to its parent at this point.
     *
     * @param view               The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        Activity activity = getActivity();

        FloatingActionButton ab = view.findViewById(R.id.add_fab);
        //EditText textView = view.findViewById(R.id.textView5);
        ab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddPost.class);
                startActivity(intent);
                activity.overridePendingTransition(0, 0);
            }
        });

        loadingBar = view.findViewById(R.id.loadingBar);
        ConstraintLayout postsContainer = view.findViewById(R.id.postsContainer);
        loadingBar.setLoadedLayout(postsContainer);

        mRecyclerView = view.findViewById(R.id.recyclerViewNotification);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(null);
        swipeRefreshLayout = view.findViewById(R.id.homeRefresh);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            //getActivity().getIntent().removeExtra("postId");
            notificationId = null;
            showData();

            swipeRefreshLayout.setRefreshing(false);
        });
        db = FirebaseFirestore.getInstance();
        modelList = new ArrayList<>();

        notificationId = activity.getIntent().getStringExtra("postId");
        //Toast.makeText(getActivity(), notificationId, Toast.LENGTH_SHORT).show();
    }

    /**
     * Called when the fragment is visible to the user and actively running.
     */
    @Override
    public void onResume() {
        super.onResume();
        showData();
    }

    /**
     * Initialize the contents of the Fragment host's standard options menu.  You
     * should place your menu items in to <var>menu</var>.  For this method
     * to be called, you must have first called {@link #setHasOptionsMenu}.
     * for more information.
     *
     * @param menu     The options menu in which you place your items.
     * @param inflater
     * @see #setHasOptionsMenu
     * @see #onPrepareOptionsMenu
     * @see #onOptionsItemSelected
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.action_bar_home, menu);
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
     * perform the default menu handling.
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to
     * proceed, true to consume it here.
     * @see #onCreateOptionsMenu
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        Activity activity = requireActivity();
        if (id == R.id.notifications) {
            Intent intent = new Intent(activity, NotificationsActivity.class);
            startActivity(intent);

            return true;
        } else if (id == R.id.profiles) {
            Intent intent = new Intent(activity, ProfilesActivity.class);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onLoadFail(Exception e) {
        if (e != null)
            e.printStackTrace();
        loadingBar.hideBoth();
        Toast.makeText(getActivity(), "An error occurred loading posts", Toast.LENGTH_SHORT)
                .show();
    }

    private void showData() {
        loadingBar.show();
        modelList.clear();

        db.collection("posts")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        for(DocumentSnapshot doc: task.getResult()){
                            Model model = new Model(doc.getId(), doc.getString("userId"),doc.getString("post"), String.valueOf(doc.get("createdAt")));
                            modelList.add(model);
                         }

                        db.collection("activities")
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task2) {
                                mRecyclerView.setAdapter(null);

                                for(DocumentSnapshot doc: task2.getResult()){
                                    Map<String, Object> data = doc.getData();
                                  
                                    if (data != null) {
                                        RecordedActivity model = RecordedActivity.from(data);

                                        if (model != null) {
                                            model.setActivityPostId(doc.getId());
                                            modelList.add(model);
                                        }
                                    }
                                }

                                Collections.sort(modelList);
                                Collections.reverse(modelList);
                              
                                int scrollPosition = 0;
                                if(notificationId != null) {
                                    for(int i = 0; i<modelList.size()-1; i++){
                                        if(modelList.get(i).getId().equals(notificationId)) {
                                            scrollPosition = i;
                                            break;
                                        }
                                    }
                                }
                              
                                adapter = new CustomAdapter(getActivity(), modelList);
                                mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                                mRecyclerView.setAdapter(adapter);
                                loadingBar.hide();
                                if(notificationId != null) {
                                    mRecyclerView.scrollToPosition(scrollPosition);
                                }
                                getActivity().getIntent().removeExtra("postId");
                            }
                        })
                        .addOnFailureListener(fail -> onLoadFail(fail));
                    }
                })
                .addOnFailureListener(this::onLoadFail);
    }
}