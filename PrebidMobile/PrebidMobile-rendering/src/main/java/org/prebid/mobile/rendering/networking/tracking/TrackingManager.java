package org.prebid.mobile.rendering.networking.tracking;

import androidx.annotation.Nullable;

import org.prebid.mobile.rendering.utils.logger.OXLog;

import java.util.ArrayList;
import java.util.List;

public class TrackingManager {

    private static final String TAG = TrackingManager.class.getSimpleName();

    private static TrackingManager sInstance = null;

    private TrackingManager() {

    }

    public static TrackingManager getInstance() {
        if (sInstance == null) {
            sInstance = new TrackingManager();
        }
        return sInstance;
    }

    public void fireEventTrackingURL(String url) {
        ServerConnection.fireAndForget(url);
    }

    public void fireEventTrackingURLs(@Nullable List<String> urls) {
        if (urls == null) {
            OXLog.debug(TAG, "fireEventTrackingURLs(): Unable to execute event tracking requests. Provided list is null");
            return;
        }
        for (String url : urls) {
            fireEventTrackingURL(url);
        }
    }

    public void fireEventTrackingImpressionURLs(ArrayList<String> impressionUrls) {
        for (String url : impressionUrls) {
            ServerConnection.fireAndForgetImpressionUrl(url);
        }
    }
}
