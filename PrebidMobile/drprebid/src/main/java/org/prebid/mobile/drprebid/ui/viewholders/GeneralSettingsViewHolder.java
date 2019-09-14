package org.prebid.mobile.drprebid.ui.viewholders;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.prebid.mobile.drprebid.R;
import org.prebid.mobile.drprebid.managers.SettingsManager;
import org.prebid.mobile.drprebid.model.AdFormat;
import org.prebid.mobile.drprebid.model.AdSize;
import org.prebid.mobile.drprebid.model.GeneralSettings;
import org.prebid.mobile.drprebid.model.HelpScreen;
import org.prebid.mobile.drprebid.ui.activities.InfoActivity;
import org.prebid.mobile.drprebid.ui.dialog.AdSizeDialog;
import org.prebid.mobile.drprebid.ui.viewmodels.SettingsViewModel;
import org.prebid.mobile.drprebid.util.HelpScreenUtil;

public class GeneralSettingsViewHolder extends RecyclerView.ViewHolder implements SettingsViewHolder, LifecycleOwner {
    private RadioGroup mAdFormatGroup;
    private TextView mAdSizeView;
    private SettingsViewModel mSettingsViewModel;

    public GeneralSettingsViewHolder(@NonNull final View itemView) {
        super(itemView);

        itemView.findViewById(R.id.button_info).setOnClickListener(v -> {
            HelpScreen aboutScreen = HelpScreenUtil.getGeneralInfo(itemView.getContext());
            Intent intent = InfoActivity.newIntent(itemView.getContext(), aboutScreen.getTitle(), aboutScreen.getHtmlAsset());
            itemView.getContext().startActivity(intent);
        });

        mAdFormatGroup = itemView.findViewById(R.id.group_format);
        mAdFormatGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.radio_banner:
                    SettingsManager.getInstance(itemView.getContext()).setAdFormat(AdFormat.BANNER);
                    break;
                case R.id.radio_interstitial:
                    SettingsManager.getInstance(itemView.getContext()).setAdFormat(AdFormat.INTERSTITIAL);
                    break;
            }
        });

        mAdSizeView = itemView.findViewById(R.id.view_ad_size);
        mAdSizeView.setOnClickListener(v -> {
            FragmentManager fragmentManager = ((AppCompatActivity) itemView.getContext()).getSupportFragmentManager();
            AdSizeDialog dialog = AdSizeDialog.newInstance();
            dialog.show(fragmentManager, AdSizeDialog.TAG);
        });

        mSettingsViewModel = ViewModelProviders.of((AppCompatActivity) itemView.getContext()).get(SettingsViewModel.class);

        mSettingsViewModel.getAdSize().observe(this, adSize -> {
            if (adSize != null) {
                fillAdSize(adSize);
            }
        });
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return ((AppCompatActivity) itemView.getContext()).getLifecycle();
    }

    @Override
    public void bind() {
        fillValues();
    }

    private void fillValues() {
        GeneralSettings settings = SettingsManager.getInstance(itemView.getContext()).getGeneralSettings();

        fillAdSize(settings.getAdSize());

        switch (settings.getAdFormat()) {
            case BANNER:
                mAdFormatGroup.check(R.id.radio_banner);
                break;
            case INTERSTITIAL:
                mAdFormatGroup.check(R.id.radio_interstitial);
                break;
            default:
                mAdFormatGroup.check(R.id.radio_banner);
        }
    }

    private void fillAdSize(AdSize adSize) {
        switch (adSize) {
            case BANNER_300x250:
                mAdSizeView.setText(R.string.ad_size_300_250);
                break;
            case BANNER_300x600:
                mAdSizeView.setText(R.string.ad_size_300_600);
                break;
            case BANNER_320x50:
                mAdSizeView.setText(R.string.ad_size_320_50);
                break;
            case BANNER_320x100:
                mAdSizeView.setText(R.string.ad_size_320_100);
                break;
            case BANNER_320x480:
                mAdSizeView.setText(R.string.ad_size_320_480);
                break;
            case BANNER_728x90:
                mAdSizeView.setText(R.string.ad_size_728_90);
                break;
        }
    }
}
