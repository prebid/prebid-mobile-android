package org.prebid.mobile.drprebid.ui.viewholders;

import android.content.Intent;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;
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

    private RadioGroup adFormatGroup;
    private TextView adSizeView;
    private SettingsViewModel settingsViewModel;

    public GeneralSettingsViewHolder(@NonNull final View itemView) {
        super(itemView);

        itemView.findViewById(R.id.button_info).setOnClickListener(v -> {
            HelpScreen aboutScreen = HelpScreenUtil.getGeneralInfo(itemView.getContext());
            Intent intent = InfoActivity.newIntent(
                    itemView.getContext(),
                    aboutScreen.getTitle(),
                    aboutScreen.getHtmlAsset()
            );
            itemView.getContext().startActivity(intent);
        });

        adFormatGroup = itemView.findViewById(R.id.group_format);
        adFormatGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.radio_banner:
                    SettingsManager.getInstance(itemView.getContext()).setAdFormat(AdFormat.BANNER);
                    break;
                case R.id.radio_interstitial:
                    SettingsManager.getInstance(itemView.getContext()).setAdFormat(AdFormat.INTERSTITIAL);
                    break;
            }
        });

        adSizeView = itemView.findViewById(R.id.view_ad_size);
        adSizeView.setOnClickListener(v -> {
            FragmentManager fragmentManager = ((AppCompatActivity) itemView.getContext()).getSupportFragmentManager();
            AdSizeDialog dialog = AdSizeDialog.newInstance();
            dialog.show(fragmentManager, AdSizeDialog.TAG);
        });

        settingsViewModel = ViewModelProviders.of((AppCompatActivity) itemView.getContext())
                                              .get(SettingsViewModel.class);

        settingsViewModel.getAdSize().observe(this, adSize -> {
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
                adFormatGroup.check(R.id.radio_banner);
                break;
            case INTERSTITIAL:
                adFormatGroup.check(R.id.radio_interstitial);
                break;
            default:
                adFormatGroup.check(R.id.radio_banner);
        }
    }

    private void fillAdSize(AdSize adSize) {
        switch (adSize) {
            case BANNER_300x250:
                adSizeView.setText(R.string.ad_size_300_250);
                break;
            case BANNER_300x600:
                adSizeView.setText(R.string.ad_size_300_600);
                break;
            case BANNER_320x50:
                adSizeView.setText(R.string.ad_size_320_50);
                break;
            case BANNER_320x100:
                adSizeView.setText(R.string.ad_size_320_100);
                break;
            case BANNER_320x480:
                adSizeView.setText(R.string.ad_size_320_480);
                break;
            case BANNER_728x90:
                adSizeView.setText(R.string.ad_size_728_90);
                break;
        }
    }
}
