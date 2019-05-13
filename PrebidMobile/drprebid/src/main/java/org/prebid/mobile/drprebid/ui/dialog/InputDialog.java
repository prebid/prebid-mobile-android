package org.prebid.mobile.drprebid.ui.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.prebid.mobile.drprebid.R;

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

            View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_input, null, false);
            builder.setView(view);

            mInput = view.findViewById(R.id.field_input);

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
