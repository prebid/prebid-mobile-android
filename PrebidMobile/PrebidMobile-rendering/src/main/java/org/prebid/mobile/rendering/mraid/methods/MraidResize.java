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

package org.prebid.mobile.rendering.mraid.methods;

import android.content.Context;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;
import org.prebid.mobile.rendering.mraid.handler.FetchPropertiesHandler;
import org.prebid.mobile.rendering.utils.helpers.Dips;
import org.prebid.mobile.rendering.utils.helpers.Utils;
import org.prebid.mobile.rendering.utils.logger.OXLog;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;
import org.prebid.mobile.rendering.views.webview.WebViewBase;
import org.prebid.mobile.rendering.views.webview.mraid.BaseJSInterface;
import org.prebid.mobile.rendering.views.webview.mraid.JSInterface;
import org.prebid.mobile.rendering.views.webview.mraid.Views;

import java.lang.ref.WeakReference;

public class MraidResize {
    private static final String TAG = "Resize";
    private static final int GRAVITY_TOP_RIGHT = Gravity.TOP | Gravity.RIGHT;

    private final FrameLayout mSecondaryAdContainer;

    private WeakReference<Context> mContextReference;
    private WebViewBase mAdBaseView;
    private BaseJSInterface mJsInterface;
    private InterstitialManager mInterstitialManager;

    private View mCloseView;

    private MraidScreenMetrics mScreenMetrics;

    private final FetchPropertiesHandler.FetchPropertyCallback mFetchPropertyCallback = new FetchPropertiesHandler.FetchPropertyCallback() {
        @Override
        public void onResult(String propertyJson) {
            handleResizePropertiesResult(propertyJson);
        }

        @Override
        public void onError(Throwable throwable) {
            OXLog.error(TAG, "executeGetResizeProperties failed: " + Log.getStackTraceString(throwable));
        }
    };

    public MraidResize(Context context,
                       BaseJSInterface jsInterface,
                       WebViewBase adBaseView,
                       InterstitialManager interstitialManager) {
        mContextReference = new WeakReference<>(context);
        mAdBaseView = adBaseView;
        mJsInterface = jsInterface;
        mInterstitialManager = interstitialManager;
        mSecondaryAdContainer = new FrameLayout(mContextReference.get());
        mSecondaryAdContainer.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                                           ViewGroup.LayoutParams.MATCH_PARENT));
        initCloseView();
    }

    public void resize() {
        final String state = mJsInterface.getMraidVariableContainer().getCurrentState();
        if (isContainerStateInvalid(state)) {
            OXLog.debug(TAG, "resize: Skipping. Wrong container state: " + state);
            return;
        }
        else if (state.equals(JSInterface.STATE_EXPANDED)) {
            mJsInterface.onError("resize_when_expanded_error", JSInterface.ACTION_RESIZE);
            return;
        }

        mJsInterface.setDefaultLayoutParams(mAdBaseView.getLayoutParams());
        mJsInterface.getJsExecutor().executeGetResizeProperties(new FetchPropertiesHandler(mFetchPropertyCallback));
    }

    public void destroy() {
        if (mJsInterface != null) {
            Views.removeFromParent(mSecondaryAdContainer);
            Views.removeFromParent(mJsInterface.getDefaultAdContainer());
        }
    }

    private void initCloseView() {
        mCloseView = Utils.createCloseView(mContextReference.get());

        if (mCloseView == null) {
            OXLog.error(TAG, "Error initializing close view. Close view is null");
            return;
        }
        mAdBaseView.post(() -> {
            if (mCloseView instanceof ImageView) {
                ((ImageView) mCloseView).setImageResource(android.R.color.transparent);
            }
        });

        mCloseView.setOnClickListener(v -> closeView());
    }

    private void showExpandDialog(final int widthDips, final int heightDips, final int offsetXDips,
                                  final int offsetYDips,
                                  final boolean allowOffscreen) {
        mScreenMetrics = mJsInterface.getScreenMetrics();
        mAdBaseView.post((() -> {
                             try {
                                 if (mAdBaseView == null) {
                                     OXLog.error(TAG, "Resize failed. Webview is null");
                                     mJsInterface.onError("Unable to resize after webview is destroyed", JSInterface.ACTION_RESIZE);
                                     return;
                                 }
                                 Context context = mContextReference.get();
                                 if (context == null) {
                                     OXLog.error(TAG, "Resize failed. Context is null");
                                     mJsInterface.onError("Unable to resize when mContext is null", JSInterface.ACTION_RESIZE);
                                     return;
                                 }
                                 // Translate coordinates to px and get the resize rect
                                 Rect resizeRect = getResizeRect(widthDips, heightDips, offsetXDips, offsetYDips, allowOffscreen);
                                 if (resizeRect == null) {
                                     return;
                                 }

                                 // Put the ad in the closeable container and resize it
                                 FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(resizeRect.width(), resizeRect.height());
                                 layoutParams.leftMargin = resizeRect.left - mScreenMetrics.getRootViewRect().left;
                                 layoutParams.topMargin = resizeRect.top - mScreenMetrics.getRootViewRect().top;

                                 final String state = mJsInterface.getMraidVariableContainer().getCurrentState();
                                 if (JSInterface.STATE_DEFAULT.equals(state)) {
                                     handleDefaultStateResize(layoutParams);
                                 }
                                 else if (JSInterface.STATE_RESIZED.equals(state)) {
                                     mSecondaryAdContainer.setLayoutParams(layoutParams);
                                 }

                                 mJsInterface.onStateChange(JSInterface.STATE_RESIZED);
                                 mInterstitialManager.interstitialDialogShown(mSecondaryAdContainer);
                             }
                             catch (Exception e) {
                                 OXLog.error(TAG, "Resize failed: " + Log.getStackTraceString(e));
                             }
                         })
        );
    }

    private void handleDefaultStateResize(FrameLayout.LayoutParams layoutParams) {
        ViewGroup adBaseViewParent = null;

        //remove from default container and add it to webAdContainer
        if (mAdBaseView.getParent().equals(mJsInterface.getDefaultAdContainer())) {
            mJsInterface.getDefaultAdContainer().removeView(mAdBaseView);
        }
        else {
            //This should never happen though!
            //Because, if resize is called, that would mean, it's parent is always a defaultAdContainer(for banner state of the ad)
            //Hence, the previous if block should suffice.
            //But adding it to fix any crash, if above is not the case.
            adBaseViewParent = mAdBaseView.getParentContainer();
            Views.removeFromParent(mAdBaseView);
        }
        mJsInterface.getDefaultAdContainer().setVisibility(View.INVISIBLE);

        initSecondaryAdContainer();

        //Add webAdContainer to the viewgroup for later use.
        if (adBaseViewParent != null) {
            adBaseViewParent.addView(mSecondaryAdContainer, layoutParams);
        }
        else {
            ViewGroup view = mJsInterface.getRootView();
            view.addView(mSecondaryAdContainer, layoutParams);
        }
    }

    private void initSecondaryAdContainer() {
        if (mSecondaryAdContainer.getParent() != null) {
            Views.removeFromParent(mSecondaryAdContainer);
        }

        mSecondaryAdContainer.removeAllViews();
        mSecondaryAdContainer.addView(mAdBaseView,
                                      new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        mSecondaryAdContainer.addView(mCloseView);

        mSecondaryAdContainer.setFocusableInTouchMode(true);
        mSecondaryAdContainer.requestFocus();
        mSecondaryAdContainer.setOnKeyListener((view, keyCode, keyEvent) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                closeView();
                return true;
            }
            return false;
        });
    }

    private void closeView() {
        MraidClose mraidClose = new MraidClose(mAdBaseView.getContext(), mJsInterface, mAdBaseView);
        mraidClose.closeThroughJS();
        mInterstitialManager.interstitialClosed(mAdBaseView);
    }

    private Rect getResizeRect(int widthDips, int heightDips, int offsetXDips, int offsetYDips, boolean allowOffscreen) {
        Context context = mContextReference.get();
        if (context == null) {
            mJsInterface.onError("Context is null", JSInterface.ACTION_RESIZE);
            return null;
        }
        int width = Dips.dipsToIntPixels(widthDips, context);
        int height = Dips.dipsToIntPixels(heightDips, context);
        int offsetX = Dips.dipsToIntPixels(offsetXDips, context);
        int offsetY = Dips.dipsToIntPixels(offsetYDips, context);
        int left = mScreenMetrics.getDefaultAdRect().left + offsetX;
        int top = mScreenMetrics.getDefaultAdRect().top + offsetY;
        Rect resizeRect = new Rect(left, top, left + width, top + height);//new requested size
        if (!allowOffscreen) {
            // Require the entire ad to be on-screen.
            Rect bounds = mScreenMetrics.getRootViewRect();
            int rectWid = bounds.width();//max allowed size
            int rectHei = bounds.height();

            if (resizeRect.width() > rectWid || resizeRect.height() > rectHei) {
                sendError(widthDips, heightDips, offsetXDips, offsetYDips);
                mJsInterface.onError("Resize properties specified a size & offset that does not allow the ad to appear within the max allowed size", JSInterface.ACTION_RESIZE);
                return null;
            }

            // Offset the resize rect so that it displays on the screen
            int newLeft = clampInt(bounds.left, resizeRect.left, bounds.right - resizeRect.width());
            int newTop = clampInt(bounds.top, resizeRect.top, bounds.bottom - resizeRect.height());
            resizeRect.offsetTo(newLeft, newTop);
        }

        // The entire close region must always be visible.
        Rect closeRect = new Rect();

        Pair<Integer, Integer> closeViewWidthHeightPair = getCloseViewWidthHeight();
        Gravity.apply(GRAVITY_TOP_RIGHT, closeViewWidthHeightPair.first, closeViewWidthHeightPair.second, resizeRect, closeRect);
        if (!mScreenMetrics.getRootViewRect().contains(closeRect)) {
            sendError(widthDips, heightDips, offsetXDips, offsetYDips);
            mJsInterface.onError("Resize properties specified a size & offset that does not allow the close region to appear within the max allowed size", JSInterface.ACTION_RESIZE);
            return null;
        }

        if (!resizeRect.contains(closeRect)) {
            String err = "ResizeProperties specified a size ("
                         + widthDips + ", " + height + ") and offset ("
                         + offsetXDips + ", " + offsetYDips + ") that don't allow the close region to appear "
                         + "within the resized ad.";
            OXLog.error(TAG, err);
            mJsInterface.onError("Resize properties specified a size & offset that does not allow the close region to appear within the resized ad", JSInterface.ACTION_RESIZE);
            return null;
        }

        return resizeRect;
    }

    private Pair<Integer, Integer> getCloseViewWidthHeight() {
        if (mCloseView == null) {
            OXLog.error(TAG, "Unable to retrieve width height from close view. Close view is null.");
            return new Pair<>(0, 0);
        }

        return new Pair<>(mCloseView.getWidth(), mCloseView.getHeight());
    }

    private void handleResizePropertiesResult(String propertyJson) {
        JSONObject resizeProperties;
        int twidth = 0;
        int theight = 0;
        int offsetX = 0;
        int offsetY = 0;
        boolean allowOffscreen = true;

        try {
            resizeProperties = new JSONObject(propertyJson);

            twidth = resizeProperties.optInt(JSInterface.JSON_WIDTH, 0);
            theight = resizeProperties.optInt(JSInterface.JSON_HEIGHT, 0);

            offsetX = resizeProperties.optInt("offsetX", 0);
            offsetY = resizeProperties.optInt("offsetY", 0);
            allowOffscreen = resizeProperties.optBoolean("allowOffscreen", true);
        }
        catch (JSONException e) {
            OXLog.error(TAG, "Failed to get resize values from JSON for MRAID: " + Log.getStackTraceString(e));
        }

        OXLog.debug(TAG, "resize: x, y, width, height: " + offsetX + " " + offsetY + " " + twidth + " " + theight);

        showExpandDialog(twidth, theight, offsetX, offsetY, allowOffscreen);
    }

    private void sendError(int widthDips, int heightDips, int offsetXDips, int offsetYDips) {
        String err = "Resize properties specified a size: " + widthDips
                     + " , " + heightDips + ") and offset ("
                     + offsetXDips + ", " + offsetYDips + ") that doesn't allow the close"
                     + " region to appear within the max allowed size ("
                     + mScreenMetrics.getRootViewRectDips().width() + ", "
                     + mScreenMetrics.getRootViewRectDips().height() + ")";
        OXLog.error(TAG, err);
    }

    private boolean isContainerStateInvalid(String state) {
        return TextUtils.isEmpty(state)
               || state.equals(JSInterface.STATE_LOADING)
               || state.equals(JSInterface.STATE_HIDDEN);
    }

    private int clampInt(int min, int target, int max) {
        return Math.max(min, Math.min(target, max));
    }
}
