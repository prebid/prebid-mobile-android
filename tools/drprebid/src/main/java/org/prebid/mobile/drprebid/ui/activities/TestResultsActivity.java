package org.prebid.mobile.drprebid.ui.activities;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.ads.MobileAds;
import org.prebid.mobile.drprebid.R;
import org.prebid.mobile.drprebid.managers.SettingsManager;
import org.prebid.mobile.drprebid.model.AdServerSettings;
import org.prebid.mobile.drprebid.ui.adapters.TestResultsAdapter;
import org.prebid.mobile.drprebid.ui.viewmodels.AdServerValidationViewModel;
import org.prebid.mobile.drprebid.ui.viewmodels.PrebidServerValidationViewModel;
import org.prebid.mobile.drprebid.ui.viewmodels.SdkValidationViewModel;
import org.prebid.mobile.drprebid.validation.AdServerTest;
import org.prebid.mobile.drprebid.validation.RealTimeDemandTest;
import org.prebid.mobile.drprebid.validation.SdkTest;

public class TestResultsActivity extends AppCompatActivity {

    private RecyclerView listView;

    private AdServerValidationViewModel adServerValidationViewModel;
    private PrebidServerValidationViewModel demandValidationViewModel;
    private SdkValidationViewModel sdkValidationViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_results);

        listView = findViewById(R.id.list_results);
        setupResultsList();

        adServerValidationViewModel = ViewModelProviders.of(this).get(AdServerValidationViewModel.class);
        demandValidationViewModel = ViewModelProviders.of(this).get(PrebidServerValidationViewModel.class);
        sdkValidationViewModel = ViewModelProviders.of(this).get(SdkValidationViewModel.class);

        AdServerSettings adServerSettings = SettingsManager.getInstance(this).getAdServerSettings();

        initGoogleAdsManager();

    }



    private void initGoogleAdsManager() {
        MobileAds.initialize(this.getApplication());
        runTests();
    }

    private void setupResultsList() {
        TestResultsAdapter adapter = new TestResultsAdapter();

        listView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        listView.setItemAnimator(new DefaultItemAnimator());
        listView.setAdapter(adapter);
    }

    private void runTests() {
        runAdServerValidationTest();
    }

    private void runAdServerValidationTest() {
        AdServerTest adServerTest = new AdServerTest(this, new AdServerTest.Listener() {
            @Override
            public void onPrebidKeywordsFoundOnRequest() {
                adServerValidationViewModel.setRequestSent(true);
            }

            @Override
            public void onPrebidKeywordsNotFoundOnRequest() {
                adServerValidationViewModel.setRequestSent(false);
            }

            @Override
            public void adServerResponseContainsPrebidCreative(@Nullable Boolean contains) {
                adServerValidationViewModel.setCreativeServed(contains);
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

            demandValidationViewModel.setBidResponseReceivedCount(totalBids);
            demandValidationViewModel.setAverageCpm(results.getAvgEcpm());
            demandValidationViewModel.setAverageResponseTime(results.getAvgResponseTime());

            runSdkValidationTest();
        });

        demandValidator.startTest();
        demandValidationViewModel.setBidRequestsSent(true);
    }

    private void runSdkValidationTest() {
        SdkTest sdkTest = new SdkTest(this, new SdkTest.Listener() {
            @Override
            public void onAdUnitRegistered() {
                sdkValidationViewModel.setAdUnitRegistered(true);
            }

            @Override
            public void requestToPrebidServerSent(boolean sent) {
                sdkValidationViewModel.setPrebidRequestSent(sent);
            }

            @Override
            public void responseFromPrebidServerReceived(boolean received) {
                sdkValidationViewModel.setPrebidResponseReceived(received);
            }

            @Override
            public void bidReceivedAndCached(boolean received) {
                sdkValidationViewModel.setCreativeContentCached(received);
            }

            @Override
            public void requestSentToAdServer(String request, String postBody) {
                sdkValidationViewModel.setAdServerRequestSent(true);
            }

            @Override
            public void adServerResponseContainsPrebidCreative(@Nullable Boolean contains) {
                sdkValidationViewModel.setCreativeServed(contains);
            }

            @Override
            public void onTestFinished() {

            }
        });

        sdkTest.startTest();
    }
}
