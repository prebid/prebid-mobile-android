package org.prebid.mobile.drprebid.ui.viewholders;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import org.prebid.mobile.drprebid.R;
import org.prebid.mobile.drprebid.model.HelpScreen;
import org.prebid.mobile.drprebid.ui.activities.InfoActivity;
import org.prebid.mobile.drprebid.ui.dialog.InputDialog;
import org.prebid.mobile.drprebid.ui.dialog.PrebidServerDialog;
import org.prebid.mobile.drprebid.util.HelpScreenUtil;

public class PrebidServerSettingsViewholder extends RecyclerView.ViewHolder implements SettingsViewHolder, View.OnClickListener {
    private TextView mServerView;
    private TextView mAccountIdView;
    private TextView mConfigIdView;

    public PrebidServerSettingsViewholder(@NonNull final View itemView) {
        super(itemView);

        itemView.findViewById(R.id.button_info).setOnClickListener(this);

        mServerView = itemView.findViewById(R.id.view_prebid_server);
        mServerView.setOnClickListener(this);
        mAccountIdView = itemView.findViewById(R.id.view_account_id);
        mAccountIdView.setOnClickListener(this);
        mConfigIdView = itemView.findViewById(R.id.view_config_id);
        mConfigIdView.setOnClickListener(this);
    }

    @Override
    public void bind() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_info:
                HelpScreen aboutScreen = HelpScreenUtil.getPrebidServerInfo(itemView.getContext());
                Intent intent = InfoActivity.newIntent(itemView.getContext(), aboutScreen.getTitle(), aboutScreen.getHtmlAsset());
                itemView.getContext().startActivity(intent);
                break;
            case R.id.view_prebid_server:
                openPrebidServerDialog();
                break;
            case R.id.view_account_id:
                openInputDialog(itemView.getContext().getString(R.string.account_id));
                break;
            case R.id.view_config_id:
                openInputDialog(itemView.getContext().getString(R.string.config_id));
                break;
        }
    }

    private void openInputDialog(String title) {
        FragmentManager fragmentManager = ((AppCompatActivity) itemView.getContext()).getSupportFragmentManager();
        InputDialog dialog = InputDialog.newInstance(title);
        dialog.show(fragmentManager, InputDialog.TAG);
    }

    private void openPrebidServerDialog() {
        FragmentManager fragmentManager = ((AppCompatActivity) itemView.getContext()).getSupportFragmentManager();
        PrebidServerDialog dialog = PrebidServerDialog.newInstance();
        dialog.show(fragmentManager, PrebidServerDialog.TAG);
    }
}
