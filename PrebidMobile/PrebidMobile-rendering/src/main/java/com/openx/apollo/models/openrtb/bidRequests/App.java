package com.openx.apollo.models.openrtb.bidRequests;

import com.openx.apollo.models.openrtb.bidRequests.apps.Publisher;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class App extends BaseBid {

    public String id = null;
    public String name = null;
    public String bundle = null;
    public String domain = null;
    //TODO: ORTB2.5: remove this? After product's decision?
    public String storeurl = null;
    public String[] cat = null;
    public String[] sectioncat = null;
    public String[] pagecat = null;
    public String ver = null;
    public Integer privacypolicy = null;
    public Integer paid = null;
    public String keywords = null;
    private Publisher mPublisher = null;
    private Ext mExt = null;

    public JSONObject getJsonObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();

        toJSON(jsonObject, "id", id);
        toJSON(jsonObject, "name", name);
        toJSON(jsonObject, "bundle", bundle);
        toJSON(jsonObject, "domain", domain);
        toJSON(jsonObject, "storeurl", storeurl);
        if (cat != null) {

            JSONArray jsonArray = new JSONArray();

            for (String cat : cat) {

                jsonArray.put(cat);
            }
            toJSON(jsonObject, "cat", jsonArray);
        }

        if (sectioncat != null) {

            JSONArray jsonArray = new JSONArray();

            for (String sectionCat : sectioncat) {

                jsonArray.put(sectionCat);
            }
            toJSON(jsonObject, "sectioncat", jsonArray);
        }

        if (pagecat != null) {

            JSONArray jsonArray = new JSONArray();

            for (String pageCat : pagecat) {

                jsonArray.put(pageCat);
            }
            toJSON(jsonObject, "pagecat", jsonArray);
        }

        toJSON(jsonObject, "ver", ver);
        toJSON(jsonObject, "privacypolicy", privacypolicy);
        toJSON(jsonObject, "paid", paid);
        toJSON(jsonObject, "keywords", keywords);
        toJSON(jsonObject, "publisher", (mPublisher != null)
                                        ? this.mPublisher.getJsonObject()
                                        : null);
        toJSON(jsonObject, "ext", (mExt != null)
                                  ? mExt.getJsonObject()
                                  : null);

        return jsonObject;
    }

    public Publisher getPublisher(){
        if(mPublisher == null){
            mPublisher = new Publisher();
        }
        return mPublisher;
    }

    public Ext getExt(){
        if(mExt == null){
            mExt = new Ext();
        }
        return mExt;
    }
}
