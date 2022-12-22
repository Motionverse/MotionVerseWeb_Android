package com.app.testwebviewforlink.activity

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.app.testwebviewforlink.JsCallAndroid
import com.app.testwebviewforlink.R
import com.app.testwebviewforlink.WebViewHelper
import com.app.testwebviewforlink.WebViewManager
import com.app.testwebviewforlink.audio.AudioManager
import com.app.testwebviewforlink.audio.VoiceButton
import com.app.testwebviewforlink.databinding.ActivityMainBinding
import com.app.testwebviewforlink.fragment.CharacterListFragment
import com.app.testwebviewforlink.http.setBaseUrl
import com.app.testwebviewforlink.http.setHttpClient
import com.app.testwebviewforlink.utils.OkHelper
import com.app.testwebviewforlink.utils.toBase64
import com.app.testwebviewforlink.utils.toast
import com.app.testwebviewforlink.view.BottomDialog

class MainActivity : AppCompatActivity() {

    // 文字播报
    private val TextBroadcast = "TextBroadcast"

    // 声音播报
    private val AudioBroadcast = "AudioBroadcast"

    // 更新数字人
    private val ChangeCharacter = "ChangeCharacter"

    // 文字问答
    private val TextAnswerMotion = "TextAnswerMotion"

    //声音问答
    private val AudioAnswerMotion = "AudioAnswerMotion"

    private val TAG = MainActivity::class.java.simpleName
    private lateinit var flContainer: FrameLayout
    private val mWebView by lazy { WebViewManager.obtain(this) }
    private val mWebViewHelper by lazy { WebViewHelper(mWebView) }
    private lateinit var bind: ActivityMainBinding

    private lateinit var mSp: SharedPreferences

    /**
     * 交互类型
     * 0: 播报
     * 1：问答
     */
    private val Key_Type_Interaction = "Key_Type_Interaction"

    /**
     * 数据
     * 0： 从EditText中获取
     * 1： 是录音文件
     */
    private var dataFrom = 0

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind.root)
        setBaseUrl("http://36.138.170.224:8060/")
        setHttpClient(OkHelper.httpClient(applicationContext))
        initWebView()
        requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 10)
        initBottomData()
        initClick()
    }

    private fun initClick() {
        bind.ivSetting.setOnClickListener {
            showSetting()
        }
        bind.tvSend.setOnClickListener {
            val keyType = mSp.getInt(Key_Type_Interaction, 0)
            val type = if (keyType == 0) TextBroadcast else TextAnswerMotion
            val str = bind.edtBottom.text.toString().trim()
            val hintStr = if (keyType == 0) "请输入播报文字" else "请输入问题文字"
            if (str.isNotEmpty()) {
                callJs(type, str)
                bind.edtBottom.setText("")
                toast("发送成功")
            } else {
                toast(hintStr)
            }

        }
    }

    /**
     * 展示设置页面
     */
    private fun showSetting() {
        BottomDialog(this@MainActivity)
            .setResId(R.layout.dialog_bottom) { view, dialog ->
                with(view) {
                    findViewById<TextView>(R.id.tvChangeCharacter).setOnClickListener {
                        toast("数字人更换")
                        dialog.dismiss()
                        val listFragment = CharacterListFragment {
                            Log.e(TAG, "character: $it")
                            callJs(ChangeCharacter, it)
                        }
                        listFragment.show(supportFragmentManager, "characterFragment")

                    }
                    findViewById<TextView>(R.id.tvChangeType).setOnClickListener {
                        toast("交互类型更换")
                        dialog.dismiss()
                        showChangType()
                    }
                    findViewById<TextView>(R.id.tvAbout).setOnClickListener {
                        toast("关于实例")
                        dialog.dismiss()
                        showExample()
                    }
                    findViewById<TextView>(R.id.tvCancel).setOnClickListener {
                        dialog.dismiss()
                    }
                }
            }
            .setOutSideCancelable(true)
            .show()
    }

    /**
     * 展示 关于示例
     */
    private fun showExample() {
        BottomDialog(this@MainActivity)
            .setResId(R.layout.dialog_example) { _, _ -> }
            .setOutSideCancelable(true)
            .show()
    }

    /**
     * 展示交互类型更换
     */
    private fun showChangType() {
        BottomDialog(this@MainActivity)
            .setResId(R.layout.dialog_type_change) { view, dialog ->
                with(view) {
                    val rbBroadcast = findViewById<RadioButton>(R.id.rbBroadcast)
                    val rbAnswer = findViewById<RadioButton>(R.id.rbAnswer)
                    var type = mSp.getInt(Key_Type_Interaction, 0)
                    if (type == 0) {
                        rbBroadcast.isChecked = true
                    } else {
                        rbAnswer.isChecked = true
                    }
                    val radioButton = findViewById<RadioGroup>(R.id.radioGroup)
                    radioButton.setOnCheckedChangeListener { group, checkedId ->
                        type = if (checkedId == R.id.rbAnswer) {
                            1
                        } else {
                            0
                        }
                    }
                    // 确定
                    findViewById<TextView>(R.id.tvSure).setOnClickListener {
                        mSp.edit().putInt(Key_Type_Interaction, type).apply()
                        setDisplayText(type)
                        dialog.dismiss()
                    }

                }
            }
            .setOutSideCancelable(true)
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 10) {
            var result = true
            for (item in grantResults) {
                if (item != PackageManager.PERMISSION_GRANTED) {
                    result = false
                    break
                }
            }
            if (result) {
                Log.e(TAG, "onRequestPermissionsResult: 有权限")
                AudioManager.initAudioRecord(this@MainActivity)
            }
        }
    }

    private fun callJs(type: String, data: String) {

        mWebView.evaluateJavascript(
            "SendMsgToWebGL('" +
                    "{\"type\":\"" + type + "\"," +
                    "\"data\":\"" + data + "\"}" +
                    "')"
        ) {
            Log.e("WebView", it)
        }
    }

    override fun onResume() {
        super.onResume()
        mWebViewHelper.onResume()
    }

    override fun onPause() {
        super.onPause()
        mWebViewHelper.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::flContainer.isInitialized)
            flContainer.removeAllViews()
        WebViewManager.recycle(mWebView)
        AudioManager.release(this)
    }

    override fun onBackPressed() {
        if (!mWebViewHelper.canGoBack()) {
            super.onBackPressed()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initBottomData() {
        // 键盘
        bind.ivKeyBoard.setOnClickListener {
            bind.tvBottom.visibility = View.GONE
            bind.edtBottom.visibility = View.VISIBLE
            bind.ivKeyBoard.visibility = View.GONE
            bind.ivVoice.visibility = View.VISIBLE
            bind.tvSend.visibility = View.VISIBLE
            dataFrom = 0
        }
        // 语音
        bind.ivVoice.setOnClickListener {
            bind.tvBottom.visibility = View.VISIBLE
            bind.edtBottom.visibility = View.GONE
            bind.ivKeyBoard.visibility = View.VISIBLE
            bind.ivVoice.visibility = View.GONE
            bind.tvSend.visibility = View.GONE
            dataFrom = 1
        }
        val type = mSp.getInt(Key_Type_Interaction, 0)
        setDisplayText(type)
        bind.tvBottom.setMotionEventListener(object : VoiceButton.MotionEventListener {
            override fun onDown() {
                val i = mSp.getInt(Key_Type_Interaction, 0)
                if (i == 1) {
                    // 开始录制
                    AudioManager.startRecord(this@MainActivity)
                }
            }

            override fun onUp() {
                val i = mSp.getInt(Key_Type_Interaction, 0)
                if (i == 1) {
                    // 结束录制
                    AudioManager.stopRecord(this@MainActivity)
                    val wavFile = AudioManager.getWavFile(this@MainActivity)
                    if (wavFile?.exists() == true) {
                        val toBase64 = wavFile.toBase64()
                        if (toBase64 != null) {
                            if (toBase64.isNotEmpty()) {
                                callJs(AudioAnswerMotion, toBase64)
                                toast("已经发送")
                            } else {
                                Log.e(TAG, "onUp: err: base64 is null")
                            }
                        }
                    }
                }
            }
        })
        bind.tvBottom.setOnClickListener {
            val i = mSp.getInt(Key_Type_Interaction, 0)
            if (i == 0) {
                val data =
                    "https://ds-model-tts.oss-cn-beijing.aliyuncs.com/temp/167144092926757110.wav"
                callJs(AudioBroadcast, data)
            }
        }
        // 平台介绍
        bind.tvPlatform.setOnClickListener {
            callJs(TextAnswerMotion,"平台介绍")
        }
        // 技术介绍
        bind.tvTechnology.setOnClickListener {
            callJs(TextAnswerMotion,"技术介绍")
        }
        // 业务介绍
        bind.tvBusiness.setOnClickListener {
            callJs(TextAnswerMotion,"业务介绍")
        }

    }

    private fun setDisplayText(type: Int) {
        if (type == 0) {
            bind.edtBottom.hint = "请输入播报文字"
            bind.tvBottom.text = "点击播报语音文件"
            bind.answerLayout.visibility = View.GONE
        } else {
            bind.edtBottom.hint = "请输入问题文字"
            bind.tvBottom.text = "长按说话"
            bind.answerLayout.visibility = View.VISIBLE
        }
    }

    private fun initWebView() {

        WebViewManager.prepare(applicationContext)
        bind.flContainer.addView(mWebView)
        mWebViewHelper.setOnPageChangedListener(object : WebViewHelper.OnPageChangedListener {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                Log.i(TAG, "onPageStarted $url")
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                Log.i(TAG, "onPageFinished $url")
            }

            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                Log.i(TAG, "onProgressChanged $newProgress")
            }
        })
        mWebView.addJavascriptInterface(JsCallAndroid(), "android")
//        mWebViewHelper.loadUrl("file:///android_asset/test.html")
//        mWebViewHelper.loadUrl("http://36.138.170.224:8060/")
//        mWebViewHelper.loadUrl("http://36.138.170.224:8060/h5/index.html")
//        mWebViewHelper.loadUrl("http://36.138.170.224:8060/avatarx/")
//        mWebViewHelper.loadUrl("https://demo.deepscience.cn/poc/index.html")
        mWebViewHelper.loadUrl("https://avatar.deepscience.cn/v1/index.html?code=xVNEJ9ovjQ7EmOlnYO4TlRTB17zMOZOpaNqDyhZLU6BS5oKbvTZvhUc9YqlFaSOe20ooP3VN446VoqK3OoazZyBG4JV4FL+UQc1use3Xlu/deW5WLMq/25h0eOiV4XKk")

        mSp = getSharedPreferences("motionverse", Context.MODE_PRIVATE)

    }
}