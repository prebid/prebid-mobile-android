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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class RequestParams {

    private String configId = "";
    private AdType adType = AdType.BANNER;
    private HashSet<AdSize> sizes = new HashSet<>();

    @Nullable
    private Map<String, Set<String>> contextDataDictionary;
    @Nullable
    private Set<String> contextKeywordsSet;

    @Nullable
    private AdSize minSizePerc; //non null only for InterstitialAdUnit(String, int, int)

    RequestParams(String configId, AdType adType, HashSet<AdSize> sizes) {
        this.configId = configId;
        this.adType = adType;
        this.sizes = sizes; // for Interstitial this will be null, will use screen width & height in the request
    }

    RequestParams(String configId, AdType adType, HashSet<AdSize> sizes, @Nullable Map<String, Set<String>> contextDataDictionary, @Nullable Set<String> contextKeywordsSet, @Nullable AdSize minSizePerc) {
        this(configId, adType, sizes);
        this.contextDataDictionary = contextDataDictionary;
        this.contextKeywordsSet = contextKeywordsSet;
        this.minSizePerc = minSizePerc;
    }

    String getConfigId() {
        return this.configId;
    }

    AdType getAdType() {
        return this.adType;
    }

    HashSet<AdSize> getAdSizes() {
        return this.sizes;
    }

    @NonNull
    public Map<String, Set<String>> getContextDataDictionary() {
        return contextDataDictionary != null ? contextDataDictionary : new HashMap<String, Set<String>>();
    }

    @NonNull
    public Set<String> getContextKeywordsSet() {
        return contextKeywordsSet != null ? contextKeywordsSet : new HashSet<String>();
    }

    @Nullable
    AdSize getMinSizePerc() {
        return minSizePerc;
    }
}
