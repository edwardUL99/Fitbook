package ie.ul.fitbook.ui.notifications;

import android.content.Context;
import android.content.Intent;
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

import java.util.List;
import java.util.Map;

import ie.ul.fitbook.R;
import ie.ul.fitbook.database.UserDatabase;
import ie.ul.fitbook.profile.Profile;
import ie.ul.fitbook.storage.UserStorage;
import ie.ul.fitbook.ui.HomeActivity;
import ie.ul.fitbook.ui.chat.MessageActivity;
import ie.ul.fitbook.ui.notifications.NotificationModel;
import ie.ul.fitbook.ui.notifications.NotificationsActivity;
import ie.ul.fitbook.ui.notifications.NotificationsViewHolder;
import ie.ul.fitbook.ui.profile.ViewProfileActivity;

public class NotificationsCustomAdapter extends RecyclerView.Adapter<NotificationsViewHolder> {

    NotificationsActivity notification;
    List<NotificationModel> notificationModelList;
    Context context;
    FirebaseFirestore db;

    public NotificationsCustomAdapter(NotificationsActivity notification, List<NotificationModel> notificationModelList) {
        this.notification = notification;
        this.notificationModelList = notificationModelList;
        context = notification;
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public NotificationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_model_layout, parent, false);

        return new NotificationsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationsViewHolder holder, int position) {
        NotificationModel model = notificationModelList.get(position);

        String userId = model.getUserId();

        new UserDatabase(userId).getChildDocument(Profile.PROFILE_DOCUMENT)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot snapshot = task.getResult();
                        Map<String, Object> data = snapshot.getData();
                        Profile profile = Profile.from(data);
                        holder.userId.setText(profile.getName());
                        holder.notificationType.setText(model.getNotificationType());

                        StorageReference reference = new UserStorage(userId).getChildFolder(Profile.PROFILE_IMAGE_PATH);
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri downloadUrl) {
                                String uri = downloadUrl.toString();
                                Picasso.get().load(uri).into(holder.profilePic);
                            }
                        });

                        holder.itemView.setOnClickListener(view -> {
                            String notificationType = model.getNotificationType();

                            if(notificationType.equals("New Message")) {
                                Intent intent = new Intent(notification, MessageActivity.class);
                                intent.putExtra("userId", userId);
                                notification.startActivity(intent);
                            } else if(notificationType.equals("New Friend")){
                                Intent intent = new Intent(notification, ViewProfileActivity.class);
                                intent.putExtra(ViewProfileActivity.USER_ID_EXTRA, userId);
                                notification.startActivity(intent);
                            } else if(notificationType.equals("New Post")){
                                Intent intent = new Intent(notification, HomeActivity.class);
                                intent.putExtra("postId", model.getPostId());
                                notification.startActivity(intent);
                            } else if(notificationType.equals("New Activity")) {
                                Intent intent = new Intent(notification, HomeActivity.class);
                                intent.putExtra("postId", model.getPostId());
                                notification.startActivity(intent);
                            }
                        });
                    }
                });
    }

    @Override
    public int getItemCount() {
        return notificationModelList.size();
    }
}
