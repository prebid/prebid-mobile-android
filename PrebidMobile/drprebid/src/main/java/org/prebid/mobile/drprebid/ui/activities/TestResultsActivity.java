package org.prebid.mobile.drprebid.ui.activities;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.android.gms.ads.MobileAds;
import com.mopub.common.MoPub;
import com.mopub.common.SdkConfiguration;

import org.prebid.mobile.drprebid.R;
import org.prebid.mobile.drprebid.managers.SettingsManager;
import org.prebid.mobile.drprebid.model.AdServer;
import org.prebid.mobile.drprebid.model.AdServerSettings;
import org.prebid.mobile.drprebid.ui.adapters.TestResultsAdapter;
import org.prebid.mobile.drprebid.ui.viewmodels.AdServerValidationViewModel;
import org.prebid.mobile.drprebid.ui.viewmodels.PrebidServerValidationViewModel;
import org.prebid.mobile.drprebid.ui.viewmodels.SdkValidationViewModel;
import org.prebid.mobile.drprebid.validation.AdServerTest;
import org.prebid.mobile.drprebid.validation.RealTimeDemandTest;
import org.prebid.mobile.drprebid.validation.SdkTest;

public class TestResultsActivity extends AppCompatActivity {

    private RecyclerView mListView;

    private AdServerValidationViewModel mAdServerValidationViewModel;
    private PrebidServerValidationViewModel mDemandValidationViewModel;
    private SdkValidationViewModel mSdkValidationViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_results);

        mListView = findViewById(R.id.list_results);
        setupResultsList();

        mAdServerValidationViewModel = ViewModelProviders.of(this).get(AdServerValidationViewModel.class);
        mDemandValidationViewModel = ViewModelProviders.of(this).get(PrebidServerValidationViewModel.class);
        mSdkValidationViewModel = ViewModelProviders.of(this).get(SdkValidationViewModel.class);

        AdServerSettings adServerSettings = SettingsManager.getInstance(this).getAdServerSettings();

        if (adServerSettings.getAdServer() == AdServer.MOPUB) {
            initMoPub(adServerSettings.getAdUnitId());
        } else {
            initGoogleAdsManager();
        }
    }

    private void initMoPub(String adUnitId) {
        SdkConfiguration sdkConfiguration = new SdkConfiguration.Builder(adUnitId).build();
        MoPub.initializeSdk(this, sdkConfiguration, this::runTests);
    }

    private void initGoogleAdsManager() {
        MobileAds.initialize(this.getApplication());
        runTests();
    }

    private void setupResultsList() {
        TestResultsAdapter adapter = new TestResultsAdapter();

        mListView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        mListView.setItemAnimator(new DefaultItemAnimator());
        mListView.setAdapter(adapter);
    }

    private void runTests() {
        runAdServerValidationTest();
    }

    private void runAdServerValidationTest() {
        AdServerTest adServerTest = new AdServerTest(this, new AdServerTest.Listener() {
            @Override
            public void onPrebidKeywordsFoundOnRequest() {
                mAdServerValidationViewModel.setRequestSent(true);
            }

            @Override
            public void onPrebidKeywordsNotFoundOnRequest() {
                mAdServerValidationViewModel.setRequestSent(false);
            }

            @Override
            public void onServerRespondedWithPrebidCreative() {
                mAdServerValidationViewModel.setCreativeServed(true);
            }

            @Override
            public void onServerNotRespondedWithPrebidCreative() {
                mAdServerValidationViewModel.setCreativeServed(false);
            }

            @Override
            public void onTestFinished() {
                runDemandValidationTest();
            }
        });

        adServerTest.startTest();
    }

    private void runDemandValidationTest() {
        RealTimeDemandTest demandValidator = new RealTimeDemandTest(this, results -> {
            int totalBids = results.getTotalBids();

            mDemandValidationViewModel.setBidResponseReceivedCount(totalBids);
            mDemandValidationViewModel.setAverageCpm(results.getAvgEcpm());
            mDemandValidationViewModel.setAverageResponseTime(results.getAvgResponseTime());

            runSdkValidationTest();
        });

        demandValidator.startTest();
        mDemandValidationViewModel.setBidRequestsSent(true);
    }

    private void runSdkValidationTest() {
        SdkTest sdkTest = new SdkTest(this, new SdkTest.Listener() {
            @Override
            public void onAdUnitRegistered() {
                mSdkValidationViewModel.setAdUnitRegistered(true);
            }

            @Override
            public void requestToPrebidServerSent(boolean sent) {
                mSdkValidationViewModel.setPrebidRequestSent(sent);
            }

            @Override
            public void responseFromPrebidServerReceived(boolean received) {
                mSdkValidationViewModel.setPrebidResponseReceived(received);
            }

            @Override
            public void bidReceivedAndCached(boolean received) {
                mSdkValidationViewModel.setCreativeContentCached(received);
            }

            @Override
            public void requestSentToAdServer(String request, String postBody) {
                mSdkValidationViewModel.setAdServerRequestSent(true);
            }

            @Override
            public void adServerResponseContainsPrebidCreative(boolean contains) {
                mSdkValidationViewModel.setCreativeServed(contains);
            }

            @Override
            public void onTestFinished() {

            }
        });

        sdkTest.startTest();
    }
}
