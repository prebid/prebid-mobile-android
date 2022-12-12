package org.prebid.mobile.rendering.sdk;

import android.content.Context;

import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;

/**
 * Global Context holder for Prebid SDK.
 */
public class PrebidContextHolder {

    @Nullable
    private static WeakReference<Context> contextReference;

    @Nullable
    public static Context getContext() {
        if (contextReference != null) {
            return contextReference.get();
        }

        return null;
    }

    protected static void setContext(Context context) {
        contextReference = new WeakReference<>(context);
    }

    /**
     * It clears Context reference and thereby SDK will skip any fetch demand calls.
     * Must be called only if initialization is failed.
     */
    protected static void clearContext() {
        contextReference = null;
    }


    private PrebidContextHolder() {
    }

}
