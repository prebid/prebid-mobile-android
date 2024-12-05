package org.prebid.mobile.rendering.interstitial.rewarded;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.prebid.mobile.test.utils.ResourceUtils;

import static org.junit.Assert.*;

public class RewardedExtParserTest {

    @Test
    public void parse_emptyRewardedJson_default() throws JSONException {
        String json = "{}";
        JSONObject jsonObject = new JSONObject(json);

        RewardedExt ext = RewardedExtParser.parse(jsonObject);

        // Reward
        assertNull(ext.getReward());

        // Completion rules
        RewardedCompletionRules completionRules = ext.getCompletionRules();
        assertNotNull(completionRules);

        assertEquals(120, completionRules.getBannerTime());
        assertEquals(120, completionRules.getEndCardTime());
        assertNull(completionRules.getVideoTime());

        assertNull(completionRules.getVideoEvent());
        assertNull(completionRules.getBannerEvent());
        assertNull(completionRules.getEndCardEvent());

        // Closing rules
        RewardedClosingRules closingRules = ext.getClosingRules();
        assertNotNull(closingRules);

        assertEquals(0, closingRules.getPostRewardTime());
        assertEquals(RewardedClosingRules.Action.CLOSE_BUTTON, closingRules.getAction());
    }

    @Test
    public void parse_fullRewardedJson_fromJson() throws Throwable {
        String json = ResourceUtils.convertResourceToString("RewardedExt/full.json");
        JSONObject jsonObject = new JSONObject(json);
        RewardedExt ext = RewardedExtParser.parse(jsonObject);

        // Reward
        Reward reward = ext.getReward();
        assertNotNull(reward);
        assertEquals(11, reward.getCount());
        assertEquals("SuperDollars", reward.getType());
        assertNull(reward.getExt());

        // Completion rules
        RewardedCompletionRules completionRules = ext.getCompletionRules();
        assertNotNull(completionRules);

        assertEquals(12, completionRules.getBannerTime());
        assertEquals(Integer.valueOf(13), completionRules.getVideoTime());
        assertEquals(14, completionRules.getEndCardTime());

        assertEquals("banner_event", completionRules.getBannerEvent());
        assertEquals(RewardedCompletionRules.PlaybackEvent.COMPLETE, completionRules.getVideoEvent());
        assertEquals("endcard_event", completionRules.getEndCardEvent());

        // Closing rules
        RewardedClosingRules closingRules = ext.getClosingRules();
        assertNotNull(closingRules);

        assertEquals(15, closingRules.getPostRewardTime());
        assertEquals(RewardedClosingRules.Action.AUTO_CLOSE, closingRules.getAction());
    }


    @Test
    public void parse_rewardWithExt() throws Throwable {
        String json = ResourceUtils.convertResourceToString("RewardedExt/reward_with_ext.json");
        JSONObject jsonObject = new JSONObject(json);
        RewardedExt ext = RewardedExtParser.parse(jsonObject);

        // Reward
        Reward reward = ext.getReward();
        assertNotNull(reward);
        assertEquals(11, reward.getCount());
        assertEquals("SuperDollars", reward.getType());
        assertNotNull(reward.getExt());
        assertEquals("{\"additional_data\":\"test\"}", reward.getExt().toString());
    }

    @Test
    public void parse_closeAction_autoClose() throws Throwable {
        String json = ResourceUtils.convertResourceToString("RewardedExt/close_type_1.json");
        JSONObject jsonObject = new JSONObject(json);
        RewardedExt ext = RewardedExtParser.parse(jsonObject);

        assertEquals(RewardedClosingRules.Action.AUTO_CLOSE, ext.getClosingRules().getAction());
    }

    @Test
    public void parse_closeAction_closeButton() throws Throwable {
        String json = ResourceUtils.convertResourceToString("RewardedExt/close_type_2.json");
        JSONObject jsonObject = new JSONObject(json);
        RewardedExt ext = RewardedExtParser.parse(jsonObject);

        assertEquals(RewardedClosingRules.Action.CLOSE_BUTTON, ext.getClosingRules().getAction());
    }

    @Test
    public void parse_playbackEvent_start() throws Throwable {
        String json = ResourceUtils.convertResourceToString("RewardedExt/playbackevent_start.json");
        JSONObject jsonObject = new JSONObject(json);
        RewardedExt ext = RewardedExtParser.parse(jsonObject);

        assertEquals(RewardedCompletionRules.PlaybackEvent.START, ext.getCompletionRules().getVideoEvent());
    }

    @Test
    public void parse_playbackEvent_firstQuartile() throws Throwable {
        String json = ResourceUtils.convertResourceToString("RewardedExt/playbackevent_firstquartile.json");
        JSONObject jsonObject = new JSONObject(json);
        RewardedExt ext = RewardedExtParser.parse(jsonObject);

        assertEquals(RewardedCompletionRules.PlaybackEvent.FIRST_QUARTILE, ext.getCompletionRules().getVideoEvent());
    }

    @Test
    public void parse_playbackEvent_midpoint() throws Throwable {
        String json = ResourceUtils.convertResourceToString("RewardedExt/playbackevent_midpoint.json");
        JSONObject jsonObject = new JSONObject(json);
        RewardedExt ext = RewardedExtParser.parse(jsonObject);

        assertEquals(RewardedCompletionRules.PlaybackEvent.MIDPOINT, ext.getCompletionRules().getVideoEvent());
    }

    @Test
    public void parse_playbackEvent_thirdQuartile() throws Throwable {
        String json = ResourceUtils.convertResourceToString("RewardedExt/playbackevent_thirdquartile.json");
        JSONObject jsonObject = new JSONObject(json);
        RewardedExt ext = RewardedExtParser.parse(jsonObject);

        assertEquals(RewardedCompletionRules.PlaybackEvent.THIRD_QUARTILE, ext.getCompletionRules().getVideoEvent());
    }

    @Test
    public void parse_playbackEvent_complete() throws Throwable {
        String json = ResourceUtils.convertResourceToString("RewardedExt/playbackevent_complete.json");
        JSONObject jsonObject = new JSONObject(json);
        RewardedExt ext = RewardedExtParser.parse(jsonObject);

        assertEquals(RewardedCompletionRules.PlaybackEvent.COMPLETE, ext.getCompletionRules().getVideoEvent());
    }
}