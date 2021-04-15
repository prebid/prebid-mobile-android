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

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import org.prebid.mobile.rendering.models.internal.MraidVariableContainer;
import org.prebid.mobile.rendering.utils.exposure.ViewExposure;
import org.prebid.mobile.rendering.utils.helpers.HandlerQueueManager;
import org.prebid.mobile.rendering.utils.logger.OXLog;
import org.prebid.mobile.rendering.views.webview.WebViewBase;

import java.lang.ref.WeakReference;
import java.util.Locale;

public class JsExecutor {
    private static final String TAG = JsExecutor.class.getSimpleName();

    private final HandlerQueueManager mHandlerQueueManager;
    private final WebView mWebView;
    private final Handler mScriptExecutionHandler;

    private MraidVariableContainer mMraidVariableContainer;

    public JsExecutor(WebView webView, Handler scriptExecutionHandler, HandlerQueueManager handlerQueueManager) {
        mWebView = webView;
        mHandlerQueueManager = handlerQueueManager;
        mScriptExecutionHandler = scriptExecutionHandler;
    }

    public void setMraidVariableContainer(@NonNull MraidVariableContainer mraidVariableContainer) {
        mMraidVariableContainer = mraidVariableContainer;
    }

    public HandlerQueueManager getHandlerQueueManager() {
        return mHandlerQueueManager;
    }

    public void executeGetResizeProperties(Handler handler) {
        evaluateJavaScriptMethodWithResult("getResizeProperties", handler);
    }

    public void executeGetExpandProperties(Handler handler) {
        evaluateJavaScriptMethodWithResult("getExpandProperties", handler);
    }

    public void executeSetScreenSize(Rect screenSize) {
        evaluateJavaScript(String.format(Locale.US, "mraid.setScreenSize(%d, %d);",
                                         screenSize.width(), screenSize.height()));
    }

    public void executeSetMaxSize(Rect maxSize) {
        evaluateJavaScript(String.format(Locale.US, "mraid.setMaxSize(%d, %d);",
                                         maxSize.width(), maxSize.height()));
    }

    public void executeSetCurrentPosition(Rect currentPosition) {
        evaluateJavaScript(String.format(Locale.US, "mraid.setCurrentPosition(%d, %d, %d, %d);",
                                         currentPosition.left, currentPosition.top,
                                         currentPosition.width(), currentPosition.height()));
    }

    public void executeSetDefaultPosition(Rect defaultPosition) {
        evaluateJavaScript(String.format(Locale.US, "mraid.setDefaultPosition(%d, %d, %d, %d);",
                                         defaultPosition.left, defaultPosition.top,
                                         defaultPosition.width(), defaultPosition.height()));
    }

    public void executeOnSizeChange(Rect rect) {
        evaluateJavaScript(String.format(Locale.US, "mraid.onSizeChange(%d, %d);",
                                         rect.width(), rect.height()));
    }

    public void executeOnError(String message, String action) {
        evaluateJavaScript(String.format("mraid.onError('%1$s', '%2$s');", message, action));
    }

    public void executeDisabledFlags(String disabledFlags) {
        evaluateMraidScript(disabledFlags);
    }

    public void executeOnReadyExpanded() {
        mMraidVariableContainer.setCurrentState(JSInterface.STATE_EXPANDED);
        evaluateMraidScript("mraid.onReadyExpanded();");
    }

    public void executeOnReady() {
        mMraidVariableContainer.setCurrentState(JSInterface.STATE_DEFAULT);
        evaluateMraidScript("mraid.onReady();");
    }

    public void executeAudioVolumeChange(Float volume) {
        evaluateMraidScript("mraid.onAudioVolumeChange(" + volume + ");");
    }

    public void executeStateChange(String state) {
        if (!TextUtils.equals(state, mMraidVariableContainer.getCurrentState())) {
            mMraidVariableContainer.setCurrentState(state);
            evaluateMraidScript(String.format("mraid.onStateChange('%1$s');", state));
        }
    }

    /**
     * Deprecated since SDK v4.12.0 (since MRAID 3 implementation)
     */
    @Deprecated
    public void executeOnViewableChange(boolean isViewable) {
        final Boolean currentViewable = mMraidVariableContainer.getCurrentViewable();
        if (currentViewable == null || currentViewable != isViewable) {
            mMraidVariableContainer.setCurrentViewable(isViewable);

            evaluateJavaScript(String.format("mraid.onViewableChange(%1$b);", isViewable));
        }
    }

    public void executeExposureChange(ViewExposure viewExposure) {
        String exposureChangeString = viewExposure != null ? viewExposure.toString() : null;

        if (!TextUtils.equals(exposureChangeString, mMraidVariableContainer.getCurrentExposure())) {
            evaluateMraidScript(String.format("mraid.onExposureChange('%1$s');", exposureChangeString));
            mMraidVariableContainer.setCurrentExposure(exposureChangeString);
        }
    }

    public void executeNativeCallComplete() {
        evaluateMraidScript("mraid.nativeCallComplete();");
    }

    public void loading() {
        mMraidVariableContainer.setCurrentState(JSInterface.STATE_LOADING);
    }

    @VisibleForTesting
    String getCurrentState() {
        return mMraidVariableContainer.getCurrentState();
    }

    @VisibleForTesting
    void evaluateJavaScript(final String script) {
        if (mWebView == null) {
            OXLog.debug(TAG, "evaluateJavaScript failure. mWebView is null");
            return;
        }

        OXLog.debug(TAG, "evaluateJavaScript: " + script);
        try {
            String scriptToEvaluate = "javascript: if (window.mraid && (window.mraid.getState() != 'loading' ) && ( window.mraid.getState() != 'hidden') ) { " + script + " }";
            mScriptExecutionHandler.post(new EvaluateScriptRunnable(mWebView, scriptToEvaluate));
        }
        catch (Exception e) {
            OXLog.error(TAG, "evaluateJavaScript failed for script " + script + Log.getStackTraceString(e));
        }
    }

    @VisibleForTesting
    void evaluateJavaScriptMethodWithResult(String method, Handler handler) {
        if (mWebView instanceof WebViewBase && ((WebViewBase) mWebView).isMRAID()) {
            String handlerHash = mHandlerQueueManager.queueHandler(handler);
            if (handlerHash != null) {
                evaluateJavaScript("jsBridge.javaScriptCallback('"
                                   + handlerHash + "', '"
                                   + method + "', (function() { var retVal = mraid."
                                   + method + "(); if (typeof retVal === 'object') { retVal = JSON.stringify(retVal); } return retVal; })())");
            }
        }
        else if (handler != null) {
            Message responseMessage = new Message();
            Bundle bundle = new Bundle();
            bundle.putString(JSInterface.JSON_METHOD, method);
            bundle.putString(JSInterface.JSON_VALUE, "");
            responseMessage.setData(bundle);
            handler.dispatchMessage(responseMessage);
        }
    }

    @VisibleForTesting
    void evaluateMraidScript(final String script) {
        if (mWebView == null) {
            OXLog.debug(TAG, "evaluateMraidScript failure. mWebView is null");
            return;
        }

        try {
            String scriptToEvaluate = "javascript: if (window.mraid  ) { " + script + " }";
            mScriptExecutionHandler.post(new EvaluateScriptRunnable(mWebView, scriptToEvaluate));
        }
        catch (Exception e) {
            OXLog.error(TAG, "evaluateMraidScript failed: " + Log.getStackTraceString(e));
        }
    }

    @VisibleForTesting
    static class EvaluateScriptRunnable implements Runnable {
        private static final String TAG = EvaluateScriptRunnable.class.getSimpleName();

        private final WeakReference<WebView> mWeakAdView;
        private final String mScript;

        EvaluateScriptRunnable(WebView webViewBase, String script) {
            mWeakAdView = new WeakReference<>(webViewBase);
            mScript = script;
        }

        @Override
        public void run() {
            WebView webView = mWeakAdView.get();
            if (webView == null) {
                OXLog.error(TAG, "Failed to evaluate script. WebView is null");
                return;
            }

            webView.loadUrl(mScript);
        }
    }
}
