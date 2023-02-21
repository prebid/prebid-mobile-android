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

package org.prebid.mobile.rendering.networking.parameters;

import static org.prebid.mobile.PrebidMobile.SDK_VERSION;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONObject;
import org.prebid.mobile.AdSize;
import org.prebid.mobile.BannerBaseAdUnit;
import org.prebid.mobile.DataObject;
import org.prebid.mobile.ExternalUserId;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.Signals;
import org.prebid.mobile.TargetingParams;
import org.prebid.mobile.VideoBaseAdUnit;
import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.Prebid;
import org.prebid.mobile.rendering.models.PlacementType;
import org.prebid.mobile.rendering.models.openrtb.BidRequest;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.Imp;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.User;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.devices.Geo;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.imps.Banner;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.imps.Video;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.source.Source;
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

    private final AdUnitConfiguration adConfiguration;
    private final boolean browserActivityAvailable;
    private final Resources resources;

    public BasicParameterBuilder(
            AdUnitConfiguration adConfiguration,
            Resources resources,
            boolean browserActivityAvailable
    ) {
        this.adConfiguration = adConfiguration;
        this.browserActivityAvailable = browserActivityAvailable;
        this.resources = resources;
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
        if (adConfiguration != null) {
            setDisplayManager(imp);
            setCommonImpValues(imp, uuid);
            if (adConfiguration.getNativeConfiguration() != null) {
                setNativeImpValues(imp);
            } else {
                if (adConfiguration.isAdType(AdFormat.BANNER) || adConfiguration.isAdType(AdFormat.INTERSTITIAL)) {
                    setBannerImpValues(imp);
                }
                if (adConfiguration.isAdType(AdFormat.VAST)) {
                    setVideoImpValues(imp);
                }
            }
        }
    }

    private void configureBidRequest(BidRequest bidRequest, String uuid) {
        bidRequest.setId(uuid);
        boolean isVideo = adConfiguration.isAdType(AdFormat.VAST);
        bidRequest.getExt().put("prebid", Prebid.getJsonObjectForBidRequest(PrebidMobile.getPrebidServerAccountId(), isVideo, adConfiguration.isOriginalAdUnit()));
        //if coppaEnabled - set 1, else No coppa is sent
        if (PrebidMobile.isCoppaEnabled) {
            bidRequest.getRegs().coppa = 1;
        }
    }

    private void configureSource(Source source, String uuid) {
        source.setTid(uuid);

        boolean isNotOriginalApi = !adConfiguration.isOriginalAdUnit();

        String userDefinedPartnerName = TargetingParams.getOmidPartnerName();
        if (userDefinedPartnerName != null && !userDefinedPartnerName.isEmpty()) {
            source.getExt().put(KEY_OM_PARTNER_NAME, userDefinedPartnerName);
        } else if (isNotOriginalApi) {
            source.getExt().put(KEY_OM_PARTNER_NAME, OmAdSessionManager.PARTNER_NAME);
        }

        String userDefinedPartnerVersion = TargetingParams.getOmidPartnerVersion();
        if (userDefinedPartnerVersion != null && !userDefinedPartnerVersion.isEmpty()) {
            source.getExt().put(KEY_OM_PARTNER_VERSION, userDefinedPartnerVersion);
        } else if (isNotOriginalApi) {
            source.getExt().put(KEY_OM_PARTNER_VERSION, OmAdSessionManager.PARTNER_VERSION);
        }
    }

    private void appendUserTargetingParameters(AdRequestInput adRequestInput) {
        final BidRequest bidRequest = adRequestInput.getBidRequest();
        final User user = bidRequest.getUser();

        user.id = TargetingParams.getUserId();
        user.keywords = TargetingParams.getUserKeywords();
        user.customData = TargetingParams.getUserCustomData();
        user.buyerUid = TargetingParams.getBuyerId();
        user.ext = TargetingParams.getUserExt();

        ArrayList<DataObject> userData = adConfiguration.getUserData();
        if (!userData.isEmpty()) {
            user.dataObjects = userData;
        }

        int yearOfBirth = TargetingParams.getYearOfBirth();
        if (yearOfBirth != 0) {
            user.yob = TargetingParams.getYearOfBirth();
        }

        TargetingParams.GENDER gender = TargetingParams.getGender();
        if (gender != TargetingParams.GENDER.UNKNOWN) {
            user.gender = gender.getKey();
        }

        final Map<String, Set<String>> userDataDictionary = TargetingParams.getUserDataDictionary();
        if (!userDataDictionary.isEmpty()) {
            user.getExt().put("data", Utils.toJson(userDataDictionary));
        }

        List<ExternalUserId> extendedIds = TargetingParams.fetchStoredExternalUserIds();
        if (extendedIds != null && extendedIds.size() > 0) {
            JSONArray idsJson = new JSONArray();
            for (ExternalUserId id : extendedIds) {
                if (id != null) {
                    idsJson.put(id.getJson());
                }
            }
            user.getExt().put("eids", idsJson);
        }

        final Pair<Float, Float> userLatLng = TargetingParams.getUserLatLng();
        if (userLatLng != null) {
            final Geo userGeo = user.getGeo();
            userGeo.lat = userLatLng.first;
            userGeo.lon = userLatLng.second;
        }
    }

    private void setVideoImpValues(Imp imp) {
        Video video = new Video();
        if (adConfiguration.isOriginalAdUnit()) {
            VideoBaseAdUnit.Parameters videoParameters = adConfiguration.getVideoParameters();
            if (videoParameters != null) {
                video.minduration = videoParameters.getMinDuration();
                video.maxduration = videoParameters.getMaxDuration();

                video.minbitrate = videoParameters.getMinBitrate();
                video.maxbitrate = videoParameters.getMaxBitrate();
                video.linearity = videoParameters.getLinearity();
                if (videoParameters.getPlacement() != null) {
                    video.placement = videoParameters.getPlacement().getValue();
                } else if (adConfiguration.isPlacementTypeValid()){
                    video.placement = adConfiguration.getPlacementTypeValue();
                }

                if (videoParameters.getStartDelay() != null) {
                    video.startDelay = videoParameters.getStartDelay().getValue();
                }

                List<Signals.PlaybackMethod> playbackObjects = videoParameters.getPlaybackMethod();
                if (playbackObjects != null) {
                    int size = playbackObjects.size();
                    int[] playbackMethods = new int[size];

                    for (int i = 0; i < size; i++) {
                        playbackMethods[i] = playbackObjects.get(i).getValue();
                    }

                    video.playbackmethod = playbackMethods;
                }

                List<Signals.Api> apiObjects = videoParameters.getApi();
                if (apiObjects != null && apiObjects.size() > 0) {
                    int size = apiObjects.size();
                    int[] apiArray = new int[size];
                    for (int i = 0; i < size; i++) {
                        apiArray[i] = apiObjects.get(i).getValue();
                    }
                    video.api = apiArray;
                }

                List<String> mimesObjects = videoParameters.getMimes();
                if (mimesObjects != null && mimesObjects.size() > 0) {
                    int size = mimesObjects.size();
                    String[] mimesArray = new String[size];
                    for (int i = 0; i < size; i++) {
                        mimesArray[i] = mimesObjects.get(i);
                    }
                    video.mimes = mimesArray;
                }

                List<Signals.Protocols> protocolsObjects = videoParameters.getProtocols();
                if (protocolsObjects != null && protocolsObjects.size() > 0) {
                    int size = protocolsObjects.size();
                    int[] protocolsArray = new int[size];
                    for (int i = 0; i < size; i++) {
                        protocolsArray[i] = protocolsObjects.get(i).getValue();
                    }
                    video.protocols = protocolsArray;
                }
            }
            if (video.placement == null && adConfiguration.isPlacementTypeValid()) {
                video.placement = adConfiguration.getPlacementTypeValue();
            }
        } else {
            //Common values for all video reqs
            video.mimes = SUPPORTED_VIDEO_MIME_TYPES;
            video.protocols = SUPPORTED_VIDEO_PROTOCOLS;
            video.linearity = VIDEO_LINEARITY_LINEAR;

            //Interstitial video specific values
            video.playbackend = VIDEO_INTERSTITIAL_PLAYBACK_END;//On Leaving Viewport or when Terminated by User

            if (adConfiguration.isAdPositionValid()) {
                video.pos = adConfiguration.getAdPositionValue();
            }

            if (!adConfiguration.isPlacementTypeValid()) {
                video.placement = PlacementType.INTERSTITIAL.getValue();
            } else {
                video.placement = adConfiguration.getPlacementTypeValue();
            }
        }

        HashSet<AdSize> adSizes = adConfiguration.getSizes();
        if (!adSizes.isEmpty()) {
            for (AdSize size : adConfiguration.getSizes()) {
                video.w = size.getWidth();
                video.h = size.getHeight();
                break;
            }
        } else if (resources != null) {
            Configuration deviceConfiguration = resources.getConfiguration();
            video.w = deviceConfiguration.screenWidthDp;
            video.h = deviceConfiguration.screenHeightDp;
        }
        video.delivery = new int[]{VIDEO_DELIVERY_DOWNLOAD};

        imp.video = video;
    }

    private void setBannerImpValues(Imp imp) {
        Banner banner = new Banner();
        if (adConfiguration.isOriginalAdUnit()) {
            BannerBaseAdUnit.Parameters parameters = adConfiguration.getBannerParameters();
            if (parameters != null && parameters.getApi() != null && parameters.getApi().size() > 0) {
                List<Signals.Api> apiObjects = parameters.getApi();
                int[] api = new int[apiObjects.size()];
                for (int i = 0; i < apiObjects.size(); i++) {
                    api[i] = apiObjects.get(i).getValue();
                }
                banner.api = api;
            }
        } else {
            banner.api = getApiFrameworks();
        }

        if (adConfiguration.isAdType(AdFormat.BANNER)) {
            for (AdSize size : adConfiguration.getSizes()) {
                banner.addFormat(size.getWidth(), size.getHeight());
            }
        } else if (adConfiguration.isAdType(AdFormat.INTERSTITIAL) && resources != null) {
            Configuration deviceConfiguration = resources.getConfiguration();
            banner.addFormat(deviceConfiguration.screenWidthDp, deviceConfiguration.screenHeightDp);
        }

        if (adConfiguration.isAdPositionValid()) {
            banner.pos = adConfiguration.getAdPositionValue();
        }

        imp.banner = banner;
    }

    private void setNativeImpValues(Imp imp) {
        if (adConfiguration.getNativeConfiguration() != null) {
            imp.getNative().setRequestFrom(adConfiguration.getNativeConfiguration());
        }
    }

    private void setCommonImpValues(Imp imp, String uuid) {
        imp.id = uuid;
        boolean isInterstitial = adConfiguration.isAdType(AdFormat.VAST) || adConfiguration.isAdType(AdFormat.INTERSTITIAL);
        //Send 1 for interstitial/interstitial video and 0 for banners
        imp.instl = isInterstitial ? 1 : 0;
        // 0 == embedded, 1 == native
        imp.clickBrowser = !PrebidMobile.useExternalBrowser && browserActivityAvailable ? 0 : 1;
        //set secure=1 for https or secure=0 for http
        if (!adConfiguration.isAdType(AdFormat.VAST)) {
            imp.secure = 1;
        }
        imp.getExt().put("prebid", Prebid.getJsonObjectForImp(adConfiguration));

        final Map<String, Set<String>> extDataDictionary = adConfiguration.getExtDataDictionary();
        JSONObject data = Utils.toJson(extDataDictionary);
        Utils.addValue(data, "adslot", adConfiguration.getPbAdSlot());
        if (data.length() > 0) {
            imp.getExt().put("data", data);
        }

        final Set<String> extKeywords = adConfiguration.getExtKeywordsSet();
        if (extKeywords.size() > 0) {
            String string = TextUtils.join(",", extKeywords);
            imp.getExt().put("keywords", string);
        }

        // TODO: 15.12.2020 uncomment when Prebid server will be able to process Ext content not related to bidders
        //imp.getExt().put(KEY_DEEPLINK_PLUS, 1);
    }

    private void setDisplayManager(Imp imp) {
        imp.displaymanager = adConfiguration.isOriginalAdUnit() ? null : DISPLAY_MANAGER_VALUE;
        imp.displaymanagerver = adConfiguration.isOriginalAdUnit() ? null : SDK_VERSION;
    }

    private int[] getApiFrameworks() {
        List<Integer> supportedApiFrameworks = new ArrayList<>();

        // If MRAID is on, then add api(3,5)
        if (PrebidMobile.sendMraidSupportParams) {
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
