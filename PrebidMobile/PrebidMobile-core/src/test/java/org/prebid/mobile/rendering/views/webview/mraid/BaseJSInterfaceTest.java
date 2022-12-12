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

package org.prebid.mobile.rendering.views.webview.mraid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.prebid.mobile.rendering.views.webview.mraid.JSInterface.DEVICE_ORIENTATION;
import static org.prebid.mobile.rendering.views.webview.mraid.JSInterface.DEVICE_ORIENTATION_LOCKED;
import static org.prebid.mobile.rendering.views.webview.mraid.JSInterface.LOCATION_ACCURACY;
import static org.prebid.mobile.rendering.views.webview.mraid.JSInterface.LOCATION_ERROR;
import static org.prebid.mobile.rendering.views.webview.mraid.JSInterface.LOCATION_LASTFIX;
import static org.prebid.mobile.rendering.views.webview.mraid.JSInterface.LOCATION_LAT;
import static org.prebid.mobile.rendering.views.webview.mraid.JSInterface.LOCATION_LON;
import static org.prebid.mobile.rendering.views.webview.mraid.JSInterface.LOCATION_TYPE;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.reflection.Reflection;
import org.prebid.mobile.reflection.sdk.ManagersResolverReflection;
import org.prebid.mobile.rendering.models.CreativeModel;
import org.prebid.mobile.rendering.models.HTMLCreative;
import org.prebid.mobile.rendering.models.internal.MraidEvent;
import org.prebid.mobile.rendering.models.internal.MraidVariableContainer;
import org.prebid.mobile.rendering.mraid.methods.MraidController;
import org.prebid.mobile.rendering.mraid.methods.MraidScreenMetrics;
import org.prebid.mobile.rendering.mraid.methods.network.GetOriginalUrlTask;
import org.prebid.mobile.rendering.mraid.methods.network.RedirectUrlListener;
import org.prebid.mobile.rendering.networking.BaseNetworkTask;
import org.prebid.mobile.rendering.networking.ResponseHandler;
import org.prebid.mobile.rendering.sdk.ManagersResolver;
import org.prebid.mobile.rendering.session.manager.OmAdSessionManager;
import org.prebid.mobile.rendering.utils.device.DeviceVolumeObserver;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;
import org.prebid.mobile.rendering.views.webview.PrebidWebViewBase;
import org.prebid.mobile.rendering.views.webview.WebViewBase;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowLocationManager;

import java.lang.reflect.Field;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class BaseJSInterfaceTest {

    private Activity testActivity;
    private Context mockContext;
    private BaseJSInterface spyBaseJSInterface;
    private HTMLCreative mockCreative;

    @Mock private WebViewBase mockWebViewBase;
    @Mock private PrebidWebViewBase mockPrebidWebViewBase;
    @Mock private JsExecutor mockJsExecutor;
    @Mock private MraidController mockMraidController;
    @Mock private DeviceVolumeObserver mockDeviceVolumeObserver;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        testActivity = Robolectric.buildActivity(Activity.class).create().get();
        mockContext = testActivity.getApplicationContext();

        mockCreative = Mockito.spy(new HTMLCreative(mockContext, mock(CreativeModel.class), mock(OmAdSessionManager.class), mock(InterstitialManager.class)));
        WhiteBox.setInternalState(mockCreative, "mraidController", mockMraidController);

        when(mockPrebidWebViewBase.getCreative()).thenReturn(mockCreative);

        spyBaseJSInterface = Mockito.spy(new BaseJSInterface(testActivity, mockWebViewBase, mockJsExecutor));
        WhiteBox.setInternalState(spyBaseJSInterface, "deviceVolumeObserver", mockDeviceVolumeObserver);

        when(mockWebViewBase.getPreloadedListener()).thenReturn(mockPrebidWebViewBase);

        Mockito.when(mockWebViewBase.post(Mockito.any(Runnable.class))).thenAnswer(
            invocation -> {
                Runnable runnable = invocation.getArgument(0);
                if (runnable != null) {
                    runnable.run();
                }
                return null;
            }
        );
    }

    @After
    public void cleanup() {
        ManagersResolver manager = ManagersResolver.getInstance();
        ManagersResolverReflection.resetManagers(manager);
    }

    @Test
    public void getMaxSizeTest() {
        assertEquals("{}", spyBaseJSInterface.getMaxSize());
        final MraidScreenMetrics screenMetrics = spyBaseJSInterface.getScreenMetrics();

        Rect rect = new Rect(0, 0, 100, 100);
        screenMetrics.setCurrentMaxSizeRect(rect);
        assertEquals("{\"width\":100,\"height\":100}", spyBaseJSInterface.getMaxSize());
    }

    @Test
    public void setMaxSizeTest() {
        assertEquals("{}", spyBaseJSInterface.getMaxSize());

        Rect rect = new Rect(0, 0, 0, 0);
        final MraidScreenMetrics screenMetrics = spyBaseJSInterface.getScreenMetrics();
        screenMetrics.setCurrentMaxSizeRect(rect);
        assertEquals("{\"width\":0,\"height\":0}", spyBaseJSInterface.getMaxSize());

        rect = new Rect(100, 100, 0, 0);
        screenMetrics.setCurrentMaxSizeRect(rect);
        assertEquals("{\"width\":-100,\"height\":-100}", spyBaseJSInterface.getMaxSize());

        rect = new Rect(100, 0, 0, 0);
        screenMetrics.setCurrentMaxSizeRect(rect);
        assertEquals("{\"width\":-100,\"height\":0}", spyBaseJSInterface.getMaxSize());

        rect = new Rect(0, 100, 0, 0);
        screenMetrics.setCurrentMaxSizeRect(rect);
        assertEquals("{\"width\":0,\"height\":-100}", spyBaseJSInterface.getMaxSize());

        rect = new Rect(0, 0, 100, 0);
        screenMetrics.setCurrentMaxSizeRect(rect);
        assertEquals("{\"width\":100,\"height\":0}", spyBaseJSInterface.getMaxSize());

        rect = new Rect(0, 0, 0, 100);
        screenMetrics.setCurrentMaxSizeRect(rect);
        assertEquals("{\"width\":0,\"height\":100}", spyBaseJSInterface.getMaxSize());

        rect = new Rect(0, 0, 100, 100);
        screenMetrics.setCurrentMaxSizeRect(rect);
        assertEquals("{\"width\":100,\"height\":100}", spyBaseJSInterface.getMaxSize());
    }

    @Test
    public void getDefaultPositionTest() {
        assertEquals("{}", spyBaseJSInterface.getDefaultPosition());

        final MraidScreenMetrics screenMetrics = spyBaseJSInterface.getScreenMetrics();
        screenMetrics.setDefaultPosition(new Rect(0, 0, 0, 0));

        assertEquals("{\"x\":0,\"width\":0,\"y\":0,\"height\":0}", spyBaseJSInterface.getDefaultPosition());
    }

    @Test
    public void onOrientationPropertiesChangedTest() throws Exception {
        Field field = WhiteBox.field(BaseJSInterface.class, "mraidEvent");

        spyBaseJSInterface.onOrientationPropertiesChanged("test");
        MraidEvent event = (MraidEvent) field.get(spyBaseJSInterface);

        assertEquals(JSInterface.ACTION_ORIENTATION_CHANGE, event.mraidAction);
        assertEquals("test", event.mraidActionHelper);

        verify(mockMraidController).handleMraidEvent(eq(event), eq(mockCreative), any(WebViewBase.class), any());
    }

    @Test
    public void closeTest() throws Exception {
        Field field = WhiteBox.field(BaseJSInterface.class, "mraidEvent");

        spyBaseJSInterface.close();
        MraidEvent event = (MraidEvent) field.get(spyBaseJSInterface);

        assertEquals(JSInterface.ACTION_CLOSE, event.mraidAction);

        verify(mockMraidController).handleMraidEvent(eq(event), eq(mockCreative), any(WebViewBase.class), any());
    }

    @Test
    public void resizeTest() throws Exception {
        Field field = WhiteBox.field(BaseJSInterface.class, "mraidEvent");

        spyBaseJSInterface.resize();
        MraidEvent event = (MraidEvent) field.get(spyBaseJSInterface);

        assertEquals(JSInterface.ACTION_RESIZE, event.mraidAction);

        verify(mockMraidController).handleMraidEvent(eq(event), eq(mockCreative), any(WebViewBase.class), any());
    }

    @Test
    public void expandNoUrlTest() throws Exception {
        Field field = WhiteBox.field(BaseJSInterface.class, "mraidEvent");

        spyBaseJSInterface.expand();
        MraidEvent event = (MraidEvent) field.get(spyBaseJSInterface);

        assertEquals(JSInterface.ACTION_EXPAND, event.mraidAction);
        assertNull(event.mraidActionHelper);

        verify(mockMraidController).handleMraidEvent(eq(event), eq(mockCreative), any(WebViewBase.class), any());
    }

    @Test
    public void expandWithUrlTest() throws Exception {
        Field field = WhiteBox.field(BaseJSInterface.class, "mraidEvent");

        spyBaseJSInterface.expand(null);
        MraidEvent event = (MraidEvent) field.get(spyBaseJSInterface);

        assertEquals(JSInterface.ACTION_EXPAND, event.mraidAction);

        verify(mockMraidController).handleMraidEvent(eq(event), eq(mockCreative), any(WebViewBase.class), any());
    }

    @Test
    public void openTest() throws Exception {
        Field field = WhiteBox.field(BaseJSInterface.class, "mraidEvent");

        spyBaseJSInterface.open("test");
        MraidEvent event = (MraidEvent) field.get(spyBaseJSInterface);

        verify(mockWebViewBase, times(1)).sendClickCallBack(anyString());
        assertEquals(JSInterface.ACTION_OPEN, event.mraidAction);
        assertEquals("test", event.mraidActionHelper);

        verify(mockMraidController).handleMraidEvent(eq(event), eq(mockCreative), any(WebViewBase.class), any());
    }

    @Test
    public void createCalendarEventTest() throws Exception {
        Field field = WhiteBox.field(BaseJSInterface.class, "mraidEvent");

        spyBaseJSInterface.createCalendarEvent("test");
        MraidEvent event = (MraidEvent) field.get(spyBaseJSInterface);

        assertEquals(JSInterface.ACTION_CREATE_CALENDAR_EVENT, event.mraidAction);
        assertEquals("test", event.mraidActionHelper);

        verify(mockMraidController).handleMraidEvent(eq(event), eq(mockCreative), any(WebViewBase.class), any());
    }

    @Test
    public void storePictureTest() throws Exception {
        Field field = WhiteBox.field(BaseJSInterface.class, "mraidEvent");

        spyBaseJSInterface.storePicture("test");
        MraidEvent event = (MraidEvent) field.get(spyBaseJSInterface);

        assertEquals(JSInterface.ACTION_STORE_PICTURE, event.mraidAction);
        assertEquals("test", event.mraidActionHelper);

        verify(mockMraidController).handleMraidEvent(eq(event), eq(mockCreative), any(WebViewBase.class), any());
    }

    @Test
    public void playVideoTest() throws Exception {
        Field field = WhiteBox.field(BaseJSInterface.class, "mraidEvent");

        spyBaseJSInterface.playVideo("test");
        MraidEvent event = (MraidEvent) field.get(spyBaseJSInterface);

        assertEquals(JSInterface.ACTION_PLAY_VIDEO, event.mraidAction);
        assertEquals("test", event.mraidActionHelper);

        verify(mockMraidController).handleMraidEvent(eq(event), eq(mockCreative), any(WebViewBase.class), any());
    }

    @Test
    public void getPlacementTypeTest() {
        assertNull(spyBaseJSInterface.getPlacementType());
    }

    @Test
    public void setOrientationPropertiesTest() throws IllegalAccessException {
        final MraidVariableContainer mraidVariableContainer = spyBaseJSInterface.getMraidVariableContainer();

        assertNull(mraidVariableContainer.getOrientationProperties());

        String properties = "test";
        mraidVariableContainer.setOrientationProperties(properties);

        assertEquals(properties, mraidVariableContainer.getOrientationProperties());
    }

    @Test
    @LooperMode(LooperMode.Mode.PAUSED)
    public void onStateChangeTest() {
        spyBaseJSInterface.onStateChange(null);
        verify(spyBaseJSInterface, never()).updateScreenMetricsAsync(any());

        spyBaseJSInterface.onStateChange("test");
        verify(spyBaseJSInterface, times(1)).updateScreenMetricsAsync(any());
    }

    @Test
    public void getScreenSizeTest() {
        ManagersResolver manager = ManagersResolver.getInstance();
        manager.prepare(testActivity);
        Reflection.setVariableTo(manager.getDeviceManager(), "windowManager", null);

        String size = spyBaseJSInterface.getScreenSize();
        assertEquals("{\"width\":0,\"height\":0}", size);

        manager.prepare(testActivity);

        size = spyBaseJSInterface.getScreenSize();

        assertNotEquals("{}", size);
    }

    @Test
    public void getCurrentPositionTest() {
        String currentPosition = spyBaseJSInterface.getCurrentPosition();
        assertEquals("{\"x\":0,\"width\":0,\"y\":0,\"height\":0}", currentPosition);

        when(mockWebViewBase.getGlobalVisibleRect(any(Rect.class))).then(invocation -> {
            Rect argumentRect = invocation.getArgument(0);
            argumentRect.left = 1;
            argumentRect.top = 2;
            argumentRect.right = 3;
            argumentRect.bottom = 4;
            return null;
        });

        currentPosition = spyBaseJSInterface.getCurrentPosition();
        assertNotEquals("{\"x\":0,\"width\":0,\"y\":0,\"height\":0}", currentPosition);
    }

    @Test
    @LooperMode(LooperMode.Mode.PAUSED)
    public void prepareAndSendReadyTest() {
        when(mockWebViewBase.getHeight()).thenReturn(1);
        when(mockPrebidWebViewBase.getHeight()).thenReturn(1);

        spyBaseJSInterface.prepareAndSendReady();
        Shadows.shadowOf(Looper.getMainLooper()).idle();

        verify(mockJsExecutor).executeOnReady();
    }

    @Test
    @LooperMode(LooperMode.Mode.PAUSED)
    public void onReadyExpandedTest() {
        when(mockWebViewBase.getHeight()).thenReturn(1);
        when(mockPrebidWebViewBase.getHeight()).thenReturn(1);

        spyBaseJSInterface.onReadyExpanded();
        Shadows.shadowOf(Looper.getMainLooper()).idle();

        verify(spyBaseJSInterface, timeout(100)).updateScreenMetricsAsync(any(Runnable.class));
        verify(spyBaseJSInterface).supports(any());
        verify(mockJsExecutor).executeOnReadyExpanded();
    }

    @Test
    public void setExpandPropertiesTest() {
        final MraidVariableContainer mraidVariableContainer = spyBaseJSInterface.getMraidVariableContainer();
        String expandProperties = mraidVariableContainer.getExpandProperties();

        assertNull(expandProperties);

        mraidVariableContainer.setExpandProperties("test");
        assertEquals("test", mraidVariableContainer.getExpandProperties());
    }

    @Test
    public void setURLForLaunchingTest() throws IllegalAccessException {
        final MraidVariableContainer mraidVariableContainer = spyBaseJSInterface.getMraidVariableContainer();

        assertEquals("", mraidVariableContainer.getUrlForLaunching());

        mraidVariableContainer.setUrlForLaunching("test");
        assertEquals("test", mraidVariableContainer.getUrlForLaunching());
    }

    @Test
    public void getURLForLaunchingTest() throws IllegalAccessException {
        final MraidVariableContainer mraidVariableContainer = spyBaseJSInterface.getMraidVariableContainer();

        assertEquals("", mraidVariableContainer.getUrlForLaunching());
        mraidVariableContainer.setUrlForLaunching("test");
        assertEquals("test", mraidVariableContainer.getUrlForLaunching());
    }

    @Test
    public void getOriginalURLCallBackTest() throws IllegalAccessException {
        RedirectUrlListener mockListener = mock(RedirectUrlListener.class);
        spyBaseJSInterface.followToOriginalUrl("test", mockListener);

        GetOriginalUrlTask redirectedUrlAsyncTask = WhiteBox.getInternalState(spyBaseJSInterface, "redirectedUrlAsyncTask");
        ResponseHandler getOriginalURLCallBack = WhiteBox.getInternalState(redirectedUrlAsyncTask, "responseHandler");

        getOriginalURLCallBack.onResponse(mock(BaseNetworkTask.GetUrlResult.class));
        verify(mockListener).onSuccess(any(), any());

        getOriginalURLCallBack.onResponse(null);
        verify(mockListener).onFailed();

        reset(mockListener);
        getOriginalURLCallBack.onError("test", 0);
        verify(mockListener).onFailed();

        reset(mockListener);
        getOriginalURLCallBack.onErrorWithException(mock(Exception.class), 0);
        verify(mockListener).onFailed();
    }

    @Test
    public void whenGetLocationAndLocationAvailable_ReturnLocationJson() throws JSONException {
        ShadowActivity shadowActivity = shadowOf(testActivity);
        shadowActivity.grantPermissions("android.permission.ACCESS_FINE_LOCATION");

        LocationManager locationManager = (LocationManager) testActivity.getSystemService(Context.LOCATION_SERVICE);
        ShadowLocationManager shadowLocationManager = shadowOf(locationManager);
        Location location = new Location("");
        location.setLatitude(1.0);
        location.setLongitude(2.0);
        location.setAccuracy(3F);
        location.setTime(System.currentTimeMillis() - 4000);
        shadowLocationManager.setLastKnownLocation("gps", location);
        ManagersResolver.getInstance().prepare(testActivity);

        JSONObject locationJson = new JSONObject();
        locationJson.put(LOCATION_LAT, 1.0);
        locationJson.put(LOCATION_LON, 2.0);
        locationJson.put(LOCATION_TYPE, 1);
        locationJson.put(LOCATION_ACCURACY, 3F);
        locationJson.put(LOCATION_LASTFIX, (long) 4);

        assertEquals(locationJson.toString(), spyBaseJSInterface.getLocation());
    }

    @Test
    public void whenGetLocationAndLocationNotAvailable_ReturnError() {
        ManagersResolver.getInstance().prepare(testActivity);

        assertEquals(LOCATION_ERROR, spyBaseJSInterface.getLocation());
    }

    @Test
    @Config(qualifiers = "land")
    public void whenGetAppOrientation_ReturnAppOrientation() throws JSONException {
        testActivity.setRequestedOrientation(1);
        ManagersResolver.getInstance().prepare(testActivity);

        JSONObject appOrientation = new JSONObject();
        appOrientation.put(DEVICE_ORIENTATION, "landscape");
        appOrientation.put(DEVICE_ORIENTATION_LOCKED, true);

        assertEquals(appOrientation.toString(), spyBaseJSInterface.getCurrentAppOrientation());
    }

    @Test
    public void handleScreenViewabilityChange_ExecuteOnViewableChange() {
        spyBaseJSInterface.handleScreenViewabilityChange(true);

        verify(mockJsExecutor).executeOnViewableChange(true);
    }

    @Test
    public void handleScreenViewabilityChangeAndIsViewable_StartVolumeObserver() {
        spyBaseJSInterface.handleScreenViewabilityChange(true);

        verify(mockDeviceVolumeObserver).start();
    }

    @Test
    public void handleScreenViewabilityChangeAndNotViewable_StopVolumeObserver() {
        spyBaseJSInterface.handleScreenViewabilityChange(false);

        verify(mockDeviceVolumeObserver).stop();
    }

    @Test
    public void handleScreenViewabilityChangeAndNotViewable_ExecuteAudioVolumeChangedNull() {
        spyBaseJSInterface.handleScreenViewabilityChange(false);

        verify(mockJsExecutor).executeAudioVolumeChange(null);
    }

    @Test
    public void notifyScreenMetrics_UpdateAdStateWithJsExecutor() {
        final MraidScreenMetrics mockScreenMetrics = mock(MraidScreenMetrics.class);

        spyBaseJSInterface.notifyScreenMetricsChanged();

        verify(mockJsExecutor).executeSetScreenSize(any(Rect.class));
        assertNotNull(spyBaseJSInterface.getScreenMetrics().getCurrentMaxSizeRect());
        verify(mockJsExecutor).executeSetCurrentPosition(any(Rect.class));
        verify(mockJsExecutor).executeSetDefaultPosition(any(Rect.class));
        verify(mockJsExecutor).executeOnSizeChange(any(Rect.class));
    }

    @Test
    public void getDefaultPositionNotSet_ReturnNull() {
        final MraidScreenMetrics screenMetrics = spyBaseJSInterface.getScreenMetrics();
        assertNull(spyBaseJSInterface.getScreenMetrics().getDefaultPosition());
    }

    @Test
    public void getDefaultPositionAndItWasSet_ReturnSetPosition() {
        final MraidScreenMetrics screenMetrics = spyBaseJSInterface.getScreenMetrics();
        Rect rect = new Rect(0, 1, 2, 3);

        screenMetrics.setDefaultPosition(rect);

        assertEquals(rect, screenMetrics.getDefaultPosition());
    }
}