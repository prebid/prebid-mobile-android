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
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import org.prebid.mobile.renderingtestapp.R

class EventCounterView : ConstraintLayout {

    private var eventCount = 0
    private var delta = 0

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        reflectAttrs(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        reflectAttrs(attrs)
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_event_counter, this, true)
        setOnClickListener {
            isEnabled = false
        }
        isEnabled = false
    }

    override fun setEnabled(enabled: Boolean) {
        if (enabled) {
            eventCount++
            delta++
        }
        else {
            delta = 0
        }
        findViewById<TextView>(R.id.btnEvent).isEnabled = enabled
        updateCounter()
        super.setEnabled(enabled)
    }

    fun setText(text: String) {
        findViewById<Button>(R.id.btnEvent).text = text
    }

    private fun updateCounter() {
        val deltaText: String = if (delta > 0) {
            "+${delta}"
        }
        else {
            delta.toString()
        }
        findViewById<TextView>(R.id.tvCounter).text = context.getString(R.string.event_counter, eventCount, deltaText)
    }

    private fun reflectAttrs(attrs: AttributeSet) {
        val typedArray = context
                .theme
                .obtainStyledAttributes(attrs, R.styleable.EventCounterView, 0, 0)
        val eventText = typedArray.getText(R.styleable.EventCounterView_android_text)
        findViewById<TextView>(R.id.btnEvent).text = eventText
    }
}