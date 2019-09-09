package org.prebid.mobile.drprebid.ui.viewholders;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import org.prebid.mobile.drprebid.Constants;
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
            if (!TextUtils.isEmpty(accountId)) {
                mAccountIdView.setText(accountId);
            } else {
                mAccountIdView.setText(R.string.click_to_choose);
            }
        });

        mSettingsViewModel.getConfigId().observe(this, configId -> {
            if (!TextUtils.isEmpty(configId)) {
                mConfigIdView.setText(configId);
            } else {
                mConfigIdView.setText(R.string.click_to_choose);
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

        if (!TextUtils.isEmpty(settings.getAccountId())) {
            mAccountIdView.setText(settings.getAccountId());
        }

        if (!TextUtils.isEmpty(settings.getConfigId())) {
            mConfigIdView.setText(settings.getConfigId());
        }
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
                openInputDialog(itemView.getContext().getString(R.string.account_id),
                        Constants.Params.TYPE_ACCOUNT_ID,
                        Constants.Params.FORMAT_TEXT, true);
                break;
            case R.id.view_config_id:
                openInputDialog(itemView.getContext().getString(R.string.config_id),
                        Constants.Params.TYPE_CONFIG_ID,
                        Constants.Params.FORMAT_TEXT, true);
                break;
        }
    }

    private void openInputDialog(String title, int type, int format, boolean shouldShowQrScanner) {
        FragmentManager fragmentManager = ((AppCompatActivity) itemView.getContext()).getSupportFragmentManager();
        InputDialog dialog = InputDialog.newInstance(title, type, format, shouldShowQrScanner);
        dialog.show(fragmentManager, InputDialog.TAG);
    }

    private void openPrebidServerDialog() {
        FragmentManager fragmentManager = ((AppCompatActivity) itemView.getContext()).getSupportFragmentManager();
        PrebidServerDialog dialog = PrebidServerDialog.newInstance();
        dialog.show(fragmentManager, PrebidServerDialog.TAG);
    }
}
