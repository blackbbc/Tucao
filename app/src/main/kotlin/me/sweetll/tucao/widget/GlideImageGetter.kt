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

        GlideApp.with(context)
                .asBitmap()
                .placeholder(R.drawable.placeholder)
                .load(url)
                .into(BitmapTarget(urlDrawable))

        return urlDrawable
    }

    inner class BitmapTarget(val urlDrawable: UrlDrawable): SimpleTarget<Bitmap>() {


        private fun setDrawable(drawable: Drawable) {
            val width = drawable.intrinsicWidth
            val height = drawable.intrinsicHeight
            val rect = Rect(0, 0, width, height)
            drawable.bounds = rect
            urlDrawable.drawable = drawable
            urlDrawable.bounds = rect

            view.invalidate() // 解决图文重叠
            view.text = view.text
        }

        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
            val w = if (resource.width > 0) resource.width else 24f.dp2px().toInt()
            val h = if (resource.height > 0) resource.height else 24f.dp2px().toInt()
            var res = resource

            view.post {
                val maxWidth = view.width - view.paddingLeft - view.paddingRight

                if (w > maxWidth) {
                    val matrix = Matrix()
                    matrix.postScale(maxWidth.toFloat() / w, maxWidth.toFloat() / w)
                    res = Bitmap.createBitmap(res, 0, 0, w, h, matrix, true)
                }

                val drawable = BitmapDrawable(context.resources, res)
                setDrawable(drawable)
            }

        }

        override fun onLoadStarted(placeholder: Drawable?) {
            placeholder?.let {
                setDrawable(it)
            }
        }

        override fun onLoadFailed(errorDrawable: Drawable?) {
            errorDrawable?.let {
                setDrawable(it)
            }
        }
    }

}
