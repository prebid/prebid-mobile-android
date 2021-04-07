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