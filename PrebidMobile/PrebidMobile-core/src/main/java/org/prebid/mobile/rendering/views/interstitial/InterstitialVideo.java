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

package org.prebid.mobile.rendering.views.interstitial;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import org.prebid.mobile.LogUtil;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.core.R;
import org.prebid.mobile.rendering.interstitial.AdBaseDialog;
import org.prebid.mobile.rendering.models.InterstitialDisplayPropertiesInternal;
import org.prebid.mobile.rendering.utils.helpers.InsetsUtils;
import org.prebid.mobile.rendering.utils.helpers.Utils;
import org.prebid.mobile.rendering.views.base.BaseAdView;
import org.prebid.mobile.rendering.views.webview.mraid.Views;

import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("NewApi")
//Interstitial video
public class InterstitialVideo extends AdBaseDialog {

    private static final String TAG = InterstitialVideo.class.getSimpleName();

    private static final int CLOSE_DELAY_DEFAULT_IN_MS = 10 * 1000;
    private static final int CLOSE_DELAY_MAX_IN_MS = 30 * 1000;

    private boolean useSkipButton = false;
    private boolean hasEndCard = false;
    private boolean isRewarded = false;

    //Leaving context here for testing
    //Reason:
    // "If these are pure JVM unit tests (i.e. run on your computer's JVM and not on an Android emulator/device), then you have no real implementations of methods on any Android classes.
    // You are using a mockable jar which just contains empty classes and methods with "final" removed so you can mock them, but they don't really work like when running normal Android."
    private final WeakReference<Context> contextReference;
    private final AdUnitConfiguration adConfiguration;

    private Handler handler;

    private Timer timer;
    private TimerTask currentTimerTask = null;
    private int currentTimerTaskHash = 0;

    // Flag used by caller to close manually; More intuitive and reliable way to show
    // close button at the end of the video versus trusting the duration from VAST
    private boolean showCloseBtnOnComplete;

    private CountDownTimer countDownTimer;
    @Nullable private RelativeLayout lytCountDownCircle;

    private int remainingTimeInMs = -1;
    private boolean videoPaused = true;

    public InterstitialVideo(
            Context context,
            FrameLayout adView,
            InterstitialManager interstitialManager,
            AdUnitConfiguration adConfiguration
    ) {
        super(context, interstitialManager);

        contextReference = new WeakReference<>(context);
        this.adConfiguration = adConfiguration;
        isRewarded = adConfiguration.isRewarded();
        adViewContainer = adView;
        init();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    protected void handleCloseClick() {
        close();
    }

    @Override
    protected void handleDialogShow() {
        handleAdViewShow();

        scheduleShowButtonTask();
    }

    public boolean shouldShowCloseButtonOnComplete() {
        return showCloseBtnOnComplete;
    }

    public void setShowButtonOnComplete(boolean isEnabled) {
        showCloseBtnOnComplete = isEnabled;
    }

    public void setHasEndCard(boolean hasEndCard) {
        this.hasEndCard = hasEndCard;
    }

    public boolean isVideoPaused() {
        return videoPaused;
    }

    public void scheduleShowCloseBtnTask(View adView) {
        scheduleShowCloseBtnTask(adView, AdUnitConfiguration.SKIP_OFFSET_NOT_ASSIGNED);
    }

    public void scheduleShowButtonTask() {
        if (hasEndCard) useSkipButton = true;

        int skipDelay = getSkipDelayMs();
        long videoLength = getDuration(adViewContainer);

        if (videoLength <= skipDelay) {
            scheduleTimer(videoLength);
            showCloseBtnOnComplete = true;
        } else {
            scheduleTimer(skipDelay);
        }
    }

    public void scheduleShowCloseBtnTask(
            View adView,
            int closeDelayInMs
    ) {
        long delayInMs = getCloseDelayInMs(adView, closeDelayInMs);
        if (delayInMs == 0) {
            LogUtil.debug(TAG, "Delay is 0. Not scheduling skip button show.");
            return;
        }

        long videoLength = getDuration(adView);
        LogUtil.debug(TAG, "Video length: " + videoLength);
        if (videoLength <= delayInMs) {
            // Short video, show close at the end
            showCloseBtnOnComplete = true;
        } else {
            // Clamp close delay value
            long upperBound = Math.min(videoLength, CLOSE_DELAY_MAX_IN_MS);
            long closeDelayTimeInMs = Utils.clampInMillis((int) delayInMs, 0, (int) upperBound);
            scheduleTimer(closeDelayTimeInMs);
        }
    }

    public void pauseVideo() {
        LogUtil.debug(TAG, "pauseVideo");
        videoPaused = true;
        stopTimer();
        stopCountDownTimer();
    }

    public void resumeVideo() {
        LogUtil.debug(TAG, "resumeVideo");
        videoPaused = false;
        if (getRemainingTimerTimeInMs() != AdUnitConfiguration.SKIP_OFFSET_NOT_ASSIGNED && getRemainingTimerTimeInMs() > 500L) {
            scheduleShowCloseBtnTask(adViewContainer, getRemainingTimerTimeInMs());
        }
    }

    /**
     * Remove all views
     */
    public void removeViews() {
        if (adViewContainer != null) {
            adViewContainer.removeAllViews();
        }
    }

    /**
     * Queue new task that should be performed in UI thread.
     *
     * @param task that will perform in UI thread
     */
    public void queueUIThreadTask(Runnable task) {
        if (task != null && handler != null) {
            handler.post(task);
        }
    }

    public void close() {
        LogUtil.debug(TAG, "closeableAdContainer -  onClose()");
        cancel();

        //IMPORTANT: call interstitialClosed() so it sends back to the adViewContainer to reimplant after closing an ad.
        interstitialManager.interstitialAdClosed();
    }

    protected void init() {
        handler = new Handler(Looper.getMainLooper());
        timer = new Timer();

        Context context = contextReference.get();
        if (context == null) {
            return;
        }

        if (isRewarded) {
            lytCountDownCircle = (RelativeLayout) LayoutInflater.from(context)
                .inflate(R.layout.lyt_countdown_circle_overlay, null);
        }

        //remove it from parent, if any, before adding it to the new view
        Views.removeFromParent(adViewContainer);
        addContentView(
            adViewContainer,
            new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
            )
        );
        // interstitialManager.setCountDownTimerView(lytCountDownCircle);
        setOnKeyListener((dialog, keyCode, event) -> keyCode == KeyEvent.KEYCODE_BACK);
    }

    private long getOffsetLong(View view) {
        return (view instanceof BaseAdView) ? ((BaseAdView) view).getMediaOffset() : AdUnitConfiguration.SKIP_OFFSET_NOT_ASSIGNED;
    }

    private long getCloseDelayInMs(
            View adView,
            int closeDelayInMs
    ) {
        long delayInMs = AdUnitConfiguration.SKIP_OFFSET_NOT_ASSIGNED;

        long offsetLong = getOffsetLong(adView);
        if (offsetLong >= 0) {
            delayInMs = offsetLong;
        }

        int remainingTime = getRemainingTimerTimeInMs();
        if (closeDelayInMs == remainingTime && remainingTime >= 0) {
            delayInMs = closeDelayInMs;
        }

        if (delayInMs == AdUnitConfiguration.SKIP_OFFSET_NOT_ASSIGNED) {
            delayInMs = CLOSE_DELAY_DEFAULT_IN_MS;
        }
        LogUtil.debug(TAG, "Picked skip offset: " + delayInMs + " ms.");
        return delayInMs;
    }

    private void createCurrentTimerTask() {
        currentTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (currentTimerTaskHash != this.hashCode()) {
                    cancel();
                    return;
                }

                queueUIThreadTask(() -> {
                    try {
                        if (useSkipButton) {
                            skipView.setVisibility(View.VISIBLE);
                        } else {
                            changeCloseViewVisibility(View.VISIBLE);
                        }
                    } catch (Exception e) {
                        LogUtil.error(TAG, "Failed to render custom close icon: " + Log.getStackTraceString(e));
                    }
                });
            }
        };

        currentTimerTaskHash = currentTimerTask.hashCode();
    }

    protected long getDuration(View view) {
        return (view instanceof BaseAdView) ? ((BaseAdView) view).getMediaDuration() : 0;
    }

    private void stopTimer() {
        if (timer != null) {
            if (currentTimerTask != null) {
                currentTimerTask.cancel();
                currentTimerTask = null;
            }

            timer.cancel();
            timer.purge();

            timer = null;
        }
    }

    private void stopCountDownTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer.onFinish();
            countDownTimer = null;
        }
    }

    private int getRemainingTimerTimeInMs() {
        return remainingTimeInMs;
    }

    private void handleAdViewShow() {
        if (interstitialManager != null) {
            interstitialManager.show();
        }
    }

    @VisibleForTesting
    protected void scheduleTimer(long delayInMs) {
        LogUtil.debug(TAG, "Scheduling timer at: " + delayInMs);

        stopTimer();

        timer = new Timer();

        createCurrentTimerTask();

        if (delayInMs >= 0) {
            // we should convert it to milliseconds, so timer.schedule properly gets the delay in millisconds?
            timer.schedule(currentTimerTask, (delayInMs));
        }

        // Show timer until close
        if (isRewarded) {
            showDurationTimer(delayInMs);
        } else {
            startTimer(delayInMs);
        }
    }

    protected void startTimer(long durationInMillis) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(durationInMillis, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                int roundedMillis = Math.round((float) millisUntilFinished / 1000f);
                remainingTimeInMs = (int) millisUntilFinished;
            }

            @Override
            public void onFinish() {}
        };
        countDownTimer.start();
    }

    /**
     * @param durationInMillis - duration to count down
     */
    @VisibleForTesting
    protected void showDurationTimer(long durationInMillis) {
        if (durationInMillis == 0) {
            return;
        }

        final ProgressBar pbProgress = lytCountDownCircle.findViewById(R.id.Progress);
        pbProgress.setMax((int) durationInMillis);

        // Turns progress bar ccw 90 degrees so progress starts from the top
        final Animation animation = new RotateAnimation(0.0f,
                -90.0f,
                Animation.RELATIVE_TO_PARENT,
                0.5f,
                Animation.RELATIVE_TO_PARENT,
                0.5f
        );
        animation.setFillAfter(true);
        pbProgress.startAnimation(animation);

        final TextView lblCountdown = lytCountDownCircle.findViewById(R.id.lblCountdown);
        final WeakReference<FrameLayout> weakAdViewContainer = new WeakReference<>(adViewContainer);
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(durationInMillis, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                int roundedMillis = Math.round((float) millisUntilFinished / 1000f);
                remainingTimeInMs = (int) millisUntilFinished;
                pbProgress.setProgress((int) millisUntilFinished);
                lblCountdown.setText(String.format(Locale.US, "%d", roundedMillis));
            }

            @Override
            public void onFinish() {
                FrameLayout adViewContainer = weakAdViewContainer.get();
                if (adViewContainer == null) {
                    return;
                }
                adViewContainer.removeView(lytCountDownCircle);
            }
        };
        countDownTimer.start();
        if (lytCountDownCircle.getParent() != null) {
            Views.removeFromParent(lytCountDownCircle);
        }
        adViewContainer.addView(lytCountDownCircle);
        InsetsUtils.addCutoutAndNavigationInsets(lytCountDownCircle);
    }

    @VisibleForTesting
    protected void setRemainingTimeInMs(int value) {
        remainingTimeInMs = value;
    }

    protected int getSkipDelayMs() {
        InterstitialDisplayPropertiesInternal properties = interstitialManager.getInterstitialDisplayProperties();
        if (properties != null) {
            long videoDuration = getDuration(adViewContainer);
            long upperBound = Math.min(videoDuration, CLOSE_DELAY_MAX_IN_MS);

            int delay = properties.skipDelay * 1000;
            return Utils.clampInMillis(delay, 0, (int) upperBound);
        }
        return CLOSE_DELAY_DEFAULT_IN_MS;
    }

}
