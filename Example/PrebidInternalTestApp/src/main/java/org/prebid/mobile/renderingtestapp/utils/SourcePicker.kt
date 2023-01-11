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

package org.prebid.mobile.renderingtestapp.utils

import org.prebid.mobile.Host
import org.prebid.mobile.PrebidMobile

object SourcePicker {


    const val PBS_SERVER_DOMAIN = "https://prebid-server-test-j.prebid.org/openrtb2/auction"
    private const val PROD_ACCOUNT_ID = "0689a263-318d-448b-a3d4-b02e8a709d9d"

    private const val QA_SERVER_DOMAIN = "https://prebid.qa.openx.net/openrtb2/auction"
    private const val QA_ACCOUNT_ID = "08efa38c-b6b4-48c4-adc0-bcb791caa791"


    fun enableQaEndpoint(enable: Boolean) {
        var host: String
        var accountId: String
        if (enable) {
            host = QA_SERVER_DOMAIN
            accountId = QA_ACCOUNT_ID
        } else {
            host = PBS_SERVER_DOMAIN
            accountId = PROD_ACCOUNT_ID
        }
        setBidServerHost(host)
        PrebidMobile.setPrebidServerAccountId(accountId)
    }

    fun setBidServerHost(hostUrl: String) {
        val host = Host.CUSTOM
        host.hostUrl = hostUrl
        PrebidMobile.setPrebidServerHost(host)
    }
}