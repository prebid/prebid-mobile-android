package org.prebid.mobile.javademo.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.preference.PreferenceManager;
import org.prebid.mobile.Host;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.javademo.R;
import org.prebid.mobile.javademo.ads.AdType;
import org.prebid.mobile.javademo.ads.AdTypesRepository;
import org.prebid.mobile.javademo.databinding.ActivityDemoBinding;

import java.util.List;
import java.util.Map;

public class DemoActivity extends AppCompatActivity {

    private static final String TAG = DemoActivity.class.getSimpleName();

    private static final String ARGS_AD_SERVER_NAME = "adServer";
    private static final String ARGS_AD_TYPE_NAME = "adType";
    private static final String ARGS_AD_REFRESH_TIME = "autoRefresh";

    public static Intent getIntent(
        Context context,
        String adPrimaryServerName,
        String adTypeName,
        int adAutoRefreshTime
    ) {
        Intent intent = new Intent(context, DemoActivity.class);
        intent.putExtra(ARGS_AD_SERVER_NAME, adPrimaryServerName);
        intent.putExtra(ARGS_AD_TYPE_NAME, adTypeName);
        intent.putExtra(ARGS_AD_REFRESH_TIME, adAutoRefreshTime);
        return intent;
    }

    private int adAutoRefreshTime = 0;

    private String adPrimaryServerName = "";
    private String adTypeName = "";

    private ActivityDemoBinding binding;
    private AdType currentAdType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_demo);

        parseArguments();
        choosePrebidServer();
        useFakeGpdr();
        initViews();
        createAd();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (currentAdType != null) {
            currentAdType.getOnDestroy().run();
        }
        PrebidMobile.clearStoredBidResponses();
    }

    private void parseArguments() {
        Intent intent = getIntent();
        adPrimaryServerName = intent.getStringExtra(ARGS_AD_SERVER_NAME);
        adTypeName = intent.getStringExtra(ARGS_AD_TYPE_NAME);
        adAutoRefreshTime = intent.getIntExtra(ARGS_AD_REFRESH_TIME, 0);
    }

    private void choosePrebidServer() {
        if (adPrimaryServerName.equals("Google Ad Manager (Rubicon)")) {
            PrebidMobile.setPrebidServerAccountId("1001");
            PrebidMobile.setPrebidServerHost(
                Host.createCustomHost("https://prebid-server.rubiconproject.com/openrtb2/auction")
            );
        } else {
            PrebidMobile.setPrebidServerAccountId("0689a263-318d-448b-a3d4-b02e8a709d9d");
            PrebidMobile.setPrebidServerHost(
                Host.createCustomHost("https://prebid-server-test-j.prebid.org/openrtb2/auction")
            );
        }
    }

    private void useFakeGpdr() {
        PreferenceManager
            .getDefaultSharedPreferences(this)
            .edit()
            .putInt("IABTCF_gdprApplies", 0)
            .putInt("IABTCF_CmpSdkID", 123)
            .apply();
    }

    private void initViews() {
        binding.tvPrimaryAdServer.setText(adPrimaryServerName);
        binding.tvAdType.setText(adTypeName);
    }

    private void createAd() {
        binding.frameAdWrapper.removeAllViews();

        Map<String, List<AdType>> allAdTypes = AdTypesRepository.get();
        List<AdType> currentPrimaryAdServerTypes = allAdTypes.get(adPrimaryServerName);
        for (AdType adType : currentPrimaryAdServerTypes) {
            if (adType.getName().equals(adTypeName)) {
                currentAdType = adType;
            }
        }

        if (currentAdType != null) {
            currentAdType.getOnCreate().run(this, binding.frameAdWrapper, adAutoRefreshTime);
        } else {
            Log.e(TAG, "Can't find such ad type in repository.");
            finish();
        }
    }

}
