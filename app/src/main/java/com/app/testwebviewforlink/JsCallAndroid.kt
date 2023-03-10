package com.app.testwebviewforlink

import android.util.Log
import android.webkit.JavascriptInterface

class JsCallAndroid(val callBack: (msg: String) -> Unit) {

    private val TAG = "JsCallAndroid"

    @JavascriptInterface
    fun print(msg: String) {
        Log.e(TAG, msg)
    }

    @JavascriptInterface
    fun GetMsgFromAPP(msg: String) {
        Log.e(TAG, "GetMsgFromAPP $msg")
    }

    @JavascriptInterface
    fun SendMsgToAPP(msg: String) {
        Log.e(TAG, "SendMsgToAPP $msg")
        callBack.invoke(msg)
    }

}