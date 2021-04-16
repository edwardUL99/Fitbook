package ie.ul.fitbook.ui.chat;

import android.content.Context;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

import ie.ul.fitbook.R;
import ie.ul.fitbook.database.UserDatabase;
import ie.ul.fitbook.profile.Profile;
import ie.ul.fitbook.ui.chat.MessageActivity;
import ie.ul.fitbook.ui.chat.MessageModel;

/**
 * CustomerAdapter for the main-view messaging activity recycler view. This inflates the
 * message_model_layout for your messages with whatever given user. If I had time, I might have made this adapter
 * take two viewholder and implemented getItemViewType, but I did not get the chance. Still, it would have been an easy check.
 * If modelList.get(position).getUserId() was equal to Login.getUserId() could have set Item View Type to 0 else 1. Could have inflated
 * two model layouts just as in the adapter for the home fragment
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageViewHolder> {

    /**
     * Takes a MessageActivity object
     */

    MessageActivity messageActivity;

    /**
     * Takes an array list of type MessageModel
     */
    List<MessageModel> modelList;

    /**
     * Takes a Context context
     */
    Context context;
    /**
     * A Firestore db instance
     */
    FirebaseFirestore db;


    public MessageAdapter(MessageActivity messageActivity, List<MessageModel> modelList) {
        this.messageActivity = messageActivity;
        this.modelList = modelList;
        context = messageActivity;
        db = FirebaseFirestore.getInstance();


    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_model_layout, parent, false);

        MessageViewHolder viewHolder = new MessageViewHolder(itemView);

        viewHolder.setOnClickListener(new MessageViewHolder.ClickListener() {
            @Override
            public void onItemClicked(View view, int position) {

                String senderId = modelList.get(position).getSender();
                String content = modelList.get(position).getContent();

            }

            @Override
            public void onItemLongClicked(View view, int position) {




            }
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {

        new UserDatabase(modelList.get(position).getSender()).getChildDocument(Profile.PROFILE_DOCUMENT)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {


                        DocumentSnapshot snapshot = task.getResult();
                        Map<String, Object> data = snapshot.getData();
                        Profile profile = Profile.from(data);
                        holder.senderId.setText(profile.getName());
                    }});

        holder.messageContent.setText(modelList.get(position).getContent());
        holder.createdAt.setText(modelList.get(position).getDate());
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }
}
