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

package org.prebid.mobile.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import org.prebid.mobile.ExternalUserId;
import org.prebid.mobile.Host;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.app.ads.AdType;
import org.prebid.mobile.app.ads.AdTypesRepository;
import org.prebid.mobile.app.ads.xandr.*;
import org.prebid.mobile.app.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private String adType = "";
    private String adServer = "";
    private String adSize = "";
    private Host host = Host.RUBICON;

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.btnShowAd.setOnClickListener((view) -> showAd());
        initPrebidExternalUserIds();
        initAdServerSpinner();
    }

    public void showAd() {
        int autoRefreshTime = getAutoRefreshTime();
        Intent intent = DemoActivity.getIntent(this, adServer, adType, autoRefreshTime);
        startActivity(intent);
    }

    private int getAutoRefreshTime() {
        String refreshTimeString = binding.etAutoRefresh.getText().toString();
        try {
            return Integer.parseInt(refreshTimeString);
        } catch (Exception ignored) {
        }
        return 0;
    }

    @Deprecated
    public void showAd(View view) {
        EditText autoRefreshMillis = binding.etAutoRefresh;
        String refreshMillisString = autoRefreshMillis.getText().toString();
        Intent intent = null;
        if (host.equals(Host.APPNEXUS)) {
            if (adType.equals(getString(R.string.adTypeBanner)) && adServer.equals(getString(R.string.adServerAdManager))) {
                intent = new Intent(this, XandrBannerGamDemoActivity.class);
                intent.putExtra(Constants.AD_SIZE_NAME, adSize);
                if (!TextUtils.isEmpty(refreshMillisString)) {
                    int refreshMillis = Integer.valueOf(refreshMillisString);
                    intent.putExtra(Constants.AUTO_REFRESH_NAME, refreshMillis);
                }
            } else if (adType.equals(getString(R.string.adTypeInterstitial)) && adServer.equals(getString(R.string.adServerAdManager))) {
                intent = new Intent(this, XandrInterstitialGamDemoActivity.class);
                if (!TextUtils.isEmpty(refreshMillisString)) {
                    int refreshMillis = Integer.valueOf(refreshMillisString);
                    intent.putExtra(Constants.AUTO_REFRESH_NAME, refreshMillis);
                }
            } else if (adType.equals(getString(R.string.adTypeInBannerNative)) && adServer.equals(getString(R.string.adServerAdManager))) {
                intent = new Intent(this, XandrNativeInBannerGamDemoActivity.class);
                if (!TextUtils.isEmpty(refreshMillisString)) {
                    int refreshMillis = Integer.valueOf(refreshMillisString);
                    intent.putExtra(Constants.AUTO_REFRESH_NAME, refreshMillis);
                }
            } else if (adType.equals(getString(R.string.adTypeInstreamVideo)) && adServer.equals(getString(R.string.adServerAdManager))) {
                intent = new Intent(this, XandrInstreamVideoGamActivity.class);
                if (!TextUtils.isEmpty(refreshMillisString)) {
                    int refreshMillis = Integer.valueOf(refreshMillisString);
                    intent.putExtra(Constants.AUTO_REFRESH_NAME, refreshMillis);
                }
            } else if (adType.equals(getString(R.string.adTypeInAppNative)) && adServer.equals(getString(R.string.adServerAdManager))) {
                intent = new Intent(this, XandrNativeInAppGAMDemoActivity.class);
            }
        }
        if (intent != null) {
            startActivity(intent);
        } else {
            Toast.makeText(getApplicationContext(), "Not supported yet.", Toast.LENGTH_SHORT).show();
        }
    }


    private void initPrebidExternalUserIds() {
        ArrayList<ExternalUserId> externalUserIdArray = new ArrayList<>();
        externalUserIdArray.add(new ExternalUserId(
                "adserver.org",
                "111111111111",
                null,
                new HashMap() {{
                    put("rtiPartner", "TDID");
                }}
        ));
        externalUserIdArray.add(new ExternalUserId("netid.de", "999888777", null, null));
        externalUserIdArray.add(new ExternalUserId("criteo.com", "_fl7bV96WjZsbiUyQnJlQ3g4ckh5a1N", null, null));
        externalUserIdArray.add(new ExternalUserId("liveramp.com", "AjfowMv4ZHZQJFM8TpiUnYEyA81Vdgg", null, null));
        externalUserIdArray.add(new ExternalUserId(
                "sharedid.org",
                "111111111111",
                1,
                new HashMap() {{
                    put("third", "01ERJWE5FS4RAZKG6SKQ3ZYSKV");
                }}
        ));
        PrebidMobile.setExternalUserIds(externalUserIdArray);
    }

    private void initAdServerSpinner() {
        Map<String, List<AdType>> repository = AdTypesRepository.get();
        ArrayList<String> primaryAdServers = new ArrayList<>(repository.keySet());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, primaryAdServers);

        Spinner spinner = binding.spinnerAdServer;
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                adServer = primaryAdServers.get(position);
                List<AdType> adTypes = repository.get(adServer);
                ArrayList<String> stringTypes = new ArrayList<>(5);
                for (AdType adType : adTypes) {
                    stringTypes.add(adType.getName());
                }
                initAdTypeSpinner(stringTypes);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void initAdTypeSpinner(ArrayList<String> list) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, list);

        Spinner spinner = binding.spinnerAdType;
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                adType = list.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

}
