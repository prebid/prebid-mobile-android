package org.prebid.mobile.prebidnextgendemo.testcases

import android.app.Activity
import android.os.Bundle
import androidx.annotation.StringRes

data class TestCase(
    @StringRes val titleStringRes: Int,
    val adFormat: AdFormat,
    val integrationKind: IntegrationKind,
    val activity: Class<out Activity>,
    val extras: Bundle? = null,
)
