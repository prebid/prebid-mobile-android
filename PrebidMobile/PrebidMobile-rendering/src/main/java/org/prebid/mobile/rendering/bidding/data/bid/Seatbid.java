package org.prebid.mobile.rendering.bidding.data.bid;

import org.json.JSONArray;
import org.json.JSONObject;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.Ext;

import java.util.ArrayList;
import java.util.List;

public class Seatbid {

    // Array of 1+ Bid objects each related to an impression.
    private List<Bid> mBids = new ArrayList<>();

    // ID of the buyer seat (e.g., advertiser, agency) on whose behalf
    // this bid is made
    private String mSeat;

    // 0 = impressions can be won individually; 1 = impressions must
    // be won or lost as a group.
    private int mGroup;

    // Placeholder for bidder-specific extensions to OpenRTB.
    private Ext mExt;

    protected Seatbid() {
    }

    public List<Bid> getBids() {
        return mBids;
    }

    public String getSeat() {
        return mSeat;
    }

    public int getGroup() {
        return mGroup;
    }

    public Ext getExt() {
        if (mExt == null) {
            mExt = new Ext();
        }
        return mExt;
    }

    public static Seatbid fromJSONObject(JSONObject jsonObject) {
        Seatbid seatbid = new Seatbid();
        if (jsonObject == null) {
            return seatbid;
        }

        if (jsonObject.has("bid")) {
            JSONArray jsonArray = jsonObject.optJSONArray("bid");
            for (int i = 0; i < jsonArray.length(); i++) {
                Bid bid = Bid.fromJSONObject(jsonArray.optJSONObject(i));
                if (bid != null) {
                    seatbid.mBids.add(bid);
                }
            }
        }
        seatbid.mSeat = jsonObject.optString("seat");
        seatbid.mGroup = jsonObject.optInt("group", -1);
        seatbid.mExt = new Ext();
        if (jsonObject.has("ext")) {
            seatbid.mExt.put(jsonObject.optJSONObject("ext"));
        }

        return seatbid;
    }
}
