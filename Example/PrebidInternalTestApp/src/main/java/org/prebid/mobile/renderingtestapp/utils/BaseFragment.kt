package org.prebid.mobile.renderingtestapp.utils

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.prebid.mobile.renderingtestapp.MainActivity

abstract class BaseFragment : Fragment() {
    private val TAG = BaseFragment::class.java.simpleName
    abstract val layoutRes: Int
    private var title: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(layoutRes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated()")
        super.onViewCreated(view, savedInstanceState)
        initUi(view, savedInstanceState)
    }

    abstract fun initUi(view: View, savedInstanceState: Bundle?)

    protected fun setTitle(title: String) {
        this.title = title
        val mainActivity = requireActivity() as MainActivity
        mainActivity.setTitleString(title)
    }

    protected fun getTitle() = title
}