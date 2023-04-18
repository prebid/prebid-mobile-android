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

import java.util.EnumSet;
import java.util.HashSet;

public class BannerAdUnit extends BannerBaseAdUnit {

    public BannerAdUnit(@NonNull String configId, int width, int height) {
        super(configId, EnumSet.of(AdFormat.BANNER));
        configuration.addSize(new AdSize(width, height));
    }

    /**
     * Constructor for multi-format or video banner request.
     *
     * @param adUnitFormats <p>
     *                      for multi-format request `EnumSet.of(AdUnitFormat.DISPLAY, AdUnitFormat.VIDEO);`
     *                      <p>
     *                      for video banner `EnumSet.of(AdUnitFormat.VIDEO);`
     */
    public BannerAdUnit(@NonNull String configId, int width, int height, EnumSet<AdUnitFormat> adUnitFormats) {
        super(configId, AdFormat.fromSet(adUnitFormats, false));
        configuration.addSize(new AdSize(width, height));
    }

    public void addAdditionalSize(int width, int height) {
        configuration.addSize(new AdSize(width, height));
    }

    HashSet<AdSize> getSizes() {
        return configuration.getSizes();
    }

}
