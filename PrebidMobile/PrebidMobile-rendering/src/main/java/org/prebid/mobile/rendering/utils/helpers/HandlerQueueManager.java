package org.prebid.mobile.rendering.utils.helpers;

import android.os.Handler;

import java.util.Hashtable;

public class HandlerQueueManager {
    private Hashtable<String, Handler> mHandlersQueue = new Hashtable<>();

    /**
     * Calculates handler hash and puts handler in queue.
     *
     * @param handler to be stored in queue
     * @return handler hash
     */
    public String queueHandler(Handler handler) {
        if (handler == null) {
            return null;
        }

        String handlerHash = String.valueOf(System.identityHashCode(handler));

        getHandlersQueue().put(handlerHash, handler);

        return handlerHash;
    }

    /**
     * Searches for handler by key
     *
     * @param handlerHash
     * @return handler from queue or null if not found
     */
    public Handler findHandler(String handlerHash) {
        if (handlerHash == null || handlerHash.equals("")) {
            return null;
        }

        if (getHandlersQueue().containsKey(handlerHash)) {
            return getHandlersQueue().get(handlerHash);
        }

        return null;
    }

    public void removeHandler(String handlerHash) {
        if (handlerHash == null || handlerHash.equals("")) {
            return;
        }

        getHandlersQueue().remove(handlerHash);
    }

    public void clearQueue() {
        mHandlersQueue.clear();
    }

    private Hashtable<String, Handler> getHandlersQueue() {
        return mHandlersQueue;
    }
}
