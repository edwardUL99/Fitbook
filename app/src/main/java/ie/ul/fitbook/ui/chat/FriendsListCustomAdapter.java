package ie.ul.fitbook.ui.chat;

import android.app.Activity;
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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ie.ul.fitbook.R;
import ie.ul.fitbook.database.UserDatabase;
import ie.ul.fitbook.login.Login;
import ie.ul.fitbook.profile.Profile;
import ie.ul.fitbook.storage.PostsStorage;
import ie.ul.fitbook.storage.UserStorage;
import ie.ul.fitbook.ui.profile.ViewProfileActivity;

import static java.lang.Integer.parseInt;


public class FriendsListCustomAdapter extends RecyclerView.Adapter<FriendsListViewHolder> {

    CreateMessageActivity friendsList;
    List<FriendModel> friendModelList;
    Context context;
    FirebaseFirestore db;
    Profile profile;

    public FriendsListCustomAdapter(CreateMessageActivity friendsList, List<FriendModel> friendModelList) {
        this.friendsList = friendsList;
        this.friendModelList = friendModelList;
        context = friendsList;
        db = FirebaseFirestore.getInstance();


    }

    @NonNull
    @Override
    public FriendsListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friends_model_layout, parent, false);

        FriendsListViewHolder viewHolder = new FriendsListViewHolder(itemView);

        viewHolder.setOnClickListener(new FriendsListViewHolder.ClickListener() {
            @Override
            public void onItemClicked(View view, int position) {

                //String title = friendModelList.get(position).getUserName();
                //String post = friendModelList.get(position).getUserLocation();

            }

            @Override
            public void onItemLongClicked(View view, int position) {

                String userId = friendModelList.get(position).getUserId();
//                db.collection("users/" + Login.getUserId() + "/unmessaged").document(userId)
//                        .delete()
//                        .addOnSuccessListener(new OnSuccessListener<Void>() {
//                            @Override
//                            public void onSuccess(Void aVoid) {
//
//
//                            }
//
//                        });
//                db.collection("users").document( Login.getUserId() + "/messages/" + userId)
//                        .set(new HashMap<String, Object>())
//                        .addOnSuccessListener(new OnSuccessListener<Void>() {
//                            @Override
//                            public void onSuccess(Void aVoid) {
//
//                            }
//                        });
//
//                friendsList.finish();


                Intent intent = new Intent(friendsList, NewMessageActivity.class);
                intent.putExtra("userId", userId );
                friendsList.startActivity(intent);



            }
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull FriendsListViewHolder holder, int position) {

        final String[] userId = {friendModelList.get(position).getUserId()};
        //String id = modelList.get(position).getId();









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

                        //StorageReference reference2 = new PostsStorage(id).getChildFolder("jpg");


                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
                        {
                            @Override
                            public void onSuccess(Uri downloadUrl)
                            {
                                String uri = downloadUrl.toString();
                                //Picasso.get().load(uri).into(holder.postsPic);
                                Picasso.get().load(uri).into(holder.profilePic2);

                            }
                        });



                        //Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/fitbook-35d87.appspot.com/o/posts%2FL6xu9qTB1DenfIjHIcgC.jpg?alt=media&token=47fd9876-1b76-40d4-a0e2-2b2795e85b20").into(holder.postsPic);







                    }});




    }

    @Override
    public int getItemCount() {
        return friendModelList.size();
    }
}
