package org.prebid.mobile.unification;

import org.prebid.mobile.AdType;
import org.prebid.mobile.ContentObject;
import org.prebid.mobile.DataObject;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public abstract interface BaseAdUnitConfigurationInterface {

    public void setAdType(AdType adType);

    public AdType getAdType();


    public void setConfigId(String configId);

    public String getConfigId();


    public void setAppContent(ContentObject content);

    public ContentObject getAppContent();


    public void setPbAdSlot(String pbAdSlot);

    public String getPbAdSlot();


    public void addUserData(DataObject dataObject);

    public ArrayList<DataObject> getUserData();

    public void clearUserData();


    public void addContextData(String key, String value);

    public void updateContextData(String key, Set<String> value);

    public void removeContextData(String key);

    public Map<String, Set<String>> getContextDataDictionary();

    public void clearContextData();


    public void addContextKeyword(String keyword);

    public void addContextKeywords(Set<String> keywords);

    public void removeContextKeywords(String key);

    public Set<String> getContextKeywordsSet();

    public void clearContextKeywords();

}
