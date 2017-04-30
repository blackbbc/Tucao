package me.sweetll.tucao.extension

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.stream.StreamModelLoader
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import me.sweetll.tucao.AppApplication
import java.io.IOException
import java.io.InputStream
import java.lang.Exception

fun ImageView.load(context: Context, url: String): Unit {
    Glide.with(context)
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
            .into(this)
}

fun ImageView.load(context: Context, url: String, completeCallback: () -> Unit) {
    Glide.with(context)
            .using(cacheOnlyStreamLoader)
            .load(url)
            .dontAnimate()
            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
            .listener(object : RequestListener<String, GlideDrawable> {
                override fun onException(e: Exception?, model: String?, target: Target<GlideDrawable>?, isFirstResource: Boolean): Boolean {
                    completeCallback()
                    return false
                }

                override fun onResourceReady(resource: GlideDrawable?, model: String?, target: Target<GlideDrawable>?, isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
                    completeCallback()
                    return false
                }

            })
            .into(this)
}

val cacheOnlyStreamLoader = StreamModelLoader<String> {
    model, _, _ ->
    object: DataFetcher<InputStream> {
        override fun cancel() {

        }

        override fun cleanup() {

        }

        override fun getId(): String {
            return model
        }

        override fun loadData(priority: Priority?): InputStream {
            throw IOException()
        }
    }
}

