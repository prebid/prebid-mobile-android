package org.prebid.mobile.drprebid.ui.viewholders;

import android.content.Intent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.prebid.mobile.drprebid.R;
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
        Intent intent = new Intent(itemView.getContext(), TestResultsActivity.class);
        itemView.getContext().startActivity(intent);
    }
}
