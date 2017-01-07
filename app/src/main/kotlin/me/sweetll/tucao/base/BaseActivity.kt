package me.sweetll.tucao.base

import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView(savedInstanceState)
        initToolbar()
        initStatusBar()
    }

    abstract fun initView(savedInstanceState: Bundle?)
    abstract fun getToolbar(): Toolbar
    abstract fun getStatusBar(): View

    open fun initToolbar() {
        val toolbar = getToolbar()
        setSupportActionBar(toolbar)
    }

    fun initStatusBar() {
        val statusBar = getStatusBar()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            var statusBarHeight = 0
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                statusBarHeight = resources.getDimensionPixelSize(resourceId)
            }
            statusBar.layoutParams.height = statusBarHeight
        }
    }
}
