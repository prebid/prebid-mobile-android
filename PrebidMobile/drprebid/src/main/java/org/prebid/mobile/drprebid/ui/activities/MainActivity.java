package org.prebid.mobile.drprebid.ui.activities;

import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.prebid.mobile.drprebid.R;
import org.prebid.mobile.drprebid.managers.UserPrefsManager;
import org.prebid.mobile.drprebid.model.HelpScreen;
import org.prebid.mobile.drprebid.util.HelpScreenUtil;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_WELCOME = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            if (!UserPrefsManager.getInstance(this).hasWelcomeBeenShown()) {
                startActivityForResult(new Intent(this, WelcomeActivity.class), REQUEST_WELCOME);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case REQUEST_WELCOME:
                UserPrefsManager.getInstance(this).setHasWelcomeBeenShown(true);
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                HelpScreen aboutScreen = HelpScreenUtil.getAbout(this);
                Intent intent = InfoActivity.newIntent(this, aboutScreen.getTitle(), aboutScreen.getHtmlAsset());
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}
