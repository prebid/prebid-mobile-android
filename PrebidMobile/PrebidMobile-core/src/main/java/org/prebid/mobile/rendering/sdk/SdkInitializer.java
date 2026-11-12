package org.prebid.mobile.rendering.sdk;

import static org.prebid.mobile.api.rendering.pluginrenderer.PrebidMobilePluginRegister.PREBID_MOBILE_RENDERER_NAME;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import org.prebid.mobile.LogUtil;
import org.prebid.mobile.LogUtil.PrebidLogger;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.api.plugin.PluginCapability;
import org.prebid.mobile.api.rendering.PrebidRenderer;
import org.prebid.mobile.api.rendering.pluginrenderer.PrebidMobilePluginRegister;
import org.prebid.mobile.api.rendering.pluginrenderer.PrebidMobilePluginRenderer;
import org.prebid.mobile.rendering.listeners.SdkInitializationListener;
import org.prebid.mobile.rendering.session.manager.OmAdSessionManager;
import org.prebid.mobile.rendering.utils.helpers.AdvertisingIdManager;
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

        if (PrebidMobile.getLogLevel() != null) {
            LogUtil.setLogLevel(PrebidMobile.getLogLevel().getValue());
        }

        PrebidLogger customLogger = PrebidMobile.getCustomLogger();
        if (customLogger != null) {
            LogUtil.setLogger(customLogger);
        }

        try {
            registerSdkRenderer();

            AppInfoManager.init(applicationContext);

            OmAdSessionManager.activateOmSdk(applicationContext);

            ManagersResolver.getInstance().prepare(applicationContext);

            JSLibraryManager.getInstance(applicationContext).checkIfScriptsDownloadedAndStartDownloadingIfNot();
        } catch (Throwable throwable) {
            initializationNotifier.initializationFailed("Exception during initialization: " + throwable.getMessage() + "\n" + Log.getStackTraceString(throwable));
            return;
        }

        new Thread(() -> runBackgroundTasks(
                initializationNotifier,
                Executors.newFixedThreadPool(2))
        ).start();
    }

    /**
     * Registers the SDK's own renderer. Initialization can run more than once (e.g. after a failed
     * attempt), so a renderer that is already registered is kept, without the duplicate warning.
     * If that renderer is not the SDK's own, it stays the default renderer, and a warning says so.
     */
    @VisibleForTesting
    static void registerSdkRenderer() {
        PrebidMobilePluginRenderer registered = PrebidMobilePluginRegister.getInstance().getDefaultPluginRenderer();

        if (registered == null) {
            PrebidMobile.registerPlugin(new PrebidRenderer(), PluginCapability.RENDERER);
            return;
        }

        if (registered.getClass() != PrebidRenderer.class) {
            LogUtil.warning(TAG, "The app registered " + registered.getClass().getName() + " with the name "
                    + PREBID_MOBILE_RENDERER_NAME + ", so it stays the default renderer instead of the SDK's own. "
                    + "To use the SDK's renderer, unregister it.");
        }
    }

    @VisibleForTesting
    public static void runBackgroundTasks(
            InitializationNotifier initializationNotifier,
            ExecutorService executor
    ) {
        try {
            Future<String> statusRequesterResult = null;
            if (!PrebidMobile.shouldDisableStatusCheck()) {
                statusRequesterResult = executor.submit(new StatusRequester());
            } else {
                LogUtil.debug(TAG, "Prebid SDK initialization skipping status check");
            }
            executor.execute(new UserConsentFetcherTask());
            executor.execute(new UserAgentFetcherTask());
            executor.execute(AdvertisingIdManager::initAdvertisingId);
            executor.shutdown();

            boolean terminatedByTimeout = !executor.awaitTermination(10, TimeUnit.SECONDS);
            if (terminatedByTimeout) {
                initializationNotifier.initializationFailed("Terminated by timeout.");
                return;
            }

            String statusRequesterError = statusRequesterResult != null ? statusRequesterResult.get() : null;
            initializationNotifier.initializationCompleted(statusRequesterError);
        } catch (Exception exception) {
            initializationNotifier.initializationFailed("Exception during initialization: " + Log.getStackTraceString(exception));
        }
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

    protected static class UserConsentFetcherTask implements Runnable {

        @Override
        public void run() {
            ManagersResolver.getInstance().getUserConsentManager().initConsentValues();
        }

    }

}
