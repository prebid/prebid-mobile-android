package org.prebid.mobile.renderingtestapp.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.view_event_counter.view.*
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
        btnEvent.isEnabled = enabled
        updateCounter()
        super.setEnabled(enabled)
    }

    fun setText(text: String) {
        btnEvent.text = text
    }

    private fun updateCounter() {
        val deltaText: String = if (delta > 0) {
            "+${delta}"
        }
        else {
            delta.toString()
        }
        tvCounter.text = context.getString(R.string.event_counter, eventCount, deltaText)
    }

    private fun reflectAttrs(attrs: AttributeSet) {
        val typedArray = context
                .theme
                .obtainStyledAttributes(attrs, R.styleable.EventCounterView, 0, 0)
        val eventText = typedArray.getText(R.styleable.EventCounterView_android_text)
        btnEvent.text = eventText
    }
}