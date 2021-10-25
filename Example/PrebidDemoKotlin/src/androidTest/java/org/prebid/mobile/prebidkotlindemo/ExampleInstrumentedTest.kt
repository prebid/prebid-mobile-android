package org.prebid.mobile.prebidkotlindemo

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(androidx.test.ext.junit.runners.AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("org.prebid.mobile.prebidkotlindemo", context.packageName)
    }
}
