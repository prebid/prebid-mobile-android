package org.prebid.mobile.drprebid.validation;

import android.content.Context;
import android.net.Uri;
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
    private static final int REQUEST_MAX = 100;

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
                hostUrl = buildCustomServerEndpoint(prebidServerSettings.getCustomPrebidServerUrl());
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

        for (int i = 0; i < REQUEST_MAX; i++) {
            runTest(hostUrl, request, requestCompletionListener);
        }
    }

    private String buildCustomServerEndpoint(String url) {
        if (!TextUtils.isEmpty(url)) {
            Uri.Builder uriBuilder = Uri.parse(url).buildUpon();
            uriBuilder.appendPath("openrtb2");
            uriBuilder.appendPath("auction");

            return uriBuilder.build().toString();
        } else {
            return "";
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
                        bidderDetails.setResponseTime(responseMillis.getInt(key));

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
                                        String bidderName = seatbid.getString("seat");
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
                    if (bidderDetails != null) {
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
                }

                if (testResponseCount == REQUEST_MAX) {
                    for (String key : bidderResponseStatuses.keySet()) {
                        Bidder bidderDetails = testResults.getBidders().get(key);
                        if (bidderDetails != null && TextUtils.isEmpty(bidderDetails.getServerResponse())) {
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

        if (testResponseCount == REQUEST_MAX) {
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

            if (testResults.getBidders().size() > 0) {
                averageResponseTime = averageResponseTime / testResults.getBidders().size();
            }

            if (totalBids > 0) {
                testResults.setAvgEcpm(totalCpm / totalBids);
            } else {
                testResults.setAvgEcpm(0);
            }

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
