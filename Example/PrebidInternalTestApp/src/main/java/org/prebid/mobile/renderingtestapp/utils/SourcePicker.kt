package org.prebid.mobile.renderingtestapp.utils

import org.prebid.mobile.rendering.bidding.enums.Host
import org.prebid.mobile.rendering.sdk.PrebidRenderingSettings

object SourcePicker {

    private const val MOCK_SERVER_DOMAIN = "https://10.0.2.2:8000/openrtb2/auction"

    private const val PROD_SERVER_DOMAIN = "https://prebid.openx.net/openrtb2/auction"
    private const val PROD_ACCOUNT_ID = "0689a263-318d-448b-a3d4-b02e8a709d9d"

    private const val QA_SERVER_DOMAIN = "https://prebid.qa.openx.net/openrtb2/auction"
    private const val QA_ACCOUNT_ID = "08efa38c-b6b4-48c4-adc0-bcb791caa791"

    var useMockServer = false
        set(value) {
            field = value
            val host = if (value)
                MOCK_SERVER_DOMAIN
            else
                PROD_SERVER_DOMAIN
            setBidServerHost(host)
        }

    fun enableQaEndpoint(enable: Boolean) {
        if (!useMockServer) {
            var host = ""
            var accountId = ""
            if (enable) {
                host = QA_SERVER_DOMAIN
                accountId = QA_ACCOUNT_ID
            }
            else {
                host = PROD_SERVER_DOMAIN
                accountId = PROD_ACCOUNT_ID
            }
            setBidServerHost(host)
            PrebidRenderingSettings.setAccountId(accountId)
        }
    }

    private fun setBidServerHost(hostUrl: String) {
        val host = Host.CUSTOM
        host.hostUrl = hostUrl
        PrebidRenderingSettings.setBidServerHost(host)
    }
}