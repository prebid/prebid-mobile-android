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
import java.util.Map;

/**
 * Defines the User Id Object from an External Third Party Source
 */
public class ExternalUserId {

    private String source;
    public String getSource() {
        return source;
    }
    public void setSource(String source) {
        this.source = source;
    }

    private String identifier;
    public String getIdentifier() {
        return identifier;
    }
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    private Integer atype;
    public Integer getAtype() {
        return atype;
    }
    public void setAtype(Integer atype) {
        this.atype = atype;
    }

    private Map<String,Object> ext;
    public Map<String, Object> getExt() {
        return ext;
    }
    public void setExt(Map<String, Object> ext) {
        this.ext = ext;
    }

    /**
     Initialize ExternalUserId Class
     - Parameter source: Source of the External User Id String.
     - Parameter identifier: String of the External User Id.
     - Parameter atype: (Optional) Integer of the External User Id.
     - Parameter ext: (Optional) Map of the External User Id.
     */
    public ExternalUserId(@NonNull String source, @NonNull String identifier, Integer atype, Map<String, Object> ext) {
        this.source = source;
        this.identifier = identifier;
        this.atype = atype;
        this.ext = ext;
    }

}
