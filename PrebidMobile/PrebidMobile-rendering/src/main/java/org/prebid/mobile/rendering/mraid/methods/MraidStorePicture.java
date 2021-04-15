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

package org.prebid.mobile.rendering.mraid.methods;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.prebid.mobile.rendering.sdk.ManagersResolver;
import org.prebid.mobile.rendering.sdk.deviceData.managers.DeviceInfoManager;
import org.prebid.mobile.rendering.utils.logger.OXLog;
import org.prebid.mobile.rendering.views.webview.WebViewBase;
import org.prebid.mobile.rendering.views.webview.mraid.BaseJSInterface;
import org.prebid.mobile.rendering.views.webview.mraid.JSInterface;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MraidStorePicture {
    private static final String TAG = MraidStorePicture.class.getSimpleName();
    private WebViewBase mAdBaseView;
    private BaseJSInterface mJsi;

    private String mUrlToStore = null;

    private Context mContext;

    public MraidStorePicture(Context context, BaseJSInterface jsInterface, WebViewBase adBaseView) {
        mContext = context;
        mAdBaseView = adBaseView;
        mJsi = jsInterface;
    }

    public void storePicture(String url) {
        if (url != null && !url.equals("")) {
            mUrlToStore = url;

            if (mAdBaseView != null && mContext != null) {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("Save image?");

                    builder.setMessage("Would you like to save this image? " + mUrlToStore);

                    builder.setPositiveButton(android.R.string.yes, (dialogInterface, i) -> storePicture());

                    builder.setNegativeButton(android.R.string.no, null);

                    AlertDialog dialog = builder.create();
                    //android.view.WindowManager$BadTokenException: Unable to add window -- token android.os.BinderProxy@4f935b0 is not valid; is your activity running?
                    if (mContext instanceof Activity && !((Activity) mContext).isFinishing()) {
                        //show dialog
                        dialog.show();
                    }
                    else {
                        OXLog.error(TAG, "Context is not activity or activity is finishing, can not show expand dialog");
                    }
                });
            }
        }
    }

    private void storePicture() {
        new Thread(() -> {
            try {
                DeviceInfoManager devicePolicyManager = ManagersResolver.getInstance().getDeviceManager();
                if (!devicePolicyManager.isPermissionGranted(WRITE_EXTERNAL_STORAGE)) {
                    mJsi.onError("store_picture", JSInterface.ACTION_STORE_PICTURE);
                }
                else {
                    devicePolicyManager.storePicture(mUrlToStore);
                }
            }
            catch (Exception e) {
                //send a mraid error back to the ad
                mJsi.onError("Failed to store picture", JSInterface.ACTION_STORE_PICTURE);
                OXLog.error(TAG, "Failed to store picture: " + Log.getStackTraceString(e));
            }
        }).start();
    }
}
