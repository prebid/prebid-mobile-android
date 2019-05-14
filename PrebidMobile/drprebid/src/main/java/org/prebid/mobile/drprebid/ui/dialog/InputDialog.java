package org.prebid.mobile.drprebid.ui.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.prebid.mobile.drprebid.R;
import org.prebid.mobile.drprebid.managers.QrCodeScanCacheManager;
import org.prebid.mobile.drprebid.ui.activities.QrCodeCaptureActivity;

public class InputDialog extends DialogFragment {
    public static final String TAG = InputDialog.class.getSimpleName();
    private static final String ARG_TITLE = "title";

    private EditText mInput;

    public InputDialog() {

    }

    public static InputDialog newInstance(String title) {
        InputDialog fragment = new InputDialog();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        fragment.setArguments(args);

        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (getActivity() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            if (getArguments() != null && getArguments().containsKey(ARG_TITLE)) {
                String title = getArguments().getString(ARG_TITLE);
                builder.setTitle(title);
            }

            final View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_input, null, false);
            builder.setView(view);

            mInput = view.findViewById(R.id.field_input);
            view.findViewById(R.id.button_scan).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openCaptureActivity();
                }
            });

            builder.setPositiveButton(R.string.action_accept, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dismiss();
                }
            });

            builder.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dismiss();
                }
            });

            return builder.create();
        }

        return super.onCreateDialog(savedInstanceState);
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
                mInput.setText(readValue);
            }
        }
    }
}
