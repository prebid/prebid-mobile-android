package org.prebid.mediationadapters.mopub;

import android.content.Context;
import android.util.Log;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.mopub.mobileads.CustomEventBanner;
import com.mopub.mobileads.MoPubErrorCode;

import org.json.JSONException;
import org.json.JSONObject;
import org.prebid.mobile.prebidserver.CacheService;

import java.util.Map;

public class PrebidCustomEventBanner extends CustomEventBanner implements AdListener, CacheService.CacheListener {
    private CustomEventBannerListener customEventBannerListener;
    private AdView adView;
    private Context context;
    private int width;
    private int height;
    private String bidder;

    @Override
    protected void loadBanner(Context context, CustomEventBannerListener customEventBannerListener, Map<String, Object> localExtras, Map<String, String> serverExtras) {
        Log.d("FB-Integration", "Load Prebid content for Facebook");
        this.customEventBannerListener = customEventBannerListener;
        if (localExtras != null) {
            String cache_id = (String) localExtras.get("hb_cache_id");
            bidder = (String) localExtras.get("hb_bidder");
            width = (int) localExtras.get("com_mopub_ad_width");
            height = (int) localExtras.get("com_mopub_ad_height");
            CacheService cs = new CacheService(this, cache_id);
            cs.execute();
        } else {
            if (customEventBannerListener != null) {
                customEventBannerListener.onBannerFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
            }
        }
    }

    @Override
    protected void onInvalidate() {
        customEventBannerListener = null;
        context = null;
        if (adView != null) {
            adView.destroy();
        }
    }

    @Override
    public void onError(Ad ad, AdError adError) {
        if (customEventBannerListener != null) {
            if (adError != null) {
                switch (adError.getErrorCode()) {
                    case 1000:
                        customEventBannerListener.onBannerFailed(MoPubErrorCode.NO_CONNECTION);
                        break;
                    case 1001:
                        customEventBannerListener.onBannerFailed(MoPubErrorCode.NO_FILL);
                        break;
                    case 1002:
                        customEventBannerListener.onBannerFailed(MoPubErrorCode.INTERNAL_ERROR);
                        break;
                    case 2000:
                        customEventBannerListener.onBannerFailed(MoPubErrorCode.SERVER_ERROR);
                        break;
                    case 2001:
                        customEventBannerListener.onBannerFailed(MoPubErrorCode.INTERNAL_ERROR);
                        break;
                    case 3001:
                        customEventBannerListener.onBannerFailed(MoPubErrorCode.INTERNAL_ERROR);
                        break;
                }
            } else {
                customEventBannerListener.onBannerFailed(MoPubErrorCode.INTERNAL_ERROR);
            }

        }
    }

    @Override
    public void onAdLoaded(Ad ad) {
        if (customEventBannerListener != null) {
            customEventBannerListener.onBannerLoaded(adView);
        }
    }

    @Override
    public void onAdClicked(Ad ad) {
        if (customEventBannerListener != null) {
            customEventBannerListener.onBannerClicked();
        }
    }

    @Override
    public void onLoggingImpression(Ad ad) {

    }

    @Override
    public void onResponded(JSONObject jsonObject) {
        String adm = null;
        String placementID = "";
        try {
            adm = jsonObject.getString("adm");
            JSONObject bid = new JSONObject(adm);
            placementID = bid.getString("placement_id");
        } catch (JSONException e) {
        }
        if ("audienceNetwork".equals(bidder)) {

            adView = new AdView(context, placementID, new AdSize(width, height));
            adView.setAdListener(this); // todo figure out how to send notification back more generically
            adView.loadAdFromBid(adm);
        }

    }
}
