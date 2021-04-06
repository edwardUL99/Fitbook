package ie.ul.fitbook.ui.chat;

import android.content.Intent;
import android.content.Context;


import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.List;
import java.util.Map;

import ie.ul.fitbook.R;
import ie.ul.fitbook.database.UserDatabase;
import ie.ul.fitbook.profile.Profile;
import ie.ul.fitbook.storage.PostsStorage;
import ie.ul.fitbook.storage.UserStorage;
import ie.ul.fitbook.ui.chat.MessageActivity;
import ie.ul.fitbook.ui.chat.MessageModel;
import ie.ul.fitbook.ui.profile.ViewProfileActivity;

import static java.lang.Integer.parseInt;


public class MessageAdapter extends RecyclerView.Adapter<MessageViewHolder> {

    MessageActivity messageActivity;
    List<MessageModel> modelList;
    Context context;
    FirebaseFirestore db;
    //Profile profile;

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

        System.out.println("hereherehereo"  + modelList.get(position).getSender());
        System.out.println("hereherehereo"  + modelList.get(position).getContent());

        new UserDatabase(modelList.get(position).getSender()).getChildDocument(Profile.PROFILE_DOCUMENT)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {


                        DocumentSnapshot snapshot = task.getResult();
                        Map<String, Object> data = snapshot.getData();
                        Profile profile = Profile.from(data);
                        holder.senderId.setText(profile.getName());
                    }});

        //String s = modelList.get(position).getSender();




        //holder.senderId.setText(s.getName());
        holder.messageContent.setText(modelList.get(position).getContent());
        holder.createdAt.setText(modelList.get(position).getDate());

//        final String[] userId = {modelList.get(position).getTile()};
//        String id = modelList.get(position).getId();
//        new UserDatabase(userId[0]).getChildDocument(Profile.PROFILE_DOCUMENT)
//                .get()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//
//
//                        DocumentSnapshot snapshot = task.getResult();
//                        Map<String, Object> data = snapshot.getData();
//                        Profile profile = Profile.from(data);
//                        holder.userId.setText(profile.getName());
//                        holder.postContent.setText(modelList.get(position).getPost());
//                        holder.createdAt.setText(modelList.get(position).getDate());
//
//
//
//
//                        StorageReference reference = new UserStorage(userId[0]).getChildFolder(Profile.PROFILE_IMAGE_PATH);
//
//                        StorageReference reference2 = new PostsStorage(id).getChildFolder("jpg");
//
//
//                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
//                        {
//                            @Override
//                            public void onSuccess(Uri downloadUrl)
//                            {
//                                String uri = downloadUrl.toString();
//                                //Picasso.get().load(uri).into(holder.postsPic);
//                                Picasso.get().load(uri).into(holder.profilePic);
//
//                            }
//                        });
//
//
//                        reference2.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
//                        {
//                            @Override
//                            public void onSuccess(Uri downloadUrl)
//                            {
//                                String uri = downloadUrl.toString();
//                                Picasso.get().load(uri).into(holder.postsPic);
//                                //Picasso.get().load(uri).into(holder.profilePic);
//
//                            }
//                        });
//                        //Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/fitbook-35d87.appspot.com/o/posts%2FL6xu9qTB1DenfIjHIcgC.jpg?alt=media&token=47fd9876-1b76-40d4-a0e2-2b2795e85b20").into(holder.postsPic);
//
//
//
//
//
//
//
//                    }});




    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }
}
