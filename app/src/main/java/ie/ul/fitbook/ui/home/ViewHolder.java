package ie.ul.fitbook.ui.home;

import android.view.View;

import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import ie.ul.fitbook.R;



public class ViewHolder extends RecyclerView.ViewHolder {

    TextView userId, postContent, createdAt;
    ImageView postsPic;
    ImageView profilePic;
    View mView;

    public ViewHolder(@NonNull View itemView) {
        super(itemView);

        mView = itemView;

//        itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                mClickListener.onItemClicked(v, getAdapterPosition());
//
//            }
//        });
//
//        itemView.setOnLongClickListener(new View.OnLongClickListener(){

//            @Override
//            public boolean onLongClick(View v){
//                mClickListener.onItemLongClicked(v, getAdapterPosition());
//
//
//                return true;
//            }});

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
//    public void setOnClickListener(ViewHolder.ClickListener clicklistener){
//        mClickListener = clicklistener;
//    }
}
