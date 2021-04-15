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
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import org.json.JSONObject;
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.models.internal.MraidVariableContainer;
import org.prebid.mobile.rendering.mraid.handler.FetchPropertiesHandler;
import org.prebid.mobile.rendering.mraid.methods.others.OrientationManager;
import org.prebid.mobile.rendering.sdk.ManagersResolver;
import org.prebid.mobile.rendering.sdk.deviceData.managers.DeviceInfoManager;
import org.prebid.mobile.rendering.utils.broadcast.OrientationBroadcastReceiver;
import org.prebid.mobile.rendering.utils.helpers.Utils;
import org.prebid.mobile.rendering.utils.logger.OXLog;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;
import org.prebid.mobile.rendering.views.webview.WebViewBase;
import org.prebid.mobile.rendering.views.webview.mraid.JsExecutor;
import org.prebid.mobile.rendering.views.webview.mraid.Views;

import java.lang.ref.WeakReference;

//Class to show ad as an interstitial i.e, a fullscreen ad
public abstract class AdBaseDialog extends Dialog {
    private static final String TAG = AdBaseDialog.class.getSimpleName();

    private final WeakReference<Context> mContextReference;
    private final OrientationBroadcastReceiver mOrientationBroadcastReceiver =
        new OrientationBroadcastReceiver();

    protected JsExecutor mJsExecutor;
    protected InterstitialManager mInterstitialManager;

    protected WebViewBase mWebViewBase;
    protected FrameLayout mAdViewContainer;
    protected View mDisplayView;
    protected View mAdIndicatorView;
    private View mCloseView;

    // IMP: shud be always none. cos this val is used when expand is called with an url.
    protected OrientationManager.ForcedOrientation mForceOrientation = OrientationManager.ForcedOrientation.none;
    @Nullable private Integer mOriginalActivityOrientation;
    /**
     * Is used when deciding to handle orientation changes. Orientation changes allowed (true) by default.
     */
    protected boolean mAllowOrientationChange = true;
    protected boolean mHasExpandProperties;

    protected int mInitialOrientation;
    private int mScreenVisibility;
    private int mCloseViewVisibility = View.GONE;

    private final FetchPropertiesHandler.FetchPropertyCallback mExpandPropertiesCallback = new FetchPropertiesHandler.FetchPropertyCallback() {
        @Override
        public void onResult(String propertyJson) {
            handleExpandPropertiesResult(propertyJson);
        }

        @Override
        public void onError(Throwable throwable) {
            OXLog.error(TAG, "ExpandProperties failed: " + Log.getStackTraceString(throwable));
        }
    };
    private DialogEventListener mListener;

    public AdBaseDialog(Context context, int theme, InterstitialManager interstitialManager) {
        super(context, theme);
        mContextReference = new WeakReference<>(context);
        mInterstitialManager = interstitialManager;

        setOnShowListener(new OnDialogShowListener(this));
    }

    public AdBaseDialog(Context context, WebViewBase webViewBaseLocal, InterstitialManager interstitialManager) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);

        mContextReference = new WeakReference<>(context);
        mWebViewBase = webViewBaseLocal;
        mInterstitialManager = interstitialManager;
        mJsExecutor = webViewBaseLocal.getMRAIDInterface().getJsExecutor();

        setOnShowListener(new OnDialogShowListener(this));

        setOnKeyListener((dialog, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (mWebViewBase.isMRAID()) {
                    handleCloseClick();
                }
                return true;
            }
            return false;
        });
    }

    public void setDialogListener(DialogEventListener listener) {
        mListener = listener;
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {

        super.onWindowFocusChanged(hasWindowFocus);
        int visibility = (!hasWindowFocus ? View.INVISIBLE : View.VISIBLE);
        if (Utils.hasScreenVisibilityChanged(mScreenVisibility, visibility)) {
            //visibility has changed. Send the changed value for mraid update for interstitials
            mScreenVisibility = visibility;
            if (mJsExecutor != null) {
                mJsExecutor.executeOnViewableChange(Utils.isScreenVisible(mScreenVisibility));
            }
        }
    }

    @Override
    public void cancel() {
        super.cancel();
        if (mListener != null) {
            mListener.onEvent(DialogEventListener.EventType.CLOSED);
        }
    }

    @VisibleForTesting
    void setCloseView(View closeView) {
        mCloseView = closeView;
    }

    public void setAdIndicatorView(View adIndicatorView) {
        mAdIndicatorView = adIndicatorView;
    }

    public View getDisplayView() {
        return mDisplayView;
    }

    public void setDisplayView(View displayView) {
        mDisplayView = displayView;
    }

    /**
     * Changes {@link #mCloseView} visibility. if {@link #mCloseView} is not defined - visibility is remembered
     * and applied when closeView is added to container
     */
    public void changeCloseViewVisibility(int visibility) {
        if (mCloseView != null) {
            mCloseView.setVisibility(visibility);
            return;
        }

        mCloseViewVisibility = visibility;
    }

    public void handleSetOrientationProperties() throws AdException {
        initOrientationProperties();
        applyOrientation();
        if (mWebViewBase.isMRAID()) {
            mWebViewBase.getMRAIDInterface().updateScreenMetricsAsync(null);
        }
    }

    public void cleanup() {
        try {
            mOrientationBroadcastReceiver.unregister();
        }
        catch (IllegalArgumentException e) {

            OXLog.error(TAG, Log.getStackTraceString(e));
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
            mInitialOrientation = getActivity().getRequestedOrientation();
        }

        RelativeLayout.LayoutParams params =
            new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                                            LayoutParams.MATCH_PARENT);

        params.addRule(RelativeLayout.CENTER_IN_PARENT);

        mWebViewBase.setLayoutParams(params);

        if (mWebViewBase.isMRAID()) {
            MraidContinue();
        }
        else {
            init();
        }

        //Remove the current parent of webViewBase(that's a default container, in all cases)
        //java.lang.IllegalStateException: The specified child already has a parent. You must call removeView() on the child's parent first.??
        Views.removeFromParent(mWebViewBase);

        if (mAdViewContainer == null) {
            mAdViewContainer = new FrameLayout(getContext());
            mAdViewContainer.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        }
        mAdViewContainer.addView(mWebViewBase, mAdViewContainer.getChildCount());
    }

    protected void init() {
        if (mWebViewBase.isMRAID()) {

            try {
                applyOrientation();
            }
            catch (AdException e) {
                OXLog.error(TAG, Log.getStackTraceString(e));
            }
            //Register orientation change listener for MRAID ads only
            if (mContextReference.get() != null) {
                mOrientationBroadcastReceiver.register(mContextReference.get());
            }
        }
        mWebViewBase.setVisibility(View.VISIBLE);

        // render default close btn
        //good only if it is from expanded ad. Interstitial crash with null displayproperties. check.
        changeCloseViewVisibility(View.VISIBLE);

        mWebViewBase.requestLayout();

        if (mJsExecutor != null) {
            mJsExecutor.executeOnViewableChange(true);
        }
    }

    protected void MraidContinue() {
        if (!mHasExpandProperties) {
            //1st always false. so this always happens 1st.
            loadExpandProperties();
        }
        else {
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
        if (mJsExecutor != null) {
            mJsExecutor.executeGetExpandProperties(new FetchPropertiesHandler(mExpandPropertiesCallback));
        }
    }

    protected void lockOrientation() {
        Activity activity = getActivity();
        if (activity == null) {
            OXLog.error(TAG, "lockOrientation failure. Activity is null");
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
            OXLog.error(TAG, "lockOrientation failure. Activity is null");
            return;
        }

        if (mOriginalActivityOrientation == null) {
            mOriginalActivityOrientation = activity.getRequestedOrientation();
        }

        activity.setRequestedOrientation(screenOrientation);
    }

    protected void unApplyOrientation() {

        if (getActivity() != null && mOriginalActivityOrientation != null) {
            getActivity().setRequestedOrientation(mOriginalActivityOrientation);
        }
        mOriginalActivityOrientation = null;
    }

    protected Activity getActivity() {
        try {
            return (Activity) mContextReference.get();
        }
        catch (Exception e) {
            OXLog.error(TAG, "Context is not an activity");
            return null;
        }
    }

    protected void addCloseView() {
        if (mAdViewContainer == null) {
            OXLog.error(TAG, "Unable to add close button. Container is null");
            return;
        }

        mCloseView = Utils.createCloseView(mContextReference.get());

        if (mCloseView == null) {
            OXLog.error(TAG, "Unable to add close button. Close view is null");
            return;
        }

        mCloseView.setVisibility(mCloseViewVisibility);

        Views.removeFromParent(mCloseView);
        mAdViewContainer.addView(mCloseView);
        mCloseView.setOnClickListener(v -> handleCloseClick());
    }

    private void applyOrientation() throws AdException {
        DeviceInfoManager deviceManager = ManagersResolver.getInstance().getDeviceManager();
        if (mForceOrientation == OrientationManager.ForcedOrientation.none) {
            if (mAllowOrientationChange) {
                // If screen orientation can be changed, an orientation of NONE means that any
                // orientation lock should be removed
                unApplyOrientation();
            }
            else {
                if (getActivity() == null) {
                    throw new AdException(AdException.INTERNAL_ERROR, "Unable to set MRAID expand orientation to " + "'none'; expected passed in Activity Context.");
                }

                // If screen orientation cannot be changed and we can obtain the current
                // screen orientation, locking it to the current orientation is a best effort

                int orientation = deviceManager.getDeviceOrientation();
                lockOrientation(orientation);
            }
        }
        else {
            // Otherwise, we have a valid, non-NONE orientation. Lock the screen based on this value
            lockOrientation(mForceOrientation.getActivityInfoOrientation());
        }
    }

    private void handleExpandPropertiesResult(String expandProperties) {
        if (mWebViewBase == null || mWebViewBase.getMRAIDInterface() == null) {
            OXLog.debug(TAG, "handleExpandPropertiesResult: WebViewBase or MraidInterface is null. Skipping.");
            return;
        }

        final MraidVariableContainer mraidVariableContainer = mWebViewBase.getMRAIDInterface()
                                                                          .getMraidVariableContainer();

        mraidVariableContainer.setExpandProperties(expandProperties);

        // Fill interstitial manager with expand properties.
        mDisplayView = mWebViewBase;
        mHasExpandProperties = true;
        MraidContinue();
    }

    private void initOrientationProperties() {
        final MraidVariableContainer mraidVariableContainer = mWebViewBase.getMRAIDInterface().getMraidVariableContainer();

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
            OXLog.error(TAG, "Failed to get the orientation details from JSON for MRAID: " + Log.getStackTraceString(e));
        }

        if (!mraidVariableContainer.isLaunchedWithUrl()) {
            mAllowOrientationChange = allowOrientationChange;
            mForceOrientation = OrientationManager.ForcedOrientation.valueOf(forceOrientation);
        }
    }

    private static class OnDialogShowListener implements OnShowListener {
        private final WeakReference<AdBaseDialog> mWeakAdBaseDialog;

        OnDialogShowListener(AdBaseDialog adBaseDialog) {
            mWeakAdBaseDialog = new WeakReference<>(adBaseDialog);
        }

        @Override
        public void onShow(DialogInterface dialog) {
            AdBaseDialog adBaseDialog = mWeakAdBaseDialog.get();
            if (adBaseDialog == null) {
                OXLog.debug(TAG, "onShown(): Error notifying show listeners. AdBaseDialog is null.");
                return;
            }
            adBaseDialog.handleDialogShow();
            adBaseDialog.addCloseView();
            adBaseDialog.mInterstitialManager.interstitialDialogShown(adBaseDialog.mAdViewContainer);
            final DialogEventListener listener = adBaseDialog.mListener;

            if (listener != null) {
                listener.onEvent(DialogEventListener.EventType.SHOWN);
            }
        }
    }
}
