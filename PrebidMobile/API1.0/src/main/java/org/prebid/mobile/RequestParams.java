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

import java.util.ArrayList;
import java.util.HashSet;

class RequestParams {
    private String configId = "";
    private AdType adType = AdType.BANNER;
    private HashSet<AdSize> sizes = new HashSet<>();
    private ArrayList<String> keywords;

    RequestParams(String configId, AdType adType, HashSet<AdSize> sizes, ArrayList<String> keywords) {
        this.configId = configId;
        this.adType = adType;
        this.sizes = sizes; // for Interstitial this will be null, will use screen width & height in the request
        this.keywords = keywords;
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

    ArrayList<String> getKeywords() {
        return keywords;
    }

}
