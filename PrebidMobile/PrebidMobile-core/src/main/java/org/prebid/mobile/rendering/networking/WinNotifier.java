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

package org.prebid.mobile.rendering.networking;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import org.json.JSONException;
import org.json.JSONObject;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.data.bid.Prebid;
import org.prebid.mobile.rendering.networking.tracking.ServerConnection;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

public class WinNotifier {

    private static final String TAG = WinNotifier.class.getSimpleName();

    private static final String KEY_CACHE_HOST = "hb_cache_host";
    private static final String KEY_CACHE_PATH = "hb_cache_path";
    private static final String KEY_CACHE_ID = "hb_cache_id";
    private static final String KEY_UUID = "hb_uuid";
    private static final String CACHE_URL_TEMPLATE = "https://%1$s%2$s?uuid=%3$s";

    private final LinkedList<String> urlQueue = new LinkedList<>();
    private WinNotifierListener winNotifierListener;
    private Bid bid;

    // For Testing purposes
    private static final String CACHE_URL_TEST_TEMPLATE = "http://%1$s%2$s?uuid=%3$s";
    private boolean isUnderTest = false;

    private final ResponseHandler winResponseHandler = new ResponseHandler() {
        @Override
        public void onResponse(BaseNetworkTask.GetUrlResult response) {
            String adMarkup;
            String responseString = response.responseString;
            if (isJson(responseString)) {
                adMarkup = extractAdm(responseString);
            } else {
                adMarkup = responseString;
            }
            bid.setAdm(adMarkup);
            sendNextWinRequest();
        }

        @Override
        public void onError(String msg, long responseTime) {
            LogUtil.error(TAG, "Failed to send win event: " + msg);
            sendNextWinRequest();
        }

        @Override
        public void onErrorWithException(Exception e, long responseTime) {
            LogUtil.error(TAG, "Failed to send win event: " + e.getMessage());
            sendNextWinRequest();
        }
    };

    public interface WinNotifierListener {
        void onResult();
    }

    public void notifyWin(BidResponse bidResponse,
                          @NonNull
                              WinNotifierListener listener) {
        winNotifierListener = listener;
        bid = bidResponse.getWinningBid();

        if (bid == null) {
            winNotifierListener.onResult();
            return;
        }

        String cacheIdUrl = getCacheUrlFromBid(bid, KEY_CACHE_ID);
        String uuidUrl = getCacheUrlFromBid(bid, KEY_UUID);
        String winUrl = getWinUrl(bid);

        urlQueue.add(cacheIdUrl);
        urlQueue.add(uuidUrl);
        urlQueue.add(bid.getNurl());
        urlQueue.add(winUrl);
        urlQueue.removeAll(Collections.singleton(null));

        sendNextWinRequest();
    }

    private void sendNextWinRequest() {
        // All events have been fired, notify listener
        if (urlQueue.isEmpty()) {
            if (winNotifierListener != null) {
                winNotifierListener.onResult();
                cleanup();
            }
            return;
        }
        String winUrl = urlQueue.poll();
        if (TextUtils.isEmpty(winUrl)) {
            // Skip url and start next one
            sendNextWinRequest();
            return;
        }
        if (bid.getAdm() != null && !TextUtils.isEmpty(bid.getAdm())) {
            // Fire async event and start next one
            ServerConnection.fireAndForget(winUrl);
            sendNextWinRequest();
        } else {
            // Fire async event and wait for its result
            LogUtil.debug(TAG, "Bid.adm is null or empty. Getting the ad from prebid cache");
            ServerConnection.fireWithResult(winUrl, winResponseHandler);
        }
    }

    private String getCacheUrlFromBid(Bid bid, String idKey) {
        if (bid.getPrebid() == null || bid.getPrebid().getTargeting() == null) {
            return null;
        }
        HashMap<String, String> targetingMap = bid.getPrebid().getTargeting();
        String host = targetingMap.get(KEY_CACHE_HOST);
        String path = targetingMap.get(KEY_CACHE_PATH);
        String id = targetingMap.get(idKey);

        if (host == null || path == null || id == null) {
            return null;
        }
        return String.format(getUrlTemplate(), host, path, id);
    }

    private void cleanup() {
        bid = null;
        winNotifierListener = null;
    }

    private boolean isJson(String response) {
        try {
            new JSONObject(response);
        }
        catch (JSONException ex) {
            return false;
        }
        return true;
    }

    private String extractAdm(String response) {
        try {
            JSONObject responseJson = new JSONObject(response);
            return responseJson.getString("adm");
        }
        catch (JSONException e) {
            return null;
        }
    }

    private String getUrlTemplate() {
        if (isUnderTest) {
            return CACHE_URL_TEST_TEMPLATE;
        } else {
            return CACHE_URL_TEMPLATE;
        }
    }

    private String getWinUrl(@NonNull Bid bid) {
        Prebid prebid = bid.getPrebid();
        if (prebid != null) {
            return prebid.getWinEventUrl();
        }
        return null;
    }

    @VisibleForTesting
    void enableTestFlag() {
        isUnderTest = true;
    }

}
