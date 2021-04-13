package org.prebid.mobile.rendering.networking.parameters;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Pair;

import org.json.JSONObject;
import org.prebid.mobile.rendering.bidding.data.AdSize;
import org.prebid.mobile.rendering.bidding.data.bid.Prebid;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.models.AdConfiguration.AdUnitIdentifierType;
import org.prebid.mobile.rendering.models.PlacementType;
import org.prebid.mobile.rendering.models.openrtb.BidRequest;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.Imp;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.User;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.devices.Geo;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.imps.Banner;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.imps.Video;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.source.Source;
import org.prebid.mobile.rendering.networking.targeting.Targeting;
import org.prebid.mobile.rendering.sdk.PrebidRenderingSettings;
import org.prebid.mobile.rendering.session.manager.OmAdSessionManager;
import org.prebid.mobile.rendering.utils.helpers.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class BasicParameterBuilder extends ParameterBuilder {

    public static final String[] SUPPORTED_VIDEO_MIME_TYPES = new String[]{
        "video/mp4",
        "video/3gpp",
        "video/webm",
        "video/mkv"};

    static final String DISPLAY_MANAGER_VALUE = "prebid-mobile";
    static final String KEY_OM_PARTNER_NAME = "omidpn";
    static final String KEY_OM_PARTNER_VERSION = "omidpv";
    static final String KEY_DEEPLINK_PLUS = "dlp";

    // 2 - VAST 2.0
    // 5 - VAST 2.0 Wrapper
    static final int[] SUPPORTED_VIDEO_PROTOCOLS = new int[]{2, 5};

    // 2 - On Leaving Viewport or when Terminated by User
    static final int VIDEO_INTERSTITIAL_PLAYBACK_END = 2;
    //term to say cached locally as per Mopub & dfp - approved by product
    static final int VIDEO_DELIVERY_DOWNLOAD = 3;
    static final int VIDEO_LINEARITY_LINEAR = 1;
    static final int API_OPEN_MEASUREMENT = 7;

    /**
     * 1 - VPAID 1.0
     * 2 - VPAID 2.0
     * 3 - MRAID-1
     * 4 - ORMMA
     * 5 - MRAID-2
     * 6 - MRAID-3
     */
    private static final List<Integer> SUPPORTED_MRAID_VERSIONS = Arrays.asList(3, 5, 6);

    private final AdConfiguration mAdConfiguration;
    private final boolean mBrowserActivityAvailable;
    private final Resources mResources;

    public BasicParameterBuilder(AdConfiguration adConfiguration, Resources resources, boolean browserActivityAvailable) {
        mAdConfiguration = adConfiguration;
        mBrowserActivityAvailable = browserActivityAvailable;
        mResources = resources;
    }

    @Override
    public void appendBuilderParameters(AdRequestInput adRequestInput) {
        final String uuid = UUID.randomUUID().toString();

        configureBidRequest(adRequestInput.getBidRequest(), uuid);
        configureSource(adRequestInput.getBidRequest().getSource(), uuid);
        appendUserTargetingParameters(adRequestInput);

        ArrayList<Imp> impsArrayList = adRequestInput.getBidRequest().getImp();
        if (impsArrayList != null) {
            Imp newImp = new Imp();
            configureImpObject(newImp, uuid);
            impsArrayList.add(newImp);
        }
    }

    private void configureImpObject(Imp imp, String uuid) {
        if (mAdConfiguration != null) {
            setDisplayManager(imp);
            setCommonImpValues(imp, uuid);
            if (mAdConfiguration.getNativeAdConfiguration() != null) {
                setNativeImpValues(imp);
            }
            else if (mAdConfiguration.isAdType(AdUnitIdentifierType.VAST)) {
                setVideoImpValues(imp);
            }
            else {
                setBannerImpValues(imp);
            }
        }
    }

    private void configureBidRequest(BidRequest bidRequest, String uuid) {
        bidRequest.setId(uuid);
        boolean isVideo = mAdConfiguration.isAdType(AdConfiguration.AdUnitIdentifierType.VAST);
        bidRequest.getExt().put("prebid", Prebid.getJsonObjectForBidRequest(PrebidRenderingSettings.getAccountId(), isVideo));
        //if coppaEnabled - set 1, else No coppa is sent
        if (PrebidRenderingSettings.isCoppaEnabled) {
            bidRequest.getRegs().coppa = 1;
        }
    }

    private void configureSource(Source source, String uuid) {
        source.setTid(uuid);
        source.getExt().put(KEY_OM_PARTNER_NAME, OmAdSessionManager.PARTNER_NAME);
        source.getExt().put(KEY_OM_PARTNER_VERSION, OmAdSessionManager.PARTNER_VERSION);
    }

    private void appendUserTargetingParameters(AdRequestInput adRequestInput) {
        final BidRequest bidRequest = adRequestInput.getBidRequest();
        final User user = bidRequest.getUser();

        user.id = Targeting.getUserId();
        user.yob = Targeting.getUserYob();
        user.keywords = Targeting.getUserKeyWords();
        user.customData = Targeting.getUserCustomData();
        user.gender = Targeting.getUserGender();
        user.buyerUid = Targeting.getBuyerUid();
        user.ext = Targeting.getUserExt();

        final Map<String, Set<String>> userDataDictionary = Targeting.getUserDataDictionary();
        if (!userDataDictionary.isEmpty()) {
            user.getExt().put("data", Utils.toJson(userDataDictionary));
        }

        if (Targeting.getEids() != null) {
            user.getExt().put("eids", Targeting.getEids());
        }

        final Pair<Float, Float> userLatLng = Targeting.getUserLatLng();
        if (userLatLng != null) {
            final Geo userGeo = user.getGeo();
            userGeo.lat = userLatLng.first;
            userGeo.lon = userLatLng.second;
        }
    }

    private void setVideoImpValues(Imp imp) {
        Video video = new Video();
        //Common values for all video reqs
        video.mimes = SUPPORTED_VIDEO_MIME_TYPES;
        video.protocols = SUPPORTED_VIDEO_PROTOCOLS;
        video.linearity = VIDEO_LINEARITY_LINEAR;

        //Interstitial video specific values
        video.playbackend = VIDEO_INTERSTITIAL_PLAYBACK_END;//On Leaving Viewport or when Terminated by User
        video.delivery = new int[]{VIDEO_DELIVERY_DOWNLOAD};

        if (mAdConfiguration.isAdPositionValid()) {
            video.pos = mAdConfiguration.getAdPositionValue();
        }

        if (!mAdConfiguration.isPlacementTypeValid()) {
            video.placement = PlacementType.INTERSTITIAL.getValue();
            if (mResources != null) {
                Configuration deviceConfiguration = mResources.getConfiguration();
                video.w = deviceConfiguration.screenWidthDp;
                video.h = deviceConfiguration.screenHeightDp;
            }
        }
        else {
            video.placement = mAdConfiguration.getPlacementTypeValue();
            for (AdSize size : mAdConfiguration.getAdSizes()) {
                video.w = size.width;
                video.h = size.height;
                break;
            }
        }

        imp.video = video;
    }

    private void setBannerImpValues(Imp imp) {
        Banner banner = new Banner();
        banner.api = getApiFrameworks();

        if (mAdConfiguration.isAdType(AdUnitIdentifierType.BANNER)) {
            for (AdSize size : mAdConfiguration.getAdSizes()) {
                banner.addFormat(size.width, size.height);
            }
        }
        else if (mAdConfiguration.isAdType(AdUnitIdentifierType.INTERSTITIAL) && mResources != null) {
            Configuration deviceConfiguration = mResources.getConfiguration();
            banner.addFormat(deviceConfiguration.screenWidthDp,
                             deviceConfiguration.screenHeightDp);
        }

        if (mAdConfiguration.isAdPositionValid()) {
            banner.pos = mAdConfiguration.getAdPositionValue();
        }

        imp.banner = banner;
    }

    private void setCommonImpValues(Imp imp, String uuid) {
        imp.id = uuid;
        boolean isInterstitial = mAdConfiguration.isAdType(AdUnitIdentifierType.VAST) ||
                                 mAdConfiguration.isAdType(AdUnitIdentifierType.INTERSTITIAL);
        //Send 1 for interstitial/interstitial video and 0 for banners
        imp.instl = isInterstitial ? 1 : 0;
        // 0 == embedded, 1 == native
        imp.clickBrowser = !PrebidRenderingSettings.useExternalBrowser && mBrowserActivityAvailable ? 0 : 1;
        //set secure=1 for https or secure=0 for http
        if (!mAdConfiguration.isAdType(AdUnitIdentifierType.VAST)) {
            imp.secure = 1;
        }
        imp.getExt().put("prebid", Prebid.getJsonObjectForImp(mAdConfiguration));

        final Map<String, Set<String>> contextDataDictionary = mAdConfiguration.getContextDataDictionary();
        if (!contextDataDictionary.isEmpty()) {
            JSONObject data = Utils.toJson(contextDataDictionary);
            JSONObject context = new JSONObject();
            Utils.addValue(context, "data", data);
            imp.getExt().put("context", context);
        }

        // TODO: 15.12.2020 uncomment when Prebid server will be able to process Ext content not related to bidders
        //imp.getExt().put(KEY_DEEPLINK_PLUS, 1);
    }

    private void setNativeImpValues(Imp imp) {
        if (mAdConfiguration.getNativeAdConfiguration() != null) {
            imp.getNative().setRequestFrom(mAdConfiguration.getNativeAdConfiguration());
        }
    }

    private void setDisplayManager(Imp imp) {
        imp.displaymanager = DISPLAY_MANAGER_VALUE;
        imp.displaymanagerver = PrebidRenderingSettings.SDK_VERSION;
    }

    private int[] getApiFrameworks() {
        List<Integer> supportedApiFrameworks = new ArrayList<>();

        // If MRAID is on, then add api(3,5)
        if (PrebidRenderingSettings.sendMraidSupportParams) {
            supportedApiFrameworks.addAll(SUPPORTED_MRAID_VERSIONS);
        }

        // Add OM support
        supportedApiFrameworks.add(API_OPEN_MEASUREMENT);

        // If list of supported frameworks is not empty, set api field
        if (!supportedApiFrameworks.isEmpty()) {
            // Remove duplicates
            supportedApiFrameworks = new ArrayList<>(new HashSet<>(supportedApiFrameworks));

            // Create api array
            int[] result = new int[supportedApiFrameworks.size()];
            for (int i = 0; i < supportedApiFrameworks.size(); i++) {
                result[i] = supportedApiFrameworks.get(i);
            }

            return result;
        }
        else {
            return null;
        }
    }
}
