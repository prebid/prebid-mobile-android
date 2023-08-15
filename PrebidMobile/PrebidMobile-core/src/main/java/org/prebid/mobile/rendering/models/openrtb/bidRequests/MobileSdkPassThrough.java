package org.prebid.mobile.rendering.models.openrtb.bidRequests;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.api.data.Position;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.configuration.PBSConfig;
import org.prebid.mobile.core.BuildConfig;

/**
 * A class responsible for parsing "prebidmobilesdk" pass through type from bid response.
 * It contains interstitial control settings, like close button size or skip delay.
 * It can be located in ext.prebid.passthrough[] or seatbid[].bid[].ext.prebid.passthrough[].
 */
public class MobileSdkPassThrough {

    private static final String TAG = MobileSdkPassThrough.class.getSimpleName();

    @Nullable
    public static MobileSdkPassThrough create(JSONObject extJson) {
        try {
            JSONObject rootJsonObject;
            if (!BuildConfig.DEBUG) {
                if (extJson.has("prebid")) {
                    rootJsonObject = extJson.getJSONObject("prebid");
                } else return null;
            } else {
                rootJsonObject = extJson;
            }

            if (rootJsonObject.has("passthrough")) {
                JSONArray passThroughArray = rootJsonObject.getJSONArray("passthrough");
                for (int i = 0; i < passThroughArray.length(); i++) {
                    JSONObject passThrough = passThroughArray.getJSONObject(i);
                    if (passThrough.has("type")) {
                        String currentType = passThrough.getString("type");
                        if (currentType.equals("prebidmobilesdk") && (passThrough.has("adconfiguration")
                        || passThrough.has("sdkconfiguration"))) {
                            return new MobileSdkPassThrough(passThrough);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            LogUtil.error(TAG, "Can't parse json");
        }
        return null;
    }

    /**
     * Creates unified MobileSdkPassThrough. An object from bid has higher priority.
     *
     * @param fromBid  - object from seatbid[].bid[].ext.prebid.passthrough[]
     * @param fromRoot - object from ext.prebid.passthrough[]
     */
    @Nullable
    public static MobileSdkPassThrough combine(
        @Nullable MobileSdkPassThrough fromBid,
        @Nullable MobileSdkPassThrough fromRoot
    ) {
        if (fromBid == null && fromRoot == null) {
            return null;
        } else if (fromBid == null) {
            return fromRoot;
        } else if (fromRoot == null) {
            return fromBid;
        }

        if (fromBid.isMuted == null) {
            fromBid.isMuted = fromRoot.isMuted;
        }
        if (fromBid.maxVideoDuration == null) {
            fromBid.maxVideoDuration = fromRoot.maxVideoDuration;
        }
        if (fromBid.skipDelay == null) {
            fromBid.skipDelay = fromRoot.skipDelay;
        }
        if (fromBid.closeButtonArea == null) {
            fromBid.closeButtonArea = fromRoot.closeButtonArea;
        }
        if (fromBid.skipButtonArea == null) {
            fromBid.skipButtonArea = fromRoot.skipButtonArea;
        }
        if (fromBid.closeButtonPosition == null) {
            fromBid.closeButtonPosition = fromRoot.closeButtonPosition;
        }
        if (fromBid.skipButtonPosition == null) {
            fromBid.skipButtonPosition = fromRoot.skipButtonPosition;
        }
        return fromBid;
    }

    /**
     * Combines unified pass through object with rendering controls
     * from ad unit configuration. Settings from ad unit configuration
     * have lower priority.
     */
    @NonNull
    public static MobileSdkPassThrough combine(
        @Nullable MobileSdkPassThrough unifiedPassThrough,
        @NonNull AdUnitConfiguration configuration
    ) {
        MobileSdkPassThrough result;
        if (unifiedPassThrough == null) {
            result = new MobileSdkPassThrough();
        } else {
            result = unifiedPassThrough;
        }

        if (result.isMuted == null) {
            result.isMuted = configuration.isMuted();
        }
        if (result.maxVideoDuration == null) {
            result.maxVideoDuration = configuration.getMaxVideoDuration();
        }
        if (result.skipDelay == null) {
            result.skipDelay = configuration.getSkipDelay();
        }
        if (result.skipButtonArea == null) {
            result.skipButtonArea = configuration.getSkipButtonArea();
        }
        if (result.skipButtonPosition == null) {
            result.skipButtonPosition = configuration.getSkipButtonPosition();
        }
        if (result.closeButtonArea == null) {
            result.closeButtonArea = configuration.getCloseButtonArea();
        }
        if (result.closeButtonPosition == null) {
            result.closeButtonPosition = configuration.getCloseButtonPosition();
        }
        return result;
    }


    public Boolean isMuted;

    public Integer maxVideoDuration;
    public Integer skipDelay;

    public Double closeButtonArea;
    public Double skipButtonArea;

    public Position closeButtonPosition;
    public Position skipButtonPosition;

    public Integer bannerTimeout = 0;
    public Integer preRenderTimeout = 0;

    private JSONObject configuration;

    private MobileSdkPassThrough() {}

    private MobileSdkPassThrough(JSONObject passThrough) {
        try {
            if (passThrough.has("adconfiguration")) {
                configuration = passThrough.getJSONObject("adconfiguration");

                getAndSave("ismuted", Boolean.class, it -> isMuted = it);
                getAndSave("maxvideoduration", Integer.class, it -> maxVideoDuration = it);
                getAndSave("skipdelay", Integer.class, it -> skipDelay = it);
                getAndSave("closebuttonarea", Double.class, it -> closeButtonArea = it);
                getAndSave("skipbuttonarea", Double.class, it -> skipButtonArea = it);
                getAndSave("closebuttonposition", String.class, it -> closeButtonPosition = Position.fromString(it));
                getAndSave("skipbuttonposition", String.class, it -> skipButtonPosition = Position.fromString(it));
            }
        } catch (JSONException exception) {
            LogUtil.error(TAG, "Can't parse adconfiguration");
        }
        try {
            if (passThrough.has("sdkconfiguration")) {
                configuration = passThrough.getJSONObject("sdkconfiguration");
                if (configuration.has("cftbanner")) {
                    getAndSave("cftbanner", Integer.class, it -> bannerTimeout = it);
                }
                if (configuration.has("cftprerender")) {
                    getAndSave("cftprerender", Integer.class, it -> preRenderTimeout = it);
                }
                PrebidMobile.setPbsConfig(new PBSConfig(bannerTimeout, preRenderTimeout));
            }
        } catch (JSONException exception) {
            LogUtil.error(TAG, "Can't parse sdkconfiguration");
        }

    }


    public void modifyAdUnitConfiguration(AdUnitConfiguration adUnitConfiguration) {
        if (isMuted != null) {
            adUnitConfiguration.setIsMuted(isMuted);
        }
        if (maxVideoDuration != null) {
            adUnitConfiguration.setMaxVideoDuration(maxVideoDuration);
        }
        if (skipDelay != null) {
            adUnitConfiguration.setSkipDelay(skipDelay);
        }
        if (closeButtonArea != null) {
            adUnitConfiguration.setCloseButtonArea(closeButtonArea);
        }
        if (skipButtonArea != null) {
            adUnitConfiguration.setSkipButtonArea(skipButtonArea);
        }
        if (closeButtonPosition != null) {
            adUnitConfiguration.setCloseButtonPosition(closeButtonPosition);
        }
        if (skipButtonPosition != null) {
            adUnitConfiguration.setSkipButtonPosition(skipButtonPosition);
        }
    }

    private <T> void getAndSave(
        String key,
        Class<T> classType,
        AfterCast<T> afterCast
    ) {
        try {
            if (configuration.has(key)) {
                T result = classType.cast(configuration.get(key));
                afterCast.save(result);
            }
        } catch (JSONException e) {
            LogUtil.error(TAG, "Object " + key + " has wrong type!");
        }
    }

    private interface AfterCast<T> {

        void save(T variable);

    }

}
