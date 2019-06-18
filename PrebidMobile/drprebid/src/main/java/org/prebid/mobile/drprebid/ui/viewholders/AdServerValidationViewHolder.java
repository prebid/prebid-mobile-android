package org.prebid.mobile.drprebid.ui.viewholders;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import org.prebid.mobile.drprebid.R;
import org.prebid.mobile.drprebid.model.HelpScreen;
import org.prebid.mobile.drprebid.ui.activities.InfoActivity;
import org.prebid.mobile.drprebid.ui.viewmodels.AdServerValidationViewModel;
import org.prebid.mobile.drprebid.util.HelpScreenUtil;

import java.util.Locale;

public class AdServerValidationViewHolder extends RecyclerView.ViewHolder implements TestResultViewHolder, LifecycleOwner {
    private ProgressBar totalProgress;
    private ImageView totalIcon;

    private ProgressBar sendRequestProgress;
    private ImageView sendRequestIcon;

    private ProgressBar responseReceivedProgress;
    private ImageView responseReceivedIcon;

    private boolean sentPassed = false;
    private boolean sentFinished = false;
    private boolean receivedPassed = false;
    private boolean receivedFinished = false;

    public AdServerValidationViewHolder(@NonNull final View itemView) {
        super(itemView);

        itemView.findViewById(R.id.button_info).setOnClickListener(v -> {
            HelpScreen aboutScreen = HelpScreenUtil.getAdServerTestInfo(itemView.getContext());
            Intent intent = InfoActivity.newIntent(itemView.getContext(), aboutScreen.getTitle(), aboutScreen.getHtmlAsset());
            itemView.getContext().startActivity(intent);
        });

        totalProgress = itemView.findViewById(R.id.progress_total_result);
        totalIcon = itemView.findViewById(R.id.image_total_result);

        sendRequestProgress = itemView.findViewById(R.id.progress_ad_server_request_result);
        sendRequestIcon = itemView.findViewById(R.id.image_ad_server_request_result);

        responseReceivedProgress = itemView.findViewById(R.id.progress_prebid_creative_served_result);
        responseReceivedIcon = itemView.findViewById(R.id.image_prebid_creative_served_result);

        AdServerValidationViewModel mViewModel = ViewModelProviders.of((AppCompatActivity) itemView.getContext()).get(AdServerValidationViewModel.class);

        mViewModel.getRequestSent().observe(this, sent -> {
            sendRequestProgress.setVisibility(View.GONE);
            sendRequestIcon.setVisibility(View.VISIBLE);

            if (sent) {
                sendRequestIcon.setImageResource(R.drawable.icon_success_step);
                sentPassed = true;
            } else {
                sendRequestIcon.setImageResource(R.drawable.icon_fail_step);
                sentPassed = false;
            }

            sentFinished = true;

            updateTotal();
        });

        mViewModel.getCreativeServed().observe(this, served -> {
            responseReceivedProgress.setVisibility(View.GONE);
            responseReceivedIcon.setVisibility(View.VISIBLE);

            if (served) {
                responseReceivedIcon.setImageResource(R.drawable.icon_success_step);
                receivedPassed = true;
            } else {
                responseReceivedIcon.setImageResource(R.drawable.icon_fail_step);
                receivedPassed = false;
            }

            receivedFinished = true;

            updateTotal();
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

    private void updateTotal() {
        if (sentFinished && receivedFinished) {
            totalProgress.setVisibility(View.GONE);
            totalIcon.setVisibility(View.VISIBLE);
            if (sentPassed && receivedPassed) {
                totalIcon.setImageResource(R.drawable.icon_success_main);
            } else {
                totalIcon.setImageResource(R.drawable.icon_fail_main);
            }
        }
    }
}
