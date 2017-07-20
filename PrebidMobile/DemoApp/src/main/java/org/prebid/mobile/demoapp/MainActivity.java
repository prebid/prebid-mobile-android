package org.prebid.mobile.demoapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView dfpButton = (TextView) findViewById(R.id.DFP);
        dfpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, FormatChoiceActivity.class);
                intent.putExtra(Constants.ADSERVER, "dfp");
                startActivity(intent);
            }
        });

        TextView mopubButton = (TextView) findViewById(R.id.MoPub);
        mopubButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, FormatChoiceActivity.class);
                intent.putExtra(Constants.ADSERVER, "mopub");
                startActivity(intent);
            }
        });
    }
}
