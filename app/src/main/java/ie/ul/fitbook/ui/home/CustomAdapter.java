package ie.ul.fitbook.ui.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.sql.SQLOutput;
import java.util.List;
import java.util.Map;

import ie.ul.fitbook.R;
import ie.ul.fitbook.database.UserDatabase;
import ie.ul.fitbook.profile.Profile;
import ie.ul.fitbook.ui.home.ViewHolder;
import ie.ul.fitbook.ui.profile.ViewProfileActivity;


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
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.model_layout, parent, false);

        ViewHolder viewHolder = new ViewHolder(itemView);

        viewHolder.setOnClickListener(new ViewHolder.ClickListener() {
            @Override
            public void onItemClicked(View view, int position) {

                String title = modelList.get(position).getTile();
                String post = modelList.get(position).getPost();
                //Toast.makeText(homeFragment, title+"\n"+post, Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onItemLongClicked(View view, int position) {

               // AlertDialog.Builder builder = new AlertDialog.Builder(homeFragment.getActivity());




                String userId = modelList.get(position).getTile();
                Intent intent = new Intent(homeFragment.getActivity(), ViewProfileActivity.class);
                intent.putExtra(ViewProfileActivity.USER_ID_EXTRA, userId);
                context.startActivity(intent);



            }
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {




//        new UserDatabase(modelList.get(position).getTile()).getChildDocument(Profile.PROFILE_DOCUMENT)
//                .get()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//
//
//                        DocumentSnapshot snapshot = task.getResult();
//                        Map<String, Object> data = snapshot.getData();
//                        profile = Profile.from(data);
//                    }     }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//
//            }
//        });








        holder.userId.setText(modelList.get(position).getTile());
        holder.postContent.setText(modelList.get(position).getPost());

    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }
}
