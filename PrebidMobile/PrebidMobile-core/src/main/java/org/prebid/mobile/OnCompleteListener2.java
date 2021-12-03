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

import androidx.annotation.MainThread;
import androidx.annotation.Nullable;

import java.util.Map;

public interface OnCompleteListener2 {
    /**
     * This method will be called when PrebidMobile finishes attaching keywords to unmodifiableMap.
     * @param resultCode see {@link ResultCode} class definition for details
     * @param unmodifiableMap a map of targeting Key/Value pairs
     */
    @MainThread
    void onComplete(ResultCode resultCode, @Nullable Map<String, String> unmodifiableMap);
}
