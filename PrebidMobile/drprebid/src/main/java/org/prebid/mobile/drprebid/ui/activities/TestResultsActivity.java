package org.prebid.mobile.drprebid.ui.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.prebid.mobile.drprebid.R;
import org.prebid.mobile.drprebid.ui.adapters.TestResultsAdapter;
import org.prebid.mobile.drprebid.ui.viewmodels.PrebidServerValidationViewModel;
import org.prebid.mobile.drprebid.validation.RealTimeDemandTest;

public class TestResultsActivity extends AppCompatActivity {

    private RecyclerView mListView;

    private PrebidServerValidationViewModel mDemandValidationViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_results);

        mListView = findViewById(R.id.list_results);
        setupResultsList();

        mDemandValidationViewModel = ViewModelProviders.of(this).get(PrebidServerValidationViewModel.class);

        runTests();
    }

    private void setupResultsList() {
        TestResultsAdapter adapter = new TestResultsAdapter(this);

        mListView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        mListView.setItemAnimator(new DefaultItemAnimator());
        mListView.setAdapter(adapter);
    }

    private void runTests() {
        runDemandValidationTest();
    }

    private void runDemandValidationTest() {
        RealTimeDemandTest demandValidator = new RealTimeDemandTest(this, results -> {
            int totalBids = results.getTotalBids();

            mDemandValidationViewModel.setBidResponseReceivedCount(totalBids);
            mDemandValidationViewModel.setAverageCpm(results.getAvgEcpm());
            mDemandValidationViewModel.setAverageResponseTime(results.getAvgResponseTime());

        });

        demandValidator.startTest();
        mDemandValidationViewModel.setBidRequestsSent(true);
    }
}
