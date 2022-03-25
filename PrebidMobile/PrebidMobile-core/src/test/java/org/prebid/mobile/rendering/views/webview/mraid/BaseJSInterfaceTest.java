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

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.prebid.mobile.rendering.views.webview.mraid.JSInterface.*;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class BaseJSInterfaceTest {

    private Activity mTestActivity;
    private Context mMockContext;
    private BaseJSInterface mSpyBaseJSInterface;
    private HTMLCreative mMockCreative;

    @Mock private WebViewBase mMockWebViewBase;
    @Mock private PrebidWebViewBase mMockPrebidWebViewBase;
    @Mock private JsExecutor mMockJsExecutor;
    @Mock private MraidController mMockMraidController;
    @Mock private DeviceVolumeObserver mMockDeviceVolumeObserver;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mTestActivity = Robolectric.buildActivity(Activity.class).create().get();
        mMockContext = mTestActivity.getApplicationContext();

        mMockCreative = Mockito.spy(new HTMLCreative(mMockContext, mock(CreativeModel.class), mock(OmAdSessionManager.class), mock(InterstitialManager.class)));
        WhiteBox.setInternalState(mMockCreative, "mMraidController", mMockMraidController);

        when(mMockPrebidWebViewBase.getCreative()).thenReturn(mMockCreative);

        mSpyBaseJSInterface = Mockito.spy(new BaseJSInterface(mTestActivity, mMockWebViewBase, mMockJsExecutor));
        WhiteBox.setInternalState(mSpyBaseJSInterface, "mDeviceVolumeObserver", mMockDeviceVolumeObserver);

        when(mMockWebViewBase.getPreloadedListener()).thenReturn(mMockPrebidWebViewBase);

        Mockito.when(mMockWebViewBase.post(Mockito.any(Runnable.class))).thenAnswer(
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
        ManagersResolver.getInstance().dispose();
    }

    @Test
    public void getMaxSizeTest() {
        assertEquals("{}", mSpyBaseJSInterface.getMaxSize());
        final MraidScreenMetrics screenMetrics = mSpyBaseJSInterface.getScreenMetrics();

        Rect rect = new Rect(0, 0, 100, 100);
        screenMetrics.setCurrentMaxSizeRect(rect);
        assertEquals("{\"width\":100,\"height\":100}", mSpyBaseJSInterface.getMaxSize());
    }

    @Test
    public void setMaxSizeTest() {
        assertEquals("{}", mSpyBaseJSInterface.getMaxSize());

        Rect rect = new Rect(0, 0, 0, 0);
        final MraidScreenMetrics screenMetrics = mSpyBaseJSInterface.getScreenMetrics();
        screenMetrics.setCurrentMaxSizeRect(rect);
        assertEquals("{\"width\":0,\"height\":0}", mSpyBaseJSInterface.getMaxSize());

        rect = new Rect(100, 100, 0, 0);
        screenMetrics.setCurrentMaxSizeRect(rect);
        assertEquals("{\"width\":-100,\"height\":-100}", mSpyBaseJSInterface.getMaxSize());

        rect = new Rect(100, 0, 0, 0);
        screenMetrics.setCurrentMaxSizeRect(rect);
        assertEquals("{\"width\":-100,\"height\":0}", mSpyBaseJSInterface.getMaxSize());

        rect = new Rect(0, 100, 0, 0);
        screenMetrics.setCurrentMaxSizeRect(rect);
        assertEquals("{\"width\":0,\"height\":-100}", mSpyBaseJSInterface.getMaxSize());

        rect = new Rect(0, 0, 100, 0);
        screenMetrics.setCurrentMaxSizeRect(rect);
        assertEquals("{\"width\":100,\"height\":0}", mSpyBaseJSInterface.getMaxSize());

        rect = new Rect(0, 0, 0, 100);
        screenMetrics.setCurrentMaxSizeRect(rect);
        assertEquals("{\"width\":0,\"height\":100}", mSpyBaseJSInterface.getMaxSize());

        rect = new Rect(0, 0, 100, 100);
        screenMetrics.setCurrentMaxSizeRect(rect);
        assertEquals("{\"width\":100,\"height\":100}", mSpyBaseJSInterface.getMaxSize());
    }

    @Test
    public void getDefaultPositionTest() {
        assertEquals("{}", mSpyBaseJSInterface.getDefaultPosition());

        final MraidScreenMetrics screenMetrics = mSpyBaseJSInterface.getScreenMetrics();
        screenMetrics.setDefaultPosition(new Rect(0, 0, 0, 0));

        assertEquals("{\"x\":0,\"width\":0,\"y\":0,\"height\":0}", mSpyBaseJSInterface.getDefaultPosition());
    }

    @Test
    public void onOrientationPropertiesChangedTest() throws Exception {
        Field field = WhiteBox.field(BaseJSInterface.class, "mMraidEvent");

        mSpyBaseJSInterface.onOrientationPropertiesChanged("test");
        MraidEvent event = (MraidEvent) field.get(mSpyBaseJSInterface);

        assertEquals(JSInterface.ACTION_ORIENTATION_CHANGE, event.mraidAction);
        assertEquals("test", event.mraidActionHelper);

        verify(mMockMraidController).handleMraidEvent(eq(event), eq(mMockCreative), any(WebViewBase.class), any());
    }

    @Test
    public void closeTest() throws Exception {
        Field field = WhiteBox.field(BaseJSInterface.class, "mMraidEvent");

        mSpyBaseJSInterface.close();
        MraidEvent event = (MraidEvent) field.get(mSpyBaseJSInterface);

        assertEquals(JSInterface.ACTION_CLOSE, event.mraidAction);

        verify(mMockMraidController).handleMraidEvent(eq(event), eq(mMockCreative), any(WebViewBase.class), any());
    }

    @Test
    public void resizeTest() throws Exception {
        Field field = WhiteBox.field(BaseJSInterface.class, "mMraidEvent");

        mSpyBaseJSInterface.resize();
        MraidEvent event = (MraidEvent) field.get(mSpyBaseJSInterface);

        assertEquals(JSInterface.ACTION_RESIZE, event.mraidAction);

        verify(mMockMraidController).handleMraidEvent(eq(event), eq(mMockCreative), any(WebViewBase.class), any());
    }

    @Test
    public void expandNoUrlTest() throws Exception {
        Field field = WhiteBox.field(BaseJSInterface.class, "mMraidEvent");

        mSpyBaseJSInterface.expand();
        MraidEvent event = (MraidEvent) field.get(mSpyBaseJSInterface);

        assertEquals(JSInterface.ACTION_EXPAND, event.mraidAction);
        assertNull(event.mraidActionHelper);

        verify(mMockMraidController).handleMraidEvent(eq(event), eq(mMockCreative), any(WebViewBase.class), any());
    }

    @Test
    public void expandWithUrlTest() throws Exception {
        Field field = WhiteBox.field(BaseJSInterface.class, "mMraidEvent");

        mSpyBaseJSInterface.expand(null);
        MraidEvent event = (MraidEvent) field.get(mSpyBaseJSInterface);

        assertEquals(JSInterface.ACTION_EXPAND, event.mraidAction);

        verify(mMockMraidController).handleMraidEvent(eq(event), eq(mMockCreative), any(WebViewBase.class), any());
    }

    @Test
    public void openTest() throws Exception {
        Field field = WhiteBox.field(BaseJSInterface.class, "mMraidEvent");

        mSpyBaseJSInterface.open("test");
        MraidEvent event = (MraidEvent) field.get(mSpyBaseJSInterface);

        verify(mMockWebViewBase, times(1)).sendClickCallBack(anyString());
        assertEquals(JSInterface.ACTION_OPEN, event.mraidAction);
        assertEquals("test", event.mraidActionHelper);

        verify(mMockMraidController).handleMraidEvent(eq(event), eq(mMockCreative), any(WebViewBase.class), any());
    }

    @Test
    public void createCalendarEventTest() throws Exception {
        Field field = WhiteBox.field(BaseJSInterface.class, "mMraidEvent");

        mSpyBaseJSInterface.createCalendarEvent("test");
        MraidEvent event = (MraidEvent) field.get(mSpyBaseJSInterface);

        assertEquals(JSInterface.ACTION_CREATE_CALENDAR_EVENT, event.mraidAction);
        assertEquals("test", event.mraidActionHelper);

        verify(mMockMraidController).handleMraidEvent(eq(event), eq(mMockCreative), any(WebViewBase.class), any());
    }

    @Test
    public void storePictureTest() throws Exception {
        Field field = WhiteBox.field(BaseJSInterface.class, "mMraidEvent");

        mSpyBaseJSInterface.storePicture("test");
        MraidEvent event = (MraidEvent) field.get(mSpyBaseJSInterface);

        assertEquals(JSInterface.ACTION_STORE_PICTURE, event.mraidAction);
        assertEquals("test", event.mraidActionHelper);

        verify(mMockMraidController).handleMraidEvent(eq(event), eq(mMockCreative), any(WebViewBase.class), any());
    }

    @Test
    public void playVideoTest() throws Exception {
        Field field = WhiteBox.field(BaseJSInterface.class, "mMraidEvent");

        mSpyBaseJSInterface.playVideo("test");
        MraidEvent event = (MraidEvent) field.get(mSpyBaseJSInterface);

        assertEquals(JSInterface.ACTION_PLAY_VIDEO, event.mraidAction);
        assertEquals("test", event.mraidActionHelper);

        verify(mMockMraidController).handleMraidEvent(eq(event), eq(mMockCreative), any(WebViewBase.class), any());
    }

    @Test
    public void getPlacementTypeTest() {
        assertNull(mSpyBaseJSInterface.getPlacementType());
    }

    @Test
    public void setOrientationPropertiesTest() throws IllegalAccessException {
        final MraidVariableContainer mraidVariableContainer = mSpyBaseJSInterface.getMraidVariableContainer();

        assertNull(mraidVariableContainer.getOrientationProperties());

        String properties = "test";
        mraidVariableContainer.setOrientationProperties(properties);

        assertEquals(properties, mraidVariableContainer.getOrientationProperties());
    }

    @Test
    @LooperMode(LooperMode.Mode.PAUSED)
    public void onStateChangeTest() {
        mSpyBaseJSInterface.onStateChange(null);
        verify(mSpyBaseJSInterface, never()).updateScreenMetricsAsync(any());

        mSpyBaseJSInterface.onStateChange("test");
        verify(mSpyBaseJSInterface, times(1)).updateScreenMetricsAsync(any());
    }

    @Test
    public void getScreenSizeTest() {
        String size = mSpyBaseJSInterface.getScreenSize();
        assertEquals("{\"width\":0,\"height\":0}", size);

        ManagersResolver.getInstance().prepare(mTestActivity);

        size = mSpyBaseJSInterface.getScreenSize();

        assertNotEquals("{}", size);
    }

    @Test
    public void getCurrentPositionTest() {
        String currentPosition = mSpyBaseJSInterface.getCurrentPosition();
        assertEquals("{\"x\":0,\"width\":0,\"y\":0,\"height\":0}", currentPosition);

        when(mMockWebViewBase.getGlobalVisibleRect(any(Rect.class))).then(invocation -> {
            Rect argumentRect = invocation.getArgument(0);
            argumentRect.left = 1;
            argumentRect.top = 2;
            argumentRect.right = 3;
            argumentRect.bottom = 4;
            return null;
        });

        currentPosition = mSpyBaseJSInterface.getCurrentPosition();
        assertNotEquals("{\"x\":0,\"width\":0,\"y\":0,\"height\":0}", currentPosition);
    }

    @Test
    @LooperMode(LooperMode.Mode.PAUSED)
    public void prepareAndSendReadyTest() {
        when(mMockWebViewBase.getHeight()).thenReturn(1);
        when(mMockPrebidWebViewBase.getHeight()).thenReturn(1);

        mSpyBaseJSInterface.prepareAndSendReady();
        Shadows.shadowOf(Looper.getMainLooper()).idle();

        verify(mMockJsExecutor).executeOnReady();
    }

    @Test
    @LooperMode(LooperMode.Mode.PAUSED)
    public void onReadyExpandedTest() {
        when(mMockWebViewBase.getHeight()).thenReturn(1);
        when(mMockPrebidWebViewBase.getHeight()).thenReturn(1);

        mSpyBaseJSInterface.onReadyExpanded();
        Shadows.shadowOf(Looper.getMainLooper()).idle();

        verify(mSpyBaseJSInterface, timeout(100)).updateScreenMetricsAsync(any(Runnable.class));
        verify(mSpyBaseJSInterface).supports(any());
        verify(mMockJsExecutor).executeOnReadyExpanded();
    }

    @Test
    public void setExpandPropertiesTest() {
        final MraidVariableContainer mraidVariableContainer = mSpyBaseJSInterface.getMraidVariableContainer();
        String expandProperties = mraidVariableContainer.getExpandProperties();

        assertNull(expandProperties);

        mraidVariableContainer.setExpandProperties("test");
        assertEquals("test", mraidVariableContainer.getExpandProperties());
    }

    @Test
    public void setURLForLaunchingTest() throws IllegalAccessException {
        final MraidVariableContainer mraidVariableContainer = mSpyBaseJSInterface.getMraidVariableContainer();

        assertEquals("", mraidVariableContainer.getUrlForLaunching());

        mraidVariableContainer.setUrlForLaunching("test");
        assertEquals("test", mraidVariableContainer.getUrlForLaunching());
    }

    @Test
    public void getURLForLaunchingTest() throws IllegalAccessException {
        final MraidVariableContainer mraidVariableContainer = mSpyBaseJSInterface.getMraidVariableContainer();

        assertEquals("", mraidVariableContainer.getUrlForLaunching());
        mraidVariableContainer.setUrlForLaunching("test");
        assertEquals("test", mraidVariableContainer.getUrlForLaunching());
    }

    @Test
    public void getOriginalURLCallBackTest() throws IllegalAccessException {
        RedirectUrlListener mockListener = mock(RedirectUrlListener.class);
        mSpyBaseJSInterface.followToOriginalUrl("test", mockListener);

        GetOriginalUrlTask redirectedUrlAsyncTask = WhiteBox.getInternalState(mSpyBaseJSInterface, "mRedirectedUrlAsyncTask");
        ResponseHandler getOriginalURLCallBack = WhiteBox.getInternalState(redirectedUrlAsyncTask, "mResponseHandler");

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
        ShadowActivity shadowActivity = shadowOf(mTestActivity);
        shadowActivity.grantPermissions("android.permission.ACCESS_FINE_LOCATION");

        LocationManager locationManager = (LocationManager) mTestActivity.getSystemService(Context.LOCATION_SERVICE);
        ShadowLocationManager shadowLocationManager = shadowOf(locationManager);
        Location location = new Location("");
        location.setLatitude(1.0);
        location.setLongitude(2.0);
        location.setAccuracy(3F);
        location.setTime(System.currentTimeMillis() - 4000);
        shadowLocationManager.setLastKnownLocation("gps", location);
        ManagersResolver.getInstance().dispose();
        ManagersResolver.getInstance().prepare(mTestActivity);

        JSONObject locationJson = new JSONObject();
        locationJson.put(LOCATION_LAT, 1.0);
        locationJson.put(LOCATION_LON, 2.0);
        locationJson.put(LOCATION_TYPE, 1);
        locationJson.put(LOCATION_ACCURACY, 3F);
        locationJson.put(LOCATION_LASTFIX, (long) 4);

        assertEquals(locationJson.toString(), mSpyBaseJSInterface.getLocation());
    }

    @Test
    public void whenGetLocationAndLocationNotAvailable_ReturnError() {
        ManagersResolver.getInstance().prepare(mTestActivity);

        assertEquals(LOCATION_ERROR, mSpyBaseJSInterface.getLocation());
    }

    @Test
    @Config(qualifiers = "land")
    public void whenGetAppOrientation_ReturnAppOrientation() throws JSONException {
        mTestActivity.setRequestedOrientation(1);
        ManagersResolver.getInstance().prepare(mTestActivity);

        JSONObject appOrientation = new JSONObject();
        appOrientation.put(DEVICE_ORIENTATION, "landscape");
        appOrientation.put(DEVICE_ORIENTATION_LOCKED, true);

        assertEquals(appOrientation.toString(), mSpyBaseJSInterface.getCurrentAppOrientation());
    }

    @Test
    public void handleScreenViewabilityChange_ExecuteOnViewableChange() {
        mSpyBaseJSInterface.handleScreenViewabilityChange(true);

        verify(mMockJsExecutor).executeOnViewableChange(true);
    }

    @Test
    public void handleScreenViewabilityChangeAndIsViewable_StartVolumeObserver() {
        mSpyBaseJSInterface.handleScreenViewabilityChange(true);

        verify(mMockDeviceVolumeObserver).start();
    }

    @Test
    public void handleScreenViewabilityChangeAndNotViewable_StopVolumeObserver() {
        mSpyBaseJSInterface.handleScreenViewabilityChange(false);

        verify(mMockDeviceVolumeObserver).stop();
    }

    @Test
    public void handleScreenViewabilityChangeAndNotViewable_ExecuteAudioVolumeChangedNull() {
        mSpyBaseJSInterface.handleScreenViewabilityChange(false);

        verify(mMockJsExecutor).executeAudioVolumeChange(null);
    }

    @Test
    public void notifyScreenMetrics_UpdateAdStateWithJsExecutor() {
        final MraidScreenMetrics mockScreenMetrics = mock(MraidScreenMetrics.class);

        mSpyBaseJSInterface.notifyScreenMetricsChanged();

        verify(mMockJsExecutor).executeSetScreenSize(any(Rect.class));
        assertNotNull(mSpyBaseJSInterface.getScreenMetrics().getCurrentMaxSizeRect());
        verify(mMockJsExecutor).executeSetCurrentPosition(any(Rect.class));
        verify(mMockJsExecutor).executeSetDefaultPosition(any(Rect.class));
        verify(mMockJsExecutor).executeOnSizeChange(any(Rect.class));
    }

    @Test
    public void getDefaultPositionNotSet_ReturnNull() {
        final MraidScreenMetrics screenMetrics = mSpyBaseJSInterface.getScreenMetrics();
        assertNull(mSpyBaseJSInterface.getScreenMetrics().getDefaultPosition());
    }

    @Test
    public void getDefaultPositionAndItWasSet_ReturnSetPosition() {
        final MraidScreenMetrics screenMetrics = mSpyBaseJSInterface.getScreenMetrics();
        Rect rect = new Rect(0, 1, 2, 3);

        screenMetrics.setDefaultPosition(rect);

        assertEquals(rect, screenMetrics.getDefaultPosition());
    }
}