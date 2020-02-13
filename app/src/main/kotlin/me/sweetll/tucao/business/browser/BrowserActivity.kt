package me.sweetll.tucao.business.browser

import android.content.Context
import android.content.Intent
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.webkit.*
import androidx.appcompat.widget.Toolbar
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseActivity
import me.sweetll.tucao.databinding.ActivityBrowserBinding

class BrowserActivity : BaseActivity() {
    lateinit var binding: ActivityBrowserBinding

    companion object {
        val ARG_URL = "url"

        fun intentTo(context: Context, url: String) {
            val intent = Intent(context, BrowserActivity::class.java)
            intent.putExtra(ARG_URL, url)
            context.startActivity(intent)
        }
    }

    override fun getStatusBar(): View = binding.statusBar

    override fun getToolbar(): Toolbar = binding.toolbar

    override fun initView(savedInstanceState: Bundle?) {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_browser)

        val url: String = intent.getStringExtra(ARG_URL)
        binding.webView.loadUrl(url)

        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.javaScriptCanOpenWindowsAutomatically = true
        binding.webView.settings.databaseEnabled = true
        binding.webView.settings.domStorageEnabled = true

        binding.webView.setWebViewClient(object: WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return false
            }
        })

        binding.webView.setWebChromeClient(object: WebChromeClient() {
            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                supportActionBar?.let {
                    it.title = title
                }
            }
        })

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && binding.webView.canGoBack()) {
            binding.webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun initToolbar() {
        super.initToolbar()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

}
