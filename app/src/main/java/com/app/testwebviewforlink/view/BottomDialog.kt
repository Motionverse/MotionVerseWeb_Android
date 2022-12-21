package com.app.testwebviewforlink.view

import android.app.Activity
import android.app.Dialog
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import com.app.testwebviewforlink.R

typealias OnDismissListener = () -> Unit
typealias OnViewInit = (View, BottomDialog) -> Unit


class BottomDialog(private val context: Activity, private val isDialog: Boolean = false) {

    private val inAnim: Animation by lazy {
        AnimationUtils.loadAnimation(context, R.anim.dialog_slide_in_bottom)
    }

    private val outAnim: Animation by lazy {
        AnimationUtils.loadAnimation(context, R.anim.dialog_slide_out_bottom)
    }

    private lateinit var decorView: ViewGroup
    private lateinit var rootView: ViewGroup
    private lateinit var contentContainer: ViewGroup
    private var dialogView: ViewGroup? = null
    private var mDialog: Dialog? = null

    private var isViewShowing = false
    private var isDismissing = false
    private var isInited = false
    private var isDialogCancelable = false

    private var dismissListener: OnDismissListener? = null
    private var viewInit: OnViewInit? = null
    private var resId: Int = 0

    init {
        initRoot()
    }

    private fun initRoot() {

        if (isDialog) run {
            //如果是对话框模式
            dialogView = LayoutInflater.from(context)
                .inflate(R.layout.dialog_root_layout, null, false) as ViewGroup
            //设置界面的背景为透明
            dialogView!!.setBackgroundResource(R.drawable.shape_dialog_bg)
            //这个是真正要加载选择器的父布局
            contentContainer = dialogView!!.findViewById(R.id.content_container)
        } else {
            //decorView是activity的根View,包含 contentView 和 titleView
            decorView = context.window.decorView.findViewById(android.R.id.content)
            //将控件添加到decorView中
            rootView = (LayoutInflater.from(context)
                .inflate(R.layout.layout_bottom_dialog, decorView, false) as ViewGroup).apply {
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
            //这个是真正要加载时间选取器的父布局
            contentContainer = (rootView.findViewById(R.id.content_container) as ViewGroup).apply {
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM
                )
            }
        }
    }

    private fun initResource() {
        if (isInited) return
        viewInit?.invoke(LayoutInflater.from(context).inflate(resId, contentContainer), this)
        isInited = true
    }

    private fun createDialog() {
        val temp = dialogView
        if (temp != null) {
            initResource()
            //设置对话框 默认左右间距屏幕30
//            val params = FrameLayout.LayoutParams(
//                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM)
//            params.leftMargin = 30
//            params.rightMargin = 30
//            contentContainer.layoutParams = params

            mDialog ?: Dialog(context, R.style.bottom_dialog).apply {
                mDialog = this
                setCancelable(isDialogCancelable)//不能点外面取消,也不能点back取消
                setContentView(temp)
                window?.apply {
                    setGravity(Gravity.CENTER)//可以改成Bottom
                    setOnDismissListener {
                        dismissListener?.invoke()
                    }
                }
            }
        }
    }

    /**
     * 添加View到根视图
     */
    fun show() {
        if (isDialog) {
            //创建对话框
            if (mDialog == null) createDialog()
            //给背景设置点击事件,这样当点击内容以外的地方会关闭界面
            dialogView?.setOnClickListener { dismiss() }
            mDialog?.show()
        } else {
            if (isShowing()) {
                return
            }
            isViewShowing = true
            initResource()
            onAttached(rootView)
            rootView.requestFocus()
        }
    }

    private fun onAttached(view: View) {
        decorView.addView(view)
        contentContainer.startAnimation(inAnim)
    }


    private fun isShowing(): Boolean {
        return rootView.parent != null || isViewShowing

    }

    fun dismiss(withAnim: Boolean = true) {
        if (isDismissing) {
            return
        }

        if (isDialog) {
            mDialog?.dismiss()
            isDismissing = true
        } else {
            if (withAnim) {
                outAnim.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {

                    }

                    override fun onAnimationEnd(animation: Animation) {
                        dismissImmediately()
                    }

                    override fun onAnimationRepeat(animation: Animation) {

                    }
                })
                contentContainer.startAnimation(outAnim)
                isDismissing = true
            } else {
                isDismissing = true
                dismissImmediately()
            }
        }
    }

    private fun dismissImmediately() {
        decorView.post {
            //从根视图移除
            decorView.removeView(rootView)
            isViewShowing = false
            isDismissing = false
            dismissListener?.invoke()
        }
    }


    /**
     * 作为decoreView设置返回取消
     */
    fun setKeyBackCancelable(isCancelable: Boolean): BottomDialog {
        rootView.isFocusable = isCancelable
        rootView.isFocusableInTouchMode = isCancelable
        if (isCancelable) {
            rootView.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_BACK && event.action == MotionEvent.ACTION_DOWN && isShowing()) {
                    dismiss()
                    true
                } else {
                    false
                }
            }
        } else {
            rootView.setOnKeyListener(null)
        }
        return this
    }


    /**
     * 作为decoreView设置父布局
     */
    fun setParent(view: ViewGroup): BottomDialog {
        decorView = view
        return this
    }

//==========================公用方法=======================

    fun setResId(resID: Int, init: OnViewInit): BottomDialog {
        resId = resID
        viewInit = init
        return this
    }

    fun setOutSideCancelable(isCancelable: Boolean): BottomDialog {
        if (isDialog) {
            this@BottomDialog.isDialogCancelable = isCancelable
        } else {
            val view = rootView.findViewById<View>(R.id.outmost_container)
            if (isCancelable) {
                view.setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        dismiss()
                    }
                    false
                }
            } else {
                view.setOnTouchListener(null)
            }
        }
        return this
    }

    fun setOnDismissListener(onDismissListener: OnDismissListener): BottomDialog {
        dismissListener = onDismissListener
        return this
    }

}