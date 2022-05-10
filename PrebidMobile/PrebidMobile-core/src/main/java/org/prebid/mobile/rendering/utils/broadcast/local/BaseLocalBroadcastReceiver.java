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

package org.prebid.mobile.rendering.utils.broadcast.local;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public abstract class BaseLocalBroadcastReceiver extends BroadcastReceiver {
    private static final String BROADCAST_IDENTIFIER_KEY = "BROADCAST_IDENTIFIER_KEY";

    private final long broadcastId;
    @Nullable private Context applicationContext;

    public BaseLocalBroadcastReceiver(long broadcastId) {
        this.broadcastId = broadcastId;
    }

    public static void sendLocalBroadcast(
        @NonNull
        final Context context, final long broadcastIdentifier,
        @NonNull
        final String action) {
        Intent intent = new Intent(action);
        intent.putExtra(BROADCAST_IDENTIFIER_KEY, broadcastIdentifier);
        LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(intent);
    }

    @NonNull
    public abstract IntentFilter getIntentFilter();

    public void register(
        @NonNull
        final Context context,
        @NonNull
        final BroadcastReceiver broadcastReceiver) {
        applicationContext = context.getApplicationContext();
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(broadcastReceiver, getIntentFilter());
    }

    public void unregister(final @Nullable
                               BroadcastReceiver broadcastReceiver) {
        if (applicationContext != null && broadcastReceiver != null) {
            LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(broadcastReceiver);
            applicationContext = null;
        }
    }

    /**
     * Only consume this broadcast if the identifier on the received Intent and this broadcast
     * match up. This allows us to target broadcasts to the ad that spawned them. We include
     * this here because there is no appropriate IntentFilter condition that can recreate this
     * behavior.
     */
    public boolean shouldConsumeBroadcast(
        @NonNull
        final Intent intent) {
        final long receivedIdentifier = intent.getLongExtra(BROADCAST_IDENTIFIER_KEY, -1);
        return broadcastId == receivedIdentifier;
    }
}
