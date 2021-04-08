package org.prebid.mobile.rendering.bidding.data.bid;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.Ext;
import org.prebid.mobile.rendering.utils.helpers.Dips;
import org.prebid.mobile.rendering.utils.helpers.Utils;
import org.prebid.mobile.rendering.utils.logger.OXLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BidResponse {
    private final static String TAG = BidResponse.class.getSimpleName();
    public static final String KEY_CACHE_ID = "hb_cache_id_local";

    // ID of the bid request to which this is a response
    private String mId;

    // Bid currency using ISO-4217 alpha codes.
    private String mCur;

    //Bidder generated response ID to assist with logging/tracking.
    private String mBidId;

    //Optional feature to allow a bidder to set data in the exchange’s cookie
    private String mCustomData;

    // Reason for not bidding
    private int mNbr;

    // Array of seatbid objects; 1+ required if a bid is to be made.
    private List<Seatbid> mSeatbids;
    private Ext mExt;

    private boolean mHasParseError = false;
    private String mParseError;

    private long mCreationTime;

    public BidResponse(String json) {
        mSeatbids = new ArrayList<>();
        parseJson(json);
    }

    public String getId() {
        return mId;
    }

    public List<Seatbid> getSeatbids() {
        return mSeatbids;
    }

    public String getCur() {
        return mCur;
    }

    public Ext getExt() {
        if (mExt == null) {
            mExt = new Ext();
        }
        return mExt;
    }

    public boolean hasParseError() {
        return mHasParseError;
    }

    public String getParseError() {
        return mParseError;
    }

    public String getBidId() {
        return mBidId;
    }

    public String getCustomData() {
        return mCustomData;
    }

    public int getNbr() {
        return mNbr;
    }

    private void parseJson(String json) {
        try {
            JSONObject responseJson = new JSONObject(json);
            mId = responseJson.optString("id");
            mCur = responseJson.optString("cur");
            mBidId = responseJson.optString("bidid");
            mCustomData = responseJson.optString("customdata");
            mNbr = responseJson.optInt("nbr", -1);

            if (responseJson.has("ext")) {
                mExt = new Ext();
                mExt.put(responseJson.optJSONObject("ext"));
            }

            JSONArray jsonSeatbids = responseJson.optJSONArray("seatbid");
            if (jsonSeatbids != null) {
                for (int i = 0; i < jsonSeatbids.length(); i++) {
                    Seatbid seatbid = Seatbid.fromJSONObject(jsonSeatbids.optJSONObject(i));
                    mSeatbids.add(seatbid);
                }
            }
            if (getWinningBid() == null) {
                mHasParseError = true;
                mParseError = "Failed to parse bids. No winning bids were found.";
                OXLog.info(TAG, mParseError);
            }

            mCreationTime = System.currentTimeMillis();
        }
        catch (JSONException e) {
            mHasParseError = true;
            mParseError = "Failed to parse JSON String: " + e.getMessage();
            OXLog.error(TAG, mParseError);
        }
    }

    public long getCreationTime() {
        return mCreationTime;
    }

    @Nullable
    public Bid getWinningBid() {
        if (mSeatbids == null) {
            return null;
        }

        for (Seatbid seatbid : mSeatbids) {
            for (Bid bid : seatbid.getBids()) {
                if (hasWinningKeywords(bid.getPrebid())) {
                    return bid;
                }
            }
        }

        return null;
    }

    @NonNull
    public HashMap<String, String> getTargeting() {
        HashMap<String, String> keywords = new HashMap<>();
        for (Seatbid seatbid : mSeatbids) {
            for (Bid bid : seatbid.getBids()) {
                if (bid.getPrebid() != null) {
                    keywords.putAll(bid.getPrebid().getTargeting());
                }
            }
        }

        return keywords;
    }

    @NonNull
    public HashMap<String, String> getTargetingWithCacheId() {
        // required for future BidResponseCache access
        final HashMap<String, String> targeting = getTargeting();
        targeting.put(KEY_CACHE_ID, mId);
        return targeting;
    }

    public boolean isVideo() {
        Bid bid = getWinningBid();
        if (bid != null) {
            return Utils.isVast(bid.getAdm());
        }
        return false;
    }

    private boolean hasWinningKeywords(Prebid prebid) {
        if (prebid == null || prebid.getTargeting().isEmpty()) {
            return false;
        }
        HashMap<String, String> targeting = prebid.getTargeting();
        return targeting.containsKey("hb_pb")
               && targeting.containsKey("hb_bidder")
               && targeting.containsKey("hb_cache_id");
    }

    @NonNull
    public Pair<Integer, Integer> getWinningBidWidthHeightPairDips(Context context) {
        final Bid winningBid = getWinningBid();
        if (winningBid == null) {
            return new Pair<>(0, 0);
        }

        final int width = Dips.dipsToIntPixels(winningBid.getWidth(), context);
        final int height = Dips.dipsToIntPixels(winningBid.getHeight(), context);
        return new Pair<>(width, height);
    }
}
