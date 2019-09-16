package org.prebid.mobile.drprebid.ui.viewholders;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import org.prebid.mobile.drprebid.R;
import org.prebid.mobile.drprebid.model.HelpScreen;
import org.prebid.mobile.drprebid.ui.activities.InfoActivity;
import org.prebid.mobile.drprebid.ui.viewmodels.SdkValidationViewModel;
import org.prebid.mobile.drprebid.util.HelpScreenUtil;

public class SdkValidationViewHolder extends RecyclerView.ViewHolder implements TestResultViewHolder, LifecycleOwner {
    private ProgressBar totalProgress;
    private ImageView totalIcon;

    private ProgressBar adUnitRegisteredProgress;
    private ImageView adUnitRegisteredIcon;

    private ProgressBar sendPrebidRequestProgress;
    private ImageView sendPrebidRequestIcon;

    private ProgressBar responsePrebidReceivedProgress;
    private ImageView responsePrebidReceivedIcon;

    private ProgressBar creativeCachedProgress;
    private ImageView creativeCachedIcon;

    private ProgressBar sendAdServerRequestProgress;
    private ImageView sendAdServerRequestIcon;

    private ProgressBar responseAdServerReceivedProgress;
    private ImageView responseAdServerReceivedIcon;

    private boolean adUnitRegisteredPassed = false;
    private boolean adUnitRegisteredFinished = false;
    private boolean sentPrebidPassed = false;
    private boolean sentPrebidFinished = false;
    private boolean receivedPrebidPassed = false;
    private boolean receivedPrebidFinished = false;
    private boolean creativeCachedPassed = false;
    private boolean creativeCachedFinished = false;
    private boolean sentAdServerPassed = false;
    private boolean sentAdServerFinished = false;
    private boolean receivedAdServerPassed = false;
    private boolean receivedAdServerFinished = false;

    public SdkValidationViewHolder(@NonNull final View itemView) {
        super(itemView);

        itemView.findViewById(R.id.button_info).setOnClickListener(v -> {
            HelpScreen aboutScreen = HelpScreenUtil.getSdkTestInfo(itemView.getContext());
            Intent intent = InfoActivity.newIntent(itemView.getContext(), aboutScreen.getTitle(), aboutScreen.getHtmlAsset());
            itemView.getContext().startActivity(intent);
        });

        totalProgress = itemView.findViewById(R.id.progress_total_result);
        totalIcon = itemView.findViewById(R.id.image_total_result);

        adUnitRegisteredProgress = itemView.findViewById(R.id.progress_ad_unit_registered_result);
        adUnitRegisteredIcon = itemView.findViewById(R.id.image_ad_unit_registered_result);

        sendPrebidRequestProgress = itemView.findViewById(R.id.progress_prebid_request_sent_result);
        sendPrebidRequestIcon = itemView.findViewById(R.id.image_prebid_request_sent_result);

        responsePrebidReceivedProgress = itemView.findViewById(R.id.progress_prebid_response_received_result);
        responsePrebidReceivedIcon = itemView.findViewById(R.id.image_prebid_response_received_result);

        creativeCachedProgress = itemView.findViewById(R.id.progress_creative_cached_result);
        creativeCachedIcon = itemView.findViewById(R.id.image_creative_cached_result);

        sendAdServerRequestProgress = itemView.findViewById(R.id.progress_ad_server_request_result);
        sendAdServerRequestIcon = itemView.findViewById(R.id.image_ad_server_request_result);

        responseAdServerReceivedProgress = itemView.findViewById(R.id.progress_prebid_creative_served_result);
        responseAdServerReceivedIcon = itemView.findViewById(R.id.image_prebid_creative_served_result);

        SdkValidationViewModel viewModel = ViewModelProviders.of((AppCompatActivity) itemView.getContext()).get(SdkValidationViewModel.class);

        viewModel.getAdUnitRegistered().observe(this, registered -> {
            adUnitRegisteredProgress.setVisibility(View.GONE);
            adUnitRegisteredIcon.setVisibility(View.VISIBLE);

            if (registered) {
                adUnitRegisteredIcon.setImageResource(R.drawable.icon_success_step);
                adUnitRegisteredPassed = true;
            } else {
                adUnitRegisteredIcon.setImageResource(R.drawable.icon_fail_step);
                adUnitRegisteredPassed = false;
            }

            adUnitRegisteredFinished = true;

            updateTotal();
        });

        viewModel.getPrebidRequestSent().observe(this, sent -> {
            sendPrebidRequestProgress.setVisibility(View.GONE);
            sendPrebidRequestIcon.setVisibility(View.VISIBLE);

            if (sent) {
                sendPrebidRequestIcon.setImageResource(R.drawable.icon_success_step);
                sentPrebidPassed = true;
            } else {
                sendPrebidRequestIcon.setImageResource(R.drawable.icon_fail_step);
                sentPrebidPassed = false;
            }

            sentPrebidFinished = true;

            updateTotal();
        });

        viewModel.getPrebidResponseReceived().observe(this, received -> {
            responsePrebidReceivedProgress.setVisibility(View.GONE);
            responsePrebidReceivedIcon.setVisibility(View.VISIBLE);

            if (received) {
                responsePrebidReceivedIcon.setImageResource(R.drawable.icon_success_step);
                receivedPrebidPassed = true;
            } else {
                responsePrebidReceivedIcon.setImageResource(R.drawable.icon_fail_step);
                receivedPrebidPassed = false;
            }

            receivedPrebidFinished = true;

            updateTotal();
        });

        viewModel.getCreativeContentCached().observe(this, cached -> {
            creativeCachedProgress.setVisibility(View.GONE);
            creativeCachedIcon.setVisibility(View.VISIBLE);

            if (cached) {
                creativeCachedIcon.setImageResource(R.drawable.icon_success_step);
                creativeCachedPassed = true;
            } else {
                creativeCachedIcon.setImageResource(R.drawable.icon_fail_step);
                creativeCachedPassed = false;
            }

            creativeCachedFinished = true;

            updateTotal();
        });

        viewModel.getAdServerRequestSent().observe(this, sent -> {
            sendAdServerRequestProgress.setVisibility(View.GONE);
            sendAdServerRequestIcon.setVisibility(View.VISIBLE);

            if (sent) {
                sendAdServerRequestIcon.setImageResource(R.drawable.icon_success_step);
                sentAdServerPassed = true;
            } else {
                sendAdServerRequestIcon.setImageResource(R.drawable.icon_fail_step);
                sentAdServerPassed = false;
            }

            sentAdServerFinished = true;

            updateTotal();
        });

        viewModel.getCreativeServed().observe(this, served -> {
            responseAdServerReceivedProgress.setVisibility(View.GONE);
            responseAdServerReceivedIcon.setVisibility(View.VISIBLE);

            if (served) {
                responseAdServerReceivedIcon.setImageResource(R.drawable.icon_success_step);
                receivedAdServerPassed = true;
            } else {
                responseAdServerReceivedIcon.setImageResource(R.drawable.icon_fail_step);
                receivedAdServerPassed = false;
            }

            receivedAdServerFinished = true;

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
        if (adUnitRegisteredFinished
                && sentPrebidFinished
                && receivedPrebidFinished
                && creativeCachedFinished
                && sentAdServerFinished
                && receivedAdServerFinished) {
            totalProgress.setVisibility(View.GONE);
            totalIcon.setVisibility(View.VISIBLE);
            if (adUnitRegisteredPassed
                    && sentPrebidPassed
                    && receivedPrebidPassed
                    && creativeCachedPassed
                    && sentAdServerPassed
                    && receivedAdServerPassed) {
                totalIcon.setImageResource(R.drawable.icon_success_main);
            } else {
                totalIcon.setImageResource(R.drawable.icon_fail_main);
            }
        }
    }
}
