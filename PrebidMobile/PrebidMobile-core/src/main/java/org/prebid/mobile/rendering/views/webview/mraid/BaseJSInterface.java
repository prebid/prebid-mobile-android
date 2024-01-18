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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.*;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.JavascriptInterface;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import org.json.JSONException;
import org.json.JSONObject;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.rendering.models.HTMLCreative;
import org.prebid.mobile.rendering.models.internal.MraidEvent;
import org.prebid.mobile.rendering.models.internal.MraidVariableContainer;
import org.prebid.mobile.rendering.mraid.methods.MraidEventHandlerNotifierRunnable;
import org.prebid.mobile.rendering.mraid.methods.MraidScreenMetrics;
import org.prebid.mobile.rendering.mraid.methods.network.GetOriginalUrlTask;
import org.prebid.mobile.rendering.mraid.methods.network.RedirectUrlListener;
import org.prebid.mobile.rendering.networking.BaseNetworkTask;
import org.prebid.mobile.rendering.networking.parameters.GeoLocationParameterBuilder;
import org.prebid.mobile.rendering.sdk.ManagersResolver;
import org.prebid.mobile.rendering.sdk.deviceData.managers.DeviceInfoManager;
import org.prebid.mobile.rendering.sdk.deviceData.managers.LocationInfoManager;
import org.prebid.mobile.rendering.utils.broadcast.MraidOrientationBroadcastReceiver;
import org.prebid.mobile.rendering.utils.device.DeviceVolumeObserver;
import org.prebid.mobile.rendering.utils.helpers.AppInfoManager;
import org.prebid.mobile.rendering.utils.helpers.HandlerQueueManager;
import org.prebid.mobile.rendering.utils.helpers.MraidUtils;
import org.prebid.mobile.rendering.utils.helpers.Utils;
import org.prebid.mobile.rendering.views.webview.PrebidWebViewBase;
import org.prebid.mobile.rendering.views.webview.WebViewBase;

import java.lang.ref.WeakReference;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

@SuppressLint("NewApi")
public class BaseJSInterface implements JSInterface {

    private static final String TAG = BaseJSInterface.class.getSimpleName();

    @NonNull private final WeakReference<Activity> weakActivity;
    protected Context context;
    protected WebViewBase adBaseView;

    private final JsExecutor jsExecutor;
    private final DeviceVolumeObserver deviceVolumeObserver;

    private final MraidEvent mraidEvent = new MraidEvent();
    private final MraidVariableContainer mraidVariableContainer = new MraidVariableContainer();

    // An ad container, which contains the ad web view in default state, but is empty when expanded.
    protected PrebidWebViewBase defaultAdContainer;

    @NonNull @VisibleForTesting final MraidScreenMetrics screenMetrics;
    @NonNull final ScreenMetricsWaiter screenMetricsWaiter;

    private AsyncTask redirectedUrlAsyncTask;
    private LayoutParams defaultLayoutParams;

    private MraidOrientationBroadcastReceiver orientationBroadcastReceiver = new MraidOrientationBroadcastReceiver(this);


    public BaseJSInterface(
            Context context,
            final WebViewBase adBaseView,
            JsExecutor jsExecutor
    ) {
        this.context = context;
        this.adBaseView = adBaseView;
        this.jsExecutor = jsExecutor;

        this.jsExecutor.setMraidVariableContainer(mraidVariableContainer);

        if (context instanceof Activity) {
            weakActivity = new WeakReference<>((Activity) context);
        } else {
            weakActivity = new WeakReference<>(null);
        }

        //need this for all metric updates - DONOT do this here cos metric update happens in a thread & this js class may not
        //have been initiated by then.
        defaultAdContainer = (PrebidWebViewBase) adBaseView.getPreloadedListener();

        screenMetrics = new MraidScreenMetrics(this.context, this.context.getResources().getDisplayMetrics().density);
        screenMetricsWaiter = new ScreenMetricsWaiter();
        deviceVolumeObserver = new DeviceVolumeObserver(this.context.getApplicationContext(),
                new Handler(Looper.getMainLooper()),
                this.jsExecutor::executeAudioVolumeChange
        );
    }

    @Override
    @JavascriptInterface
    public String getMaxSize() {
        JSONObject maxSize = new JSONObject();
        try {
            final Rect currentMaxSizeRect = screenMetrics.getCurrentMaxSizeRect();
            if (currentMaxSizeRect != null) {
                maxSize.put(JSON_WIDTH, currentMaxSizeRect.width());
                maxSize.put(JSON_HEIGHT, currentMaxSizeRect.height());
                return maxSize.toString();
            }
        }
        catch (Exception e) {
            LogUtil.error(TAG, "Failed getMaxSize() for MRAID: " + Log.getStackTraceString(e));
        }

        return "{}";
    }

    @Override
    @JavascriptInterface
    public String getScreenSize() {
        JSONObject position = new JSONObject();
        try {
            DeviceInfoManager deviceManager = ManagersResolver.getInstance().getDeviceManager();
            position.put(JSON_WIDTH, (int) (deviceManager.getScreenWidth() / Utils.DENSITY));
            position.put(JSON_HEIGHT, (int) (deviceManager.getScreenHeight() / Utils.DENSITY));
            return position.toString();
        }
        catch (Exception e) {
            LogUtil.error(TAG, "Failed getScreenSize() for MRAID: " + Log.getStackTraceString(e));
        }

        return "{}";
    }

    @Override
    @JavascriptInterface
    public String getDefaultPosition() {
        JSONObject position = new JSONObject();
        try {
            Rect rect = screenMetrics.getDefaultPosition();
            position.put(JSON_X, (int) (rect.left / Utils.DENSITY));
            position.put(JSON_Y, (int) (rect.top / Utils.DENSITY));
            position.put(JSON_WIDTH, (int) (rect.right / Utils.DENSITY - rect.left / Utils.DENSITY));
            position.put(JSON_HEIGHT, (int) (rect.bottom / Utils.DENSITY - rect.top / Utils.DENSITY));
            return position.toString();
        }
        catch (Exception e) {
            LogUtil.error(TAG, "Failed to get defaultPosition for MRAID: " + Log.getStackTraceString(e));
        }

        return "{}";
    }

    @Override
    @JavascriptInterface
    public String getCurrentPosition() {
        JSONObject position = new JSONObject();
        Rect rect = new Rect();
        Handler mainHandler = new Handler(Looper.getMainLooper());
        Runnable mainThreadRunnable = () -> adBaseView.getGlobalVisibleRect(rect);
        RunnableFuture<Void> task = new FutureTask<>(mainThreadRunnable, null);
        try {
            mainHandler.post(task);
            task.get();
            position.put(JSON_X, (int) (rect.left / Utils.DENSITY));
            position.put(JSON_Y, (int) (rect.top / Utils.DENSITY));
            position.put(JSON_WIDTH, (int) (rect.right / Utils.DENSITY - rect.left / Utils.DENSITY));
            position.put(JSON_HEIGHT, (int) (rect.bottom / Utils.DENSITY - rect.top / Utils.DENSITY));
            return position.toString();
        }
        catch (Exception e) {
            LogUtil.error(TAG, "Failed to get currentPosition for MRAID: " + Log.getStackTraceString(e));
        }
        return "{}";
    }

    @Override
    @JavascriptInterface
    public void onOrientationPropertiesChanged(String properties) {
        mraidVariableContainer.setOrientationProperties(properties);
        mraidEvent.mraidAction = ACTION_ORIENTATION_CHANGE;
        mraidEvent.mraidActionHelper = properties;

        notifyMraidEventHandler();
    }

    @Override
    @JavascriptInterface
    public String getPlacementType() {
        // This is overridden in the sub class
        return null;
    }

    @Override
    @JavascriptInterface
    public void close() {
        mraidEvent.mraidAction = ACTION_CLOSE;
        notifyMraidEventHandler();
    }

    @Override
    @JavascriptInterface
    public void resize() {

        // passing this off to the MRAIDResize facade
        // trying to thin out this to make this
        // refactorable for the future
        mraidEvent.mraidAction = ACTION_RESIZE;

        if (adBaseView.isMRAID() && orientationBroadcastReceiver != null && orientationBroadcastReceiver.isOrientationChanged()) {
            updateScreenMetricsAsync(this::notifyMraidEventHandler);
        } else {
            notifyMraidEventHandler();
        }

        if (adBaseView.isMRAID() && orientationBroadcastReceiver != null) {
            orientationBroadcastReceiver.setOrientationChanged(false);
        }
    }

    @Override
    @JavascriptInterface
    public void expand() {
        LogUtil.debug(TAG, "Expand with no url");
        expand(null);
    }

    @Override
    @JavascriptInterface
    public void expand(final String url) {
        LogUtil.debug(TAG, "Expand with url: " + url);

        mraidEvent.mraidAction = ACTION_EXPAND;
        mraidEvent.mraidActionHelper = url;

        //call creative's api that handles all mraid events
        notifyMraidEventHandler();
    }

    @Override
    @JavascriptInterface
    public void open(String url) {
        adBaseView.sendClickCallBack(url);

        mraidEvent.mraidAction = ACTION_OPEN;
        mraidEvent.mraidActionHelper = url;

        notifyMraidEventHandler();
    }

    @Override
    @JavascriptInterface
    public void javaScriptCallback(String handlerHash, String method, String value) {
        HandlerQueueManager handlerQueueManager = jsExecutor.getHandlerQueueManager();
        Handler handler = handlerQueueManager.findHandler(handlerHash);
        if (handler != null) {

            Message responseMessage = new Message();
            Bundle bundle = new Bundle();
            bundle.putString(JSON_METHOD, method);
            bundle.putString(JSON_VALUE, value);
            responseMessage.setData(bundle);
            handler.dispatchMessage(responseMessage);
            handlerQueueManager.removeHandler(handlerHash);
        }
    }

    @Override
    @JavascriptInterface
    public void createCalendarEvent(String parameters) {
        adBaseView.sendClickCallBack(parameters);

        mraidEvent.mraidAction = ACTION_CREATE_CALENDAR_EVENT;
        mraidEvent.mraidActionHelper = parameters;

        notifyMraidEventHandler();
    }

    @Override
    @JavascriptInterface
    public void storePicture(String url) {
        adBaseView.sendClickCallBack(url);

        mraidEvent.mraidAction = ACTION_STORE_PICTURE;
        mraidEvent.mraidActionHelper = url;

        notifyMraidEventHandler();
    }

    @Override
    @JavascriptInterface
    public boolean supports(String feature) {
        return MraidUtils.isFeatureSupported(feature);
    }

    @Override
    @JavascriptInterface
    public void playVideo(String url) {
        mraidEvent.mraidAction = ACTION_PLAY_VIDEO;
        mraidEvent.mraidActionHelper = url;

        notifyMraidEventHandler();
    }

    @Override
    @Deprecated
    @JavascriptInterface
    public void shouldUseCustomClose(String useCustomClose) {
        jsExecutor.executeNativeCallComplete();
        LogUtil.debug(TAG, "Deprecated: useCustomClose was deprecated in MRAID 3");
    }

    @Override
    @JavascriptInterface
    public String getLocation() {
        LocationInfoManager locationInfoManager = ManagersResolver.getInstance().getLocationManager();

        JSONObject location = new JSONObject();
        if (locationInfoManager.isLocationAvailable()) {
            try {
                location.put(LOCATION_LAT, locationInfoManager.getLatitude());
                location.put(LOCATION_LON, locationInfoManager.getLongitude());
                // type - static value "1" - GPS provider
                location.put(LOCATION_TYPE, GeoLocationParameterBuilder.LOCATION_SOURCE_GPS);
                location.put(LOCATION_ACCURACY, locationInfoManager.getAccuracy());
                location.put(LOCATION_LASTFIX, locationInfoManager.getElapsedSeconds());
                return location.toString();
            }
            catch (JSONException e) {
                LogUtil.error(TAG, "MRAID: Error providing location: " + Log.getStackTraceString(e));
            }
        }

        return LOCATION_ERROR;
    }

    @Override
    @JavascriptInterface
    public String getCurrentAppOrientation() {
        DeviceInfoManager deviceManager = ManagersResolver.getInstance().getDeviceManager();
        int deviceOrientation = deviceManager.getDeviceOrientation();
        String orientation = deviceOrientation == Configuration.ORIENTATION_PORTRAIT
                             ? "portrait"
                             : "landscape";

        JSONObject deviceOrientationJson = new JSONObject();
        try {
            deviceOrientationJson.put(DEVICE_ORIENTATION, orientation);
            deviceOrientationJson.put(DEVICE_ORIENTATION_LOCKED, deviceManager.isActivityOrientationLocked(context));
            return deviceOrientationJson.toString();
        }
        catch (JSONException e) {
            LogUtil.error(TAG, "MRAID: Error providing deviceOrientationJson: " + Log.getStackTraceString(e));
        }

        return "{}";
    }

    @Override
    @JavascriptInterface
    public void unload() {
        LogUtil.debug(TAG, "unload called");
        mraidEvent.mraidAction = ACTION_UNLOAD;
        notifyMraidEventHandler();
    }

    public void onStateChange(String state) {
        if (state == null) {
            LogUtil.debug(TAG, "onStateChange failure. State is null");
            return;
        }
        orientationBroadcastReceiver.setState(state);
        updateScreenMetricsAsync(() -> jsExecutor.executeStateChange(state));
    }

    public void handleScreenViewabilityChange(boolean isViewable) {
        jsExecutor.executeOnViewableChange(isViewable);

        if (isViewable) {
            deviceVolumeObserver.start();
        }
        else {
            deviceVolumeObserver.stop();
            jsExecutor.executeAudioVolumeChange(null);
        }
    }

    public MraidVariableContainer getMraidVariableContainer() {
        return mraidVariableContainer;
    }

    public MraidScreenMetrics getScreenMetrics() {
        return screenMetrics;
    }

    public PrebidWebViewBase getDefaultAdContainer() {
        return defaultAdContainer;
    }

    public void onError(String message, String action) {
        jsExecutor.executeOnError(message, action);
    }

    public void followToOriginalUrl(String url, final RedirectUrlListener listener) {
        GetOriginalUrlTask.GetUrlParams params = new BaseNetworkTask.GetUrlParams();

        params.url = url;
        params.name = BaseNetworkTask.REDIRECT_TASK;
        params.requestType = "GET";
        params.userAgent = AppInfoManager.getUserAgent();

        GetOriginalUrlTask redirectTask = new GetOriginalUrlTask(new OriginalUrlResponseCallBack(listener));
        redirectedUrlAsyncTask = redirectTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
    }

    public void setDefaultLayoutParams(LayoutParams originalParentLayoutParams) {
        defaultLayoutParams = originalParentLayoutParams;
    }

    public LayoutParams getDefaultLayoutParams() {
        return defaultLayoutParams;
    }

    public ViewGroup getRootView() {
        final View bestRootView = Views.getTopmostView(weakActivity.get(), defaultAdContainer);
        return bestRootView instanceof ViewGroup ? (ViewGroup) bestRootView : defaultAdContainer;
    }

    public JsExecutor getJsExecutor() {
        return jsExecutor;
    }

    public void loading() {
        jsExecutor.loading();
    }

    public void onReadyExpanded() {
        if (adBaseView != null) {
            Rect defaultPosition = new Rect();
            adBaseView.getGlobalVisibleRect(defaultPosition);
            screenMetrics.setDefaultPosition(defaultPosition);
            supports(MraidVariableContainer.getDisabledFlags());

            updateScreenMetricsAsync(() -> {
                LogUtil.debug(TAG, "MRAID OnReadyExpanded Fired");
                jsExecutor.executeStateChange(STATE_EXPANDED);
                jsExecutor.executeOnReadyExpanded();
            });
        }
    }

    public void prepareAndSendReady() {
        /*
         * Page 28 of the MRAID 2.0 spec says "Note that when getting the expand
         * properties before setting them, the values for width and height will
         * reflect the actual values of the screen. This will allow ad designers
         * who want to use application or device values to adjust as necessary."
         * This means we should set the expandProperties with the screen width
         * and height immediately upon prepareAndSendReady.
         */
        if (adBaseView != null && screenMetrics.getDefaultPosition() == null) {
            Rect defaultPosition = new Rect();

            adBaseView.getGlobalVisibleRect(defaultPosition);
            screenMetrics.setDefaultPosition(defaultPosition);
            registerReceiver();

            jsExecutor.executeDisabledFlags(MraidVariableContainer.getDisabledFlags());
            jsExecutor.executeStateChange(STATE_DEFAULT);
            jsExecutor.executeOnReady();
        }
    }

    /**
     * Updates screen metrics, calling the successRunnable once they are available. The
     * successRunnable will always be called asynchronously, ie on the next main thread loop.
     */
    public void updateScreenMetricsAsync(
        @Nullable
        final Runnable successRunnable) {
        if (adBaseView == null) {
            return;
        }
        defaultAdContainer = (PrebidWebViewBase) adBaseView.getPreloadedListener();
        // Determine which web view should be used for the current ad position

        LogUtil.debug(TAG, "updateMetrics()  Width: " + adBaseView.getWidth() + " Height: " + adBaseView.getHeight());
        // Wait for the next draw pass on the default ad container and current web view

        screenMetricsWaiter.queueMetricsRequest(() -> {

            if (context != null) {
                DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
                screenMetrics.setScreenSize(displayMetrics.widthPixels, displayMetrics.heightPixels);
            }

            int[] location = new int[2];
            View rootView = getRootView();
            if (rootView != null) {
                rootView.getLocationOnScreen(location);
                screenMetrics.setRootViewPosition(location[0], location[1], rootView.getWidth(), rootView.getHeight());
            }

            adBaseView.getLocationOnScreen(location);
            screenMetrics.setCurrentAdPosition(location[0], location[1], adBaseView.getWidth(), adBaseView.getHeight());

            defaultAdContainer.getLocationOnScreen(location);

            screenMetrics.setDefaultAdPosition(location[0],
                    location[1],
                    defaultAdContainer.getWidth(),
                    defaultAdContainer.getHeight()
            );

            notifyScreenMetricsChanged();

            if (successRunnable != null) {
                successRunnable.run();
            }
            screenMetricsWaiter.finishAndStartNextRequest();
        }, successRunnable != null, defaultAdContainer, adBaseView);
    }

    public void destroy() {
        screenMetricsWaiter.cancelPendingRequests();
        orientationBroadcastReceiver.unregister();
        deviceVolumeObserver.stop();

        if (redirectedUrlAsyncTask != null) {
            redirectedUrlAsyncTask.cancel(true);
        }

        // Remove all ad containers from view hierarchy
        Views.removeFromParent(defaultAdContainer);

        context = null;
    }

    protected void notifyScreenMetricsChanged() {
        final Rect rootViewRectDips = screenMetrics.getRootViewRectDips();

        screenMetrics.setCurrentMaxSizeRect(rootViewRectDips);

        jsExecutor.executeSetScreenSize(screenMetrics.getScreenRectDips());
        jsExecutor.executeSetMaxSize(rootViewRectDips);
        jsExecutor.executeSetCurrentPosition(screenMetrics.getCurrentAdRectDips());
        jsExecutor.executeSetDefaultPosition(screenMetrics.getDefaultAdRectDips());

        jsExecutor.executeOnSizeChange(screenMetrics.getCurrentAdRect());
    }

    private void notifyMraidEventHandler() {
        orientationBroadcastReceiver.setMraidAction(mraidEvent.mraidAction);
        HTMLCreative htmlCreative = ((PrebidWebViewBase) adBaseView.getPreloadedListener()).getCreative();
        adBaseView.post(new MraidEventHandlerNotifierRunnable(htmlCreative, adBaseView, mraidEvent, jsExecutor));
    }

    private void registerReceiver() {
        if (adBaseView.isMRAID()) {
            orientationBroadcastReceiver.register(context);
        }
    }
}
