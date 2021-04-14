package ie.ul.fitbook.ui.home;

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
import ie.ul.fitbook.ui.profile.ViewProfileActivity;


public class SearchAdapter extends RecyclerView.Adapter<FriendsListViewHolder> {

    ProfilesActivity profilesActivity;
    List<FriendModel> friendModelList;
    Context context;
    FirebaseFirestore db;
    Profile profile;

    public SearchAdapter(ProfilesActivity profilesActivity, List<FriendModel> friendModelList) {
        this.profilesActivity = profilesActivity;
        this.friendModelList = friendModelList;
        context = profilesActivity;
        db = FirebaseFirestore.getInstance();


    }

    @NonNull
    @Override
    public FriendsListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friends_model_layout, parent, false);

        return new FriendsListViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendsListViewHolder holder, int position) {
        FriendModel friendModel = friendModelList.get(position);
        final String userId = friendModel.getUserId();
        //String id = modelList.get(position).getId();

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(profilesActivity, ViewProfileActivity.class);
            intent.putExtra(ViewProfileActivity.USER_ID_EXTRA, userId);
            context.startActivity(intent);
        });

        new UserDatabase(userId).getChildDocument(Profile.PROFILE_DOCUMENT)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot snapshot = task.getResult();
                        Map<String, Object> data = snapshot.getData();
                        Profile profile = Profile.from(data);
                        holder.userName.setText(profile.getName());
                        holder.userLocation.setText(profile.getCity());

                        StorageReference reference = new UserStorage(userId).getChildFolder(Profile.PROFILE_IMAGE_PATH);

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
