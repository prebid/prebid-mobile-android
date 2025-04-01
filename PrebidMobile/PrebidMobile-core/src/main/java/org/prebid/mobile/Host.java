/*
 *    Copyright 2018-2019 Prebid.org, Inc.
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

package org.prebid.mobile;

/**
 * Host for requesting ad.
 */
public enum Host {

    CUSTOM("");

    private String url;

    Host(String url) {
        this.url = url;
    }

    public String getHostUrl() {
        return this.url;
    }

    public void setHostUrl(String url) {
        if (this.equals(CUSTOM)) {
            this.url = url;
        }
    }

    public static Host createCustomHost(String url) {
        Host custom = Host.CUSTOM;
        custom.setHostUrl(url);
        return custom;
    }

}
