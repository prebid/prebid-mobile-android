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

import java.util.ArrayList;
import java.util.UUID;

/**
 * AdUnit class defines the configuration for auction.
 */
public abstract class AdUnit {
    //region Class variables
    protected String auctionId;
    protected String code; // Unique code for AdUnit set by user.
    protected String configId; // Config Id for prebid server
    protected ArrayList<AdSize> sizes;
    //endregion

    //region Constructors

    /**
     * Creates an ad unit object with the specified identifier
     *
     * @param code Unique config id for an adUnit
     */
    AdUnit(String code, String configId) {
        this.code = code;
        this.configId = configId;
        sizes = new ArrayList<AdSize>();
        generateNewAuctionId();
    }
    //endregion

    //region Public APIs


    public String getConfigId() {
        return configId;
    }

    /**
     * Gets the identifier for the ad unit
     *
     * @return code
     */
    public String getCode() {
        return code;
    }

    /**
     * Gets the auctionId which is auto generated when ad unit is created
     *
     * @return auctionId
     */
    public String getAuctionId() {
        return auctionId;
    }


    /**
     * Gets the supported sizes of the ad unit
     *
     * @return sizes
     */
    public ArrayList<AdSize> getSizes() {
        return sizes;
    }

    /**
     * Gets the type that how the ad will be displayed
     * Choices are banner, interstitial, native
     *
     * @return adType
     */
    public abstract AdType getAdType();

    //endregion

    //region Package only methods for internal use

    void generateNewAuctionId() {
        this.auctionId = UUID.randomUUID().toString();
    }


    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof AdUnit) {
            String otherAdSlotCode = ((AdUnit) o).getCode();
            String otherAdSlotConfig = ((AdUnit) o).getConfigId();
            if (getCode() != null && otherAdSlotCode != null) {
                return getCode().equalsIgnoreCase(otherAdSlotCode) && getConfigId().equalsIgnoreCase(otherAdSlotConfig);
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (code == null || configId == null) {
            return super.hashCode();
        }
        return (code + configId).hashCode();
    }

    @Override
    public String toString() {
        return "code: " + getCode() +
                " config: " + getConfigId() +
                " sizes: " + getSizesString();
    }


    private String getSizesString() {
        String output = "";
        for (AdSize size : sizes)
            output = output.concat("Width: " + size.getWidth() + " Height: " + size.getHeight());
        return output;
    }

    private long timeToExpireAllBids = 0;

    void setTimeThatShouldExpireAllBids(long millis) {
        if (millis > timeToExpireAllBids) {
            timeToExpireAllBids = millis;
        }
    }

    boolean shouldExpireAllBids(long currentTime) {
        if (currentTime > timeToExpireAllBids) {
            return true;
        }
        return false;
    }

    //endregion
}
