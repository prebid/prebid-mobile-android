package org.prebid.mobile.units.configuration;

import androidx.annotation.NonNull;
import org.prebid.mobile.AdType;
import org.prebid.mobile.ContentObject;
import org.prebid.mobile.DataObject;

import java.util.*;

public abstract class BaseAdUnitConfiguration {

    protected AdType adType;
    protected String configId;
    protected ContentObject appContent;
    protected String pbAdSlot;
    protected final ArrayList<DataObject> userDataObjects = new ArrayList<>();
    protected final Map<String, Set<String>> contextDataDictionary = new HashMap<>();
    protected final Set<String> contextKeywordsSet = new HashSet<>();

    public void setAdType(AdType adType) {
        this.adType = adType;
    }

    public AdType getAdType() {
        return adType;
    }

    public void setConfigId(String configId) {
        this.configId = configId;
    }

    public String getConfigId() {
        return configId;
    }

    public void setAppContent(ContentObject content) {
        appContent = content;
    }

    public ContentObject getAppContent() {
        return appContent;
    }

    public void setPbAdSlot(String pbAdSlot) {
        this.pbAdSlot = pbAdSlot;
    }

    public String getPbAdSlot() {
        return pbAdSlot;
    }

    public void addUserData(DataObject dataObject) {
        if (dataObject != null) {
            userDataObjects.add(dataObject);
        }
    }

    @NonNull
    public ArrayList<DataObject> getUserData() {
        return userDataObjects;
    }

    public void clearUserData() {
        userDataObjects.clear();
    }

    public void addContextData(String key, String value) {
        if (key != null && value != null) {
            HashSet<String> hashSet = new HashSet<>();
            hashSet.add(value);
            contextDataDictionary.put(key, hashSet);
        }
    }

    public void addContextData(String key, Set<String> value) {
        if (key != null && value != null) {
            contextDataDictionary.put(key, value);
        }
    }

    public void removeContextData(String key) {
        contextDataDictionary.remove(key);
    }

    @NonNull
    public Map<String, Set<String>> getContextDataDictionary() {
        return contextDataDictionary;
    }

    public void clearContextData() {
        contextDataDictionary.clear();
    }

    public void addContextKeyword(String keyword) {
        if (keyword != null) {
            contextKeywordsSet.add(keyword);
        }
    }

    public void addContextKeywords(Set<String> keywords) {
        if (keywords != null) {
            contextKeywordsSet.addAll(keywords);
        }
    }

    public void removeContextKeyword(String key) {
        if (key != null) {
            contextKeywordsSet.remove(key);
        }
    }

    @NonNull
    public Set<String> getContextKeywordsSet() {
        return contextKeywordsSet;
    }

    public void clearContextKeywords() {
        contextKeywordsSet.clear();
    }

    public AdUnitConfiguration castToOriginal() {
        if (this instanceof AdUnitConfiguration) {
            return (AdUnitConfiguration) this;
        }
        throw new IllegalStateException("Can't cast to AdUnitConfiguration.");
    }

    public NativeAdUnitConfiguration castToNative() {
        if (this instanceof NativeAdUnitConfiguration) {
            return (NativeAdUnitConfiguration) this;
        }
        throw new IllegalStateException("Can't cast to NativeAdUnitConfiguration.");
    }

}
