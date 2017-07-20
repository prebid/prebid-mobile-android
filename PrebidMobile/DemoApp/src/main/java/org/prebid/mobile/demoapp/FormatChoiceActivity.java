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
            if ("dfp".equals(adServerName)) {
                rootView.addView(getTextView("Show Banner Example", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(FormatChoiceActivity.this, DemoActivity.class);
                        intent.putExtra(Constants.ADSERVER, "dfp");
                        intent.putExtra(Constants.ADFORMAT, "banner");
                        startActivity(intent);
                    }
                }));
                rootView.addView(getTextView("Show Interstitial Example", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(FormatChoiceActivity.this, DemoActivity.class);
                        intent.putExtra(Constants.ADSERVER, "dfp");
                        intent.putExtra(Constants.ADFORMAT, "interstitial");
                        startActivity(intent);
                    }
                }));
            } else if ("mopub".equals(adServerName)) {
                rootView.addView(getTextView("Show Banner Example", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(FormatChoiceActivity.this, DemoActivity.class);
                        intent.putExtra(Constants.ADSERVER, "mopub");
                        intent.putExtra(Constants.ADFORMAT, "banner");
                        startActivity(intent);
                    }
                }));
                rootView.addView(getTextView("Show Interstitial Example", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(FormatChoiceActivity.this, DemoActivity.class);
                        intent.putExtra(Constants.ADSERVER, "mopub");
                        intent.putExtra(Constants.ADFORMAT, "interstitial");
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
