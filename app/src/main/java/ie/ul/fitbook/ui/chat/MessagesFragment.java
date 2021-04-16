package ie.ul.fitbook.ui.chat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ie.ul.fitbook.R;
import ie.ul.fitbook.login.Login;

/**
 * The main messages fragment. Friends messaged are displayed in a recycler view here.
 * A floating action button brings us to a "Select Contact" screen where we might message
 * a friend that we have not messaged before
 */
public class MessagesFragment extends Fragment {

    /**
     * We create an array list of type FriendModel to send to the adapter here
     */
    List<FriendModel> friendModelList;

    /**
     * A RecyclerView mRecyclerView
     */
    RecyclerView mRecyclerView;
    /**
     *A LayoutManager layoutManager
     */
    RecyclerView.LayoutManager layoutManager;
    /**
     * An instance of Firestore db
     */
    FirebaseFirestore db;
    /**
     * FriendsMessagedAdapter adapter
     */

    FriendsMessagedAdapter adapter;

    /**
     * A FloatingActionButton ab, actually this looks like a bug but I'm afraid to touch it right now haha. It works!
     */

    FloatingActionButton ab;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_messages, container, false);

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

        /**
         * Porbably should just be "ab = view.findViewById(R.id.add_fab2);"
         */
        FloatingActionButton ab = view.findViewById(R.id.add_fab2);

        mRecyclerView = view.findViewById(R.id.recyclerView21);
        mRecyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        db = FirebaseFirestore.getInstance();
        friendModelList = new ArrayList<>();

        /**
         * Brings you to the "Select Contact" screen
         */

        ab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), CreateMessageActivity.class);
                startActivity(intent);
                ((Activity) getActivity()).overridePendingTransition(0, 0);

            }
        });

}


    /**
     * This onResume clears the friends and calls show data. This is is that the sort is always correct.
     * New messages by the logged in user will change it, and new messages in the db from another user
     *
     */

    @Override
    public void onResume(){

        friendModelList.clear();
        super.onResume();

        showData();
    }

    /**
     * This grabs a list of the friends you have messaged from the database and sends it to one of the adapters
     */
    private void showData(){



        db.collection("users/" + Login.getUserId() +"/messages")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        for(DocumentSnapshot doc: task.getResult()){


                            FriendModel model = new FriendModel(doc.getId(), String.valueOf(doc.get("timeStamp")));
                            friendModelList.add(model);



                        }
                        Collections.sort(friendModelList);
                        Collections.reverse(friendModelList);


                        adapter = new FriendsMessagedAdapter(MessagesFragment.this, friendModelList);
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