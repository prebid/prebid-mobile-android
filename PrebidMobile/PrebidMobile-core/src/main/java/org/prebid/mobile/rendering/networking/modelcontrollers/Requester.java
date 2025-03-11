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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.networking.BaseNetworkTask;
import org.prebid.mobile.rendering.networking.ResponseHandler;
import org.prebid.mobile.rendering.networking.parameters.AdRequestInput;
import org.prebid.mobile.rendering.networking.parameters.AppInfoParameterBuilder;
import org.prebid.mobile.rendering.networking.parameters.BasicParameterBuilder;
import org.prebid.mobile.rendering.networking.parameters.DeviceInfoParameterBuilder;
import org.prebid.mobile.rendering.networking.parameters.GeoLocationParameterBuilder;
import org.prebid.mobile.rendering.networking.parameters.NetworkParameterBuilder;
import org.prebid.mobile.rendering.networking.parameters.ParameterBuilder;
import org.prebid.mobile.rendering.networking.parameters.UserConsentParameterBuilder;
import org.prebid.mobile.rendering.networking.parameters.UserParameters;
import org.prebid.mobile.rendering.networking.urlBuilder.PathBuilderBase;
import org.prebid.mobile.rendering.networking.urlBuilder.URLBuilder;
import org.prebid.mobile.rendering.networking.urlBuilder.URLComponents;
import org.prebid.mobile.rendering.sdk.ManagersResolver;
import org.prebid.mobile.rendering.sdk.PrebidContextHolder;
import org.prebid.mobile.rendering.sdk.deviceData.managers.ConnectionInfoManager;
import org.prebid.mobile.rendering.sdk.deviceData.managers.DeviceInfoManager;
import org.prebid.mobile.rendering.utils.helpers.AdvertisingIdManager;
import org.prebid.mobile.rendering.utils.helpers.AppInfoManager;
import org.prebid.mobile.rendering.utils.helpers.ExternalViewerUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class Requester {

    private static final String TAG = Requester.class.getSimpleName();

    protected String requestName;
    protected AdUnitConfiguration adConfiguration;
    protected URLBuilder urlBuilder;
    protected ResponseHandler adResponseCallBack;
    protected BaseNetworkTask networkTask;
    @Nullable
    protected JSONObject builtRequest;

    Requester(
            AdUnitConfiguration config,
            AdRequestInput adRequestInput,
            ResponseHandler responseHandler
    ) {
        requestName = "";
        adConfiguration = config;

        /*
            IMPORTANT
            Order of builders in the array matters because of decorator pattern
            Later builder parameters has more priority
        */
        ArrayList<ParameterBuilder> parameterBuilderArray = new ArrayList<>(getParameterBuilders());
        urlBuilder = new URLBuilder(getPathBuilder(), parameterBuilderArray, adRequestInput);
        adResponseCallBack = responseHandler;
    }

    public abstract void startAdRequest();

    @NonNull
    public JSONObject getBuiltRequest() {
        return builtRequest == null ? new JSONObject() : builtRequest;
    }

    public void destroy() {
        if (networkTask != null) {
            networkTask.cancel(true);
        }
        networkTask = null;
        adResponseCallBack = null;
    }

    protected List<ParameterBuilder> getParameterBuilders() {
        Context context = PrebidContextHolder.getContext();
        Resources resources = null;
        if (context != null) {
            resources = context.getResources();
        }
        boolean browserActivityAvailable = ExternalViewerUtils.isBrowserActivityCallable(context);

        ArrayList<ParameterBuilder> parameterBuilderArray = new ArrayList<>();
        parameterBuilderArray.add(new BasicParameterBuilder(adConfiguration, resources, browserActivityAvailable));
        parameterBuilderArray.add(new GeoLocationParameterBuilder());
        parameterBuilderArray.add(new AppInfoParameterBuilder(adConfiguration));
        parameterBuilderArray.add(new DeviceInfoParameterBuilder(adConfiguration));
        parameterBuilderArray.add(new NetworkParameterBuilder());
        parameterBuilderArray.add(new UserConsentParameterBuilder());
        return parameterBuilderArray;
    }

    /*
     * Attempts to get the advertisement ID
     *
     * Ad request continues AFTER the attempt to get the advertisement ID. This is because the SDK
     * must attempt to grab and honor the latest LMT value for each ad request
     */
    protected void getAdId() {
        final Context context = PrebidContextHolder.getContext();
        if (context == null) {
            sendAdException(
                "Context is null",
                "Context is null. Can't continue with ad request"
            );
            return;
        }
        makeAdRequest();
        AdvertisingIdManager.updateAdvertisingId();
    }

    protected abstract PathBuilderBase getPathBuilder();

    private void sendAdException(String logMsg, String exceptionMsg) {
        LogUtil.warning(TAG, logMsg);
        AdException adException = new AdException(AdException.INIT_ERROR, exceptionMsg);
        if (adResponseCallBack != null) {
            adResponseCallBack.onErrorWithException(adException, 0);
        }
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
        return urlBuilder.buildUrl();
    }

    protected void sendAdRequest(URLComponents jsonUrlComponents) {
        BaseNetworkTask.GetUrlParams params = new BaseNetworkTask.GetUrlParams();
        params.url = jsonUrlComponents.getBaseUrl();
        String queryArgString = jsonUrlComponents.getQueryArgString();
        params.queryParams = queryArgString;
        params.requestType = "POST";
        params.userAgent = AppInfoManager.getUserAgent();
        params.name = requestName;

        builtRequest = jsonUrlComponents.getRequestJsonObject();

        networkTask = new BaseNetworkTask(adResponseCallBack);
        networkTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
    }

}
