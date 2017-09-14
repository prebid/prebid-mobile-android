package org.prebid.mobile.demoapp.dummyfragments;


import android.content.Context;
import android.os.AsyncTask;

import com.facebook.ads.BidderTokenProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class FBRequest extends AsyncTask<Object, Object, JSONObject> {

    interface FBListener {
        void onFBResponded(JSONObject jsonObject);
    }

    private JSONObject postData;
    private FBListener listener;
    private String type;

    public FBRequest(String type, FBListener listener, Context context) {
        this.listener = listener;
        this.type = type;
        switch (type) {
            case "banner":
                try {
                    postData = new JSONObject("{ \n" +
                            "   \"id\":\"d3013e9e-ca55-4a86-9baa-d44e31355e1d\",\n" +
                            "   \"imp\":[ \n" +
                            "      { \n" +
                            "         \"id\":\"Interstitial Ad\",\n" +
                            "         \"banner\":{ \n" +
                            "            \"w\":-1,\n" +
                            "            \"h\":250\n" +
                            "         },\n" +
                            "         \"tagid\":\"1959066997713356_1959836684303054\",\n" +
                            "         \"instl\":0\n" +
                            "      }\n" +
                            "   ],\n" +
                            "   \"app\":{ \n" +
                            "      \"bundle\":\"org.prebid.mobile.demoapp\",\n" +
                            "      \"ver\":\"0.0.1\",\n" +
                            "      \"publisher\":{ \n" +
                            "         \"id\":\"1959066997713356\"\n" +
                            "      }\n" +
                            "   },\n" +
                            "   \"device\":{\n" +
                            "      \"ifa\":\"19e7a8e3-4544-49f4-bfb1-99370ecfbc73\",\n" +
                            "      \"ip\":\"10.6.252.173\",\n" +
                            "      \"ua\": \"Mozilla/5.0 (Linux; Android 7.0; SM-G935U Build/NRD90M; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/59.0.3071.125 Mobile Safari/537.36\",\n" +
                            "      \"dnt\":0,\n" +
                            "      \"make\":\"samsung\",\n" +
                            "      \"model\":\"SM-G935U\",\n" +
                            "      \"os\":\"android\",\n" +
                            "      \"osv\": \"24\",\n" +
                            "      \"h\":\"568\",\n" +
                            "      \"w\":\"320\"\n" +
                            "   },\n" +
                            "   \"user\":{\n" +
                            "      \"buyeruid\":\"eJxNUV1rgzAU/S8+a8g1ps69pZqtodZIElfGGKLTjsL6QctgMPbfd1O7srfk5OTcc879DuZNVZQyuA8Op3dyPI39diC7Q7/9GMkw7g7d8RiEQS6MUdIgCy82N1JW7VoVboEIm1EPFsv2SRqrdIVYQmJOaISf9+PX55mg6LDdv195uajFXJXKPSP1hYVJyMM0BAhhFkL6iizxUONTNzLoNgOwmHIKlGWQ8YTTnkLaQfbWI1Fb5In9cDpsB68urXfQqgLhWZokPeNJBJu4i/DIogyGt4jC2ANNs4FuMj+rrr1x/EAJ/NNwauVrAU5jdkdZCmTGY3xvqsn4pvs4j3hfiaXnnbvd+fMSsdS5uDQ67tvGIqCK1urG5HLqDwfOG1V6izA14rPeMqx0IUtE7Cp6zBhvvEBlnSjLvwVoezWcEl+90drJm9q0nIVUjwvnS0g8RThn1Lxxt24mG5W4RCxwz+Ky50q6tTbL1j3X8ipYyMpOgRlO+/kFU76RQA==\"\n" +
                            "   },\n" +
                            "   \"ext\":{ \n" +
                            "      \"platformid\":442648859414574\n" +
                            "   },\n" +
                            "   \"at\":1,\n" +
                            "   \"tmax\":1000,\n" +
                            "   \"test\":1\n" +
                            "}");
                    JSONObject obj = (JSONObject) postData.get("user");
                    obj.put("buyeruid", BidderTokenProvider.getBidderToken(context));
                } catch (JSONException e) {
                    postData = new JSONObject();
                }
                break;
            case "interstitial":
                try {
                    postData = new JSONObject("{\n" +
                            "  \"id\": \"14ab8022-efbc-479f-b667-a46be59e1118\",\n" +
                            "  \"imp\": [\n" +
                            "    {\n" +
                            "      \"id\": \"Interstitial Ad\",\n" +
                            "      \"banner\": {\n" +
                            "        \"w\": -1,\n" +
                            "        \"h\": -1,\n" +
                            "        \"format\": [\n" +
                            "          {\n" +
                            "            \"w\": -1,\n" +
                            "            \"h\": -1\n" +
                            "          }\n" +
                            "        ]\n" +
                            "      },\n" +
                            "      \"tagid\": \"1959066997713356_1960406244246098\",\n" +
                            "      \"secure\": 1,\n" +
                            "      \"instl\":1\n" +
                            "    }\n" +
                            "  ],\n" +
                            "  \"app\": {\n" +
                            "    \"name\": \"DemoApp\",\n" +
                            "    \"bundle\": \"org.prebid.mobile.demoapp\",\n" +
                            "    \"ver\": \"0.0.2\",\n" +
                            "    \"publisher\": {\n" +
                            "      \"id\": \"1959066997713356\"\n" +
                            "    }\n" +
                            "  },\n" +
                            "  \"device\": {\n" +
                            "    \"ua\": \"Mozilla/5.0 (Linux; Android 7.0; SM-G935U Build/NRD90M; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/60.0.3112.116 Mobile Safari/537.36\",\n" +
                            "    \"ip\": \"FE80::DAC4:6AFF:FEC6:4D58\",\n" +
                            "    \"make\": \"samsung\",\n" +
                            "    \"model\": \"SM-G935U\",\n" +
                            "    \"os\": \"android\",\n" +
                            "    \"osv\": \"24\",\n" +
                            "    \"h\": 616,\n" +
                            "    \"w\": 360,\n" +
                            "    \"mccmnc\": \"310-260\",\n" +
                            "    \"connectiontype\": 1,\n" +
                            "    \"ifa\": \"19e7a8e3-4544-49f4-bfb1-99370ecfbc73\"\n" +
                            "  },\n" +
                            "  \"user\": {\n" +
                            "    \"buyeruid\": \"eJxNUV2L2zAQ/C9+toVlyx/pm2LrLiKOZCS5x1EOI8fyNZAPk3BQKP3vXcVp6Jt2djQ7O/s7WHei\\nbljwLbhcP9F8dcNhRKfLcDg6NLrTxc5zEAYVVYozBSwodKUYE/0br80GkDSPPVhv++9MaS4FYAQl\\nOcKRPc4/bUTnWbhfX7cHq6ItXfOGm3cg/khDEmZhEWIc4jzExQew6EsLrX0RT2SahgTjvR32Q4HJ\\ntMryHGcraCUWiFIDj57H6+UwenWm/fye196CJW7ApYuwG2xE9mMRlamLozKL4xRnrkwK4me1rbcN\\nH2IUo+Q/FcN3PhacxRnOCcYlWiV+004s1id7vDmod3TreTd7un2dPwFoZEXvibpz32kAeN1r2amK\\nLfnByHXHG28SL5nAyz632MmaNYDoXfS6SrPOCwhtaNP8O4DUD8sF8oaUlIY91ZbjbBh/3RjAcuIp\\n1BjF1515prPYEPS+Yg13pvc7C2bepNr25r1lD8GaCb0snMK0P38B2BKRog==\\n\",\n" +
                            "    \"gender\": \"F\"\n" +
                            "  },\n" +
                            "  \"at\": 1,\n" +
                            "  \"tmax\": 400,\n" +
                            "  \"source\": {\n" +
                            "    \"tid\": \"14ab8022-efbc-479f-b667-a46be59e1118\"\n" +
                            "  },\n" +
                            "  \"ext\": {\n" +
                            "    \"platformid\": 442648859414574\n" +
                            "  }\n" +
                            "}");
                    JSONObject obj = (JSONObject) postData.get("user");
                    obj.put("buyeruid", BidderTokenProvider.getBidderToken(context));
                } catch (JSONException e) {
                    postData = new JSONObject();
                }
                break;
        }


    }

    @Override
    protected JSONObject doInBackground(Object... objects) {
        try {
            URL url = new URL("https://an.facebook.com/placementbid.ortb");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestMethod("POST");

            // Add post data
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
            wr.write(postData.toString());
            wr.flush();

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
                JSONObject response = new JSONObject(result);
                // in the future, this can be improved to parse response base on request versions
                return response;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        if (listener != null) {
            try {
                jsonObject.put("type", type);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            listener.onFBResponded(jsonObject);
        }
    }
}
