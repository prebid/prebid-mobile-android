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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.WindowManager;
import org.prebid.mobile.rendering.utils.logger.LogUtil;

public class OrientationBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = OrientationBroadcastReceiver.class.getSimpleName();

    private Context mApplicationContext;

    // -1 until this gets set at least once
    private int mLastRotation = -1;
    private boolean orientationChanged;

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtil.debug(TAG, "onReceive");
        if (Intent.ACTION_CONFIGURATION_CHANGED.equals(intent.getAction())) {
            int orientation = getDisplayRotation();
            if (orientation != mLastRotation) {
                mLastRotation = orientation;
                setOrientationChanged(true);
                handleOrientationChange(mLastRotation);
            } else {
                setOrientationChanged(false);
            }
        }
    }

    public boolean isOrientationChanged() {
        LogUtil.debug(TAG, "isOrientationChanged: " + orientationChanged);
        return orientationChanged;
    }

    public void setOrientationChanged(boolean orientationChanged) {
        LogUtil.debug(TAG, "setOrientationChanged: " + orientationChanged);
        this.orientationChanged = orientationChanged;
    }

    public void handleOrientationChange(int currentRotation){
        LogUtil.debug(TAG, "handleOrientationChange currentRotation = " + currentRotation);
    }

    private int getDisplayRotation() {
        WindowManager wm = (WindowManager) mApplicationContext.getSystemService(Context.WINDOW_SERVICE);
        return wm.getDefaultDisplay().getRotation();
    }

    public void register(final Context context) {
        if (context != null) {
            LogUtil.debug(TAG, "register");
            mApplicationContext = context.getApplicationContext();
            if (mApplicationContext != null) {
                mApplicationContext.registerReceiver(this,
                                                         new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED));
            }
        }
    }

    public void unregister() {
        if (mApplicationContext != null) {
            LogUtil.debug(TAG, "unregister");
            mApplicationContext.unregisterReceiver(this);
            mApplicationContext = null;
        }
    }
}
