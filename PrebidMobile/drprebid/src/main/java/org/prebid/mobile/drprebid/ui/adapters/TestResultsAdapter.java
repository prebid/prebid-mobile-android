package org.prebid.mobile.drprebid.ui.adapters;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.prebid.mobile.drprebid.model.ResultsItem;

import java.util.ArrayList;
import java.util.List;

public class TestResultsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<ResultsItem> mItems;

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
        return 0;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    private void setupItems(Context context) {

    }
}
