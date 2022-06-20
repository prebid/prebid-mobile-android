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
import android.widget.ImageView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.core.R;
import org.prebid.mobile.reflection.Reflection;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class AdBaseDialogTest {

    private AdBaseDialog adBaseDialog;

    @Mock
    Activity mockActivity;
    Context mockContext;
    @Mock
    WebViewBase mockWebViewBase;
    @Mock
    BaseJSInterface mockBaseJSInterface;
    @Mock
    JsExecutor mockJsExecutor;
    @Mock
    InterstitialManager mockInterstitialManager;
    @Mock
    MraidVariableContainer mockMraidVariableContainer;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockContext = Robolectric.buildActivity(Activity.class).create().get().getApplicationContext();

        when(mockWebViewBase.getMRAIDInterface()).thenReturn(mockBaseJSInterface);
        when(mockBaseJSInterface.getJsExecutor()).thenReturn(mockJsExecutor);
        when(mockBaseJSInterface.getMraidVariableContainer()).thenReturn(mockMraidVariableContainer);
        when(mockInterstitialManager.getInterstitialDisplayProperties()).thenReturn(mock(InterstitialDisplayPropertiesInternal.class));

        adBaseDialog = spy(new AdBaseDialog(mockContext, mockWebViewBase, mockInterstitialManager) {
            @Override
            protected void handleCloseClick() {

            }

            @Override
            protected void handleDialogShow() {

            }
        });

        when(adBaseDialog.getActivity()).thenReturn(mockActivity);
    }

    @Test
    public void preInitTest() throws Exception {

        FrameLayout mockAdContainer = mock(FrameLayout.class);
        Field adContainerField = WhiteBox.field(AdBaseDialog.class, "adViewContainer");
        adContainerField.set(adBaseDialog, mockAdContainer);

        when(mockWebViewBase.isMRAID()).thenReturn(false);
        adBaseDialog.preInit();
        verify(adBaseDialog, times(1)).init();
        verify(adBaseDialog, times(0)).MraidContinue();
        verify(mockAdContainer, atLeastOnce()).addView(eq(mockWebViewBase), anyInt());

        reset(adBaseDialog);
        when(mockWebViewBase.isMRAID()).thenReturn(true);
        adBaseDialog.preInit();
        verify(adBaseDialog, times(0)).init();
        verify(adBaseDialog, times(1)).MraidContinue();
        verify(mockAdContainer, atLeastOnce()).addView(eq(mockWebViewBase), anyInt());
    }

    @Test
    public void onWindowFocusChangedTest() {
        adBaseDialog.onWindowFocusChanged(true);
        verify(mockJsExecutor, times(0)).executeOnViewableChange(anyBoolean());

        adBaseDialog.onWindowFocusChanged(false);
        verify(mockJsExecutor, times(1)).executeOnViewableChange(anyBoolean());
    }

    @Test
    public void MRAIDContinueTest() throws Exception {
        Field hasExpandPropertiesField = WhiteBox.field(AdBaseDialog.class, "hasExpandProperties");
        String expandedProperties = "{\"width\":0,\"height\":0,\"useCustomClose\":false,\"isModal\":true}";

        doAnswer(prepareHandlerAnswer(expandedProperties))
                .when(mockJsExecutor).executeGetExpandProperties(any(Handler.class));

        //MRAIDGetExpandProperties()
        hasExpandPropertiesField.set(adBaseDialog, false);
        adBaseDialog.MraidContinue();
        verify(adBaseDialog, times(1)).loadExpandProperties();

        //init()
        reset(adBaseDialog);
        hasExpandPropertiesField.set(adBaseDialog, true);
        adBaseDialog.MraidContinue();
        verify(adBaseDialog, times(1)).init();
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
        adBaseDialog.init();

        verify(mockWebViewBase).requestLayout();
    }

    @Test
    public void lockOrientationTest() throws AdException {
        adBaseDialog.lockOrientation(0);

        verify(mockActivity).setRequestedOrientation(0);
    }

    @Test
    public void renderCustomCloseTest() {
        View mockCloseView = mock(View.class);
        adBaseDialog.setCloseView(mockCloseView);

        adBaseDialog.changeCloseViewVisibility(View.VISIBLE);

        verify(mockCloseView).setVisibility(View.VISIBLE);
    }

    @Test
    public void handleSetOrientationProperties() throws AdException, IllegalAccessException {
        Field forceOrientationField = WhiteBox.field(AdBaseDialog.class, "forceOrientation");
        Field allowOrientationChangeField = WhiteBox.field(AdBaseDialog.class, "allowOrientationChange");
        String orientationProperties = "{\"allowOrientationChange\":true,\"forceOrientation\":\"landscape\"}";
        when(mockMraidVariableContainer.getOrientationProperties()).thenReturn(orientationProperties);

        when(mockWebViewBase.isMRAID()).thenReturn(true);

        adBaseDialog.handleSetOrientationProperties();
        assertTrue(allowOrientationChangeField.getBoolean(adBaseDialog));
        assertEquals(OrientationManager.ForcedOrientation.landscape, forceOrientationField.get(adBaseDialog));
        verify(mockBaseJSInterface).updateScreenMetricsAsync(null);
    }

    @Test
    public void cleanup() {
        adBaseDialog.cleanup();

        verify(adBaseDialog).cancel();
    }

    @Test
    public void getActivity() {
        assertEquals(mockActivity, adBaseDialog.getActivity());
    }

    @Test
    public void testAddSoundView() {
        FrameLayout wrapper = mock(FrameLayout.class);
        Reflection.setVariableTo(adBaseDialog, "adViewContainer", wrapper);

        ImageView view = mock(ImageView.class);
        doReturn(view).when(adBaseDialog).createSoundView(any());

        adBaseDialog.addSoundView(true);

        verify(view).setVisibility(View.VISIBLE);
        verify(view).setImageResource(R.drawable.ic_volume_on);
        verify(view).setTag("on");
        verify(view).setOnClickListener(any());
        verify(wrapper).addView(any());
    }

}