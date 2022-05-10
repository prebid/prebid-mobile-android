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
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.api.exceptions.AdException;
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
    private long duration;
    private VideoCreativeViewListener trackEventListener;
    private boolean firstQuartile, midpoint, thirdQuartile;
    private long vastVideoDuration = -1;
    private Handler mainHandler;

    private long lastTime;

    public AdViewProgressUpdateTask(
            VideoCreativeViewListener trackEventListener,
            int duration
    ) throws AdException {
        if (trackEventListener == null) {
            throw new AdException(AdException.INTERNAL_ERROR, "VideoViewListener is null");
        }
        this.trackEventListener = trackEventListener;
        AbstractCreative creative = (AbstractCreative) trackEventListener;
        creativeViewWeakReference = new WeakReference<>(creative.getCreativeView());
        this.duration = duration;
        mainHandler = new Handler(Looper.getMainLooper());
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
                                                    "VAST duration reached, video interrupted. VAST duration:" + vastVideoDuration + " ms, Video duration: " + duration + " ms"
                                            );
                                            videoView.forceStop();
                                        }

                                        if (newCurrent == 0 && current > 0) {
                                            current = duration;
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
                            if (duration > 0) {
                                publishProgress((current * 100 / duration), duration);
                            }
                            if (current >= duration) {
                                break;
                            }
                        } catch (Exception e) {
                            LogUtil.error(TAG, "Failed to publish video progress: " + Log.getStackTraceString(e));
                        }
                    }
                    lastTime = System.currentTimeMillis();
                }
            } while (current <= duration && !isCancelled());
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
        // PbLog.debug(TAG, "progress: " + values[0]);

        //TODO - uncomment when we have to show the countdown on video
        //trackEventListener.countdown(values[1]);

        if (!firstQuartile && values[0] >= 25) {
            LogUtil.debug(TAG, "firstQuartile: " + values[0]);
            firstQuartile = true;
            trackEventListener.onEvent(VideoAdEvent.Event.AD_FIRSTQUARTILE);
        }
        if (!midpoint && values[0] >= 50) {
            LogUtil.debug(TAG, "midpoint: " + values[0]);
            midpoint = true;
            trackEventListener.onEvent(VideoAdEvent.Event.AD_MIDPOINT);
        }
        if (!thirdQuartile && values[0] >= 75) {
            LogUtil.debug(TAG, "thirdQuartile: " + values[0]);
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
}
