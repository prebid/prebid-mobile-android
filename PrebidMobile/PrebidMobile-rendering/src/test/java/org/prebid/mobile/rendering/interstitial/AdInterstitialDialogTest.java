package org.prebid.mobile.rendering.interstitial;

import android.app.Activity;
import android.content.Context;
import android.widget.FrameLayout;

import com.apollo.test.utils.WhiteBox;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;
import org.prebid.mobile.rendering.views.webview.WebViewBase;
import org.prebid.mobile.rendering.views.webview.mraid.BaseJSInterface;
import org.prebid.mobile.rendering.views.webview.mraid.JSInterface;
import org.prebid.mobile.rendering.views.webview.mraid.JsExecutor;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class AdInterstitialDialogTest {

    private AdInterstitialDialog mAdInterstitialDialog;

    private Context mMockContext;
    private WebViewBase mMockWebViewBase;
    private BaseJSInterface mMockBaseJSInterface;
    private FrameLayout mMockAdContainer;
    private InterstitialManager mMockInterstitialManager;

    @Before
    public void setUp() throws Exception {
        mMockContext = Robolectric.buildActivity(Activity.class).create().get();
        mMockWebViewBase = mock(WebViewBase.class);
        mMockAdContainer = mock(FrameLayout.class);
        mMockBaseJSInterface = mock(BaseJSInterface.class);
        mMockInterstitialManager = mock(InterstitialManager.class);

        when(mMockWebViewBase.getMRAIDInterface()).thenReturn(mMockBaseJSInterface);
        when(mMockBaseJSInterface.getJsExecutor()).thenReturn(mock(JsExecutor.class));

        mAdInterstitialDialog = spy(new AdInterstitialDialog(mMockContext, mMockWebViewBase, mMockAdContainer, mMockInterstitialManager));
    }

    @Test
    public void handleCloseClick() throws IllegalAccessException {
        InterstitialManager interstitialManager = mock(InterstitialManager.class);
        Field interstitialManagerField = WhiteBox.field(AdInterstitialDialog.class, "mInterstitialManager");
        interstitialManagerField.set(mAdInterstitialDialog, interstitialManager);

        mAdInterstitialDialog.handleCloseClick();
        verify(interstitialManager).interstitialClosed(mMockWebViewBase);
    }

    @Test
    public void nullifyDialog() {
        mAdInterstitialDialog.nullifyDialog();

        verify(mAdInterstitialDialog, atLeastOnce()).cancel();
        verify(mAdInterstitialDialog).cleanup();
    }

    @Test
    public void cancelTest() {
        when(mMockWebViewBase.isMRAID()).thenReturn(true);

        mAdInterstitialDialog.cancel();
        verify(mMockBaseJSInterface).onStateChange(JSInterface.STATE_DEFAULT);
        verify(mMockWebViewBase).detachFromParent();
    }
}