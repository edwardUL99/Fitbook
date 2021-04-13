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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ie.ul.fitbook.R;
import ie.ul.fitbook.recording.RecordedActivity;
import ie.ul.fitbook.ui.notifications.NotificationsActivity;

public class HomeFragment extends Fragment {

    List<Model> modelList;
    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager layoutManager;
    FirebaseFirestore db;
    CustomAdapter adapter;
    String notificationId;
    private SwipeRefreshLayout swipeRefreshLayout;

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

        FloatingActionButton ab = view.findViewById(R.id.add_fab);
        //EditText textView = view.findViewById(R.id.textView5);
        ab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddPost.class);
                startActivity(intent);
                ((Activity) getActivity()).overridePendingTransition(0, 0);
            }
        });

        mRecyclerView = view.findViewById(R.id.recyclerViewNotification);
        mRecyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        swipeRefreshLayout = getActivity().findViewById(R.id.homeRefresh);
        swipeRefreshLayout.setOnRefreshListener(() -> {

            modelList.clear();
            showData();


            swipeRefreshLayout.setRefreshing(false);
        });
        db = FirebaseFirestore.getInstance();
        modelList = new ArrayList<>();

        notificationId = getActivity().getIntent().getStringExtra("postId");
        //Toast.makeText(getActivity(), notificationId, Toast.LENGTH_SHORT).show();
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

        if (id == R.id.notifications) {
            Intent intent = new Intent(requireActivity(), NotificationsActivity.class);
            startActivity(intent);

            return true;
        } else if (id == R.id.profiles) {
            Intent intent = new Intent(requireActivity(), ProfilesActivity.class);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void showData(){

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
                                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task2) {


                                for(DocumentSnapshot doc: task2.getResult()){
                                    //Model model = new ActivitiesModel(doc.getDouble("distance"),doc.getDouble("elevation_gain"), doc.getString("timestamp"),doc.getId());
                                   // modelList.add(model);


                                     Model model = RecordedActivity.from(doc.getData());
                                     modelList.add(model);
                                }

                                Collections.sort(modelList);
                                Collections.reverse(modelList);

                                adapter = new CustomAdapter(HomeFragment.this, modelList);
                                mRecyclerView.setAdapter(adapter);




                            }
                        });







                        //System.out.println("hereherehere" + modelList.size());
                        //Collections.sort(modelList);
                        //Collections.reverse(modelList);
//                        if(notificationId != ""){
//
//                        for(int i = 0; i<modelList.size()-1; i++){
//                            if(modelList.get(i).getId().equals(notificationId)) {
//                                position = i;
//                                break;
//                            }
//                        }}

//                        adapter = new CustomAdapter(HomeFragment.this, modelList);
//                        mRecyclerView.setAdapter(adapter);

//                        if(notificationId != null) {
//                            mRecyclerView.scrollToPosition(position);
//                        }
                        getActivity().getIntent().removeExtra("postId");
                    }

                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });


    }

}