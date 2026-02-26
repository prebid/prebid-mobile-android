package org.prebid.mobile.prebidnextgendemo.activities

import android.os.Bundle
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.prebid.mobile.prebidnextgendemo.R
import org.prebid.mobile.prebidnextgendemo.databinding.ActivityDemoBinding
import org.prebid.mobile.prebidnextgendemo.testcases.TestCase
import org.prebid.mobile.prebidnextgendemo.testcases.TestCaseRepository
import org.prebid.mobile.prebidnextgendemo.utils.Settings

open class BaseAdActivity : AppCompatActivity() {

    protected val TAG = "ExampleActivity"

    /**
     * ViewGroup container for any ad view.
     */
    protected val adWrapperView: ViewGroup
        get() = binding.frameAdWrapper

    /**
     * Seconds for auto-refreshing any banner ad.
     */
    protected val refreshTimeSeconds: Int
        get() = Settings.get().refreshTimeSeconds

    private lateinit var binding: ActivityDemoBinding
    private var testCase: TestCase = TestCaseRepository.lastTestCase

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_demo)
        binding.tvTestCaseName.text = getText(testCase.titleStringRes)
    }

}