package ie.ul.fitbook.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

import ie.ul.fitbook.interfaces.ActionHandler;

/**
 * This class allows you to "trace" certain events of a ScrollView's scroll process.
 * The standard ScrollView does not provide a means of tracking touch events. This class, however,
 * provides a means to set a call back for when events like a scroll is detected or when a scroll is finished
 */
public class TraceableScrollView extends ScrollView {
    /**
     * The handler for when a scroll finished event is detected
     */
    private ActionHandler onScrollFinished;
    /**
     * The handler for when a scroll movement is detected
     */
    private ActionHandler onScrollDetected;
    /**
     * True if a scroll has been detected and an ACTION_UP has not been fired yet
     */
    private boolean scrollDetected;

    /**
     * Creates a TraceableScrollView object
     * @param context the context it is associated with
     */
    public TraceableScrollView(Context context) {
        super(context);
    }

    /**
     * Creates a TraceableScrollView object
     * @param context the context it is associated with
     * @param attrs the related attributes
     */
    public TraceableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Creates a TraceableScrollView object
     * @param context the context it is associated with
     * @param attrs the related attributes
     */
    public TraceableScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Creates a TraceableScrollView object
     * @param context the context it is associated with
     * @param attrs the related attributes
     */
    public TraceableScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * Set the handler to call when a scroll has been completed. This occurs when the
     * user raises their finger from the ScrollView after scrolling it
     * @param onScrollFinished the handler for when touch is released
     */
    public void setOnScrollFinished(ActionHandler onScrollFinished) {
        this.onScrollFinished = onScrollFinished;
    }

    /**
     * Sets the handler to call when a scroll has been detected on the ScrollView. This occurs when the user touches the screen and scrolls it
     * @param onScrollDetected the event handler for when a scroll is detected
     */
    public void setOnScrollDetected(ActionHandler onScrollDetected) {
        this.onScrollDetected = onScrollDetected;
    }

    /**
     * Call this view's OnClickListener, if it is defined.  Performs all normal
     * actions associated with clicking: reporting accessibility event, playing
     * a sound, etc.
     *
     * @return True there was an assigned OnClickListener that was called, false
     * otherwise is returned.
     */
    @Override
    public boolean performClick() {
        return super.performClick();
    }

    /**
     * Check the different touch events and call appropriate event handlers if not null
     * @param ev the event to handle
     * @return true if successful, false if not
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean handled = super.onTouchEvent(ev);
        int action = ev.getAction();

        if (action == MotionEvent.ACTION_UP) {
            if (scrollDetected) {
                if (onScrollFinished != null)
                    onScrollFinished.doAction();
                scrollDetected = false;
            }

            performClick();
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (onScrollDetected != null && getScrollY() != 0)
                onScrollDetected.doAction();
            scrollDetected = true;
        }

        return handled;
    }
}
