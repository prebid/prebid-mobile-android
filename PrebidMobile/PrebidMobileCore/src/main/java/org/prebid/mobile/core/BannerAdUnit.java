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

public class BannerAdUnit extends AdUnit {
    //region BannerAdUnit Constructor

    /**
     * Creates an ad unit object to fetch banner ads with the specified identifier
     *
     * @param code Unique identifier for an adUnit
     */
    public BannerAdUnit(String code, String configId) {
        super(code, configId);
    }
    //endregion

    //region BannerAdUnit Public APIs

    @Override
    public AdType getAdType() {
        return AdType.BANNER;
    }

    /**
     * Adds an ad size with width and height as specified
     *
     * @param width  width of the ad container
     * @param height height of the ad container
     */
    public void addSize(int width, int height) {
        sizes.add(new AdSize(width, height));
    }
    //endregion

}
