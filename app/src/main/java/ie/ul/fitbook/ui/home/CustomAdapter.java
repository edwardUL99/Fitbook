package ie.ul.fitbook.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ie.ul.fitbook.R;
import ie.ul.fitbook.ui.home.ViewHolder;

public class CustomAdapter extends RecyclerView.Adapter<ViewHolder> {

    HomeFragment homeFragment;
    List<Model> modelList;
    Context context;

    public CustomAdapter(HomeFragment homeFragment, List<Model> modelList) {
        this.homeFragment = homeFragment;
        this.modelList = modelList;

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

            }
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.userId.setText(modelList.get(position).getTile());
        holder.postContent.setText(modelList.get(position).getPost());
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }
}
