package me.sweetll.tucao.extension

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import me.sweetll.tucao.AppApplication

fun ImageView.load(context: Context, url: String): Unit {
    Glide.with(AppApplication.get()).load(url).into(this)
}
