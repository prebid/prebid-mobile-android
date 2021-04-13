package org.prebid.mobile.rendering.views.webview.mraid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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
import org.prebid.mobile.rendering.utils.logger.OXLog;
import org.prebid.mobile.rendering.views.webview.PrebidWebViewBase;
import org.prebid.mobile.rendering.views.webview.WebViewBase;

import java.lang.ref.WeakReference;

@SuppressLint("NewApi")
public class BaseJSInterface implements JSInterface {
    private static final String TAG = BaseJSInterface.class.getSimpleName();

    @NonNull
    private final WeakReference<Activity> mWeakActivity;
    protected Context mContext;
    protected WebViewBase mAdBaseView;

    private final JsExecutor mJsExecutor;
    private final DeviceVolumeObserver mDeviceVolumeObserver;

    private final MraidEvent mMraidEvent = new MraidEvent();
    private final MraidVariableContainer mMraidVariableContainer = new MraidVariableContainer();

    // An ad container, which contains the ad web view in default state, but is empty when expanded.
    protected PrebidWebViewBase mDefaultAdContainer;

    @NonNull
    @VisibleForTesting
    final MraidScreenMetrics mScreenMetrics;
    @NonNull
    final ScreenMetricsWaiter mScreenMetricsWaiter;

    private AsyncTask mRedirectedUrlAsyncTask;
    private LayoutParams mDefaultLayoutParams;

    private MraidOrientationBroadcastReceiver mOrientationBroadcastReceiver =
        new MraidOrientationBroadcastReceiver(this);


    public BaseJSInterface(Context context, final WebViewBase adBaseView, JsExecutor jsExecutor) {
        mContext = context;
        mAdBaseView = adBaseView;
        mJsExecutor = jsExecutor;

        mJsExecutor.setMraidVariableContainer(mMraidVariableContainer);

        if (context instanceof Activity) {
            mWeakActivity = new WeakReference<>((Activity) context);
        }
        else {
            mWeakActivity = new WeakReference<>(null);
        }

        //need this for all metric updates - DONOT do this here cos metric update happens in a thread & this js class may not
        //have been initiated by then.
        mDefaultAdContainer = (PrebidWebViewBase) adBaseView.getPreloadedListener();

        mScreenMetrics = new MraidScreenMetrics(mContext, mContext.getResources().getDisplayMetrics().density);
        mScreenMetricsWaiter = new ScreenMetricsWaiter();
        mDeviceVolumeObserver = new DeviceVolumeObserver(mContext.getApplicationContext(),
                                                         new Handler(Looper.getMainLooper()),
                                                         mJsExecutor::executeAudioVolumeChange);
    }

    @Override
    @JavascriptInterface
    public String getMaxSize() {
        JSONObject maxSize = new JSONObject();
        try {
            final Rect currentMaxSizeRect = mScreenMetrics.getCurrentMaxSizeRect();
            maxSize.put(JSON_WIDTH, currentMaxSizeRect.width());
            maxSize.put(JSON_HEIGHT, currentMaxSizeRect.height());
            return maxSize.toString();
        }
        catch (Exception e) {
            OXLog.error(TAG, "Failed getMaxSize() for MRAID: " + Log.getStackTraceString(e));
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
            OXLog.error(TAG, "Failed getScreenSize() for MRAID: " + Log.getStackTraceString(e));
        }

        return "{}";
    }

    @Override
    @JavascriptInterface
    public String getDefaultPosition() {
        JSONObject position = new JSONObject();
        try {
            Rect rect = mScreenMetrics.getDefaultPosition();
            position.put(JSON_X, (int) (rect.left / Utils.DENSITY));
            position.put(JSON_Y, (int) (rect.top / Utils.DENSITY));
            position.put(JSON_WIDTH, (int) (rect.right / Utils.DENSITY - rect.left / Utils.DENSITY));
            position.put(JSON_HEIGHT, (int) (rect.bottom / Utils.DENSITY - rect.top / Utils.DENSITY));
            return position.toString();
        }
        catch (Exception e) {
            OXLog.error(TAG, "Failed to get defaultPosition for MRAID: " + Log.getStackTraceString(e));
        }

        return "{}";
    }

    @Override
    @JavascriptInterface
    public String getCurrentPosition() {
        JSONObject position = new JSONObject();
        Rect rect = new Rect();

        mAdBaseView.getGlobalVisibleRect(rect);

        try {
            position.put(JSON_X, (int) (rect.left / Utils.DENSITY));
            position.put(JSON_Y, (int) (rect.top / Utils.DENSITY));
            position.put(JSON_WIDTH, (int) (rect.right / Utils.DENSITY - rect.left / Utils.DENSITY));
            position.put(JSON_HEIGHT, (int) (rect.bottom / Utils.DENSITY - rect.top / Utils.DENSITY));
            return position.toString();
        }
        catch (Exception e) {
            OXLog.error(TAG, "Failed to get currentPosition for MRAID: " + Log.getStackTraceString(e));
        }
        return "{}";
    }

    @Override
    @JavascriptInterface
    public void onOrientationPropertiesChanged(String properties) {
        mMraidVariableContainer.setOrientationProperties(properties);
        mMraidEvent.mraidAction = ACTION_ORIENTATION_CHANGE;
        mMraidEvent.mraidActionHelper = properties;

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
        mMraidEvent.mraidAction = ACTION_CLOSE;
        notifyMraidEventHandler();
    }

    @Override
    @JavascriptInterface
    public void resize() {

        // passing this off to the MRAIDResize facade
        // trying to thin out this to make this
        // refactorable for the future
        mMraidEvent.mraidAction = ACTION_RESIZE;

        if (mAdBaseView.isMRAID() && mOrientationBroadcastReceiver != null && mOrientationBroadcastReceiver.isOrientationChanged()) {
            updateScreenMetricsAsync(this::notifyMraidEventHandler);
        }
        else {
            notifyMraidEventHandler();
        }

        if (mAdBaseView.isMRAID() && mOrientationBroadcastReceiver != null) {
            mOrientationBroadcastReceiver.setOrientationChanged(false);
        }
    }

    @Override
    @JavascriptInterface
    public void expand() {
        OXLog.debug(TAG, "Expand with no url");
        expand(null);
    }

    @Override
    @JavascriptInterface
    public void expand(final String url) {
        OXLog.debug(TAG, "Expand with url: " + url);

        mMraidEvent.mraidAction = ACTION_EXPAND;
        mMraidEvent.mraidActionHelper = url;

        //call creative's api that handles all mraid events
        notifyMraidEventHandler();
    }

    @Override
    @JavascriptInterface
    public void open(String url) {
        mAdBaseView.sendClickCallBack(url);

        mMraidEvent.mraidAction = ACTION_OPEN;
        mMraidEvent.mraidActionHelper = url;

        notifyMraidEventHandler();
    }

    @Override
    @JavascriptInterface
    public void javaScriptCallback(String handlerHash, String method, String value) {
        HandlerQueueManager handlerQueueManager = mJsExecutor.getHandlerQueueManager();
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
        mAdBaseView.sendClickCallBack(parameters);

        mMraidEvent.mraidAction = ACTION_CREATE_CALENDAR_EVENT;
        mMraidEvent.mraidActionHelper = parameters;

        notifyMraidEventHandler();
    }

    @Override
    @JavascriptInterface
    public void storePicture(String url) {
        mAdBaseView.sendClickCallBack(url);

        mMraidEvent.mraidAction = ACTION_STORE_PICTURE;
        mMraidEvent.mraidActionHelper = url;

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
        mMraidEvent.mraidAction = ACTION_PLAY_VIDEO;
        mMraidEvent.mraidActionHelper = url;

        notifyMraidEventHandler();
    }

    @Override
    @Deprecated
    @JavascriptInterface
    public void shouldUseCustomClose(String useCustomClose) {
        mJsExecutor.executeNativeCallComplete();
        OXLog.debug(TAG, "Deprecated: useCustomClose was deprecated in MRAID 3");
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
                OXLog.error(TAG, "MRAID: Error providing location: " + Log.getStackTraceString(e));
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
            deviceOrientationJson.put(DEVICE_ORIENTATION_LOCKED, deviceManager.isActivityOrientationLocked(mContext));
            return deviceOrientationJson.toString();
        }
        catch (JSONException e) {
            OXLog.error(TAG, "MRAID: Error providing deviceOrientationJson: " + Log.getStackTraceString(e));
        }

        return "{}";
    }

    @Override
    @JavascriptInterface
    public void unload() {
        OXLog.debug(TAG, "unload called");
        mMraidEvent.mraidAction = ACTION_UNLOAD;
        notifyMraidEventHandler();
    }

    public void onStateChange(String state) {
        if (state == null) {
            OXLog.debug(TAG, "onStateChange failure. State is null");
            return;
        }
        mOrientationBroadcastReceiver.setState(state);
        updateScreenMetricsAsync(() -> mJsExecutor.executeStateChange(state));
    }

    public void handleScreenViewabilityChange(boolean isViewable) {
        mJsExecutor.executeOnViewableChange(isViewable);

        if (isViewable) {
            mDeviceVolumeObserver.start();
        }
        else {
            mDeviceVolumeObserver.stop();
            mJsExecutor.executeAudioVolumeChange(null);
        }
    }

    public MraidVariableContainer getMraidVariableContainer() {
        return mMraidVariableContainer;
    }

    public MraidScreenMetrics getScreenMetrics() {
        return mScreenMetrics;
    }

    public PrebidWebViewBase getDefaultAdContainer() {
        return mDefaultAdContainer;
    }

    public void onError(String message, String action) {
        mJsExecutor.executeOnError(message, action);
    }

    public void followToOriginalUrl(String url, final RedirectUrlListener listener) {
        GetOriginalUrlTask.GetUrlParams params = new BaseNetworkTask.GetUrlParams();

        params.url = url;
        params.name = BaseNetworkTask.REDIRECT_TASK;
        params.requestType = "GET";
        params.userAgent = AppInfoManager.getUserAgent();

        GetOriginalUrlTask redirectTask = new GetOriginalUrlTask(new OriginalUrlResponseCallBack(listener));
        mRedirectedUrlAsyncTask = redirectTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
    }

    public void setDefaultLayoutParams(LayoutParams originalParentLayoutParams) {
        mDefaultLayoutParams = originalParentLayoutParams;
    }

    public LayoutParams getDefaultLayoutParams() {
        return mDefaultLayoutParams;
    }

    public ViewGroup getRootView() {
        final View bestRootView = Views.getTopmostView(mWeakActivity.get(),
                                                       mDefaultAdContainer);
        return bestRootView instanceof ViewGroup
               ? (ViewGroup) bestRootView
               : mDefaultAdContainer;
    }

    public JsExecutor getJsExecutor() {
        return mJsExecutor;
    }

    public void loading() {
        mJsExecutor.loading();
    }

    public void onReadyExpanded() {
        if (mAdBaseView != null) {
            Rect defaultPosition = new Rect();
            mAdBaseView.getGlobalVisibleRect(defaultPosition);
            mScreenMetrics.setDefaultPosition(defaultPosition);
            supports(MraidVariableContainer.getDisabledFlags());

            updateScreenMetricsAsync(() -> {
                OXLog.debug(TAG, "MRAID OnReadyExpanded Fired");
                mJsExecutor.executeStateChange(STATE_EXPANDED);
                mJsExecutor.executeOnReadyExpanded();
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
        if (mAdBaseView != null && mScreenMetrics.getDefaultPosition() == null) {
            Rect defaultPosition = new Rect();

            mAdBaseView.getGlobalVisibleRect(defaultPosition);
            mScreenMetrics.setDefaultPosition(defaultPosition);
            registerReceiver();

            mJsExecutor.executeDisabledFlags(MraidVariableContainer.getDisabledFlags());
            mJsExecutor.executeStateChange(STATE_DEFAULT);
            mJsExecutor.executeOnReady();
        }
    }

    /**
     * Updates screen metrics, calling the successRunnable once they are available. The
     * successRunnable will always be called asynchronously, ie on the next main thread loop.
     */
    public void updateScreenMetricsAsync(
        @Nullable
        final Runnable successRunnable) {
        if (mAdBaseView == null) {
            return;
        }
        mDefaultAdContainer = (PrebidWebViewBase) mAdBaseView.getPreloadedListener();
        // Determine which web view should be used for the current ad position

        OXLog.debug(TAG, "updateMetrics()  Width: " + mAdBaseView.getWidth() + " Height: " + mAdBaseView.getHeight());
        // Wait for the next draw pass on the default ad container and current web view

        mScreenMetricsWaiter.queueMetricsRequest(() -> {

            if (mContext != null) {
                DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
                mScreenMetrics.setScreenSize(
                    displayMetrics.widthPixels, displayMetrics.heightPixels);
            }

            int[] location = new int[2];
            View rootView = getRootView();
            if (rootView != null) {
                rootView.getLocationOnScreen(location);
                mScreenMetrics.setRootViewPosition(location[0], location[1],
                                                   rootView.getWidth(),
                                                   rootView.getHeight());
            }

            mAdBaseView.getLocationOnScreen(location);
            mScreenMetrics.setCurrentAdPosition(location[0], location[1],
                                                mAdBaseView.getWidth(),
                                                mAdBaseView.getHeight());

            mDefaultAdContainer.getLocationOnScreen(location);

            mScreenMetrics.setDefaultAdPosition(location[0], location[1],
                                                mDefaultAdContainer.getWidth(),
                                                mDefaultAdContainer.getHeight());

            notifyScreenMetricsChanged();

            if (successRunnable != null) {
                successRunnable.run();
            }
            mScreenMetricsWaiter.finishAndStartNextRequest();
        }, successRunnable != null, mDefaultAdContainer, mAdBaseView);
    }

    public void destroy() {
        mScreenMetricsWaiter.cancelPendingRequests();
        mOrientationBroadcastReceiver.unregister();
        mDeviceVolumeObserver.stop();

        if (mRedirectedUrlAsyncTask != null) {
            mRedirectedUrlAsyncTask.cancel(true);
        }

        // Remove all ad containers from view hierarchy
        Views.removeFromParent(mDefaultAdContainer);

        mContext = null;
    }

    protected void notifyScreenMetricsChanged() {
        final Rect rootViewRectDips = mScreenMetrics.getRootViewRectDips();

        mScreenMetrics.setCurrentMaxSizeRect(rootViewRectDips);

        mJsExecutor.executeSetScreenSize(mScreenMetrics.getScreenRectDips());
        mJsExecutor.executeSetMaxSize(rootViewRectDips);
        mJsExecutor.executeSetCurrentPosition(mScreenMetrics.getCurrentAdRectDips());
        mJsExecutor.executeSetDefaultPosition(mScreenMetrics.getDefaultAdRectDips());

        mJsExecutor.executeOnSizeChange(mScreenMetrics.getCurrentAdRect());
    }

    private void notifyMraidEventHandler() {
        mOrientationBroadcastReceiver.setMraidAction(mMraidEvent.mraidAction);
        HTMLCreative htmlCreative = ((PrebidWebViewBase) mAdBaseView.getPreloadedListener())
            .getCreative();
        mAdBaseView.post(new MraidEventHandlerNotifierRunnable(htmlCreative,
                                                               mAdBaseView,
                                                               mMraidEvent,
                                                               mJsExecutor));
    }

    private void registerReceiver() {
        if (mAdBaseView.isMRAID()) {
            mOrientationBroadcastReceiver.register(mContext);
        }
    }
}
