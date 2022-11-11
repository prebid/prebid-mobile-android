package org.prebid.mobile.prebidkotlindemo.testcases

import android.app.Activity

data class TestCase(
    val adFormat: AdFormat,
    val integrationKind: IntegrationKind,
    val description: String,
    val activity: Class<out Activity>
) {

    val fullName = "${integrationKind.adServer} ${adFormat.description} $description"

}
