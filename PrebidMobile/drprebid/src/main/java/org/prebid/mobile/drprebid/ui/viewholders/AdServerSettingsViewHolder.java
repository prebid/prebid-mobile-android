package org.prebid.mobile.drprebid.ui.viewholders;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import org.prebid.mobile.drprebid.R;
import org.prebid.mobile.drprebid.model.HelpScreen;
import org.prebid.mobile.drprebid.ui.activities.InfoActivity;
import org.prebid.mobile.drprebid.ui.dialog.InputDialog;
import org.prebid.mobile.drprebid.util.HelpScreenUtil;

public class AdServerSettingsViewHolder extends RecyclerView.ViewHolder implements SettingsViewHolder {
    private TextView mAdUnitIdView;

    public AdServerSettingsViewHolder(@NonNull final View itemView) {
        super(itemView);

        itemView.findViewById(R.id.button_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HelpScreen aboutScreen = HelpScreenUtil.getAdServerInfo(itemView.getContext());
                Intent intent = InfoActivity.newIntent(itemView.getContext(), aboutScreen.getTitle(), aboutScreen.getHtmlAsset());
                itemView.getContext().startActivity(intent);
            }
        });

        mAdUnitIdView = itemView.findViewById(R.id.view_ad_unit_id);
        mAdUnitIdView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = ((AppCompatActivity) itemView.getContext()).getSupportFragmentManager();
                InputDialog dialog = InputDialog.newInstance(itemView.getContext().getString(R.string.ad_unit_id));
                dialog.show(fragmentManager, InputDialog.TAG);
            }
        });
    }

    @Override
    public void bind() {

    }
}
