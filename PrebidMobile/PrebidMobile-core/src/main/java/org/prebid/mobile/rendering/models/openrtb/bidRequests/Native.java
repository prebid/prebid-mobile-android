package org.prebid.mobile.rendering.models.openrtb.bidRequests;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.prebid.mobile.NativeAsset;
import org.prebid.mobile.NativeEventTracker;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.units.configuration.NativeAdUnitConfiguration;

import java.util.ArrayList;
import java.util.List;

public class Native extends BaseBid {
    private JSONObject mRequest;
    private Ext mExt;

    // Won't be implemented in 1.1
    private int[] mApi;
    private int[] mBattr;

    public JSONObject getJsonObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("request", mRequest.toString());
        jsonObject.put("ver", PrebidMobile.NATIVE_VERSION);
        jsonObject.putOpt("ext", mExt != null ? mExt.getJsonObject() : null);
        return jsonObject;
    }

    public void setRequestFrom(NativeAdUnitConfiguration config) {
        mRequest = new JSONObject();
        try {
            mRequest.put("ver", PrebidMobile.NATIVE_VERSION);
            if (config.getContextType() != null) {
                mRequest.put("context", config.getContextType().getID());
            }
            if (config.getContextSubtype() != null) {
                mRequest.put("contextsubtype", config.getContextSubtype().getID());
            }
            if (config.getPlacementType() != null) {
                mRequest.put("plcmttype", config.getPlacementType().getID());
            }
            if (config.getSeq() >= 0) {
                mRequest.put("seq", config.getSeq());
            }
            mRequest.put("assets", getAssetsJsonArray(config.getAssets()));
            if (!config.getEventTrackers().isEmpty()) {
                mRequest.put("eventtrackers", getTrackersJsonArray(config.getEventTrackers()));
            }
            if (config.getPrivacy()) {
                mRequest.put("privacy", 1);
            }
            mRequest.putOpt("ext", config.getExt() != null
                    ? config.getExt()
                    : null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Ext getExt() {
        if (mExt == null) {
            mExt = new Ext();
        }
        return mExt;
    }

    private JSONArray getAssetsJsonArray(List<NativeAsset> assetList) throws JSONException {
        JSONArray assetJsonArray = new JSONArray();
        for (NativeAsset asset : assetList) {
            assetJsonArray.put(asset.getJsonObject());
        }
        return assetJsonArray;
    }

    private JSONArray getTrackersJsonArray(List<NativeEventTracker> trackerList)
            throws JSONException {
        JSONArray trackersJsonArray = new JSONArray();
        for (NativeEventTracker eventTracker : trackerList) {
            JSONObject evenTrackerJson = new JSONObject();
            if (eventTracker.getEvent() != null) {
                evenTrackerJson.put("event", eventTracker.getEvent().getID());
            }

            ArrayList<NativeEventTracker.EVENT_TRACKING_METHOD> eventTrackingMethods = eventTracker.getMethods();
            if (eventTrackingMethods != null) {
                JSONArray methodsArray = new JSONArray();
                for (NativeEventTracker.EVENT_TRACKING_METHOD method : eventTrackingMethods) {
                    methodsArray.put(method.getID());
                }
                evenTrackerJson.put("methods", methodsArray);
            }

            if (eventTracker.getExtObject() != null) {
                evenTrackerJson.put("ext", eventTracker.getExtObject());
            }
            trackersJsonArray.put(evenTrackerJson);
        }
        return trackersJsonArray;
    }
}