package org.prebid.mobile;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class VisibilityActivityListener implements Application.ActivityLifecycleCallbacks {

    private final VisibilityMonitor monitor;
    private final String burl;
    private final String cacheId;

    public VisibilityActivityListener(VisibilityMonitor monitor, String burl, String cacheId) {
        this.monitor = monitor;
        this.burl = burl;
        this.cacheId = cacheId;
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        View rootView = activity.getWindow().getDecorView();
        monitor.trackView(rootView, burl, cacheId);
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {}

    @Override
    public void onActivityStarted(@NonNull Activity activity) {}

    @Override
    public void onActivityPaused(@NonNull Activity activity) {}

    @Override
    public void onActivityStopped(@NonNull Activity activity) {}

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {}
}
