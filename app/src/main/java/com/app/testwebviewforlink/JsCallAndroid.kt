package com.app.testwebviewforlink

import android.util.Log
import android.webkit.JavascriptInterface

class JsCallAndroid {

    private val tag = javaClass.simpleName

    @JavascriptInterface
    fun print(msg: String) {
        Log.i(tag, msg)
    }

    @JavascriptInterface
    fun GetMsgFromAPP(msg: String) {
        Log.i(tag, "GetMsgFromAPP $msg")
    }

    @JavascriptInterface
    fun SendMsgToAPP(msg: String) {
        Log.i(tag, "SendMsgToAPP $msg")
    }

}