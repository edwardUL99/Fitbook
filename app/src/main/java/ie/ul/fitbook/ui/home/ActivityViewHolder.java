package ie.ul.fitbook.ui.home;

import android.view.View;

import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import ie.ul.fitbook.R;



public class ActivityViewHolder extends ViewHolder {

    TextView distance;
    TextView elevation;
    TextView time;
    View mView;

    public ActivityViewHolder(@NonNull View itemView) {
        super(itemView);

        mView = itemView;

//        itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                mClickListener.onItemClicked(v, getAdapterPosition());
//
//            }
//        });

//        itemView.setOnLongClickListener(new View.OnLongClickListener(){
//
//            @Override
//            public boolean onLongClick(View v){
//                mClickListener.onItemLongClicked(v, getAdapterPosition());
//
//
//                return true;
//            }});

        distance = itemView.findViewById(R.id.distanceRecorded);
        time = itemView.findViewById(R.id.timeRecorded);
        elevation = itemView.findViewById(R.id.elevationRecorded);




    }
    private ActivityViewHolder.ClickListener mClickListener;

    public interface ClickListener{
        void onItemClicked(View view, int position);
        void onItemLongClicked(View view, int position);

    }
//    public void setOnClickListener(ActivityViewHolder.ClickListener clicklistener){
//        mClickListener = clicklistener;
//    }
}
