package com.openx.apollo.views.interstitial;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.CountDownTimer;
import android.os.Handler;
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

import androidx.annotation.VisibleForTesting;

import com.openx.apollo.R;
import com.openx.apollo.interstitial.AdBaseDialog;
import com.openx.apollo.models.AdConfiguration;
import com.openx.apollo.utils.helpers.Utils;
import com.openx.apollo.utils.logger.OXLog;
import com.openx.apollo.views.base.BaseAdView;
import com.openx.apollo.views.webview.mraid.Views;

import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("NewApi")
//Interstitial video
public class InterstitialVideo extends AdBaseDialog {

    private static final String TAG = InterstitialVideo.class.getSimpleName();

    private static final int CLOSE_DELAY_DEFAULT_IN_MS = 2 * 1000;
    private static final int CLOSE_DELAY_MAX_IN_MS = 30 * 1000;

    //Leaving context here for testing
    //Reason:
    // "If these are pure JVM unit tests (i.e. run on your computer's JVM and not on an Android emulator/device), then you have no real implementations of methods on any Android classes.
    // You are using a mockable jar which just contains empty classes and methods with "final" removed so you can mock them, but they don't really work like when running normal Android."
    private final WeakReference<Context> mContextReference;
    private final AdConfiguration mAdConfiguration;

    private Handler mHandler;

    private Timer mTimer;
    private TimerTask mCurrentTimerTask = null;
    private int mCurrentTimerTaskHash = 0;

    // Flag used by caller to close manually; More intuitive and reliable way to show
    // close button at the end of the video versus trusting the duration from VAST
    private boolean mShowCloseBtnOnComplete;

    private CountDownTimer mCountDownTimer;
    private RelativeLayout mLytCountDownCircle;

    private int mRemainingTimeInMs = -1;
    private boolean mVideoPaused = true;

    public InterstitialVideo(Context context,
                             FrameLayout adView,
                             InterstitialManager interstitialManager,
                             AdConfiguration adConfiguration) {
        super(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen, interstitialManager);

        mContextReference = new WeakReference<>(context);
        mAdConfiguration = adConfiguration;
        mAdViewContainer = adView;
        init();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    @Override
    protected void handleCloseClick() {
        close();
    }

    @Override
    protected void handleDialogShow() {
        handleAdViewShow();

        // For rewarded video, show close button on video complete;
        // Else, schedule close button show according to close button delay logic
        if (mAdConfiguration.isRewarded()) {
            mShowCloseBtnOnComplete = true;
            long durationInMillis = getDuration(mAdViewContainer);
            showDurationTimer(durationInMillis);
        }
        else {
            scheduleShowCloseBtnTask(mAdViewContainer);
        }

        Views.removeFromParent(mAdIndicatorView);
        mAdViewContainer.addView(mAdIndicatorView);
    }

    public boolean shouldShowCloseButtonOnComplete() {
        return mShowCloseBtnOnComplete;
    }

    public void setShowButtonOnComplete(boolean isEnabled) {
        mShowCloseBtnOnComplete = isEnabled;
    }

    public boolean isVideoPaused() {
        return mVideoPaused;
    }

    public void scheduleShowCloseBtnTask(View adView) {
        scheduleShowCloseBtnTask(adView, AdConfiguration.SKIP_OFFSET_NOT_ASSIGNED);
    }

    public void scheduleShowCloseBtnTask(View adView, int closeDelayInMs) {
        long delayInMs = getCloseDelayInMs(adView, closeDelayInMs);
        if (delayInMs == 0) {
            OXLog.debug(TAG, "Delay is 0. Not scheduling skip button show.");
            return;
        }

        long videoLength = getDuration(adView);
        OXLog.debug(TAG, "Video length: " + videoLength);
        if (videoLength <= delayInMs) {
            // Short video, show close at the end
            mShowCloseBtnOnComplete = true;
        }
        else {
            // Clamp close delay value
            long upperBound = Math.min(videoLength, CLOSE_DELAY_MAX_IN_MS);
            long closeDelayTimeInMs = Utils.clampInMillis((int) delayInMs, 0, (int) upperBound);
            scheduleTimer(closeDelayTimeInMs);
        }
    }

    public void pauseVideo() {
        OXLog.debug(TAG, "pauseVideo");
        mVideoPaused = true;
        stopTimer();
        stopCountDownTimer();
    }

    public void resumeVideo() {
        OXLog.debug(TAG, "resumeVideo");
        mVideoPaused = false;
        if (getRemainingTimerTimeInMs() != AdConfiguration.SKIP_OFFSET_NOT_ASSIGNED
            && getRemainingTimerTimeInMs() > 500L) {
            scheduleShowCloseBtnTask(mAdViewContainer, getRemainingTimerTimeInMs());
        }
    }

    /**
     * Remove all views
     */
    public void removeViews() {
        if (mAdViewContainer != null) {
            mAdViewContainer.removeAllViews();
        }
    }

    /**
     * Queue new task that should be performed in UI thread.
     *
     * @param task that will perform in UI thread
     */
    public void queueUIThreadTask(Runnable task) {
        if (task != null && mHandler != null) {
            mHandler.post(task);
        }
    }

    public void close() {
        OXLog.debug(TAG, "closeableAdContainer -  onClose()");
        cancel();

        //IMPORTANT: call interstitialClosed() so it sends back to the mAdViewContainer to reimplant after closing an ad.
        mInterstitialManager.interstitialAdClosed();
    }

    protected void init() {
        mHandler = new Handler();
        mTimer = new Timer();

        Context context = mContextReference.get();
        if (context == null) {
            return;
        }

        mLytCountDownCircle = (RelativeLayout) LayoutInflater.from(context)
                                                             .inflate(R.layout.lyt_countdown_circle_overlay, null);

        //remove it from parent, if any, before adding it to the new view
        Views.removeFromParent(mAdViewContainer);
        addContentView(mAdViewContainer,
                       new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                                                       RelativeLayout.LayoutParams.MATCH_PARENT)
        );
        // mInterstitialManager.setCountDownTimerView(mLytCountDownCircle);
        setOnKeyListener((dialog, keyCode, event) -> keyCode == KeyEvent.KEYCODE_BACK);
    }

    private long getOffsetLong(View view) {
        return (view instanceof BaseAdView)
               ? ((BaseAdView) view).getMediaOffset()
               : AdConfiguration.SKIP_OFFSET_NOT_ASSIGNED;
    }

    private long getCloseDelayInMs(View adView, int closeDelayInMs) {
        long delayInMs = AdConfiguration.SKIP_OFFSET_NOT_ASSIGNED;

        long offsetLong = getOffsetLong(adView);
        if (offsetLong >= 0) {
            delayInMs = offsetLong;
        }

        int remainingTime = getRemainingTimerTimeInMs();
        if (closeDelayInMs == remainingTime && remainingTime >= 0) {
            delayInMs = closeDelayInMs;
        }

        if (delayInMs == AdConfiguration.SKIP_OFFSET_NOT_ASSIGNED) {
            delayInMs = CLOSE_DELAY_DEFAULT_IN_MS;
        }
        OXLog.debug(TAG, "Picked skip offset: " + delayInMs + " ms.");
        return delayInMs;
    }

    private void createCurrentTimerTask() {
        mCurrentTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (mCurrentTimerTaskHash != this.hashCode()) {
                    cancel();
                    return;
                }

                queueUIThreadTask(() -> {
                    try {
                        // with default x image);
                        if (!mAdConfiguration.isRewarded()) {
                            changeCloseViewVisibility(View.VISIBLE);
                        }
                    }
                    catch (Exception e) {
                        OXLog.error(TAG, "Failed to render custom close icon: " + Log.getStackTraceString(e));
                    }
                });
            }
        };

        mCurrentTimerTaskHash = mCurrentTimerTask.hashCode();
    }

    private long getDuration(View view) {
        return (view instanceof BaseAdView)
               ? ((BaseAdView) view).getMediaDuration()
               : 0;
    }

    private void stopTimer() {
        if (mTimer != null) {
            if (mCurrentTimerTask != null) {
                mCurrentTimerTask.cancel();
                mCurrentTimerTask = null;
            }

            mTimer.cancel();
            mTimer.purge();

            mTimer = null;
        }
    }

    private void stopCountDownTimer() {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer.onFinish();
            mCountDownTimer = null;
        }
    }

    private int getRemainingTimerTimeInMs() {
        return mRemainingTimeInMs;
    }

    private void handleAdViewShow() {
        if (mInterstitialManager != null) {
            mInterstitialManager.show();
        }
    }

    @VisibleForTesting
    protected void scheduleTimer(long delayInMs) {
        OXLog.debug(TAG, "Scheduling timer at: " + delayInMs);

        stopTimer();

        mTimer = new Timer();

        createCurrentTimerTask();

        if (delayInMs >= 0) {
            // we should convert it to milliseconds, so mTimer.schedule properly gets the delay in millisconds?
            mTimer.schedule(mCurrentTimerTask, (delayInMs));
        }

        // Show timer until close
        showDurationTimer(delayInMs);
    }

    /**
     * @param durationInMillis - duration to count down
     */
    @VisibleForTesting
    protected void showDurationTimer(long durationInMillis) {
        if (durationInMillis == 0) {
            return;
        }

        final ProgressBar pbProgress = mLytCountDownCircle.findViewById(R.id.Progress);
        pbProgress.setMax((int) durationInMillis);

        // Turns progress bar ccw 90 degrees so progress starts from the top
        final Animation animation = new RotateAnimation(0.0f, -90.0f,
                                                        Animation.RELATIVE_TO_PARENT, 0.5f,
                                                        Animation.RELATIVE_TO_PARENT, 0.5f);
        animation.setFillAfter(true);
        pbProgress.startAnimation(animation);

        final TextView lblCountdown = mLytCountDownCircle.findViewById(R.id.lblCountdown);
        final WeakReference<FrameLayout> weakAdViewContainer = new WeakReference<>(mAdViewContainer);
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
        mCountDownTimer = new CountDownTimer(durationInMillis, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                int roundedMillis = Math.round((float) millisUntilFinished / 1000f);
                mRemainingTimeInMs = (int) millisUntilFinished;
                pbProgress.setProgress((int) millisUntilFinished);
                lblCountdown.setText(String.format(Locale.US, "%d", roundedMillis));
            }

            @Override
            public void onFinish() {
                FrameLayout adViewContainer = weakAdViewContainer.get();
                if (adViewContainer == null) {
                    return;
                }
                adViewContainer.removeView(mLytCountDownCircle);
            }
        };
        mCountDownTimer.start();
        if (mLytCountDownCircle.getParent() != null) {
            Views.removeFromParent(mLytCountDownCircle);
        }
        mAdViewContainer.addView(mLytCountDownCircle);
    }

    @VisibleForTesting
    protected void setRemainingTimeInMs(int value) {
        mRemainingTimeInMs = value;
    }
}
