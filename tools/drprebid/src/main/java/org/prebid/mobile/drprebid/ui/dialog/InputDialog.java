package org.prebid.mobile.drprebid.ui.dialog;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProviders;
import org.prebid.mobile.drprebid.Constants;
import org.prebid.mobile.drprebid.R;
import org.prebid.mobile.drprebid.managers.QrCodeScanCacheManager;
import org.prebid.mobile.drprebid.managers.SettingsManager;
import org.prebid.mobile.drprebid.ui.activities.QrCodeCaptureActivity;
import org.prebid.mobile.drprebid.ui.viewmodels.SettingsViewModel;

public class InputDialog extends DialogFragment {

    public static final String TAG = InputDialog.class.getSimpleName();

    private EditText input;
    private SettingsViewModel settingsViewModel;

    public InputDialog() {

    }

    public static InputDialog newInstance(
            String title,
            int type,
            int format,
            boolean shouldShowQrScanner
    ) {
        InputDialog fragment = new InputDialog();
        Bundle args = new Bundle();
        args.putString(Constants.Params.INPUT_TITLE, title);
        args.putInt(Constants.Params.INPUT_TYPE, type);
        args.putInt(Constants.Params.INPUT_FORMAT, format);
        args.putBoolean(Constants.Params.INPUT_SHOW_QR_SCANNER, shouldShowQrScanner);
        fragment.setArguments(args);

        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (getActivity() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            if (getArguments() != null && getArguments().containsKey(Constants.Params.INPUT_TITLE)) {
                String title = getArguments().getString(Constants.Params.INPUT_TITLE);
                builder.setTitle(title);
            }

            final View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_input, null, false);
            builder.setView(view);

            input = view.findViewById(R.id.field_input);

            if (getArguments() != null && getArguments().containsKey(Constants.Params.INPUT_TYPE)) {
                int type = getArguments().getInt(Constants.Params.INPUT_TYPE, -1);

                SettingsManager settingsManager = SettingsManager.getInstance(getActivity());

                switch (type) {
                    case Constants.Params.TYPE_AD_UNIT_ID:
                        input.setText(settingsManager.getAdServerSettings().getAdUnitId());
                        break;
                    case Constants.Params.TYPE_BID_PRICE:
                        input.setText(String.valueOf(settingsManager.getAdServerSettings().getBidPrice()));
                        break;
                    case Constants.Params.TYPE_ACCOUNT_ID:
                        input.setText(settingsManager.getPrebidServerSettings().getAccountId());
                        break;
                    case Constants.Params.TYPE_CONFIG_ID:
                        input.setText(settingsManager.getPrebidServerSettings().getConfigId());
                        break;
                }
            }


            if (getArguments() != null && getArguments().containsKey(Constants.Params.INPUT_FORMAT)) {
                int format = getArguments().getInt(Constants.Params.INPUT_FORMAT, Constants.Params.FORMAT_TEXT);

                switch (format) {
                    case Constants.Params.FORMAT_TEXT:
                        input.setInputType(InputType.TYPE_CLASS_TEXT);
                        break;
                    case Constants.Params.FORMAT_INT:
                        input.setInputType(InputType.TYPE_CLASS_NUMBER);
                        break;
                    case Constants.Params.FORMAT_FLOAT:
                        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                        break;
                    default:
                        input.setInputType(InputType.TYPE_CLASS_TEXT);

                }
            }

            ImageButton scanButton = view.findViewById(R.id.button_scan);
            if (getArguments() != null && getArguments().getBoolean(Constants.Params.INPUT_SHOW_QR_SCANNER, true)) {
                scanButton.setVisibility(View.VISIBLE);
                scanButton.setOnClickListener(v -> openCaptureActivity());
            } else {
                scanButton.setVisibility(View.GONE);
            }


            builder.setPositiveButton(R.string.action_accept, (dialog, which) -> {

                if (getArguments() != null && getArguments().containsKey(Constants.Params.INPUT_TYPE)) {
                    int type = getArguments().getInt(Constants.Params.INPUT_TYPE, -1);
                    settingsViewModel = ViewModelProviders.of(getActivity()).get(SettingsViewModel.class);

                    String text = input.getText().toString();
                    SettingsManager settingsManager = SettingsManager.getInstance(getActivity());

                    switch (type) {
                        case Constants.Params.TYPE_AD_UNIT_ID:
                            settingsViewModel.setAdUnitId(text);
                            settingsManager.setAdUnitId(text);
                            break;
                        case Constants.Params.TYPE_BID_PRICE:
                            if (TextUtils.isEmpty(text)) {
                                settingsViewModel.setBidPrice(0.0f);
                                settingsManager.setBidPrice(0.0f);
                            } else {
                                float value = Float.valueOf(text);
                                settingsViewModel.setBidPrice(value);
                                settingsManager.setBidPrice(value);
                            }
                            break;
                        case Constants.Params.TYPE_ACCOUNT_ID:
                            settingsViewModel.setAccountId(text);
                            settingsManager.setAccountId(text);
                            break;
                        case Constants.Params.TYPE_CONFIG_ID:
                            settingsViewModel.setConfigId(text);
                            settingsManager.setConfigId(text);
                            break;
                    }
                }

                dismiss();
            });

            builder.setNegativeButton(R.string.action_cancel, (dialog, which) -> dismiss());

            return builder.create();
        }

        return super.

                onCreateDialog(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
        checkForQrCodeCache();
    }

    @Override
    public void onDestroy() {
        QrCodeScanCacheManager.getInstance(getContext()).clearCache();
        super.onDestroy();
    }

    private void openCaptureActivity() {
        if (getContext() != null) {
            Intent intent = new Intent(getContext(), QrCodeCaptureActivity.class);
            intent.putExtra(QrCodeCaptureActivity.EXTRA_AUTO_FOCUS, true);
            intent.putExtra(QrCodeCaptureActivity.EXTRA_USE_FLASH, false);

            getContext().startActivity(intent);
        }
    }

    private void checkForQrCodeCache() {
        if (getContext() != null)
            if (QrCodeScanCacheManager.getInstance(getContext()).hasCache()) {
                String readValue = QrCodeScanCacheManager.getInstance(getContext()).getCache();
                if (!TextUtils.isEmpty(readValue)) {
                    input.setText(readValue);
                }
            }
    }
}
