package org.prebid.mobile.prebidkotlindemo.activities

import android.os.Bundle
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.prebid.mobile.prebidkotlindemo.R
import org.prebid.mobile.prebidkotlindemo.databinding.ActivityDemoBinding
import org.prebid.mobile.prebidkotlindemo.testcases.TestCase
import org.prebid.mobile.prebidkotlindemo.testcases.TestCaseRepository

open class BaseAdActivity : AppCompatActivity() {

    protected val adWrapperView: ViewGroup
        get() = binding.frameAdWrapper

    private lateinit var binding: ActivityDemoBinding
    private var testCase: TestCase = TestCaseRepository.lastTestCase

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_demo)
        binding.tvTestCaseName.text = getText(testCase.titleStringRes)
    }

}