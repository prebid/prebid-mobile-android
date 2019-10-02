package org.prebid.mobile.drprebid.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.prebid.mobile.drprebid.R;
import org.prebid.mobile.drprebid.managers.SettingsManager;
import org.prebid.mobile.drprebid.model.AdServerSettings;
import org.prebid.mobile.drprebid.model.GeneralSettings;
import org.prebid.mobile.drprebid.model.PrebidServerSettings;
import org.prebid.mobile.drprebid.model.SettingsItem;
import org.prebid.mobile.drprebid.model.SubmitSettings;
import org.prebid.mobile.drprebid.ui.viewholders.AdServerSettingsViewHolder;
import org.prebid.mobile.drprebid.ui.viewholders.DividerViewHolder;
import org.prebid.mobile.drprebid.ui.viewholders.GeneralSettingsViewHolder;
import org.prebid.mobile.drprebid.ui.viewholders.PrebidServerSettingsViewholder;
import org.prebid.mobile.drprebid.ui.viewholders.SettingsViewHolder;
import org.prebid.mobile.drprebid.ui.viewholders.SubmitViewHolder;

import java.util.ArrayList;
import java.util.List;

public class SettingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_DIVIDER = 0;
    private static final int VIEW_TYPE_GENERAL_SETTINGS = 1;
    private static final int VIEW_TYPE_AD_SERVER_SETTINGS = 2;
    private static final int VIEW_TYPE_PREBID_SERVER_SETTINGS = 3;
    private static final int VIEW_TYPE_SUBMIT = 4;

    private final List<SettingsItem> mItems;

    public SettingsAdapter(Context context) {
        mItems = new ArrayList<>();

        setupSettings(context);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_GENERAL_SETTINGS:
                return new GeneralSettingsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_general_settings, parent, false));
            case VIEW_TYPE_AD_SERVER_SETTINGS:
                return new AdServerSettingsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_ad_server_settings, parent, false));
            case VIEW_TYPE_PREBID_SERVER_SETTINGS:
                return new PrebidServerSettingsViewholder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_prebid_server_settings, parent, false));
            case VIEW_TYPE_SUBMIT:
                return new SubmitViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_submit, parent, false));
            default:
                return new DividerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_divider, parent, false));
        }
    }

    @Override
    public int getItemViewType(int position) {
        SettingsItem item = mItems.get(position);
        if (item instanceof GeneralSettings) {
            return VIEW_TYPE_GENERAL_SETTINGS;
        } else if (item instanceof AdServerSettings) {
            return VIEW_TYPE_AD_SERVER_SETTINGS;
        } else if (item instanceof PrebidServerSettings) {
            return VIEW_TYPE_PREBID_SERVER_SETTINGS;
        } else if (item instanceof SubmitSettings) {
            return VIEW_TYPE_SUBMIT;
        } else {
            return VIEW_TYPE_DIVIDER;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SettingsViewHolder viewHolder = (SettingsViewHolder) holder;
        viewHolder.bind();
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    private void setupSettings(Context context) {
        mItems.add(SettingsManager.getInstance(context).getGeneralSettings());
        mItems.add(SettingsManager.getInstance(context).getAdServerSettings());
        mItems.add(SettingsManager.getInstance(context).getPrebidServerSettings());
        mItems.add(new SubmitSettings());
    }
}
