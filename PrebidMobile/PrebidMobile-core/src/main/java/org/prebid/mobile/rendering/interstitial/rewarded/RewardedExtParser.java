/*
 * © 2024 SMARTYADS. LDA doing business as “TEQBLAZE”.
 * All rights reserved. You may not use this file except in  compliance with the applicable  license granted to
 * you  by SMARTYADS,  LDA doing business as “TEQBLAZE”  (the "License"). Unless required by applicable law or
 * agreed to in writing, software distributed under the  License is distributed on  an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either  express or implied. Specific authorizations and restrictions
 * shall be provided for in the License.
 */

package org.prebid.mobile.rendering.interstitial.rewarded;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.prebid.mobile.LogUtil;


/**
 * Parser for rewarded ext JSON object. {@link RewardedExt}
 */
public class RewardedExtParser {

    private static final String TAG = "RewardedExtParser";

    private RewardedExtParser() {
    }

    @NonNull
    public static RewardedExt parse(@Nullable JSONObject bidExtJson) {
        JSONObject rootRewardedJson = getRootRewardedJson(bidExtJson);
        if (rootRewardedJson == null) return RewardedExt.defaultExt();

        JSONObject rewardJson = rootRewardedJson.optJSONObject("reward");
        Reward reward = parseReward(rewardJson);

        JSONObject completionJson = rootRewardedJson.optJSONObject("completion");
        RewardedCompletionRules completionRules = parseCompletionRules(completionJson);

        JSONObject closingJson = rootRewardedJson.optJSONObject("close");
        RewardedClosingRules closingRules = parseClosingRules(closingJson);

        return new RewardedExt(reward, completionRules, closingRules);
    }

    @Nullable
    private static JSONObject getRootRewardedJson(@Nullable JSONObject bidExtJson) {
        if (bidExtJson == null) return null;

        JSONObject prebidJson = bidExtJson.optJSONObject("prebid");
        if (prebidJson == null) return null;

        JSONArray passThroughArray = prebidJson.optJSONArray("passthrough");
        if (passThroughArray == null || passThroughArray.length() < 1) return null;

        for (int i = 0; i < passThroughArray.length(); i++) {
            JSONObject passThroughObject = passThroughArray.optJSONObject(i);
            if (passThroughObject == null) continue;

            String type = passThroughObject.optString("type");
            if (type.equals("prebidmobilesdk")) {
                return passThroughObject.optJSONObject("rwdd");
            }
        }
        return null;
    }


    @Nullable
    private static Reward parseReward(@Nullable JSONObject rewardJson) {
        if (rewardJson == null) {
            LogUtil.warning(TAG, "No 'reward' object for the rewarded ad.");
            return null;
        }

        String type = rewardJson.optString("type");
        int count = rewardJson.optInt("count", -1);

        if (type.isEmpty() || count < 0) {
            LogUtil.warning(TAG, "No required fields (type, count) in `reward` object for the rewarded ad.");
            return null;
        }

        return new Reward(type, count, rewardJson.optJSONObject("ext"));
    }

    @NonNull
    private static RewardedCompletionRules parseCompletionRules(@Nullable JSONObject completionJson) {
        if (completionJson == null) return new RewardedCompletionRules();

        Integer bannerTime = null;
        Integer videoTime = null;
        Integer endCardTime = null;
        String bannerEvent = null;
        RewardedCompletionRules.PlaybackEvent videoEvent = null;
        String endCardEvent = null;

        JSONObject bannerJson = completionJson.optJSONObject("banner");
        if (bannerJson != null) {
            int time = bannerJson.optInt("time", -1);
            if (time > -1) {
                bannerTime = time;
            }

            String event = bannerJson.optString("event");
            if (!event.isEmpty()) {
                bannerEvent = event;
            }
        }

        JSONObject videoJson = completionJson.optJSONObject("video");
        if (videoJson != null) {
            int time = videoJson.optInt("time", -1);
            if (time > -1) {
                videoTime = time;
            }

            String event = videoJson.optString("playbackevent");
            if (!event.isEmpty()) {
                videoEvent = RewardedCompletionRules.PlaybackEvent.fromString(event);
            }

            JSONObject endCardJson = videoJson.optJSONObject("endcard");
            if (endCardJson != null) {
                int cardTime = endCardJson.optInt("time", -1);
                if (cardTime > -1) {
                    endCardTime = cardTime;
                }

                String cardEvent = endCardJson.optString("event");
                if (!cardEvent.isEmpty()) {
                    endCardEvent = cardEvent;
                }
            }
        }

        return new RewardedCompletionRules(bannerTime, videoTime, endCardTime, bannerEvent, videoEvent, endCardEvent);
    }

    @NonNull
    private static RewardedClosingRules parseClosingRules(@Nullable JSONObject closingJson) {
        if (closingJson == null) return new RewardedClosingRules();

        Integer postRewardTime = null;
        RewardedClosingRules.Action action = null;

        int time = closingJson.optInt("postrewardtime", -1);
        if (time > -1) {
            postRewardTime = time;
        }

        String actionString = closingJson.optString("action");
        if (!actionString.isEmpty()) {
            if (actionString.equals("closebutton")) {
                action = RewardedClosingRules.Action.CLOSE_BUTTON;
            }
            if (actionString.equals("autoclose")) {
                action = RewardedClosingRules.Action.AUTO_CLOSE;
            }
        }

        return new RewardedClosingRules(postRewardTime, action);
    }

}
