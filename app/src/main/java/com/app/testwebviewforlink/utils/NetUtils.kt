package com.app.testwebviewforlink.utils

import android.content.Context
import android.util.Log
import com.app.testwebviewforlink.http.CharacterBean
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.IOException

class NetUtils {

    companion object {
        private val TAG = "NetUtils"
        private val client = OkHttpClient()

        fun getCharacterList(formatData: FormatData<List<CharacterBean>>) {
            get("https://demo.deepscience.cn/poc/StreamingAssets/config.json",
                object : NetCallback {
                    override fun onFail(msg: String) {
                        Log.e(TAG, "onFail: $msg")
                    }

                    override fun onSuccess(data: String) {
                        val list = Gson().fromJson<List<CharacterBean>>(data,
                            object : TypeToken<List<CharacterBean>>() {}.type
                        )
                        formatData.onResult(list)
                    }

                })
        }

        @OptIn(DelicateCoroutinesApi::class)
        private fun get(url: String, callback: NetCallback) {
            val req = Request.Builder()
                .url(url)
                .build()
            client.newCall(req).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e(TAG, "onFailure: err:${e.message}")
                    GlobalScope.launch(Dispatchers.Main) {
                        callback.onFail(e.message!!)
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val string = response.body()?.string()
                    if (string?.isNotEmpty() == true) {
                        GlobalScope.launch(Dispatchers.Main) {
                            callback.onSuccess(string)
                        }
                    }
                }

            })
        }
    }

    interface FormatData<T> {
        fun onResult(t: T)
    }

    interface NetCallback {
        fun onFail(msg: String)
        fun onSuccess(data: String)
    }

}