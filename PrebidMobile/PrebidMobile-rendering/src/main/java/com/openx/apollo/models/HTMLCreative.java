package com.openx.apollo.models;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.VisibleForTesting;

import com.openx.apollo.errors.AdException;
import com.openx.apollo.interstitial.InterstitialManagerDisplayDelegate;
import com.openx.apollo.listeners.CreativeViewListener;
import com.openx.apollo.listeners.WebViewDelegate;
import com.openx.apollo.models.internal.MraidEvent;
import com.openx.apollo.models.internal.VisibilityTrackerOption;
import com.openx.apollo.models.internal.VisibilityTrackerResult;
import com.openx.apollo.models.ntv.NativeEventTracker;
import com.openx.apollo.mraid.methods.MraidController;
import com.openx.apollo.session.manager.OmAdSessionManager;
import com.openx.apollo.utils.exposure.ViewExposure;
import com.openx.apollo.utils.logger.OXLog;
import com.openx.apollo.views.interstitial.InterstitialManager;
import com.openx.apollo.views.webview.OpenXWebViewBanner;
import com.openx.apollo.views.webview.OpenXWebViewBase;
import com.openx.apollo.views.webview.OpenXWebViewInterstitial;
import com.openx.apollo.views.webview.WebViewBase;
import com.openx.apollo.views.webview.mraid.Views;

import static com.openx.apollo.models.AdConfiguration.AdUnitIdentifierType.BANNER;
import static com.openx.apollo.models.AdConfiguration.AdUnitIdentifierType.INTERSTITIAL;

public class HTMLCreative extends AbstractCreative
    implements WebViewDelegate, InterstitialManagerDisplayDelegate, Comparable {

    private static final String TAG = HTMLCreative.class.getSimpleName();

    private MraidController mMraidController;

    private OpenXWebViewBase mTwoPartNewWebViewBase;

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
        AdConfiguration.AdUnitIdentifierType adType = model.getAdConfiguration().getAdUnitIdentifierType();
        if (model.getAdConfiguration().isBuiltInVideo()) {
            adType = BANNER;
        }

        if (adType == null) {
            throw new AdException(AdException.INTERNAL_ERROR, "Can't create a WebView for a null adtype");
        }
        //create a webview here
        OpenXWebViewBase openXWebView = null;
        if (adType == BANNER) {
            //do all banner
            openXWebView = (OpenXWebViewBanner) ViewPool.getInstance()
                                                        .getUnoccupiedView(mContextReference.get(), null, adType, mInterstitialManager);
        }
        else if (adType == INTERSTITIAL) {
            //do all interstitials
            openXWebView = (OpenXWebViewInterstitial) ViewPool.getInstance()
                                                              .getUnoccupiedView(mContextReference.get(), null, adType, mInterstitialManager);
        }

        if (openXWebView == null) {
            throw new AdException(AdException.INTERNAL_ERROR, "OpenXWebView creation failed");
        }
        openXWebView.setWebViewDelegate(this);
        openXWebView.setCreative(this);

        String html = model.getHtml();
        int width = model.getWidth();
        int height = model.getHeight();

        if (TextUtils.isEmpty(html)) {
            String msg = "No HTML in creative data";
            OXLog.error(TAG, msg);
            throw new AdException(AdException.SERVER_ERROR, msg);
        }
        else {
            html = injectingScriptContent(html);
            openXWebView.loadHTML(html, width, height);
            setCreativeView(openXWebView);
        }

        mIsEndCard = model.hasEndCard();
    }

    @Override
    public void display() {

        if (!(getCreativeView() instanceof OpenXWebViewBase)) {
            OXLog.error(TAG, "Could not cast mCreativeView to a OpenXWebViewBase");
            return;
        }
        OpenXWebViewBase creativeWebView = (OpenXWebViewBase) getCreativeView();

        // Fire impression
        startViewabilityTracker();

        View adIndicatorView = getAdIndicatorView();
        if (adIndicatorView != null) {
            Views.removeFromParent(adIndicatorView);
            creativeWebView.addView(adIndicatorView);
        }
    }

    @Override
    public void createOmAdSession() {
        if (getCreativeView() == null || getCreativeView().getWebView() == null) {
            OXLog.error(TAG, "initOmAdSession error. Opex webView is null");
            return;
        }

        OmAdSessionManager omAdSessionManager = mWeakOmAdSessionManager.get();
        if (omAdSessionManager == null) {
            OXLog.error(TAG, "Error creating adSession. OmAdSessionManager is null");
            return;
        }

        WebViewBase webView = getCreativeView().getWebView();

        AdConfiguration adConfiguration = getCreativeModel().getAdConfiguration();
        omAdSessionManager.initWebAdSessionManager(webView, adConfiguration.getContentUrl());
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
            ((OpenXWebViewBase) getCreativeView()).getWebView().isMRAID()
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
        OXLog.debug(TAG, "MRAID Expand/Resize is closing.");
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
            OXLog.debug(TAG, "interstitialDialogShown(): Failed to notify creativeViewListener. creativeViewListener is null.");
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
    public OpenXWebViewBase getCreativeView() {
        return (OpenXWebViewBase) super.getCreativeView();
    }

    public void mraidAdExpanded() {
        OXLog.debug(TAG, "MRAID ad expanded");
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
            OXLog.debug(TAG, "Impression fired");
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
        OXLog.debug(TAG, "MRAID ad collapsed");
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
                OXLog.debug(TAG, "Unable to injectScriptContent. AdSessionManager is null.");
                return html;
            }
            return omAdSessionManager.injectValidationScriptIntoHtml(html);
        }
        catch (IllegalArgumentException | IllegalStateException e) {
            OXLog.error(TAG, "Failed to inject script content into html  " + Log.getStackTraceString(e));
            return html;
        }
    }

    public void setTwoPartNewWebViewBase(OpenXWebViewBase twoPartNewWebViewBase) {
        mTwoPartNewWebViewBase = twoPartNewWebViewBase;
    }
}
