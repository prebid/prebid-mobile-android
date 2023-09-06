package org.prebid.mobile.rendering.sdk;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import org.prebid.mobile.LogUtil;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.api.rendering.PrebidRenderer;
import org.prebid.mobile.api.rendering.pluginrenderer.PrebidMobilePluginRegister;
import org.prebid.mobile.rendering.listeners.SdkInitializationListener;
import org.prebid.mobile.rendering.session.manager.OmAdSessionManager;
import org.prebid.mobile.rendering.utils.helpers.AppInfoManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class SdkInitializer {

    private static final String TAG = SdkInitializer.class.getSimpleName();

    public static void init(
        @Nullable Context context,
        @Nullable SdkInitializationListener listener
    ) {
        if (PrebidMobile.isSdkInitialized() || InitializationNotifier.isInitializationInProgress()) {
            return;
        }

        InitializationNotifier initializationNotifier = new InitializationNotifier(listener);

        Context applicationContext = getApplicationContext(context);
        if (applicationContext == null) {
            initializationNotifier.initializationFailed("Context must be not null!");
            return;
        }

        LogUtil.debug(TAG, "Initializing Prebid SDK");
        PrebidContextHolder.setContext(applicationContext);

        if (PrebidMobile.logLevel != null) {
            LogUtil.setLogLevel(PrebidMobile.getLogLevel().getValue());
        }

        try {
            // todo using internal api until pluginrenderer feature is released
//            PrebidMobile.registerPluginRenderer(new PrebidRenderer());
            PrebidMobilePluginRegister.getInstance().registerPlugin(new PrebidRenderer());

            AppInfoManager.init(applicationContext);

            OmAdSessionManager.activateOmSdk(applicationContext);

            ManagersResolver.getInstance().prepare(applicationContext);

            JSLibraryManager.getInstance(applicationContext).checkIfScriptsDownloadedAndStartDownloadingIfNot();
        } catch (Throwable throwable) {
            initializationNotifier.initializationFailed("Exception during initialization: " + throwable.getMessage() + "\n" + Log.getStackTraceString(throwable));
            return;
        }

        runBackgroundTasks(initializationNotifier);
    }

    private static void runBackgroundTasks(InitializationNotifier initializationNotifier) {
        Thread thread = new Thread(() -> {
            try {
                ExecutorService executor = Executors.newFixedThreadPool(2);

                Future<String> statusRequesterResult = executor.submit(StatusRequester::makeRequest);
                executor.execute(() -> ManagersResolver.getInstance().getUserConsentManager().initConsentValues());
                executor.execute(UserAgentFetcherTask::run);
                executor.shutdown();

                boolean terminatedByTimeout = !executor.awaitTermination(10, TimeUnit.SECONDS);
                if (terminatedByTimeout) {
                    initializationNotifier.initializationFailed("Terminated by timeout.");
                    return;
                }

                String statusRequesterError = statusRequesterResult.get();
                initializationNotifier.initializationCompleted(statusRequesterError);
            } catch (Exception exception) {
                initializationNotifier.initializationFailed("Exception during initialization: " + Log.getStackTraceString(exception));
            }
        });
        thread.start();
    }

    @Nullable
    private static Context getApplicationContext(
        @Nullable Context context
    ) {
        if (context instanceof Application) {
            return context;
        } else if (context != null) {
            return context.getApplicationContext();
        }
        return null;
    }

}
