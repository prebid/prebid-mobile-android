/*
 *    Copyright 2018-2021 Prebid.org, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.prebid.mobile.rendering.views.browser;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import androidx.annotation.VisibleForTesting;

import org.prebid.mobile.LogUtil;
import org.prebid.mobile.core.R;
import org.prebid.mobile.rendering.utils.helpers.ExternalViewerUtils;
import org.prebid.mobile.rendering.utils.helpers.Utils;

final class BrowserControls extends TableLayout {

    private static String TAG = BrowserControls.class.getSimpleName();
    private final static int BROWSER_CONTROLS_PANEL_COLOR = Color.rgb(43, 47, 50);

    private Button closeBtn;
    private Button backBtn;
    private Button forthBtn;
    private Button refreshBtn;
    private Button openInExternalBrowserBtn;
    private LinearLayout leftPart;
    private LinearLayout rightPart;
    private Handler UIHandler;
    private BrowserControlsEventsListener browserControlsEventsListener;

    /**
     * Create browser controls
     *
     * @param context  for which controls will be created
     * @param listener responsible for callbacks
     */
    public BrowserControls(
            Context context,
            BrowserControlsEventsListener listener
    ) {
        super(context);
        init(listener);
    }

    /**
     * Update navigation controls (back and forth buttons)
     */
    public void updateNavigationButtonsState() {
        UIHandler.post(() -> {
            if (browserControlsEventsListener == null) {
                LogUtil.error(
                        TAG,
                        "updateNavigationButtonsState: Unable to update state. browserControlsEventsListener is null"
                );
                return;
            }

            if (browserControlsEventsListener.canGoBack()) {
                backBtn.setBackgroundResource(R.drawable.prebid_ic_back_active);
            } else {
                backBtn.setBackgroundResource(R.drawable.prebid_ic_back_inactive);
            }

            if (browserControlsEventsListener.canGoForward()) {
                forthBtn.setBackgroundResource(R.drawable.prebid_ic_forth_active);
            } else {
                forthBtn.setBackgroundResource(R.drawable.prebid_ic_forth_inactive);
            }
        });
    }

    private void bindEventListeners() {
        closeBtn.setOnClickListener(v -> {
            if (browserControlsEventsListener == null) {
                LogUtil.error(TAG, "Close button click failed: browserControlsEventsListener is null");
                return;
            }
            browserControlsEventsListener.closeBrowser();
        });

        backBtn.setOnClickListener(v -> {
            if (browserControlsEventsListener == null) {
                LogUtil.error(TAG, "Back button click failed: browserControlsEventsListener is null");
                return;
            }
            browserControlsEventsListener.onGoBack();
        });

        forthBtn.setOnClickListener(v -> {
            if (browserControlsEventsListener == null) {
                LogUtil.error(TAG, "Forward button click failed: browserControlsEventsListener is null");
                return;
            }
            browserControlsEventsListener.onGoForward();
        });

        refreshBtn.setOnClickListener(v -> {
            if (browserControlsEventsListener == null) {
                LogUtil.error(TAG, "Refresh button click failed: browserControlsEventsListener is null");
                return;
            }
            browserControlsEventsListener.onRelaod();
        });

        openInExternalBrowserBtn.setOnClickListener(v -> {
            String url = null;

            if (browserControlsEventsListener != null) {
                url = browserControlsEventsListener.getCurrentURL();
            }

            if (url == null) {
                LogUtil.error(TAG, "Open external link failed. url is null");
                return;
            }

            openURLInExternalBrowser(url);
        });
    }

    public void openURLInExternalBrowser(String url) {

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            ExternalViewerUtils.startActivity(getContext(), intent);
        }
        catch (Exception e) {
            LogUtil.error(TAG, "Could not handle intent: " + url + " : " + Log.getStackTraceString(e));
        }
    }

    private void setButtonDefaultSize(Button button) {
        button.setHeight((int) (50 * Utils.DENSITY));
        button.setWidth((int) (50 * Utils.DENSITY));
    }

    private void init(BrowserControlsEventsListener listener) {
        UIHandler = new Handler(Looper.getMainLooper());
        browserControlsEventsListener = listener;
        if (getContext() != null) {
            TableRow controlsSet = new TableRow(getContext());
            leftPart = new LinearLayout(getContext());
            rightPart = new LinearLayout(getContext());

            leftPart.setVisibility(GONE);
            rightPart.setGravity(Gravity.RIGHT);

            setBackgroundColor(BROWSER_CONTROLS_PANEL_COLOR);

            setAllButtons();

            bindEventListeners();

            leftPart.addView(backBtn);
            leftPart.addView(forthBtn);
            leftPart.addView(refreshBtn);
            leftPart.addView(openInExternalBrowserBtn);
            rightPart.addView(closeBtn);

            controlsSet.addView(
                    leftPart,
                    new TableRow.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, Gravity.LEFT)
            );
            controlsSet.addView(
                    rightPart,
                    new TableRow.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, Gravity.RIGHT)
            );

            BrowserControls.this.addView(controlsSet);
        }
    }

    private void setAllButtons() {
        closeBtn = new Button(getContext());
        closeBtn.setContentDescription("close");
        setButtonDefaultSize(closeBtn);
        closeBtn.setBackgroundResource(R.drawable.prebid_ic_close_browser);

        backBtn = new Button(getContext());
        backBtn.setContentDescription("back");
        setButtonDefaultSize(backBtn);
        backBtn.setBackgroundResource(R.drawable.prebid_ic_back_inactive);

        forthBtn = new Button(getContext());
        forthBtn.setContentDescription("forth");
        setButtonDefaultSize(forthBtn);
        forthBtn.setBackgroundResource(R.drawable.prebid_ic_forth_inactive);

        refreshBtn = new Button(getContext());
        refreshBtn.setContentDescription("refresh");
        setButtonDefaultSize(refreshBtn);
        refreshBtn.setBackgroundResource(R.drawable.prebid_ic_refresh);

        openInExternalBrowserBtn = new Button(getContext());
        openInExternalBrowserBtn.setContentDescription("openInExternalBrowser");
        setButtonDefaultSize(openInExternalBrowserBtn);
        openInExternalBrowserBtn.setBackgroundResource(R.drawable.prebid_ic_open_in_browser);
    }

    public void showNavigationControls() {
        leftPart.setVisibility(VISIBLE);
    }

    public void hideNavigationControls() {
        leftPart.setVisibility(GONE);
    }

    @VisibleForTesting
    public BrowserControlsEventsListener getBrowserControlsEventsListener() {
        return browserControlsEventsListener;
    }
}
