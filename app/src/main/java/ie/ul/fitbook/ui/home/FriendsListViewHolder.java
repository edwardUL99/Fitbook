package ie.ul.fitbook.ui.home;

import android.view.View;

import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import ie.ul.fitbook.R;

/**
 * A FriendsListViewHolder for inflating the friend_model_layout
 */

public class FriendsListViewHolder extends RecyclerView.ViewHolder {


    /**
     * TextViews of userName, userlocation
     * used for displaying userName, userLocation in the model
     */
    TextView userName, userLocation;

    /**
     * An ImageView of profilePic2
     * used for displaying the friend's profile pic in the model
     */
    ImageView profilePic2;

    public FriendsListViewHolder(@NonNull View itemView) {
        super(itemView);

        userName = itemView.findViewById(R.id.userName);
        userLocation = itemView.findViewById(R.id.userLocation);

        profilePic2 = itemView.findViewById(R.id.profilePic2);
    }
}
