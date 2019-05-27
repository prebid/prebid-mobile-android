package org.prebid.mobile.drprebid.ui.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.prebid.mobile.drprebid.R;
import org.prebid.mobile.drprebid.ui.adapters.TestResultsAdapter;

public class TestResultsActivity extends AppCompatActivity {

    private RecyclerView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_results);

        mListView = findViewById(R.id.list_results);
        setupResultsList();

        runTests();
    }

    private void setupResultsList() {
        TestResultsAdapter adapter = new TestResultsAdapter(this);

        mListView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        mListView.setItemAnimator(new DefaultItemAnimator());
        mListView.setAdapter(adapter);
    }

    private void runTests() {

    }
}
