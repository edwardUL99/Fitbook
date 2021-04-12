package ie.ul.fitbook.ui.profile.goals;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.threeten.bp.Duration;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Locale;

import ie.ul.fitbook.R;
import ie.ul.fitbook.goals.Goal;
import ie.ul.fitbook.goals.GoalType;
import ie.ul.fitbook.utils.Utils;

/**
 * This class provides the adapter for the Goals RecyclerView
 */
public class GoalsAdapter extends RecyclerView.Adapter<GoalsAdapter.ViewHolder> {
    /**
     * The list of goals for the data set
     */
    private final ArrayList<Goal> goals;
    /**
     * The context hosting this goals adapter
     */
    private final GoalsActivity context;
    /**
     * This boolean tracks whether a user can modify the goals in the adapter
     */
    private boolean canModifyGoals;

    /**
     * This class provides the view holder that will hold the views in this RecyclerView
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        /**
         * The TextView displaying the goal target at the top
         */
        private final TextView targetText;
        /**
         * The TextView containing the final date
         */
        private final TextView targetDate;
        /**
         * The TextView displaying the type of sport this goal is for
         */
        private final TextView sportValue;
        /**
         * The TextView displaying the achieved goal value
         */
        private final TextView achievedValue;
        /**
         * The TextView displaying the remaining vgoal value
         */
        private final TextView remainingValue;

        /**
         * Constructs the ViewHolder for provided item
         * @param itemView the view this holder is holding
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            targetText = itemView.findViewById(R.id.targetText);
            targetDate = itemView.findViewById(R.id.targetDate);
            sportValue = itemView.findViewById(R.id.sportValue);
            achievedValue = itemView.findViewById(R.id.achievedValue);
            remainingValue = itemView.findViewById(R.id.remainingValue);
        }
    }

    /**
     * Constructs a GoalsAdapter instance with the provided goals and context
     * @param goals the list of goals for this data set. If null, a new list will be created,
     *              else a copy of this list will be created
     * @param context the context this GoalsAdapter is associated with
     */
    public GoalsAdapter(ArrayList<Goal> goals, GoalsActivity context) {
        this.goals = goals == null ? new ArrayList<>():new ArrayList<>(goals);
        this.context = context;
    }

    /**
     * Adds the provided goal to the data set
     * @param goal the goal to add
     */
    public void addGoal(Goal goal) {
        if (!goals.contains(goal)) {
            goals.add(goal);
            notifyDataSetChanged();
        }
    }

    /**
     * Removes the provided goal from the data set
     * @param goal the goal to remove
     */
    public void removeGoal(Goal goal) {
        if (goals.remove(goal))
            notifyDataSetChanged();
    }

    /**
     * Clears the data set behind this adapter
     */
    public void clear() {
        goals.clear();
        notifyDataSetChanged();
    }

    /**
     * Handler for when a ViewHolder needs to be created
     * @param parent the parent group
     * @param viewType the integer id representing view type if defined
     * @return the created ViewHolder
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.goal_layout, parent, false));
    }

    /**
     * Gets the target text from the provided goal
     * @param goal the goal to process
     * @return text for the targetText TextView
     */
    private String getTargetText(Goal goal) {
        GoalType type = goal.getType();

        if (type == GoalType.DISTANCE) {
            Double value = (Double)goal.getTargetValue();

            return String.format(Locale.getDefault(), "%,.01f km", value);
        } else if (type == GoalType.ELEVATION) {
            Integer value = (Integer)goal.getTargetValue();

            return value + " m";
        } else if (type == GoalType.TIME) {
            Duration value = (Duration)goal.getTargetValue();

            return Utils.durationToHoursMinutes(value);
        }

        return null;
    }

    /**
     * Retrieves the achieved text for provided goal
     * @param goal the goal to process
     * @return the text for the achievedValue textView
     */
    private String getAchievedText(Goal goal) {
        GoalType type = goal.getType();

        if (type == GoalType.DISTANCE) {
            Double value = (Double)goal.getAchievedValue();

            return String.format(Locale.getDefault(), "%,.01fkm", value);
        } else if (type == GoalType.ELEVATION) {
            Integer value = (Integer)goal.getAchievedValue();

            return value + " m";
        } else if (type == GoalType.TIME) {
            Duration value = (Duration)goal.getAchievedValue();

            return Utils.durationToHoursMinutes(value); // TODO maybe show seconds too
        }

        return null;
    }

    /**
     * Retrieves the remaining text for provided goal
     * @param goal the goal to process
     * @return the text for the remainingValue textView
     */
    private String getRemainingText(Goal goal) {
        GoalType type = goal.getType();
        Object achieved = goal.getAchievedValue();
        Object target = goal.getTargetValue();

        if (type == GoalType.DISTANCE) {
            Double achieved1 = (Double)achieved;
            Double target1 = (Double)target;
            double subtraction = target1 - achieved1;
            subtraction = Math.max(0, subtraction);

            return String.format(Locale.getDefault(), "%,.01fkm", subtraction);
        } else if (type == GoalType.ELEVATION) {
            Integer achieved1 = (Integer)achieved;
            Integer target1 = (Integer)target;
            int subtraction = target1 - achieved1;
            subtraction = Math.max(0, subtraction);

            return subtraction + "m";
        } else if (type == GoalType.TIME) {
            Duration achieved1 = (Duration)achieved;
            Duration target1 = (Duration)target;

            Duration subtraction = target1.minus(achieved1);

            if (subtraction.compareTo(Duration.ZERO) < 0)
                subtraction = Duration.ZERO;

            return Utils.durationToHoursMinutes(subtraction);
        }

        return null;
    }

    /**
     * Retrieves the color id using the appropriate method for the current API level as on some levels,
     * the methods may be deprecated
     * @param colorId the id of the color
     * @return the int representing the retrieved color
     */
    private int getTextColor(int colorId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getResources().getColor(colorId, null);
        } else {
            return context.getResources().getColor(colorId);
        }
    }

    /**
     * Sets the target date text for this view holder
     * @param holder the holder containing the view
     * @param goal the goal to achieve
     */
    private void setTargetDate(@NonNull ViewHolder holder, Goal goal) {
        if (goal.isCompleted()) {
            holder.targetDate.setTextColor(getTextColor(android.R.color.holo_green_light));
            holder.targetDate.setText("Completed");
        } else if (goal.isExpired()) {
            holder.targetDate.setTextColor(getTextColor(android.R.color.holo_red_light));
            holder.targetDate.setText("Expired");
        } else {
            LocalDateTime target = goal.getTargetDate();
            String text = target.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            holder.targetDate.setTextColor(context.getResources().getColor(android.R.color.secondary_text_light));
            holder.targetDate.setText(text);
        }
    }

    /**
     * Handles the context menu for a goal
     * @param holder the ViewHolder containing the goal's view
     * @param goal the goal that context menu is for
     * @return true if success, false if not
     */
    private boolean handleContextMenu(@NonNull ViewHolder holder, Goal goal) {
        PopupMenu popupMenu = new PopupMenu(context, holder.itemView);
        popupMenu.inflate(R.menu.goal_menu);
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.deleteGoal) {
                context.deleteGoal(goal);
            } else {
                return false;
            }

            return true;
        });

        popupMenu.show();

        return true;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Goal goal = goals.get(position);

        holder.targetText.setText(getTargetText(goal));
        holder.sportValue.setText(Utils.capitalise(goal.getSport().toString()));
        holder.achievedValue.setText(getAchievedText(goal));
        holder.remainingValue.setText(getRemainingText(goal));
        setTargetDate(holder, goal);
        if (canModifyGoals)
            holder.itemView.setOnLongClickListener(view -> handleContextMenu(holder, goal));
    }

    /**
     * Retrieves the number of items in the data set
     * @return number of data items in this adapter
     */
    @Override
    public int getItemCount() {
        return goals.size();
    }

    /**
     * If this is set to false, the popup menu for editing and interacting with goals is hidden.
     * This is useful for when viewing goals that are not your own goals, i.e., they are a different person's
     * goals
     * @param canModifyGoals true if you want to modify items, false if not
     */
    public void setCanModifyGoals(boolean canModifyGoals) {
        this.canModifyGoals = canModifyGoals;
    }
}
