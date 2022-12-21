package com.app.testwebviewforlink.audio

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent

class VoiceButton : androidx.appcompat.widget.AppCompatTextView {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attr: AttributeSet?) : this(context, attr, 0)
    constructor(context: Context, attr: AttributeSet?, def: Int) : super(context, attr, def)

    private val TAG = "VoiceButon"
    private var listener: MotionEventListener? = null
    fun setMotionEventListener(listener: MotionEventListener) {
        this.listener = listener
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.action?.let {
            when (it) {
                // 按下
                MotionEvent.ACTION_DOWN -> {
                    Log.e(TAG, "onTouchEvent: 按下")
                    listener?.onDown()
                }
                // 抬起
                MotionEvent.ACTION_UP -> {
                    Log.e(TAG, "onTouchEvent: 抬起")
                    listener?.onUp()
                }
                else -> {

                }
            }
        }

        return super.onTouchEvent(event)
    }

    interface MotionEventListener {
        fun onDown()
        fun onUp()
    }

}