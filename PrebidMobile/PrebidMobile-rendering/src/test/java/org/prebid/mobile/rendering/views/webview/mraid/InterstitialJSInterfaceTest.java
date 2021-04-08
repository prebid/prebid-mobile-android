package org.prebid.mobile.rendering.views.webview.mraid;

import android.app.Activity;
import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.rendering.views.webview.WebViewBase;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class InterstitialJSInterfaceTest {

    private InterstitialJSInterface mInterstitialJSInterface;
    private Context mContext;
    private WebViewBase mMockWebViewBase;

    @Before
    public void setUp() throws Exception {
        mContext = Robolectric.buildActivity(Activity.class).create().get();
        mMockWebViewBase = mock(WebViewBase.class);

        mInterstitialJSInterface = new InterstitialJSInterface(mContext, mMockWebViewBase, mock(JsExecutor.class));
    }

    @Test
    public void getPlacementTypeTest() {
        assertEquals("interstitial", mInterstitialJSInterface.getPlacementType());
    }

    @Test
    public void expandTest() {
        //do nothing
        mInterstitialJSInterface.expand();
    }

}