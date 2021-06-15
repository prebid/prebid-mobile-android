/*
 *    Copyright 2018-2021 Prebid.org, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

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