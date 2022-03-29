package org.prebid.mobile;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ExtObject implements Serializable {

    private Map<String, Object> extValuesHashMap = new HashMap<>();

    public JSONObject getJsonObject() {
        return new JSONObject(extValuesHashMap);
    }

    public void put(String key, String value) {
        extValuesHashMap.put(key, value);
    }

    public void put(String key, Integer value) {
        extValuesHashMap.put(key, value);
    }

    public void put(String key, JSONObject value) {
        extValuesHashMap.put(key, value);
    }

    public void put(String key, JSONArray value) {
        extValuesHashMap.put(key, value);
    }

    public void put(JSONObject jsonObject) {
        if (jsonObject == null) {
            return;
        }
        Iterator<String> jsonIterator = jsonObject.keys();
        while (jsonIterator.hasNext()) {
            String key = jsonIterator.next();
            extValuesHashMap.put(key, jsonObject.opt(key));
        }
    }

    public void remove(String key) {
        extValuesHashMap.remove(key);
    }

    public Map<String, Object> getMap() {
        return extValuesHashMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ExtObject ext = (ExtObject) o;

        return extValuesHashMap != null ? extValuesHashMap.equals(ext.extValuesHashMap) : ext.extValuesHashMap == null;
    }

    @Override
    public int hashCode() {
        return extValuesHashMap != null ? extValuesHashMap.hashCode() : 0;
    }
}
