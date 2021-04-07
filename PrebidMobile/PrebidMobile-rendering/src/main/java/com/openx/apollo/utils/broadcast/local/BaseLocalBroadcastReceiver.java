package com.openx.apollo.utils.broadcast.local;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public abstract class BaseLocalBroadcastReceiver extends BroadcastReceiver {
    private static final String BROADCAST_IDENTIFIER_KEY = "BROADCAST_IDENTIFIER_KEY";

    private final long mBroadcastId;
    @Nullable
    private Context mApplicationContext;

    public BaseLocalBroadcastReceiver(long broadcastId) {
        mBroadcastId = broadcastId;
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
        mApplicationContext = context.getApplicationContext();
        LocalBroadcastManager.getInstance(mApplicationContext).registerReceiver(broadcastReceiver,
                                                                                getIntentFilter());
    }

    public void unregister(final @Nullable
                               BroadcastReceiver broadcastReceiver) {
        if (mApplicationContext != null && broadcastReceiver != null) {
            LocalBroadcastManager.getInstance(mApplicationContext).unregisterReceiver(broadcastReceiver);
            mApplicationContext = null;
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
        return mBroadcastId == receivedIdentifier;
    }
}
