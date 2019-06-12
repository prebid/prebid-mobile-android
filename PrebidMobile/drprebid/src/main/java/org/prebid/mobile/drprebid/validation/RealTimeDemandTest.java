package org.prebid.mobile.drprebid.validation;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
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
import org.prebid.mobile.drprebid.model.Bidder;
import org.prebid.mobile.drprebid.model.DemandTestResults;
import org.prebid.mobile.drprebid.model.GeneralSettings;
import org.prebid.mobile.drprebid.model.PrebidServer;
import org.prebid.mobile.drprebid.model.PrebidServerSettings;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RealTimeDemandTest {
    private static final String TAG = RealTimeDemandTest.class.getSimpleName();

    public interface Listener {
        void onTestFinished(DemandTestResults results);
    }

    private Listener mListener;
    private Context mContext;

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
                Host.CUSTOM.setHostUrl(hostUrl);
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

    private final DemandTestResultTask.RequestCompletionListener requestCompletionListener = (response, responseCode) -> {
        if (responseCode == 200) {
            try {
                JSONObject responseJson = new JSONObject(response);
                boolean containsBids = false;
                testResponseCount++;

                Map<String, String> bidderResponseStatuses = new HashMap<>();

                JSONObject ext = responseJson.getJSONObject("ext");
                JSONObject responseMillis = ext.getJSONObject("responsetimemillis");

                Iterator<String> responseIterator = responseMillis.keys();

                while (responseIterator.hasNext()) {
                    String key = responseIterator.next();
                    bidderResponseStatuses.put(key, "0");

                    Bidder bidderDetails = testResults.getBidders().get(key);

                    if (bidderDetails == null) {
                        bidderDetails = new Bidder();

                        bidderDetails.setBid(0);
                        bidderDetails.setNobid(0);
                        bidderDetails.setTimeout(0);
                        bidderDetails.setError(0);
                        bidderDetails.setCpm(0);
                        bidderDetails.setServerResponse("");
                        bidderDetails.setResponseTime(responseMillis.getInt("responseTime"));

                        testResults.getBidders().put(key, bidderDetails);
                    }
                }

                if (responseJson.has("seatbid")) {
                    JSONArray seatbids = responseJson.getJSONArray("seatbid");
                    if (seatbids != null) {
                        for (int i = 0; i < seatbids.length(); i++) {
                            JSONObject seatbid = seatbids.getJSONObject(i);

                            if (seatbid.has("bid")) {
                                JSONArray bids = seatbid.getJSONArray("bid");
                                if (bids != null) {
                                    for (int j = 0; j < bids.length(); j++) {
                                        JSONObject bid = bids.getJSONObject(j);
                                        containsBids = true;
                                        String bidderName = bid.getString("seat");
                                        bidderResponseStatuses.put(bidderName, "3");

                                        Bidder bidderDetails = testResults.getBidders().get(bidderName);

                                        if (bidderDetails != null) {
                                            JSONObject bidExt = bid.getJSONObject("ext");
                                            if (bidExt != null) {
                                                JSONObject extPrebid = bidExt.getJSONObject("prebid");
                                                if (extPrebid != null) {
                                                    JSONObject prebidTargeting = extPrebid.getJSONObject("targeting");
                                                    if (prebidTargeting != null) {
                                                        bidderDetails.setBid(bidderDetails.getBid() + 1);
                                                        bidderDetails.setServerResponse(response);
                                                    }
                                                }
                                            }

                                            double price = bid.getDouble("price");
                                            price += bidderDetails.getCpm();
                                            bidderDetails.setCpm(price);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (containsBids) {
                    testResults.setTotalBids(testResults.getTotalBids() + 1);
                }

                if (ext.has("errors")) {
                    JSONObject bidderErrors = ext.getJSONObject("errors");
                    if (bidderErrors != null) {
                        if (SettingsManager.getInstance(mContext).getPrebidServerSettings().getPrebidServer() == PrebidServer.RUBICON) {
                            Iterator<String> keyIterator = bidderErrors.keys();
                            while (keyIterator.hasNext()) {
                                String key = keyIterator.next();
                                JSONArray errors = bidderErrors.getJSONArray(key);
                                if (errors != null && errors.length() > 0) {
                                    bidderResponseStatuses.put(key, "2");
                                }
                            }
                        } else {
                            Iterator<String> keyIterator = bidderErrors.keys();
                            while (keyIterator.hasNext()) {
                                String key = keyIterator.next();
                                bidderResponseStatuses.put(key, "2");
                            }
                        }
                    }
                }

                for (String key : bidderResponseStatuses.keySet()) {
                    Bidder bidderDetails = testResults.getBidders().get(key);
                    switch (bidderResponseStatuses.get(key)) {
                        case "0":
                            bidderDetails.setNobid(bidderDetails.getNobid() + 1);
                            break;
                        case "1":
                            bidderDetails.setTimeout(bidderDetails.getTimeout() + 1);
                            break;
                        case "2":
                            bidderDetails.setError(bidderDetails.getError() + 1);
                            break;
                    }
                }

                if (testResponseCount == 100) {
                    for (String key : bidderResponseStatuses.keySet()) {
                        Bidder bidderDetails = testResults.getBidders().get(key);
                        if (TextUtils.isEmpty(bidderDetails.getServerResponse())) {
                            bidderDetails.setServerResponse(response);
                        }
                    }
                }

            } catch (Exception exception) {
                Log.e(TAG, exception.getMessage());
            }
        } else {
            testResults.setResponseStatus(responseCode);
            testResults.setError(new Exception(response));
        }

        if (testResponseCount == 100) {
            int totalBids = 0;
            float totalCpm = 0.0f;
            int averageResponseTime = 0;

            for (String key : testResults.getBidders().keySet()) {
                Bidder bidder = testResults.getBidders().get(key);
                double totalPrice = bidder.getCpm();
                int bids = bidder.getBid();
                totalBids += bids;
                totalCpm += totalPrice;
                averageResponseTime += bidder.getResponseTime();
            }

            averageResponseTime = averageResponseTime / testResults.getBidders().size();
            float averageCpm = totalCpm / totalBids;
            testResults.setAvgEcpm(averageCpm);
            testResults.setAvgResponseTime(averageResponseTime);
            if (mListener != null) {
                mListener.onTestFinished(testResults);
            }
        }
    };

    private void runTest(String url, String requestBody, DemandTestResultTask.RequestCompletionListener listener) {
        DemandTestResultTask testResultTask = new DemandTestResultTask(listener);

        DemandTestTask testTask = new DemandTestTask(url, requestBody, testResultTask);
        DemandTestManager.getInstance().runRequest(testTask);
    }
}
