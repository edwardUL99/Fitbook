package ie.ul.fitbook.ui.home;

import android.content.Intent;
import android.content.Context;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import ie.ul.fitbook.R;
import ie.ul.fitbook.profile.Profile;
import ie.ul.fitbook.ui.profile.ViewProfileActivity;
import ie.ul.fitbook.utils.ProfileUtils;


public class FriendsListCustomAdapter extends RecyclerView.Adapter<FriendsListViewHolder> {

    FriendsList friendsList;
    List<FriendModel> friendModelList;
    Context context;
    FirebaseFirestore db;
    Profile profile;

    public FriendsListCustomAdapter(FriendsList friendsList, List<FriendModel> friendModelList) {
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

        return new FriendsListViewHolder(itemView);
    }

    /**
     * Handles the download of a profile in the friends listF
     * @param profile the profile downloaded
     * @param holder the holder to download into
     */
    private void onProfileDownload(Profile profile, FriendsListViewHolder holder) {
        holder.userName.setText(profile.getName());
        holder.userLocation.setText(profile.getCity());
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(friendsList, ViewProfileActivity.class);
            intent.putExtra(ViewProfileActivity.USER_PROFILE_EXTRA, profile);
            context.startActivity(intent);
        });
    }

    @Override
    public void onBindViewHolder(@NonNull FriendsListViewHolder holder, int position) {
        FriendModel friendModel = friendModelList.get(position);
        final String userId = friendModel.getUserId();

        ProfileUtils.downloadProfile(userId, profile -> onProfileDownload(profile, holder),
                () -> Toast.makeText(context, "Failed to download profile", Toast.LENGTH_SHORT).show(),
                holder.profilePic2, context, true, false);
    }

    @Override
    public int getItemCount() {
        return friendModelList.size();
    }
}
