package com.xiaobo.proxysocks5

import android.content.Intent
import android.graphics.Bitmap
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.HttpAuthHandler
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.ProxyConfig
import androidx.webkit.ProxyController
import androidx.webkit.WebViewFeature
import com.xiaobo.proxysocks5.constants.AppConstant
import java.util.concurrent.Executor


class MainActivity : AppCompatActivity() {
    val wvView by lazy { findViewById<WebView>(R.id.wv_view) }
    private val TAG:String = "MainActivity"
    private var isProxy = AppConstant.defProxyOff
    private val uri = AppConstant.wvUri
    private var proxyUri = "socks5://8.134.174.221:7891"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val selectIp = intent.getStringExtra("ip")
        if(selectIp.isNullOrEmpty()){
            isProxy = false
        }else{
            isProxy = true
            proxyUri = "socks5://$selectIp:7891"
        }

        initWebView()

        wvView.loadUrl(uri)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT
            ) {
                backWebView()
            }
        }else{
            onBackPressedDispatcher.addCallback {
                backWebView()
            }
        }
    }

    private fun backWebView(){
        if(wvView.canGoBack()){
            wvView.goBack()
        }else{
            finish()
        }
    }


    private fun initWebView() {

        //Set WebView properties to execute Javascript scripts
        wvView.settings.javaScriptEnabled = true
        //Do not load images during webpage loading, wait until webpage loading is complete before starting
        //web_view.settings.blockNetworkImage = true
        //Do not use cache
        wvView.settings.cacheMode = WebSettings.LOAD_NO_CACHE //LOAD_NO_CACHE
        wvView.settings.domStorageEnabled = true //Enable local DOM storage to solve the problem of blank pages when loading some links
        wvView.settings.allowContentAccess = true
        //Settings can support scaling
        wvView.settings.setSupportZoom(false)
        //Set the Zoom Tool to appear
        wvView.settings.builtInZoomControls = false
        //Adapt to webview
        wvView.settings.useWideViewPort = true
        wvView.settings.loadWithOverviewMode = true
        wvView.settings.javaScriptCanOpenWindowsAutomatically = true
        //Fix in Android 5.0 and above, mixed content is disabled by default, and the security certificate is not recognized when loading some HTTPS resources
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            wvView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // chromium, enable hardware acceleration
            wvView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            // older android version, disable hardware acceleration
            wvView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        //Accelerate the completion speed of HTML webpage loading
        if (Build.VERSION.SDK_INT >= 19) {
            wvView.settings.loadsImagesAutomatically = true
        } else {
            wvView.settings.loadsImagesAutomatically = false
        }
        setProxy()

        wvView.webViewClient = object: WebViewClient(){

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                setProxy()
                view?.loadUrl(request?.url?.toString()?:"")
                return false
            }

            override fun onReceivedHttpAuthRequest(
                view: WebView?,
                handler: HttpAuthHandler?,
                host: String?,
                realm: String?
            ) {
                setProxy()
                super.onReceivedHttpAuthRequest(view, handler, host, realm)
            }

            override fun onLoadResource(view: WebView?, url: String?) {
                super.onLoadResource(view, url)
                setProxy()
            }

            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                setProxy()
                return super.shouldInterceptRequest(view, request)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                setProxy()
                super.onPageStarted(view, url, favicon)

            }

            override fun onPageFinished(view: WebView?, url: String?) {
                setProxy()
                super.onPageFinished(view, url)

            }

            override fun onPageCommitVisible(view: WebView?, url: String?) {
                setProxy()
                super.onPageCommitVisible(view, url)

            }

            override fun onReceivedLoginRequest(
                view: WebView?,
                realm: String?,
                account: String?,
                args: String?
            ) {
                setProxy()
                super.onReceivedLoginRequest(view, realm, account, args)

            }

            override fun shouldOverrideKeyEvent(view: WebView?, event: KeyEvent?): Boolean {
                setProxy()
                return super.shouldOverrideKeyEvent(view, event)
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                setProxy()
                return super.shouldOverrideUrlLoading(view, url)
            }

        }

        wvView.setWebChromeClient(object : WebChromeClient() {
            override fun onConsoleMessage(cm: ConsoleMessage): Boolean {
                return super.onConsoleMessage(cm)
            }

            override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean {
                return super.onJsAlert(view, url, message, result)
            }


            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
            }

            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
            }

        })
    }

    private fun setProxy() {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) {
            if(isProxy){
                val proxyConfig: ProxyConfig = ProxyConfig.Builder()
                    .addProxyRule(proxyUri)
                    .addDirect()
                    .build()
                ProxyController.getInstance().setProxyOverride(proxyConfig, Executor {
                    //do nothing
                    Log.i(TAG, "代理设置完成")
                }, Runnable {
                    Log.w(TAG, "WebView代理改变")
                })
            }else{
                ProxyController.getInstance().clearProxyOverride(Executor {
                    //do nothing
                    Log.i(TAG, "清除代理设置完成")
                }, Runnable {
                    Log.w(TAG, "清除代理完成")
                })
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

    }
}