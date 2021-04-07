package com.openx.apollo.interstitial;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.apollo.test.utils.WhiteBox;
import com.openx.apollo.models.HTMLCreative;
import com.openx.apollo.views.interstitial.InterstitialManager;
import com.openx.apollo.views.webview.OpenXWebViewBase;
import com.openx.apollo.views.webview.WebViewBase;
import com.openx.apollo.views.webview.mraid.BaseJSInterface;
import com.openx.apollo.views.webview.mraid.JSInterface;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class AdExpandedDialogTest {

    private AdExpandedDialog mAdExpandedDialog;

    private Context mMockContext;
    private WebViewBase mMockWebViewBase;
    private BaseJSInterface mMockBaseJSInterface;
    private OpenXWebViewBase mMockOpenXWebView;
    private InterstitialManager mMockInterstitialManager;

    @Before
    public void setUp() throws Exception {
        mMockContext = Robolectric.buildActivity(Activity.class).create().get();
        mMockWebViewBase = mock(WebViewBase.class);
        mMockBaseJSInterface = mock(BaseJSInterface.class);
        mMockInterstitialManager = mock(InterstitialManager.class);

        mMockOpenXWebView = mock(OpenXWebViewBase.class);

        when(mMockWebViewBase.getMRAIDInterface()).thenReturn(mMockBaseJSInterface);
        when(mMockWebViewBase.getPreloadedListener()).thenReturn(mMockOpenXWebView);

        mAdExpandedDialog = new AdExpandedDialog(mMockContext, mMockWebViewBase, mMockInterstitialManager);
    }

    @Test
    public void handleCloseClick() throws IllegalAccessException {
        InterstitialManager interstitialManager = mock(InterstitialManager.class);
        Field interstitialManagerField = WhiteBox.field(AdInterstitialDialog.class, "mInterstitialManager");
        interstitialManagerField.set(mAdExpandedDialog, interstitialManager);

        mAdExpandedDialog.handleCloseClick();
        verify(interstitialManager).interstitialClosed(eq(mMockWebViewBase));
    }

    @Test
    public void cancelTest() {
        when(mMockOpenXWebView.getCreative()).thenReturn(mock(HTMLCreative.class));
        when(mMockWebViewBase.getJSName()).thenReturn("");

        mAdExpandedDialog.cancel();
        verify(mMockOpenXWebView, atLeast(1)).addView(any(View.class));
        verify(mMockBaseJSInterface).onStateChange(eq(JSInterface.STATE_DEFAULT));
    }
}