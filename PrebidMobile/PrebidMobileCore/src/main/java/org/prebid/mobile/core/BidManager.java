/*
 *    Copyright 2016 Prebid.org, Inc.
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

package org.prebid.mobile.core;

import android.content.Context;
import android.os.Handler;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BidManager class manages the auctions and caches bid responses for registered adUnits.
 * Behavior description:
 * 1. On start, make initial requests for a list of ad units
 * 2. When a cached bid is retrieved, request a new bid for the ad unit
 * 3. If existing bid is expired, request a new bid for the ad unit
 */
public class BidManager {
    //region Class Variables
    private static final String TAG;
    private static Runnable expireBids;
    private static Handler handler = new Handler();
    private static long periodToCheckExpiration = 30000; // 30 seconds by default
    static DemandAdapter adapter;

    static {
        TAG = LogUtil.getTagWithBase("BidManager");

    }

    // data structure to keep all the registered ad units
    private static Set<AdUnit> adUnits = new HashSet<AdUnit>();
    // data structure to keep all the responded bids
    private static ConcurrentHashMap<String, ArrayList<BidResponse>> bidMap = new ConcurrentHashMap<String, ArrayList<BidResponse>>();
    //endregion

    //region BidManager Configuration
    static void registerAdUnit(AdUnit adUnit) {
        adUnits.add(adUnit);
    }

    static void setBidsExpirationRunnable(final Context context) {
        if (expireBids == null) {
            // check every 30 seconds that if there is any ad unit that all bids are expired
            expireBids = new Runnable() {
                @Override
                public void run() {
                    if (bidMap != null && !bidMap.isEmpty() && context != null) {
                        long currentTime = System.currentTimeMillis();
                        for (String code : bidMap.keySet()) {
                            AdUnit adUnit = getAdUnitByCode(code);
                            if (adUnit.shouldExpireAllBids(currentTime)) {
                                startNewAuction(context, adUnit);
                            }
                        }
                    }
                    handler.postDelayed(expireBids, periodToCheckExpiration);
                }
            };
            handler.postDelayed(expireBids, periodToCheckExpiration);
        }
    }
    //endregion

    //region Auction Logic Handling

    /**
     * Fetches bids for all the ad slots
     *
     * @param context Application context
     */
    static void requestBidsForAdUnits(Context context, ArrayList<AdUnit> adUnits) {
        TargetingParams.fetchLocationUpdates(context);
        // send requests for a list of AdUnits
        if (adapter != null) {
            adapter.requestBid(context, bidResponseListener, adUnits);
        }
    }

    /**
     * Starts the new auction for the given adUnit configuration
     *
     * @param context Application context
     * @param adUnit  ad slot configuration
     */
    static void startNewAuction(Context context, AdUnit adUnit) {
        if (adUnit != null && adUnits.contains(adUnit)) {
            LogUtil.v(TAG, "New Auction run for AdUnit: " + adUnit.getCode());
            // generate new auction id for the given ad slot
            adUnit.generateNewAuctionId();

            // Clear the bid responses for the given ad slot
            bidMap.remove(adUnit.getCode());

            // fetch bids for given ad slot
            ArrayList<AdUnit> adUnits = new ArrayList<AdUnit>();
            adUnits.add(adUnit);
            requestBidsForAdUnits(context, adUnits);
        }
    }

    //endregion

    //region Auction Response Saving

    /**
     * Listener that listens to the responses from demand sources
     */
    public interface BidResponseListener {
        void onBidSuccess(AdUnit bidRequest, ArrayList<BidResponse> bidResponses); // TODO should this be passing back String name if we're not using auction id?

        void onBidFailure(AdUnit bidRequest, ErrorCode reason);
    }

    private static BidResponseListener bidResponseListener = new BidResponseListener() {
        @Override
        public void onBidSuccess(AdUnit adUnit, ArrayList<BidResponse> bidResponses) {
            //First iterate through the List of AdUnits.
            /**
             * Check if the auctionId of response (BidRequest) matches with AdUnit's auctionId.
             * If server responds back after some time and meanwhile application has displayed the ad from the winning bidResponse,
             * then that auction will be considered over and a new auction id would be generated,
             * which will not match with the response and therefore bidResponse will be ignored.
             */
            LogUtil.i(TAG, "Bid Successful for adUnitId: " + adUnit.getCode());
            for (BidResponse bidResponse : bidResponses) {
                adUnit.setTimeThatShouldExpireAllBids(System.currentTimeMillis() + bidResponse.getExpiryTime());
            }
            bidMap.remove(adUnit.getCode());
            // save the bids sorted
            Collections.sort(bidResponses, new BidComparator());
            if (Prebid.getAdServer() == Prebid.AdServer.DFP) {
                BidResponse topBid = bidResponses.get(0);
                if (topBid != null) {
                    topBid.addCustomKeyword("hb_cache_id", topBid.getCreative());
                }
            }
            bidMap.put(adUnit.getCode(), bidResponses);
        }


        @Override
        public void onBidFailure(AdUnit adUnit, ErrorCode reason) {
            //First iterate through the List of AdUnits.
            LogUtil.e(TAG, "Request failed because of " + reason.toString().toLowerCase(Locale.getDefault()) + " for AdUnit code: " + adUnit.getCode());
        }
    };

    private static class BidComparator implements Comparator<BidResponse> {
        @Override
        public int compare(BidResponse firstBid, BidResponse secondBid) {
            if (firstBid == null || secondBid == null) {
                return 0;
            }
            return secondBid.getCpm().compareTo(firstBid.getCpm());
        }
    }

    //endregion

    //region Auction Response Retrieval

    /**
     * Checks if the Auction has completed for a given adUnit.
     *
     * @param adUnitCode The code of AdUnit for which we want to check auction Completed.
     * @return true if the auction is complete false otherwise.
     */
    private static boolean isBidReady(String adUnitCode) {
        // TODO think deeper about whether to check bid expiration here, since we already have an expiration check mechanism, can we assume that the bids in the map are always valid?
        return bidMap.keySet().contains(adUnitCode) && (bidMap.get(adUnitCode) != null) && !bidMap.get(adUnitCode).isEmpty();
    }

    /**
     * Returns the ad slot object by using the code
     *
     * @param code ad slot identifier
     * @return AdUnit
     */
    static AdUnit getAdUnitByCode(String code) {
        for (AdUnit adUnit : adUnits) {
            if (adUnit.getCode() != null && adUnit.getCode().equals(code)) {
                return adUnit;
            }
        }
        return null;
    }

    /**
     * Returns the winning bid for a given ad unit code
     *
     * @param adUnitCode code for the ad unit
     * @return winning bid
     */
    static ArrayList<BidResponse> getWinningBids(String adUnitCode) {
        if (adUnitCode == null) return null;

        ArrayList<BidResponse> responses = bidMap.get(adUnitCode);
        if (responses != null && !responses.isEmpty()) {
            return responses;
        }
        return null;
    }

    /**
     * Returns a list of keywords with the given bidResponse
     *
     * @param adUnitCode BidResponse object to be passed
     * @return ArrayList list of keywords
     */
    protected static ArrayList<Pair<String, String>> getKeywordsForAdUnit(String adUnitCode, Context context) {
        AdUnit adUnit = getAdUnitByCode(adUnitCode);
        if (adUnit == null) {
            LogUtil.e(TAG, String.format("AdUnit for code %s is not registered, no bid available.", adUnitCode));
            return null;
        }
        ArrayList<BidResponse> bidResponses = null;
        if (isBidReady(adUnitCode)) {
            bidResponses = getWinningBids(adUnitCode);
        }
        startNewAuction(context, adUnit);
        if (bidResponses != null) {
            ArrayList<Pair<String, String>> keywordsPairs = new ArrayList<Pair<String, String>>();
            for (BidResponse bid : bidResponses) {
                for (Pair<String, String> keywords : bid.getCustomKeywords()) {
                    keywordsPairs.add(keywords);
                }
            }
            return keywordsPairs;
        }
        return null;
    }


    protected interface BidReadyListener {

        void onBidReady(String adUnitCode);
    }

    protected static void getKeywordsWhenReadyForAdUnit(final String adUnitCode, final int timeOut, final BidReadyListener listener) {
        if (listener != null) {
            final long startTime = System.currentTimeMillis();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    // Auction is complete attach the top bid and fire the completed listener.
                    if (isBidReady(adUnitCode)) {
                        LogUtil.i(TAG, String.format(Locale.ENGLISH, "Top bid ready within %d ms", (System.currentTimeMillis() - startTime)));
                        listener.onBidReady(adUnitCode);
                    }
                    // Auction is not completed but timeout has occured send failure notification.
                    else if (System.currentTimeMillis() - startTime >= timeOut) {
                        LogUtil.i(TAG, String.format(Locale.ENGLISH, "Auction didn't complete within %d ms, trying to attach available top bid.", timeOut));
                        listener.onBidReady(adUnitCode);
                    }
                    // Auction is not complete and timeout has not occured re-run the runnable with the said time delay.
                    else {
                        handler.postDelayed(this, 50);
                    }
                }
            });
        }
    }

    //endregion

    //region Methods for testing purpose
    protected static void reset() {
        if (adUnits != null)
            adUnits.clear();
        if (bidMap != null)
            bidMap.clear();
        if (expireBids != null) {
            handler.removeCallbacks(expireBids);
            expireBids = null;
        }
    }

    static void refreshBids(Context context) {
        if (bidMap != null) {
            bidMap.clear();
        }
        ArrayList<AdUnit> toBeRequested = new ArrayList<AdUnit>();
        if (adUnits != null) {
            for (AdUnit adUnit : adUnits) {
                toBeRequested.add(adUnit);
            }
        }
        requestBidsForAdUnits(context, toBeRequested);
    }

    static void setPeriodToCheckExpiration(long period) {
        periodToCheckExpiration = period;
    }

    static ConcurrentHashMap<String, ArrayList<BidResponse>> getBidMap() {
        return bidMap;
    } // TODO check where this is being used, delete when confirm not being used
    //endregion
}
