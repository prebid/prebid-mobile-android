package org.prebid.mobile;

import android.text.TextUtils;
import android.webkit.URLUtil;

import org.json.JSONException;
import org.json.JSONObject;

public class PrebidNativeAd {
    private String title;
    private String description;
    private String iconUrl;
    private String imageUrl;
    private String cta;
    private String clickUrl;

    static PrebidNativeAd create(String cacheId) {
        String content = CacheManager.get(cacheId);
        if (!TextUtils.isEmpty(content)) {
            try {
                JSONObject details = new JSONObject(content);
                PrebidNativeAd ad = new PrebidNativeAd();
                ad.setTitle(details.getString("title"));
                ad.setDescription(details.getString("description"));
                ad.setIconUrl(details.getString("iconUrl"));
                ad.setImageUrl(details.getString("imageUrl"));
                ad.setCallToAction(details.getString("cta"));
                ad.setClickUrl(details.getString("clickUrl"));
                return ad.isValid() ? ad : null;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private boolean isValid() {
        return URLUtil.isValidUrl(clickUrl) &&
                !TextUtils.isEmpty(title) &&
                !TextUtils.isEmpty(description) &&
                URLUtil.isValidUrl(iconUrl) &&
                URLUtil.isValidUrl(imageUrl) &&
                !TextUtils.isEmpty(cta);
    }

    private PrebidNativeAd() {
    }

    private void setClickUrl(String clickUrl) {
        this.clickUrl = clickUrl;
    }

    private void setTitle(String title) {
        this.title = title;
    }

    private void setDescription(String description) {
        this.description = description;
    }

    private void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    private void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    private void setCallToAction(String cta) {
        this.cta = cta;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getCallToAction() {
        return cta;
    }

    public String getClickUrl() {
        return clickUrl;
    }
}
