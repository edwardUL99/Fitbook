package ie.ul.fitbook.ui.home;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;

import ie.ul.fitbook.R;

public class ActivityViewHolder extends ViewHolder {
    TextView distance;
    TextView elevation;
    TextView time;
    View mView;
    TextView nameView;
    TextView dateView;
    TextView sportType;
    ImageView profilePic;

    public ActivityViewHolder(@NonNull View itemView) {
        super(itemView);
        mView = itemView;
        distance = itemView.findViewById(R.id.distanceRecorded);
        time = itemView.findViewById(R.id.timeRecorded);
        elevation = itemView.findViewById(R.id.elevationRecorded);
        nameView = itemView.findViewById(R.id.nameView);
        dateView = itemView.findViewById(R.id.dateView);
        sportType = itemView.findViewById(R.id.sportType);
        profilePic = itemView.findViewById(R.id.userProfilePhoto);


    }
    
    private ActivityViewHolder.ClickListener mClickListener;
    public interface ClickListener{
        void onItemClicked(View view, int position);
        void onItemLongClicked(View view, int position);
    }
}