package ie.ul.fitbook.ui.notifications;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import ie.ul.fitbook.R;
import ie.ul.fitbook.ui.home.FriendsListViewHolder;

public class NotificationsViewHolder extends RecyclerView.ViewHolder{

    TextView userId;
    TextView notificationType;
    ImageView profilePic;
    View mView;

    public NotificationsViewHolder(@NonNull View itemView) {
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

        userId = itemView.findViewById(R.id.userId);
        notificationType = itemView.findViewById(R.id.notificationType);
        profilePic = itemView.findViewById(R.id.profilePicNotification);
    }

    private NotificationsViewHolder.ClickListener mClickListener;

    public interface ClickListener{
        void onItemClicked(View view, int position);
        void onItemLongClicked(View view, int position);

    }
    public void setOnClickListener(NotificationsViewHolder.ClickListener clicklistener){
        mClickListener = clicklistener;
    }
}
