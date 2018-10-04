package org.prebid.mobile;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Default values
    private String adType = "Banner";
    private String adServer = "DFP";
    private String adSize = "300x250";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        // Ad Type Spinner
        Spinner adTypeSpinner = (Spinner) findViewById(R.id.adTypeSpinner);
        ArrayAdapter<CharSequence> adTypeAdapter = ArrayAdapter.createFromResource(
                this, R.array.adTypeArray,
                android.R.layout.simple_spinner_item);
        adTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adTypeSpinner.setAdapter(adTypeAdapter);
        adTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            List<String> adTypes = Arrays.asList(getResources().getStringArray(R.array.adTypeArray));

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                if (pos > adTypes.size()) {
                    return;
                }
                adType = adTypes.get(pos);
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
        Intent demoActivityIntent = new Intent(this, DemoActivity.class);
        demoActivityIntent.putExtra(Constants.AD_SERVER_NAME, adServer);
        demoActivityIntent.putExtra(Constants.AD_TYPE_NAME, adType);
        if (adType.equals("Banner")) {
            demoActivityIntent.putExtra(Constants.AD_SIZE_NAME, adSize);
        }
        startActivity(demoActivityIntent);
    }


}
