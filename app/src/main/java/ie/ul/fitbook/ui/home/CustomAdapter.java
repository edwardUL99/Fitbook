package ie.ul.fitbook.ui.home;

import android.content.Context;
import android.content.Intent;


import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;

import org.threeten.bp.format.DateTimeFormatter;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import ie.ul.fitbook.R;
import ie.ul.fitbook.profile.Profile;
import ie.ul.fitbook.recording.RecordedActivity;
import ie.ul.fitbook.storage.PostsStorage;
import ie.ul.fitbook.storage.UserStorage;
import ie.ul.fitbook.ui.profile.ViewProfileActivity;
import ie.ul.fitbook.ui.recording.ViewRecordedActivity;
import ie.ul.fitbook.utils.ProfileUtils;
import ie.ul.fitbook.utils.Utils;

/**
 * A custom adapter for the home fragment recycler view
 * Here layouts are inflated for both posts and activites depending on
 * itemview type. Different onclicklisteners are also set for different itemviews
 */

public class CustomAdapter extends RecyclerView.Adapter<ViewHolder> {
    /**
     * Takes a Context context
     *
     */

    Context context;

    /**
     * Takes an array list of type Model
     */
    List<Model> modelList;

    /**
     * A firestore db instacne
     */
    FirebaseFirestore db;

    /**
     * A profile object
     */
    Profile profile;
    /**
     * A cache of all downloaded profiles
     */
    private final HashMap<String, Profile> cachedProfiles = new HashMap<>();
    /**
     * True if this is the initial scroll or not down to the bottom of the adapter
     */
    private boolean initialScroll;

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
        Utils.invalidateImageCache(context);
        initialScroll = true;
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

    /**
     * Downloads the post image
     * @param model
     * @param holder
     */
    private void downloadPostImage(Model model, ViewHolder holder) {
        StorageReference reference = new PostsStorage(model.id).getChildFolder("jpg");
        Utils.downloadImage(reference, holder.postsPic, true, context);
    }

    /**
     * Handles profile download for the post
     * @param profile
     * @param holder
     */
    private void handlePostProfileDownload(Profile profile, ViewHolder holder, Model model) {
        setPost(profile, model, holder);
        cachedProfiles.put(profile.getUserId(), profile);
    }

     /* Handles profile download and sets onclicklistener
     * @param profile
     * @param activity
     * @param viewHolder
     */
    private void handleActivityProfileDownload(Profile profile, RecordedActivity activity, ActivityViewHolder viewHolder){
        setActivity(profile, activity, viewHolder);
        cachedProfiles.put(profile.getUserId(), profile);
    }

    /**
     * Set the posts profile and model
     * @param profile the profile to set
     * @param model the model to set post content
     * @param holder the holder for the view
     */
    private void setPost(Profile profile, Model model, ViewHolder holder) {
        holder.userId.setText(profile.getName());
        holder.postContent.setText(model.getPost());
        holder.createdAt.setText(model.getDate());

        Bitmap bitmap = profile.getProfileImage();

        if (bitmap != null) {
            holder.profilePic.setImageBitmap(profile.getProfileImage());
        } else {
            Utils.downloadImage(new UserStorage(profile.getUserId()).getChildFolder(Profile.PROFILE_IMAGE_PATH),
                    holder.profilePic, context);
        }

        holder.profilePic.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ViewProfileActivity.class);
                intent.putExtra(ViewProfileActivity.USER_PROFILE_EXTRA, profile);
                context.startActivity(intent);
            }
        });
    }

    /**
     * Set the posts profile and model
     * @param profile the profile to set
     * @param model the model to set post content
     * @param holder the holder for the view
     */
    private void setActivity(Profile profile, RecordedActivity model, ActivityViewHolder holder) {
        Bitmap bitmap = profile.getProfileImage();

        if (bitmap != null) {
            holder.profilePic.setImageBitmap(profile.getProfileImage());
        } else {
            Utils.downloadImage(new UserStorage(profile.getUserId()).getChildFolder(Profile.PROFILE_IMAGE_PATH),
                    holder.profilePic, context);
        }

        holder.nameView.setText(profile.getName());
        holder.dateView.setText(model.getTimestamp().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy HH:mm")));
        holder.sportType.setText(Utils.capitalise(model.getSport().toString()));
        holder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ViewRecordedActivity.class);
                intent.putExtra(ViewRecordedActivity.ACTIVITY_PROFILE, profile);
                intent.putExtra(ViewRecordedActivity.RECORDED_ACTIVITY, model);
                context.startActivity(intent);
            }
        });

        holder.distance.setText(String.format(Locale.getDefault(), "%,.02fkm", model.getDistance()));
        String elevation = "" + (int)model.getElevationGain() + "m";
        holder.elevation.setText(elevation);
        holder.time.setText(Utils.durationToHoursMinutesSeconds(model.getRecordedDuration()));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Model model = modelList.get(position);
        
        if(getItemViewType(position)==0){
            final String userId = model.getUserId();

            if (profile == null || !userId.equals(profile.getUserId())) {
                Profile cachedProfile = cachedProfiles.get(userId);

                if (cachedProfile != null) {
                    setPost(cachedProfile, model, holder);
                } else {
                    ProfileUtils.downloadProfile(userId, profile -> handlePostProfileDownload(profile, holder, model),
                            () -> Toast.makeText(context, "Failed to download post", Toast.LENGTH_SHORT).show(),
                            null, context, true, false);
                }
            } else {
                setPost(profile, model, holder);
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

            if (profile == null || !id.equals(profile.getUserId())) {
                Profile cachedProfile = cachedProfiles.get(id);

                if (cachedProfile != null) {
                    setActivity(cachedProfile, activity, holder2);
                } else {
                    ProfileUtils.downloadProfile(id, profile -> handleActivityProfileDownload(profile, activity, holder2),() -> Toast.makeText(context, "Failed to download activity", Toast.LENGTH_SHORT).show(),
                            null, context, true, false);
                }
            } else {
                setActivity(profile, activity, holder2);
            }
        }

        if (initialScroll && position == getItemCount() - 1) {
            initialScroll = false;
            Utils.setUseImageCache(context);
        }
    }

    /**
     * Gets itemview type for onBindViewHolder
     * @param position
     * @return
     */

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
