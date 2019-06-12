package org.prebid.mobile.drprebid.validation;

import android.content.Context;

import org.prebid.mobile.AdUnit;
import org.prebid.mobile.BannerAdUnit;
import org.prebid.mobile.Host;
import org.prebid.mobile.InterstitialAdUnit;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.TargetingParams;
import org.prebid.mobile.drprebid.managers.SettingsManager;
import org.prebid.mobile.drprebid.model.AdFormat;
import org.prebid.mobile.drprebid.model.AdServerSettings;
import org.prebid.mobile.drprebid.model.AdSize;
import org.prebid.mobile.drprebid.model.GeneralSettings;
import org.prebid.mobile.drprebid.model.PrebidServerSettings;
import org.prebid.mobile.drprebid.model.SdkTestResults;

public class SdkTest {
    private static final String TAG = SdkTest.class.getSimpleName();

    public interface Listener {
        void onAdUnitRegistered();
        void onTestFinished(SdkTestResults results);
    }

    private Listener mListener;
    private Context mContext;

    private AdUnit mAdUnit;
    private boolean mInitialPrebidServerRequestReceived;
    private boolean mInitialPrebidServerResponseReceived;
    private boolean mBidReceived;

    public SdkTest(Context context, Listener listener) {
        mContext = context;
        mListener = listener;

        setupPrebid();
    }

    private void setupPrebid() {
        GeneralSettings generalSettings = SettingsManager.getInstance(mContext).getGeneralSettings();
        AdServerSettings adServerSettings = SettingsManager.getInstance(mContext).getAdServerSettings();
        PrebidServerSettings prebidServerSettings = SettingsManager.getInstance(mContext).getPrebidServerSettings();

        setPrebidTargetingParams();

        if (generalSettings.getAdFormat() == AdFormat.BANNER) {
            AdSize adSize = generalSettings.getAdSize();
            mAdUnit = new BannerAdUnit(prebidServerSettings.getConfigId(), adSize.getWidth(), adSize.getHeight());
        } else if (generalSettings.getAdFormat() == AdFormat.INTERSTITIAL) {
            mAdUnit = new InterstitialAdUnit(prebidServerSettings.getConfigId());
        }

        PrebidMobile.setPrebidServerAccountId(prebidServerSettings.getAccountId());

        switch (prebidServerSettings.getPrebidServer()) {
            case APPNEXUS:
                PrebidMobile.setPrebidServerHost(Host.APPNEXUS);
                break;
            case RUBICON:
                PrebidMobile.setPrebidServerHost(Host.RUBICON);
                break;
            case CUSTOM:
                PrebidMobile.setPrebidServerHost(Host.CUSTOM);
                Host.CUSTOM.setHostUrl(prebidServerSettings.getCustomPrebidServerUrl());
                break;
        }

        if (mListener != null) {
            mListener.onAdUnitRegistered();
        }
    }

    private void setPrebidTargetingParams() {
        TargetingParams.setGender(TargetingParams.GENDER.FEMALE);
        PrebidMobile.setShareGeoLocation(true);
    }

    public void startTest() {

    }
}
