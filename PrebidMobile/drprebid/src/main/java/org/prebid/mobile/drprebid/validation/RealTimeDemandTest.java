package org.prebid.mobile.drprebid.validation;

import android.content.Context;

import org.prebid.mobile.AdUnit;
import org.prebid.mobile.BannerAdUnit;
import org.prebid.mobile.InterstitialAdUnit;
import org.prebid.mobile.drprebid.managers.SettingsManager;
import org.prebid.mobile.drprebid.model.AdFormat;
import org.prebid.mobile.drprebid.model.AdSize;
import org.prebid.mobile.drprebid.model.GeneralSettings;
import org.prebid.mobile.drprebid.model.PrebidServerSettings;

public class RealTimeDemandTest {
    public interface Listener {

    }

    private final Listener mListener;
    private final Context mContext;

    public RealTimeDemandTest(Context context, Listener listener) {
        mContext = context;
        mListener = listener;
    }

    public void startTest() {
        GeneralSettings generalSettings = SettingsManager.getInstance(mContext).getGeneralSettings();
        PrebidServerSettings prebidServerSettings = SettingsManager.getInstance(mContext).getPrebidServerSettings();

        AdUnit adUnit;
        if (generalSettings.getAdFormat() == AdFormat.BANNER) {
            AdSize adSize = generalSettings.getAdSize();
            adUnit = new BannerAdUnit(prebidServerSettings.getConfigId(), adSize.getWidth(), adSize.getHeight());
        } else if (generalSettings.getAdFormat() == AdFormat.INTERSTITIAL) {
            adUnit = new InterstitialAdUnit(prebidServerSettings.getConfigId());
        }


    }
}
