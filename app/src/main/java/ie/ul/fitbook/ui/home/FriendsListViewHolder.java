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
