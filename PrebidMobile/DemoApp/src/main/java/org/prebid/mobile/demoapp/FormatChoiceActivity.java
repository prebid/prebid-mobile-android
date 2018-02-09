package org.prebid.mobile.demoapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FormatChoiceActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice);
        LinearLayout rootView = (LinearLayout) findViewById(R.id.choiceRoot);
        rootView.setBackgroundColor(getResources().getColor(R.color.colorWhite));
        Intent intent = getIntent();
        if (intent != null) {
            String adServerName = (String) intent.getExtras().get(Constants.ADSERVER);
            if (Constants.DFP.equals(adServerName)) {
                rootView.addView(getTextView("AppNexus Banner 300x250", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(FormatChoiceActivity.this, DemoActivity.class);
                        intent.putExtra(Constants.ADSERVER, Constants.DFP);
                        intent.putExtra(Constants.DEMAND, Constants.APN_BANNER);
                        intent.putExtra(Constants.WIDTH, 300);
                        intent.putExtra(Constants.HEIGHT, 250);
                        startActivity(intent);
                    }
                }));
                rootView.addView(getTextView("AppNexus Banner 320x50", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(FormatChoiceActivity.this, DemoActivity.class);
                        intent.putExtra(Constants.ADSERVER, Constants.DFP);
                        intent.putExtra(Constants.DEMAND, Constants.APN_BANNER);
                        intent.putExtra(Constants.WIDTH, 320);
                        intent.putExtra(Constants.HEIGHT, 50);
                        startActivity(intent);
                    }
                }));
                rootView.addView(getTextView("AppNexus Interstitial", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(FormatChoiceActivity.this, DemoActivity.class);
                        intent.putExtra(Constants.ADSERVER, Constants.DFP);
                        intent.putExtra(Constants.DEMAND, Constants.APN_INTERSTITIAL);
                        startActivity(intent);
                    }
                }));
                rootView.addView(getTextView("Facebook Banner 300x250", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(FormatChoiceActivity.this, DemoActivity.class);
                        intent.putExtra(Constants.ADSERVER, Constants.DFP);
                        intent.putExtra(Constants.DEMAND, Constants.FB_BANNER);
                        intent.putExtra(Constants.WIDTH, 300);
                        intent.putExtra(Constants.HEIGHT, 250);
                        startActivity(intent);
                    }
                }));
                rootView.addView(getTextView("Facebook Interstitial", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(FormatChoiceActivity.this, DemoActivity.class);
                        intent.putExtra(Constants.ADSERVER, Constants.DFP);
                        intent.putExtra(Constants.DEMAND, Constants.FB_INTERSTITIAL);
                        startActivity(intent);
                    }
                }));
            } else if (Constants.MOPUB.equals(adServerName)) {
                rootView.addView(getTextView("AppNexus Banner 300x250", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(FormatChoiceActivity.this, DemoActivity.class);
                        intent.putExtra(Constants.ADSERVER, Constants.MOPUB);
                        intent.putExtra(Constants.DEMAND, Constants.APN_BANNER);
                        intent.putExtra(Constants.WIDTH, 300);
                        intent.putExtra(Constants.HEIGHT, 250);
                        startActivity(intent);
                    }
                }));
                rootView.addView(getTextView("AppNexus Banner 320x50", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(FormatChoiceActivity.this, DemoActivity.class);
                        intent.putExtra(Constants.ADSERVER, Constants.MOPUB);
                        intent.putExtra(Constants.DEMAND, Constants.APN_BANNER);
                        intent.putExtra(Constants.WIDTH, 320);
                        intent.putExtra(Constants.HEIGHT, 50);
                        startActivity(intent);
                    }
                }));
                rootView.addView(getTextView("AppNexus Interstitial", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(FormatChoiceActivity.this, DemoActivity.class);
                        intent.putExtra(Constants.ADSERVER, Constants.MOPUB);
                        intent.putExtra(Constants.DEMAND, Constants.APN_INTERSTITIAL);
                        startActivity(intent);
                    }
                }));
                rootView.addView(getTextView("Facebook Banner 300x250", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(FormatChoiceActivity.this, DemoActivity.class);
                        intent.putExtra(Constants.ADSERVER, Constants.MOPUB);
                        intent.putExtra(Constants.DEMAND, Constants.FB_BANNER);
                        intent.putExtra(Constants.WIDTH, 300);
                        intent.putExtra(Constants.HEIGHT, 250);
                        startActivity(intent);

                    }
                }));
                rootView.addView(getTextView("Facebook Interstitial", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(FormatChoiceActivity.this, DemoActivity.class);
                        intent.putExtra(Constants.ADSERVER, Constants.MOPUB);
                        intent.putExtra(Constants.DEMAND, Constants.FB_INTERSTITIAL);
                        startActivity(intent);
                    }
                }));
            } else {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(FormatChoiceActivity.this);
                alertBuilder.setTitle("You should never see this alert, something's wrong.");
                alertBuilder.create().show();
            }
        }
    }

    private TextView getTextView(String textToBeDisplayed, View.OnClickListener clickListener) {
        TextView textView = new TextView(FormatChoiceActivity.this);
        textView.setText(textToBeDisplayed);
        textView.setTextColor(getResources().getColor(R.color.colorBlack));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 50, 0, 50);
        textView.setLayoutParams(lp);
        textView.setTextSize(18);
        textView.setClickable(true);
        textView.setGravity(Gravity.CENTER);
        textView.setOnClickListener(clickListener);
        return textView;
    }
}
