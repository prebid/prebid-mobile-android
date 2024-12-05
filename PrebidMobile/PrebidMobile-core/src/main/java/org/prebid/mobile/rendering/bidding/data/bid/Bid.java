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

package org.prebid.mobile.rendering.bidding.data.bid;


import android.util.Base64;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.prebid.mobile.api.data.BidInfo;
import org.prebid.mobile.rendering.interstitial.rewarded.RewardedExt;
import org.prebid.mobile.rendering.interstitial.rewarded.RewardedExtParser;
import org.prebid.mobile.rendering.models.internal.MacrosModel;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.MobileSdkPassThrough;
import org.prebid.mobile.rendering.utils.helpers.MacrosResolutionHelper;

import java.util.HashMap;
import java.util.Map;

public class Bid {

    // Bidder generated bid ID to assist with logging/tracking.
    private String id;

    //  ID of the Imp object in the related bid request.
    private String impId;

    // Bid price expressed as CPM although the actual transaction is
    // for a unit impression only.
    private double price;

    // Optional means of conveying ad markup in case the bid wins;
    // supersedes the win notice if markup is included in both.
    // Substitution macros (Section 4.4) may be included.
    private String adm;

    // Creative ID to assist with ad quality checking.
    private String crid;

    // Width of the creative in device independent pixels (DIPS)
    private int width;

    // Height of the creative in device independent pixels (DIPS).
    private int height;

    // "prebid" object from "ext"
    private Prebid prebid;

    // Win notice URL called by the exchange if the bid wins (not  necessarily indicative of a delivered, viewed, or billable ad);
    // optional means of serving ad markup
    private String nurl;

    // Billing notice URL called by the exchange when a winning bid
    // becomes billable based on exchange-specific business policy
    private String burl;

    // Loss notice URL called by the exchange when a bid is known to
    // have been lost
    private String lurl;

    // ID of a preloaded ad to be served if the bid wins
    private String adid;

    // Advertiser domain for block list checking
    private String[] adomain;

    // A platform-specific application identifier intended to be unique to the app and independent of the exchange.
    private String bundle;

    // URL without cache-busting to an image that is representative
    // of the content of the campaign for ad quality/safety checking
    private String iurl;

    // Campaign ID to assist with ad quality checking; the collection
    // of creatives for which iurl should be representative
    private String cid;

    // Bid json string. Used only for CacheManager.
    private String jsonString;

    // Tactic ID to enable buyers to label bids for reporting to the
    // exchange the tactic through which their bid was submitted
    private String tactic;

    // IAB content categories of the creative
    private String[] cat;

    // Set of attributes describing the creative
    private int[] attr;

    // API required by the markup if applicable
    private int api;

    // Video response protocol of the markup if applicable.
    private int protocol;

    // Creative media rating per IQG guidelines
    private int qagmediarating;

    // Language of the creative using ISO-639-1-alpha-2
    private String language;

    // Reference to the deal.id from the bid request if this bid
    // pertains to a private marketplace direct deal
    private String dealId;

    // Relative width of the creative when expressing size as a ratio.
    // Required for Flex Ads
    private int WRatio;

    // Relative height of the creative when expressing size as a ratio.
    // Required for Flex Ads
    private int HRatio;

    // Advisory as to the number of seconds the bidder is willing to
    // wait between the auction and the actual impression
    private int exp;

    @Nullable
    private Map<String, String> events;

    private MobileSdkPassThrough mobileSdkPassThrough;

    @NonNull
    private RewardedExt rewardedExt = RewardedExt.defaultExt();

    protected Bid() {
    }

    public String getId() {
        return id;
    }

    public String getImpId() {
        return impId;
    }

    public double getPrice() {
        return price;
    }

    public String getAdm() {
        return adm;
    }

    public String getCrid() {
        return crid;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Prebid getPrebid() {
        if (prebid == null) {
            prebid = new Prebid();
        }
        return prebid;
    }

    public String getNurl() {
        return nurl;
    }

    public String getBurl() {
        return burl;
    }

    public String getLurl() {
        return lurl;
    }

    public String getAdid() {
        return adid;
    }

    public String[] getAdomain() {
        return adomain;
    }

    public String getBundle() {
        return bundle;
    }

    public String getIurl() {
        return iurl;
    }

    public String getCid() {
        return cid;
    }

    public String getTactic() {
        return tactic;
    }

    public String[] getCat() {
        return cat;
    }

    public int[] getAttr() {
        return attr;
    }

    public int getApi() {
        return api;
    }

    public int getProtocol() {
        return protocol;
    }

    public int getQagmediarating() {
        return qagmediarating;
    }

    public String getLanguage() {
        return language;
    }

    public String getDealId() {
        return dealId;
    }

    public int getWRatio() {
        return WRatio;
    }

    public int getHRatio() {
        return HRatio;
    }

    public int getExp() {
        return exp;
    }

    public String getJsonString() {
        return jsonString;
    }

    public MobileSdkPassThrough getMobileSdkPassThrough() {
        return mobileSdkPassThrough;
    }

    @Nullable
    public Map<String, String> getEvents() {
        return events;
    }

    public static Bid fromJSONObject(JSONObject jsonObject) {
        Bid bid = new Bid();
        if (jsonObject == null) {
            return bid;
        }
        bid.jsonString = jsonObject.toString();
        bid.id = jsonObject.optString("id", null);
        bid.impId = jsonObject.optString("impid", null);
        bid.price = jsonObject.optDouble("price", 0);
        bid.adm = jsonObject.optString("adm", null);
        bid.crid = jsonObject.optString("crid", null);
        bid.width = jsonObject.optInt("w");
        bid.height = jsonObject.optInt("h");

        bid.nurl = jsonObject.optString("nurl", null);
        bid.burl = jsonObject.optString("burl", null);
        bid.lurl = jsonObject.optString("lurl", null);
        bid.adid = jsonObject.optString("adid", null);
        bid.adomain = getStringArrayFromJson(jsonObject, "adomain");
        bid.bundle = jsonObject.optString("bundle", null);
        bid.iurl = jsonObject.optString("iurl", null);
        bid.cid = jsonObject.optString("cid", null);
        bid.tactic = jsonObject.optString("tactic", null);
        bid.cat = getStringArrayFromJson(jsonObject, "cat");
        bid.attr = getIntArrayFromJson(jsonObject, "attr");
        bid.api = jsonObject.optInt("api", -1);
        bid.protocol = jsonObject.optInt("protocol", -1);
        bid.qagmediarating = jsonObject.optInt("qagmediarating", -1);
        bid.language = jsonObject.optString("language", null);
        bid.dealId = jsonObject.optString("dealid", null);
        bid.WRatio = jsonObject.optInt("wratio");
        bid.HRatio = jsonObject.optInt("hratio");
        bid.exp = jsonObject.optInt("exp", -1);

        JSONObject ext = jsonObject.optJSONObject("ext");
        if (ext != null) {
            Prebid prebidObject = Prebid.fromJSONObject(ext.optJSONObject("prebid"));
            setEvents(bid, prebidObject);
            bid.prebid = prebidObject;
            bid.mobileSdkPassThrough = MobileSdkPassThrough.create(ext);
            bid.rewardedExt = RewardedExtParser.parse(ext);
        }

        substituteMacros(bid);

        return bid;
    }

    public void setAdm(String adm) {
        this.adm = adm;
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

        bid.adm = MacrosResolutionHelper.resolveAuctionMacros(bid.adm, macrosModelMap);
        bid.nurl = MacrosResolutionHelper.resolveAuctionMacros(bid.nurl, macrosModelMap);
    }


    private static void setEvents(Bid bid, Prebid prebidObject) {
        HashMap<String, String> events = new HashMap<>();
        String winUrl = prebidObject.getWinEventUrl();
        if (winUrl != null) {
            events.put(BidInfo.EVENT_WIN, winUrl);
        }
        String impUrl = prebidObject.getImpEventUrl();
        if (impUrl != null) {
            events.put(BidInfo.EVENT_IMP, impUrl);
        }
        if (!events.isEmpty()) {
            bid.events = events;
        }
    }

    @NonNull
    public RewardedExt getRewardedExt() {
        return rewardedExt;
    }
}
