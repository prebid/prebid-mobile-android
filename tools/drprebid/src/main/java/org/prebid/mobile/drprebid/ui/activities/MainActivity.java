package org.prebid.mobile.drprebid.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.prebid.mobile.drprebid.R;
import org.prebid.mobile.drprebid.managers.UserPrefsManager;
import org.prebid.mobile.drprebid.model.HelpScreen;
import org.prebid.mobile.drprebid.ui.adapters.SettingsAdapter;
import org.prebid.mobile.drprebid.util.HelpScreenUtil;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_WELCOME = 2000;

    private RecyclerView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            if (!UserPrefsManager.getInstance(this).hasWelcomeBeenShown()) {
                startActivityForResult(new Intent(this, WelcomeActivity.class), REQUEST_WELCOME);
            }
        }

        listView = findViewById(R.id.list_settings);
        setupSettingsList();

    }

    private void setupSettingsList() {
        SettingsAdapter adapter = new SettingsAdapter(this);

        listView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        listView.setItemAnimator(new DefaultItemAnimator());
        listView.setAdapter(adapter);
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
