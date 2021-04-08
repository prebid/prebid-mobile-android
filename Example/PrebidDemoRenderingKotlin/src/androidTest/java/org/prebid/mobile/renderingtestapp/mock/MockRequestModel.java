package org.prebid.mobile.renderingtestapp.mock;

import java.util.Map;

public class MockRequestModel {

    private final String mPath;
    private final String mHost;
    private final String mMethod;
    private final String mBody;
    private final Map<String, String> mQueryString;

    public MockRequestModel(String path, String host, String method, String body, Map<String, String> queryString) {
        mPath = path;
        mHost = host;
        mMethod = method;
        mBody = body;
        mQueryString = queryString;
    }


    public String getPath() {
        return mPath;
    }

    public String getHost() {
        return mHost;
    }

    public String getMethod() {
        return mMethod;
    }

    public String getBody() {
        return mBody;
    }

    public Map<String, String> getQueryString() {
        return mQueryString;
    }
}
