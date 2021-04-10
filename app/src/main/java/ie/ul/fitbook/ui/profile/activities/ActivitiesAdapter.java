package ie.ul.fitbook.ui.profile.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import ie.ul.fitbook.R;
import ie.ul.fitbook.profile.Profile;
import ie.ul.fitbook.recording.RecordedActivity;
import ie.ul.fitbook.ui.recording.ViewRecordedActivity;
import ie.ul.fitbook.utils.Utils;

/**
 * This class provides the adapter for the activities RecyclerView
 */
public class ActivitiesAdapter extends RecyclerView.Adapter<ActivitiesAdapter.ViewHolder> {
    /**
     * The list of activities for the data set
     */
    private final ArrayList<RecordedActivity> activities;
    /**
     * The context for the activities
     */
    private final ListActivitiesActivity context;

    /**
     * This class provides the view holder that will hold the views in this RecyclerView
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        /**
         * The Profile photo on the post
         */
        private final CircleImageView profilePhoto;
        /**
         * The TextView containing the user's name
         */
        private final TextView nameView;
        /**
         * The TextView displaying the activity date
         */
        private final TextView dateView;
        /**
         * The TextView displaying the sport type
         */
        private final TextView sportType;
        /**
         * The TextView displaying the distance recorded
         */
        private final TextView distanceRecorded;
        /**
         * The TextView representing the elevation gain
         */
        private final TextView elevationRecorded;
        /**
         * The TextView representing the time recorded
         */
        private final TextView timeRecorded;

        /**
         * Constructs the ViewHolder for provided item
         * @param itemView the view this holder is holding
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profilePhoto = itemView.findViewById(R.id.userProfilePhoto);
            nameView = itemView.findViewById(R.id.nameView);
            dateView = itemView.findViewById(R.id.dateView);
            sportType = itemView.findViewById(R.id.sportType);
            distanceRecorded = itemView.findViewById(R.id.distanceRecorded);
            elevationRecorded = itemView.findViewById(R.id.elevationRecorded);
            timeRecorded = itemView.findViewById(R.id.timeRecorded);
        }
    }

    /**
     * Constructs an ActivitiesAdapter instance with the provided activities and context
     * @param activities the activities for this data set. If null, a new list will be created, else
     *                   a copy will be created
     * @param context the context this adapter is associated with
     */
    public ActivitiesAdapter(ArrayList<RecordedActivity> activities, ListActivitiesActivity context) {
        this.activities = activities == null ? new ArrayList<>():new ArrayList<>(activities);
        this.context = context;
    }

    /**
     * Adds the activity to the adapter
     * @param activity the activity to add
     */
    public void addActivity(RecordedActivity activity) {
        if (!activities.contains(activity)) {
            activities.add(activity);
            notifyDataSetChanged();
        }
    }

    /**
     * Removes the provided activity from the adapter
     * @param activity the activity to remove
     */
    public void removeActivity(RecordedActivity activity) {
        if (activities.remove(activity))
            notifyDataSetChanged();
    }

    /**
     * Clears the data set
     */
    public void clear() {
        activities.clear();;
        notifyDataSetChanged();
    }

    /**
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     * <p>
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     * <p>
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     * @see #getItemViewType(int)
     * @see #onBindViewHolder(ViewHolder, int)
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
        .inflate(R.layout.recorded_activity_layout, parent, false));
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link ViewHolder#itemView} to reflect the item at the given
     * position.
     * <p>
     * Note that unlike {@link ListView}, RecyclerView will not call this method
     * again if the position of the item changes in the data set unless the item itself is
     * invalidated or the new position cannot be determined. For this reason, you should only
     * use the <code>position</code> parameter while acquiring the related data item inside
     * this method and should not keep a copy of it. If you need the position of an item later
     * on (e.g. in a click listener), use {@link ViewHolder#getAdapterPosition()} which will
     * have the updated adapter position.
     * <p>
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecordedActivity activity = activities.get(position);

        Profile profile = context.profile;
        if (profile != null) {
            Bitmap bitmap = profile.getProfileImage();
            if (bitmap != null)
                holder.profilePhoto.setImageBitmap(bitmap);
            holder.nameView.setText(profile.getName());
            holder.dateView.setText(activity.getTimestamp().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy HH:mm")));
            holder.sportType.setText(Utils.capitalise(activity.getSport().toString()));
            holder.distanceRecorded.setText(String.format(Locale.getDefault(), "%,.02fkm", activity.getDistance()));
            String elevation = "" + (int)activity.getElevationGain() + "m";
            holder.elevationRecorded.setText(elevation);
            holder.timeRecorded.setText(Utils.durationToHoursMinutes(activity.getRecordedDuration()));

            holder.itemView.setOnClickListener(view -> openActivity(profile, activity));
        }
    }

    /**
     * Launch the view activity screen
     * @param profile the profile to set the the view activity
     * @param recordedActivity the activity to display
     */
    private void openActivity(Profile profile, RecordedActivity recordedActivity) {
        Intent intent = new Intent(context, ViewRecordedActivity.class);
        intent.putExtra(ViewRecordedActivity.ACTIVITY_PROFILE, profile);
        intent.putExtra(ViewRecordedActivity.RECORDED_ACTIVITY, recordedActivity);
        ViewRecordedActivity.setProfileImage(profile.getProfileImage());
        context.startActivity(intent);
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return activities.size();
    }
}
