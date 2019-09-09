package org.prebid.mobile.drprebid.ui.dialog;

import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;

import org.prebid.mobile.drprebid.R;
import org.prebid.mobile.drprebid.managers.SettingsManager;
import org.prebid.mobile.drprebid.model.AdSize;
import org.prebid.mobile.drprebid.model.GeneralSettings;
import org.prebid.mobile.drprebid.ui.viewmodels.SettingsViewModel;

public class AdSizeDialog extends DialogFragment {
    public static final String TAG = AdSizeDialog.class.getSimpleName();

    private RadioGroup mSizeGroup;
    private SettingsViewModel mSettingsViewModel;

    public AdSizeDialog() {

    }

    public static AdSizeDialog newInstance() {
        AdSizeDialog fragment = new AdSizeDialog();
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (getActivity() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle(R.string.ad_size);
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_ad_size_selection, null, false);
            builder.setView(view);

            mSettingsViewModel = ViewModelProviders.of(getActivity()).get(SettingsViewModel.class);
            mSizeGroup = view.findViewById(R.id.group_size);

            fillValues();

            builder.setPositiveButton(R.string.action_accept, (dialog, which) -> {
                switch (mSizeGroup.getCheckedRadioButtonId()) {
                    case R.id.radio_300_250:
                        mSettingsViewModel.setAdSize(AdSize.BANNER_300x250);
                        SettingsManager.getInstance(getActivity()).setAdSize(AdSize.BANNER_300x250);
                        break;
                    case R.id.radio_300_600:
                        mSettingsViewModel.setAdSize(AdSize.BANNER_300x600);
                        SettingsManager.getInstance(getActivity()).setAdSize(AdSize.BANNER_300x600);
                        break;
                    case R.id.radio_320_50:
                        mSettingsViewModel.setAdSize(AdSize.BANNER_320x50);
                        SettingsManager.getInstance(getActivity()).setAdSize(AdSize.BANNER_320x50);
                        break;
                    case R.id.radio_320_100:
                        mSettingsViewModel.setAdSize(AdSize.BANNER_320x100);
                        SettingsManager.getInstance(getActivity()).setAdSize(AdSize.BANNER_320x100);
                        break;
                    case R.id.radio_320_480:
                        mSettingsViewModel.setAdSize(AdSize.BANNER_320x480);
                        SettingsManager.getInstance(getActivity()).setAdSize(AdSize.BANNER_320x480);
                        break;
                    case R.id.radio_728_90:
                        mSettingsViewModel.setAdSize(AdSize.BANNER_728x90);
                        SettingsManager.getInstance(getActivity()).setAdSize(AdSize.BANNER_728x90);
                        break;
                }

                dismiss();
            });

            builder.setNegativeButton(R.string.action_cancel, (dialog, which) -> dismiss());

            return builder.create();
        }

        return super.onCreateDialog(savedInstanceState);
    }

    private void fillValues() {
        GeneralSettings generalSettings = SettingsManager.getInstance(getActivity()).getGeneralSettings();
        switch (generalSettings.getAdSize()) {
            case BANNER_300x250:
                mSizeGroup.check(R.id.radio_300_250);
                break;
            case BANNER_300x600:
                mSizeGroup.check(R.id.radio_300_600);
                break;
            case BANNER_320x50:
                mSizeGroup.check(R.id.radio_320_50);
                break;
            case BANNER_320x100:
                mSizeGroup.check(R.id.radio_320_100);
                break;
            case BANNER_320x480:
                mSizeGroup.check(R.id.radio_320_480);
                break;
            case BANNER_728x90:
                mSizeGroup.check(R.id.radio_728_90);
                break;
            default:
                mSizeGroup.check(R.id.radio_300_250);
        }
    }
}
