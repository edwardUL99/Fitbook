package ie.ul.fitbook.ui.home;

import android.view.View;

import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import ie.ul.fitbook.R;


/**
 * A viewHolder for the posts
 */
public class ViewHolder extends RecyclerView.ViewHolder {
    /**
     *
     * TextViews of userId, postContent, createdAt
     */
    TextView userId, postContent, createdAt;

    /**
     * ImageView of postsPic
     */
    ImageView postsPic;

    /**
     * ImageView of profilePic
     */
    ImageView profilePic;
    View mView;

    public ViewHolder(@NonNull View itemView) {
        super(itemView);

        mView = itemView;
        userId = itemView.findViewById(R.id.post_userId);
        postContent = itemView.findViewById(R.id.post_userPost);
        createdAt = itemView.findViewById(R.id.post_createdAt);

        postsPic = itemView.findViewById(R.id.postsPic);
        profilePic = itemView.findViewById(R.id.profilePic);


    }
    private ViewHolder.ClickListener mClickListener;

    public interface ClickListener{
        void onItemClicked(View view, int position);
        void onItemLongClicked(View view, int position);

    }
}
