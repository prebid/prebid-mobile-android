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
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.interstitial.InterstitialManagerDisplayDelegate;
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
import org.prebid.mobile.rendering.views.webview.PrebidWebViewBanner;
import org.prebid.mobile.rendering.views.webview.PrebidWebViewBase;
import org.prebid.mobile.rendering.views.webview.PrebidWebViewInterstitial;
import org.prebid.mobile.rendering.views.webview.WebViewBase;
import org.prebid.mobile.units.configuration.AdUnitConfiguration;

public class HTMLCreative extends AbstractCreative
    implements WebViewDelegate, InterstitialManagerDisplayDelegate, Comparable {

    private static final String TAG = HTMLCreative.class.getSimpleName();

    private MraidController mMraidController;

    private PrebidWebViewBase mTwoPartNewWebViewBase;

    private boolean mIsEndCard = false;
    private boolean mResolved;

    public HTMLCreative(Context context, CreativeModel model, OmAdSessionManager omAdSessionManager, InterstitialManager interstitialManager)
    throws AdException {
        super(context, model, omAdSessionManager, interstitialManager);
        mInterstitialManager.setInterstitialDisplayDelegate(this);
        mMraidController = new MraidController(mInterstitialManager);
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
        if (mContextReference == null || mContextReference.get() == null) {
            throw new AdException(AdException.INTERNAL_ERROR, "Context is null. Could not load adHtml");
        }
        CreativeModel model = getCreativeModel();
        AdUnitConfiguration.AdUnitIdentifierType adType = model.getAdConfiguration().getAdUnitIdentifierType();
        if (model.getAdConfiguration().isBuiltInVideo()) {
            adType = AdUnitConfiguration.AdUnitIdentifierType.BANNER;
        }

        if (adType == null) {
            throw new AdException(AdException.INTERNAL_ERROR, "Can't create a WebView for a null adtype");
        }
        //create a webview here
        PrebidWebViewBase prebidWebView = null;
        if (adType == AdUnitConfiguration.AdUnitIdentifierType.BANNER) {
            //do all banner
            prebidWebView = (PrebidWebViewBanner) ViewPool.getInstance()
                    .getUnoccupiedView(mContextReference.get(), null, adType, mInterstitialManager);
        } else if (adType == AdUnitConfiguration.AdUnitIdentifierType.INTERSTITIAL) {
            //do all interstitials
            prebidWebView = (PrebidWebViewInterstitial) ViewPool.getInstance()
                    .getUnoccupiedView(mContextReference.get(), null, adType, mInterstitialManager);
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
        }

        mIsEndCard = model.hasEndCard();
    }

    @Override
    public void display() {

        if (!(getCreativeView() instanceof PrebidWebViewBase)) {
            LogUtil.error(TAG, "Could not cast mCreativeView to a PrebidWebViewBase");
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

        OmAdSessionManager omAdSessionManager = mWeakOmAdSessionManager.get();
        if (omAdSessionManager == null) {
            LogUtil.error(TAG, "Error creating adSession. OmAdSessionManager is null");
            return;
        }

        WebViewBase webView = getCreativeView().getWebView();

        AdUnitConfiguration adConfiguration = getCreativeModel().getAdConfiguration();
        ContentObject contentObject = adConfiguration.getAppContent();
        String contentUrl = null;
        if (contentObject != null) contentUrl = contentObject.getUrl();
        omAdSessionManager.initWebAdSessionManager(webView, contentUrl);
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
        mCreativeVisibilityTracker = new CreativeVisibilityTracker(
            getCreativeView().getWebView(),
            visibilityTrackerOption,
            ((PrebidWebViewBase) getCreativeView()).getWebView().isMRAID()
        );
        mCreativeVisibilityTracker.setVisibilityTrackerListener(this::onVisibilityEvent);
        mCreativeVisibilityTracker.startVisibilityCheck(mContextReference.get());
    }

    @Override
    public void handleAdWindowFocus() {
    }

    @Override
    public void handleAdWindowNoFocus() {
    }

    @Override
    public void webViewReadyToDisplay() {
        if (mResolved) {
            return;
        }

        mResolved = true;
        getResolutionListener().creativeReady(this);
    }

    @Override
    public void webViewFailedToLoad(AdException error) {
        if (mResolved) {
            return;
        }

        mResolved = true;
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
        return mResolved;
    }

    /**
     * @return true if creative is an endcard, false otherwise.
     */
    @Override
    public boolean isEndCard() {
        return mIsEndCard;
    }

    public void destroy() {
        super.destroy();

        if (getCreativeView() != null) {
            getCreativeView().destroy();
        }

        if (mMraidController != null) {
            mMraidController.destroy();
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
        if (mMraidController == null) {
            mMraidController = new MraidController(mInterstitialManager);
        }
        mMraidController.handleMraidEvent(mraidEvent, this, oldWebViewBase, mTwoPartNewWebViewBase);
    }

    /**
     * Injects OM script content into HTML
     *
     * @param html creative html content
     * @return html with injected OMSDK script or non modified creative html content if failure.
     */
    private String injectingScriptContent(String html) {
        try {
            OmAdSessionManager omAdSessionManager = mWeakOmAdSessionManager.get();
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
        mTwoPartNewWebViewBase = twoPartNewWebViewBase;
    }
}
