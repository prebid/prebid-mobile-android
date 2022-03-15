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

package org.prebid.mobile.rendering.session.manager;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import androidx.annotation.Nullable;
import com.iab.omid.library.prebidorg.Omid;
import com.iab.omid.library.prebidorg.ScriptInjector;
import com.iab.omid.library.prebidorg.adsession.*;
import com.iab.omid.library.prebidorg.adsession.media.InteractionType;
import com.iab.omid.library.prebidorg.adsession.media.MediaEvents;
import com.iab.omid.library.prebidorg.adsession.media.Position;
import com.iab.omid.library.prebidorg.adsession.media.VastProperties;
import org.prebid.mobile.core.BuildConfig;
import org.prebid.mobile.rendering.models.TrackingEvent;
import org.prebid.mobile.rendering.models.internal.InternalFriendlyObstruction;
import org.prebid.mobile.rendering.models.internal.InternalPlayerState;
import org.prebid.mobile.rendering.sdk.JSLibraryManager;
import org.prebid.mobile.rendering.utils.logger.LogUtil;
import org.prebid.mobile.rendering.video.VideoAdEvent;
import org.prebid.mobile.rendering.video.vast.AdVerifications;
import org.prebid.mobile.rendering.video.vast.Verification;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * OmAdSessionManager is an implementation of Open Measurement used to track
 * web and native video ad events.
 */
public class OmAdSessionManager {
    private static final String TAG = OmAdSessionManager.class.getSimpleName();

    public static final String PARTNER_NAME = "Prebid";
    public static final String PARTNER_VERSION = BuildConfig.VERSION;

    private MediaEvents mMediaEvents;
    private AdEvents mAdEvents;
    private JSLibraryManager mJsLibraryManager;

    private Partner mPartner;
    private AdSession mAdSession;

    private OmAdSessionManager(JSLibraryManager instance) {
        mJsLibraryManager = instance;
        initPartner();
    }

    /**
     * First step to begin with when working with this class.
     * NOTE: The {@link #OmAdSessionManager} instance won't be created if OMSDK activation fails.
     */
    public static boolean activateOmSdk(Context applicationContext) {
        try {
            Omid.activate(applicationContext);
            return Omid.isActive();
        }
        catch (Throwable e) {
            LogUtil.error(TAG, "Did you add omsdk-android.aar? Failed to init openMeasurementSDK: " + Log.getStackTraceString(e));
        }
        return false;
    }

    /**
     * @return SessionManager instance or null, if OMSDK is not active.
     */
    @Nullable
    public static OmAdSessionManager createNewInstance(JSLibraryManager jsLibraryManager) {
        if (!isActive()) {
            LogUtil.error(TAG, "Failed to initialize OmAdSessionManager. Did you activate OMSDK?");
            return null;
        }

        return new OmAdSessionManager(jsLibraryManager);
    }

    public String injectValidationScriptIntoHtml(String html) {
        return ScriptInjector.injectScriptContentIntoHtml(mJsLibraryManager.getOMSDKScript(), html);
    }

    public void initWebAdSessionManager(WebView adView, String contentUrl) {
        AdSessionConfiguration adSessionConfiguration = createAdSessionConfiguration(CreativeType.HTML_DISPLAY,
                                                                                     ImpressionType.ONE_PIXEL,
                                                                                     Owner.NATIVE,
                                                                                     null);
        AdSessionContext adSessionContext = createAdSessionContext(adView, contentUrl);
        initAdSession(adSessionConfiguration, adSessionContext);
        initAdEvents();
    }

    /**
     * Initializes Native Video AdSession from AdVerifications.
     *
     * @param adVerifications VAST AdVerification node
     */
    public void initVideoAdSession(AdVerifications adVerifications, String contentUrl) {
        Owner owner = Owner.NATIVE;
        AdSessionConfiguration adSessionConfiguration = createAdSessionConfiguration(CreativeType.VIDEO,
                                                                                     ImpressionType.ONE_PIXEL,
                                                                                     owner,
                                                                                     owner);
        AdSessionContext adSessionContext = createAdSessionContext(adVerifications, contentUrl);
        initAdSession(adSessionConfiguration, adSessionContext);
        initAdEvents();
        initMediaAdEvents();
    }

    /**
     * Registers the display ad load event.
     */
    public void displayAdLoaded() {
        if (mAdEvents == null) {
            LogUtil.error(TAG, "Failed to register displayAdLoaded. AdEvent is null");
            return;
        }
        mAdEvents.loaded();
    }

    /**
     * Registers the video ad load event.
     *
     * @param isAutoPlay indicates if ad starts after load.
     */
    public void nonSkippableStandaloneVideoAdLoaded(final boolean isAutoPlay) {
        if (mAdEvents == null) {
            LogUtil.error(TAG, "Failed to register videoAdLoaded. adEvent is null");
            return;
        }
        try {
            VastProperties vastProperties = VastProperties
                .createVastPropertiesForNonSkippableMedia(isAutoPlay, Position.STANDALONE);
            mAdEvents.loaded(vastProperties);
        }
        catch (Exception e) {
            LogUtil.error(TAG, "Failed to register videoAdLoaded. Reason: " + Log.getStackTraceString(e));
        }
    }

    /**
     * Registers video ad started event.
     *
     * @param duration          native video ad duration.
     * @param videoPlayerVolume native video player volume.
     */
    public void videoAdStarted(final float duration, final float videoPlayerVolume) {
        if (mMediaEvents == null) {
            LogUtil.error(TAG, "Failed to register videoAdStarted. videoAdEvent is null");
            return;
        }
        mMediaEvents.start(duration, videoPlayerVolume);
    }

    /**
     * Signals the impression event occurring. Generally accepted to be on ad render.
     */
    public void registerImpression() {
        if (mAdEvents == null) {
            LogUtil.error(TAG, "Failed to registerImpression: AdEvent is null");
            return;
        }
        try {
            mAdEvents.impressionOccurred();
        }
        catch (IllegalArgumentException | IllegalStateException e) {
            LogUtil.error(TAG, "Failed to registerImpression: " + Log.getStackTraceString(e));
        }
    }

    /**
     * Registers volume change event.
     *
     * @param volume changed volume.
     */
    public void trackVolumeChange(float volume) {
        if (mMediaEvents == null) {
            LogUtil.error(TAG, "Failed to trackVolumeChange. videoAdEvent is null");
            return;
        }
        mMediaEvents.volumeChange(volume);
    }

    /**
     * <pre>
     * Registers ad video events.
     *
     * Playback:
     *                      {@link VideoAdEvent.Event#AD_FIRSTQUARTILE},
     *                      {@link VideoAdEvent.Event#AD_MIDPOINT},
     *                      {@link VideoAdEvent.Event#AD_THIRDQUARTILE},
     *                      {@link VideoAdEvent.Event#AD_COMPLETE};
     * Visibility:
     *                      {@link VideoAdEvent.Event#AD_PAUSE},
     *                      {@link VideoAdEvent.Event#AD_RESUME},
     * player state change:
     *                      {@link VideoAdEvent.Event#AD_FULLSCREEN},
     *                      {@link VideoAdEvent.Event#AD_EXITFULLSCREEN},
     * impression:
     *                      {@link VideoAdEvent.Event#AD_IMPRESSION}
     * adUserInteraction:
     *                      {@link VideoAdEvent.Event#AD_CLICK}
     *
     * @param adEvent events which are handled.
     * <pre/>
     */
    public void trackAdVideoEvent(VideoAdEvent.Event adEvent) {
        if (mMediaEvents == null) {
            LogUtil.error(TAG, "Failed to trackAdVideoEvent. videoAdEvent is null");
            return;
        }
        switch (adEvent) {
            case AD_PAUSE:
                mMediaEvents.pause();
                break;
            case AD_RESUME:
                mMediaEvents.resume();
                break;
            case AD_SKIP:
                mMediaEvents.skipped();
                break;
            case AD_COMPLETE:
                mMediaEvents.complete();
                break;
            case AD_FIRSTQUARTILE:
                mMediaEvents.firstQuartile();
                break;
            case AD_MIDPOINT:
                mMediaEvents.midpoint();
                break;
            case AD_THIRDQUARTILE:
                mMediaEvents.thirdQuartile();
                break;
            case AD_FULLSCREEN:
                trackPlayerStateChangeEvent(InternalPlayerState.FULLSCREEN);
                break;
            case AD_EXITFULLSCREEN:
                trackPlayerStateChangeEvent(InternalPlayerState.NORMAL);
                break;
            case AD_IMPRESSION:
                registerImpression();
                break;
            case AD_CLICK:
                trackAdUserInteractionEvent(InteractionType.CLICK);
                break;
        }
    }

    /**
     * <pre>
     * Registers display events.
     * Supported events:
     *                      {@link TrackingEvent.Events#IMPRESSION}
     *                      {@link TrackingEvent.Events#LOADED}
     *
     * @param adEvent events which are handled.
     * <pre/>
     */
    public void trackDisplayAdEvent(TrackingEvent.Events adEvent) {
        switch (adEvent) {
            case IMPRESSION:
                registerImpression();
                break;
            case LOADED:
                displayAdLoaded();
                break;
        }
    }

    /**
     * Registers video player state change events.
     *
     * @param playerState current video player state.
     */
    public void trackPlayerStateChangeEvent(InternalPlayerState playerState) {
        if (mMediaEvents == null) {
            LogUtil.error(TAG, "Failed to track PlayerStateChangeEvent. videoAdEvent is null");
            return;
        }
        mMediaEvents.playerStateChange(OmModelMapper.mapToPlayerState(playerState));
    }

    /**
     * Prepares the AdSessions for tracking. This does not trigger an impression.
     */
    public void startAdSession() {
        if (mAdSession == null) {
            LogUtil.error(TAG, "Failed to startAdSession. adSession is null");
            return;
        }
        mAdSession.start();
    }

    /**
     * Stop the AdSessions when the impression has completed and the ad will be destroyed
     */
    public void stopAdSession() {
        if (mAdSession == null) {
            LogUtil.error(TAG, "Failed to stopAdSession. adSession is null");
            return;
        }
        mAdSession.finish();
        mAdSession = null;
        mMediaEvents = null;
    }

    /**
     * Registers a view on which to track viewability.
     *
     * @param adView View on which to track viewability.
     */
    public void registerAdView(View adView) {
        if (mAdSession == null) {
            LogUtil.error(TAG, "Failed to registerAdView. adSession is null");
            return;
        }
        try {
            mAdSession.registerAdView(adView);
        }
        catch (IllegalArgumentException e) {
            LogUtil.error(TAG, "Failed to registerAdView. " + Log.getStackTraceString(e));
        }
    }

    /**
     * Registers any native view elements which are considered to be a part of the ad
     * (e.g. close button).
     */
    public void addObstruction(InternalFriendlyObstruction friendlyObstruction) {
        if (mAdSession == null) {
            LogUtil.error(TAG, "Failed to addObstruction: mAdSession is null");
            return;
        }
        try {
            FriendlyObstructionPurpose friendlyObstructionPurpose =
                OmModelMapper.mapToFriendlyObstructionPurpose(friendlyObstruction.getPurpose());
            mAdSession.addFriendlyObstruction(friendlyObstruction.getView(),
                                              friendlyObstructionPurpose,
                                              friendlyObstruction.getDetailedDescription());
        }
        catch (IllegalArgumentException e) {
            LogUtil.error(TAG, "Failed to addObstruction. Reason: " + Log.getStackTraceString(e));
        }
    }

    private void trackAdUserInteractionEvent(InteractionType type) {
        if (mMediaEvents == null) {
            LogUtil.error(TAG, "Failed to register adUserInteractionEvent with type: " + type);
            return;
        }

        mMediaEvents.adUserInteraction(type);
    }

    @Nullable
    private AdSessionConfiguration createAdSessionConfiguration(CreativeType creativeType, ImpressionType impressionType,
                                                                Owner impressionOwner, Owner mediaEventsOwner) {
        try {
            return AdSessionConfiguration.createAdSessionConfiguration(creativeType,
                                                                       impressionType,
                                                                       impressionOwner,
                                                                       mediaEventsOwner,
                                                                       false);
        }
        catch (IllegalArgumentException e) {
            LogUtil.error(TAG, "Failure createAdSessionConfiguration: " + Log.getStackTraceString(e));
            return null;
        }
    }

    private static boolean isActive() {
        try {
            return Omid.isActive();
        }
        catch (Throwable ignore) {
            LogUtil.error(TAG, "Failed to check OpenMeasurement status. Did you include omsdk-android? " + Log.getStackTraceString(ignore));
        }
        return false;
    }

    /**
     * Creates a Partner instance to identify the integration with the OMSDK.
     */
    private void initPartner() {
        try {
            mPartner = Partner.createPartner(PARTNER_NAME, PARTNER_VERSION);
        }
        catch (IllegalArgumentException e) {
            LogUtil.error(TAG, "Failed to initPartner. Reason: " + Log.getStackTraceString(e));
        }
    }

    /**
     * Initializes adSession. This does not trigger AdSession start.
     *
     * @param adSessionConfiguration concrete configuration (native/web) for AdSessions.
     * @param adSessionContext       concrete context (native/web) for AdSessions.
     */
    private void initAdSession(AdSessionConfiguration adSessionConfiguration, AdSessionContext adSessionContext) {
        if (mAdSession != null) {
            LogUtil.debug(TAG, "initAdSession: adSession is already created");
            return;
        }

        if (adSessionConfiguration == null || adSessionContext == null) {
            LogUtil.error(TAG, "Failure initAdSession. adSessionConfiguration OR adSessionContext is null");
            return;
        }

        mAdSession = AdSession.createAdSession(adSessionConfiguration, adSessionContext);
    }

    /**
     * Creates MediaEvents instance.
     */
    private void initMediaAdEvents() {
        try {
            mMediaEvents = MediaEvents.createMediaEvents(mAdSession);
        }
        catch (IllegalArgumentException e) {
            LogUtil.error(TAG, "Failure initMediaAdEvents: " + Log.getStackTraceString(e));
        }
    }

    /**
     * Creates AdEvents instance.
     */
    private void initAdEvents() {
        try {
            mAdEvents = AdEvents.createAdEvents(mAdSession);
        }
        catch (IllegalArgumentException e) {
            LogUtil.error(TAG, "Failure initAdEvents: " + Log.getStackTraceString(e));
        }
    }

    @Nullable
    private AdSessionContext createAdSessionContext(WebView adView, String contentUrl) {
        try {
            String customReferenceData = "";
            return AdSessionContext.createHtmlAdSessionContext(mPartner, adView, contentUrl, customReferenceData);
        }
        catch (IllegalArgumentException e) {
            LogUtil.error(TAG, "Failure createAdSessionContext: " + Log.getStackTraceString(e));
            return null;
        }
    }

    @Nullable
    private AdSessionContext createAdSessionContext(List<VerificationScriptResource> verifications, String contentUrl) {
        try {
            return AdSessionContext.createNativeAdSessionContext(mPartner,
                                                                 mJsLibraryManager.getOMSDKScript(),
                                                                 verifications,
                                                                 contentUrl,
                                                                 null);
        }
        catch (IllegalArgumentException e) {
            LogUtil.error(TAG, "Failure createAdSessionContext: " + Log.getStackTraceString(e));
            return null;
        }
    }

    @Nullable
    private AdSessionContext createAdSessionContext(AdVerifications adVerifications, String contentUrl) {
        if (adVerifications == null) {
            LogUtil.error(TAG, "Unable to createAdSessionContext. AdVerification is null");
            return null;
        }

        // Log all jsResources being used
        for (Verification verification : adVerifications.getVerifications()) {
            LogUtil.debug(TAG, "Using jsResource: " + verification.getJsResource());
        }

        try {
            List<VerificationScriptResource> verificationScriptResources = createVerificationScriptResources(adVerifications);
            return createAdSessionContext(verificationScriptResources, contentUrl);
        }
        catch (IllegalArgumentException e) {
            LogUtil.error(TAG, "Failure createAdSessionContext: " + Log.getStackTraceString(e));
            return null;
        }
        catch (MalformedURLException e) {
            LogUtil.error(TAG, "Failure createAdSessionContext: " + Log.getStackTraceString(e));
            return null;
        }
    }

    private List<VerificationScriptResource> createVerificationScriptResources(AdVerifications adVerifications)
    throws MalformedURLException, IllegalArgumentException {
        if (adVerifications == null || adVerifications.getVerifications() == null) {
            return null;
        }
        List<VerificationScriptResource> verificationScriptResources = new ArrayList<>();
        List<Verification> verificationList = adVerifications.getVerifications();

        for (Verification verification : verificationList) {
            final URL url = new URL(verification.getJsResource());
            final String vendorKey = verification.getVendor();
            final String params = verification.getVerificationParameters();

            VerificationScriptResource verificationScriptResource =
                VerificationScriptResource
                    .createVerificationScriptResourceWithParameters(vendorKey, url, params);
            verificationScriptResources.add(verificationScriptResource);
        }

        return verificationScriptResources;
    }

}
