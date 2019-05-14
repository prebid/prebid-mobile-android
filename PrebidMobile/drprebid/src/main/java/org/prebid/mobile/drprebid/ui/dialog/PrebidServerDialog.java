package org.prebid.mobile.drprebid.ui.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.prebid.mobile.drprebid.R;

public class PrebidServerDialog extends DialogFragment {
    public static final String TAG = PrebidServerDialog.class.getSimpleName();
    private static final String ARG_TITLE = "title";

    private RadioGroup mServerGroup;
    private EditText mCustomServerField;

    public PrebidServerDialog() {

    }

    public static PrebidServerDialog newInstance() {
        PrebidServerDialog fragment = new PrebidServerDialog();

        return fragment;
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

            mServerGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
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
}
