package org.prebid.mobile.api.original;

import org.jetbrains.annotations.Nullable;
import org.prebid.mobile.BannerParameters;
import org.prebid.mobile.ContentObject;
import org.prebid.mobile.DataObject;
import org.prebid.mobile.NativeParameters;
import org.prebid.mobile.VideoParameters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PrebidRequest {

    @Nullable
    private BannerParameters bannerParameters;
    @Nullable
    private VideoParameters videoParameters;
    @Nullable
    private NativeParameters nativeParameters;

    private boolean isInterstitial = false;
    private boolean isRewarded = false;

    @Nullable
    private String gpid;
    @Nullable
    private Map<String, Set<String>> extData;
    @Nullable
    private Set<String> extKeywords;
    @Nullable
    private ContentObject appContent;
    @Nullable
    private ArrayList<DataObject> userData;

    public PrebidRequest() {
    }


    BannerParameters getBannerParameters() {
        return bannerParameters;
    }

    public void setBannerParameters(BannerParameters bannerParameters) {
        this.bannerParameters = bannerParameters;
    }

    VideoParameters getVideoParameters() {
        return videoParameters;
    }

    public void setVideoParameters(VideoParameters videoParameters) {
        this.videoParameters = videoParameters;
    }

    NativeParameters getNativeParameters() {
        return nativeParameters;
    }

    public void setNativeParameters(NativeParameters nativeParameters) {
        this.nativeParameters = nativeParameters;
    }

    boolean isInterstitial() {
        return isInterstitial;
    }

    public void setInterstitial(boolean interstitial) {
        this.isInterstitial = interstitial;
    }

    boolean isRewarded() {
        return isRewarded;
    }

    public void setRewarded(boolean rewarded) {
        if (rewarded) {
            this.isInterstitial = true;
            this.isRewarded = true;
        }
    }


    @Nullable
    String getGpid() {
        return gpid;
    }

    public void setGpid(@Nullable String gpid) {
        this.gpid = gpid;
    }

    @Nullable
    ContentObject getAppContent() {
        return appContent;
    }

    public void setAppContent(@Nullable ContentObject appContent) {
        this.appContent = appContent;
    }

    @Nullable
    Map<String, Set<String>> getExtData() {
        return extData;
    }

    public void setExtData(@Nullable Map<String, Set<String>> extData) {
        if (extData == null) {
            this.extData = null;
            return;
        }
        this.extData = new HashMap<>(extData);
    }

    @Nullable
    Set<String> getExtKeywords() {
        return extKeywords;
    }

    public void setExtKeywords(@Nullable Set<String> extKeywords) {
        if (extKeywords == null) {
            this.extKeywords = null;
            return;
        }
        this.extKeywords = new HashSet<>(extKeywords);
    }

    @Nullable
    ArrayList<DataObject> getUserData() {
        return userData;
    }

    public void setUserData(@Nullable ArrayList<DataObject> userData) {
        if (userData == null) {
            this.userData = null;
            return;
        }
        this.userData = new ArrayList<>(userData);
    }

}
