package com.openx.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceViewHolder
import com.openx.internal_test_app.R

class CustomEditTextPreferenceWithValue : EditTextPreference {

    private var textValue: TextView? = null

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        layoutResource = R.layout.edit_text_preference_with_value
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        layoutResource = R.layout.edit_text_preference_with_value
    }

    constructor(context: Context) : super(context) {
        layoutResource = R.layout.edit_text_preference_with_value
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder?) {
        super.onBindViewHolder(holder)
        textValue = holder?.findViewById(R.id.pref_value) as TextView?
        textValue?.text = text
    }

    override fun setText(text: String) {
        super.setText(text)
        textValue?.text = this.text
    }
}