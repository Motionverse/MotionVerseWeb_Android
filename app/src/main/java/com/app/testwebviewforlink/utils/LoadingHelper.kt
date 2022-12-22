package com.app.testwebviewforlink.utils

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.widget.TextView
import com.app.testwebviewforlink.R
import com.app.testwebviewforlink.databinding.DialogLoadingBinding

@SuppressLint("StaticFieldLeak")
object LoadingHelper {

    private var loadingDialog: Dialog? = null
    private var msgView: TextView? = null
    private lateinit var bind: DialogLoadingBinding

    @SuppressLint("InflateParams")
    fun showDialog(
        context: Context,
        msg: String = "请稍等...",
        cancelBack: () -> Unit = {},
        dismissCallBack: () -> Unit = {},
        cancelable: Boolean = true
    ) {
        if (loadingDialog == null) {
            val inflater = LayoutInflater.from(context)
            val view = inflater.inflate(R.layout.dialog_loading, null)
            bind = DialogLoadingBinding.bind(view)
            bind.idDialogLoadingMsg.text = msg
            loadingDialog = Dialog(context, R.style.dialog)
            loadingDialog?.apply {
                window!!.setBackgroundDrawableResource(android.R.color.transparent)
                window!!.decorView.setBackgroundColor(Color.TRANSPARENT)
                setOnCancelListener {
                    cancelBack.invoke()
                }
                setOnDismissListener { dismissCallBack.invoke() }
            }
            loadingDialog!!.apply {
                setContentView(view)
                setCancelable(cancelable)
                show()
            }
        } else {
            loadingDialog?.setCancelable(cancelable)
            msgView?.text = msg
        }
    }

    fun dismiss() {
        loadingDialog?.apply {
            if (isShowing) {
                dismiss()
            }
            msgView = null
            loadingDialog = null
        }
    }

}