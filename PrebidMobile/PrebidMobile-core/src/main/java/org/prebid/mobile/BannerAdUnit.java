/*
 *    Copyright 2018-2019 Prebid.org, Inc.
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

package org.prebid.mobile;

import androidx.annotation.NonNull;

import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.api.data.AdUnitFormat;
import org.prebid.mobile.rendering.models.AdPosition;

import java.util.EnumSet;
import java.util.HashSet;

/**
 * Original API banner ad unit for displaying banner ad.
 */
public class BannerAdUnit extends BannerBaseAdUnit {

    /**
     * Default constructor for banner ad.
     *
     * @param configId config id
     * @param width    ad width
     * @param height   ad height
     */
    public BannerAdUnit(@NonNull String configId, int width, int height) {
        super(configId, EnumSet.of(AdFormat.BANNER));
        configuration.addSize(new AdSize(width, height));
    }

    /**
     * Constructor with ad formats.
     *
     * @param configId      config id
     * @param width         ad width
     * @param height        ad height
     * @param adUnitFormats ad formats ({@link AdUnitFormat}). <br>
     *                      For multi-format request {@code EnumSet.of(AdUnitFormat.BANNER, AdUnitFormat.VIDEO);} <br>
     *                      For only video request {@code EnumSet.of(AdUnitFormat.VIDEO);}
     */
    public BannerAdUnit(@NonNull String configId, int width, int height, EnumSet<AdUnitFormat> adUnitFormats) {
        super(configId, AdFormat.fromSet(adUnitFormats, false));
        configuration.addSize(new AdSize(width, height));
    }

    /**
     * Add additional size.
     */
    public void addAdditionalSize(int width, int height) {
        configuration.addSize(new AdSize(width, height));
    }

    HashSet<AdSize> getSizes() {
        return configuration.getSizes();
    }

    public void setAdPosition(AdPosition adPosition) {
        configuration.setAdPosition(adPosition);
    }

}
