package ie.ul.fitbook.ui.chat;

import android.view.View;

import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import ie.ul.fitbook.R;

/**
 * A message viewholder by which the adapter can inflate the message layout
 */

public class MessageViewHolder extends RecyclerView.ViewHolder {

    /**
     * TextView fields of
     * senderId
     * messageContent
     * createdAt
     *
     */
    TextView senderId, messageContent, createdAt;
    View mView;

    public MessageViewHolder(@NonNull View itemView) {
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

        senderId = itemView.findViewById(R.id.message_sender);
        messageContent = itemView.findViewById(R.id.message_content);
        createdAt = itemView.findViewById(R.id.message_createdAt);



    }
    private MessageViewHolder.ClickListener mClickListener;

    public interface ClickListener{
        void onItemClicked(View view, int position);
        void onItemLongClicked(View view, int position);

    }
    public void setOnClickListener(MessageViewHolder.ClickListener clicklistener){
        mClickListener = clicklistener;
    }
}
