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
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.prebid.mobile.Host;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Default values
    private String adType = "";
    private String adServer = "";
    private String adSize = "";
    private Host host = Host.RUBICON;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        // Get all the components
        Spinner hostSpinner = findViewById(R.id.hostSpinner);
        ArrayAdapter<CharSequence> hostAdapter = ArrayAdapter.createFromResource(this, R.array.hostArray, android.R.layout.simple_spinner_item);
        hostAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hostSpinner.setAdapter(hostAdapter);
        hostSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            List<String> hosts = Arrays.asList(getResources().getStringArray(R.array.hostArray));

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > hosts.size()) {
                    return;
                }
                host = hosts.get(position).equals("Rubicon") ? Host.RUBICON : Host.APPNEXUS;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        Spinner adTypeSpinner = (Spinner) findViewById(R.id.adTypeSpinner);
        // Ad Type Spinner set up
        ArrayAdapter<CharSequence> adTypeAdapter = ArrayAdapter.createFromResource(
                this, R.array.adTypeArray,
                android.R.layout.simple_spinner_item);
        adTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adTypeSpinner.setAdapter(adTypeAdapter);

        final LinearLayout adSizeRow = findViewById(R.id.adSizeRow);
        final LinearLayout adRefreshRow = findViewById(R.id.autoRefreshRow);
        adTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            List<String> adTypes = Arrays.asList(getResources().getStringArray(R.array.adTypeArray));

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {

                adSizeRow.setVisibility(View.INVISIBLE);
                adRefreshRow.setVisibility(View.INVISIBLE);

                if (pos > adTypes.size()) {
                    return;
                }
                adType = adTypes.get(pos);
                if (adType.equals("Banner")) {
                    // show size and refresh millis

                    adSizeRow.setVisibility(View.VISIBLE);
                    adRefreshRow.setVisibility(View.VISIBLE);
                } else if (adType.equals("Interstitial")) {
                    adRefreshRow.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        // Ad Server Spinner
        Spinner adServerSpinner = (Spinner) findViewById(R.id.adServerSpinner);
        ArrayAdapter<CharSequence> adServerAdapter = ArrayAdapter.createFromResource(
                this, R.array.adServerArray,
                android.R.layout.simple_spinner_item);
        adServerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adServerSpinner.setAdapter(adServerAdapter);
        adServerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            List<String> adServers = Arrays.asList(getResources().getStringArray(R.array.adServerArray));

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                if (pos > adServers.size()) {
                    return;
                }
                adServer = adServers.get(pos);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        // Ad Size Spinner
        Spinner adSizeSpinner = (Spinner) findViewById(R.id.adSizeSpinner);
        ArrayAdapter<CharSequence> adSizeAdapter = ArrayAdapter.createFromResource(
                this, R.array.adSizeArray,
                android.R.layout.simple_spinner_item);
        adSizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adSizeSpinner.setEnabled(false);
        adSizeSpinner.setAdapter(adSizeAdapter);
        adSizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            List<String> adSizes = Arrays.asList(getResources().getStringArray(R.array.adSizeArray));

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                if (pos > adSizes.size()) {
                    return;
                }
                adSize = adSizes.get(pos);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    public void showAd(View view) {
        EditText autoRefreshMillis = (EditText) findViewById(R.id.autoRefreshInput);
        String refreshMillisString = autoRefreshMillis.getText().toString();
        Intent intent = null;
        if (host.equals(Host.RUBICON)) {
            if (adType.equals(getString(R.string.adTypeBanner)) && adServer.equals(getString(R.string.adServerAdManager))) {
                intent = new Intent(this, RubiconBannerGamDemoActivity.class);
                intent.putExtra(Constants.AD_SIZE_NAME, adSize);
                if (!TextUtils.isEmpty(refreshMillisString)) {
                    int refreshMillis = Integer.valueOf(refreshMillisString);
                    intent.putExtra(Constants.AUTO_REFRESH_NAME, refreshMillis);
                }
            } else if (adType.equals(getString(R.string.adTypeBanner)) && adServer.equals(getString(R.string.adServerMoPub))) {
                intent = new Intent(this, RubiconBannerMoPubDemoActivity.class);
                intent.putExtra(Constants.AD_SIZE_NAME, adSize);
                if (!TextUtils.isEmpty(refreshMillisString)) {
                    int refreshMillis = Integer.valueOf(refreshMillisString);
                    intent.putExtra(Constants.AUTO_REFRESH_NAME, refreshMillis);
                }
            } else if (adType.equals(getString(R.string.adTypeInterstitial)) && adServer.equals(getString(R.string.adServerAdManager))) {
                intent = new Intent(this, RubiconIntersitialGamDemoActivity.class);
                if (!TextUtils.isEmpty(refreshMillisString)) {
                    int refreshMillis = Integer.valueOf(refreshMillisString);
                    intent.putExtra(Constants.AUTO_REFRESH_NAME, refreshMillis);
                }
            } else if (adType.equals(getString(R.string.adTypeInterstitial)) && adServer.equals(getString(R.string.adServerMoPub))) {
                intent = new Intent(this, RubiconInterstitialMoPubDemoActivity.class);
                if (!TextUtils.isEmpty(refreshMillisString)) {
                    int refreshMillis = Integer.valueOf(refreshMillisString);
                    intent.putExtra(Constants.AUTO_REFRESH_NAME, refreshMillis);
                }
            } else if (adType.equals(getString(R.string.adTypeBannerVideo)) && adServer.equals(getString(R.string.adServerAdManager))) {
                intent = new Intent(this, RubiconBannerVideoGamDemoActivity.class);
                if (!TextUtils.isEmpty(refreshMillisString)) {
                    int refreshMillis = Integer.valueOf(refreshMillisString);
                    intent.putExtra(Constants.AUTO_REFRESH_NAME, refreshMillis);
                }
            } else if (adType.equals(getString(R.string.adTypeInterstitialVideo)) && adServer.equals(getString(R.string.adServerAdManager))) {
                intent = new Intent(this, RubiconInterstitialVideoGamDemoActivity.class);
                if (!TextUtils.isEmpty(refreshMillisString)) {
                    int refreshMillis = Integer.valueOf(refreshMillisString);
                    intent.putExtra(Constants.AUTO_REFRESH_NAME, refreshMillis);
                }
            } else if (adType.equals(getString(R.string.adTypeInterstitialVideo)) && adServer.equals(getString(R.string.adServerMoPub))) {
                intent = new Intent(this, RubiconInterstitialVideoMoPubDemoActivity.class);
                if (!TextUtils.isEmpty(refreshMillisString)) {
                    int refreshMillis = Integer.valueOf(refreshMillisString);
                    intent.putExtra(Constants.AUTO_REFRESH_NAME, refreshMillis);
                }
            } else if (adType.equals(getString(R.string.adTypeRewardedVideo)) && adServer.equals(getString(R.string.adServerAdManager))) {
                intent = new Intent(this, RubiconRewardedVideoGamDemoActivity.class);
                if (!TextUtils.isEmpty(refreshMillisString)) {
                    int refreshMillis = Integer.valueOf(refreshMillisString);
                    intent.putExtra(Constants.AUTO_REFRESH_NAME, refreshMillis);
                }
            } else if (adType.equals(getString(R.string.adTypeRewardedVideo)) && adServer.equals(getString(R.string.adServerMoPub))) {
                intent = new Intent(this, RubiconRewardedVideoMoPubDemoActivity.class);
                if (!TextUtils.isEmpty(refreshMillisString)) {
                    int refreshMillis = Integer.valueOf(refreshMillisString);
                    intent.putExtra(Constants.AUTO_REFRESH_NAME, refreshMillis);
                }
            } else if (adType.equals(getString(R.string.adTypeInstreamVideo)) && adServer.equals(getString(R.string.adServerAdManager))) {
                intent = new Intent(this, RubiconInstreamVideoIMADemoActivity.class);
                if (!TextUtils.isEmpty(refreshMillisString)) {
                    int refreshMillis = Integer.valueOf(refreshMillisString);
                    intent.putExtra(Constants.AUTO_REFRESH_NAME, refreshMillis);
                }
            }
        } else if (host.equals(Host.APPNEXUS)) {
            if (adType.equals(getString(R.string.adTypeBanner)) && adServer.equals(getString(R.string.adServerAdManager))) {
                intent = new Intent(this, XandrBannerGamDemoActivity.class);
                intent.putExtra(Constants.AD_SIZE_NAME, adSize);
                if (!TextUtils.isEmpty(refreshMillisString)) {
                    int refreshMillis = Integer.valueOf(refreshMillisString);
                    intent.putExtra(Constants.AUTO_REFRESH_NAME, refreshMillis);
                }
            } else if (adType.equals(getString(R.string.adTypeBanner)) && adServer.equals(getString(R.string.adServerMoPub))) {
                intent = new Intent(this, XandrBannerMoPubDemoActivity.class);
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
            } else if (adType.equals(getString(R.string.adTypeInterstitial)) && adServer.equals(getString(R.string.adServerMoPub))) {
                intent = new Intent(this, XandrInterstitialMoPubDemoActivity.class);
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
            } else if (adType.equals(getString(R.string.adTypeInBannerNative)) && adServer.equals(getString(R.string.adServerMoPub))) {
                intent = new Intent(this, XandrNativeInBannerMoPubDemoActivity.class);
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
            }
        }
        if (intent != null) {
            startActivity(intent);
        } else {
            Toast.makeText(getApplicationContext(), "Not supported yet.", Toast.LENGTH_SHORT).show();
        }

    }
}
