package me.sweetll.tucao.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.Html
import android.widget.TextView
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import me.sweetll.tucao.GlideApp
import me.sweetll.tucao.R
import me.sweetll.tucao.extension.dp2px

class GlideImageGetter(val context: Context, val view: TextView): Html.ImageGetter {

    override fun getDrawable(url: String): Drawable {
        val urlDrawable = UrlDrawable()

//        val placeholder = context.resources.getDrawable(R.drawable.placeholder)
//        urlDrawable.drawable = placeholder
//        urlDrawable.bounds = Rect(0, 0, placeholder.intrinsicWidth, placeholder.intrinsicHeight)

        GlideApp.with(context)
                .asBitmap()
                .placeholder(R.drawable.placeholder)
                .load(url)
                .into(BitmapTarget(urlDrawable))

        return urlDrawable
    }

    inner class BitmapTarget(val urlDrawable: UrlDrawable): SimpleTarget<Bitmap>() {
        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
            val w = if (resource.width > 0) resource.width else 24f.dp2px().toInt()
            val h = if (resource.height > 0) resource.height else 24f.dp2px().toInt()
            var res = resource

            val maxWidth = view.width - view.paddingLeft - view.paddingRight

            if (w > maxWidth) {
                val matrix = Matrix()
                matrix.postScale(maxWidth.toFloat() / w, maxWidth.toFloat() / w)
                res = Bitmap.createBitmap(res, 0, 0, w, h, matrix, true)
            }

            val drawable = BitmapDrawable(context.resources, res)
            val ww = drawable.intrinsicWidth
            val hh = drawable.intrinsicHeight
            val rect = Rect(0, 0, ww, hh)
            drawable.bounds = rect
            urlDrawable.bounds = rect
            urlDrawable.drawable = drawable

            view.invalidate() // 解决图文重叠
            view.text = view.text
        }
    }

}
