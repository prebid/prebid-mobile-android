package org.prebid.mobile.rendering.networking;

import android.os.AsyncTask;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowLegacyAsyncTask;

import java.util.concurrent.Executor;

@Implements(AsyncTask.class)
public class MyShadowAsyncTask<Params, Progress, Result>
    extends ShadowLegacyAsyncTask<Params, Progress, Result> {

    @Implementation
    public AsyncTask<Params, Progress, Result> executeOnExecutor(Executor executor, Params... params) {
        return super.execute(params);
    }
}