package org.prebid.mobile.rendering.views.browser;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import androidx.annotation.VisibleForTesting;

import org.prebid.mobile.rendering.R;
import org.prebid.mobile.rendering.utils.helpers.Utils;
import org.prebid.mobile.rendering.utils.logger.OXLog;

final class BrowserControls extends TableLayout {
    private static String TAG = BrowserControls.class.getSimpleName();
    private final static int BROWSER_CONTROLS_PANEL_COLOR = Color.rgb(43, 47, 50);

    private Button mCloseBtn;
    private Button mBackBtn;
    private Button mForthBtn;
    private Button mRefreshBtn;
    private Button mOpenInExternalBrowserBtn;
    private LinearLayout mLeftPart;
    private LinearLayout mRightPart;
    private Handler mUIHandler;
    private BrowserControlsEventsListener mBrowserControlsEventsListener;

    /**
     * Create browser controls
     *
     * @param context  for which controls will be created
     * @param listener responsible for callbacks
     */
    public BrowserControls(Context context, BrowserControlsEventsListener listener) {
        super(context);
        init(listener);
    }

    /**
     * Update navigation controls (back and forth buttons)
     */
    public void updateNavigationButtonsState() {
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mBrowserControlsEventsListener == null) {
                    OXLog.error(TAG, "updateNavigationButtonsState: Unable to update state. mBrowserControlsEventsListener is null");
                    return;
                }

                if (mBrowserControlsEventsListener.canGoBack()) {
                    mBackBtn.setBackgroundResource(R.drawable.openx_res_back_active);
                }
                else {
                    mBackBtn.setBackgroundResource(R.drawable.openx_res_back_inactive);
                }

                if (mBrowserControlsEventsListener.canGoForward()) {
                    mForthBtn.setBackgroundResource(R.drawable.openx_res_forth_active);
                }
                else {
                    mForthBtn.setBackgroundResource(R.drawable.openx_res_forth_inactive);
                }
            }
        });
    }

    private void bindEventListeners() {
        mCloseBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBrowserControlsEventsListener == null) {
                    OXLog.error(TAG, "Close button click failed: mBrowserControlsEventsListener is null");
                    return;
                }
                mBrowserControlsEventsListener.closeBrowser();
            }
        });

        mBackBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBrowserControlsEventsListener == null) {
                    OXLog.error(TAG, "Back button click failed: mBrowserControlsEventsListener is null");
                    return;
                }
                mBrowserControlsEventsListener.onGoBack();
            }
        });

        mForthBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBrowserControlsEventsListener == null) {
                    OXLog.error(TAG, "Forward button click failed: mBrowserControlsEventsListener is null");
                    return;
                }
                mBrowserControlsEventsListener.onGoForward();
            }
        });

        mRefreshBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBrowserControlsEventsListener == null) {
                    OXLog.error(TAG, "Refresh button click failed: mBrowserControlsEventsListener is null");
                    return;
                }
                mBrowserControlsEventsListener.onRelaod();
            }
        });

        mOpenInExternalBrowserBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = null;

                if (mBrowserControlsEventsListener != null) {
                    url = mBrowserControlsEventsListener.getCurrentURL();
                }

                if (url == null) {
                    OXLog.error(TAG, "Open external link failed. url is null");
                    return;
                }

                openURLInExternalBrowser(url);
            }
        });
    }

    public void openURLInExternalBrowser(String url) {

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            getContext().startActivity(intent);
        }
        catch (Exception e) {
            OXLog.error(TAG, "Could not handle intent: " + url + " : " + Log.getStackTraceString(e));
        }
    }

    private void setButtonDefaultSize(Button button) {
        button.setHeight((int) (50 * Utils.DENSITY));
        button.setWidth((int) (50 * Utils.DENSITY));
    }

    private void init(BrowserControlsEventsListener listener) {
        mUIHandler = new Handler();
        mBrowserControlsEventsListener = listener;
        if (getContext() != null) {
            TableRow controlsSet = new TableRow(getContext());
            mLeftPart = new LinearLayout(getContext());
            mRightPart = new LinearLayout(getContext());

            mLeftPart.setVisibility(GONE);
            mRightPart.setGravity(Gravity.RIGHT);

            setBackgroundColor(BROWSER_CONTROLS_PANEL_COLOR);

            setAllButtons();

            bindEventListeners();

            mLeftPart.addView(mBackBtn);
            mLeftPart.addView(mForthBtn);
            mLeftPart.addView(mRefreshBtn);
            mLeftPart.addView(mOpenInExternalBrowserBtn);
            mRightPart.addView(mCloseBtn);

            controlsSet.addView(mLeftPart, new TableRow.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, Gravity.LEFT));
            controlsSet.addView(mRightPart, new TableRow.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, Gravity.RIGHT));

            BrowserControls.this.addView(controlsSet);
        }
    }

    private void setAllButtons() {
        mCloseBtn = new Button(getContext());
        mCloseBtn.setContentDescription("close");
        setButtonDefaultSize(mCloseBtn);
        mCloseBtn.setBackgroundResource(R.drawable.openx_res_close_browser);

        mBackBtn = new Button(getContext());
        mBackBtn.setContentDescription("back");
        setButtonDefaultSize(mBackBtn);
        mBackBtn.setBackgroundResource(R.drawable.openx_res_back_inactive);

        mForthBtn = new Button(getContext());
        mForthBtn.setContentDescription("forth");
        setButtonDefaultSize(mForthBtn);
        mForthBtn.setBackgroundResource(R.drawable.openx_res_forth_inactive);

        mRefreshBtn = new Button(getContext());
        mRefreshBtn.setContentDescription("refresh");
        setButtonDefaultSize(mRefreshBtn);
        mRefreshBtn.setBackgroundResource(R.drawable.openx_res_refresh);

        mOpenInExternalBrowserBtn = new Button(getContext());
        mOpenInExternalBrowserBtn.setContentDescription("openInExternalBrowser");
        setButtonDefaultSize(mOpenInExternalBrowserBtn);
        mOpenInExternalBrowserBtn.setBackgroundResource(R.drawable.openx_res_open_in_browser);
    }

    public void showNavigationControls() {
        mLeftPart.setVisibility(VISIBLE);
    }

    public void hideNavigationControls() {
        mLeftPart.setVisibility(GONE);
    }

    @VisibleForTesting
    public BrowserControlsEventsListener getBrowserControlsEventsListener() {
        return mBrowserControlsEventsListener;
    }
}
