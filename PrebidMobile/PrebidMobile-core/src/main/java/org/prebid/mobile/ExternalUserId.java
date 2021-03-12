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

package org.prebid.mobile;

import android.support.annotation.NonNull;

import java.util.List;
import java.util.Map;

/**
 * Defines the User Id Object from an External Third Party Source
 */
public class ExternalUserId {

    private String source;
    /**
     Get source of the External User Id String
     */
    public String getSource() {
        return source;
    }
    /**
     Set source of the External User Id String
     */
    public void setSource(String source) {
        this.source = source;
    }

    private List<Map<String,Object>> userIdArray;
    /**
     Get List of Maps containing objects that hold UserId parameters.
     */
    public List<Map<String,Object>> getUserIdArray() {
        return userIdArray;
    }
    /**
     Set List of Maps containing objects that hold UserId parameters.
     */
    public void setUserIdArray(List<Map<String,Object>> userIdArray) {
        this.userIdArray = userIdArray;
    }
    /**
     Initialize ExternalUserId Class
     - Parameter source: Source of the External User Id String.
     - Parameter userIdArray: List of Maps containing objects that hold UserId parameters.
     */
    public ExternalUserId(@NonNull String source,@NonNull List<Map<String,Object>> userIdArray) {
        this.source = source;
        this.userIdArray = userIdArray;
    }
}
