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

package org.prebid.mobile.renderingtestapp.data

import android.os.Bundle

data class DemoItem(val label: String, val action: Int = -1, val tag: List<Tag> = listOf(), val bundle: Bundle? = null)

enum class Tag(val tagName: String) {
    REMOTE("Remote"),

    BANNER("Banner"),
    MRAID("MRAID"),
    VIDEO("Video"),
    INTERSTITIAL("Interstitial"),
    NATIVE("Native"),

    IN_APP("In-App"),
    GAM("GAM"),
    MOPUB("MoPub"),
    ADMOB("AdMob"),

    ALL("All")
}