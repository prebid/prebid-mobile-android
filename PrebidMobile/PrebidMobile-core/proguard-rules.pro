
# GAM
-keep class com.google.android.gms.ads.doubleclick.AdManagerAdRequest {
    public android.os.Bundle getCustomTargeting();
}

-keep class com.google.android.gms.ads.doubleclick.AdManagerAdRequest$Builder {
    public com.google.android.gms.ads.doubleclick.AdManagerAdRequest build();
    public com.google.android.gms.ads.doubleclick.AdManagerAdRequest$Builder addCustomTargeting(java.lang.String, java.lang.String);
}

# MoPub Just to be sure
-keep class com.mopub.mobileads.MoPubView {
    public java.lang.String getKeywords();
    public void setKeywords(java.lang.String);
}

-keep class com.mopub.mobileads.MoPubInterstitial {
    public java.lang.String getKeywords();
    public void setKeywords(java.lang.String);
}

