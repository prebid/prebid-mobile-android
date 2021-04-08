package org.prebid.mobile.renderingtestapp.widgets

import android.content.Context
import android.util.AttributeSet
import androidx.preference.EditTextPreference

class IntegerEditTextPreferenceWithValue : EditTextPreference {
    private val INVALID_RESULT = -1

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context) : super(context)

    override fun getPersistedString(defaultReturnValue: String?): String {
        val persistedInt = getPersistedInt(INVALID_RESULT)
        return if (persistedInt == INVALID_RESULT) {
            ""
        }
        else {
            persistedInt.toString()
        }
    }

    override fun persistString(value: String?): Boolean {
        return persistInt(getIntValue(value))
    }

    override fun getText(): String {
        return getPersistedString("")
    }

    override fun getSummary(): CharSequence {
        return getPersistedString("")
    }

    private fun getIntValue(value: String?): Int {
        return try {
            value?.toInt() ?: INVALID_RESULT
        }
        catch (ex: NumberFormatException) {
            INVALID_RESULT
        }
    }
}