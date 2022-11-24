package org.prebid.mobile.javademo.testcases;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.prebid.mobile.javademo.databinding.ListItemAdTypeBinding;

import java.util.ArrayList;
import java.util.List;

public class TestCaseAdapter extends RecyclerView.Adapter<TestCaseAdapter.AdTypeViewHolder> {

    private List<TestCase> list = new ArrayList<>();
    private final OnItemClick onItemClick;

    public TestCaseAdapter(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }

    @NonNull
    @Override
    public AdTypeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListItemAdTypeBinding binding = ListItemAdTypeBinding.inflate(
            LayoutInflater.from(parent.getContext()),
            parent,
            false
        );

        AdTypeViewHolder viewHolder = new AdTypeViewHolder(binding);
        viewHolder.setOnItemClickedListener(onItemClick);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull AdTypeViewHolder holder, int position) {
        holder.bind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setList(List<TestCase> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    static class AdTypeViewHolder extends RecyclerView.ViewHolder {

        private final ListItemAdTypeBinding binding;
        private TestCase testCase;

        AdTypeViewHolder(ListItemAdTypeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(TestCase testCase) {
            this.testCase = testCase;
            binding.tvName.setText(itemView.getContext().getString(testCase.getTitleStringRes()));
        }

        void setOnItemClickedListener(OnItemClick onItemClickListener) {
            binding.getRoot().setOnClickListener(v -> {
                onItemClickListener.click(testCase);
            });
        }

    }

    public interface OnItemClick {

        void click(TestCase testCase);

    }

}
