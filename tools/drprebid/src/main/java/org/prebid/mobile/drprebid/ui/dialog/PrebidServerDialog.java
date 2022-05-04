package org.prebid.mobile.drprebid.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProviders;
import org.prebid.mobile.drprebid.R;
import org.prebid.mobile.drprebid.managers.SettingsManager;
import org.prebid.mobile.drprebid.model.PrebidServer;
import org.prebid.mobile.drprebid.model.PrebidServerSettings;
import org.prebid.mobile.drprebid.ui.viewmodels.SettingsViewModel;

public class PrebidServerDialog extends DialogFragment {

    public static final String TAG = PrebidServerDialog.class.getSimpleName();

    private RadioGroup serverGroup;
    private EditText customServerField;
    private SettingsViewModel settingsViewModel;

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
            View view = LayoutInflater.from(getActivity())
                                      .inflate(R.layout.dialog_prebid_server_selection, null, false);
            builder.setView(view);

            serverGroup = view.findViewById(R.id.group_server);
            customServerField = view.findViewById(R.id.field_custom_server);

            serverGroup.setOnCheckedChangeListener((group, checkedId) -> {
                switch (checkedId) {
                    case R.id.radio_appnexus:
                        customServerField.setVisibility(View.GONE);
                        break;
                    case R.id.radio_rubicon:
                        customServerField.setVisibility(View.GONE);
                        break;
                    case R.id.radio_custom:
                        customServerField.setVisibility(View.VISIBLE);
                        break;
                }
            });

            fillValues();

            builder.setPositiveButton(R.string.action_accept, (dialog, which) -> {
                SettingsManager settingsManager = SettingsManager.getInstance(getActivity());
                settingsViewModel = ViewModelProviders.of(getActivity()).get(SettingsViewModel.class);

                switch (serverGroup.getCheckedRadioButtonId()) {
                    case R.id.radio_appnexus:
                        settingsManager.setPrebidServer(PrebidServer.APPNEXUS);
                        settingsManager.setPrebidServerCustomUrl("");
                        settingsViewModel.setPrebidServer(PrebidServer.APPNEXUS);
                        break;
                    case R.id.radio_rubicon:
                        settingsManager.setPrebidServer(PrebidServer.RUBICON);
                        settingsManager.setPrebidServerCustomUrl("");
                        settingsViewModel.setPrebidServer(PrebidServer.RUBICON);
                        break;
                    case R.id.radio_custom:
                        settingsManager.setPrebidServer(PrebidServer.CUSTOM);
                        settingsManager.setPrebidServerCustomUrl(customServerField.getText().toString());
                        settingsViewModel.setPrebidServer(PrebidServer.CUSTOM);
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
                serverGroup.check(R.id.radio_appnexus);
                break;
            case RUBICON:
                serverGroup.check(R.id.radio_rubicon);
                break;
            case CUSTOM:
                serverGroup.check(R.id.radio_custom);
                customServerField.setText(settings.getCustomPrebidServerUrl());
                break;
        }
    }
}
