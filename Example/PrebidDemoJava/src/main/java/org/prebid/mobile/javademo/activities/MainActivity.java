/*
 *    Copyright 2018-2019 Prebid.org, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.prebid.mobile.javademo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.prebid.mobile.javademo.R;
import org.prebid.mobile.javademo.databinding.ActivityMainBinding;
import org.prebid.mobile.javademo.testcases.AdFormat;
import org.prebid.mobile.javademo.testcases.IntegrationKind;
import org.prebid.mobile.javademo.testcases.TestCase;
import org.prebid.mobile.javademo.testcases.TestCaseAdapter;
import org.prebid.mobile.javademo.testcases.TestCaseRepository;
import org.prebid.mobile.javademo.utils.Settings;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private IntegrationKind integrationKind;
    private AdFormat adFormat;
    private String searchRequest;

    private ActivityMainBinding binding;
    private TestCaseAdapter testCaseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        initSpinners();
        initSearch();
        initList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.settings) {
            startActivity(SettingsActivity.getIntent(this));
            return true;
        }
        return false;
    }

    private void initSpinners() {
        Spinner kindSpinner = binding.spinnerIntegrationKind;
        IntegrationKind[] kinds = IntegrationKind.values();
        ArrayList<String> kindTitles = new ArrayList<>(kinds.length);
        kindTitles.add(0, "All");
        for (IntegrationKind kind : kinds) {
            kindTitles.add(kind.getAdServer());
        }
        kindSpinner.setAdapter(
            new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                kindTitles
            )
        );
        kindSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    integrationKind = null;
                } else {
                    integrationKind = IntegrationKind.values()[position - 1];
                }
                Settings.get().setLastIntegrationKindId(position);
                updateList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        kindSpinner.setSelection(Settings.get().getLastIntegrationKindId());


        Spinner formatSpinner = binding.spinnerAdType;
        AdFormat[] formats = AdFormat.values();
        ArrayList<String> formatTitles = new ArrayList<>(formats.length);
        formatTitles.add(0, "All");
        for (AdFormat format : formats) {
            formatTitles.add(format.getDescription());
        }
        formatSpinner.setAdapter(
            new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                formatTitles
            )
        );
        formatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    adFormat = null;
                } else {
                    adFormat = AdFormat.values()[position - 1];
                }
                Settings.get().setLastAdFormatId(position);
                updateList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        formatSpinner.setSelection(Settings.get().getLastAdFormatId());
    }

    private void initSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchRequest = s.toString();
                updateList();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void initList() {
        RecyclerView rv = binding.rvAdTypes;
        testCaseAdapter = new TestCaseAdapter(this::showAd);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(testCaseAdapter);
    }

    private void updateList() {
        ArrayList<TestCase> list = TestCaseRepository.getList();
        ArrayList<TestCase> filteredList = new ArrayList<>(list.size());

        for (TestCase testCase : list) {
            boolean ignore = false;
            if (integrationKind != null && integrationKind != testCase.getIntegrationKind()) {
                ignore = true;
            }

            if (adFormat != null && adFormat != testCase.getAdFormat()) {
                ignore = true;
            }

            if (searchRequest != null && !searchRequest.isEmpty() && !getString(testCase.getTitleStringRes()).toLowerCase().contains(searchRequest.toLowerCase())) {
                ignore = true;
            }

            if (!ignore) {
                filteredList.add(testCase);
            }
        }

        testCaseAdapter.setList(filteredList);
    }

    private void showAd(TestCase testCase) {
        TestCaseRepository.lastTestCase = testCase;
        startActivity(new Intent(this, testCase.getActivity()));
    }

}
