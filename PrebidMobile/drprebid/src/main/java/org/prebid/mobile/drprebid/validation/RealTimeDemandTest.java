package org.prebid.mobile.drprebid.validation;

import android.content.Context;

import org.prebid.mobile.AdUnit;
import org.prebid.mobile.BannerAdUnit;
import org.prebid.mobile.Host;
import org.prebid.mobile.InterstitialAdUnit;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.drprebid.Constants;
import org.prebid.mobile.drprebid.async.DemandTestResultTask;
import org.prebid.mobile.drprebid.async.DemandTestTask;
import org.prebid.mobile.drprebid.managers.DemandTestManager;
import org.prebid.mobile.drprebid.managers.SettingsManager;
import org.prebid.mobile.drprebid.model.AdFormat;
import org.prebid.mobile.drprebid.model.AdSize;
import org.prebid.mobile.drprebid.model.DemandTestResponse;
import org.prebid.mobile.drprebid.model.DemandTestResults;
import org.prebid.mobile.drprebid.model.GeneralSettings;
import org.prebid.mobile.drprebid.model.PrebidServerSettings;

import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.List;

public class RealTimeDemandTest {
    public interface Listener {

    }

    private final Listener mListener;
    private final Context mContext;

    private int testResponseCount = 0;
    private DemandTestResults testResults;

    public RealTimeDemandTest(Context context, Listener listener) {
        mContext = context;
        mListener = listener;
    }

    public void startTest() {
        GeneralSettings generalSettings = SettingsManager.getInstance(mContext).getGeneralSettings();
        PrebidServerSettings prebidServerSettings = SettingsManager.getInstance(mContext).getPrebidServerSettings();

        AdUnit adUnit = null;
        if (generalSettings.getAdFormat() == AdFormat.BANNER) {
            AdSize adSize = generalSettings.getAdSize();
            adUnit = new BannerAdUnit(prebidServerSettings.getConfigId(), adSize.getWidth(), adSize.getHeight());
        } else if (generalSettings.getAdFormat() == AdFormat.INTERSTITIAL) {
            adUnit = new InterstitialAdUnit(prebidServerSettings.getConfigId());
        }

        List<AdUnit> adUnits = Collections.singletonList(adUnit);

        String hostUrl;
        switch (prebidServerSettings.getPrebidServer()) {
            case APPNEXUS:
                PrebidMobile.setPrebidServerHost(Host.APPNEXUS);
                hostUrl = Constants.EndpointUrls.APPNEXUS_PREBID_SERVER;
                break;
            case RUBICON:
                PrebidMobile.setPrebidServerHost(Host.RUBICON);
                hostUrl = Constants.EndpointUrls.RUBICON_PREBID_SERVER;
                break;
            case CUSTOM:
                PrebidMobile.setPrebidServerHost(Host.CUSTOM);
                hostUrl = prebidServerSettings.getCustomPrebidServerUrl();
                break;
            default:
                PrebidMobile.setPrebidServerHost(Host.APPNEXUS);
                hostUrl = Constants.EndpointUrls.APPNEXUS_PREBID_SERVER;
        }

        DemandRequestBuilder builder = new DemandRequestBuilder(mContext, prebidServerSettings.getConfigId(), generalSettings.getAdSize());
        String request = builder.buildRequest(adUnits, prebidServerSettings.getAccountId(), true);

        testResponseCount = 0;
        testResults = new DemandTestResults(request);

        for (int i = 0; i < 100; i++) {
            runTest(hostUrl, request, requestCompletionListener);
        }
    }

    private final DemandTestResultTask.RequestCompletionListener requestCompletionListener = response -> {

    };

    private void runTest(String url, String requestBody, DemandTestResultTask.RequestCompletionListener listener) {
        DemandTestResultTask testResultTask = new DemandTestResultTask(listener);

        DemandTestTask testTask = new DemandTestTask(url, requestBody, testResultTask);
        DemandTestManager.getInstance().runRequest(testTask);
    }
}
