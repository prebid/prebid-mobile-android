package com.openx.apollo.views.indicator;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.openx.apollo.R;
import com.openx.apollo.models.AdConfiguration;
import com.openx.apollo.utils.helpers.ExternalViewerUtils;

public class AdIndicatorView extends RelativeLayout {

    private static final String PRIVACY_URL = "https://www.openx.com/legal/privacy-policy/";

    private ImageView mAdIconView;
    private AdConfiguration.AdUnitIdentifierType mAdUnitIdentifierType;
    protected AdIconState mSwitchStatus = AdIconState.AD_ICON_DEFAULT;
    private AdIconPosition mSwitchPosition;

    protected enum AdIconState {
        AD_ICON_DEFAULT,
        AD_ICON_PRESSED
    }

    public enum AdIconPosition {
        TOP,
        BOTTOM
    }

    public AdIndicatorView(Context context) {
        super(context);
    }

    public AdIndicatorView(Context context, AdConfiguration.AdUnitIdentifierType adUnitIdentifierType) {
        super(context);
        mAdUnitIdentifierType = adUnitIdentifierType;
        init();
    }

    private void init() {
        LayoutInflater inflater = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE));
        inflater.inflate(R.layout.lyt_ad_indicator, AdIndicatorView.this, true);
        mAdIconView = findViewById(R.id.adIndicatorIV);

        mSwitchStatus = AdIconState.AD_ICON_DEFAULT;
        if (AdConfiguration.AdUnitIdentifierType.BANNER.equals(mAdUnitIdentifierType)) {
            setPosition(AdIconPosition.TOP);
        }
        else {
            setPosition(AdIconPosition.BOTTOM);
        }
        initAdIndicatorClickListener(mAdIconView);
    }

    private void initAdIndicatorClickListener(final ImageView adIcon) {
        adIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                handleIVClick(view);
            }

            private void handleIVClick(View view) {
                switch (mSwitchStatus) {
                    case AD_ICON_DEFAULT:
                        view.setActivated(!view.isActivated());
                        mSwitchStatus = AdIconState.AD_ICON_PRESSED;
                        setupExpanded();
                        break;
                    case AD_ICON_PRESSED:
                        ExternalViewerUtils.startBrowser(getContext(), PRIVACY_URL, false, null);
                        break;
                    default:
                        mSwitchStatus = AdIconState.AD_ICON_DEFAULT;
                        break;
                }
            }
        });
    }

    public void setPosition(AdIconPosition adIconPosition) {
        if (adIconPosition == null || adIconPosition.equals(mSwitchPosition)) {
            return;
        }
        mSwitchPosition = adIconPosition;
        if (mSwitchStatus.equals(AdIconState.AD_ICON_DEFAULT)) {
            setupDefault();
        }
        else {
            setupExpanded();
        }
        setupGravity();
    }

    public AdConfiguration.AdUnitIdentifierType getAdUnitIdentifierType() {
        return mAdUnitIdentifierType;
    }

    private void setupExpanded() {
        if (mSwitchPosition.equals(AdIconPosition.TOP)) {
            mAdIconView.setImageResource(R.drawable.ic_adchoices_expanded_top_right);
        }
        else {
            mAdIconView.setImageResource(R.drawable.ic_adchoices_expanded_bottom_right);
        }
    }

    private void setupDefault() {
        if (mSwitchPosition.equals(AdIconPosition.TOP)) {
            mAdIconView.setImageResource(R.drawable.ic_adchoices_collapsed_top_right);
        }
        else {
            mAdIconView.setImageResource(R.drawable.ic_adchoices_collapsed_bottom_right);
        }
    }

    private void setupGravity() {
        if (mSwitchPosition.equals(AdIconPosition.TOP)) {
            setGravity(Gravity.RIGHT | Gravity.TOP);
        }
        else {
            setGravity(Gravity.RIGHT | Gravity.BOTTOM);
        }
    }
}
