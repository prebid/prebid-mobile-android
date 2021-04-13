package org.prebid.mobile.rendering.mraid.methods;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.rendering.models.HTMLCreative;
import org.prebid.mobile.rendering.models.internal.MraidVariableContainer;
import org.prebid.mobile.rendering.views.indicator.AdIndicatorView;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;
import org.prebid.mobile.rendering.views.webview.PrebidWebViewBase;
import org.prebid.mobile.rendering.views.webview.WebViewBase;
import org.prebid.mobile.rendering.views.webview.mraid.BaseJSInterface;
import org.prebid.mobile.rendering.views.webview.mraid.JSInterface;
import org.prebid.mobile.rendering.views.webview.mraid.JsExecutor;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class MraidResizeTest {

    private MraidResize mMraidResize;

    private Context mMockContext;

    private BaseJSInterface mSpyBaseJsInterface;

    @Mock
    private WebViewBase mMockWebViewBase;
    @Mock
    private InterstitialManager mMockManager;
    @Mock
    private MraidVariableContainer mMockMraidVariableContainer;
    @Mock
    private JsExecutor mMockJsExecutor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        PrebidWebViewBase prebidWebViewBase = mock(PrebidWebViewBase.class);
        mMockContext = Robolectric.buildActivity(Activity.class).create().get().getApplicationContext();

        mSpyBaseJsInterface = spy(new BaseJSInterface(mMockContext, mMockWebViewBase, mMockJsExecutor));
        when(mSpyBaseJsInterface.getMraidVariableContainer()).thenReturn(mMockMraidVariableContainer);

        when(mMockWebViewBase.post(any(Runnable.class))).thenAnswer(invocation -> {
            Runnable runnable = invocation.getArgumentAt(0, Runnable.class);
            runnable.run();
            return null;
        });
        doAnswer(invocation -> {
            Handler handler = invocation.getArgumentAt(0, Handler.class);
            Message message = new Message();
            Bundle data = new Bundle();
            data.putString(JSInterface.JSON_VALUE, "{\"width\":320,\"height\":250,\"customClosePosition\":\"top-right\",\"offsetX\":0,\"offsetY\":0,\"allowOffscreen\":true}");
            message.setData(data);
            handler.handleMessage(message);
            return null;
        }).when(mMockJsExecutor).executeGetResizeProperties(any(Handler.class));

        when(mMockWebViewBase.getParent()).thenReturn(prebidWebViewBase);
        when(mSpyBaseJsInterface.getDefaultAdContainer()).thenReturn(prebidWebViewBase);

        mMraidResize = new MraidResize(mMockContext, mSpyBaseJsInterface, mMockWebViewBase, mMockManager);
    }

    @Test
    public void resizeWithDefaultState_executeGetResizeProperties() throws Exception {
        when(mMockMraidVariableContainer.getCurrentState()).thenReturn(JSInterface.STATE_DEFAULT);

        mMraidResize.resize();

        verify(mMockJsExecutor).executeGetResizeProperties(Mockito.any(Handler.class));
    }

    @Test
    public void resizeWithExpandedState_executeOnError() throws Exception {
        when(mMockMraidVariableContainer.getCurrentState()).thenReturn(JSInterface.STATE_EXPANDED);

        mMraidResize.resize();

        verify(mSpyBaseJsInterface).onError("resize_when_expanded_error", JSInterface.ACTION_RESIZE);
    }

    @Test
    public void resizeWithInvalidState_DoNothing() throws Exception {
        when(mMockMraidVariableContainer.getCurrentState()).thenReturn(JSInterface.STATE_LOADING);

        mMraidResize.resize();

        verify(mSpyBaseJsInterface, times(1)).getMraidVariableContainer();
        verifyNoMoreInteractions(mSpyBaseJsInterface);
    }

    @Test
    public void showExpandDialogTest() throws InvocationTargetException, IllegalAccessException {

        Method showExpandDialogMethod = WhiteBox.method(MraidResize.class, "showExpandDialog",
                                                        int.class, int.class, int.class,
                                                        int.class, boolean.class);

        Field secondaryAdContainerField = WhiteBox.field(MraidResize.class, "mSecondaryAdContainer");
        secondaryAdContainerField.set(mMraidResize, mock(FrameLayout.class));

        Field closeViewField = WhiteBox.field(MraidResize.class, "mCloseView");
        View mockView = mock(View.class);
        when(mockView.getLayoutParams()).thenReturn(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        closeViewField.set(mMraidResize, mockView);

        MraidScreenMetrics mockMetrics = mock(MraidScreenMetrics.class);
        when(mockMetrics.getRootViewRect()).thenReturn(new Rect(0, 0, 100, 100));
        when(mockMetrics.getDefaultAdRect()).thenReturn(new Rect(0, 0, 0, 0));
        when(mockMetrics.getRootViewRectDips()).thenReturn(new Rect(0, 0, 0, 0));
        when(mSpyBaseJsInterface.getScreenMetrics()).thenReturn(mockMetrics);

        PrebidWebViewBase mockPrebidWebViewBase = mock(PrebidWebViewBase.class);
        when(mMockWebViewBase.getParent()).thenReturn(mockPrebidWebViewBase);
        when(mSpyBaseJsInterface.getDefaultAdContainer()).thenReturn(mockPrebidWebViewBase);
        when(mSpyBaseJsInterface.getRootView()).thenReturn(mock(FrameLayout.class));

        showExpandDialogMethod.invoke(mMraidResize, 100, 100, 0, 0, true);
        verify(mSpyBaseJsInterface).onStateChange(eq(JSInterface.STATE_RESIZED));

        showExpandDialogMethod.invoke(mMraidResize, 1000, 1000, 0, 0, false);
        verify(mSpyBaseJsInterface).onError("Resize properties specified a size & offset that does not allow the ad to appear within the max allowed size", JSInterface.ACTION_RESIZE);

        showExpandDialogMethod.invoke(mMraidResize, 100, 100, 0, 0, false);
        verify(mSpyBaseJsInterface, times(2)).onStateChange(eq(JSInterface.STATE_RESIZED));

        WeakReference<Context> weakReference = new WeakReference<>(null);
        WhiteBox.field(MraidResize.class, "mContextReference").set(mMraidResize, weakReference);
        showExpandDialogMethod.invoke(mMraidResize, 100, 100, 0, 0, false);
        verify(mSpyBaseJsInterface).onError("Unable to resize when mContext is null", JSInterface.ACTION_RESIZE);
    }

    @Test
    public void closeViewTest() throws Exception {
        Method closeViewMethod = WhiteBox.method(MraidResize.class, "closeView");
        MraidClose mockMraidClose = mock(MraidClose.class);

        closeViewMethod.invoke(mMraidResize);
        verify(mMockManager).interstitialClosed(any(View.class));
    }

    @Test
    public void moveAdIndicatorTest() throws InvocationTargetException, IllegalAccessException {
        Method moveAdIndicatorMethod = WhiteBox.method(MraidResize.class, "moveAdIndicator");

        PrebidWebViewBase mockPrebidWebViewBase = mock(PrebidWebViewBase.class);
        HTMLCreative mockCreative = mock(HTMLCreative.class);
        View mockView = mock(AdIndicatorView.class);
        FrameLayout mockSecondaryContainer = mock(FrameLayout.class);
        when(mockCreative.getAdIndicatorView()).thenReturn(mockView);
        when(mockPrebidWebViewBase.getCreative()).thenReturn(mockCreative);
        when(mSpyBaseJsInterface.getDefaultAdContainer()).thenReturn(mockPrebidWebViewBase);

        WhiteBox.field(MraidResize.class, "mSecondaryAdContainer").set(mMraidResize, mockSecondaryContainer);

        moveAdIndicatorMethod.invoke(mMraidResize);
        verify(mockSecondaryContainer).addView(mockView);
    }
}