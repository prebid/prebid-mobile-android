package org.prebid.mobile.rendering.sdk;

import android.content.Context;

/**
 * Base manager. Each manager extends base manager logic to provide additional
 * functionality.
 */
public interface Manager {
    /**
     * Check initialization of manager.
     *
     * @return true, if manager was initialized
     */
    boolean isInit();

    /**
     * Initialize manager.
     *
     * @param context the context for which manager will be initialized.
     */
    void init(Context context);

    /**
     * Get the context for which manager was initialized.
     *
     * @return the context
     */
    Context getContext();

    /**
     * Dispose manager and release all necessary resources.
     */
    void dispose();
}
