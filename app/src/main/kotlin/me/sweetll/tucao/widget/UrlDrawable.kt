package me.sweetll.tucao.widget

import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable

class UrlDrawable: BitmapDrawable() {
    var drawable: Drawable? = null

    override fun draw(canvas: Canvas?) {
        drawable?.draw(canvas)
    }
}
