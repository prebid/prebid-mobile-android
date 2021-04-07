package com.openx.apollo.models.openrtb.bidRequests;

import com.openx.apollo.models.ntv.NativeAdConfiguration;
import com.openx.apollo.models.ntv.NativeEventTracker;
import com.openx.apollo.models.openrtb.bidRequests.assets.NativeAsset;
import com.openx.apollo.sdk.ApolloSettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
        jsonObject.put("ver", ApolloSettings.NATIVE_VERSION);
        jsonObject.putOpt("ext", mExt != null ? mExt.getJsonObject() : null);
        return jsonObject;
    }

    public void setRequestFrom(NativeAdConfiguration config) {
        mRequest = new JSONObject();
        try {
            mRequest.put("ver", ApolloSettings.NATIVE_VERSION);
            if (config.getContextType() != null) {
                mRequest.put("context", config.getContextType().getId());
            }
            if (config.getContextSubType() != null) {
                mRequest.put("contextsubtype", config.getContextSubType().getId());
            }
            if (config.getPlacementType() != null) {
                mRequest.put("plcmttype", config.getPlacementType().getId());
            }
            if (config.getSeq() != null && config.getSeq() >= 0) {
                mRequest.put("seq", config.getSeq());
            }
            mRequest.put("assets", getAssetsJsonArray(config.getAssets()));
            if (!config.getTrackers().isEmpty()) {
                mRequest.put("eventtrackers", getTrackersJsonArray(config.getTrackers()));
            }
            if (config.getPrivacy()) {
                mRequest.put("privacy", 1);
            }
            mRequest.putOpt("ext", config.getExt() != null
                                   ? config.getExt().getJsonObject()
                                   : null);
        }
        catch (JSONException e) {
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
            assetJsonArray.put(asset.getAssetJsonObject());
        }
        return assetJsonArray;
    }

    private JSONArray getTrackersJsonArray(List<NativeEventTracker> trackerList)
    throws JSONException {
        JSONArray trackersJsonArray = new JSONArray();
        for (NativeEventTracker eventTracker : trackerList) {
            JSONObject evenTrackerJson = new JSONObject();
            if (eventTracker.getEventType() != null) {
                evenTrackerJson.put("event", eventTracker.getEventType().getId());
            }

            ArrayList<NativeEventTracker.EventTrackingMethod> eventTrackingMethods = eventTracker.getEventTrackingMethods();
            if (eventTrackingMethods != null) {
                JSONArray methodsArray = new JSONArray();
                for (NativeEventTracker.EventTrackingMethod method : eventTrackingMethods) {
                    methodsArray.put(method.getId());
                }
                evenTrackerJson.put("methods", methodsArray);
            }

            if (eventTracker.getExt() != null) {
                evenTrackerJson.put("ext", eventTracker.getExt().getJsonObject());
            }
            trackersJsonArray.put(evenTrackerJson);
        }
        return trackersJsonArray;
    }
}
