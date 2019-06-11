package org.prebid.mobile.drprebid.async;

import android.util.Log;

import org.prebid.mobile.drprebid.managers.DemandTestManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DemandTestTask implements Runnable {
    private static final String TAG = DemandTestTask.class.getSimpleName();

    private final String url;
    private final String request;
    private final DemandTestResultTask resultTask;


    public DemandTestTask(String hostUrl, String requestBody, DemandTestResultTask resultTask) {
        this.url = hostUrl;
        this.request = requestBody;
        this.resultTask = resultTask;
    }

    @Override
    public void run() {
        if (resultTask != null) {
            String responseText = "";
            int responseCode = 0;

            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url).build();

                Response response = client.newCall(request).execute();

                if (response.body() != null) {
                    InputStream inputStream = response.body().byteStream();
                    responseText = getStringFromStream(inputStream);
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

    private String getStringFromStream(InputStream inputStream) throws IOException {
        final int BUFFER_SIZE = 4096;
        ByteArrayOutputStream resultStream = new ByteArrayOutputStream(BUFFER_SIZE);
        byte[] buffer = new byte[BUFFER_SIZE];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            resultStream.write(buffer, 0, length);
        }
        return resultStream.toString("UTF-8");
    }
}
