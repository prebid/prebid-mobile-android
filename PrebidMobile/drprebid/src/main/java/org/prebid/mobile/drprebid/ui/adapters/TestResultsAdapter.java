package org.prebid.mobile.drprebid.ui.adapters;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.prebid.mobile.drprebid.model.AdServerValidationResult;
import org.prebid.mobile.drprebid.model.PrebidServerValidationResult;
import org.prebid.mobile.drprebid.model.ResultItem;
import org.prebid.mobile.drprebid.model.SdkValidationResult;
import org.prebid.mobile.drprebid.ui.viewholders.TestResultViewHolder;

import java.util.ArrayList;
import java.util.List;

public class TestResultsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_DIVIDER = 0;
    private static final int VIEW_TYPE_AD_SERVER_RESULTS = 1;
    private static final int VIEW_TYPE_PREBID_SERVER_RESULTS = 2;
    private static final int VIEW_TYPE_SDK_RESULTS = 3;

    private final List<ResultItem> mItems;

    public TestResultsAdapter(Context context) {
        mItems = new ArrayList<>();

        setupItems(context);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
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

    private void setupItems(Context context) {

    }
}
