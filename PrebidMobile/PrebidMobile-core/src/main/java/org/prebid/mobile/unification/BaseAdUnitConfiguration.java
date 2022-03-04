package org.prebid.mobile.unification;

import androidx.annotation.NonNull;
import org.prebid.mobile.AdType;
import org.prebid.mobile.ContentObject;
import org.prebid.mobile.DataObject;

import java.util.*;

public abstract class BaseAdUnitConfiguration implements BaseAdUnitConfigurationInterface {

    private AdType adType;
    private String configId;
    private ContentObject appContent;
    private String pbAdSlot;
    private final ArrayList<DataObject> userDataObjects = new ArrayList<>();
    private final Map<String, Set<String>> contextDataDictionary = new HashMap<>();
    private final Set<String> contextKeywordsSet = new HashSet<>();

    @Override
    public void setAdType(AdType adType) {
        this.adType = adType;
    }

    @Override
    public AdType getAdType() {
        return adType;
    }

    @Override
    public void setConfigId(String configId) {
        this.configId = configId;
    }

    @Override
    public String getConfigId() {
        return configId;
    }

    @Override
    public void setAppContent(ContentObject content) {
        appContent = content;
    }

    @Override
    public ContentObject getAppContent() {
        return appContent;
    }

    @Override
    public void setPbAdSlot(String pbAdSlot) {
        this.pbAdSlot = pbAdSlot;
    }

    @Override
    public String getPbAdSlot() {
        return pbAdSlot;
    }

    @Override
    public void addUserData(DataObject dataObject) {
        if (dataObject != null) {
            userDataObjects.add(dataObject);
        }
    }

    @Override
    @NonNull
    public ArrayList<DataObject> getUserData() {
        return userDataObjects;
    }

    @Override
    public void clearUserData() {
        userDataObjects.clear();
    }

    @Override
    public void addContextData(String key, String value) {
        if (key != null && value != null) {
            HashSet<String> hashSet = new HashSet<>();
            hashSet.add(value);
            contextDataDictionary.put(key, hashSet);
        }
    }

    @Override
    public void addContextData(String key, Set<String> value) {
        if (key != null && value != null) {
            contextDataDictionary.put(key, value);
        }
    }

    @Override
    public void removeContextData(String key) {
        contextDataDictionary.remove(key);
    }

    @Override
    @NonNull
    public Map<String, Set<String>> getContextDataDictionary() {
        return contextDataDictionary;
    }

    @Override
    public void clearContextData() {
        contextDataDictionary.clear();
    }

    @Override
    public void addContextKeyword(String keyword) {
        if (keyword != null) {
            contextKeywordsSet.add(keyword);
        }
    }

    @Override
    public void addContextKeywords(Set<String> keywords) {
        if (keywords != null) {
            contextKeywordsSet.addAll(keywords);
        }
    }

    @Override
    public void removeContextKeyword(String key) {
        if (key != null) {
            contextKeywordsSet.remove(key);
        }
    }

    @Override
    @NonNull
    public Set<String> getContextKeywordsSet() {
        return contextKeywordsSet;
    }

    @Override
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
