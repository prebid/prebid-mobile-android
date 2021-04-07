package com.openx.internal_test_app.utils

import android.util.Log
import com.openx.apollo.sdk.ApolloSettings
import java.lang.reflect.Field
import java.lang.reflect.Modifier

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
            ApolloSettings.setAccountId(accountId)
        }
    }

    private fun setBidServerHost(host: String) {
        try {
            val field = ApolloSettings::class.java.getDeclaredField("BID_SERVER_HOST")
            field.isAccessible = true

            val modifiersField: Field = Field::class.java.getDeclaredField("accessFlags")
            modifiersField.isAccessible = true
            modifiersField.setInt(field, field.modifiers and Modifier.FINAL.inv())

            field.set(field, host)
        }
        catch (throwable: Throwable) {
            Log.d("SourcePicker", "Failed to change bid server host")
            throwable.printStackTrace()
        }
    }
}