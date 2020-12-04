/*
 *    Copyright 2020-2021 Prebid.org, Inc.
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

package org.prebid.mobile.http;

import java.util.List;
import java.util.Map;

public class HTTPResponse {
    private boolean succeeded;
    private String responseBody;
    private Map<String, List<String>> headers;
    private HttpErrorCode errorCode;

    public HTTPResponse() {

    }

    public HTTPResponse(boolean succeeded, String responseBody, Map<String, List<String>> headers) {
        this.succeeded = succeeded;
        this.responseBody = responseBody;
        this.headers = headers;
    }

    public boolean getSucceeded() {
        return succeeded;
    }

    public void setSucceeded(boolean succeeded) {
        this.succeeded = succeeded;
    }

    public void setErrorCode(HttpErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public HttpErrorCode getErrorCode() {
        return errorCode;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }
}
