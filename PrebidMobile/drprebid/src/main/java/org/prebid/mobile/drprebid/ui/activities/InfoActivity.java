package org.prebid.mobile.drprebid.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.webkit.WebView;

import org.prebid.mobile.drprebid.R;

import java.util.Locale;

public class InfoActivity extends AppCompatActivity {
    public static final String EXTRA_TITLE = "screen_title";
    public static final String EXTRA_HTML_CONTENT = "screen_html_content";

    private WebView mWebView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        mWebView = findViewById(R.id.web_view);

        Bundle extras = getIntent().getExtras();

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (extras != null && extras.containsKey(EXTRA_TITLE) && extras.containsKey(EXTRA_HTML_CONTENT)) {
            setupUI(extras.getString(EXTRA_TITLE), extras.getString(EXTRA_HTML_CONTENT));
        } else {
            finish();
        }
    }

    private void setupUI(String title, String htmlFile) {
        if (!TextUtils.isEmpty(title)) {
            setTitle(title);
        }

        if (TextUtils.isEmpty(htmlFile)) {
            finish();
        } else {
            String url = String.format(Locale.ENGLISH, "file:///android_asset/%s", htmlFile);
            mWebView.loadUrl(url);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static Intent newIntent(Context context, int titleRes, int htmlContentFileRes) {
        return newIntent(context, context.getString(titleRes), context.getString(htmlContentFileRes));
    }

    public static Intent newIntent(Context context, int titleRes, String htmlContentFile) {
        return newIntent(context, context.getString(titleRes), htmlContentFile);
    }

    public static Intent newIntent(Context context, String title, String htmlContentFile) {
        Intent intent = new Intent(context, InfoActivity.class);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_HTML_CONTENT, htmlContentFile);
        return intent;
    }
}
