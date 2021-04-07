package com.openx.apollo.mraid.handler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.openx.apollo.views.webview.mraid.JSInterface;

public class FetchPropertiesHandler extends Handler {
    @NonNull
    private final FetchPropertyCallback mCallback;

    public FetchPropertiesHandler(
        @NonNull
            FetchPropertyCallback callback) {
        mCallback = callback;
    }

    @Override
    public void handleMessage(final Message message) {
        super.handleMessage(message);

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            try {
                final String expandProperties = message.getData().getString(JSInterface.JSON_VALUE);
                mCallback.onResult(expandProperties);
            }
            catch (Exception e) {
                mCallback.onError(e);
            }
        });
    }

    public interface FetchPropertyCallback {
        void onResult(String propertyJson);

        void onError(Throwable throwable);
    }
}
