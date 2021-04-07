package com.openx.internal_test_app.plugplay.config

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.openx.apollo.sdk.ApolloSettings
import com.openx.internal_test_app.R

private const val ARG_CONFIGURATOR_MODE = "ARG_CONFIGURATOR_MODE"
private const val ARG_CONFIG_ID = "ARG_CONFIG_ID"
private const val ARG_WIDTH = "ARG_WIDTH"
private const val ARG_HEIGHT = "ARG_HEIGHT"

const val EXTRA_CONFIG_ID = "EXTRA_CONFIG_ID"
const val EXTRA_REFRESH_DELAY = "EXTRA_REFRESH_DELAY"
const val EXTRA_WIDTH = "EXTRA_WIDTH"
const val EXTRA_HEIGHT = "EXTRA_HEIGHT"

class AdConfiguratorDialogFragment : DialogFragment() {

    private lateinit var mode: AdConfiguratorMode
    private var configIdField: EditText? = null
    private var widthField: EditText? = null
    private var heightField: EditText? = null
    private var refreshDelayField: EditText? = null

    private lateinit var argConfigId: String
    private var argWidth: Int = 0
    private var argHeight: Int = 0

    companion object {
        fun newInstance(mode: AdConfiguratorMode, configId: String, width: Int, height: Int): AdConfiguratorDialogFragment {
            val args = Bundle()
            args.putString(ARG_CONFIGURATOR_MODE, mode.name)
            args.putString(ARG_CONFIG_ID, configId)
            args.putInt(ARG_WIDTH, width)
            args.putInt(ARG_HEIGHT, height)
            val fragment = AdConfiguratorDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        mode = AdConfiguratorMode.valueOf(requireArguments().getString(ARG_CONFIGURATOR_MODE, ""))
        arguments?.let {
            argConfigId = it.getString(ARG_CONFIG_ID, "")
            argWidth = it.getInt(ARG_WIDTH, 0)
            argHeight = it.getInt(ARG_HEIGHT, 0)
        }
        val view = prepareDialogView()

        return AlertDialog.Builder(activity)
                .setTitle(getString(R.string.dialog_configurator_title))
                .setView(view)
                .setOnCancelListener {
                    targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_CANCELED, null)
                }
                .create()
    }

    private fun prepareDialogView(): View {
        val resId = when (mode) {
            AdConfiguratorMode.BANNER -> R.layout.dialog_configurator_banner
            AdConfiguratorMode.INTERSTITIAL -> R.layout.dialog_configurator_interstitial
        }
        val dialogView = LayoutInflater.from(requireContext()).inflate(resId, null)
        configIdField = dialogView.findViewById(R.id.etConfigId)
        configIdField?.setText(argConfigId)
        widthField = dialogView.findViewById(R.id.etWidth)
        widthField?.setText(argWidth.toString())
        heightField = dialogView.findViewById(R.id.etHeight)
        heightField?.setText(argHeight.toString())
        if (mode == AdConfiguratorMode.BANNER) {
            refreshDelayField = dialogView.findViewById(R.id.etRefreshDelay)
            refreshDelayField?.setText((ApolloSettings.AUTO_REFRESH_DELAY_DEFAULT / 1000).toString())
        }

        dialogView.findViewById<Button>(R.id.btnLoad).setOnClickListener {
            onLoadClicked(dialogView)
        }

        return dialogView
    }

    private fun onLoadClicked(view: View) {
        targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK, getDialogData())
        hideKeyboardFrom(requireContext(), view)
        dismiss()
    }

    private fun getDialogData(): Intent {
        val intent = Intent()

        intent.putExtra(EXTRA_CONFIG_ID, configIdField?.text.toString())
        intent.putExtra(EXTRA_WIDTH, widthField?.text.toString().toInt())
        intent.putExtra(EXTRA_HEIGHT, heightField?.text.toString().toInt())
        if (mode == AdConfiguratorMode.BANNER) {
            intent.putExtra(EXTRA_REFRESH_DELAY, refreshDelayField?.text.toString().toInt())
        }

        return intent
    }

    private fun hideKeyboardFrom(context: Context, view: View) {
        val imm: InputMethodManager = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    enum class AdConfiguratorMode {
        BANNER,
        INTERSTITIAL
    }
}