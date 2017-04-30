package me.sweetll.tucao.extension

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import me.sweetll.tucao.AppApplication

fun ImageView.load(context: Context, url: String): Unit {
    Glide.with(context).load(url).into(this)
}
