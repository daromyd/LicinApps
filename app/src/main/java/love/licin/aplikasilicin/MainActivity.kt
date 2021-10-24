package love.licin.aplikasilicin

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import love.licin.aplikasilicin.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    companion object{
        private const val BASE_URL = "https://app.licin.love/"
    }

    var uploadMessage:ValueCallback<Array<Uri>>? = null
    private val requestCode = 1

    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initWebView()
        loadUrl()

        binding.swipe.setOnRefreshListener {
            binding.webView.reload()
            hideError()
            binding.swipe.isRefreshing = false
        }
    }

    private fun loadUrl() {
        binding.webView.loadUrl(BASE_URL)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        CookieManager.getInstance().setAcceptThirdPartyCookies(binding.webView, true)

        binding.webView.apply {
            webViewClient = object: WebViewClient(){
                @SuppressLint("NewApi")
                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    showError()
                }
            }

            settings.javaScriptEnabled = true
            settings.allowFileAccess = true
            settings.allowContentAccess = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            settings.domStorageEnabled = true
            settings.userAgentString = "random"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                settings.safeBrowsingEnabled = true
            }

            webChromeClient = object : WebChromeClient(){
                override fun onJsAlert(
                    view: WebView?,
                    url: String?,
                    message: String,
                    result: JsResult?
                ): Boolean {
                    Log.d("alert", message)
                    val dialogBuilder = AlertDialog.Builder(context)

                    dialogBuilder.setMessage(message)
                        .setCancelable(false)
                        .setPositiveButton("OK") { _, _ ->
                            result?.confirm()
                        }

                    val alert = dialogBuilder.create()
                    alert.show()

                    return true
                }

                // For Lollipop 5.0+ Devices
                override fun onShowFileChooser(
                    webView: WebView?,
                    filePathCallback: ValueCallback<Array<Uri>>?,
                    fileChooserParams: FileChooserParams?
                ): Boolean {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                        if (uploadMessage != null) {
                            uploadMessage?.onReceiveValue(null)
                            uploadMessage = null
                        }
                        uploadMessage = filePathCallback
                        val intent = fileChooserParams?.createIntent()
                            intent?.type = "image/*"
                        try {
                            startActivityForResult(intent, requestCode)
                        } catch (e: ActivityNotFoundException) {
                            uploadMessage = null
                            Toast.makeText(context, "Cannot Open File Chooser", Toast.LENGTH_LONG).show()
                            return false
                        }
                        return true
                    }else{
                        return false
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            if(requestCode == requestCode){
                if(uploadMessage != null){
                    uploadMessage?.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode,data))
                    uploadMessage = null
                }
            }
        }else{
            Toast.makeText(this,"Failed to open file uploader, please check app permissions.",Toast.LENGTH_LONG).show()
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onBackPressed() {
        if (binding.webView.canGoBack()){
            binding.webView.goBack()
        } else{
            super.onBackPressed()
        }
    }

    private fun showError(){
        binding.webView.visibility = View.GONE
        binding.linearError.visibility = View.VISIBLE

    }

    private fun hideError(){
        binding.linearError.visibility = View.GONE
        binding.webView.visibility = View.VISIBLE
    }
}