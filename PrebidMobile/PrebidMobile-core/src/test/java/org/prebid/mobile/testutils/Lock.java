/*
 *    Copyright 2018-2019 Prebid.org, Inc.
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

package org.prebid.mobile.testutils;

import android.util.Log;

public class Lock {
    private static final Object lock = new Object();
    private static boolean notified = false;

    public static void pause() {
        synchronized (lock) {
            Log.w("Prebid-UnitTests", "pausing " + Thread.currentThread().getName());
            while (!notified) {
                try {
                    lock.wait();
                } catch (InterruptedException ignored) {
                    continue; // recheck and go back to waiting if still not notified
                }
            }
        }
        notified = false;
        Log.w("Prebid-UnitTests", "unpausing " + Thread.currentThread().getName());
    }

    public static void pause(long time) {
        synchronized (lock) {
            Log.w("Prebid-UnitTests", "pausing " + Thread.currentThread().getName());
            if (!notified) {
                try {
                    lock.wait(time);
                } catch (InterruptedException ignored) {
                    // wake up
                }
            }
        }
        notified = false;
        Log.w("Prebid-UnitTests", "unpausing " + Thread.currentThread().getName());
    }

    public static void unpause() {
        Log.w("Prebid-UnitTests", "notify from " + Thread.currentThread().getName());
        synchronized (lock) {
            lock.notifyAll();
            notified = true;
        }
    }
}
