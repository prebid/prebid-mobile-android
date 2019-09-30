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

import android.support.annotation.Nullable;

import java.util.Map;

public final class VideoUtils {
    public static String buildAdTagUrl(String adUnitId, @Nullable String adSlotSize, Map<String, String> targeting) {

        StringBuilder keywordsBuilder = new StringBuilder();
        for (String key : targeting.keySet()) {
            keywordsBuilder.append(key).append("%3D").append(targeting.get(key)).append("%26");
        }

        if (keywordsBuilder.length() > 0) {
            keywordsBuilder.delete(keywordsBuilder.length() - 3, keywordsBuilder.length());
        }

        String adTagUrl = buildAdTagUrl(adUnitId, adSlotSize, keywordsBuilder.toString());

        return adTagUrl;
    }

    //https://support.google.com/admanager/answer/1068325?hl=en
    static String buildAdTagUrl(String adUnitId, @Nullable String adSlotSize, @Nullable String customParams) {
        String currentMillis = String.valueOf(System.currentTimeMillis());

        StringBuilder adTagUrl = new StringBuilder();

        adTagUrl.append("https://pubads.g.doubleclick.net/gampad/ads?");
                        //Required parameters
        adTagUrl.append("env=vp"); //vp Indicates that the request is from a video player.
        adTagUrl.append("&gdfp_req=1"); //Indicates that the user is on the Ad Manager schema.
        adTagUrl.append("&unviewed_position_start=1"); //    Setting this to 1 turns on delayed impressions for video.
                        //Required parameters with variable values
        adTagUrl.append("&output=xml_vast4"); //Output format of ad
        adTagUrl.append("&vpmute=1"); //Indicates whether the ad playback starts while the video player is muted.
        adTagUrl.append("&iu=").append(adUnitId);

        if (adSlotSize != null) {
            adTagUrl.append("&sz=").append(adSlotSize); //Size of master video ad slot. Multiple sizes should be separated by the pipe (|) character.
        }

        if (customParams != null) {
            adTagUrl.append("&cust_params=").append(customParams);
        }

        adTagUrl.append("&correlator=").append(currentMillis);

        return adTagUrl.toString();
    }

}
