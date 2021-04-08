package org.prebid.mobile.rendering.views.indicator;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.widget.ImageView;

import com.apollo.test.utils.WhiteBox;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.rendering.R;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class AdIndicatorViewTest {

    private AdIndicatorView mAdIndicatorView;

    @Before
    public void setUp() throws Exception {
        Context context = Robolectric.buildActivity(Activity.class).create().get();
        mAdIndicatorView = new AdIndicatorView(context, AdConfiguration.AdUnitIdentifierType.BANNER);
    }

    @Test
    public void setPositionTest() throws Exception {
        ImageView mockIcon = mock(ImageView.class);
        WhiteBox.field(AdIndicatorView.class, "mAdIconView").set(mAdIndicatorView, mockIcon);

        mAdIndicatorView.setPosition(null);
        verify(mockIcon, never()).setImageResource(anyInt());

        mAdIndicatorView.setPosition(AdIndicatorView.AdIconPosition.BOTTOM);
        assertEquals(Gravity.RIGHT | Gravity.BOTTOM, mAdIndicatorView.getGravity());
        verify(mockIcon).setImageResource(R.drawable.ic_adchoices_collapsed_bottom_right);

        mAdIndicatorView.setPosition(AdIndicatorView.AdIconPosition.TOP);
        assertEquals(Gravity.RIGHT | Gravity.TOP, mAdIndicatorView.getGravity());
        verify(mockIcon).setImageResource(R.drawable.ic_adchoices_collapsed_top_right);

        mAdIndicatorView.mSwitchStatus = AdIndicatorView.AdIconState.AD_ICON_PRESSED;
        mAdIndicatorView.setPosition(AdIndicatorView.AdIconPosition.BOTTOM);
        assertEquals(Gravity.RIGHT | Gravity.BOTTOM, mAdIndicatorView.getGravity());
        verify(mockIcon).setImageResource(R.drawable.ic_adchoices_expanded_bottom_right);

        mAdIndicatorView.setPosition(AdIndicatorView.AdIconPosition.TOP);
        assertEquals(Gravity.RIGHT | Gravity.TOP, mAdIndicatorView.getGravity());
        verify(mockIcon).setImageResource(R.drawable.ic_adchoices_expanded_top_right);
    }

}