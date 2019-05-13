package org.prebid.mobile.drprebid.ui.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.prebid.mobile.drprebid.R;

public class AdSizeDialog extends DialogFragment {
    public static final String TAG = AdSizeDialog.class.getSimpleName();

    private RadioGroup mSizeGroup;

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

            mSizeGroup = view.findViewById(R.id.group_size);

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
