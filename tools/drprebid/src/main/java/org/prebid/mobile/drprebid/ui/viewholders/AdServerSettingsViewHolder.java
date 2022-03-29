package org.prebid.mobile.drprebid.ui.viewholders;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProviders;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.prebid.mobile.drprebid.Constants;
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

    private RadioGroup adServerGroup;
    private TextView adUnitIdView;
    private TextView bidPriceView;
    private SettingsViewModel settingsViewModel;

    public AdServerSettingsViewHolder(@NonNull final View itemView) {
        super(itemView);

        itemView.findViewById(R.id.button_info).setOnClickListener(v -> {
            HelpScreen aboutScreen = HelpScreenUtil.getAdServerInfo(itemView.getContext());
            Intent intent = InfoActivity.newIntent(
                    itemView.getContext(),
                    aboutScreen.getTitle(),
                    aboutScreen.getHtmlAsset()
            );
            itemView.getContext().startActivity(intent);
        });

        bidPriceView = itemView.findViewById(R.id.view_bid_price);
        bidPriceView.setOnClickListener(v -> {
            FragmentManager fragmentManager = ((AppCompatActivity) itemView.getContext()).getSupportFragmentManager();
            InputDialog dialog = InputDialog.newInstance(itemView.getContext().getString(R.string.bid_price),
                    Constants.Params.TYPE_BID_PRICE,
                    Constants.Params.FORMAT_FLOAT,
                    false
            );
            dialog.show(fragmentManager, InputDialog.TAG);
        });

        adUnitIdView = itemView.findViewById(R.id.view_ad_unit_id);
        adUnitIdView.setOnClickListener(v -> {
            FragmentManager fragmentManager = ((AppCompatActivity) itemView.getContext()).getSupportFragmentManager();
            InputDialog dialog = InputDialog.newInstance(itemView.getContext().getString(R.string.ad_unit_id),
                    Constants.Params.TYPE_AD_UNIT_ID,
                    Constants.Params.FORMAT_TEXT,
                    true
            );
            dialog.show(fragmentManager, InputDialog.TAG);
        });

        adServerGroup = itemView.findViewById(R.id.group_ad_server);
        adServerGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.radio_dfp:
                    SettingsManager.getInstance(itemView.getContext()).setAdServer(AdServer.GOOGLE_AD_MANAGER);
                    break;
                case R.id.radio_mopub:
                    SettingsManager.getInstance(itemView.getContext()).setAdServer(AdServer.MOPUB);
                    break;
            }
        });

        settingsViewModel = ViewModelProviders.of((AppCompatActivity) itemView.getContext())
                                              .get(SettingsViewModel.class);

        settingsViewModel.getBidPrice().observe(this, bidPrice -> {
            if (bidPrice != null) {
                bidPriceView.setText(String.format(Locale.ENGLISH, "$ %.2f", bidPrice));
            }
        });

        settingsViewModel.getAdUnitId().observe(this, adUnitId -> {
            if (!TextUtils.isEmpty(adUnitId)) {
                adUnitIdView.setText(adUnitId);
            } else {
                adUnitIdView.setText(R.string.click_to_choose);
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
                adServerGroup.check(R.id.radio_dfp);
                break;
            case MOPUB:
                adServerGroup.check(R.id.radio_mopub);
                break;
        }

        bidPriceView.setText(String.format(Locale.ENGLISH, "$ %.2f", settings.getBidPrice()));

        if (!TextUtils.isEmpty(settings.getAdUnitId())) {
            adUnitIdView.setText(settings.getAdUnitId());
        }
    }
}
