package ie.ul.fitbook.ui.home;

import android.content.Context;
import android.content.Intent;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;

import org.threeten.bp.format.DateTimeFormatter;

import java.util.List;
import java.util.Locale;

import ie.ul.fitbook.R;
import ie.ul.fitbook.profile.Profile;
import ie.ul.fitbook.recording.RecordedActivity;
import ie.ul.fitbook.storage.PostsStorage;
import ie.ul.fitbook.ui.profile.ViewProfileActivity;
import ie.ul.fitbook.ui.recording.ViewRecordedActivity;
import ie.ul.fitbook.utils.ProfileUtils;
import ie.ul.fitbook.utils.Utils;

public class CustomAdapter extends RecyclerView.Adapter<ViewHolder> {
    Context context;
    List<Model> modelList;
    FirebaseFirestore db;
    Profile profile;

    public CustomAdapter(Context context, List<Model> modelList) {
        this(context, modelList, null);
    }

    /**
     * Pass in an already loaded profile to the adapter if each post is going to be from the 
     * same user all the time.
     * @param context the context for the adapter
     * @param modelList the list for the models (actually "Posts")
     * @param profile the profile to set
     * @author Edward Lynch-Milner
     */
    public CustomAdapter(Context context, List<Model> modelList, Profile profile) {
        this.context = context;
        this.modelList = modelList;
        db = FirebaseFirestore.getInstance();
        this.profile = profile;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolder viewHolder;

        if(viewType==0){
             View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.model_layout, parent, false);
             viewHolder = new ViewHolder(itemView);}
        else{
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recorded_activity_layout, parent, false);
            viewHolder = new ActivityViewHolder(itemView);
        }

        return viewHolder;
    }

    private void downloadPostImage(Model model, ViewHolder holder) {
        StorageReference reference = new PostsStorage(model.id).getChildFolder("jpg");
        Utils.downloadImage(reference, holder.postsPic);
    }

    private void handlePostProfileDownload(Profile profile, ViewHolder holder) {
        holder.userId.setText(profile.getName());

        holder.profilePic.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ViewProfileActivity.class);
                intent.putExtra(ViewProfileActivity.USER_PROFILE_EXTRA, profile);
                context.startActivity(intent);
            }
        });
    }

    private void handleActivityProfileDownload(Profile profile, RecordedActivity activity, ActivityViewHolder viewHolder){
        viewHolder.nameView.setText(profile.getName());
        viewHolder.dateView.setText(activity.getTimestamp().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy HH:mm")));
        viewHolder.sportType.setText(Utils.capitalise(activity.getSport().toString()));
        viewHolder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ViewRecordedActivity.class);
                intent.putExtra(ViewRecordedActivity.ACTIVITY_PROFILE, profile);
                intent.putExtra(ViewRecordedActivity.RECORDED_ACTIVITY, activity);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Model model = modelList.get(position);
        
        if(getItemViewType(position)==0){
            final String userId = model.getUserId();
            String id = model.getId();
        
            if (profile == null || !id.equals(profile.getUserId())) {
                ProfileUtils.downloadProfile(userId, profile -> handlePostProfileDownload(profile, holder),
                        () -> Toast.makeText(context, "Failed to download post", Toast.LENGTH_SHORT).show(),
                        holder.profilePic, context, true, false);
                holder.postContent.setText(model.getPost());
                holder.createdAt.setText(model.getDate());
            } else {
                holder.userId.setText(profile.getName());
                holder.postContent.setText(model.getPost());
                holder.createdAt.setText(model.getDate());
                holder.profilePic.setImageBitmap(profile.getProfileImage());
            }
            downloadPostImage(model, holder);
        }
        else{
            RecordedActivity activity = (RecordedActivity)model;

            ActivityViewHolder holder2 = (ActivityViewHolder) holder;
            holder2.distance.setText(String.format(Locale.getDefault(), "%,.02fkm", activity.getDistance()));
            String elevation = "" + (int)activity.getElevationGain() + "m";
            holder2.elevation.setText(elevation);

            holder2.time.setText(Utils.durationToHoursMinutesSeconds(activity.getRecordedDuration()));
            String id = activity.getUserId();
            ProfileUtils.downloadProfile(id, profile -> handleActivityProfileDownload(profile, activity, holder2),() -> Toast.makeText(context, "Failed to download activity", Toast.LENGTH_SHORT).show(),
                    holder2.profilePic, context, true, false);
        }

    }

    @Override
    public int getItemViewType(int position) {

        if (modelList.get(position) instanceof RecordedActivity){return 1;}
        else{return 0;}
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }
}
