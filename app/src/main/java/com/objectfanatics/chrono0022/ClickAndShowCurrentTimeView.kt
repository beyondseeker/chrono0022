package com.objectfanatics.chrono0022

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.objectfanatics.commons.android.view.instancestate.onRestoreInstanceState
import com.objectfanatics.commons.android.view.instancestate.onSaveInstanceState
import java.text.SimpleDateFormat
import java.util.*

class ClickAndShowCurrentTimeView : AppCompatTextView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        setOnClickListener { text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.JAPAN).format(Date()) }
    }

    @SuppressLint("MissingSuperCall")
    override fun onSaveInstanceState(): Parcelable = onSaveInstanceState(::getText as () -> CharSequence?)

    @SuppressLint("MissingSuperCall")
    override fun onRestoreInstanceState(state: Parcelable) = onRestoreInstanceState(state, { it: CharSequence? -> text = it })
}