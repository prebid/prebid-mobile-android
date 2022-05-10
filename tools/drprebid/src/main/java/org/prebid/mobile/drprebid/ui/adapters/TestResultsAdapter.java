package org.prebid.mobile.drprebid.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.prebid.mobile.drprebid.R;
import org.prebid.mobile.drprebid.model.AdServerValidationResult;
import org.prebid.mobile.drprebid.model.PrebidServerValidationResult;
import org.prebid.mobile.drprebid.model.ResultItem;
import org.prebid.mobile.drprebid.model.SdkValidationResult;
import org.prebid.mobile.drprebid.ui.viewholders.*;

import java.util.ArrayList;
import java.util.List;

public class TestResultsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_DIVIDER = 0;
    private static final int VIEW_TYPE_AD_SERVER_RESULTS = 1;
    private static final int VIEW_TYPE_PREBID_SERVER_RESULTS = 2;
    private static final int VIEW_TYPE_SDK_RESULTS = 3;

    private final List<ResultItem> items;

    public TestResultsAdapter() {
        items = new ArrayList<>();

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
        ResultItem item = items.get(position);
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
        return items.size();
    }

    private void setupItems() {
        items.add(new AdServerValidationResult());
        items.add(new PrebidServerValidationResult());
        items.add(new SdkValidationResult());
    }
}
