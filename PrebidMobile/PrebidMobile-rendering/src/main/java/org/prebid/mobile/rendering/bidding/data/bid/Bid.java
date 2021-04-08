package org.prebid.mobile.rendering.bidding.data.bid;


import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;
import org.prebid.mobile.rendering.models.internal.MacrosModel;
import org.prebid.mobile.rendering.utils.helpers.MacrosResolutionHelper;

import java.util.HashMap;
import java.util.Map;

public class Bid {

    // Bidder generated bid ID to assist with logging/tracking.
    private String mId;

    //  ID of the Imp object in the related bid request.
    private String mImpId;

    // Bid price expressed as CPM although the actual transaction is
    // for a unit impression only.
    private double mPrice;

    // Optional means of conveying ad markup in case the bid wins;
    // supersedes the win notice if markup is included in both.
    // Substitution macros (Section 4.4) may be included.
    private String mAdm;

    // Creative ID to assist with ad quality checking.
    private String mCrid;

    // Width of the creative in device independent pixels (DIPS)
    private int mWidth;

    // Height of the creative in device independent pixels (DIPS).
    private int mHeight;

    // "prebid" object from "ext"
    private Prebid mPrebid;

    // Win notice URL called by the exchange if the bid wins (not  necessarily indicative of a delivered, viewed, or billable ad);
    // optional means of serving ad markup
    private String mNurl;

    // Billing notice URL called by the exchange when a winning bid
    // becomes billable based on exchange-specific business policy
    private String mBurl;

    // Loss notice URL called by the exchange when a bid is known to
    // have been lost
    private String mLurl;

    // ID of a preloaded ad to be served if the bid wins
    private String mAdid;

    // Advertiser domain for block list checking
    private String[] mAdomain;

    // A platform-specific application identifier intended to be unique to the app and independent of the exchange.
    private String mBundle;

    // URL without cache-busting to an image that is representative
    // of the content of the campaign for ad quality/safety checking
    private String mIurl;

    // Campaign ID to assist with ad quality checking; the collection
    // of creatives for which iurl should be representative
    private String mCid;

    // Tactic ID to enable buyers to label bids for reporting to the
    // exchange the tactic through which their bid was submitted
    private String mTactic;

    // IAB content categories of the creative
    private String[] mCat;

    // Set of attributes describing the creative
    private int[] mAttr;

    // API required by the markup if applicable
    private int mApi;

    // Video response protocol of the markup if applicable.
    private int mProtocol;

    // Creative media rating per IQG guidelines
    private int mQagmediarating;

    // Language of the creative using ISO-639-1-alpha-2
    private String mLanguage;

    // Reference to the deal.id from the bid request if this bid
    // pertains to a private marketplace direct deal
    private String mDealId;

    // Relative width of the creative when expressing size as a ratio.
    // Required for Flex Ads
    private int mWRatio;

    // Relative height of the creative when expressing size as a ratio.
    // Required for Flex Ads
    private int mHRatio;

    // Advisory as to the number of seconds the bidder is willing to
    // wait between the auction and the actual impression
    private int mExp;

    protected Bid() {
    }

    public String getId() {
        return mId;
    }

    public String getImpId() {
        return mImpId;
    }

    public double getPrice() {
        return mPrice;
    }

    public String getAdm() {
        return mAdm;
    }

    public String getCrid() {
        return mCrid;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public Prebid getPrebid() {
        if (mPrebid == null) {
            mPrebid = new Prebid();
        }
        return mPrebid;
    }

    public String getNurl() {
        return mNurl;
    }

    public String getBurl() {
        return mBurl;
    }

    public String getLurl() {
        return mLurl;
    }

    public String getAdid() {
        return mAdid;
    }

    public String[] getAdomain() {
        return mAdomain;
    }

    public String getBundle() {
        return mBundle;
    }

    public String getIurl() {
        return mIurl;
    }

    public String getCid() {
        return mCid;
    }

    public String getTactic() {
        return mTactic;
    }

    public String[] getCat() {
        return mCat;
    }

    public int[] getAttr() {
        return mAttr;
    }

    public int getApi() {
        return mApi;
    }

    public int getProtocol() {
        return mProtocol;
    }

    public int getQagmediarating() {
        return mQagmediarating;
    }

    public String getLanguage() {
        return mLanguage;
    }

    public String getDealId() {
        return mDealId;
    }

    public int getWRatio() {
        return mWRatio;
    }

    public int getHRatio() {
        return mHRatio;
    }

    public int getExp() {
        return mExp;
    }

    public static Bid fromJSONObject(JSONObject jsonObject) {
        Bid bid = new Bid();
        if (jsonObject == null) {
            return bid;
        }
        bid.mId = jsonObject.optString("id", null);
        bid.mImpId = jsonObject.optString("impid", null);
        bid.mPrice = jsonObject.optDouble("price", 0);
        bid.mAdm = jsonObject.optString("adm", null);
        bid.mCrid = jsonObject.optString("crid", null);
        bid.mWidth = jsonObject.optInt("w");
        bid.mHeight = jsonObject.optInt("h");

        bid.mNurl = jsonObject.optString("nurl", null);
        bid.mBurl = jsonObject.optString("burl", null);
        bid.mLurl = jsonObject.optString("lurl", null);
        bid.mAdid = jsonObject.optString("adid", null);
        bid.mAdomain = getStringArrayFromJson(jsonObject, "adomain");
        bid.mBundle = jsonObject.optString("bundle", null);
        bid.mIurl = jsonObject.optString("iurl", null);
        bid.mCid = jsonObject.optString("cid", null);
        bid.mTactic = jsonObject.optString("tactic", null);
        bid.mCat = getStringArrayFromJson(jsonObject, "cat");
        bid.mAttr = getIntArrayFromJson(jsonObject, "attr");
        bid.mApi = jsonObject.optInt("api", -1);
        bid.mProtocol = jsonObject.optInt("protocol", -1);
        bid.mQagmediarating = jsonObject.optInt("qagmediarating", -1);
        bid.mLanguage = jsonObject.optString("language", null);
        bid.mDealId = jsonObject.optString("dealid", null);
        bid.mWRatio = jsonObject.optInt("wratio");
        bid.mHRatio = jsonObject.optInt("hratio");
        bid.mExp = jsonObject.optInt("exp", -1);

        JSONObject ext = jsonObject.optJSONObject("ext");
        if (ext != null) {
            bid.mPrebid = Prebid.fromJSONObject(ext.optJSONObject("prebid"));
        }

        substituteMacros(bid);

        return bid;
    }

    public void setAdm(String adm) {
        mAdm = adm;
    }

    private static String[] getStringArrayFromJson(JSONObject jsonObject, String key) {
        if (jsonObject.has(key)) {
            JSONArray jsonArray = jsonObject.optJSONArray(key);
            if (jsonArray != null && jsonArray.length() > 0) {
                int length = jsonArray.length();
                String[] stringArray = new String[length];
                for (int i = 0; i < length; i++) {
                    stringArray[i] = jsonArray.optString(i);
                }
                return stringArray;
            }
        }
        return new String[0];
    }

    private static int[] getIntArrayFromJson(JSONObject jsonObject, String key) {
        if (jsonObject.has(key)) {
            JSONArray jsonArray = jsonObject.optJSONArray(key);
            if (jsonArray != null && jsonArray.length() > 0) {
                int length = jsonArray.length();
                int[] intArray = new int[length];
                for (int i = 0; i < length; i++) {
                    intArray[i] = jsonArray.optInt(i);
                }
                return intArray;
            }
        }
        return new int[0];
    }

    private static void substituteMacros(Bid bid) {
        Map<String, MacrosModel> macrosModelMap = new HashMap<>();

        String priceText = String.valueOf(bid.getPrice());
        String base64PriceText = Base64.encodeToString(priceText.getBytes(), Base64.NO_WRAP);

        macrosModelMap.put(MacrosModel.MACROS_AUCTION_PRICE, new MacrosModel(priceText));
        macrosModelMap.put(MacrosModel.MACROS_AUCTION_PRICE_BASE_64, new MacrosModel(base64PriceText));

        bid.mAdm = MacrosResolutionHelper.resolveAuctionMacros(bid.mAdm, macrosModelMap);
        bid.mNurl = MacrosResolutionHelper.resolveAuctionMacros(bid.mNurl, macrosModelMap);
    }
}
