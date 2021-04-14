package ie.ul.fitbook.ui.home;

import android.view.View;

import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import ie.ul.fitbook.R;



public class FriendsListViewHolder extends RecyclerView.ViewHolder {

    TextView userName, userLocation;
    ImageView profilePic2;

    public FriendsListViewHolder(@NonNull View itemView) {
        super(itemView);

        userName = itemView.findViewById(R.id.userName);
        userLocation = itemView.findViewById(R.id.userLocation);

        profilePic2 = itemView.findViewById(R.id.profilePic2);
    }
}
