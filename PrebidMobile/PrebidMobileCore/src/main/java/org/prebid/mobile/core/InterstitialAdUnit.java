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

import java.util.ArrayList;

public class InterstitialAdUnit extends AdUnit {
    private static ArrayList<AdSize> standardSizes = new ArrayList<>();

    static {
        standardSizes.add(new AdSize(300, 250));
        standardSizes.add(new AdSize(300, 600));
        standardSizes.add(new AdSize(320, 250));
        standardSizes.add(new AdSize(254, 133));
        standardSizes.add(new AdSize(580, 400));
        standardSizes.add(new AdSize(320, 320));
        standardSizes.add(new AdSize(320, 160));
        standardSizes.add(new AdSize(320, 480));
        standardSizes.add(new AdSize(336, 280));
        standardSizes.add(new AdSize(320, 400));
        standardSizes.add(new AdSize(1, 1));
    }

    public InterstitialAdUnit(String code, String configId) {
        super(code, configId);
    }

    @Override
    public AdType getAdType() {
        return AdType.INTERSTITIAL;
    }

    void setInterstitialSizes(Context context) {
        if (context != null) {
            int width = context.getResources().getDisplayMetrics().widthPixels;
            int height = context.getResources().getDisplayMetrics().heightPixels;
            for (AdSize size : standardSizes) {
                if (size.getWidth() <= width && size.getHeight() <= height) {
                    sizes.add(size);
                }
            }
        }
    }

}
