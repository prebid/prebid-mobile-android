package com.openx.apollo.utils.broadcast.local;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.NonNull;

import static com.openx.apollo.utils.constants.IntentActions.ACTION_BROWSER_CLOSE;

public class EventForwardingLocalBroadcastReceiver extends BaseLocalBroadcastReceiver {

    @NonNull
    private final EventForwardingBroadcastListener mListener;

    public EventForwardingLocalBroadcastReceiver(long broadcastId,
                                                 @NonNull
                                                     EventForwardingBroadcastListener listener) {
        super(broadcastId);
        mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!shouldConsumeBroadcast(intent)) {
            return;
        }

        final String action = intent.getAction();
        mListener.onEvent(action);
    }

    @NonNull
    @Override
    public IntentFilter getIntentFilter() {
        return new IntentFilter(ACTION_BROWSER_CLOSE);
    }

    public interface EventForwardingBroadcastListener {
        void onEvent(String action);
    }
}