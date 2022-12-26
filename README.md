# WebView 接入文档

[toc]

***



## 1. 主要功能

### 调用js方法
```kotlin
// 更换数字人 type: 方法名 data:方法参数【数字人的索引】
mWebView.evaluateJavascript("SendMsgToWebGL('{\"type\":\"ChangeCharacter\",\"data\":\"${robot.abName}\"}')") {}

// 播报声音文件地址 type: 方法名 data:方法参数【声音文件URL地址】
mWebView.evaluateJavaScript("SendMsgToWebGL('{\"type\":\"AudioBroadcast\",\"data\":\"$audioUrl\"}')") {}

// 播报文本 type: 方法名 data:方法参数【播报的文本】
mWebView.evaluateJavaScript("SendMsgToWebGL('{\"type\":\"TextBroadcast\",\"data\":\"$text\"}')") {}

// 问答 文字问答 type: 方法名 data:方法参数【问题的文本】
mWebView.evaluateJavaScript("SendMsgToWebGL('{\"type\":\"TextAnswerMotion\",\"data\":\"$text\"}')") {}

// 问答 语音问答 type: 方法名 data:方法参数【录音流的BASE64编码】
mWebView.evaluateJavaScript("SendMsgToWebGL('{\"type\":\"AudioAnswerMotion\",\"data\":\"$audioEncode\"}')") {}
```

### 展示设置页面
```kotlin
/**
 * 展示设置页面
 */
private fun showSetting() {
    BottomDialog(this@MainActivity)
        .setResId(R.layout.dialog_bottom) { view, dialog ->
      		...    
        }
        .setOutSideCancelable(true)
        .show()
}
```

### 数字人更换
```kotlin
val listFragment = CharacterListFragment {
    Log.e(TAG, "character: $it")
    // 拿到数字人的地址后，调用js方法
    callJs(ChangeCharacter, it)
}
listFragment.show(supportFragmentManager, "characterFragment")
```

### 交互类型更换
```kotlin
/**
 * 展示交互类型更换
 */
private fun showChangType() {
    BottomDialog(this@MainActivity)
        .setResId(R.layout.dialog_type_change) { view, dialog ->
         	...
        }
        .setOutSideCancelable(true)
        .show()
}
```

### 关于示例
```kotlin
/**
 * 展示 关于示例
 */
private fun showExample() {
    BottomDialog(this@MainActivity)
        .setResId(R.layout.dialog_example)
        .setOutSideCancelable(true)
        .show()
}
```



##  2. 网络框架的初始化

主要作用是缓存网页中包含的图片、文件、字体等资源，在离线时也可正常使用。

```kotlin
setBaseUrl("http://www.xxx.com/")
setHttpClient(OkHelper.httpClient(applicationContext))
```

##  3. WebView的使用

###  WebView的预加载

主要作用是提前WebView的初始化，在使用时可以更快的获取WebView的实例。

```kotlin
WebViewManager.prepare(applicationContext)
```

###  WebView设置加载监听

```kotlin
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
```

###  WebView加载指定网页

```kotlin
mWebViewHelper.loadUrl("http://www.xxx.com/")
```

###  WebView中执行js函数

```kotlin
mWebView.evaluateJavascript("js function")
```

###  WebView的回收

```kotlin
WebViewManager.recycle(mWebView)
```


