package com.app.testwebviewforlink

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.MutableContextWrapper
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Looper
import android.view.ViewGroup
import android.webkit.*
import androidx.annotation.RequiresApi
import com.app.testwebviewforlink.http.HttpRequest
import com.app.testwebviewforlink.http.download
import kotlinx.coroutines.runBlocking
import okio.ByteString
import java.io.File

class WebViewHelper(private val webView: WebView) {

    private var onPageChangedListener: OnPageChangedListener? = null

    private var injectState = false
    private var injectVConsole = false
    private var originalUrl = "about:blank"

    init {
        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (view != null && url != null) {
                    if (!url.startsWith("http")) {
                        try {
                            view.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        return true
                    }
                }
                return super.shouldOverrideUrlLoading(view, url)
            }

            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                if (view != null && request != null && request.url != null) {
                    if ("http" != request.url.scheme && "https" != request.url.scheme) {
                        try {
                            view.context.startActivity(Intent(Intent.ACTION_VIEW, request.url))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        return true
                    }
                }
                return false
            }

            override fun shouldInterceptRequest(
                view: WebView?,
                url: String?
            ): WebResourceResponse? {
                if (view != null && url != null) {
                    when {
                        canCacheResource(url) -> {
                            return cacheResourceRequest(view.context, url)
                        }
                    }
                }
                return super.shouldInterceptRequest(view, url)
            }

            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                if (view != null && request != null) {
                    when {
                        canCacheResource(request) -> {
                            return cacheResourceRequest(view.context, request)
                        }
                    }
                }
                return super.shouldInterceptRequest(view, request)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                onPageChangedListener?.onPageStarted(view, url, favicon)
                injectState = false
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                onPageChangedListener?.onPageFinished(view, url)
                injectState = false
            }
        }
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                onPageChangedListener?.onProgressChanged(view, newProgress)
            }
        }
        webView.setDownloadListener { url, _, _, _, _ ->
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                intent.addCategory(Intent.CATEGORY_BROWSABLE)
                webView.context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun evaluateJavascript(js: String, callback: (String) -> Unit): WebViewHelper {
        webView.evaluateJavascript(js, callback)
        return this
    }

    fun injectVConsole(inject: Boolean): WebViewHelper {
        injectVConsole = inject
        return this
    }

    fun setOnPageChangedListener(onPageChangedListener: OnPageChangedListener?): WebViewHelper {
        this.onPageChangedListener = onPageChangedListener
        return this
    }

    fun canGoBack(): Boolean {
        val canBack = webView.canGoBack()
        if (canBack) webView.goBack()
        return canBack
    }

    fun canGoForward(): Boolean {
        val canForward = webView.canGoForward()
        if (canForward) webView.goForward()
        return canForward
    }

    fun loadUrl(url: String?): WebView {
        if (!url.isNullOrBlank()) {
            webView.loadUrl(url)
            originalUrl = url
        }
        return webView
    }

    fun reload() {
        webView.reload()
    }

    fun onResume() {
        webView.onResume()
    }

    fun onPause() {
        webView.onPause()
    }

    fun onDestroyView() {
        onPageChangedListener = null
        WebViewManager.recycle(webView)
    }

    private fun canCacheResource(url: String): Boolean {
        val extension = getExtensionFromUrl(url)
//        Log.e("WebView can cache", "$extension , $url")
        return extension == "ico" || extension == "bmp" || extension == "gif"
                || extension == "jpeg" || extension == "jpg" || extension == "png"
                || extension == "svg" || extension == "webp" || extension == "css"
                || extension == "js" || extension == "json" || extension == "eot"
                || extension == "otf" || extension == "ttf" || extension == "woff"
                || extension == "unityweb" || extension == "wav" || extension == "gz" || extension == "fbx"
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun canCacheResource(webRequest: WebResourceRequest): Boolean {
        val url = webRequest.url.toString()
        return canCacheResource(url)
    }

    private fun cacheResourceRequest(context: Context, url: String): WebResourceResponse? {
        try {
            val cachePath = context.cacheDir.toString() + "/web_cache"
            val cacheFileDir = File(cachePath)
            if (!cacheFileDir.exists()) {
                cacheFileDir.mkdirs()
            }
            val filePathName = cachePath + File.separator + ByteString.encodeUtf8(url).md5().hex()
            val file = File(filePathName)
//            Log.e("cacheResourceRequest", "${file.exists()} $url $cachePath")
            if (!file.exists() || !file.isFile) {
                runBlocking {
                    download(HttpRequest(url), filePathName)
                }
            }
            if (file.exists() && file.isFile) {
                return WebResourceResponse(
                    getMimeTypeFromUrl(url), "UTF-8", file.inputStream()
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun cacheResourceRequest(
        context: Context,
        webRequest: WebResourceRequest
    ): WebResourceResponse? {
        try {
            val url = webRequest.url.toString()
            val cachePath = context.cacheDir.toString() + "/web_cache"
            val cacheFileDir = File(cachePath)
            if (!cacheFileDir.exists()) {
                cacheFileDir.mkdirs()
            }
            val filePathName = cachePath + File.separator + ByteString.encodeUtf8(url).md5().hex()
            val file = File(filePathName)
//            Log.e("cacheResourceRequest", "${file.exists()} $url $cachePath")
            if (!file.exists() || !file.isFile) {
                runBlocking {
                    download(HttpRequest(url).apply {
                        webRequest.requestHeaders.forEach { putHeader(it.key, it.value) }
                    }, filePathName)
                }
            }
            if (file.exists() && file.isFile) {
                val webResourceResponse = WebResourceResponse(
                    getMimeTypeFromUrl(url), "UTF-8", file.inputStream()
                )
                webResourceResponse.responseHeaders = mapOf("access-control-allow-origin" to "*")
                return webResourceResponse
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun getExtensionFromUrl(url: String): String {
        try {
            if (url.isNotBlank() && url != "null") {
                return MimeTypeMap.getFileExtensionFromUrl(url)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    private fun getMimeTypeFromUrl(url: String): String {
        try {
            val extension = getExtensionFromUrl(url)
            if (extension.isNotBlank() && extension != "null") {
                if (extension == "json") {
                    return "application/json"
                }
                return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "*/*"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "*/*"
    }

    interface OnPageChangedListener {
        fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?)
        fun onPageFinished(view: WebView?, url: String?)
        fun onProgressChanged(view: WebView?, newProgress: Int)
    }
}

@SuppressLint("SetJavaScriptEnabled")
class WebViewManager private constructor() {

    companion object {
        @Volatile
        private var INSTANCE: WebViewManager? = null

        private fun instance() = INSTANCE ?: synchronized(this) {
            INSTANCE ?: WebViewManager().also { INSTANCE = it }
        }

        fun prepare(context: Context) {
            instance().prepare(context)
        }

        fun obtain(context: Context): WebView {
            return instance().obtain(context)
        }

        fun recycle(webView: WebView) {
            instance().recycle(webView)
        }

        fun destroy() {
            instance().destroy()
        }
    }

    private val webViewCache: MutableList<WebView> = ArrayList(1)

    private fun create(context: Context): WebView {
        val webView = WebView(context)
        webView.setBackgroundColor(Color.TRANSPARENT)
        webView.overScrollMode = WebView.OVER_SCROLL_NEVER
        val webSetting = webView.settings
        webSetting.allowFileAccess = true
        webSetting.setAppCacheEnabled(true)
        webSetting.cacheMode = WebSettings.LOAD_DEFAULT
        webSetting.domStorageEnabled = true
        webSetting.setGeolocationEnabled(true)
        webSetting.javaScriptEnabled = true
        webSetting.loadWithOverviewMode = true
        webSetting.setSupportZoom(true)
        webSetting.displayZoomControls = false
        webSetting.useWideViewPort = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSetting.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
        }
        return webView
    }

    fun prepare(context: Context) {
        if (webViewCache.isEmpty()) {
            Looper.myQueue().addIdleHandler {
                webViewCache.add(create(MutableContextWrapper(context)))
                false
            }
        }
    }

    fun obtain(context: Context): WebView {
        if (webViewCache.isEmpty()) {
            webViewCache.add(create(MutableContextWrapper(context)))
        }
        val webView = webViewCache.removeFirst()
        val contextWrapper = webView.context as MutableContextWrapper
        contextWrapper.baseContext = context
        webView.clearHistory()
        webView.resumeTimers()
        return webView
    }

    fun recycle(webView: WebView) {
        try {
            webView.stopLoading()
            webView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null)
            webView.clearHistory()
            webView.pauseTimers()
            webView.webChromeClient = null
//            webView.webViewClient = null
            val parent = webView.parent
            if (parent != null) {
                (parent as ViewGroup).removeView(webView)
            }
            val contextWrapper = webView.context as MutableContextWrapper
            contextWrapper.baseContext = webView.context.applicationContext
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (!webViewCache.contains(webView)) {
                webViewCache.add(webView)
            }
        }
    }

    fun destroy() {
        try {
            webViewCache.forEach {
                it.removeAllViews()
                it.destroy()
                webViewCache.remove(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}