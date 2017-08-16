package me.sweetll.tucao.extension

import android.databinding.BindingAdapter
import android.widget.ImageView
import me.sweetll.tucao.R

object DataBindingAdapters {
    @BindingAdapter("app:avatar")
    @JvmStatic
    fun loadImage(imageView: ImageView, url: String?) {
        url?.let {
            imageView.load(imageView.context, it, R.drawable.default_avatar)
        }
    }

    @BindingAdapter("app:imageData")
    @JvmStatic
    fun loadImage(imageView: ImageView, bytes: ByteArray?) {
        bytes?.let {
            imageView.load(imageView.context, it)
        }
    }
}
