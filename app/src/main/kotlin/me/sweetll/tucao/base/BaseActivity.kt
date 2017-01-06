package me.sweetll.tucao.base

import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView(savedInstanceState)
        initToolbar()
    }

    abstract fun initView(savedInstanceState: Bundle?)

    abstract fun getToolbar() : Toolbar

    open fun initToolbar() {
        val toolbar = getToolbar()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            var statusBarHeight = 0
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                statusBarHeight = resources.getDimensionPixelSize(resourceId)
            }
            toolbar.setPadding(toolbar.paddingLeft,
                    statusBarHeight,
                    toolbar.paddingRight,
                    toolbar.paddingBottom)
        }
        setSupportActionBar(toolbar)
    }
}
