package org.prebid.mobile.drprebid.ui.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.prebid.mobile.drprebid.R;
import org.prebid.mobile.drprebid.model.AdServerValidationResult;
import org.prebid.mobile.drprebid.model.PrebidServerValidationResult;
import org.prebid.mobile.drprebid.model.ResultItem;
import org.prebid.mobile.drprebid.model.SdkValidationResult;
import org.prebid.mobile.drprebid.ui.viewholders.AdServerValidationViewHolder;
import org.prebid.mobile.drprebid.ui.viewholders.DividerViewHolder;
import org.prebid.mobile.drprebid.ui.viewholders.PrebidServerValidationViewHolder;
import org.prebid.mobile.drprebid.ui.viewholders.SdkValidationViewHolder;
import org.prebid.mobile.drprebid.ui.viewholders.TestResultViewHolder;

import java.util.ArrayList;
import java.util.List;

public class TestResultsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_DIVIDER = 0;
    private static final int VIEW_TYPE_AD_SERVER_RESULTS = 1;
    private static final int VIEW_TYPE_PREBID_SERVER_RESULTS = 2;
    private static final int VIEW_TYPE_SDK_RESULTS = 3;

    private final List<ResultItem> mItems;

    public TestResultsAdapter() {
        mItems = new ArrayList<>();

        setupItems();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_AD_SERVER_RESULTS:
                return new AdServerValidationViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_ad_server_results, parent, false));
            case VIEW_TYPE_PREBID_SERVER_RESULTS:
                return new PrebidServerValidationViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_prebid_server_results, parent, false));
            case VIEW_TYPE_SDK_RESULTS:
                return new SdkValidationViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_sdk_results, parent, false));
            default:
                return new DividerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_divider, parent, false));
        }
    }

    @Override
    public int getItemViewType(int position) {
        ResultItem item = mItems.get(position);
        if (item instanceof AdServerValidationResult) {
            return VIEW_TYPE_AD_SERVER_RESULTS;
        } else if (item instanceof PrebidServerValidationResult) {
            return VIEW_TYPE_PREBID_SERVER_RESULTS;
        } else if (item instanceof SdkValidationResult) {
            return VIEW_TYPE_SDK_RESULTS;
        } else {
            return VIEW_TYPE_DIVIDER;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TestResultViewHolder viewHolder = (TestResultViewHolder) holder;
        viewHolder.bind();
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    private void setupItems() {
        mItems.add(new AdServerValidationResult());
        mItems.add(new PrebidServerValidationResult());
        mItems.add(new SdkValidationResult());
    }
}
