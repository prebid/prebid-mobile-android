package com.openx.apollo.networking;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.openx.apollo.bidding.data.bid.Bid;
import com.openx.apollo.bidding.data.bid.BidResponse;
import com.openx.apollo.networking.tracking.ServerConnection;
import com.openx.apollo.utils.logger.OXLog;

import org.json.JSONException;
import org.json.JSONObject;

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

    private final LinkedList<String> mUrlQueue = new LinkedList<>();
    private WinNotifierListener mWinNotifierListener;
    private Bid mBid;

    // For Testing purposes
    private static final String CACHE_URL_TEST_TEMPLATE = "http://%1$s%2$s?uuid=%3$s";
    private boolean mIsUnderTest = false;

    private final ResponseHandler mWinResponseHandler = new ResponseHandler() {
        @Override
        public void onResponse(BaseNetworkTask.GetUrlResult response) {
            String adMarkup;
            String responseString = response.responseString;
            if (isJson(responseString)) {
                adMarkup = extractAdm(responseString);
            }
            else {
                adMarkup = responseString;
            }
            mBid.setAdm(adMarkup);
            sendNextWinRequest();
        }

        @Override
        public void onError(String msg, long responseTime) {
            OXLog.error(TAG, "Failed to send win event: " + msg);
            sendNextWinRequest();
        }

        @Override
        public void onErrorWithException(Exception e, long responseTime) {
            OXLog.error(TAG, "Failed to send win event: " + e.getMessage());
            sendNextWinRequest();
        }
    };

    public interface WinNotifierListener {
        void onResult();
    }

    public void notifyWin(BidResponse bidResponse,
                          @NonNull
                              WinNotifierListener listener) {
        mWinNotifierListener = listener;
        mBid = bidResponse.getWinningBid();

        if (mBid == null) {
            mWinNotifierListener.onResult();
            return;
        }

        String cacheIdUrl = getCacheUrlFromBid(mBid, KEY_CACHE_ID);
        String uuidUrl = getCacheUrlFromBid(mBid, KEY_UUID);

        mUrlQueue.add(cacheIdUrl);
        mUrlQueue.add(uuidUrl);
        mUrlQueue.add(mBid.getNurl());
        mUrlQueue.removeAll(Collections.singleton(null));

        sendNextWinRequest();
    }

    private void sendNextWinRequest() {
        // All events have been fired, notify listener
        if (mUrlQueue.isEmpty()) {
            if (mWinNotifierListener != null) {
                mWinNotifierListener.onResult();
                cleanup();
            }
            return;
        }
        String winUrl = mUrlQueue.poll();
        if (TextUtils.isEmpty(winUrl)) {
            // Skip url and start next one
            sendNextWinRequest();
            return;
        }
        if (mBid.getAdm() != null && !TextUtils.isEmpty(mBid.getAdm())) {
            // Fire async event and start next one
            ServerConnection.fireAndForget(winUrl);
            sendNextWinRequest();
        }
        else {
            // Fire async event and wait for its result
            OXLog.debug(TAG, "Bid.adm is null or empty. Getting the ad from prebid cache");
            ServerConnection.fireWithResult(winUrl, mWinResponseHandler);
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
        mBid = null;
        mWinNotifierListener = null;
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
        if (mIsUnderTest) {
            return CACHE_URL_TEST_TEMPLATE;
        }
        else {
            return CACHE_URL_TEMPLATE;
        }
    }

    @VisibleForTesting
    void enableTestFlag() {
        mIsUnderTest = true;
    }
}
