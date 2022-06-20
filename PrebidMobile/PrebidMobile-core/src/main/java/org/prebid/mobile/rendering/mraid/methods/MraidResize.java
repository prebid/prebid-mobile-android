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
import androidx.annotation.DrawableRes;
import org.json.JSONException;
import org.json.JSONObject;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.core.R;
import org.prebid.mobile.rendering.mraid.handler.FetchPropertiesHandler;
import org.prebid.mobile.rendering.utils.helpers.Dips;
import org.prebid.mobile.rendering.utils.helpers.Utils;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;
import org.prebid.mobile.rendering.views.webview.WebViewBase;
import org.prebid.mobile.rendering.views.webview.mraid.BaseJSInterface;
import org.prebid.mobile.rendering.views.webview.mraid.JSInterface;
import org.prebid.mobile.rendering.views.webview.mraid.Views;

import java.lang.ref.WeakReference;

public class MraidResize {

    private static final String TAG = "Resize";
    private static final int GRAVITY_TOP_RIGHT = Gravity.TOP | Gravity.RIGHT;

    private final FrameLayout secondaryAdContainer;

    private WeakReference<Context> contextReference;
    private WebViewBase adBaseView;
    private BaseJSInterface jsInterface;
    private InterstitialManager interstitialManager;

    private View closeView;

    private MraidScreenMetrics screenMetrics;

    private final FetchPropertiesHandler.FetchPropertyCallback fetchPropertyCallback = new FetchPropertiesHandler.FetchPropertyCallback() {
        @Override
        public void onResult(String propertyJson) {
            handleResizePropertiesResult(propertyJson);
        }

        @Override
        public void onError(Throwable throwable) {
            LogUtil.error(TAG, "executeGetResizeProperties failed: " + Log.getStackTraceString(throwable));
        }
    };

    public MraidResize(Context context,
                       BaseJSInterface jsInterface,
                       WebViewBase adBaseView,
                       InterstitialManager interstitialManager) {
        contextReference = new WeakReference<>(context);
        this.adBaseView = adBaseView;
        this.jsInterface = jsInterface;
        this.interstitialManager = interstitialManager;
        secondaryAdContainer = new FrameLayout(contextReference.get());
        secondaryAdContainer.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        initCloseView();
    }

    public void resize() {
        final String state = jsInterface.getMraidVariableContainer().getCurrentState();
        if (isContainerStateInvalid(state)) {
            LogUtil.debug(TAG, "resize: Skipping. Wrong container state: " + state);
            return;
        } else if (state.equals(JSInterface.STATE_EXPANDED)) {
            jsInterface.onError("resize_when_expanded_error", JSInterface.ACTION_RESIZE);
            return;
        }

        jsInterface.setDefaultLayoutParams(adBaseView.getLayoutParams());
        jsInterface.getJsExecutor().executeGetResizeProperties(new FetchPropertiesHandler(fetchPropertyCallback));
    }

    public void destroy() {
        if (jsInterface != null) {
            Views.removeFromParent(secondaryAdContainer);
            Views.removeFromParent(jsInterface.getDefaultAdContainer());
        }
    }

    private void initCloseView() {
        closeView = Utils.createCloseView(contextReference.get());

        if (closeView == null) {
            LogUtil.error(TAG, "Error initializing close view. Close view is null");
            return;
        }
        adBaseView.post(() -> {
            if (closeView instanceof ImageView) {
                ((ImageView) closeView).setImageResource(android.R.color.transparent);
            }
        });

        closeView.setOnClickListener(v -> closeView());
    }

    private void showExpandDialog(final int widthDips, final int heightDips, final int offsetXDips,
                                  final int offsetYDips,
                                  final boolean allowOffscreen) {
        screenMetrics = jsInterface.getScreenMetrics();
        adBaseView.post((() -> {
            try {
                if (adBaseView == null) {
                    LogUtil.error(TAG, "Resize failed. Webview is null");
                    jsInterface.onError("Unable to resize after webview is destroyed", JSInterface.ACTION_RESIZE);
                    return;
                }
                Context context = contextReference.get();
                if (context == null) {
                    LogUtil.error(TAG, "Resize failed. Context is null");
                    jsInterface.onError("Unable to resize when context is null", JSInterface.ACTION_RESIZE);
                    return;
                }
                // Translate coordinates to px and get the resize rect
                Rect resizeRect = getResizeRect(widthDips, heightDips, offsetXDips, offsetYDips, allowOffscreen);
                if (resizeRect == null) {
                    return;
                }

                // Put the ad in the closeable container and resize it
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                        resizeRect.width(),
                        resizeRect.height()
                );
                layoutParams.leftMargin = resizeRect.left - screenMetrics.getRootViewRect().left;
                layoutParams.topMargin = resizeRect.top - screenMetrics.getRootViewRect().top;

                final String state = jsInterface.getMraidVariableContainer().getCurrentState();
                if (JSInterface.STATE_DEFAULT.equals(state)) {
                    handleDefaultStateResize(layoutParams);
                } else if (JSInterface.STATE_RESIZED.equals(state)) {
                    secondaryAdContainer.setLayoutParams(layoutParams);
                }

                jsInterface.onStateChange(JSInterface.STATE_RESIZED);
                interstitialManager.interstitialDialogShown(secondaryAdContainer);
            }
                             catch (Exception e) {
                                 LogUtil.error(TAG, "Resize failed: " + Log.getStackTraceString(e));
                             }
                         })
        );
    }

    private void handleDefaultStateResize(FrameLayout.LayoutParams layoutParams) {
        ViewGroup adBaseViewParent = null;

        //remove from default container and add it to webAdContainer
        if (adBaseView.getParent().equals(jsInterface.getDefaultAdContainer())) {
            jsInterface.getDefaultAdContainer().removeView(adBaseView);
        } else {
            //This should never happen though!
            //Because, if resize is called, that would mean, it's parent is always a defaultAdContainer(for banner state of the ad)
            //Hence, the previous if block should suffice.
            //But adding it to fix any crash, if above is not the case.
            adBaseViewParent = adBaseView.getParentContainer();
            Views.removeFromParent(adBaseView);
        }
        jsInterface.getDefaultAdContainer().setVisibility(View.INVISIBLE);

        initSecondaryAdContainer();

        //Add webAdContainer to the viewgroup for later use.
        if (adBaseViewParent != null) {
            adBaseViewParent.addView(secondaryAdContainer, layoutParams);
        }
        else {
            ViewGroup view = jsInterface.getRootView();
            view.addView(secondaryAdContainer, layoutParams);
        }
    }

    private void initSecondaryAdContainer() {
        if (secondaryAdContainer.getParent() != null) {
            Views.removeFromParent(secondaryAdContainer);
        }

        secondaryAdContainer.removeAllViews();
        secondaryAdContainer.addView(adBaseView,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                )
        );

        secondaryAdContainer.addView(closeView);

        secondaryAdContainer.setFocusableInTouchMode(true);
        secondaryAdContainer.requestFocus();
        secondaryAdContainer.setOnKeyListener((view, keyCode, keyEvent) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                closeView();
                return true;
            }
            return false;
        });
    }

    private void closeView() {
        MraidClose mraidClose = new MraidClose(adBaseView.getContext(), jsInterface, adBaseView);
        mraidClose.closeThroughJS();
        interstitialManager.interstitialClosed(adBaseView);
    }

    private Rect getResizeRect(int widthDips, int heightDips, int offsetXDips, int offsetYDips, boolean allowOffscreen) {
        Context context = contextReference.get();
        if (context == null) {
            jsInterface.onError("Context is null", JSInterface.ACTION_RESIZE);
            return null;
        }
        int width = Dips.dipsToIntPixels(widthDips, context);
        int height = Dips.dipsToIntPixels(heightDips, context);
        int offsetX = Dips.dipsToIntPixels(offsetXDips, context);
        int offsetY = Dips.dipsToIntPixels(offsetYDips, context);
        int left = screenMetrics.getDefaultAdRect().left + offsetX;
        int top = screenMetrics.getDefaultAdRect().top + offsetY;
        Rect resizeRect = new Rect(left, top, left + width, top + height);//new requested size

        if (!allowOffscreen) {
            changeCloseButtonIcon(android.R.color.transparent);

            // Require the entire ad to be on-screen.
            Rect bounds = screenMetrics.getRootViewRect();
            int maxAllowedWidth = bounds.width();
            int maxAllowedHeight = bounds.height();

            // 2 - possible offset after px to dp conversion
            if (resizeRect.width() - 2 > maxAllowedWidth || resizeRect.height() - 2 > maxAllowedHeight) {
                sendError(widthDips, heightDips, offsetXDips, offsetYDips);
                jsInterface.onError(
                    "Resize properties specified a size & offset that does not allow the ad to appear within the max allowed size",
                    JSInterface.ACTION_RESIZE
                );
                return null;
            }

            // Offset the resize rect so that it displays on the screen
            int newLeft = clampInt(bounds.left, resizeRect.left, bounds.right - resizeRect.width());
            int newTop = clampInt(bounds.top, resizeRect.top, bounds.bottom - resizeRect.height());
            resizeRect.offsetTo(newLeft, newTop);

            // The entire close region must always be visible.
            Rect closeRect = new Rect();

            Pair<Integer, Integer> closeViewWidthHeightPair = getCloseViewWidthHeight();
            Gravity.apply(GRAVITY_TOP_RIGHT, closeViewWidthHeightPair.first, closeViewWidthHeightPair.second, resizeRect, closeRect);
            if (!screenMetrics.getRootViewRect().contains(closeRect)) {
                sendError(widthDips, heightDips, offsetXDips, offsetYDips);
                jsInterface.onError(
                    "Resize properties specified a size & offset that does not allow the close region to appear within the max allowed size",
                    JSInterface.ACTION_RESIZE
                );
                return null;
            }

            if (!resizeRect.contains(closeRect)) {
                String err = "ResizeProperties specified a size ("
                    + widthDips + ", " + height + ") and offset ("
                    + offsetXDips + ", " + offsetYDips + ") that don't allow the close region to appear "
                    + "within the resized ad.";
                LogUtil.error(TAG, err);
                jsInterface.onError(
                    "Resize properties specified a size & offset that does not allow the close region to appear within the resized ad",
                    JSInterface.ACTION_RESIZE
                );
                return null;
            }
        } else {
            changeCloseButtonIcon(R.drawable.prebid_ic_close_interstitial);
            calculateMarginsToPlaceCloseButtonInScreen(resizeRect);
        }

        return resizeRect;
    }

    private Pair<Integer, Integer> getCloseViewWidthHeight() {
        if (closeView == null) {
            LogUtil.error(TAG, "Unable to retrieve width height from close view. Close view is null.");
            return new Pair<>(0, 0);
        }

        return new Pair<>(closeView.getWidth(), closeView.getHeight());
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
            LogUtil.error(TAG, "Failed to get resize values from JSON for MRAID: " + Log.getStackTraceString(e));
        }

        LogUtil.debug(TAG, "resize: x, y, width, height: " + offsetX + " " + offsetY + " " + twidth + " " + theight);

        showExpandDialog(twidth, theight, offsetX, offsetY, allowOffscreen);
    }

    private void sendError(int widthDips, int heightDips, int offsetXDips, int offsetYDips) {
        String err = "Resize properties specified a size: " + widthDips + " , " + heightDips + ") and offset (" + offsetXDips + ", " + offsetYDips + ") that doesn't allow the close" + " region to appear within the max allowed size (" + screenMetrics.getRootViewRectDips()
                                                                                                                                                                                                                                                         .width() + ", " + screenMetrics.getRootViewRectDips()
                                                                                                                                                                                                                                                                                        .height() + ")";
        LogUtil.error(TAG, err);
    }

    private boolean isContainerStateInvalid(String state) {
        return TextUtils.isEmpty(state)
            || state.equals(JSInterface.STATE_LOADING)
            || state.equals(JSInterface.STATE_HIDDEN);
    }

    private int clampInt(
        int min,
        int target,
        int max
    ) {
        return Math.max(min, Math.min(target, max));
    }

    private void setCloseButtonMargins(
        int left,
        int top,
        int right,
        int bottom
    ) {
        adBaseView.post(() -> {
            ViewGroup.LayoutParams layoutParams = closeView.getLayoutParams();
            if (layoutParams instanceof FrameLayout.LayoutParams) {
                ((FrameLayout.LayoutParams) layoutParams).setMargins(
                    left,
                    top,
                    right,
                    bottom
                );
                closeView.setLayoutParams(layoutParams);
            }
        });
    }

    private void changeCloseButtonIcon(@DrawableRes int resource) {
        adBaseView.post(() -> {
            if (closeView instanceof ImageView) {
                ((ImageView) closeView).setImageResource(resource);
            } else {
                Log.e(TAG, "Close button isn't ImageView");
            }
        });
    }

    /**
     * Calculates margins for close button to place it in screen boundaries.
     * Margins are applied only when close button is out of screen in offscreen mode.
     */
    private void calculateMarginsToPlaceCloseButtonInScreen(Rect resizeRect) {
        Rect closeRect = new Rect();
        Pair<Integer, Integer> closeViewWidthHeightPair = getCloseViewWidthHeight();
        Gravity.apply(GRAVITY_TOP_RIGHT, closeViewWidthHeightPair.first, closeViewWidthHeightPair.second, resizeRect, closeRect);
        if (!screenMetrics.getRootViewRect().contains(closeRect)) {
            Rect deviceRect = screenMetrics.getRootViewRect();

            int marginTop = 0;
            if (deviceRect.top > resizeRect.top) {
                marginTop = deviceRect.top - resizeRect.top;
            }

            int marginRight = 0;
            if (resizeRect.right > deviceRect.right) {
                marginRight = resizeRect.right - deviceRect.right;
            }

            setCloseButtonMargins(
                0,
                marginTop,
                marginRight,
                0
            );
        } else {
            setCloseButtonMargins(0, 0, 0, 0);
        }
    }

}
