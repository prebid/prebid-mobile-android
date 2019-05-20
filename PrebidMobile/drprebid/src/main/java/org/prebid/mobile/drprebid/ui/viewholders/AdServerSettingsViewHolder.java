package org.prebid.mobile.drprebid.ui.viewholders;

import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
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
import org.prebid.mobile.drprebid.model.AdServer;
import org.prebid.mobile.drprebid.model.AdServerSettings;
import org.prebid.mobile.drprebid.model.HelpScreen;
import org.prebid.mobile.drprebid.ui.activities.InfoActivity;
import org.prebid.mobile.drprebid.ui.dialog.InputDialog;
import org.prebid.mobile.drprebid.ui.viewmodels.SettingsViewModel;
import org.prebid.mobile.drprebid.util.HelpScreenUtil;

import java.util.Locale;

public class AdServerSettingsViewHolder extends RecyclerView.ViewHolder implements SettingsViewHolder, LifecycleOwner {
    private RadioGroup mAdServerGroup;
    private TextView mAdUnitIdView;
    private EditText mBidPriceView;
    private SettingsViewModel mSettingsViewModel;

    public AdServerSettingsViewHolder(@NonNull final View itemView) {
        super(itemView);

        itemView.findViewById(R.id.button_info).setOnClickListener(v -> {
            HelpScreen aboutScreen = HelpScreenUtil.getAdServerInfo(itemView.getContext());
            Intent intent = InfoActivity.newIntent(itemView.getContext(), aboutScreen.getTitle(), aboutScreen.getHtmlAsset());
            itemView.getContext().startActivity(intent);
        });

        mBidPriceView = itemView.findViewById(R.id.field_bid_price);
        mAdUnitIdView = itemView.findViewById(R.id.view_ad_unit_id);
        mAdUnitIdView.setOnClickListener(v -> {
            FragmentManager fragmentManager = ((AppCompatActivity) itemView.getContext()).getSupportFragmentManager();
            InputDialog dialog = InputDialog.newInstance(itemView.getContext().getString(R.string.ad_unit_id));
            dialog.show(fragmentManager, InputDialog.TAG);
        });

        mAdServerGroup = itemView.findViewById(R.id.group_ad_server);
        mAdServerGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.radio_dfp:
                    SettingsManager.getInstance(itemView.getContext()).setAdServer(AdServer.GOOGLE_AD_MANAGER);
                    break;
                case R.id.radio_mopub:
                    SettingsManager.getInstance(itemView.getContext()).setAdServer(AdServer.MOPUB);
                    break;
            }
        });

        mSettingsViewModel = ViewModelProviders.of((AppCompatActivity) itemView.getContext()).get(SettingsViewModel.class);

        mSettingsViewModel.getBidPrice().observe(this, bidPrice -> {
            if (bidPrice != null) {
                mBidPriceView.setText(String.format(Locale.ENGLISH, "$ %.2f", bidPrice));
            }
        });

        mSettingsViewModel.getAdUnitId().observe(this, adUnitId -> {
            if (adUnitId != null) {
                mAdUnitIdView.setText(adUnitId);
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
        AdServerSettings settings = SettingsManager.getInstance(itemView.getContext()).getAdServerSettings();

        switch (settings.getAdServer()) {
            case GOOGLE_AD_MANAGER:
                mAdServerGroup.check(R.id.radio_dfp);
                break;
            case MOPUB:
                mAdServerGroup.check(R.id.radio_mopub);
                break;
        }

        mBidPriceView.setText(String.format(Locale.ENGLISH, "$ %.2f", settings.getBidPrice()));
        mAdUnitIdView.setText(settings.getAdUnitId());
    }
}
