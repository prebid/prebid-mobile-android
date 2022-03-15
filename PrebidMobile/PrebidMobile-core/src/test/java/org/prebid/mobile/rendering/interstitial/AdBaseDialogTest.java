/*
 *    Copyright 2018-2021 Prebid.org, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.prebid.mobile.rendering.interstitial;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.FrameLayout;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.models.InterstitialDisplayPropertiesInternal;
import org.prebid.mobile.rendering.models.internal.MraidVariableContainer;
import org.prebid.mobile.rendering.mraid.methods.others.OrientationManager;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;
import org.prebid.mobile.rendering.views.webview.WebViewBase;
import org.prebid.mobile.rendering.views.webview.mraid.BaseJSInterface;
import org.prebid.mobile.rendering.views.webview.mraid.JSInterface;
import org.prebid.mobile.rendering.views.webview.mraid.JsExecutor;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.*;

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
            Handler handler = invocation.getArgument(0);
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