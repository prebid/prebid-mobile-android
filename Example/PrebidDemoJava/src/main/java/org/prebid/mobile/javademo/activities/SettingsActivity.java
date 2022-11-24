package org.prebid.mobile.javademo.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import org.prebid.mobile.javademo.R;
import org.prebid.mobile.javademo.databinding.ActivitySettingsBinding;
import org.prebid.mobile.javademo.utils.Settings;

public class SettingsActivity extends AppCompatActivity {

    private final Settings settings = Settings.get();
    private ActivitySettingsBinding binding;

    public static Intent getIntent(Context context) {
        return new Intent(context, SettingsActivity.class);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.settings));
        }

        initViews();
    }

    @Override
    protected void onStart() {
        super.onStart();
        binding.etRefreshTime.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkAllFields();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
        });
    }

    private void initViews() {
        binding.etRefreshTime.setText(String.valueOf(settings.getRefreshTimeSeconds()));
    }

    private void checkAllFields() {
        hideError();

        String refreshTime = binding.etRefreshTime.getText().toString();
        int refreshTimeInt;
        try {
            refreshTimeInt = Integer.parseInt(refreshTime);
        } catch (Throwable any) {
            refreshTimeInt = 0;
        }
        if (refreshTimeInt < 30 || refreshTimeInt > 120) {
            showError("Refresh time must be in range from 30 to 120 seconds");
            settings.setRefreshTimeSeconds(30);
            return;
        } else {
            settings.setRefreshTimeSeconds(refreshTimeInt);
        }
    }

    private void showError(String error) {
        binding.tvError.setText(error);
    }

    private void hideError() {
        binding.tvError.setText("");
    }

}
