package org.prebid.mobile.rendering.models;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import com.apollo.test.utils.WhiteBox;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.listeners.VideoCreativeViewListener;
import org.prebid.mobile.rendering.video.ExoPlayerView;
import org.prebid.mobile.rendering.video.VideoCreative;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;
import org.prebid.mobile.rendering.views.webview.OpenXWebViewBanner;
import org.prebid.mobile.rendering.views.webview.OpenXWebViewInterstitial;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class ViewPoolTest {

    private ViewPool mViewPool;
    private Context mContext;
    private View mOccupiedView;
    private View mUnoccupiedView;
    private FrameLayout mContainer;

    @Before
    public void setUp() {
        mContext = Robolectric.buildActivity(Activity.class).create().get();
        mViewPool = ViewPool.getInstance();
        mViewPool.clear();
        mOccupiedView = new View(mContext);
        mUnoccupiedView = new View(mContext);
        mContainer = new FrameLayout(mContext);
        mContainer.addView(mOccupiedView);

        mViewPool.addToOccupied(mOccupiedView);
        mViewPool.addToUnoccupied(mUnoccupiedView);
    }

    @After
    public void clearInstance() throws IllegalAccessException {
        WhiteBox.setInternalState(mViewPool, "sInstance", null);
    }

    @Test
    public void sizeOfOccupiedTest() {
        assertEquals(1, mViewPool.sizeOfOccupied());
    }

    @Test
    public void sizeOfUnoccupiedTest() {
        assertEquals(1, mViewPool.sizeOfUnoccupied());
    }

    @Test
    public void addToOccupiedTest() {
        mViewPool.addToOccupied(new View(mContext));
        assertEquals(2, mViewPool.sizeOfOccupied());
    }

    @Test
    public void addToUnoccupiedTest() {
        mViewPool.addToUnoccupied(new View(mContext));
        assertEquals(2, mViewPool.sizeOfUnoccupied());
    }

    @Test
    public void swapToUnoccupiedTest() {
        mViewPool.swapToUnoccupied(mOccupiedView);
        assertNull(mOccupiedView.getParent());
        assertEquals(0, mViewPool.sizeOfOccupied());
        assertEquals(2, mViewPool.sizeOfUnoccupied());
    }

    @Test
    public void clearTest() {
        mViewPool.clear();
        assertEquals(0, mViewPool.sizeOfUnoccupied());
        assertEquals(0, mViewPool.sizeOfOccupied());
    }

    @Test
    public void getUnoccupiedViewTest() throws AdException {
        VideoCreativeViewListener mockVideoCreativeViewListener = mock(VideoCreativeViewListener.class);
        AdConfiguration.AdUnitIdentifierType adType = AdConfiguration.AdUnitIdentifierType.BANNER;
        InterstitialManager mockInterstitialManager = mock(InterstitialManager.class);

        View result = mViewPool.getUnoccupiedView(mContext, mockVideoCreativeViewListener, adType, mockInterstitialManager);
        assertEquals(mUnoccupiedView, result);
        assertEquals(2, mViewPool.sizeOfOccupied());
        assertEquals(0, mViewPool.sizeOfUnoccupied());

        result = mViewPool.getUnoccupiedView(mContext, mockVideoCreativeViewListener, adType, mockInterstitialManager);
        assertThat(result, instanceOf(OpenXWebViewBanner.class));
        assertEquals(3, mViewPool.sizeOfOccupied());
        assertEquals(0, mViewPool.sizeOfUnoccupied());

        adType = AdConfiguration.AdUnitIdentifierType.VAST;
        result = mViewPool.getUnoccupiedView(mContext, mock(VideoCreative.class), adType, mockInterstitialManager);
        assertThat(result, instanceOf(ExoPlayerView.class));
        assertEquals(4, mViewPool.sizeOfOccupied());
        assertEquals(0, mViewPool.sizeOfUnoccupied());

        adType = AdConfiguration.AdUnitIdentifierType.INTERSTITIAL;
        result = mViewPool.getUnoccupiedView(mContext, mockVideoCreativeViewListener, adType, mockInterstitialManager);
        assertThat(result, instanceOf(OpenXWebViewInterstitial.class));
        assertEquals(5, mViewPool.sizeOfOccupied());
        assertEquals(0, mViewPool.sizeOfUnoccupied());
    }
}