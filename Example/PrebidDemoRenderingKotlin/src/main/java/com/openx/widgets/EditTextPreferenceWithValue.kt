package com.openx.widgets

import android.content.Context
import android.util.AttributeSet
import androidx.preference.EditTextPreference

open class EditTextPreferenceWithValue : EditTextPreference {

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context) : super(context)

    override fun getSummary(): CharSequence {
        return if (text != null) {
            text
        }
        else {
            ""
        }
    }
}