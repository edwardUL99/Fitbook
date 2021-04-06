package ie.ul.fitbook.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * This class is required because a normal scroll view doesn't work with Google maps
 */
public class MapScrollView extends ScrollView {

    public MapScrollView(Context context) {
        super(context);
    }

    public MapScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MapScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:

            case MotionEvent.ACTION_CANCEL:
                super.onTouchEvent(ev);
                break;

            case MotionEvent.ACTION_MOVE:

            case MotionEvent.ACTION_UP:
                return false;

            default:
                break;
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        super.onTouchEvent(ev);
        return true;
    }
}
