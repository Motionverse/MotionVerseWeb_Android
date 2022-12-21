package com.app.testwebviewforlink.fragment

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.annotation.LayoutRes
import androidx.fragment.app.DialogFragment

abstract class BaseDialogFragment : DialogFragment() {
    private var onDismissListener: DialogInterface.OnDismissListener? = null
    var isNeedFullScreen: Boolean = false
    var isNeedViewBg: Boolean = true
    private var container: ViewGroup? = null

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        this.container = container

        return inflater.inflate(getContentViewId(), container, false)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.onDismiss(dialog)
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(getDialogWidth(), getDialogHeight())
    }

    @LayoutRes
    abstract fun getContentViewId(): Int

    open fun getDialogWidth(): Int = if (isNeedFullScreen) {
        getScreenWidth()
    } else {
        wPercentDp(740)
    }


    open fun getDialogHeight(): Int = if (isNeedFullScreen) {
        getScreenHeight()
    } else {
        hPercentDp(600)
    }

    private fun wPercentDp(value: Int): Int {
        return getScreenWidth() * value / 1024
    }

    private fun hPercentDp(value: Int): Int {
        return getScreenHeight() * value / 768
    }

    fun setOnDismissListener(listener: DialogInterface.OnDismissListener) {
        onDismissListener = listener
    }

    private fun getScreenWidth(): Int {
        return requireContext().resources.displayMetrics.widthPixels
    }


    private fun getScreenHeight(): Int {
        return requireContext().resources.displayMetrics.heightPixels
    }
}