/*
 *    Copyright 2018-2021 Prebid.org, Inc.
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

package org.prebid.mobile.rendering.video;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import androidx.annotation.Nullable;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.interstitial.rewarded.RewardedClosingRules;
import org.prebid.mobile.rendering.interstitial.rewarded.RewardedCompletionRules;
import org.prebid.mobile.rendering.interstitial.rewarded.RewardedExt;
import org.prebid.mobile.rendering.listeners.VideoCreativeViewListener;
import org.prebid.mobile.rendering.models.AbstractCreative;

import java.lang.ref.WeakReference;

/**
 * Created by matthew.rolufs on 9/1/15.
 */
public class AdViewProgressUpdateTask extends AsyncTask<Void, Long, Void> {

    private static String TAG = AdViewProgressUpdateTask.class.getSimpleName();
    private long current = 0;
    private WeakReference<View> creativeViewWeakReference;
    private long videoDuration;
    private VideoCreativeViewListener trackEventListener;
    private boolean start, firstQuartile, midpoint, thirdQuartile;
    private long vastVideoDuration = -1;
    private Handler mainHandler;
    private AdUnitConfiguration config;
    @Nullable
    private Integer percentageForReward;
    @Nullable
    private Integer autoCloseTime;

    private long lastTime;

    public AdViewProgressUpdateTask(
            VideoCreativeViewListener trackEventListener,
            int videoDuration,
            AdUnitConfiguration config
    ) throws AdException {
        if (trackEventListener == null) {
            throw new AdException(AdException.INTERNAL_ERROR, "VideoViewListener is null");
        }
        this.config = config;
        this.trackEventListener = trackEventListener;
        AbstractCreative creative = (AbstractCreative) trackEventListener;
        creativeViewWeakReference = new WeakReference<>(creative.getCreativeView());
        this.videoDuration = videoDuration;
        mainHandler = new Handler(Looper.getMainLooper());
        percentageForReward = getVideoLengthPercentageForReward(videoDuration, config);
        autoCloseTime = getAutoCloseTime();
    }

    @Override
    protected Void doInBackground(Void... params) {

        try {
            //evaluate a do while loop
            //every second determine current position of ad by duration and current position relative to that duration
            //update progress at particular intervals
            //until ad completes total duration
            //or until ad is cancelled or closed or destroyed which calls AsyncTask.cancel()
            do {
                if (System.currentTimeMillis() - lastTime >= 50) {
                    if (!isCancelled()) {
                        final View creativeView = creativeViewWeakReference.get();
                        if (creativeView instanceof VideoCreativeView) {

                            mainHandler.post(() -> {

                                try {
                                    VideoCreativeView videoCreativeView = (VideoCreativeView) creativeView;
                                    VideoPlayerView videoView = videoCreativeView.getVideoPlayerView();
                                    if (videoView != null) {
                                        // If the new current value is 0 while the old current value is > 0,
                                        // it means the video has ended so set the new current to the duration
                                        // to end the AsyncTask
                                        long newCurrent = videoView.getCurrentPosition();

                                        if (vastVideoDuration != -1 && newCurrent >= vastVideoDuration) {
                                            LogUtil.debug(
                                                    VideoCreativeView.class.getName(),
                                                    "VAST duration reached, video interrupted. VAST duration:" + vastVideoDuration + " ms, Video duration: " + videoDuration + " ms"
                                            );
                                            videoView.forceStop();
                                        }

                                        if (autoCloseTime != null && newCurrent >= autoCloseTime) {
                                            LogUtil.debug("Auto close time reached. Auto close time: " + autoCloseTime + " ms");
                                            videoView.forceStop();
                                        }

                                        if (newCurrent == 0 && current > 0) {
                                            current = videoDuration;
                                        } else {
                                            current = newCurrent;
                                        }
                                    }
                                }
                                catch (Exception e) {
                                    LogUtil.error(TAG, "Getting currentPosition from VideoCreativeView  failed: " + Log.getStackTraceString(e));
                                }
                            });
                        }

                        try {
                            if (videoDuration > 0) {
                                publishProgress((current * 100 / videoDuration), videoDuration);
                            }
                            if (current >= videoDuration) {
                                break;
                            }
                        } catch (Exception e) {
                            LogUtil.error(TAG, "Failed to publish video progress: " + Log.getStackTraceString(e));
                        }
                    }
                    lastTime = System.currentTimeMillis();
                }
            } while (current <= videoDuration && !isCancelled());
        }
        catch (Exception e) {
            LogUtil.error(TAG, "Failed to update video progress: " + Log.getStackTraceString(e));
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        cancel(true);
    }

    @Override
    protected void onProgressUpdate(Long... values) {
        if (isCancelled()) {
            return;
        }
        super.onProgressUpdate(values);

        Long completionPercentage = values[0];

        if (percentageForReward != null && completionPercentage >= percentageForReward) {
            config.getRewardManager().notifyRewardListener();
            percentageForReward = null;
        }

        if (!start && completionPercentage >= 1) {
            start = true;
        }

        if (!firstQuartile && completionPercentage >= 25) {
            LogUtil.debug(TAG, "firstQuartile: " + completionPercentage);
            firstQuartile = true;
            trackEventListener.onEvent(VideoAdEvent.Event.AD_FIRSTQUARTILE);
        }
        if (!midpoint && completionPercentage >= 50) {
            LogUtil.debug(TAG, "midpoint: " + completionPercentage);
            midpoint = true;
            trackEventListener.onEvent(VideoAdEvent.Event.AD_MIDPOINT);
        }
        if (!thirdQuartile && completionPercentage >= 75) {
            LogUtil.debug(TAG, "thirdQuartile: " + completionPercentage);
            thirdQuartile = true;
            trackEventListener.onEvent(VideoAdEvent.Event.AD_THIRDQUARTILE);
        }
    }

    public long getCurrentPosition() {
        return current;
    }

    public boolean getFirstQuartile() {
        return firstQuartile;
    }

    public boolean getMidpoint() {
        return midpoint;
    }

    public boolean getThirdQuartile() {
        return thirdQuartile;
    }

    public void setVastVideoDuration(long vastVideoDuration) {
        this.vastVideoDuration = vastVideoDuration;
    }


    /**
     * Returns video length percentage for receiving reward.
     */
    @Nullable
    protected static Integer getVideoLengthPercentageForReward(int videoDuration, AdUnitConfiguration config) {
        boolean hasEndCard = !config.isRewarded() || config.getHasEndCard();
        if (hasEndCard) {
            return null;
        }

        RewardedExt rewardedExt = config.getRewardManager().getRewardedExt();
        RewardedCompletionRules.PlaybackEvent playbackEvent = rewardedExt.getCompletionRules().getVideoEvent();
        if (playbackEvent != null) {
            if (playbackEvent == RewardedCompletionRules.PlaybackEvent.COMPLETE) {
                return 100;
            } else if (playbackEvent == RewardedCompletionRules.PlaybackEvent.THIRD_QUARTILE) {
                return 75;
            } else if (playbackEvent == RewardedCompletionRules.PlaybackEvent.MIDPOINT) {
                return 50;
            } else if (playbackEvent == RewardedCompletionRules.PlaybackEvent.FIRST_QUARTILE) {
                return 25;
            } else if (playbackEvent == RewardedCompletionRules.PlaybackEvent.START) {
                return 1;
            }
        }

        Integer secondsToReward = rewardedExt.getCompletionRules().getVideoTime();
        if (secondsToReward != null && videoDuration != 0) {
            int percentage = (int) (secondsToReward * 1000 / ((double) videoDuration) * 100);
            if (percentage > 100 || percentage < 0) {
                percentage = 100;
            }
            return percentage;
        }

        return null;
    }

    private Integer getAutoCloseTime() {
        boolean hasEndCard = !config.isRewarded() || config.getHasEndCard();
        if (hasEndCard) {
            return null;
        }

        RewardedExt rewardedExt = config.getRewardManager().getRewardedExt();
        RewardedClosingRules.Action action = rewardedExt.getClosingRules().getAction();
        if (action != RewardedClosingRules.Action.AUTO_CLOSE) {
            return null;
        }

        Integer percentageForReward = getVideoLengthPercentageForReward((int) videoDuration, config);
        if (percentageForReward == null) {
            return null;
        }

        int postRewardTime = rewardedExt.getClosingRules().getPostRewardTime() * 1000;
        double autoCloseTime = percentageForReward / 100.0 * videoDuration + postRewardTime;
        if (autoCloseTime > videoDuration) {
            return null;
        }

        return (int) autoCloseTime;
    }

}
