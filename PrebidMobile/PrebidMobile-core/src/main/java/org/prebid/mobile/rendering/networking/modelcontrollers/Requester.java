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

package org.prebid.mobile.rendering.networking.modelcontrollers;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.listeners.AdIdFetchListener;
import org.prebid.mobile.rendering.networking.BaseNetworkTask;
import org.prebid.mobile.rendering.networking.ResponseHandler;
import org.prebid.mobile.rendering.networking.parameters.*;
import org.prebid.mobile.rendering.networking.urlBuilder.PathBuilderBase;
import org.prebid.mobile.rendering.networking.urlBuilder.URLBuilder;
import org.prebid.mobile.rendering.networking.urlBuilder.URLComponents;
import org.prebid.mobile.rendering.sdk.ManagersResolver;
import org.prebid.mobile.rendering.sdk.deviceData.managers.ConnectionInfoManager;
import org.prebid.mobile.rendering.sdk.deviceData.managers.DeviceInfoManager;
import org.prebid.mobile.rendering.sdk.deviceData.managers.UserConsentManager;
import org.prebid.mobile.rendering.utils.helpers.AdIdManager;
import org.prebid.mobile.rendering.utils.helpers.AppInfoManager;
import org.prebid.mobile.rendering.utils.helpers.ExternalViewerUtils;
import org.prebid.mobile.rendering.utils.logger.LogUtil;
import org.prebid.mobile.units.configuration.AdUnitConfiguration;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public abstract class Requester {
    private static final String TAG = Requester.class.getSimpleName();

    private final UserConsentManager mUserConsentManager;

    protected String mRequestName;
    protected WeakReference<Context> mContextReference;
    protected AdUnitConfiguration mAdConfiguration;
    protected URLBuilder mUrlBuilder;
    protected ResponseHandler mAdResponseCallBack;
    protected AsyncTask mNetworkTask;

    Requester(Context context, AdUnitConfiguration config, AdRequestInput adRequestInput, ResponseHandler responseHandler) {
        mRequestName = "";
        mContextReference = new WeakReference<>(context);
        mAdConfiguration = config;
        mUserConsentManager = ManagersResolver.getInstance().getUserConsentManager();

        /*
            IMPORTANT
            Order of builders in the array matters because of decorator pattern
            Later builder parameters has more priority
        */
        ArrayList<ParameterBuilder> parameterBuilderArray = new ArrayList<>(getParameterBuilders());
        mUrlBuilder = new URLBuilder(getPathBuilder(), parameterBuilderArray, adRequestInput);
        mAdResponseCallBack = responseHandler;
    }

    public abstract void startAdRequest();

    public void destroy() {
        if (mNetworkTask != null) {
            mNetworkTask.cancel(true);
        }
    }

    protected List<ParameterBuilder> getParameterBuilders() {
        Context context = mContextReference.get();
        Resources resources = null;
        if (context != null) {
            resources = context.getResources();
        }
        boolean browserActivityAvailable = ExternalViewerUtils.isBrowserActivityCallable(context);

        ArrayList<ParameterBuilder> parameterBuilderArray = new ArrayList<>();
        parameterBuilderArray.add(new BasicParameterBuilder(mAdConfiguration, resources, browserActivityAvailable));
        parameterBuilderArray.add(new GeoLocationParameterBuilder());
        parameterBuilderArray.add(new AppInfoParameterBuilder(mAdConfiguration));
        parameterBuilderArray.add(new DeviceInfoParameterBuilder(mAdConfiguration));
        parameterBuilderArray.add(new NetworkParameterBuilder());
        parameterBuilderArray.add(new UserConsentParameterBuilder(mUserConsentManager));
        return parameterBuilderArray;
    }

    /*
     * Attempts to get the advertisement ID
     *
     * Ad request continues AFTER the attempt to get the advertisement ID. This is because the SDK
     * must attempt to grab and honor the latest LMT value for each ad request
     */
    protected void getAdId() {
        final Context context = mContextReference.get();
        if (context == null) {
            sendAdException(
                "Context is null",
                "Context is null. Can't continue with ad request"
            );
            return;
        }

        if (mUserConsentManager.canAccessDeviceData()) {
            AdIdManager.initAdId(context, new AdIdInitListener(this));
        }
        else {
            AdIdManager.setAdId(null);
            makeAdRequest();
        }
    }

    protected abstract PathBuilderBase getPathBuilder();

    private void sendAdException(String logMsg, String exceptionMsg) {
        LogUtil.warn(TAG, logMsg);
        AdException adException = new AdException(AdException.INIT_ERROR, exceptionMsg);
        mAdResponseCallBack.onErrorWithException(adException, 0);
    }

    protected void makeAdRequest() {
        // Check if app has internet permissions
        DeviceInfoManager deviceManager = ManagersResolver.getInstance().getDeviceManager();
        if (deviceManager == null || !deviceManager.isPermissionGranted("android.permission.INTERNET")) {
            sendAdException(
                "Either Prebid DeviceManager is not initialized or android.permission.INTERNET is not specified. Please check",
                "Internet permission not granted"
            );
            return;
        }

        // Check if device is connected to the internet
        ConnectionInfoManager connectionInfoManager = ManagersResolver.getInstance().getNetworkManager();
        if (connectionInfoManager == null || connectionInfoManager.getConnectionType() == UserParameters.ConnectionType.OFFLINE) {
            sendAdException(
                "Either Prebid networkManager is not initialized or Device is offline. Please check the internet connection",
                "No internet connection detected"
            );
            return;
        }

        // Send ad request
        URLComponents jsonUrlComponents = buildUrlComponent();
        sendAdRequest(jsonUrlComponents);
    }

    protected URLComponents buildUrlComponent() {
        return mUrlBuilder.buildUrl();
    }

    protected void sendAdRequest(URLComponents jsonUrlComponents) {
        BaseNetworkTask.GetUrlParams params = new BaseNetworkTask.GetUrlParams();
        params.url = jsonUrlComponents.getBaseUrl();
        params.queryParams = jsonUrlComponents.getQueryArgString();
        params.requestType = "POST";
        params.userAgent = AppInfoManager.getUserAgent();
        params.name = mRequestName;

        BaseNetworkTask networkTask = new BaseNetworkTask(mAdResponseCallBack);
        mNetworkTask = networkTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
    }

    protected static class AdIdInitListener implements AdIdFetchListener {

        private WeakReference<Requester> mWeakRequester;

        public AdIdInitListener(Requester requester) {
            mWeakRequester = new WeakReference<>(requester);
        }

        @Override
        public void adIdFetchCompletion() {
            LogUtil.info(TAG, "adIdFetchCompletion");
            makeAdRequest();
        }

        @Override
        public void adIdFetchFailure() {
            LogUtil.warn(TAG, "adIdFetchFailure");
            makeAdRequest();
        }

        private void makeAdRequest() {
            Requester requester = mWeakRequester.get();
            if (requester == null) {
                LogUtil.warn(TAG, "Requester is null");
                return;
            }

            requester.makeAdRequest();
        }
    }
}
