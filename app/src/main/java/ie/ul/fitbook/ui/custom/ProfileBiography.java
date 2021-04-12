package ie.ul.fitbook.ui.custom;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import ie.ul.fitbook.R;

/**
 * A card view for profile biography to limit height to a max height
 */
public class ProfileBiography extends CardView {
    public ProfileBiography(@NonNull Context context) {
        super(context);
    }

    public ProfileBiography(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ProfileBiography(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        heightMeasureSpec = MeasureSpec.makeMeasureSpec((int)getResources().getDimension(R.dimen.profile_bio_height), MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
