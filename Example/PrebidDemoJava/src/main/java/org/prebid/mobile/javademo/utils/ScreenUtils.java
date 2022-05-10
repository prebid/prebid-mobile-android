package org.prebid.mobile.javademo.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import static android.view.WindowManager.LayoutParams.*;

public class ScreenUtils {

    public static void closeSystemWindowsAndKeepScreenOn(Application application) {
        application.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(
                Activity activity,
                Bundle savedInstanceState
            ) {
                activity.getWindow().addFlags(FLAG_KEEP_SCREEN_ON);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    activity.setShowWhenLocked(true);
                } else {
                    //noinspection deprecation
                    activity.getWindow().addFlags(FLAG_TURN_SCREEN_ON | FLAG_SHOW_WHEN_LOCKED);
                }
            }

            @Override
            public void onActivityStarted(Activity activity) {}

            @Override
            public void onActivityResumed(Activity activity) {}

            @Override
            public void onActivityPaused(Activity activity) {}

            @Override
            public void onActivityStopped(Activity activity) {}

            @Override
            public void onActivitySaveInstanceState(
                Activity activity,
                Bundle outState
            ) {}

            @Override
            public void onActivityDestroyed(Activity activity) {}
        });
    }

}
