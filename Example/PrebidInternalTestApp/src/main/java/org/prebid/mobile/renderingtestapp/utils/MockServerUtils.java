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

package org.prebid.mobile.renderingtestapp.utils;

import android.os.Handler;
import android.os.Looper;

import org.prebid.mobile.renderingtestapp.InternalTestApplication;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MockServerUtils {

    private final static String TAG = "MockServerUtils";

    private final static String HOST = "https://10.0.2.2:8000/";

    private final static String ENDPOINT_ADD_MOCK = HOST + "api/add_mock";
    private final static String ENDPOINT_CLEAR_LOGS = HOST + "api/clear_logs";
    private final static String ENDPOINT_SET_RANDOM_NO_BIDS = HOST + "api/set_random_no_bids";
    private final static String ENDPOINT_CANCEL_RANDOM_NO_BIDS = HOST + "api/cancel_random_no_bids";
    private final static OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder().build();

    private static boolean sInitialized = false;
    private static int mRequestCounter = 0;
    private static final MockResponseCallback mCountdownCallback = success -> {
        if (success) {
            mRequestCounter--;
        }
        if (mRequestCounter == 0) {
            sInitialized = true;
        }
    };

    public static void setMockServerResponseFromAssets(String id, String assetsFileName) {
        setMockServerResponse(id, assetFileDataToString(assetsFileName));
    }

    public static void clearLogs() {
        clearLogs(null);
    }

    public static void clearLogs(MockResponseCallback callback) {
        Call newCall = HTTP_CLIENT.newCall(new Request.Builder().url(ENDPOINT_CLEAR_LOGS).build());
        sendRequest(newCall, callback);
    }

    public static void setRandomNoBids() {
        RequestBody emptyBody = RequestBody.create(null, new byte[0]);

        Call newCall = HTTP_CLIENT.newCall(new Request.Builder().post(emptyBody).url(ENDPOINT_SET_RANDOM_NO_BIDS).build());
        sendRequest(newCall, null);
    }

    public static void cancelRandomNoBids() {
        Call newCall = HTTP_CLIENT.newCall(new Request.Builder().get().url(ENDPOINT_CANCEL_RANDOM_NO_BIDS).build());
        sendRequest(newCall, null);
    }

    public static boolean isInitialized() {
        return sInitialized;
    }

    private static void setMockServerResponse(String id, String mockResponse) {
        mRequestCounter++;
        FormBody formBody = new FormBody.Builder()
            .add("auid", id)
            .add("type", "regular")
            .add("mock", mockResponse).build();
        Call newCall = HTTP_CLIENT.newCall(new Request.Builder().url(ENDPOINT_ADD_MOCK).post(formBody).build());
        sendRequest(newCall, MockServerUtils.mCountdownCallback);
    }

    private static void sendRequest(Call request, MockResponseCallback callback) {
        request.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (callback != null) {
                    new Handler(Looper.getMainLooper()).post(() -> callback.onResult(false));
                }
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (callback != null) {
                    new Handler(Looper.getMainLooper()).post(() -> callback.onResult(response.isSuccessful()));
                }
                response.body().close();
            }
        });
    }

    private static String assetFileDataToString(String assetFileName) {
        try {
            return InputStreamUtils.convert(InternalTestApplication.getInstance().getAssets().open(assetFileName));
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to open asset file: " + e.toString());
        }
    }

    public interface MockResponseCallback {
        void onResult(boolean success);
    }
}
