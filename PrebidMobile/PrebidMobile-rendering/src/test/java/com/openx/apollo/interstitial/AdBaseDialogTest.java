package com.openx.apollo.interstitial;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.FrameLayout;

import com.apollo.test.utils.WhiteBox;
import com.openx.apollo.errors.AdException;
import com.openx.apollo.models.InterstitialDisplayPropertiesInternal;
import com.openx.apollo.models.internal.MraidVariableContainer;
import com.openx.apollo.mraid.methods.others.OrientationManager;
import com.openx.apollo.views.interstitial.InterstitialManager;
import com.openx.apollo.views.webview.WebViewBase;
import com.openx.apollo.views.webview.mraid.BaseJSInterface;
import com.openx.apollo.views.webview.mraid.JSInterface;
import com.openx.apollo.views.webview.mraid.JsExecutor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class AdBaseDialogTest {

    private AdBaseDialog mAdBaseDialog;

    @Mock
    Activity mMockActivity;
    Context mMockContext;
    @Mock
    WebViewBase mMockWebViewBase;
    @Mock
    BaseJSInterface mMockBaseJSInterface;
    @Mock
    JsExecutor mMockJsExecutor;
    @Mock
    InterstitialManager mMockInterstitialManager;
    @Mock
    MraidVariableContainer mMockMraidVariableContainer;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mMockContext = Robolectric.buildActivity(Activity.class).create().get().getApplicationContext();

        when(mMockWebViewBase.getMRAIDInterface()).thenReturn(mMockBaseJSInterface);
        when(mMockBaseJSInterface.getJsExecutor()).thenReturn(mMockJsExecutor);
        when(mMockBaseJSInterface.getMraidVariableContainer()).thenReturn(mMockMraidVariableContainer);
        when(mMockInterstitialManager.getInterstitialDisplayProperties()).thenReturn(mock(InterstitialDisplayPropertiesInternal.class));

        mAdBaseDialog = spy(new AdBaseDialog(mMockContext, mMockWebViewBase, mMockInterstitialManager) {
            @Override
            protected void handleCloseClick() {

            }

            @Override
            protected void handleDialogShow() {

            }
        });

        when(mAdBaseDialog.getActivity()).thenReturn(mMockActivity);
    }

    @Test
    public void preInitTest() throws Exception {

        FrameLayout mockAdContainer = mock(FrameLayout.class);
        Field adContainerField = WhiteBox.field(AdBaseDialog.class, "mAdViewContainer");
        adContainerField.set(mAdBaseDialog, mockAdContainer);

        when(mMockWebViewBase.isMRAID()).thenReturn(false);
        mAdBaseDialog.preInit();
        verify(mAdBaseDialog, times(1)).init();
        verify(mAdBaseDialog, times(0)).MraidContinue();
        verify(mockAdContainer, atLeastOnce()).addView(eq(mMockWebViewBase), anyInt());

        reset(mAdBaseDialog);
        when(mMockWebViewBase.isMRAID()).thenReturn(true);
        mAdBaseDialog.preInit();
        verify(mAdBaseDialog, times(0)).init();
        verify(mAdBaseDialog, times(1)).MraidContinue();
        verify(mockAdContainer, atLeastOnce()).addView(eq(mMockWebViewBase), anyInt());
    }

    @Test
    public void onWindowFocusChangedTest() {
        mAdBaseDialog.onWindowFocusChanged(true);
        verify(mMockJsExecutor, times(0)).executeOnViewableChange(anyBoolean());

        mAdBaseDialog.onWindowFocusChanged(false);
        verify(mMockJsExecutor, times(1)).executeOnViewableChange(anyBoolean());
    }

    @Test
    public void MRAIDContinueTest() throws Exception {
        Field hasExpandPropertiesField = WhiteBox.field(AdBaseDialog.class, "mHasExpandProperties");
        String expandedProperties = "{\"width\":0,\"height\":0,\"useCustomClose\":false,\"isModal\":true}";

        doAnswer(prepareHandlerAnswer(expandedProperties))
            .when(mMockJsExecutor).executeGetExpandProperties(any(Handler.class));

        //MRAIDGetExpandProperties()
        hasExpandPropertiesField.set(mAdBaseDialog, false);
        mAdBaseDialog.MraidContinue();
        verify(mAdBaseDialog, times(1)).loadExpandProperties();

        //init()
        reset(mAdBaseDialog);
        hasExpandPropertiesField.set(mAdBaseDialog, true);
        mAdBaseDialog.MraidContinue();
        verify(mAdBaseDialog, times(1)).init();
    }

    private Answer<Object> prepareHandlerAnswer(final String jsonString) {
        return invocation -> {
            Handler handler = invocation.getArgumentAt(0, Handler.class);
            Message message = new Message();

            Bundle data = new Bundle();
            data.putString(JSInterface.JSON_VALUE, jsonString);

            message.setData(data);
            handler.handleMessage(message);
            return null;
        };
    }

    @Test
    public void initTest() {
        mAdBaseDialog.init();

        verify(mMockWebViewBase).requestLayout();
    }

    @Test
    public void lockOrientationTest() throws AdException {
        mAdBaseDialog.lockOrientation(0);

        verify(mMockActivity).setRequestedOrientation(0);
    }

    @Test
    public void renderCustomCloseTest() {
        View mockCloseView = mock(View.class);
        mAdBaseDialog.setCloseView(mockCloseView);

        mAdBaseDialog.changeCloseViewVisibility(View.VISIBLE);

        verify(mockCloseView).setVisibility(View.VISIBLE);
    }

    @Test
    public void handleSetOrientationProperties() throws AdException, IllegalAccessException {
        Field forceOrientationField = WhiteBox.field(AdBaseDialog.class, "mForceOrientation");
        Field allowOrientationChangeField = WhiteBox.field(AdBaseDialog.class, "mAllowOrientationChange");
        String orientationProperties = "{\"allowOrientationChange\":true,\"forceOrientation\":\"landscape\"}";
        when(mMockMraidVariableContainer.getOrientationProperties()).thenReturn(orientationProperties);

        when(mMockWebViewBase.isMRAID()).thenReturn(true);

        mAdBaseDialog.handleSetOrientationProperties();
        assertTrue(allowOrientationChangeField.getBoolean(mAdBaseDialog));
        assertEquals(OrientationManager.ForcedOrientation.landscape, forceOrientationField.get(mAdBaseDialog));
        verify(mMockBaseJSInterface).updateScreenMetricsAsync(null);
    }

    @Test
    public void cleanup() {
        mAdBaseDialog.cleanup();

        verify(mAdBaseDialog).cancel();
    }

    @Test
    public void getActivity() {
        assertEquals(mMockActivity, mAdBaseDialog.getActivity());
    }
}