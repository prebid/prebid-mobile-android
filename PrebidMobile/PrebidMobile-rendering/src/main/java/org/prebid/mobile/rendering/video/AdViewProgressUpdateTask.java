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

import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.listeners.VideoCreativeViewListener;
import org.prebid.mobile.rendering.models.AbstractCreative;
import org.prebid.mobile.rendering.utils.logger.LogUtil;

import java.lang.ref.WeakReference;

/**
 * Created by matthew.rolufs on 9/1/15.
 */
public class AdViewProgressUpdateTask extends AsyncTask<Void, Long, Void> {
    private static String TAG = AdViewProgressUpdateTask.class.getSimpleName();
    private long mCurrent = 0;
    private WeakReference<View> mCreativeViewWeakReference;
    private long mDuration;
    private VideoCreativeViewListener mTrackEventListener;
    private boolean mFirstQuartile, mMidpoint, mThirdQuartile;
    private long mVastVideoDuration = -1;
    private Handler mMainHandler;

    private long mLastTime;

    public AdViewProgressUpdateTask(VideoCreativeViewListener trackEventListener, int duration)
    throws AdException {
        if (trackEventListener == null) {
            throw new AdException(AdException.INTERNAL_ERROR, "VideoViewListener is null");
        }
        mTrackEventListener = trackEventListener;
        AbstractCreative creative = (AbstractCreative) trackEventListener;
        mCreativeViewWeakReference = new WeakReference<>(creative.getCreativeView());
        mDuration = duration;
        mMainHandler = new Handler(Looper.getMainLooper());
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
                if (System.currentTimeMillis() - mLastTime >= 50) {
                    if (!isCancelled()) {
                        final View creativeView = mCreativeViewWeakReference.get();
                        if (creativeView instanceof VideoCreativeView) {

                            mMainHandler.post(() -> {

                                try {
                                    VideoCreativeView videoCreativeView = (VideoCreativeView) creativeView;
                                    VideoPlayerView videoView = videoCreativeView.getVideoPlayerView();
                                    if (videoView != null) {
                                        // If the new current value is 0 while the old current value is > 0,
                                        // it means the video has ended so set the new current to the duration
                                        // to end the AsyncTask
                                        long newCurrent = videoView.getCurrentPosition();

                                        if (mVastVideoDuration != -1 && newCurrent >= mVastVideoDuration) {
                                            LogUtil.debug(VideoCreativeView.class.getName(), "VAST duration reached, video interrupted. VAST duration:" + mVastVideoDuration + " ms, Video duration: " + mDuration + " ms");
                                            videoView.forceStop();
                                        }

                                        if (newCurrent == 0 && mCurrent > 0) {
                                            mCurrent = mDuration;
                                        }
                                        else {
                                            mCurrent = newCurrent;
                                        }
                                    }
                                }
                                catch (Exception e) {
                                    LogUtil.error(TAG, "Getting currentPosition from VideoCreativeView  failed: " + Log.getStackTraceString(e));
                                }
                            });
                        }

                        try {
                            if (mDuration > 0) {
                                publishProgress((mCurrent * 100 / mDuration), mDuration);
                            }
                            if (mCurrent >= mDuration) {
                                break;
                            }
                        }
                        catch (Exception e) {
                            LogUtil.error(TAG, "Failed to publish video progress: " + Log.getStackTraceString(e));
                        }
                    }
                    mLastTime = System.currentTimeMillis();
                }
            }
            while (mCurrent <= mDuration && !isCancelled());
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

        if (!mFirstQuartile && values[0] >= 25) {
            LogUtil.debug(TAG, "firstQuartile: " + values[0]);
            mFirstQuartile = true;
            mTrackEventListener.onEvent(VideoAdEvent.Event.AD_FIRSTQUARTILE);
        }
        if (!mMidpoint && values[0] >= 50) {
            LogUtil.debug(TAG, "midpoint: " + values[0]);
            mMidpoint = true;
            mTrackEventListener.onEvent(VideoAdEvent.Event.AD_MIDPOINT);
        }
        if (!mThirdQuartile && values[0] >= 75) {
            LogUtil.debug(TAG, "thirdQuartile: " + values[0]);
            mThirdQuartile = true;
            mTrackEventListener.onEvent(VideoAdEvent.Event.AD_THIRDQUARTILE);
        }
    }

    public long getCurrentPosition() {
        return mCurrent;
    }

    public boolean getFirstQuartile() {
        return mFirstQuartile;
    }

    public boolean getMidpoint() {
        return mMidpoint;
    }

    public boolean getThirdQuartile() {
        return mThirdQuartile;
    }

    public void setVastVideoDuration(long vastVideoDuration) {
        mVastVideoDuration = vastVideoDuration;
    }
}
