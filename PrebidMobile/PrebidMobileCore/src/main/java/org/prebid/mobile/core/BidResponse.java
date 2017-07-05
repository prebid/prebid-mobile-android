/*
 *    Copyright 2016 APPNEXUS INC
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

import android.text.TextUtils;
import android.util.Pair;

import java.util.ArrayList;

/**
 * Class used to form bid response from response data.
 * It contains bid information like bidderCode, creative, CPM, etc.
 * BidManager Module will use the bid information to create custom keywords to pass to other ad servers.
 */
public class BidResponse {
    //region Class Variables
    private static final long DEFAULT_EXPIRY = 5 * 60 * 1000;
    private long expiryTime = DEFAULT_EXPIRY;
    private Double cpm;
    private String creative;
    private String bidderCode;
    private long createdTime;
    private ArrayList<Pair<String, String>> customKeywords = new ArrayList<Pair<String, String>>();
    //endregion

    //region Constructor
    public BidResponse(Double cpm, String creative) {
        this.createdTime = System.currentTimeMillis();
        this.cpm = cpm;
        this.creative = creative;
    }
    //endregion

    //region Public APIs

    /**
     * Get the CPM bid price of the response.
     *
     * @return bid price in CPM
     */
    public Double getCpm() {
        return cpm;
    }

    /**
     * Get the url that can return a creative.
     *
     * @return creative url
     */
    public String getCreative() {
        return creative;
    }

    /**
     * Get the custom keywords for customized BidManager targeting of Line items.
     *
     * @return an ArrayList of key value pairs
     */

    public ArrayList<Pair<String, String>> getCustomKeywords() {
        return customKeywords;
    }

    /**
     * Add a custom keyword for customized BidManager targeting of Line items.
     *
     * @param key   The key to add; this cannot be null or empty.
     * @param value The value to add; this cannot be null or empty.
     */
    public synchronized void addCustomKeyword(String key, String value) {
        if (TextUtils.isEmpty(key) || value == null)
            return;
        this.customKeywords.add(new Pair<String, String>(key, value));
    }

    /**
     * Set the expiration time. If method not used, will use the default value.
     *
     * @param expiryTime in milliseconds
     */
    public void setExpiryTime(long expiryTime) {
        this.expiryTime = expiryTime;
    }

    /**
     * For BidManager Module to check if the bid response is still valid.
     *
     * @return false if the bid has been around more then expiration time (in milliseconds)
     */
    public boolean isExpired() {
        return ((System.currentTimeMillis() - this.createdTime) >= expiryTime);
    }

    /**
     * Set the name of the bidder that responded this bid
     *
     * @param bidderCode bidder's name
     */
    public void setBidderCode(String bidderCode) {
        this.bidderCode = bidderCode;
    }
    //endregion


    //region Package only helper methods
    long getExpiryTime() {
        return expiryTime;
    }


    String getBidderCode() {
        return bidderCode;
    }

    @Override
    public String toString() {
        return "Bidder name: " + getBidderCode() + " | BidResponse Price: " + getCpm();
    }
    //endregion
}
