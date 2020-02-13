package me.sweetll.tucao.extension

import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputLayout
import android.widget.ImageView
import me.sweetll.tucao.R
import me.sweetll.tucao.model.other.User

object DataBindingAdapters {
    @BindingAdapter("app:avatar")
    @JvmStatic
    fun loadAvatar(imageView: ImageView, url: String?) {
        url?.let {
            imageView.load(imageView.context, it, R.drawable.default_avatar)
        }
    }

    @BindingAdapter("app:my_avatar")
    @JvmStatic
    fun loadMyAvatar(imageView: ImageView, url: String?) {
        url?.let {
            imageView.load(imageView.context, it, R.drawable.default_avatar, User.signature())
        }
    }

    @BindingAdapter("app:imageData")
    @JvmStatic
    fun loadImage(imageView: ImageView, bytes: ByteArray?) {
        bytes?.let {
            imageView.load(imageView.context, it)
        }
    }

    @BindingAdapter("app:error")
    @JvmStatic
    fun setError(textInputLayout: TextInputLayout, error: String?) {
        textInputLayout.error = error
    }
}
