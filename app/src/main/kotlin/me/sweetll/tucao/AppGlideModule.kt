package me.sweetll.tucao

import android.content.Context
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule

@GlideModule
class AppGlideModule: AppGlideModule() {
    override fun registerComponents(context: Context?, registry: Registry?) {
        super.registerComponents(context, registry)
    }

    override fun isManifestParsingEnabled(): Boolean {
        return false
    }
}
