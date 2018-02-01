package org.prebid.mobile.demoapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import org.prebid.mobile.core.BidManager;
import org.prebid.mobile.core.Prebid;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView dfpButton = (TextView) findViewById(R.id.DFP);
        dfpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchAdServer(Prebid.AdServer.DFP);
                Intent intent = new Intent(MainActivity.this, FormatChoiceActivity.class);
                intent.putExtra(Constants.ADSERVER, "dfp");
                startActivity(intent);
            }
        });

        TextView mopubButton = (TextView) findViewById(R.id.MoPub);
        mopubButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchAdServer(Prebid.AdServer.MOPUB);
                Intent intent = new Intent(MainActivity.this, FormatChoiceActivity.class);
                intent.putExtra(Constants.ADSERVER, "mopub");
                startActivity(intent);
            }
        });
    }

    private void switchAdServer(Prebid.AdServer adServer) {
        try {
            Field field = Prebid.class.getDeclaredField("adServer");
            field.setAccessible(true);
            field.set(null, adServer);
            Field useLocalCache = Prebid.class.getDeclaredField("useLocalCache");
            useLocalCache.setAccessible(true);
            switch (adServer) {
                case DFP:
                    useLocalCache.set(null, true);
                    break;
                case MOPUB:
                    useLocalCache.set(null, false);
                    break;
            }
            // refreshBids
            Method refreshBids = BidManager.class.getDeclaredMethod("refreshBids", Context.class);
            refreshBids.setAccessible(true);
            refreshBids.invoke(null, MainActivity.this);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
