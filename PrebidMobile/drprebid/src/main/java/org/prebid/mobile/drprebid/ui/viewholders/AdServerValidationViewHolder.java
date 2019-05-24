package org.prebid.mobile.drprebid.ui.viewholders;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

public class AdServerValidationViewHolder extends RecyclerView.ViewHolder implements TestResultViewHolder, LifecycleOwner {

    public AdServerValidationViewHolder(@NonNull final View itemView) {
        super(itemView);
    }

    @Override
    public void bind() {

    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return null;
    }
}
