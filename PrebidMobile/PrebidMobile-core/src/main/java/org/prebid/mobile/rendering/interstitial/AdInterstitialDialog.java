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

package org.prebid.mobile.rendering.interstitial;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.interstitial.rewarded.RewardManager;
import org.prebid.mobile.rendering.interstitial.rewarded.RewardedClosingRules;
import org.prebid.mobile.rendering.interstitial.rewarded.RewardedCompletionRules;
import org.prebid.mobile.rendering.interstitial.rewarded.RewardedExt;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;
import org.prebid.mobile.rendering.views.webview.WebViewBase;
import org.prebid.mobile.rendering.views.webview.mraid.JSInterface;
import org.prebid.mobile.rendering.views.webview.mraid.Views;

import java.lang.ref.WeakReference;

@SuppressLint("NewApi")
public class AdInterstitialDialog extends AdBaseDialog {

    private static final String TAG = AdInterstitialDialog.class.getSimpleName();

    @Nullable
    private RewardedCustomTimer timer;
    @Nullable
    private FragmentManager.FragmentLifecycleCallbacks lifecycleListener;

    /**
     * @param context                  activity context.
     * @param webViewBaseLocal         webview with ad.
     * @param adViewContainer          container for ad.
     */
    public AdInterstitialDialog(Context context, WebViewBase webViewBaseLocal,
                                FrameLayout adViewContainer,
                                InterstitialManager interstitialManager) {
        super(context, webViewBaseLocal, interstitialManager);
        this.adViewContainer = adViewContainer;


        preInit();
        if (this.interstitialManager.getInterstitialDisplayProperties() != null) {
            this.adViewContainer.setBackgroundColor(this.interstitialManager.getInterstitialDisplayProperties()
                                                                            .getPubBackGroundOpacity());
        }
        setUpCloseButtonTask();
        setListeners();
        webViewBase.setDialog(this);
    }

    private void setListeners() {
        setOnCancelListener(dialog -> {
            try {
                if (webViewBase.isMRAID() && jsExecutor != null) {
                    webViewBase.getMRAIDInterface().onStateChange(JSInterface.STATE_DEFAULT);
                    webViewBase.detachFromParent();
                }
            }
            catch (Exception e) {
                LogUtil.error(TAG, "Interstitial ad closed but post-close events failed: " + Log.getStackTraceString(e));
            }
        });
    }

    @Override
    protected void handleCloseClick() {
        interstitialManager.interstitialClosed(webViewBase);
    }

    @Override
    protected void handleDialogShow() {
        Views.removeFromParent(adViewContainer);
        addContentView(adViewContainer,
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT
                )
        );
    }

    public void nullifyDialog() {
        cancel();
        cleanup();
    }


    @VisibleForTesting
    protected void setUpCloseButtonTask() {
        if (interstitialManager.getInterstitialDisplayProperties() == null || interstitialManager.getInterstitialDisplayProperties().config == null) {
            return;
        }

        AdUnitConfiguration config = interstitialManager.getInterstitialDisplayProperties().config;
        RewardManager rewardManager = config.getRewardManager();
        if (config != null && config.isRewarded()) {
            if (rewardManager.getUserRewardedAlready()) {
                return;
            }

            setBackgroundListener();
            changeCloseViewVisibility(View.GONE);

            RewardedExt rewardedExt = rewardManager.getRewardedExt();
            int defaultRewardTime = RewardedCompletionRules.DEFAULT_BANNER_TIME_MS;
            int postRewardTime = rewardedExt.getClosingRules().getPostRewardTime() * 1000;

            boolean autoClose = rewardedExt.getClosingRules().getAction() == RewardedClosingRules.Action.AUTO_CLOSE;
            boolean isEndCard = config.getHasEndCard();
            boolean hasRewardEventUrl = getBannerEvent(rewardedExt, isEndCard) != null;

            if (hasRewardEventUrl) {
                scheduleRewardListener(defaultRewardTime, 0, autoClose);
                rewardManager.setAfterRewardListener(() -> scheduleCloseButtonDisplaying(postRewardTime, autoClose));
                return;
            }

            int rewardTime = getBannerTime(rewardedExt, isEndCard) * 1000;
            scheduleRewardListener(rewardTime, postRewardTime, autoClose);
        }
    }

    protected void scheduleCloseButtonDisplaying(int closeButtonDelay, boolean autoClose) {
        LogUtil.debug(TAG, "Scheduled close button displaying in " + closeButtonDelay + "ms with autoclose " + autoClose);
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new DisplayCloseButtonRunnable(this, autoClose), closeButtonDelay);
        destroyRewardedListeners();
    }

    protected void scheduleRewardListener(int rewardDelay, int afterRewardDelay, boolean autoClose) {
        LogUtil.debug(TAG, "Scheduled reward in " + rewardDelay + "ms");

        AdUnitConfiguration config = interstitialManager.getInterstitialDisplayProperties().config;
        timer = new RewardedCustomTimer(rewardDelay, () -> {
            new RewardRunnable(config).run();
            scheduleCloseButtonDisplaying(afterRewardDelay, autoClose);
        });
        timer.start();
    }

    private void setBackgroundListener() {
        try {
            FragmentManager fragmentManager = getActivity().getFragmentManager();
            if (fragmentManager == null) {
                return;
            }

            lifecycleListener = createLifecycleListener();
            fragmentManager.registerFragmentLifecycleCallbacks(lifecycleListener, true);
        } catch (Throwable e) {
            LogUtil.error(TAG, "Can't set up lifecycle listener for background rewarded tracking.");
        }
    }

    @Nullable
    private String getBannerEvent(RewardedExt rewardedExt, boolean isEndCard) {
        if (isEndCard) {
            return rewardedExt.getCompletionRules().getEndCardEvent();
        }
        return rewardedExt.getCompletionRules().getBannerEvent();
    }

    private int getBannerTime(RewardedExt rewardedExt, boolean isEndCard) {
        if (isEndCard) {
            return rewardedExt.getCompletionRules().getEndCardTime();
        }
        return rewardedExt.getCompletionRules().getBannerTime();
    }

    private void destroyRewardedListeners() {
        FragmentManager fragmentManager = getActivity().getFragmentManager();
        if (fragmentManager != null && lifecycleListener != null) {
            fragmentManager.unregisterFragmentLifecycleCallbacks(lifecycleListener);
        }

        if (timer != null) {
            timer = null;
        }
    }

    private FragmentManager.FragmentLifecycleCallbacks createLifecycleListener() {
        return new FragmentManager.FragmentLifecycleCallbacks() {
            @Override
            public void onFragmentStopped(FragmentManager fm, Fragment f) {
                super.onFragmentStopped(fm, f);

                if (timer != null) {
                    timer.cancel();
                }
            }

            @Override
            public void onFragmentStarted(FragmentManager fm, Fragment f) {
                super.onFragmentStarted(fm, f);

                if (timer != null) {
                    timer = timer.copy();
                    timer.start();
                }
            }
        };
    }

    private static class RewardedCustomTimer extends CountDownTimer {

        long lastTime = -1;
        Runnable onFinish;

        public RewardedCustomTimer(long millisInFuture, Runnable onFinish) {
            super(millisInFuture, 1000);
            this.onFinish = onFinish;

            LogUtil.debug("RewardedCustomTimer", "Created new timer with " + millisInFuture);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            lastTime = millisUntilFinished;
        }

        @Override
        public void onFinish() {
            onFinish.run();
        }


        public RewardedCustomTimer copy() {
            return new RewardedCustomTimer(lastTime, onFinish);
        }

    }

    private static class DisplayCloseButtonRunnable implements Runnable {

        private final boolean autoClose;
        private final WeakReference<AdBaseDialog> dialogReference;

        public DisplayCloseButtonRunnable(AdBaseDialog dialog, boolean autoClose) {
            this.dialogReference = new WeakReference<>(dialog);
            this.autoClose = autoClose;
        }

        @Override
        public void run() {
            AdBaseDialog dialog = dialogReference.get();
            if (dialog == null) return;

            dialog.changeCloseViewVisibility(View.VISIBLE);
            if (autoClose) {
                dialog.handleCloseClick();
            }
        }
    }

    private static class RewardRunnable implements Runnable {

        private final WeakReference<AdUnitConfiguration> configReference;

        public RewardRunnable(AdUnitConfiguration config) {
            this.configReference = new WeakReference<>(config);
        }

        @Override
        public void run() {
            AdUnitConfiguration config = configReference.get();
            if (config == null) return;

            config.getRewardManager().notifyRewardListener();
        }
    }



}
