package org.prebid.mobile.drprebid.ui.viewholders;

import android.content.Intent;
import android.view.View;
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
import org.prebid.mobile.drprebid.model.HelpScreen;
import org.prebid.mobile.drprebid.model.PrebidServer;
import org.prebid.mobile.drprebid.model.PrebidServerSettings;
import org.prebid.mobile.drprebid.ui.activities.InfoActivity;
import org.prebid.mobile.drprebid.ui.dialog.InputDialog;
import org.prebid.mobile.drprebid.ui.dialog.PrebidServerDialog;
import org.prebid.mobile.drprebid.ui.viewmodels.SettingsViewModel;
import org.prebid.mobile.drprebid.util.HelpScreenUtil;

public class PrebidServerSettingsViewholder extends RecyclerView.ViewHolder implements SettingsViewHolder, LifecycleOwner, View.OnClickListener {
    private TextView mServerView;
    private TextView mAccountIdView;
    private TextView mConfigIdView;
    private SettingsViewModel mSettingsViewModel;

    public PrebidServerSettingsViewholder(@NonNull final View itemView) {
        super(itemView);

        itemView.findViewById(R.id.button_info).setOnClickListener(this);

        mServerView = itemView.findViewById(R.id.view_prebid_server);
        mServerView.setOnClickListener(this);
        mAccountIdView = itemView.findViewById(R.id.view_account_id);
        mAccountIdView.setOnClickListener(this);
        mConfigIdView = itemView.findViewById(R.id.view_config_id);
        mConfigIdView.setOnClickListener(this);

        mSettingsViewModel = ViewModelProviders.of((AppCompatActivity) itemView.getContext()).get(SettingsViewModel.class);

        mSettingsViewModel.getPrebidServer().observe(this, prebidServer -> {
            if (prebidServer != null) {
                fillPrebidServer(prebidServer);
            }
        });

        mSettingsViewModel.getAccountId().observe(this, accountId -> {
            if (accountId != null) {
                mConfigIdView.setText(accountId);
            }
        });

        mSettingsViewModel.getConfigId().observe(this, configId -> {
            if (configId != null) {
                mConfigIdView.setText(configId);
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
        PrebidServerSettings settings = SettingsManager.getInstance(itemView.getContext()).getPrebidServerSettings();

        fillPrebidServer(settings.getPrebidServer());
        mAccountIdView.setText(settings.getAccountId());
        mConfigIdView.setText(settings.getConfigId());
    }

    private void fillPrebidServer(PrebidServer prebidServer) {
        switch (prebidServer) {
            case APPNEXUS:
                mServerView.setText(R.string.prebid_server_appnexus);
                break;
            case RUBICON:
                mServerView.setText(R.string.prebid_server_rubicon);
                break;
            case CUSTOM:
                mServerView.setText(R.string.prebid_server_custom);
                break;
        }
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
