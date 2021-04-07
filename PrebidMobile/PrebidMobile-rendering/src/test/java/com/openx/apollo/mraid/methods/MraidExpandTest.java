package com.openx.apollo.mraid.methods;

import android.app.Activity;

import com.apollo.test.utils.WhiteBox;
import com.openx.apollo.interstitial.AdBaseDialog;
import com.openx.apollo.interstitial.AdExpandedDialog;
import com.openx.apollo.models.HTMLCreative;
import com.openx.apollo.models.internal.MraidVariableContainer;
import com.openx.apollo.mraid.methods.network.RedirectUrlListener;
import com.openx.apollo.views.indicator.AdIndicatorView;
import com.openx.apollo.views.interstitial.InterstitialManager;
import com.openx.apollo.views.webview.OpenXWebViewBase;
import com.openx.apollo.views.webview.WebViewBase;
import com.openx.apollo.views.webview.mraid.BaseJSInterface;
import com.openx.apollo.views.webview.mraid.JSInterface;
import com.openx.apollo.views.webview.mraid.JsExecutor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class MraidExpandTest {

    private MraidExpand mMraidExpand;

    private Activity mTestActivity;
    private WebViewBase mMockWebViewBase;
    private BaseJSInterface mSpyBaseJsInterface;
    private JsExecutor mSpyJsExecutor;

    @Before
    public void setup() {
        mTestActivity = Robolectric.buildActivity(Activity.class).create().get();
        mSpyJsExecutor = spy(new JsExecutor(mMockWebViewBase, null, null));

        mMockWebViewBase = mock(WebViewBase.class);
        mSpyBaseJsInterface = Mockito.spy(new BaseJSInterface(mTestActivity, mMockWebViewBase, mSpyJsExecutor));

        when(mSpyBaseJsInterface.getJsExecutor()).thenReturn(mSpyJsExecutor);
        when(mMockWebViewBase.getMRAIDInterface()).thenReturn(mSpyBaseJsInterface);

        mMraidExpand = new MraidExpand(mTestActivity, mMockWebViewBase, mock(InterstitialManager.class));
    }

    @Test
    public void expandTest() {

        doAnswer(invocation -> {
            RedirectUrlListener listener = invocation.getArgumentAt(1, RedirectUrlListener.class);
            listener.onSuccess("test", "html");
            return null;
        }).when(mSpyBaseJsInterface).followToOriginalUrl(anyString(), any(RedirectUrlListener.class));

        OpenXWebViewBase mockPreloadedListener = mock(OpenXWebViewBase.class);
        HTMLCreative mockCreative = mock(HTMLCreative.class);
        when(mockCreative.getAdIndicatorView()).thenAnswer(invocation -> {
            WhiteBox.setInternalState(mMraidExpand, "mExpandedDialog", mock(AdExpandedDialog.class));
            return mock(AdIndicatorView.class);
        });
        when(mockPreloadedListener.getCreative()).thenReturn(mockCreative);
        when(mMockWebViewBase.getPreloadedListener()).thenReturn(mockPreloadedListener);

        CompletedCallBack callBack = mock(CompletedCallBack.class);
        final MraidVariableContainer mraidVariableContainer = mSpyBaseJsInterface.getMraidVariableContainer();
        mraidVariableContainer.setCurrentState(JSInterface.STATE_DEFAULT);

        mMraidExpand.expand("test", callBack);
        verify(mSpyBaseJsInterface).followToOriginalUrl(anyString(), any(RedirectUrlListener.class));
        assertEquals(mraidVariableContainer.getUrlForLaunching(), "test");
        verify(callBack).expandDialogShown();
    }

    @Test
    public void nullifyDialogTest() throws IllegalAccessException {
        AdBaseDialog mockDialog = mock(AdBaseDialog.class);
        WhiteBox.field(MraidExpand.class, "mExpandedDialog").set(mMraidExpand, mockDialog);

        mMraidExpand.nullifyDialog();

        verify(mockDialog).cleanup();
        verify(mockDialog).cancel();
    }

    @Test
    public void setMraidExpandedTest() {
        mMraidExpand.setMraidExpanded(true);
        assertEquals(true, mMraidExpand.isMraidExpanded());
    }
}
