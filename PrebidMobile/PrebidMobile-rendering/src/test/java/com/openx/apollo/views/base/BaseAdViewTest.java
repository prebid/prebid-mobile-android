package com.openx.apollo.views.base;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.apollo.test.utils.WhiteBox;
import com.openx.apollo.errors.AdException;
import com.openx.apollo.models.AdConfiguration;
import com.openx.apollo.views.AdViewManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class BaseAdViewTest {

    private BaseAdView mBaseAdView;
    private Context mMockContext;
    private AdViewManager mMockAdViewManager;

    @Before
    public void setUp() throws Exception {
        mMockContext = spy(Robolectric.buildActivity(Activity.class).create().get());

        mBaseAdView = new BaseAdView(mMockContext) {
            @Override
            protected void notifyErrorListeners(AdException adException) {

            }
        };
        mMockAdViewManager = Mockito.mock(AdViewManager.class);
        when(mMockAdViewManager.getAdConfiguration()).thenReturn(new AdConfiguration());
        Field field = WhiteBox.field(BaseAdView.class, "mAdViewManager");
        field.set(mBaseAdView, mMockAdViewManager);
    }

    @Test
    public void getMediaDurationTest() {
        when(mMockAdViewManager.getMediaDuration()).thenReturn(0L);
        assertEquals(0, mBaseAdView.getMediaDuration());
    }

    @Test
    public void whenGetMediaOffsetEmpty_ReturnDefault() {
        when(mMockAdViewManager.getSkipOffset()).thenReturn(0L);
        assertEquals(0, mBaseAdView.getMediaOffset());
    }

    @Test
    public void onWindowFocusChangedTest() throws IllegalAccessException {
        WhiteBox.field(BaseAdView.class, "mScreenVisibility").set(mBaseAdView, -1);

        mBaseAdView.onWindowFocusChanged(true);
        verify(mMockAdViewManager).setAdVisibility(eq(View.VISIBLE));
    }

}