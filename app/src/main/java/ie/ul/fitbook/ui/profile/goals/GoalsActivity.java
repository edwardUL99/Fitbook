package ie.ul.fitbook.ui.profile.goals;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;
import java.util.Map;

import ie.ul.fitbook.R;
import ie.ul.fitbook.custom.LoadingBar;
import ie.ul.fitbook.database.UserDatabase;
import ie.ul.fitbook.goals.DistanceGoal;
import ie.ul.fitbook.goals.ElevationGoal;
import ie.ul.fitbook.goals.Goal;
import ie.ul.fitbook.goals.GoalType;
import ie.ul.fitbook.goals.TimeGoal;
import ie.ul.fitbook.login.Login;

/**
 * This activity represents the activity to display user's goals.
 * If no user ID is passed in with USER_ID_EXTRA, the current user is assumed
 */
public class GoalsActivity extends AppCompatActivity {
    /**
     * Use this extra to pass in the user id of the user to display users of. If this is not passed in,
     * the uid is assumed to be that of the currently logged in user.
     */
    public static final String USER_ID_EXTRA = "ie.ul.fitbook.GOALS_USER_ID";
    /**
     * This is the no goals message for when you are viewing your own goals
     */
    private static final String NO_GOALS_OWN_GOAL = "You have no Goals. Press + to create one";
    /**
     * This is the no goals message for when you are viewing someone else's goals
     */
    private static final String NO_GOALS_OTHER = "This user has no goals";
    /**
     * The goals adapter for the recycler view
     */
    private final GoalsAdapter goalsAdapter = new GoalsAdapter(null, this);
    /**
     * The collection reference used for retrieving goals
     */
    private CollectionReference goalsReference;
    /**
     * The text view that displays that we have no goals message
     */
    private TextView noGoalsMessage;
    /**
     * This boolean tracks if goals can be modified (created, deleted etc.) or if they can be just viewed
     */
    private boolean canModifyGoals;
    /**
     * The progress bar to display on loading
     */
    private LoadingBar progressBar;
    /**
     * Our swipe refresh layout
     */
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goals);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle("Goals");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent received = getIntent();

        String userId;
        String loggedInId = Login.getUserId();

        if (received.hasExtra(USER_ID_EXTRA)) {
            userId = received.getStringExtra(USER_ID_EXTRA);
        } else {
            userId = Login.getUserId();
        }

        if (userId == null || loggedInId == null)
            throw new IllegalStateException("No UserID has been passed to this Activity or there is no user logged in");

        canModifyGoals = userId.equals(loggedInId);
        goalsAdapter.setCanModifyGoals(canModifyGoals);

        goalsReference = new UserDatabase(userId)
                .getChildCollection(Goal.COLLECTION_PATH);

        noGoalsMessage = findViewById(R.id.noGoalsMessage);
        noGoalsMessage.setText(canModifyGoals ? NO_GOALS_OWN_GOAL:NO_GOALS_OTHER);

        RecyclerView recyclerView = findViewById(R.id.goalsView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(goalsAdapter);

        ConstraintLayout goalsContainer = findViewById(R.id.goalsContainer);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setLoadedLayout(goalsContainer);

        swipeRefreshLayout = findViewById(R.id.goalsRefresh);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            progressBar.show();
            getGoals();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    /**
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are <em>not</em> resumed.
     */
    @Override
    protected void onResume() {
        super.onResume();
        progressBar.show();
        getGoals();
    }

    /**
     * Retrieves goals from the database and adds them to the adapter
     */
    private void getGoals() {
        goalsAdapter.clear();
        goalsReference.get()
                .addOnSuccessListener(success -> {
                    List<DocumentSnapshot> documentSnapshots = success.getDocuments();

                    if (documentSnapshots.size() == 0) {
                        noGoalsMessage.setVisibility(View.VISIBLE);
                        progressBar.hide();
                    } else {
                        noGoalsMessage.setVisibility(View.INVISIBLE);
                        for (DocumentSnapshot documentSnapshot : documentSnapshots) {
                            Map<String, Object> data = documentSnapshot.getData();

                            if (data != null && data.containsKey(Goal.TYPE_KEY)) {
                                GoalType type = GoalType.convertToGoalType((String) data.get(Goal.TYPE_KEY));
                                Goal goal = null;

                                switch (type) {
                                    case DISTANCE:
                                        goal = DistanceGoal.fromData(data);
                                        break;
                                    case ELEVATION:
                                        goal = ElevationGoal.fromData(data);
                                        break;
                                    case TIME:
                                        goal = TimeGoal.fromData(data);
                                        break;
                                }

                                goal.setDocumentReference(goalsReference.document(documentSnapshot.getId()));
                                goalsAdapter.addGoal(goal);
                            }
                        }
                    }

                    progressBar.hide();
                })
                .addOnFailureListener(failure -> {
                    failure.printStackTrace();
                    Toast.makeText(this, "An error occurred retrieving goals", Toast.LENGTH_SHORT)
                            .show();
                    progressBar.hideBoth();
                });
    }

    /**
     * This method deletes the provided goal from the database
     * @param goal the goal to delete
     */
    public void deleteGoal(Goal goal) {
        DocumentReference documentReference = goal.getDocumentReference();

        if (documentReference != null) {
            documentReference.delete()
                    .addOnSuccessListener(success -> {
                        goalsAdapter.removeGoal(goal);

                        if (goalsAdapter.getItemCount() == 0)
                            noGoalsMessage.setVisibility(View.VISIBLE);
                    })
                    .addOnFailureListener(fail -> Toast.makeText(this, "Failed to remove goal", Toast.LENGTH_SHORT).show());
        }
    }

    /**
     * Initialize the contents of the Activity's standard options menu.  You
     * should place your menu items in to <var>menu</var>.
     *
     * <p>This is only called once, the first time the options menu is
     * displayed.  To update the menu every time it is displayed, see
     * {@link #onPrepareOptionsMenu}.
     *
     * <p>The default implementation populates the menu with standard system
     * menu items.  These are placed in the {@link Menu#CATEGORY_SYSTEM} group so that
     * they will be correctly ordered with application-defined menu items.
     * Deriving classes should always call through to the base implementation.
     *
     * <p>You can safely hold on to <var>menu</var> (and any items created
     * from it), making modifications to it as desired, until the next
     * time onCreateOptionsMenu() is called.
     *
     * <p>When you add items to the menu, you can implement the Activity's
     * {@link #onOptionsItemSelected} method to handle them there.
     *
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed;
     * if you return false it will not be shown.
     * @see #onPrepareOptionsMenu
     * @see #onOptionsItemSelected
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (canModifyGoals) {
            boolean succeeded = super.onCreateOptionsMenu(menu);
            getMenuInflater().inflate(R.menu.action_bar_goals, menu);

            return succeeded;
        }

        return false;
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     * The default implementation simply returns false to have the normal
     * processing happen (calling the item's Runnable or sending a message to
     * its Handler as appropriate).  You can use this method for any items
     * for which you would like to do processing without those other
     * facilities.
     *
     * <p>Derived classes should call through to the base class for it to
     * perform the default menu handling.</p>
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to
     * proceed, true to consume it here.
     * @see #onCreateOptionsMenu
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.addGoals) {
            onAddGoalsClicked();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * This method handles what will happen if the add goals button is clicked
     */
    private void onAddGoalsClicked() {
        Intent intent = new Intent(this, GoalCreationActivity.class);
        startActivity(intent);
    }
}