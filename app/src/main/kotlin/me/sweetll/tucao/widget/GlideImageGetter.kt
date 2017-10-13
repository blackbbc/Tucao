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

class GlideImageGetter(val context: Context, val view: TextView): Html.ImageGetter {

    override fun getDrawable(url: String): Drawable {
        val urlDrawable = UrlDrawable()

//        val placeholder = context.resources.getDrawable(R.drawable.placeholder)
//        urlDrawable.drawable = placeholder
//        urlDrawable.bounds = Rect(0, 0, placeholder.intrinsicWidth, placeholder.intrinsicHeight)

        GlideApp.with(context)
                .asBitmap()
                .load(url)
                .into(BitmapTarget(urlDrawable))

        return urlDrawable
    }

    inner class BitmapTarget(val urlDrawable: UrlDrawable): SimpleTarget<Bitmap>() {
        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
            val w = resource.width
            val h = resource.height
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
            view.invalidate()
            view.text = view.text // 解决图文重叠
        }
    }

}
