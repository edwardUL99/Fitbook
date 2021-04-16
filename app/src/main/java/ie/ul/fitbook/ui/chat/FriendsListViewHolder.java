package ie.ul.fitbook.ui.chat;

import android.view.View;

import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import ie.ul.fitbook.R;

/**
 * A FriendsListViewHolder for the chat folder. This ViewHolder is used by
 * both the FriendsListCustomAdapter and the FriendsMessagedAdapter
 */

public class FriendsListViewHolder extends RecyclerView.ViewHolder {

    /**
     * Text fields of userName and userLocation, these are taken from a profile object
     * and are the top bar of the message screen alongside the user profile image
     */
    TextView userName, userLocation;
    /**
     * User profile image. Again, top bar of the message activity
     */

    ImageView profilePic2;
    View mView;

    public FriendsListViewHolder(@NonNull View itemView) {
        super(itemView);

        mView = itemView;

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mClickListener.onItemClicked(v, getAdapterPosition());

            }
        });

        itemView.setOnLongClickListener(new View.OnLongClickListener(){

            @Override
            public boolean onLongClick(View v){
                mClickListener.onItemLongClicked(v, getAdapterPosition());


                return true;
            }});

        userName = itemView.findViewById(R.id.userName);
        userLocation = itemView.findViewById(R.id.userLocation);

        profilePic2 = itemView.findViewById(R.id.profilePic2);


    }
    private FriendsListViewHolder.ClickListener mClickListener;

    public interface ClickListener{
        void onItemClicked(View view, int position);
        void onItemLongClicked(View view, int position);

    }
    public void setOnClickListener(FriendsListViewHolder.ClickListener clicklistener){
        mClickListener = clicklistener;
    }
}
