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

package org.prebid.mobile.rendering.models;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;
import androidx.annotation.VisibleForTesting;
import org.prebid.mobile.ContentObject;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.interstitial.InterstitialManagerDisplayDelegate;
import org.prebid.mobile.rendering.interstitial.rewarded.RewardedExt;
import org.prebid.mobile.rendering.listeners.CreativeViewListener;
import org.prebid.mobile.rendering.listeners.WebViewDelegate;
import org.prebid.mobile.rendering.models.internal.MraidEvent;
import org.prebid.mobile.rendering.models.internal.VisibilityTrackerOption;
import org.prebid.mobile.rendering.models.internal.VisibilityTrackerResult;
import org.prebid.mobile.rendering.models.ntv.NativeEventTracker;
import org.prebid.mobile.rendering.mraid.methods.MraidController;
import org.prebid.mobile.rendering.session.manager.OmAdSessionManager;
import org.prebid.mobile.rendering.utils.exposure.ViewExposure;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;
import org.prebid.mobile.rendering.views.webview.*;

import java.util.EnumSet;

public class HTMLCreative extends AbstractCreative implements WebViewDelegate, InterstitialManagerDisplayDelegate, Comparable {

    private static final String TAG = HTMLCreative.class.getSimpleName();

    private MraidController mraidController;

    private PrebidWebViewBase twoPartNewWebViewBase;

    private boolean isEndCard = false;
    private boolean resolved;

    public HTMLCreative(
            Context context,
            CreativeModel model,
            OmAdSessionManager omAdSessionManager,
            InterstitialManager interstitialManager
    ) throws AdException {
        super(context, model, omAdSessionManager, interstitialManager);
        this.interstitialManager.setInterstitialDisplayDelegate(this);
        mraidController = new MraidController(this.interstitialManager);
    }

    @Override
    public boolean isDisplay() {
        return true;
    }

    @Override
    public boolean isVideo() {
        return false;
    }

    @Override
    public void load() throws AdException {
        if (contextReference == null || contextReference.get() == null) {
            throw new AdException(AdException.INTERNAL_ERROR, "Context is null. Could not load adHtml");
        }
        CreativeModel model = getCreativeModel();

        EnumSet<AdFormat> adFormats = model.getAdConfiguration().getAdFormats();
        if (adFormats.isEmpty()) {
            throw new AdException(AdException.INTERNAL_ERROR, "Can't create a WebView for a null adtype");
        }

        AdFormat adType = adFormats.iterator().next();

        if (model.getAdConfiguration().isBuiltInVideo()) {
            adType = AdFormat.BANNER;
        }

        PrebidWebViewBase prebidWebView = null;
        if (adType == AdFormat.BANNER) {
            //do all banner
            prebidWebView = (PrebidWebViewBanner) ViewPool.getInstance()
                                                          .getUnoccupiedView(contextReference.get(),
                                                                  null,
                                                                  adType,
                                                                  interstitialManager
                                                          );
        } else if (adType == AdFormat.INTERSTITIAL) {
            //do all interstitials
            prebidWebView = (PrebidWebViewInterstitial) ViewPool.getInstance()
                                                                .getUnoccupiedView(contextReference.get(),
                                                                        null,
                                                                        adType,
                                                                        interstitialManager
                                                                );
        }

        if (prebidWebView == null) {
            throw new AdException(AdException.INTERNAL_ERROR, "PrebidWebView creation failed");
        }
        prebidWebView.setWebViewDelegate(this);
        prebidWebView.setCreative(this);

        String html = model.getHtml();
        int width = model.getWidth();
        int height = model.getHeight();

        if (TextUtils.isEmpty(html)) {
            String msg = "No HTML in creative data";
            LogUtil.error(TAG, msg);
            throw new AdException(AdException.SERVER_ERROR, msg);
        }
        else {
            html = injectingScriptContent(html);
            prebidWebView.loadHTML(html, width, height);
            setCreativeView(prebidWebView);
            rewardedTracking(prebidWebView, getCreativeModel().getAdConfiguration());
        }

        isEndCard = model.hasEndCard();
    }

    @Override
    public void display() {

        if (!(getCreativeView() instanceof PrebidWebViewBase)) {
            LogUtil.error(TAG, "Could not cast creativeView to a PrebidWebViewBase");
            return;
        }
        PrebidWebViewBase creativeWebView = (PrebidWebViewBase) getCreativeView();

        // Fire impression
        startViewabilityTracker();
    }

    @Override
    public void createOmAdSession() {
        if (getCreativeView() == null || getCreativeView().getWebView() == null) {
            LogUtil.error(TAG, "initOmAdSession error. Opex webView is null");
            return;
        }

        OmAdSessionManager omAdSessionManager = weakOmAdSessionManager.get();
        if (omAdSessionManager == null) {
            LogUtil.error(TAG, "Error creating adSession. OmAdSessionManager is null");
            return;
        }

        WebViewBase webView = getCreativeView().getWebView();

        omAdSessionManager.initWebAdSessionManager(webView, null);
        startOmSession(omAdSessionManager, webView);
    }

    @Override
    public void trackAdLoaded() {
        CreativeModel creativeModel = getCreativeModel();
        creativeModel.trackDisplayAdEvent(TrackingEvent.Events.LOADED);
    }

    @Override
    public void startViewabilityTracker() {
        VisibilityTrackerOption visibilityTrackerOption = new VisibilityTrackerOption(NativeEventTracker.EventType.IMPRESSION);
        creativeVisibilityTracker = new CreativeVisibilityTracker(getCreativeView().getWebView(),
                visibilityTrackerOption,
                ((PrebidWebViewBase) getCreativeView()).getWebView().isMRAID()
        );
        creativeVisibilityTracker.setVisibilityTrackerListener(this::onVisibilityEvent);
        creativeVisibilityTracker.startVisibilityCheck(contextReference.get());
    }

    @Override
    public void handleAdWindowFocus() {
    }

    @Override
    public void handleAdWindowNoFocus() {
    }

    @Override
    public void webViewReadyToDisplay() {
        if (resolved) {
            return;
        }

        resolved = true;
        getResolutionListener().creativeReady(this);
    }

    @Override
    public void webViewFailedToLoad(AdException error) {
        if (resolved) {
            return;
        }

        resolved = true;
        getResolutionListener().creativeFailed(error);
    }

    //Used by non-mraid banners
    @Override
    public void webViewShouldOpenExternalLink(String url) {
        //open in the browser, send to pub & track click
        if (getCreativeView() != null) {
            getCreativeView().handleOpen(url);
        }
    }

    @Override
    public void webViewShouldOpenMRAIDLink(String url) {
        //open happens directly from the basejsinterface
        //send it to the pub
        getCreativeViewListener().creativeWasClicked(this, url);
        //track click of an ad. This has to happen for mraid.open() & mraid.expand() commands. MOBILE-3327
        getCreativeView().post(() -> getCreativeModel().trackDisplayAdEvent(TrackingEvent.Events.CLICK));
    }

    @Override
    public void interstitialAdClosed() {
        LogUtil.debug(TAG, "MRAID Expand/Resize is closing.");
        //For mraid banner, this is same as mraidAdCollapsed
        //For mraid interstitials with custom
        if (getCreativeViewListener() != null) {
            getCreativeViewListener().creativeInterstitialDidClose(this);
        }
    }

    @Override
    public void interstitialDialogShown(ViewGroup rootViewGroup) {
        CreativeViewListener creativeViewListener = getCreativeViewListener();
        if (creativeViewListener == null) {
            LogUtil.debug(TAG, "interstitialDialogShown(): Failed to notify creativeViewListener. creativeViewListener is null.");
            return;
        }
        creativeViewListener.creativeInterstitialDialogShown(rootViewGroup);
    }

    @Override
    public int compareTo(Object creative) {
        if (creative.hashCode() > hashCode()) {
            return 1;
        }
        return 0;
    }

    @Override
    public PrebidWebViewBase getCreativeView() {
        return (PrebidWebViewBase) super.getCreativeView();
    }

    public void mraidAdExpanded() {
        LogUtil.debug(TAG, "MRAID ad expanded");
        //send callback to pubs when an internal browser is closed
        if (getCreativeViewListener() != null) {
            getCreativeViewListener().creativeDidExpand(this);
        }
    }

    @VisibleForTesting
    void onVisibilityEvent(VisibilityTrackerResult result) {
        boolean isViewable = result.isVisible();
        boolean shouldFireImpression = result.shouldFireImpression();
        ViewExposure viewExposure = result.getViewExposure();

        if (shouldFireImpression && isViewable) {
            LogUtil.debug(TAG, "Impression fired");
            getCreativeModel().trackDisplayAdEvent(TrackingEvent.Events.IMPRESSION);
        }
        getCreativeView().onWindowFocusChanged(isViewable);
        getCreativeView().onViewExposureChange(viewExposure);
    }

    /**
     * @return true if WebView is resolved (loaded or failed to load callback triggered), false otherwise.
     */
    public boolean isResolved() {
        return resolved;
    }

    /**
     * @return true if creative is an endcard, false otherwise.
     */
    @Override
    public boolean isEndCard() {
        return isEndCard;
    }

    public void destroy() {
        super.destroy();

        if (getCreativeView() != null) {
            getCreativeView().destroy();
        }

        if (mraidController != null) {
            mraidController.destroy();
        }

        ViewPool.getInstance().clear();
    }

    public void mraidAdCollapsed() {
        LogUtil.debug(TAG, "MRAID ad collapsed");
        if (getCreativeViewListener() != null) {
            getCreativeViewListener().creativeDidCollapse(this);
        }
    }

    public void handleMRAIDEventsInCreative(final MraidEvent mraidEvent, final WebViewBase oldWebViewBase) {
        if (mraidController == null) {
            mraidController = new MraidController(interstitialManager);
        }
        mraidController.handleMraidEvent(mraidEvent, this, oldWebViewBase, twoPartNewWebViewBase);
    }


    protected static void rewardedTracking(PrebidWebViewBase webView, AdUnitConfiguration config) {
        if (!config.isRewarded()) {
            return;
        }

        RewardedExt rewardedExt = config.getRewardManager().getRewardedExt();
        String bannerEvent;
        if (config.getHasEndCard()) {
            bannerEvent = rewardedExt.getCompletionRules().getEndCardEvent();
        } else {
            bannerEvent = rewardedExt.getCompletionRules().getBannerEvent();
        }
        if (bannerEvent == null) {
            return;
        }

        webView.setActionUrl(new ActionUrl(bannerEvent, () -> config.getRewardManager().notifyRewardListener()));
    }

    /**
     * Injects OM script content into HTML
     *
     * @param html creative html content
     * @return html with injected OMSDK script or non modified creative html content if failure.
     */
    private String injectingScriptContent(String html) {
        try {
            OmAdSessionManager omAdSessionManager = weakOmAdSessionManager.get();
            if (omAdSessionManager == null) {
                LogUtil.debug(TAG, "Unable to injectScriptContent. AdSessionManager is null.");
                return html;
            }
            return omAdSessionManager.injectValidationScriptIntoHtml(html);
        }
        catch (IllegalArgumentException | IllegalStateException e) {
            LogUtil.error(TAG, "Failed to inject script content into html  " + Log.getStackTraceString(e));
            return html;
        }
    }

    public void setTwoPartNewWebViewBase(PrebidWebViewBase twoPartNewWebViewBase) {
        this.twoPartNewWebViewBase = twoPartNewWebViewBase;
    }
}
