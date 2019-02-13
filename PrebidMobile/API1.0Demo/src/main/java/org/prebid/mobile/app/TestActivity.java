

package org.prebid.mobile.app;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

public class TestActivity extends AppCompatActivity {
    private Handler mHandler = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        mHandler = new Handler(Looper.getMainLooper());
    }

    public void post(Runnable r) {
        mHandler.post(r);
    }
}
