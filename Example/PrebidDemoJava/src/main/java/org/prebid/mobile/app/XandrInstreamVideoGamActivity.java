package org.prebid.mobile.app;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.prebid.mobile.AdUnit;

public class XandrInstreamVideoGamActivity extends AppCompatActivity {
    AdUnit adUnit;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adUnit != null) {
            adUnit.stopAutoRefresh();
            adUnit = null;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
}
