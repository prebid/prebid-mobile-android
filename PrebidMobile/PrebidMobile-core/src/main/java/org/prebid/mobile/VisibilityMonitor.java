package org.prebid.mobile;

import android.app.Application;
import android.content.Context;
import android.os.CountDownTimer;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;
import org.prebid.mobile.addendum.AdViewUtils;
import org.prebid.mobile.rendering.models.CreativeVisibilityTracker;
import org.prebid.mobile.rendering.models.internal.VisibilityTrackerOption;
import org.prebid.mobile.rendering.models.ntv.NativeEventTracker;
import org.prebid.mobile.rendering.networking.tracking.ServerConnection;
import org.prebid.mobile.rendering.sdk.PrebidContextHolder;

import java.lang.ref.WeakReference;

public class VisibilityMonitor {

    private boolean stopAfterFirstFinding = false;

    private final VisibilityTimer visibilityTimer = new VisibilityTimer();

    @Nullable
    private VisibilityActivityListener activityListener;

    public void trackView(@NotNull View adViewContainer, @NotNull String burl, @NotNull String cacheId) {
        stopTracking();

        visibilityTimer.start(adViewContainer, burl, cacheId, stopAfterFirstFinding);
    }

    public void trackInterstitial(String burl, String cacheId) {
        stopTracking();

        stopAfterFirstFinding = true;
        activityListener = new VisibilityActivityListener(this, burl, cacheId);
        getApplication().registerActivityLifecycleCallbacks(activityListener);
    }

    public void stopTracking() {
        visibilityTimer.destroy();

        if (activityListener != null) {
            getApplication().unregisterActivityLifecycleCallbacks(activityListener);
        }
    }

    private Application getApplication() {
        Context context = PrebidContextHolder.getContext();
        return (Application) context;
    }

    private static class VisibilityTimer extends CountDownTimer {

        private static final int LONGEVITY = Integer.MAX_VALUE;
        private static final int INTERVAL = 500;
        private static final String TAG = "VisibilityTimer";

        private int lastWebViewHash;
        private boolean stopAfterFirstFinding;
        private String burl;
        private String responseCacheId;

        private WeakReference<View> containerViewReference;
        @Nullable
        private CreativeVisibilityTracker visibilityTracker;

        public VisibilityTimer() {
            super(LONGEVITY, INTERVAL);
        }

        public void start(View containerView, String burl, String cacheId, boolean stopAfterFirstFinding) {
            this.burl = burl;
            this.responseCacheId = cacheId;
            this.containerViewReference = new WeakReference<>(containerView);
            this.stopAfterFirstFinding = stopAfterFirstFinding;

            LogUtil.debug(TAG, "Start of monitoring...");
            start();
        }

        @Override
        public void onTick(long millisUntilFinished) {
            View containerView = containerViewReference.get();
            if (containerView == null) {
                LogUtil.debug(TAG, "Cancelled due to ad view is null");
                destroy();
                return;
            }

            WebView webView = findIn(containerView);
            if (webView == null) {
                return;
            }

            if (lastWebViewHash == webView.hashCode()) {
                return;
            }
            lastWebViewHash = webView.hashCode();

            if (stopAfterFirstFinding) {
                LogUtil.debug(TAG, "Interstitial WebView found. Stopping...");
                destroy();
            }


            AdViewUtils.CacheIdResult onCacheIdFound = createCacheIdFoundTask(new WeakReference<>(webView), this, responseCacheId, lastWebViewHash);
            AdViewUtils.findCacheId(webView, onCacheIdFound);
        }

        private void attachVisibilityTracker(WebView webView) {
            if (visibilityTracker != null) {
                visibilityTracker.stopVisibilityCheck();
            }

            visibilityTracker = new CreativeVisibilityTracker(webView, new VisibilityTrackerOption(NativeEventTracker.EventType.IMPRESSION));
            visibilityTracker.setVisibilityTrackerListener(result -> {
                boolean visible = result.isVisible();
                if (visible) {
                    LogUtil.debug(TAG, "View is visible. Firing event: " + burl);
                    ServerConnection.fireAndForget(burl);
                    visibilityTracker.stopVisibilityCheck();
                }
            });
            visibilityTracker.startVisibilityCheck(PrebidContextHolder.getContext());
        }

        @Override
        public void onFinish() {
        }

        public void destroy() {
            if (visibilityTracker != null) {
                LogUtil.debug(TAG, "Destroying");
                visibilityTracker.stopVisibilityCheck();
                visibilityTracker = null;
            }
            cancel();
        }

        public static WebView findIn(View root) {
            if (root instanceof WebView) {
                return (WebView) root;
            }

            if (root instanceof ViewGroup) {
                return findRecursively((ViewGroup) root);
            }

            return null;
        }

        private static WebView findRecursively(ViewGroup root) {
            for (int i = 0; i < root.getChildCount(); i++) {
                View child = root.getChildAt(i);
                if (child instanceof WebView) {
                    return (WebView) child;
                }

                if (child instanceof ViewGroup) {
                    WebView result = findRecursively((ViewGroup) child);
                    if (result != null) {
                        return result;
                    }
                }
            }
            return null;
        }

        private static AdViewUtils.CacheIdResult createCacheIdFoundTask(WeakReference<WebView> webViewReference, VisibilityTimer visibilityTimer, String responseCacheId, int lastWebViewHash) {
            return cacheId -> {
                if (cacheId == null || cacheId.isEmpty()) {
                    return;
                }

                if (!cacheId.equals(responseCacheId)) {
                    LogUtil.warning(TAG, "Different cache ids");
                    return;
                }

                WebView webView = webViewReference.get();
                if (webView == null) {
                    return;
                }

                visibilityTimer.attachVisibilityTracker(webView);
                LogUtil.debug(TAG, "Registering the new WebView: " + lastWebViewHash);
            };
        }
    }

}
