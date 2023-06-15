package org.prebid.mobile.rendering.models.openrtb.bidRequests;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.prebid.mobile.NativeAsset;
import org.prebid.mobile.NativeEventTracker;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.configuration.NativeAdUnitConfiguration;

import java.util.ArrayList;
import java.util.List;

public class Native extends BaseBid {

    private JSONObject request;
    private Ext ext;

    // Won't be implemented in 1.1
    private int[] api;
    private int[] battr;

    public JSONObject getJsonObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("request", request.toString());
        jsonObject.put("ver", PrebidMobile.NATIVE_VERSION);
        jsonObject.putOpt("ext", ext != null ? ext.getJsonObject() : null);
        return jsonObject;
    }

    public void setRequestFrom(NativeAdUnitConfiguration config) {
        request = new JSONObject();
        try {
            request.put("ver", PrebidMobile.NATIVE_VERSION);
            if (config.getContextType() != null) {
                request.put("context", config.getContextType().getID());
            }
            if (config.getContextSubtype() != null) {
                request.put("contextsubtype", config.getContextSubtype().getID());
            }
            if (config.getPlacementType() != null) {
                request.put("plcmttype", config.getPlacementType().getID());
            }
            if (config.getSeq() >= 0) {
                request.put("seq", config.getSeq());
            }
            request.put("assets", getAssetsJsonArray(config.getAssets()));
            if (!config.getEventTrackers().isEmpty()) {
                request.put("eventtrackers", getTrackersJsonArray(config.getEventTrackers()));
            }
            if (config.getPrivacy()) {
                request.put("privacy", 1);
            }
            request.putOpt("ext", config.getExt() != null ? config.getExt() : null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Ext getExt() {
        if (ext == null) {
            ext = new Ext();
        }
        return ext;
    }

    private JSONArray getAssetsJsonArray(List<NativeAsset> assetList) throws JSONException {
        JSONArray assetJsonArray = new JSONArray();
        int idCount = 0;
        for (NativeAsset asset : assetList) {
            assetJsonArray.put(asset.getJsonObject(++idCount));
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