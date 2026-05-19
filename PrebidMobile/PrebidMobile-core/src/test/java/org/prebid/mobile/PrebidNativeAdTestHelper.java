package org.prebid.mobile;

import android.app.Application;
import android.content.Context;
import android.view.View;

import org.prebid.mobile.reflection.Reflection;
import org.prebid.mobile.test.utils.ResourceUtils;

import java.util.ArrayList;
import java.lang.ref.WeakReference;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

final class PrebidNativeAdTestHelper {

    private PrebidNativeAdTestHelper() {
    }

    static PrebidNativeAd nativeAdFromFile(String path) {
        String resource = ResourceUtils.convertResourceToString(path);
        String cacheId = CacheManager.save(resource);
        return PrebidNativeAd.create(cacheId);
    }

    static View createViewMock() {
        Context contextMock = mock(Context.class);
        when(contextMock.getApplicationContext()).thenReturn(mock(Application.class));

        View mainMock = mock(View.class);
        when(mainMock.getContext()).thenReturn(contextMock);
        return mainMock;
    }

    static ArrayList<String> reflectAdmImpressionTrackers(PrebidNativeAd ad) {
        return Reflection.getFieldOf(ad, "imp_trackers");
    }

    static ArrayList<ImpressionTracker> reflectImpressionTrackerObjects(PrebidNativeAd ad) {
        return Reflection.getFieldOf(ad, "impressionTrackers");
    }

    static String reflectImpressionTrackerUrl(ImpressionTracker tracker) {
        return Reflection.getFieldOf(tracker, "url");
    }

    static boolean isExpired(PrebidNativeAd ad) {
        return Reflection.getFieldOf(ad, "expired");
    }

    static void markRegisteredViewReleased(PrebidNativeAd ad) {
        Reflection.setVariableTo(ad, "registeredView", new WeakReference<View>(null));
    }

    static class TestNativeAdEventListener implements PrebidNativeAdEventListener {
        private final ExpirationCallback expirationCallback;

        TestNativeAdEventListener(ExpirationCallback expirationCallback) {
            this.expirationCallback = expirationCallback;
        }

        @Override
        public void onAdClicked() {
        }

        @Override
        public void onAdImpression() {
        }

        @Override
        public void onAdExpired() {
            expirationCallback.onExpired(true);
        }
    }

    interface ExpirationCallback {
        void onExpired(boolean expired);
    }

}
