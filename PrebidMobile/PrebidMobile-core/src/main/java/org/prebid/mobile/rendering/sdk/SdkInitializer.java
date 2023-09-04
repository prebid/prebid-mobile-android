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

public class SdkInitializer {

    private static final String TAG = SdkInitializer.class.getSimpleName();

    public static void init(
        @Nullable Context context,
        @Nullable SdkInitializationListener listener
    ) {
        if (PrebidMobile.isSdkInitialized() || InitializationManager.isInitializationInProgress()) {
            return;
        }

        Context applicationContext = getApplicationContext(context);
        InitializationManager initializationManager = new InitializationManager(listener);

        if (applicationContext == null) {
            initializationManager.initializationFailed("Context must be not null!");
            return;
        }

        LogUtil.debug(TAG, "Initializing Prebid SDK");
        PrebidContextHolder.setContext(applicationContext);

        if (PrebidMobile.logLevel != null) {
            LogUtil.setLogLevel(PrebidMobile.getLogLevel().getValue());
        }

        try {
            // todo using internal api until pluginrenderer feature is released
//        PrebidMobile.registerPluginRenderer(new PrebidRenderer());
            PrebidMobilePluginRegister.getInstance().registerPlugin(new PrebidRenderer());

            AppInfoManager.init(applicationContext);

            OmAdSessionManager.activateOmSdk(applicationContext);

            ManagersResolver.getInstance().prepare(applicationContext);

            JSLibraryManager.getInstance(applicationContext).checkIfScriptsDownloadedAndStartDownloadingIfNot();
        } catch (Throwable throwable) {
            initializationManager.initializationFailed("Exception during initialization: " + throwable.getMessage() + "\n" + Log.getStackTraceString(throwable));
            return;
        }

        runBackgroundTasks(initializationManager);
    }

    private static void runBackgroundTasks(InitializationManager manager) {
        StatusRequester.makeRequest(manager);

        UserAgentFetcherTask.run(manager);
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
