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
