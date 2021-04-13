package ie.ul.fitbook.ui.home;

import android.content.Intent;
import android.content.Context;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.threeten.bp.format.DateTimeFormatter;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ie.ul.fitbook.R;
import ie.ul.fitbook.database.UserDatabase;
import ie.ul.fitbook.profile.Profile;
import ie.ul.fitbook.recording.RecordedActivity;
import ie.ul.fitbook.storage.PostsStorage;
import ie.ul.fitbook.storage.UserStorage;

import ie.ul.fitbook.ui.profile.ViewProfileActivity;
import ie.ul.fitbook.ui.recording.ViewRecordedActivity;
import ie.ul.fitbook.utils.Utils;

import static java.lang.Integer.parseInt;


public class CustomAdapter extends RecyclerView.Adapter<ViewHolder> {

    HomeFragment homeFragment;
    List<Model> modelList;
    Context context;
    FirebaseFirestore db;
    Profile profile;

    public CustomAdapter(HomeFragment homeFragment, List<Model> modelList) {
        this.homeFragment = homeFragment;
        this.modelList = modelList;
        context = homeFragment.getActivity();
        db = FirebaseFirestore.getInstance();


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







            //                @Override
//                public void onItemClicked(View view, int position) {
//
////                    String title = modelList.get(position).getTile();
////                    String post = modelList.get(position).getPost();
//
//                }
//
//                @Override
//                public void onItemLongClicked(View view, int position) {
//
//
//                String userId = modelList.get(position).getTile();
//                Intent intent = new Intent(homeFragment.getActivity(), ViewProfileActivity.class);
//                intent.putExtra(ViewProfileActivity.USER_ID_EXTRA, userId);
//                context.startActivity(intent);
//
//                    String s = String.valueOf(position);
//
//                    Toast.makeText(homeFragment.getActivity(), s, Toast.LENGTH_SHORT).show();
//
//
//                }




        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        if(getItemViewType(position)==0){




            ViewHolder holder1 = (ViewHolder) holder;

            holder1.itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {

                    //String s = String.valueOf(position);
                    //Toast.makeText(homeFragment.getActivity(), s, Toast.LENGTH_SHORT).show();
                    String userId = modelList.get(position).getTile();
                    Intent intent = new Intent(homeFragment.getActivity(), ViewProfileActivity.class);
                    intent.putExtra(ViewProfileActivity.USER_ID_EXTRA, userId);
                    context.startActivity(intent);

                }
            });


        final String[] userId = {modelList.get(position).getTile()};
        String id = modelList.get(position).getId();
        new UserDatabase(userId[0]).getChildDocument(Profile.PROFILE_DOCUMENT)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot snapshot = task.getResult();
                        Map<String, Object> data = snapshot.getData();
                        Profile profile = Profile.from(data);
                        holder1.userId.setText(profile.getName());
                        holder1.postContent.setText(modelList.get(position).getPost());
                        holder1.createdAt.setText(modelList.get(position).getDate());

                        StorageReference reference = new UserStorage(userId[0]).getChildFolder(Profile.PROFILE_IMAGE_PATH);
                        StorageReference reference2 = new PostsStorage(id).getChildFolder("jpg");
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
                        {
                            @Override
                            public void onSuccess(Uri downloadUrl)
                            {
                                String uri = downloadUrl.toString();
                                //Picasso.get().load(uri).into(holder.postsPic);
                                Picasso.get().load(uri).into(holder1.profilePic);

                            }
                        });
                        reference2.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
                        {
                            @Override
                            public void onSuccess(Uri downloadUrl)
                            {
                                String uri = downloadUrl.toString();
                                Picasso.get().load(uri).into(holder1.postsPic);
                                //Picasso.get().load(uri).into(holder.profilePic);

                            }
                        });
                    }});


        }
        else{
            RecordedActivity activity = (RecordedActivity)modelList.get(position);


            ActivityViewHolder holder2 = (ActivityViewHolder) holder;
            holder2.distance.setText(String.format(Locale.getDefault(), "%,.02fkm", activity.getDistance()));
            String elevation = "" + (int)activity.getElevationGain() + "m";
            holder2.elevation.setText(elevation);
            String id = activity.getUserId();
            new UserDatabase(id).getChildDocument(Profile.PROFILE_DOCUMENT)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot snapshot = task.getResult();
                            Map<String, Object> data = snapshot.getData();
                            Profile profile = Profile.from(data);
                            profile.setUserId(id);
                        holder2.nameView.setText(profile.getName());


                        }});



           holder2.dateView.setText(activity.getTimestamp().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy HH:mm")));
           holder2.time.setText(Utils.durationToHoursMinutes(activity.getRecordedDuration()));
           holder2.sportType.setText(Utils.capitalise(activity.getSport().toString()));
            //((ActivitiesModel)modelList.get(position)).getTimeStamp()
            holder2.itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {


                    String id = activity.getUserId();
                    new UserDatabase(id).getChildDocument(Profile.PROFILE_DOCUMENT)
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot snapshot = task.getResult();
                                    Map<String, Object> data = snapshot.getData();
                                    Profile profile = Profile.from(data);
                                    profile.setUserId(id);

                                    Intent intent = new Intent(context, ViewRecordedActivity.class);
                                    intent.putExtra(ViewRecordedActivity.ACTIVITY_PROFILE, profile);
                                    intent.putExtra(ViewRecordedActivity.RECORDED_ACTIVITY, activity);
                                    //ViewRecordedActivity.setProfileImage(bitmap);
                                    context.startActivity(intent);












                                    //ViewRecordedActivity.setProfileImage(profile.getProfileImage());
                                    //context.startActivity(intent);






                                }});






                }
            });


        }

    }


    @Override
    public int getItemViewType(int position) {

        if (modelList.get(position) instanceof RecordedActivity){return 1;}
        else{return 0;}
//        if(modelList.get(position).getClass() == Model.class){
//
//            return 0;
//
//
//        }
//        else return 1;

    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }
}
