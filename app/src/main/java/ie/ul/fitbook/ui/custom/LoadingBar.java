package ie.ul.fitbook.ui.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;

/**
 * This class represents a ProgressBar for loading activities. It is expected to be set out as follows:
 *      Parent Layout
 *          LoadingBar
 *          Main activity layout (layout to hide whenever the LoadingBar is displayed). This could be a layout like a ConstraintLayout.
 *
 *  For this class to work, {@link #setLoadedLayout(View view)} needs to be called.
 *  It is undefined behaviour if the loaded layout's visibility is altered outside this class
 */
public class LoadingBar extends ProgressBar {
    /**
     * The layout being loaded by LoadingBar
     */
    private View loadedLayout;
    /**
     * Returns true if loading or not
     */
    private boolean loading;

    /**
     * Create a new loading bar
     *
     * @param context the application environment
     */
    public LoadingBar(Context context) {
        super(context);
    }

    /**
     * Create a new loading bar
     *
     * @param context the application environment
     * @param attrs attributes from XML
     */
    public LoadingBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Create a new loading bar
     *
     * @param context the application environment
     * @param attrs attributes from XML
     * @param defStyleAttr definition of style attributes
     */
    public LoadingBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Create a new loading bar
     *
     * @param context the application environment
     * @param attrs attributes from XML
     * @param defStyleAttr definition of style attributes
     * @param defStyleRes definition of style resource
     */
    public LoadingBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * Returns true if this LoadingBar is displayed or not
     * @return true if loading, false if not
     */
    public boolean isLoading() {
        return loading;
    }

    /**
     * Sets the loaded layout that is hidden/displayed by this class. Needs to be called before
     * {@link #show()} or {@link #hide()} is called
     * @param view the view to show/hide
     * @throws NullPointerException if view is null
     */
    public void setLoadedLayout(View view) {
        if (view == null)
            throw new NullPointerException("Null View provided to setLoadedLayout");

        loadedLayout = view;
    }

    /**
     * Check if loaded layout is not null, and throws IllegalStateException if it is
     */
    private void checkLoadedLayout() {
        if (loadedLayout == null)
            throw new IllegalStateException("Cannot use LoadingBar as setLoadedLayout(view) has not been called");
    }

    /**
     * Shows the loading bar and hides the loaded layout
     */
    public void show() {
        checkLoadedLayout();

        if (!loading) {
            loadedLayout.setVisibility(GONE);
            setVisibility(VISIBLE);
            loading = true;
        }
    }

    /**
     * Hides the loading bar and shows the loaded layout
     */
    public void hide() {
        checkLoadedLayout();

        if (loading) {
            loadedLayout.setVisibility(VISIBLE);
            setVisibility(GONE);
            loading = false;
        }
    }

    /**
     * This method hides both the loading bar and the loaded layout
     */
    public void hideBoth() {
        checkLoadedLayout();

        loadedLayout.setVisibility(GONE);
        setVisibility(GONE);
        loading = false;
    }
}
