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
import android.widget.EditText;
import android.widget.RadioGroup;

import org.prebid.mobile.drprebid.R;
import org.prebid.mobile.drprebid.managers.SettingsManager;
import org.prebid.mobile.drprebid.model.PrebidServer;
import org.prebid.mobile.drprebid.model.PrebidServerSettings;
import org.prebid.mobile.drprebid.ui.viewmodels.SettingsViewModel;

public class PrebidServerDialog extends DialogFragment {
    public static final String TAG = PrebidServerDialog.class.getSimpleName();

    private RadioGroup mServerGroup;
    private EditText mCustomServerField;
    private SettingsViewModel mSettingsViewModel;

    public PrebidServerDialog() {

    }

    public static PrebidServerDialog newInstance() {
        return new PrebidServerDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (getActivity() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle(R.string.prebid_server);
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_prebid_server_selection, null, false);
            builder.setView(view);

            mServerGroup = view.findViewById(R.id.group_server);
            mCustomServerField = view.findViewById(R.id.field_custom_server);

            mServerGroup.setOnCheckedChangeListener((group, checkedId) -> {
                switch (checkedId) {
                    case R.id.radio_appnexus:
                        mCustomServerField.setVisibility(View.GONE);
                        break;
                    case R.id.radio_rubicon:
                        mCustomServerField.setVisibility(View.GONE);
                        break;
                    case R.id.radio_custom:
                        mCustomServerField.setVisibility(View.VISIBLE);
                        break;
                }
            });

            fillValues();

            builder.setPositiveButton(R.string.action_accept, (dialog, which) -> {
                SettingsManager settingsManager = SettingsManager.getInstance(getActivity());
                mSettingsViewModel = ViewModelProviders.of(getActivity()).get(SettingsViewModel.class);

                switch (mServerGroup.getCheckedRadioButtonId()) {
                    case R.id.radio_appnexus:
                        settingsManager.setPrebidServer(PrebidServer.APPNEXUS);
                        settingsManager.setPrebidServerCustomUrl("");
                        mSettingsViewModel.setPrebidServer(PrebidServer.APPNEXUS);
                        break;
                    case R.id.radio_rubicon:
                        settingsManager.setPrebidServer(PrebidServer.RUBICON);
                        settingsManager.setPrebidServerCustomUrl("");
                        mSettingsViewModel.setPrebidServer(PrebidServer.RUBICON);
                        break;
                    case R.id.radio_custom:
                        settingsManager.setPrebidServer(PrebidServer.CUSTOM);
                        settingsManager.setPrebidServerCustomUrl(mCustomServerField.getText().toString());
                        mSettingsViewModel.setPrebidServer(PrebidServer.CUSTOM);
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
        PrebidServerSettings settings = SettingsManager.getInstance(getActivity()).getPrebidServerSettings();
        switch (settings.getPrebidServer()) {
            case APPNEXUS:
                mServerGroup.check(R.id.radio_appnexus);
                break;
            case RUBICON:
                mServerGroup.check(R.id.radio_rubicon);
                break;
            case CUSTOM:
                mServerGroup.check(R.id.radio_custom);
                mCustomServerField.setText(settings.getCustomPrebidServerUrl());
                break;
        }
    }
}
