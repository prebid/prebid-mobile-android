package org.prebid.mobile.rendering.mraid.methods;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import com.apollo.test.utils.WhiteBox;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.models.HTMLCreative;
import org.prebid.mobile.rendering.models.internal.MraidVariableContainer;
import org.prebid.mobile.rendering.views.browser.AdBrowserActivity;
import org.prebid.mobile.rendering.views.indicator.AdIndicatorView;
import org.prebid.mobile.rendering.views.webview.OpenXWebViewBase;
import org.prebid.mobile.rendering.views.webview.WebViewBase;
import org.prebid.mobile.rendering.views.webview.mraid.BaseJSInterface;
import org.prebid.mobile.rendering.views.webview.mraid.JSInterface;
import org.prebid.mobile.rendering.views.webview.mraid.JsExecutor;
import org.prebid.mobile.rendering.views.webview.mraid.ScreenMetricsWaiter;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class MraidCloseTest {

    private MraidClose mMraidClose;
    private Activity mTestActivity;
    private BaseJSInterface mSpyBaseJSInterface;
    private WebViewBase mMockWebViewBase;
    private JsExecutor mMockJsExecutor;

    @Before
    public void setUp() throws Exception {
        mTestActivity = Robolectric.buildActivity(Activity.class).create().get();
        mMockJsExecutor = mock(JsExecutor.class);

        mMockWebViewBase = mock(WebViewBase.class);
        when(mMockWebViewBase.isMRAID()).thenReturn(true);
        when(mMockWebViewBase.getContext()).thenReturn(mTestActivity);

        mSpyBaseJSInterface = Mockito.spy(new BaseJSInterface(mTestActivity, mMockWebViewBase, mMockJsExecutor));
        WhiteBox.setInternalState(mSpyBaseJSInterface, "mScreenMetricsWaiter", mock(ScreenMetricsWaiter.class));
        doNothing().when(mSpyBaseJSInterface).onStateChange(anyString());

        mMraidClose = new MraidClose(mTestActivity, mSpyBaseJSInterface, mMockWebViewBase);
    }

    @Test
    public void closeThroughJSTest() throws Exception {
        ViewGroup mockViewGroup = mock(ViewGroup.class);
        when(mSpyBaseJSInterface.getRootView()).thenReturn(mockViewGroup);
        final MraidVariableContainer mraidVariableContainer = mSpyBaseJSInterface.getMraidVariableContainer();

        mMraidClose = new MraidClose(null, mSpyBaseJSInterface, mMockWebViewBase);
        mraidVariableContainer.setCurrentState(JSInterface.STATE_DEFAULT);
        mMraidClose.closeThroughJS();
        verify(mSpyBaseJSInterface, times(0)).onStateChange(anyString());

        mMraidClose = new MraidClose(mTestActivity, mSpyBaseJSInterface, mMockWebViewBase);
        mraidVariableContainer.setCurrentState(JSInterface.STATE_LOADING);
        mMraidClose.closeThroughJS();
        verify(mSpyBaseJSInterface, times(0)).onStateChange(anyString());

        mraidVariableContainer.setCurrentState(JSInterface.STATE_DEFAULT);
        mMraidClose.closeThroughJS();
        verify(mSpyBaseJSInterface).onStateChange(eq(JSInterface.STATE_HIDDEN));

        mraidVariableContainer.setCurrentState(JSInterface.STATE_EXPANDED);
        mMraidClose.closeThroughJS();
        verify(mSpyBaseJSInterface).onStateChange(eq(JSInterface.STATE_DEFAULT));
        verify(mockViewGroup).removeView(any(View.class));

        reset(mSpyBaseJSInterface);
        AdBrowserActivity mockActivity = new AdBrowserActivity();
        mMraidClose = new MraidClose(mockActivity, mSpyBaseJSInterface, mMockWebViewBase);
        mraidVariableContainer.setCurrentState(JSInterface.STATE_EXPANDED);
        mMraidClose.closeThroughJS();
        verify(mSpyBaseJSInterface).onStateChange(eq(JSInterface.STATE_DEFAULT));
    }

    @Test
    public void makeViewVisibleTest() throws InvocationTargetException, IllegalAccessException {
        Method method = WhiteBox.method(MraidClose.class, "makeViewInvisible");

        method.invoke(mMraidClose);
        verify(mMockWebViewBase, timeout(100)).setVisibility(eq(View.INVISIBLE));
    }

    @Test
    public void changeAdIndicatorPositionTest()
    throws InvocationTargetException, IllegalAccessException {
        Method changeAdIndicatorPositionMethod = WhiteBox.method(MraidClose.class, "changeAdIndicatorPosition", WebViewBase.class);

        OpenXWebViewBase mockOpenXWebViewbase = mock(OpenXWebViewBase.class);
        HTMLCreative mockHtmlCreative = mock(HTMLCreative.class);
        AdIndicatorView mockView = mock(AdIndicatorView.class);
        when(mockHtmlCreative.getAdIndicatorView()).thenReturn(mockView);
        when(mockOpenXWebViewbase.getCreative()).thenReturn(mockHtmlCreative);

        when(mockView.getAdUnitIdentifierType()).thenReturn(AdConfiguration.AdUnitIdentifierType.BANNER);
        when(mMockWebViewBase.getPreloadedListener()).thenReturn(mockOpenXWebViewbase);

        changeAdIndicatorPositionMethod.invoke(mMraidClose, mMockWebViewBase);
        verify(mockView).setPosition(AdIndicatorView.AdIconPosition.TOP);
        verify(mockOpenXWebViewbase).addView(mockView, 0);
        verify(mockOpenXWebViewbase).setVisibility(View.VISIBLE);
    }
}