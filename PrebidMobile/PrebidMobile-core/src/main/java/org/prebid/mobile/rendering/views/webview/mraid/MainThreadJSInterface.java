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

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.JavascriptInterface;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import org.prebid.mobile.LogUtil;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * The object registered as the "jsBridge" JavaScript interface.
 * <p>
 * WebView calls JavaScript interfaces on a background thread, while {@link BaseJSInterface} works
 * with views and MRAID state owned by the main thread. MRAID commands are posted to the main thread,
 * and getters wait for the main thread for a bounded time.
 */
public class MainThreadJSInterface implements JSInterface {

    private static final String TAG = MainThreadJSInterface.class.getSimpleName();

    private static final long GETTER_TIMEOUT_MS = 250;
    private static final String EMPTY_JSON = "{}";

    @NonNull private final BaseJSInterface delegate;
    @NonNull private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final long getterTimeoutMs;

    public MainThreadJSInterface(@NonNull BaseJSInterface delegate) {
        this(delegate, GETTER_TIMEOUT_MS);
    }

    @VisibleForTesting
    MainThreadJSInterface(@NonNull BaseJSInterface delegate, long getterTimeoutMs) {
        this.delegate = delegate;
        this.getterTimeoutMs = getterTimeoutMs;
    }

    @Override
    @JavascriptInterface
    public String getMaxSize() {
        return callOnMainThread(delegate::getMaxSize, EMPTY_JSON);
    }

    @Override
    @JavascriptInterface
    public String getScreenSize() {
        return callOnMainThread(delegate::getScreenSize, EMPTY_JSON);
    }

    @Override
    @JavascriptInterface
    public String getDefaultPosition() {
        return callOnMainThread(delegate::getDefaultPosition, EMPTY_JSON);
    }

    @Override
    @JavascriptInterface
    public String getCurrentPosition() {
        return callOnMainThread(delegate::getCurrentPosition, EMPTY_JSON);
    }

    @Override
    @JavascriptInterface
    public void onOrientationPropertiesChanged(String properties) {
        postCommand(() -> delegate.onOrientationPropertiesChanged(properties));
    }

    @Override
    @JavascriptInterface
    public String getPlacementType() {
        return delegate.getPlacementType();
    }

    @Override
    @JavascriptInterface
    public void close() {
        postCommand(delegate::close);
    }

    @Override
    @JavascriptInterface
    public void resize() {
        postCommand(delegate::resize);
    }

    @Override
    @JavascriptInterface
    public void expand() {
        postCommand(() -> delegate.expand());
    }

    @Override
    @JavascriptInterface
    public void expand(String url) {
        postCommand(() -> delegate.expand(url));
    }

    @Override
    @JavascriptInterface
    public void open(String url) {
        postCommand(() -> delegate.open(url));
    }

    @Override
    @JavascriptInterface
    public void javaScriptCallback(String handlerHash, String method, String value) {
        post(() -> delegate.javaScriptCallback(handlerHash, method, value), false);
    }

    @Override
    @JavascriptInterface
    public void createCalendarEvent(String parameters) {
        postCommand(() -> delegate.createCalendarEvent(parameters));
    }

    @Override
    @JavascriptInterface
    public void storePicture(String url) {
        postCommand(() -> delegate.storePicture(url));
    }

    @Override
    @JavascriptInterface
    public boolean supports(String feature) {
        return delegate.supports(feature);
    }

    @Override
    @JavascriptInterface
    public void playVideo(String url) {
        postCommand(() -> delegate.playVideo(url));
    }

    @Override
    @Deprecated
    @JavascriptInterface
    public void shouldUseCustomClose(String useCustomClose) {
        postCommand(() -> delegate.shouldUseCustomClose(useCustomClose));
    }

    @Override
    @JavascriptInterface
    public String getLocation() {
        return callOnMainThread(delegate::getLocation, LOCATION_ERROR);
    }

    @Override
    @JavascriptInterface
    public String getCurrentAppOrientation() {
        return callOnMainThread(delegate::getCurrentAppOrientation, EMPTY_JSON);
    }

    @Override
    @JavascriptInterface
    public void unload() {
        postCommand(delegate::unload);
    }

    /**
     * mraid.js sends MRAID commands one at a time and waits for nativeCallComplete() before the next one.
     */
    private void postCommand(Runnable command) {
        post(command, true);
    }

    private void post(Runnable call, boolean isMraidCommand) {
        mainHandler.post(() -> {
            if (delegate.isDestroyed()) {
                return;
            }
            try {
                call.run();
            } catch (Exception exception) {
                // An uncaught exception here would crash the app, and mraid.js would keep waiting for nativeCallComplete().
                LogUtil.error(TAG, "MRAID call failed: " + Log.getStackTraceString(exception));
                if (isMraidCommand) {
                    delegate.getJsExecutor().executeNativeCallComplete();
                }
            }
        });
    }

    private String callOnMainThread(Callable<String> getter, String fallback) {
        Callable<String> call = () -> delegate.isDestroyed() ? fallback : getter.call();

        if (Looper.myLooper() == Looper.getMainLooper()) {
            try {
                return call.call();
            } catch (Exception exception) {
                LogUtil.error(TAG, "MRAID getter failed: " + Log.getStackTraceString(exception));
                return fallback;
            }
        }

        FutureTask<String> task = new FutureTask<>(call);
        mainHandler.post(task);
        try {
            return task.get(getterTimeoutMs, TimeUnit.MILLISECONDS);
        } catch (ExecutionException exception) {
            LogUtil.error(TAG, "MRAID getter failed: " + Log.getStackTraceString(exception.getCause()));
        } catch (TimeoutException exception) {
            LogUtil.warning(TAG, "MRAID getter timed out after " + getterTimeoutMs + " ms");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
        mainHandler.removeCallbacks(task);
        task.cancel(false);
        return fallback;
    }
}
