package me.sweetll.tucao.extension

import android.widget.ImageView
import com.bumptech.glide.Glide
import me.sweetll.tucao.AppApplication

fun ImageView.load(url: String): Unit {
    Glide.with(AppApplication.get()).load(url).into(this)
}
