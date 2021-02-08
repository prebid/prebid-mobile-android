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

import java.util.concurrent.Executor;

public class TasksManager {

    private static TasksManager instance = null;
    public Executor mainThreadExecutor;
    public Executor backgroundThreadExecutor;

    private TasksManager() {
        mainThreadExecutor = new MainThreadExecutor();
        backgroundThreadExecutor = new BackgroundThreadExecutor();
    }

    /**
     * Factory method to obtain the Singleton instance of the TasksManager
     * */
    public synchronized static TasksManager getInstance() {
        if (instance == null) {
            synchronized (TasksManager.class) {
                instance = new TasksManager();
            }

        }
        return instance;
    }

    /**
     * This API can be used to execute code block on the main thread.
     * @param task takes in task (to be executed on main thread) as a runnable
     * */
    public void executeOnMainThread(Runnable task) {
        mainThreadExecutor.execute(task);
    }

    /**
     * Utility method to cancel an ongoing main thread task
     * @param task takes in task to be cancelled
     * */
    public void cancelTaskOnMainThread(Runnable task) {
        ((CancellableExecutor) mainThreadExecutor).cancel(task);
    }

    /**
     * This API can be used to execute code block on the background thread.
     * @param task takes in task (to be executed on background thread) as a runnable
     * */
    public void executeOnBackgroundThread(Runnable task) {
        backgroundThreadExecutor.execute(task);
    }

    /**
     * Utility method to cancel an ongoing background thread task
     * @param task takes in task to be cancelled
     * */
    public void cancelTaksOnBackgroundThread(Runnable task) {
        ((CancellableExecutor) backgroundThreadExecutor).cancel(task);

    }
}
