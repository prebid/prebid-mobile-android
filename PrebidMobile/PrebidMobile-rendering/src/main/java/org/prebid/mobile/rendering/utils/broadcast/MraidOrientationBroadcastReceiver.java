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

package org.prebid.mobile.rendering.utils.broadcast;

import android.os.Build;

import org.prebid.mobile.rendering.utils.logger.OXLog;
import org.prebid.mobile.rendering.views.webview.mraid.BaseJSInterface;
import org.prebid.mobile.rendering.views.webview.mraid.JSInterface;

import java.lang.ref.WeakReference;

public class MraidOrientationBroadcastReceiver extends OrientationBroadcastReceiver {
    private static final String TAG = MraidOrientationBroadcastReceiver.class.getSimpleName();

    private final WeakReference<BaseJSInterface> mBaseJSInterfaceWeakReference;

    private String mMraidAction;
    private String mState;

    public MraidOrientationBroadcastReceiver(BaseJSInterface baseJSInterface) {
        mBaseJSInterfaceWeakReference = new WeakReference<>(baseJSInterface);
    }

    @Override
    public void handleOrientationChange(int currentRotation) {
        super.handleOrientationChange(currentRotation);
        BaseJSInterface baseJSInterface = mBaseJSInterfaceWeakReference.get();
        if (baseJSInterface == null) {
            OXLog.debug(TAG, "handleOrientationChange failure. BaseJsInterface is null");
            return;
        }

        if (shouldHandleClose()) {
            OXLog.debug(TAG, "Call 'close' action for MRAID Resize after changing rotation for API 19.");
            baseJSInterface.close();
        }
    }

    public void setState(String state) {
        mState = state;
    }

    public void setMraidAction(String action) {
        mMraidAction = action;
    }

    private boolean shouldHandleClose() {
        return mState != null
               && !JSInterface.STATE_DEFAULT.equals(mState)
               && Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT
               && JSInterface.ACTION_RESIZE.equals(mMraidAction);
    }
}
