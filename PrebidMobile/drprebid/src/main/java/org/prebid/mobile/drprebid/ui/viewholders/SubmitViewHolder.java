package org.prebid.mobile.drprebid.ui.viewholders;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.prebid.mobile.drprebid.R;

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
        
    }
}
