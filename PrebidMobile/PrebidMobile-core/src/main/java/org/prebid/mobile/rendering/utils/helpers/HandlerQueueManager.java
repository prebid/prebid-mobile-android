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

package org.prebid.mobile.rendering.utils.helpers;

import android.os.Handler;

import java.util.Hashtable;

public class HandlerQueueManager {
    private Hashtable<String, Handler> handlersQueue = new Hashtable<>();

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
        handlersQueue.clear();
    }

    private Hashtable<String, Handler> getHandlersQueue() {
        return handlersQueue;
    }
}
