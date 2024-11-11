package org.prebid.mobile;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;
import org.prebid.mobile.rendering.models.CreativeVisibilityTracker;
import org.prebid.mobile.rendering.models.internal.VisibilityTrackerOption;
import org.prebid.mobile.rendering.models.ntv.NativeEventTracker;
import org.prebid.mobile.rendering.networking.tracking.ServerConnection;
import org.prebid.mobile.rendering.sdk.PrebidContextHolder;

import java.lang.ref.WeakReference;

public class VisibilityMonitor {

    private final VisibilityTimer visibilityTimer = new VisibilityTimer();

    public void trackView(@NotNull View adViewContainer, @NotNull String burl) {
        visibilityTimer.start(adViewContainer, burl);
    }

    public void cancel() {
        visibilityTimer.destroy();
    }


    private static class VisibilityTimer extends CountDownTimer {

        private static final int LONGEVITY = Integer.MAX_VALUE;
        private static final int INTERVAL = 500;

        private int lastWebViewHash;
        private String burl;

        private WeakReference<View> containerViewReference;
        @Nullable
        private CreativeVisibilityTracker visibilityTracker;

        public VisibilityTimer() {
            super(LONGEVITY, INTERVAL);
        }

        public void start(View containerView, String burl) {
            this.burl = burl;
            this.containerViewReference = new WeakReference<>(containerView);
            start();
        }

        @Override
        public void onTick(long millisUntilFinished) {
            View containerView = containerViewReference.get();
            if (containerView == null) {
                cancel();
                return;
            }

            WebView webView = findIn(containerView);
            if (webView == null) {
                return;
            }

            if (lastWebViewHash == webView.hashCode()) {
                Log.d("TESTV", "Ignored");
                return;
            }
            lastWebViewHash = webView.hashCode();

            Log.d("TESTV", "Registering web view with hash: " + lastWebViewHash);
            attachVisibilityTracker(webView);
            cancel();
            LogUtil.info("Ad view impression tracker injected");
        }

        private void attachVisibilityTracker(WebView webView) {
            if (visibilityTracker != null) {
                visibilityTracker.stopVisibilityCheck();
            }

            visibilityTracker = new CreativeVisibilityTracker(webView, new VisibilityTrackerOption(NativeEventTracker.EventType.IMPRESSION));
            visibilityTracker.setVisibilityTrackerListener(result -> {
                boolean visible = result.isVisible();
                if (visible) {
                    Log.d("TESTV", "FIRING BURL: " + burl);
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
                visibilityTracker.stopVisibilityCheck();
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
    }

}
