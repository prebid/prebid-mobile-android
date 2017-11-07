package org.prebid.demandsdkadapters.common;

import android.os.AsyncTask;

import org.json.JSONObject;
import org.prebid.mobile.core.LogUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CacheService extends AsyncTask<Object, Object, JSONObject> {

    private String cacheId;
    private CacheListener listener;

    public CacheService(CacheListener listener, String cacheID) {
        this.listener = listener;
        this.cacheId = cacheID;
    }

    @Override
    protected JSONObject doInBackground(Object... objects) {
        try {
            StringBuilder sb = new StringBuilder("http://prebid.adnxs.com/pbc/v1/cache?uuid=");
            sb.append(this.cacheId);
            URL url = new URL(sb.toString());

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // Start the connection
            conn.connect();

            // Read request response
            int httpResult = conn.getResponseCode();

            if (httpResult == HttpURLConnection.HTTP_OK) {
                StringBuilder builder = new StringBuilder();
                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                reader.close();
                is.close();
                String result = builder.toString();
                LogUtil.i(String.format("For cache id %s, Prebid cache returned response %s", cacheId, result));
                JSONObject response = new JSONObject(result);
                return response;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        if (listener != null) {
            listener.onResponded(jsonObject);
        }
    }

    public interface CacheListener {
        void onResponded(JSONObject jsonObject);
    }
}
