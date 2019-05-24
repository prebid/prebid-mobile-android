package org.prebid.mobile.drprebid.ui.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import org.prebid.mobile.drprebid.R;

public class TestResultsActivity extends AppCompatActivity {

    private RecyclerView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListView = findViewById(R.id.list_results);
        setupResultsList();

        runTests();
    }

    private void setupResultsList() {

    }

    private void runTests() {

    }
}
