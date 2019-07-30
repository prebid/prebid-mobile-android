package org.prebid.mobile.drprebid.ui.viewholders;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import org.prebid.mobile.drprebid.R;
import org.prebid.mobile.drprebid.managers.SettingsManager;
import org.prebid.mobile.drprebid.model.AdServerSettings;
import org.prebid.mobile.drprebid.model.PrebidServer;
import org.prebid.mobile.drprebid.model.PrebidServerSettings;
import org.prebid.mobile.drprebid.ui.activities.TestResultsActivity;

public class SubmitViewHolder extends RecyclerView.ViewHolder implements SettingsViewHolder, View.OnClickListener {

    public SubmitViewHolder(@NonNull View itemView) {
        super(itemView);

        itemView.findViewById(R.id.button_submit).setOnClickListener(this);
    }

    @Override
    public void bind() {

    }

    @Override
    public void onClick(View v) {
        if (validateSettings()) {
            Intent intent = new Intent(itemView.getContext(), TestResultsActivity.class);
            itemView.getContext().startActivity(intent);
        } else {
            Toast.makeText(itemView.getContext(), R.string.settings_validation_failed, Toast.LENGTH_LONG).show();
        }
    }

    private boolean validateSettings() {
        AdServerSettings adServerSettings = SettingsManager.getInstance(itemView.getContext()).getAdServerSettings();
        PrebidServerSettings prebidServerSettings = SettingsManager.getInstance(itemView.getContext()).getPrebidServerSettings();

        boolean valid = true;

        if (TextUtils.isEmpty(adServerSettings.getAdUnitId())) {
            valid = false;
        }

        if (adServerSettings.getBidPrice() <= 0.0) {
            valid = false;
        }

        if (prebidServerSettings.getPrebidServer() == PrebidServer.CUSTOM
                && TextUtils.isEmpty(prebidServerSettings.getCustomPrebidServerUrl())) {
            valid = false;
        }

        if (TextUtils.isEmpty(prebidServerSettings.getAccountId())) {
            valid = false;
        }

        if (TextUtils.isEmpty(prebidServerSettings.getConfigId())) {
            valid = false;
        }

        return valid;
    }
}
