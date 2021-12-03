/*
 *    Copyright 2020-2021 Prebid.org, Inc.
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

package org.prebid.mobile.tasksmanager;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.VisibleForTesting;

public class MainThreadExecutor implements CancellableExecutor {
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void execute(Runnable runnable) {
        handler.post(runnable);
    }

    @Override
    public boolean cancel(Runnable runnable) {
        handler.removeCallbacks(runnable);
        return true;
    }

    @VisibleForTesting
    public Handler getMainExecutor() {
        return handler;
    }
}
