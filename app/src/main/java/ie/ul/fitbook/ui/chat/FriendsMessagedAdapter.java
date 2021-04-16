package ie.ul.fitbook.ui.chat;

import android.content.Intent;
import android.content.Context;


import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Map;

import ie.ul.fitbook.R;
import ie.ul.fitbook.database.UserDatabase;
import ie.ul.fitbook.profile.Profile;
import ie.ul.fitbook.storage.UserStorage;
/**
 * CustomerAdapter for the main-view message fragment recycler view. This inflates the
 * friends_model_layout for users you are friends and have messaged
 */

public class FriendsMessagedAdapter extends RecyclerView.Adapter<FriendsListViewHolder> {
    /**
     * Take a fragment of MessageFragment
     */
    MessagesFragment messagesFragment;
    /**
     * Takes an array list of FriendModel
     */
    List<FriendModel> friendModelList;
    /**
     * Takes a context of context
     */
    Context context;
    /**
     * Takes a FirebaseFirestore db instance
     */
    FirebaseFirestore db;


    public FriendsMessagedAdapter(MessagesFragment friendsList, List<FriendModel> friendModelList) {
        this.messagesFragment = friendsList;
        this.friendModelList = friendModelList;
        context = friendsList.getActivity();
        db = FirebaseFirestore.getInstance();


    }

    @NonNull
    @Override
    public FriendsListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friends_model_layout, parent, false);

        FriendsListViewHolder viewHolder = new FriendsListViewHolder(itemView);
        /**
         * On clicking an item in the recyclerview, a messaging activity opens for that user
         */
        viewHolder.setOnClickListener(new FriendsListViewHolder.ClickListener() {
            @Override
            public void onItemClicked(View view, int position) {

                String userId = friendModelList.get(position).getUserId();
                Intent intent = new Intent(messagesFragment.getActivity(), MessageActivity.class);
                intent.putExtra("userId", userId );
                messagesFragment.startActivity(intent);

            }

            @Override
            public void onItemLongClicked(View view, int position) {

            }
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull FriendsListViewHolder holder, int position) {

        final String[] userId = {friendModelList.get(position).getUserId()};
        new UserDatabase(userId[0]).getChildDocument(Profile.PROFILE_DOCUMENT)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {


                        DocumentSnapshot snapshot = task.getResult();
                        Map<String, Object> data = snapshot.getData();
                        Profile profile = Profile.from(data);
                        holder.userName.setText(profile.getName());
                        holder.userLocation.setText(profile.getCity());





                        StorageReference reference = new UserStorage(userId[0]).getChildFolder(Profile.PROFILE_IMAGE_PATH);
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
                        {
                            @Override
                            public void onSuccess(Uri downloadUrl)
                            {
                                String uri = downloadUrl.toString();
                                Picasso.get().load(uri).into(holder.profilePic2);

                            }
                        });


                    }});




    }

    @Override
    public int getItemCount() {
        return friendModelList.size();
    }
}
