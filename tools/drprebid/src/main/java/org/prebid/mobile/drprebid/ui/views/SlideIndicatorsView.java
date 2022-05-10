package org.prebid.mobile.drprebid.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.prebid.mobile.drprebid.R;

public class SlideIndicatorsView extends FrameLayout {

    private ImageView indicator1;
    private ImageView indicator2;
    private ImageView indicator3;

    public SlideIndicatorsView(@NonNull Context context) {
        this(context, null);
    }

    public SlideIndicatorsView(
            @NonNull Context context,
            @Nullable AttributeSet attrs
    ) {
        this(context, attrs, -1);
    }

    public SlideIndicatorsView(
            @NonNull Context context,
            @Nullable AttributeSet attrs,
            int defStyleAttr
    ) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SlideIndicatorsView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_indicators, null, false);
        addView(view);

        indicator1 = view.findViewById(R.id.indicator_1);
        indicator2 = view.findViewById(R.id.indicator_2);
        indicator3 = view.findViewById(R.id.indicator_3);
    }

    public void setSelectedPosition(int position) {
        if (position < 0 || position >= 3) {
            throw new RuntimeException("Index out of bounds for indicator. The value must be between 0 and 2");
        } else {
            indicator1.setBackgroundResource(position == 0 ? R.drawable.indicator_enabled : R.drawable.indicator_disabled);
            indicator2.setBackgroundResource(position == 1 ? R.drawable.indicator_enabled : R.drawable.indicator_disabled);
            indicator3.setBackgroundResource(position == 2 ? R.drawable.indicator_enabled : R.drawable.indicator_disabled);
        }
    }
}
