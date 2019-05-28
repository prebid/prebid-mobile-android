package org.prebid.mobile.drprebid.validation;

import android.content.Context;

import org.prebid.mobile.drprebid.managers.SettingsManager;
import org.prebid.mobile.drprebid.model.AdServer;
import org.prebid.mobile.drprebid.model.AdServerSettings;
import org.prebid.mobile.drprebid.model.GeneralSettings;

public class AdServerTest {
    public interface Listener {
        void onPrebidKeywordsFoundOnRequest();

        void onPrebidKeywordsNotFoundOnRequest();

        void onServerRespondedWithPrebidCreative();

        void onServerNotRespondedWithPrebidCreative(Throwable error);
    }

    private final Listener mListener;
    private final Context mContext;

    public AdServerTest(Context context, Listener listener) {
        mContext = context;
        mListener = listener;
    }

    public void startTest() {
        GeneralSettings generalSettings = SettingsManager.getInstance(mContext).getGeneralSettings();
        AdServerSettings adServerSettings = SettingsManager.getInstance(mContext).getAdServerSettings();

        switch (generalSettings.getAdSize()) {
            case BANNER_300x250:
                break;
            case BANNER_300x600:
                break;
            case BANNER_320x50:
                break;
            case BANNER_320x100:
                break;
            case BANNER_320x480:
                break;
            case BANNER_728x90:
                break;
        }

        if (adServerSettings.getAdServer() == AdServer.GOOGLE_AD_MANAGER) {

        } else if (adServerSettings.getAdServer() == AdServer.MOPUB) {

        }
    }
}
