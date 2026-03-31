package org.prebid.mobile.prebidnextgendemo.activities

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.prebid.mobile.prebidnextgendemo.R
import org.prebid.mobile.prebidnextgendemo.databinding.ActivityDemoBinding
import org.prebid.mobile.prebidnextgendemo.testcases.TestCase
import org.prebid.mobile.prebidnextgendemo.testcases.TestCaseRepository
import org.prebid.mobile.prebidnextgendemo.utils.BaseEvents
import org.prebid.mobile.prebidnextgendemo.utils.Settings

open class BaseAdActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_AD_UNIT_ID = "adUnitId"
        const val EXTRA_CONFIG_ID = "configId"
        const val EXTRA_WIDTH = "width"
        const val EXTRA_HEIGHT = "height"
        const val EXTRA_CUSTOM_FORMAT_ID = "customFormatId"
    }

    protected val TAG = "ExampleActivity"

    /**
     * ViewGroup container for any ad view.
     */
    protected val adWrapperView: ViewGroup
        get() = binding.frameAdWrapper

    /**
     * Root view for wiring EventCounterView instances via BaseEvents.
     */
    protected val rootView: View
        get() = binding.root

    /**
     * Seconds for auto-refreshing any banner ad.
     */
    protected val refreshTimeSeconds: Int
        get() = Settings.get().refreshTimeSeconds

    protected val events by lazy { Events(rootView) }

    private lateinit var binding: ActivityDemoBinding
    private var testCase: TestCase = TestCaseRepository.lastTestCase

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_demo)
        binding.tvTestCaseName.text = getText(testCase.titleStringRes)
    }

    class Events(rootView: View) : BaseEvents(rootView) {
        fun loaded(b: Boolean) = enable(R.id.btnAdLoaded, b)
        fun failed(b: Boolean) = enable(R.id.btnAdFailed, b)
        fun displayed(b: Boolean) = enable(R.id.btnAdDisplayed, b)
        fun clicked(b: Boolean) = enable(R.id.btnAdClicked, b)
        fun closed(b: Boolean) = enable(R.id.btnAdClosed, b)
        fun userEarnedReward(b: Boolean) = enable(R.id.btnUserEarnedReward, b)
    }

}