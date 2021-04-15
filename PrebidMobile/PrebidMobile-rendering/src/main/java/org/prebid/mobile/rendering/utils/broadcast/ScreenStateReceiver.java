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

import org.prebid.mobile.rendering.utils.logger.OXLog;

public class ScreenStateReceiver extends BroadcastReceiver {
    private static final String TAG = ScreenStateReceiver.class.getSimpleName();

    private Context mApplicationContext;

    private boolean mIsScreenOn = true;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (intent == null) {
            return;
        }

        final String action = intent.getAction();

        if (Intent.ACTION_USER_PRESENT.equals(action)) {
            mIsScreenOn = true;
        }
        else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
            mIsScreenOn = false;
        }
    }

    public boolean isScreenOn() {
        return mIsScreenOn;
    }

    public void register(final Context context) {
        if (context == null) {
            OXLog.debug(TAG, "register: Failed. Context is null");
            return;
        }

        final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);

        mApplicationContext = context.getApplicationContext();
        mApplicationContext.registerReceiver(this, filter);
    }

    public void unregister() {
        if (mApplicationContext == null) {
            OXLog.debug(TAG, "unregister: Failed. Context is null");
            return;
        }

        mApplicationContext.unregisterReceiver(this);
        mApplicationContext = null;
    }
}
