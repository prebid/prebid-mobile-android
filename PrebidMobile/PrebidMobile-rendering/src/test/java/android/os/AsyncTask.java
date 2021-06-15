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

package android.os;

import java.util.concurrent.Executor;

/**
 * This is a shadow class for AsyncTask which forces it to run synchronously.
 */
public abstract class AsyncTask<Params, Progress, Result> {

    public static final Executor THREAD_POOL_EXECUTOR = null;

    protected abstract Result doInBackground(Params... params);

    protected void onPostExecute(Result result) {
    }

    protected void onProgressUpdate(Progress... values) {
    }

    public AsyncTask<Params, Progress, Result> execute(Params... params) {
        Result result = doInBackground(params);
        onPostExecute(result);
        return this;
    }

    public AsyncTask<Params, Progress, Result> executeOnExecutor(Executor exec,
                                                                 Params... params) {
        Result result = doInBackground(params);
        onPostExecute(result);
        return this;
    }

    public final boolean isCancelled() {
        return false;
    }

    public final boolean cancel(boolean mayInterruptIfRunning) {
        return true;
    }
}