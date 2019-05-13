package org.prebid.mobile.drprebid.ui.viewholders;

import android.content.Intent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.prebid.mobile.drprebid.R;
import org.prebid.mobile.drprebid.model.HelpScreen;
import org.prebid.mobile.drprebid.ui.activities.InfoActivity;
import org.prebid.mobile.drprebid.util.HelpScreenUtil;

public class PrebidServerSettingsViewholder extends RecyclerView.ViewHolder implements SettingsViewHolder {
    public PrebidServerSettingsViewholder(@NonNull final View itemView) {
        super(itemView);

        itemView.findViewById(R.id.button_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HelpScreen aboutScreen = HelpScreenUtil.getPrebidServerInfo(itemView.getContext());
                Intent intent = InfoActivity.newIntent(itemView.getContext(), aboutScreen.getTitle(), aboutScreen.getHtmlAsset());
                itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public void bind() {

    }
}
