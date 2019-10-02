package org.prebid.mobile.drprebid.async;

import android.util.Log;

import org.prebid.mobile.drprebid.managers.DemandTestManager;
import org.prebid.mobile.drprebid.util.IOUtil;

import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DemandTestTask implements Runnable {
    private static final String TAG = DemandTestTask.class.getSimpleName();

    private static final MediaType MEDIA_TYPE = MediaType.parse("application/json");

    private final String url;
    private final String requestBody;
    private final DemandTestResultTask resultTask;


    public DemandTestTask(String hostUrl, String requestBody, DemandTestResultTask resultTask) {
        this.url = hostUrl;
        this.requestBody = requestBody;
        this.resultTask = resultTask;
    }

    @Override
    public void run() {
        if (resultTask != null) {
            String responseText = "";
            int responseCode = 0;

            try {
                OkHttpClient client = new OkHttpClient.Builder().build();

                RequestBody body = RequestBody.create(MEDIA_TYPE, requestBody);

                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();

                if (response.body() != null) {
                    InputStream inputStream = response.body().byteStream();
                    responseText = IOUtil.getStringFromStream(inputStream);
                    inputStream.close();
                }

                responseCode = response.code();

            } catch (Exception exception) {
                Log.e(TAG, exception.getMessage());
            }

            resultTask.setResponse(responseText);
            resultTask.setResponseCode(responseCode);
            DemandTestManager.getInstance().getMainThreadExecutor().execute(resultTask);
        }
    }
}
