package com.openx.apollo.views.video;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import com.apollo.test.utils.WhiteBox;
import com.openx.apollo.video.VideoCreativeView;
import com.openx.apollo.views.AdViewManager;
import com.openx.apollo.views.interstitial.InterstitialManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class VideoDialogTest {

    @Mock
    InterstitialManager mMockInterstitialManager;

    private VideoDialog mVideoDialog;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        Context context = Robolectric.buildActivity(Activity.class).create().get();
        mVideoDialog = spy(new VideoDialog(context, mock(VideoCreativeView.class), mock(AdViewManager.class),
                                           mMockInterstitialManager, mock(FrameLayout.class)));
    }

    @Test
    public void handleCloseClick_DismissDialog() {
        mVideoDialog.handleCloseClick();
        verify(mVideoDialog).dismiss();
    }

    // @Test
    // public void whenClose_NotifyVideoAdManager() {
    //     mVideoDialog.close();
    //     verify(mMockInterstitialManager).videoAdViewInterstitialAdClosed();
    // }

    @Test
    public void showBannerCreative_NullAdView() throws IllegalAccessException {
        mVideoDialog.showBannerCreative(mock(View.class));
        Object adView = WhiteBox.field(VideoDialog.class, "mAdView").get(mVideoDialog);
        Assert.assertNull(adView);
    }
}