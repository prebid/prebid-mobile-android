package org.prebid.mobile.prebidkotlindemo.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import org.prebid.mobile.prebidkotlindemo.R
import org.prebid.mobile.prebidkotlindemo.databinding.ActivitySettingsBinding
import org.prebid.mobile.prebidkotlindemo.utils.Settings

class SettingsActivity : AppCompatActivity() {

    companion object {
        fun getIntent(context: Context) = Intent(context, SettingsActivity::class.java)
    }

    private val settings = Settings.get()
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings)
        supportActionBar?.title = getString(R.string.settings)

        initViews()
    }

    override fun onStart() {
        super.onStart()
        initListeners()
    }

    private fun initViews() {
        binding.etRefreshTime.setText(settings.refreshTimeSeconds.toString())
    }

    private fun initListeners() {
        binding.etRefreshTime.addTextChangedListener {
            checkAllFields()
        }
    }

    private fun checkAllFields() {
        hideError()
        binding.apply {
            val value = etRefreshTime.text?.toString()?.toIntOrNull() ?: 0
            if (value < 30 || value > 120) {
                showError("Refresh time must be in range from 30 to 120 seconds")
                settings.refreshTimeSeconds = 30
                return
            } else {
                settings.refreshTimeSeconds = value
            }
        }
    }

    private fun showError(error: String) {
        binding.tvError.text = error
    }

    private fun hideError() {
        binding.tvError.text = ""
    }

}