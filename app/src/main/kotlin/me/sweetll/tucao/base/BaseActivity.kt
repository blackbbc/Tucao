package me.sweetll.tucao.base

import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        initView(savedInstanceState)
//        initToolbar()
    }

    abstract fun initView(savedInstanceState: Bundle?)

    abstract fun getToolbar() : Toolbar

    private fun initToolbar() {
        val toolbar = getToolbar()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val rectangle = Rect()
            window.decorView.getWindowVisibleDisplayFrame(rectangle)
            val statusBarHeight = rectangle.top
//            val contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).top
//            val titleBarHeight = contentViewTop - statusBarHeight
            toolbar.setPadding(toolbar.paddingLeft,
                    statusBarHeight,
                    toolbar.paddingRight,
                    toolbar.paddingBottom)
        }
//        setSupportActionBar(toolbar)
    }
}
