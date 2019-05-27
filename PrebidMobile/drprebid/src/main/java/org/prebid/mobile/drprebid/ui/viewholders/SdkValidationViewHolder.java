package org.prebid.mobile.drprebid.ui.viewholders;

import android.content.Intent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import org.prebid.mobile.drprebid.R;
import org.prebid.mobile.drprebid.model.HelpScreen;
import org.prebid.mobile.drprebid.ui.activities.InfoActivity;
import org.prebid.mobile.drprebid.util.HelpScreenUtil;

public class SdkValidationViewHolder extends RecyclerView.ViewHolder implements TestResultViewHolder, LifecycleOwner {

    public SdkValidationViewHolder(@NonNull final View itemView) {
        super(itemView);

        itemView.findViewById(R.id.button_info).setOnClickListener(v -> {
            HelpScreen aboutScreen = HelpScreenUtil.getSdkTestInfo(itemView.getContext());
            Intent intent = InfoActivity.newIntent(itemView.getContext(), aboutScreen.getTitle(), aboutScreen.getHtmlAsset());
            itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public void bind() {

    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return ((AppCompatActivity) itemView.getContext()).getLifecycle();
    }
}
