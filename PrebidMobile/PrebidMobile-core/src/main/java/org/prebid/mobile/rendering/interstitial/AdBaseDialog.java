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
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import org.json.JSONObject;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.core.R;
import org.prebid.mobile.rendering.models.InterstitialDisplayPropertiesInternal;
import org.prebid.mobile.rendering.models.internal.MraidVariableContainer;
import org.prebid.mobile.rendering.mraid.handler.FetchPropertiesHandler;
import org.prebid.mobile.rendering.mraid.methods.others.OrientationManager;
import org.prebid.mobile.rendering.sdk.ManagersResolver;
import org.prebid.mobile.rendering.sdk.deviceData.managers.DeviceInfoManager;
import org.prebid.mobile.rendering.utils.broadcast.OrientationBroadcastReceiver;
import org.prebid.mobile.rendering.utils.helpers.Utils;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;
import org.prebid.mobile.rendering.views.interstitial.InterstitialVideo;
import org.prebid.mobile.rendering.views.webview.WebViewBase;
import org.prebid.mobile.rendering.views.webview.mraid.JsExecutor;
import org.prebid.mobile.rendering.views.webview.mraid.Views;

import java.lang.ref.WeakReference;

//Class to show ad as an interstitial i.e, a fullscreen ad
public abstract class AdBaseDialog extends Dialog {

    private static final String TAG = AdBaseDialog.class.getSimpleName();

    private final WeakReference<Context> contextReference;
    private final OrientationBroadcastReceiver orientationBroadcastReceiver = new OrientationBroadcastReceiver();

    protected JsExecutor jsExecutor;
    protected InterstitialManager interstitialManager;

    protected WebViewBase webViewBase;
    protected FrameLayout adViewContainer;
    protected View displayView;
    protected View soundView;
    protected View skipView;
    private View closeView;

    // IMP: shud be always none. cos this val is used when expand is called with an url.
    protected OrientationManager.ForcedOrientation forceOrientation = OrientationManager.ForcedOrientation.none;
    @Nullable private Integer originalActivityOrientation;
    /**
     * Is used when deciding to handle orientation changes. Orientation changes allowed (true) by default.
     */
    protected boolean allowOrientationChange = true;
    protected boolean hasExpandProperties;

    protected int initialOrientation;
    private int screenVisibility;
    private int closeViewVisibility = View.GONE;

    private final FetchPropertiesHandler.FetchPropertyCallback expandPropertiesCallback = new FetchPropertiesHandler.FetchPropertyCallback() {
        @Override
        public void onResult(String propertyJson) {
            handleExpandPropertiesResult(propertyJson);
        }

        @Override
        public void onError(Throwable throwable) {
            LogUtil.error(TAG, "ExpandProperties failed: " + Log.getStackTraceString(throwable));
        }
    };
    private DialogEventListener listener;

    public AdBaseDialog(
        Context context,
        InterstitialManager interstitialManager
    ) {
        super(context, R.style.FullScreenDialogTheme);
        contextReference = new WeakReference<>(context);
        this.interstitialManager = interstitialManager;

        setOnShowListener(new OnDialogShowListener(this));
    }

    public AdBaseDialog(
        Context context,
        WebViewBase webViewBaseLocal,
        InterstitialManager interstitialManager
    ) {
        super(context, R.style.FullScreenDialogTheme);

        contextReference = new WeakReference<>(context);
        webViewBase = webViewBaseLocal;
        this.interstitialManager = interstitialManager;
        jsExecutor = webViewBaseLocal.getMRAIDInterface().getJsExecutor();

        setOnShowListener(new OnDialogShowListener(this));

        setOnKeyListener((dialog, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (webViewBase.isMRAID()) {
                    handleCloseClick();
                }
                return true;
            }
            return false;
        });
    }

    public void setDialogListener(DialogEventListener listener) {
        this.listener = listener;
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {

        super.onWindowFocusChanged(hasWindowFocus);
        int visibility = (!hasWindowFocus ? View.INVISIBLE : View.VISIBLE);
        if (Utils.hasScreenVisibilityChanged(screenVisibility, visibility)) {
            //visibility has changed. Send the changed value for mraid update for interstitials
            screenVisibility = visibility;
            if (jsExecutor != null) {
                jsExecutor.executeOnViewableChange(Utils.isScreenVisible(screenVisibility));
            }
        }
    }

    @Override
    public void cancel() {
        super.cancel();
        if (listener != null) {
            listener.onEvent(DialogEventListener.EventType.CLOSED);
        }
    }

    @VisibleForTesting
    void setCloseView(View closeView) {
        this.closeView = closeView;
    }

    public View getDisplayView() {
        return displayView;
    }

    public void setDisplayView(View displayView) {
        this.displayView = displayView;
    }

    /**
     * Changes {@link #closeView} visibility. if {@link #closeView} is not defined - visibility is remembered
     * and applied when closeView is added to container
     */
    public void changeCloseViewVisibility(int visibility) {
        if (closeView != null) {
            closeView.setVisibility(visibility);
            return;
        }

        closeViewVisibility = visibility;
    }

    public void handleSetOrientationProperties() throws AdException {
        initOrientationProperties();
        applyOrientation();
        if (webViewBase.isMRAID()) {
            webViewBase.getMRAIDInterface().updateScreenMetricsAsync(null);
        }
    }

    public void cleanup() {
        try {
            orientationBroadcastReceiver.unregister();
        }
        catch (IllegalArgumentException e) {

            LogUtil.error(TAG, Log.getStackTraceString(e));
        }
        cancel();
    }

    /**
     * Is executed when the close button is clicked. It is child job to handle the click
     */
    protected abstract void handleCloseClick();

    protected abstract void handleDialogShow();

    protected void preInit() {

        Activity activity = getActivity();
        if (activity != null) {
            initialOrientation = getActivity().getRequestedOrientation();
        }

        RelativeLayout.LayoutParams params =
            new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                                            LayoutParams.MATCH_PARENT);

        params.addRule(RelativeLayout.CENTER_IN_PARENT);

        webViewBase.setLayoutParams(params);

        if (webViewBase.isMRAID()) {
            MraidContinue();
        } else {
            init();
        }

        //Remove the current parent of webViewBase(that's a default container, in all cases)
        //java.lang.IllegalStateException: The specified child already has a parent. You must call removeView() on the child's parent first.??
        Views.removeFromParent(webViewBase);

        if (adViewContainer == null) {
            adViewContainer = new FrameLayout(getContext());
            adViewContainer.setLayoutParams(new FrameLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT
            ));
        }
        adViewContainer.addView(webViewBase, adViewContainer.getChildCount());
    }

    protected void init() {
        if (webViewBase.isMRAID()) {

            try {
                applyOrientation();
            } catch (AdException e) {
                LogUtil.error(TAG, Log.getStackTraceString(e));
            }
            //Register orientation change listener for MRAID ads only
            if (contextReference.get() != null) {
                orientationBroadcastReceiver.register(contextReference.get());
            }
        }
        webViewBase.setVisibility(View.VISIBLE);

        // render default close btn
        //good only if it is from expanded ad. Interstitial crash with null displayproperties. check.
        changeCloseViewVisibility(View.VISIBLE);

        webViewBase.requestLayout();

        if (jsExecutor != null) {
            jsExecutor.executeOnViewableChange(true);
        }
    }

    protected void MraidContinue() {
        if (!hasExpandProperties) {
            //1st always false. so this always happens 1st.
            loadExpandProperties();
        } else {
            init();
        }
    }

    @VisibleForTesting
    void loadExpandProperties() {
        /*
         * If it's MRAID, we have to check the Ad designer's request to launch
         * the ad in a particular expanded size by checking the ad's
         * ExpandProperties per the MRAID spec. So we go to the js and extract these
         * properties and then the layout gets built based on these things.
         */
        if (jsExecutor != null) {
            jsExecutor.executeGetExpandProperties(new FetchPropertiesHandler(expandPropertiesCallback));
        }
    }

    protected void lockOrientation() {
        Activity activity = getActivity();
        if (activity == null) {
            LogUtil.error(TAG, "lockOrientation failure. Activity is null");
            return;
        }
        Display getOrient = activity.getWindowManager().getDefaultDisplay();
        if (getOrient.getWidth() <= getOrient.getHeight()) {
            lockOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }
        else {
            lockOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }
    }

    protected void lockOrientation(final int screenOrientation) {
        Activity activity = getActivity();
        if (activity == null) {
            LogUtil.error(TAG, "lockOrientation failure. Activity is null");
            return;
        }

        if (originalActivityOrientation == null) {
            originalActivityOrientation = activity.getRequestedOrientation();
        }

        activity.setRequestedOrientation(screenOrientation);
    }

    protected void unApplyOrientation() {

        if (getActivity() != null && originalActivityOrientation != null) {
            getActivity().setRequestedOrientation(originalActivityOrientation);
        }
        originalActivityOrientation = null;
    }

    protected Activity getActivity() {
        try {
            return (Activity) contextReference.get();
        }
        catch (Exception e) {
            LogUtil.error(TAG, "Context is not an activity");
            return null;
        }
    }

    protected void addCloseView() {
        if (adViewContainer == null || interstitialManager == null) {
            LogUtil.error(TAG, "Unable to add close button. Container is null");
            return;
        }

        InterstitialDisplayPropertiesInternal properties = interstitialManager.getInterstitialDisplayProperties();
        closeView = Utils.createCloseView(contextReference.get(), properties);


        if (closeView == null) {
            LogUtil.error(TAG, "Unable to add close button. Close view is null");
            return;
        }

        closeView.setVisibility(closeViewVisibility);

        Views.removeFromParent(closeView);
        adViewContainer.addView(closeView);
        closeView.setOnClickListener(v -> handleCloseClick());
    }

    protected void addSkipView() {
        if (adViewContainer == null) {
            LogUtil.error(TAG, "Unable to add close button. Container is null");
            return;
        }

        InterstitialDisplayPropertiesInternal properties = interstitialManager.getInterstitialDisplayProperties();
        skipView = Utils.createSkipView(contextReference.get(), properties);

        if (skipView == null) {
            LogUtil.error(TAG, "Unable to add skip button. Skip view is null");
            return;
        }

        skipView.setVisibility(View.GONE);

        Views.removeFromParent(skipView);
        adViewContainer.addView(skipView);
        skipView.setOnClickListener(v -> handleCloseClick());
    }

    protected void addSoundView(boolean isMutedOnStart) {
        if (adViewContainer == null) {
            LogUtil.error(TAG, "Unable to add sound button. Container is null");
            return;
        }

        soundView = createSoundView(contextReference.get());

        if (soundView == null || !(soundView instanceof ImageView)) {
            LogUtil.error(TAG, "Unable to add sound button. Sound view is null");
            return;
        }

        soundView.setVisibility(View.VISIBLE);

        if (isMutedOnStart) {
            ImageView img = (ImageView) soundView;
            img.setImageResource(R.drawable.ic_volume_on);
            img.setTag("on");
        }

        Views.removeFromParent(soundView);
        adViewContainer.addView(soundView);
        soundView.setOnClickListener(view -> {
            if (listener != null) {
                ImageView img = (ImageView) view;
                String tag = (String) img.getTag();
                if (tag.equals("off")) {
                    listener.onEvent(DialogEventListener.EventType.MUTE);
                    img.setImageResource(R.drawable.ic_volume_on);
                    img.setTag("on");
                } else {
                    listener.onEvent(DialogEventListener.EventType.UNMUTE);
                    img.setImageResource(R.drawable.ic_volume_off);
                    img.setTag("off");
                }
            }
        });
    }

    protected View createSoundView(Context context) {
        return Utils.createSoundView(context);
    }

    private void applyOrientation() throws AdException {
        DeviceInfoManager deviceManager = ManagersResolver.getInstance().getDeviceManager();
        if (forceOrientation == OrientationManager.ForcedOrientation.none) {
            if (allowOrientationChange) {
                // If screen orientation can be changed, an orientation of NONE means that any
                // orientation lock should be removed
                unApplyOrientation();
            } else {
                if (getActivity() == null) {
                    throw new AdException(
                            AdException.INTERNAL_ERROR,
                            "Unable to set MRAID expand orientation to " + "'none'; expected passed in Activity Context."
                    );
                }

                // If screen orientation cannot be changed and we can obtain the current
                // screen orientation, locking it to the current orientation is a best effort

                int orientation = deviceManager.getDeviceOrientation();
                lockOrientation(orientation);
            }
        }
        else {
            // Otherwise, we have a valid, non-NONE orientation. Lock the screen based on this value
            lockOrientation(forceOrientation.getActivityInfoOrientation());
        }
    }

    private void handleExpandPropertiesResult(String expandProperties) {
        if (webViewBase == null || webViewBase.getMRAIDInterface() == null) {
            LogUtil.debug(TAG, "handleExpandPropertiesResult: WebViewBase or MraidInterface is null. Skipping.");
            return;
        }

        final MraidVariableContainer mraidVariableContainer = webViewBase.getMRAIDInterface()
                                                                         .getMraidVariableContainer();

        mraidVariableContainer.setExpandProperties(expandProperties);

        // Fill interstitial manager with expand properties.
        displayView = webViewBase;
        hasExpandProperties = true;
        MraidContinue();
    }

    private void initOrientationProperties() {
        final MraidVariableContainer mraidVariableContainer = webViewBase.getMRAIDInterface()
                                                                         .getMraidVariableContainer();

        JSONObject orientationProperties;
        //IMP : must  be true by default cos this is used if expand(url) is called as at line#if (!webViewBase.getMRAIDInterface().isLaunchWithURL()) check
        boolean allowOrientationChange = true;
        String forceOrientation = "none";

        try {
            orientationProperties = new JSONObject(mraidVariableContainer.getOrientationProperties());
            allowOrientationChange = orientationProperties.optBoolean("allowOrientationChange", true);
            forceOrientation = orientationProperties.optString("forceOrientation", "none");
        }
        catch (Exception e) {
            LogUtil.error(TAG, "Failed to get the orientation details from JSON for MRAID: " + Log.getStackTraceString(e));
        }

        if (!mraidVariableContainer.isLaunchedWithUrl()) {
            this.allowOrientationChange = allowOrientationChange;
            this.forceOrientation = OrientationManager.ForcedOrientation.valueOf(forceOrientation);
        }
    }

    private static class OnDialogShowListener implements OnShowListener {

        private final WeakReference<AdBaseDialog> weakAdBaseDialog;

        OnDialogShowListener(AdBaseDialog adBaseDialog) {
            weakAdBaseDialog = new WeakReference<>(adBaseDialog);
        }

        @Override
        public void onShow(DialogInterface dialog) {
            AdBaseDialog adBaseDialog = weakAdBaseDialog.get();
            if (adBaseDialog == null) {
                LogUtil.debug(TAG, "onShown(): Error notifying show listeners. AdBaseDialog is null.");
                return;
            }
            adBaseDialog.handleDialogShow();
            adBaseDialog.addCloseView();

            InterstitialDisplayPropertiesInternal properties = adBaseDialog.interstitialManager.getInterstitialDisplayProperties();
            if (properties.isSoundButtonVisible && (adBaseDialog instanceof InterstitialVideo)) {
                adBaseDialog.addSoundView(properties.isMuted);
            }

            if (adBaseDialog instanceof InterstitialVideo) {
                adBaseDialog.addSkipView();
            }

            adBaseDialog.interstitialManager.interstitialDialogShown(adBaseDialog.adViewContainer);
            final DialogEventListener listener = adBaseDialog.listener;

            if (listener != null) {
                listener.onEvent(DialogEventListener.EventType.SHOWN);
            }
        }
    }
}
